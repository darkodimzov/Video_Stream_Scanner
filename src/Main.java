import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Network Stream Scanner ---");

        // 1. Get Range
        String rangeStr = IPCalculator.getLocalIpRange();
        if (rangeStr == null) {
            System.out.print("\nEnter Target (IP/CIDR/Range): ");
            rangeStr = scanner.nextLine();
        } else {
            System.out.println("\nScanning Local Subnet: " + rangeStr);
        }

        // 2. Prepare IP List
        List<String> targets;
        if (rangeStr.contains("/")) targets = IPCalculator.getIpsFromCidr(rangeStr);
        else if (rangeStr.contains("-")) {
            String[] parts = rangeStr.split("-");
            targets = IPCalculator.getIpsFromRange(parts[0].trim(), parts[1].trim());
        } else targets = Collections.singletonList(rangeStr);

        // 3. Start Scanner
        ScannerEngine engine = new ScannerEngine(250);
        System.out.println("\nStarting scan on " + targets.size() + " hosts...");

        for (String ip : targets) {
            engine.scanAddress(ip, 1, 8080);
        }

        engine.stop();
        scanner.close();
    }
}