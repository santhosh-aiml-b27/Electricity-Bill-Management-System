package model;

/**
 * Represents an administrator in the Electricity Bill Management System.
 */
public class Admin {
    private String username;
    private String password;
    private String fullName;
    private String role;

    public Admin(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Serializes this Admin object to a pipe-delimited string.
     */
    public String toFileString() {
        return username + "|" + password + "|" + fullName + "|" + role;
    }

    /**
     * Deserializes a pipe-delimited string into an Admin object.
     */
    public static Admin fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 4) {
            return new Admin(parts[0], parts[1], parts[2], parts[3]);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
