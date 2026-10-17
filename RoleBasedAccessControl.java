import java.util.*;

/**
 * Demonstration of role-based access control (RBAC) in a Java console application.
 * Roles: ADMINISTRATION, STAFF, GUEST.
 * Demo credentials:
 *   admin / Admin@123
 *   staff / Staff@123
 *   guest / Guest@123
 */
public class RoleBasedAccessControl {
    enum Role { ADMINISTRATION, STAFF, GUEST }
    enum Permission {
        VIEW_PUBLIC_INFO,
        VIEW_OWN_PROFILE,
        UPDATE_OWN_PROFILE,
        VIEW_STAFF_DIRECTORY,
        VIEW_REPORTS,
        MANAGE_USERS,
        SYSTEM_SETTINGS
    }

    static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = new EnumMap<>(Role.class);
    static final Map<String, User> USERS = new LinkedHashMap<>();
    static final Scanner SCANNER = new Scanner(System.in);

    static {
        ROLE_PERMISSIONS.put(Role.ADMINISTRATION, EnumSet.allOf(Permission.class));
        ROLE_PERMISSIONS.put(Role.STAFF, EnumSet.of(
                Permission.VIEW_PUBLIC_INFO,
                Permission.VIEW_OWN_PROFILE,
                Permission.UPDATE_OWN_PROFILE,
                Permission.VIEW_STAFF_DIRECTORY
        ));
        ROLE_PERMISSIONS.put(Role.GUEST, EnumSet.of(Permission.VIEW_PUBLIC_INFO));

        USERS.put("admin", new User("admin", "Admin@123", Role.ADMINISTRATION, "System Administrator"));
        USERS.put("staff", new User("staff", "Staff@123", Role.STAFF, "Staff Member"));
        USERS.put("guest", new User("guest", "Guest@123", Role.GUEST, "Guest Visitor"));
    }

    static class User {
        final String username;
        final String password; // Demo only: real applications must hash passwords.
        final Role role;
        String displayName;

        User(String username, String password, Role role, String displayName) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.displayName = displayName;
        }
    }

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println(" Role-Based Access Control (RBAC) Demo");
        System.out.println("============================================");
        User currentUser = login();
        if (currentUser == null) {
            System.out.println("Too many failed login attempts. Exiting.");
            return;
        }
        System.out.printf("\nWelcome, %s! Your role is %s.%n", currentUser.displayName, currentUser.role);

        boolean running = true;
        while (running) {
            showMenu(currentUser);
            System.out.print("Choose an option: ");
            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1" -> viewPublicInfo();
                case "2" -> viewOwnProfile(currentUser);
                case "3" -> updateOwnProfile(currentUser);
                case "4" -> viewStaffDirectory(currentUser);
                case "5" -> viewReports(currentUser);
                case "6" -> manageUsers(currentUser);
                case "7" -> systemSettings(currentUser);
                case "0" -> {
                    System.out.println("You have logged out. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose a listed option.");
            }
            System.out.println();
        }
    }

    private static User login() {
        for (int attempts = 1; attempts <= 3; attempts++) {
            System.out.print("Username: ");
            String username = SCANNER.nextLine().trim();
            System.out.print("Password: ");
            String password = SCANNER.nextLine();
            User user = USERS.get(username);
            if (user != null && user.password.equals(password)) return user;
            System.out.println("Invalid username or password. Attempts remaining: " + (3 - attempts));
        }
        return null;
    }

    private static void showMenu(User user) {
        System.out.println("--- Available functions ---");
        menuItem(user, "1", "View public information", Permission.VIEW_PUBLIC_INFO);
        menuItem(user, "2", "View own profile", Permission.VIEW_OWN_PROFILE);
        menuItem(user, "3", "Update own profile", Permission.UPDATE_OWN_PROFILE);
        menuItem(user, "4", "View staff directory", Permission.VIEW_STAFF_DIRECTORY);
        menuItem(user, "5", "View reports", Permission.VIEW_REPORTS);
        menuItem(user, "6", "Manage users", Permission.MANAGE_USERS);
        menuItem(user, "7", "System settings", Permission.SYSTEM_SETTINGS);
        System.out.println("0. Logout");
    }

    private static void menuItem(User user, String number, String label, Permission permission) {
        if (hasPermission(user, permission)) System.out.println(number + ". " + label);
        else System.out.println(number + ". " + label + " [restricted]");
    }

    // Central authorization check: every protected action calls this method.
    private static boolean hasPermission(User user, Permission permission) {
        if (!ROLE_PERMISSIONS.getOrDefault(user.role, Collections.emptySet()).contains(permission)) {
            System.out.println("ACCESS DENIED: Your role (" + user.role + ") does not permit this action.");
            return false;
        }
        return true;
    }

    private static void viewPublicInfo() {
        System.out.println("Public information: Welcome to the organization portal.");
        System.out.println("Office hours: Monday-Friday, 9:00 AM-4:00 PM.");
    }

    private static void viewOwnProfile(User user) {
        if (!hasPermission(user, Permission.VIEW_OWN_PROFILE)) return;
        System.out.println("Username: " + user.username);
        System.out.println("Display name: " + user.displayName);
        System.out.println("Role: " + user.role);
    }

    private static void updateOwnProfile(User user) {
        if (!hasPermission(user, Permission.UPDATE_OWN_PROFILE)) return;
        System.out.print("Enter your new display name: ");
        String name = SCANNER.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name was not changed because it cannot be empty.");
            return;
        }
        user.displayName = name;
        System.out.println("Your profile has been updated.");
    }

    private static void viewStaffDirectory(User user) {
        if (!hasPermission(user, Permission.VIEW_STAFF_DIRECTORY)) return;
        System.out.println("Staff directory:");
        for (User account : USERS.values()) {
            if (account.role == Role.STAFF || account.role == Role.ADMINISTRATION) {
                System.out.println("- " + account.displayName + " (" + account.role + ")");
            }
        }
    }

    private static void viewReports(User user) {
        if (!hasPermission(user, Permission.VIEW_REPORTS)) return;
        long admins = USERS.values().stream().filter(u -> u.role == Role.ADMINISTRATION).count();
        long staff = USERS.values().stream().filter(u -> u.role == Role.STAFF).count();
        long guests = USERS.values().stream().filter(u -> u.role == Role.GUEST).count();
        System.out.println("User report: Administration=" + admins + ", Staff=" + staff + ", Guest=" + guests);
    }

    private static void manageUsers(User user) {
        if (!hasPermission(user, Permission.MANAGE_USERS)) return;
        System.out.println("Registered accounts:");
        for (User account : USERS.values()) {
            System.out.println("- " + account.username + " | " + account.role + " | " + account.displayName);
        }
        System.out.print("Create a staff account? (y/n): ");
        String answer = SCANNER.nextLine().trim();
        if (!answer.equalsIgnoreCase("y")) return;
        System.out.print("New username: ");
        String username = SCANNER.nextLine().trim();
        if (username.isEmpty() || USERS.containsKey(username)) {
            System.out.println("Username is empty or already exists.");
            return;
        }
        System.out.print("New password: ");
        String password = SCANNER.nextLine();
        System.out.print("Display name: ");
        String displayName = SCANNER.nextLine().trim();
        if (password.isEmpty() || displayName.isEmpty()) {
            System.out.println("Account not created: password and display name are required.");
            return;
        }
        USERS.put(username, new User(username, password, Role.STAFF, displayName));
        System.out.println("Staff account created successfully.");
    }

    private static void systemSettings(User user) {
        if (!hasPermission(user, Permission.SYSTEM_SETTINGS)) return;
        System.out.println("System settings: demo mode is enabled. No persistent settings are changed.");
    }
}
