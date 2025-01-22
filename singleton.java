import java.util.HashMap;
import java.util.Map;

public class DatabaseCredentialStore {

    // Private static instance of the class
    private static DatabaseCredentialStore instance;

    // Map to store hostnames against database credentials
    private final Map<String, DatabaseCredentials> credentialMap;

    // Private constructor to prevent instantiation from outside
    private DatabaseCredentialStore() {
        credentialMap = new HashMap<>();
    }

    // Static method to get the single instance
    public static synchronized DatabaseCredentialStore getInstance() {
        if (instance == null) {
            instance = new DatabaseCredentialStore();
        }
        return instance;
    }

    // Method to add credentials for a hostname
    public void addCredentials(String hostname, DatabaseCredentials credentials) {
        credentialMap.put(hostname, credentials);
    }

    // Method to retrieve credentials for a hostname
    public DatabaseCredentials getCredentials(String hostname) {
        return credentialMap.get(hostname);
    }

    // Method to check if credentials exist for a hostname
    public boolean hasCredentials(String hostname) {
        return credentialMap.containsKey(hostname);
    }

    // Nested class to represent database credentials
    public static class DatabaseCredentials {
        private final String username;
        private final String password;

        public DatabaseCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            return "DatabaseCredentials{" +
                   "username='" + username + '\'' +
                   ", password='*****'" +
                   '}';
        }
    }
}