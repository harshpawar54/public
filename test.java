import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameReader {
    public static void main(String[] args) {
        String hostname = "Unknown Host";

        try {
            // Try getting the hostname using InetAddress
            InetAddress inetAddress = InetAddress.getLocalHost();
            hostname = inetAddress.getHostName();
        } catch (UnknownHostException e) {
            System.err.println("Failed to resolve hostname using InetAddress: " + e.getMessage());
            
            // Fallback to environment variables
            hostname = System.getenv("COMPUTERNAME"); // Works on Windows
            if (hostname == null || hostname.isEmpty()) {
                hostname = System.getenv("HOSTNAME"); // Works on Unix-based systems
            }
        }

        System.out.println("Hostname: " + hostname);
    }
}