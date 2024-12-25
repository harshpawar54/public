public class DetectOS {
    public static void main(String[] args) {
        // Get the operating system name
        String osName = System.getProperty("os.name").toLowerCase();

        // Determine the operating system
        if (osName.contains("win")) {
            System.out.println("The operating system is Windows.");
        } else if (osName.contains("mac")) {
            System.out.println("The operating system is macOS.");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            System.out.println("The operating system is Unix/Linux.");
        } else if (osName.contains("sunos")) {
            System.out.println("The operating system is Solaris.");
        } else {
            System.out.println("Unknown operating system.");
        }
    }
}