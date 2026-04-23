import java.net.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class ScannerEngine {
    private final ExecutorService executor;
    private final Set<String> launchedIps = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final VLCLauncher vlcLauncher = new VLCLauncher();

    public ScannerEngine(int threadCount) {
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public void scanAddress(String ip, int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            final int currentPort = port;
            executor.submit(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(ip, currentPort), 200);
                    System.out.printf("[OPEN] %-15s | Port: %d%n", ip, currentPort);

                    // Logic to trigger VLC
                    if (currentPort == 1935 && launchedIps.add(ip)) {
                        System.out.println(">>> Found Stream on " + ip + ". Launching VLC...");
                        vlcLauncher.openNetworkStream(ip);
                    }
                } catch (IOException ignored) {}
            });
        }
    }

    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}