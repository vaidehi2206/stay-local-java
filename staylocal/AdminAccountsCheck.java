package staylocal;

/**
 * Phase 22 check main for the admin's account powers: issue an
 * account, refuse a duplicate, revoke without erasing the record,
 * reactivate, and force an MFA reset.
 * Run this file in NetBeans: every test should print PASS.
 */
public class AdminAccountsCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) {
        AdminUsers.Registry r = new AdminUsers.Registry();
        check("T1 the registry starts from the six demo accounts",
                r.users().size() == 6 && r.activeCount() == 6,
                r.users().size() + " accounts, all active");

        boolean added = r.addUser(new AdminUsers.User("worker.skim", "S. Kim (demo)",
                AdminUsers.ROLE_WORKER, "Outreach team", false));
        boolean dup = r.addUser(new AdminUsers.User("worker.skim", "Duplicate",
                AdminUsers.ROLE_WORKER, "Outreach team", false));
        check("T2 a new account is issued and a duplicate username is refused",
                added && !dup && r.users().size() == 7,
                "registry now holds " + r.users().size());

        r.setActive("worker.jlee", false);
        AdminUsers.User jlee = r.find("worker.jlee");
        check("T3 revoking keeps the record, marked REVOKED",
                jlee != null && !jlee.active && jlee.describe().contains("REVOKED")
                        && r.activeCount() == 6,
                "active " + r.activeCount() + " of " + r.users().size());

        r.setActive("worker.jlee", true);
        check("T4 reactivation restores the account",
                jlee.active && r.activeCount() == 7,
                "active " + r.activeCount());

        int before = AdminUsers.withoutMfa(r.users());
        r.resetMfa("worker.mpatel");
        check("T5 an MFA reset forces re-enrolment",
                AdminUsers.withoutMfa(r.users()) == before + 1
                        && !r.setActive("nobody.here", false),
                "accounts without MFA: " + AdminUsers.withoutMfa(r.users()));

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
