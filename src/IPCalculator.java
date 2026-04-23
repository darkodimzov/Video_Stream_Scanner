import java.net.*;
import java.util.*;

public class IPCalculator {

    public static String getLocalIpRange() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    if (!(addr.getAddress() instanceof Inet4Address)) continue;
                    short maskLen = addr.getNetworkPrefixLength();
                    if (maskLen > 0 && maskLen < 32) {
                        return addr.getAddress().getHostAddress() + "/" + maskLen;
                    }
                }
            }
        } catch (SocketException ignored) {}
        return null;
    }

    public static List<String> getIpsFromCidr(String cidr) {
        List<String> ips = new ArrayList<>();
        String[] parts = cidr.split("/");
        long ipLong = ipToLong(parts[0]);
        int prefix = Integer.parseInt(parts[1]);
        long mask = 0xFFFFFFFFL << (32 - prefix);
        long startIp = (ipLong & mask);
        long endIp = startIp | (~mask & 0xFFFFFFFFL);
        for (long i = startIp + 1; i < endIp; i++) ips.add(longToIp(i));
        return ips;
    }

    public static List<String> getIpsFromRange(String startIp, String endIp) {
        List<String> ips = new ArrayList<>();
        for (long i = ipToLong(startIp); i <= ipToLong(endIp); i++) {
            ips.add(longToIp(i));
        }
        return ips;
    }

    private static long ipToLong(String ipAddress) {
        String[] atoms = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (Long.parseLong(atoms[i]) << (24 - (i * 8)));
        }
        return result & 0xFFFFFFFFL;
    }

    private static String longToIp(long i) {
        return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." + (i & 0xFF);
    }
}