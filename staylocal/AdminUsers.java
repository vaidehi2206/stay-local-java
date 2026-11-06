package staylocal;

import java.util.ArrayList;
import java.util.List;

/**
 * The admin dashboard's authorization registry: who is allowed into
 * which dashboard, and whether their account has MFA on.
 *
 * LOCAL DEMO ONLY: these are sample accounts so the admin screen has
 * something real to show. In production this registry lives on the
 * server, accounts are issued by the admin after verifying the worker
 * or shelter, and MFA is enforced at sign-in — a local app cannot
 * honestly do any of that, so it simulates the shape, not the security.
 */
public class AdminUsers {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_WORKER = "HEALTH WORKER";
    public static final String ROLE_SHELTER = "SHELTER";

    public static class User {
        public String username;
        public String displayName;
        public String role;
        public String organization;
        public boolean mfaOn;
        /** Phase 22: revoked accounts stay on the record but cannot sign in. */
        public boolean active = true;

        public User(String username, String displayName, String role,
                String organization, boolean mfaOn) {
            this.username = username;
            this.displayName = displayName;
            this.role = role;
            this.organization = organization;
            this.mfaOn = mfaOn;
        }

        public String describe() {
            return username + "  -  " + displayName + "  -  " + role
                    + "  -  " + organization + "  -  MFA " + (mfaOn ? "on" : "OFF")
                    + "  -  " + (active ? "ACTIVE" : "REVOKED");
        }
    }

    public static List<User> sampleUsers() {
        List<User> users = new ArrayList<User>();
        users.add(new User("admin.demo", "App Administrator (demo)", ROLE_ADMIN,
                "Stay Local operations", true));
        users.add(new User("worker.jlee", "J. Lee (demo)", ROLE_WORKER,
                "Outreach team", true));
        users.add(new User("worker.mpatel", "M. Patel (demo)", ROLE_WORKER,
                "Outreach team", true));
        users.add(new User("worker.tnovak", "T. Novak (demo)", ROLE_WORKER,
                "Hospital discharge team", true));
        users.add(new User("shelter.beacon", "The Beacon (demo account)", ROLE_SHELTER,
                "The Beacon, Vancouver", true));
        users.add(new User("shelter.yukon", "Yukon Shelter (demo account)", ROLE_SHELTER,
                "Yukon Shelter, Vancouver", false));
        return users;
    }

    public static int countByRole(List<User> users, String role) {
        int n = 0;
        for (User u : users) {
            if (u.role.equals(role)) {
                n++;
            }
        }
        return n;
    }

    /**
     * Phase 22: the live registry the admin screen acts on. Starts
     * from the demo accounts; the admin can add accounts, revoke or
     * reactivate them, and force an MFA reset. Still a local demo:
     * production keeps this on the server behind real sign-in.
     */
    public static class Registry {
        private final List<User> users = sampleUsers();

        public List<User> users() {
            return users;
        }

        public User find(String username) {
            if (username == null) {
                return null;
            }
            for (User u : users) {
                if (u.username.equalsIgnoreCase(username.trim())) {
                    return u;
                }
            }
            return null;
        }

        /** False when the username is blank or already registered. */
        public boolean addUser(User u) {
            if (u == null || u.username == null || u.username.trim().isEmpty()
                    || find(u.username) != null) {
                return false;
            }
            users.add(u);
            return true;
        }

        /** Revoking keeps the account on the record, marked REVOKED. */
        public boolean setActive(String username, boolean active) {
            User u = find(username);
            if (u == null) {
                return false;
            }
            u.active = active;
            return true;
        }

        /** The account must re-enrol MFA at its next sign-in (demo). */
        public boolean resetMfa(String username) {
            User u = find(username);
            if (u == null) {
                return false;
            }
            u.mfaOn = false;
            return true;
        }

        public int activeCount() {
            int n = 0;
            for (User u : users) {
                if (u.active) {
                    n++;
                }
            }
            return n;
        }
    }

    public static int withoutMfa(List<User> users) {
        int n = 0;
        for (User u : users) {
            if (!u.mfaOn) {
                n++;
            }
        }
        return n;
    }
}
