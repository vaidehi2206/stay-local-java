package staylocal;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 23 check main for directory honesty: the snapshot's age is
 * computed and called out when stale, "mark verified" resets a
 * listing's freshness and lands in the audit log, and the admin
 * report states the snapshot's age.
 * Run this file in NetBeans: every test should print PASS.
 */
public class DirectoryCheck {

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
        check("T1 snapshot age counts from November 5, 2026",
                DirectoryInfo.ageDays(LocalDate.of(2026, 11, 5)) == 0
                        && DirectoryInfo.ageDays(LocalDate.of(2026, 11, 6)) == 1
                        && DirectoryInfo.ageDays(LocalDate.of(2026, 12, 5)) == 30,
                "0 / 1 / 30 days");

        check("T2 a snapshot over a week old is called stale",
                !DirectoryInfo.isStale(LocalDate.of(2026, 11, 12))
                        && DirectoryInfo.isStale(LocalDate.of(2026, 11, 13))
                        && DirectoryInfo.statusLine(LocalDate.of(2026, 11, 5)).contains("retrieved today"),
                "day 7 fine, day 8 stale");

        List<BedPool> pools = ShelterData.allShelters();
        HoldManager m = new HoldManager(pools, 90);
        BedPool beacon = byName(pools, "The Beacon");
        beacon.lastUpdatedMinAgo = 500;
        boolean wasCall = "call to verify".equals(beacon.freshness());
        m.markVerified(beacon.id);
        check("T3 mark-verified resets freshness and is audited",
                wasCall && beacon.lastUpdatedMinAgo == 0 && "fresh".equals(beacon.freshness())
                        && m.eventLog().toString().contains("VERIFIED"),
                "freshness: " + beacon.freshness());

        String report = AdminStats.dailyReport(pools, m, "November 5, 2026");
        check("T4 the admin report states the snapshot and its age",
                report.contains("Snapshot: November 5, 2026 (") && report.contains("days old)"),
                "report carries the snapshot line");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
