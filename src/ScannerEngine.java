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
                    socket.connect(new InetSocketAddress(ip, currentPort), 250);
                    handleOpenPort(ip, currentPort);
                } catch (IOException ignored) {}
            });
        }
    }

    private void handleOpenPort(String ip, int port) {
        System.out.printf("[OPEN] %-15s | Port: %d%n", ip, port);

        // RTSP / RTMP
        if ((port == 554 || port == 1935) && launchedIps.add(ip)) {
            vlcLauncher.openNetworkStream(ip, port);
        }
        // HTTP / HTTPS
        else if (port == 80 || port == 443 || port == 8000 || port == 8080) {
            if (StreamDetector.isHttpVideo(ip, port) && launchedIps.add(ip)) {
                System.out.println(">>> Found HTTP Stream on " + ip);
                vlcLauncher.openNetworkStream(ip, port);
            }
        }
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}