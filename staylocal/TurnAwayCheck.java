package staylocal;

import java.util.List;

/**
 * Phase 17 check main for turn-away and failed-arrival logging:
 * unmet need becomes data. A worker's failed search can be logged,
 * a failed arrival releases the bed at once and records a FARV
 * entry, and the admin's daily report breaks the turn-aways down.
 * Run this file in NetBeans: every test should print PASS.
 */
public class TurnAwayCheck {

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
        BedPool beacon = byName(pools, "The Beacon");

        m.logTurnAway("No beds open (full)", "Women - a couple - Vancouver");
        check("T1 a worker's failed search is logged as unmet need",
                m.turnAways().size() == 1
                        && m.turnAways().get(0).contains("No beds open (full)"),
                "log: " + m.turnAways());

        Hold h = m.placeHold(beacon.id, "Person A", 2, null, "", false);
        m.confirmHold(h);
        boolean logged = m.logFailedArrival(h, "Refused at door - eligibility");
        check("T2 a failed arrival releases both beds and records FARV",
                logged && Hold.FAILED_ARRIVAL.equals(h.status)
                        && beacon.confirmedHolds == 0 && beacon.openCount() == 5
                        && m.turnAways().size() == 2
                        && m.turnAways().get(1).startsWith("FARV"),
                "status " + h.status + ", open " + beacon.openCount());

        boolean again = m.logFailedArrival(h, "Person did not arrive");
        check("T3 a finished hold cannot be logged twice",
                !again && m.turnAways().size() == 2,
                "second attempt accepted: " + again);

        String report = AdminStats.dailyReport(pools, m, "test date");
        check("T4 the admin report breaks turn-aways down",
                report.contains("Failed arrivals: 1")
                        && report.contains("From worker searches (no bed found): 1")
                        && report.contains("Failed arrivals after a hold: 1"),
                "report carries the breakdown");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
