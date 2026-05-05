import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Network Stream Scanner ---");

        String rangeStr = "";

        // 1. Detect Local Network and Ask Permission
        String detectedRange = IPCalculator.getLocalIpRange();

        if (detectedRange != null) {
            System.out.println("\nDetected Local Subnet: " + detectedRange);
            System.out.print("Would you like to scan this network? (Y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.isEmpty() || choice.startsWith("y")) {
                rangeStr = detectedRange;
            }
        }

        // 2. Fallback to Manual Entry
        if (rangeStr.isEmpty()) {
            System.out.print("\nEnter Target (Single IP, CIDR or Range): ");
            rangeStr = scanner.nextLine().trim();
        }

        // 3. Ask for Port Range
        System.out.print("\nEnter Port Range (e.g., 80, 1-1935): ");
        String portInput = scanner.nextLine().trim();

        int startPort = 1;
        int endPort = 8080;

        try {
            if (!portInput.isEmpty()) {
                if (portInput.contains("-")) {
                    String[] pParts = portInput.split("-");
                    startPort = Integer.parseInt(pParts[0].trim());
                    endPort = Integer.parseInt(pParts[1].trim());
                } else {
                    startPort = endPort = Integer.parseInt(portInput);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid port format. Using default range 1-8080.");
        }

        // 4. Prepare IP List
        List<String> targets = new ArrayList<>();
        try {
            if (rangeStr.contains("/")) {
                targets = IPCalculator.getIpsFromCidr(rangeStr);
            } else if (rangeStr.contains("-")) {
                String[] parts = rangeStr.split("-");
                targets = IPCalculator.getIpsFromRange(parts[0].trim(), parts[1].trim());
            } else {
                targets = Collections.singletonList(rangeStr);
            }
        } catch (Exception e) {
            System.err.println("\nError parsing network input: " + e.getMessage());
            return;
        }

        // 5. Start Scanner
        if (!targets.isEmpty()) {
            ScannerEngine engine = new ScannerEngine(250);
            System.out.println("\nScanning " + targets.size() + " host(s) on ports " + startPort + "-" + endPort + "...");

            for (String ip : targets) {
                engine.scanAddress(ip, startPort, endPort);
            }

            engine.stop();
        } else {
            System.out.println("\nNo targets to scan.");
        }

        scanner.close();
    }
}