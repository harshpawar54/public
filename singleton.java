import java.util.HashMap;
import java.util.Map;

public class DatabaseCredentialStore {

    

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


public class Main {
    public static void main(String[] args) {
        // Get the singleton instance
        DatabaseCredentialStore credentialStore = DatabaseCredentialStore.getInstance();

        // Create credentials
        DatabaseCredentialStore.DatabaseCredentials credentials1 =
                new DatabaseCredentialStore.DatabaseCredentials("user1", "password1");

        DatabaseCredentialStore.DatabaseCredentials credentials2 =
                new DatabaseCredentialStore.DatabaseCredentials("user2", "password2");

        // Add credentials for hostnames
        credentialStore.addCredentials("db1.example.com", credentials1);
        credentialStore.addCredentials("db2.example.com", credentials2);

        // Retrieve and print credentials for a hostname
        DatabaseCredentialStore.DatabaseCredentials db1Credentials =
                credentialStore.getCredentials("db1.example.com");

        System.out.println("Credentials for db1.example.com: " + db1Credentials);

        // Check if credentials exist
        System.out.println("Has credentials for db2.example.com? " +
                           credentialStore.hasCredentials("db2.example.com"));

        System.out.println("Has credentials for db3.example.com? " +
                           credentialStore.hasCredentials("db3.example.com"));
    }
}