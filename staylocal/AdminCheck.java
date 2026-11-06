package staylocal;

import java.util.List;
import java.util.Random;

/**
 * Phase 8 check main for the admin dashboard's logic: the daily
 * statistics report (AdminStats), the authorization registry
 * (AdminUsers), and the MFA code step (MfaDemo).
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class AdminCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    static BedPool byName(List<BedPool> pools, String name) {
        for (BedPool p : pools) {
            if (p.siteName.equals(name)) {
                return p;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();
        HoldManager m = new HoldManager(pools, 90);

        check("T1 total beds stated open", AdminStats.statedOpenBeds(pools) == 152,
                "expected 152, got " + AdminStats.statedOpenBeds(pools));

        String report = AdminStats.dailyReport(pools, m, "November 5, 2026");
        boolean directoryOk = report.contains("Total shelters listed: 107")
                && report.contains("Cities covered: 15")
                && report.contains("Open sites: 80")
                && report.contains("Closed sites: 27")
                && report.contains("Shelters stating open beds: 22")
                && report.contains("Total beds stated open: 152")
                && report.contains("Listed full: 25")
                && report.contains("No bed information stated: 37")
                && report.contains("Vancouver: 48")
                && report.contains("Holds placed: 0");
        check("T2 daily report directory figures", directoryOk,
                "report contains all directory lines: " + directoryOk);

        // Scripted activity across the other two dashboards:
        // worker places 3 holds; one checks in, one stays held,
        // the shelter rejects one.
        BedPool beacon = byName(pools, "The Beacon");
        BedPool yukon = byName(pools, "Yukon Shelter");
        BedPool haven = byName(pools, "The Haven");
        Hold h1 = m.placeHold(beacon.id, "Worker demo");
        m.confirmHold(h1);
        m.checkIn(h1);
        m.placeHold(yukon.id, "Worker demo");
        Hold h3 = m.placeHold(haven.id, "Worker demo");
        m.rejectHold(h3, "No holds over phone - walk in");

        String report2 = AdminStats.dailyReport(pools, m, "November 5, 2026");
        boolean activityOk = report2.contains("Holds placed: 3")
                && report2.contains("Held now: 1")
                && report2.contains("Checked in: 1")
                && report2.contains("Rejected: 1")
                && report2.contains("Beds open in this session right now: 150");
        check("T3 daily report activity figures", activityOk,
                "3 holds -> checked in 1, held 1, rejected 1; session open 152 -> "
                        + AdminStats.sessionOpenBeds(pools));

        List<AdminUsers.User> users = AdminUsers.sampleUsers();
        boolean usersOk = users.size() == 6
                && AdminUsers.countByRole(users, AdminUsers.ROLE_ADMIN) == 1
                && AdminUsers.countByRole(users, AdminUsers.ROLE_WORKER) == 3
                && AdminUsers.countByRole(users, AdminUsers.ROLE_SHELTER) == 2
                && AdminUsers.withoutMfa(users) == 1;
        check("T4 authorization registry", usersOk,
                "6 demo accounts: 1 admin, 3 workers, 2 shelters, 1 without MFA");

        String code = MfaDemo.newCode(new Random(42));
        boolean codeOk = code.length() == 6
                && code.chars().allMatch(Character::isDigit)
                && MfaDemo.matches("  " + code + " ", code)
                && !MfaDemo.matches("000000".equals(code) ? "111111" : "000000", code)
                && !MfaDemo.matches(null, code);
        check("T5 MFA code step", codeOk,
                "6-digit code, exact match required (spaces trimmed)");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
