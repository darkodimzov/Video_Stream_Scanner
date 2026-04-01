import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Port_Scan {

    private static final Map<Integer, String> SERVICES = new HashMap<>();

    static {
        SERVICES.put(1, "tcpmux");
        SERVICES.put(20, "ftp-data");
        SERVICES.put(21, "ftp");
        SERVICES.put(22, "ssh");
        SERVICES.put(23, "telnet");
        SERVICES.put(25, "smtp");
        SERVICES.put(53, "dns");
        SERVICES.put(80, "http");
        SERVICES.put(110, "pop3");
        SERVICES.put(111, "rpcbind");
        SERVICES.put(143, "imap");
        SERVICES.put(443, "https");
        SERVICES.put(554, "RTSP");
        SERVICES.put(993, "imaps");
        SERVICES.put(995, "pop3s");
        SERVICES.put(1935, "RTMP");
        SERVICES.put(1723, "pptp");
        SERVICES.put(3306, "mysql");
        SERVICES.put(3389, "ms-wbt-server");
        SERVICES.put(5432, "postgresql");
        SERVICES.put(5900, "vnc");
        SERVICES.put(8080, "http-proxy");
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("\n--- Network Video Stream Scanner ---\n");
        System.out.println("Enter target (Single IP, Subnet/24, or Range 1.1.1.1-1.1.1.10): ");
        String input = in.nextLine().trim();

        List<String> ipList = new ArrayList<>();

        try {
            if (input.contains("/")) {
                // Handle CIDR: 192.168.1.0/24
                ipList = getIpsFromCidr(input);
            } else if (input.contains("-")) {
                // Handle Range: 192.168.1.1-192.168.1.50
                String[] range = input.split("-");
                ipList = getIpsFromRange(range[0].trim(), range[1].trim());
            } else {
                // Handle Single IP: 192.168.1.1
                ipList.add(input);
            }

            System.out.print("\nEnter Port Range (e.g., 1935 or 554-1935): ");
            String portInput = in.nextLine();

            int startPort = 1, endPort = 8080;
            if (!portInput.isEmpty()) {
                if (portInput.contains("-")) {
                    String[] range = portInput.split("-");
                    startPort = Integer.parseInt(range[0]);
                    endPort = Integer.parseInt(range[1]);
                } else {
                    startPort = endPort = Integer.parseInt(portInput);
                }
            }

            System.out.println("\nScanning " + ipList.size() + " host(s)...\n");
            for (String ip : ipList) {
                startScan(ip, startPort, endPort);
            }

        } catch (Exception e) {
            System.err.println("Error parsing input: " + e.getMessage());
        }

        in.close();
    }

    // New helper for dash-separated ranges
    private static List<String> getIpsFromRange(String startIp, String endIp) {
        List<String> ips = new ArrayList<>();
        long start = ipToLong(startIp);
        long end = ipToLong(endIp);
        for (long i = start; i <= end; i++) {
            ips.add(longToIp(i));
        }
        return ips;
    }

    private static List<String> getIpsFromCidr(String cidr) {
        List<String> ips = new ArrayList<>();
        String[] parts = cidr.split("/");
        String ip = parts[0];
        int prefix = Integer.parseInt(parts[1]);

        long mask = 0xFFFFFFFFL << (32 - prefix);
        long ipLong = ipToLong(ip);

        long startIp = (ipLong & mask);
        long endIp = startIp | (~mask & 0xFFFFFFFFL);

        for (long i = startIp; i <= endIp; i++) {
            ips.add(longToIp(i));
        }
        return ips;
    }

    private static void startScan(String ip, int startPort, int endPort) {
        // Reuse a thread pool to avoid overhead per IP
        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int p = startPort; p <= endPort; p++) {
            final int sport = p;
            executor.submit(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(ip, sport), 300);
                    String service = SERVICES.getOrDefault(sport, "unknown");
                    System.out.printf("[%s] %d/tcp open %s%n", ip, sport, service);

                    if (sport == 1935 || sport == 554) {
                        new VLCLauncher().openNetworkStream(ip);
                    }
                } catch (Exception ignored) {}
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }

    // Using Long instead of Int to avoid signed-bit issues with IP addresses
    private static long ipToLong(String ipAddress) {
        String[] atoms = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (Long.parseLong(atoms[i]) << (24 - (i * 8)));
        }
        return result & 0xFFFFFFFFL;
    }

    private static String longToIp(long i) {
        return ((i >> 24) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                (i & 0xFF);
    }
}