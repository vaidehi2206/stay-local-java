package staylocal;

import java.util.List;

/**
 * Phase 21 check main for the shelter console's working tools:
 * the walk-in reserve protects beds for people without a hold and
 * can never eat spoken-for beds, arrivals can be checked in or
 * no-showed from the shelter side, and the day summary counts it all.
 * Run this file in NetBeans: every test should print PASS.
 */
public class ConsoleOpsCheck {

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
        BedPool yukon = byName(pools, "Yukon Shelter");

        boolean r1 = m.setWalkInReserve(beacon.id, 2);
        boolean r2 = m.setWalkInReserve(beacon.id, 99);
        check("T1 walk-in reserve protects beds and refuses excess",
                r1 && !r2 && beacon.walkInReserve == 2 && beacon.openCount() == 3,
                "reserve " + beacon.walkInReserve + ", open " + beacon.openCount());

        Hold h = m.placeHold(beacon.id, "Person A");
        boolean r3 = m.setWalkInReserve(beacon.id, 3);
        check("T2 the reserve can grow into open beds but not held ones",
                h != null && r3 && beacon.openCount() == 1,
                "open " + beacon.openCount() + " (5 - 1 held - 3 reserve)");

        m.confirmHold(h);
        m.checkIn(h);
        String summary = m.shelterDaySummary(beacon.id);
        check("T3 shelter-side check-in lands in the day summary",
                summary.contains("checked in 1") && summary.contains("walk-in reserve 3"),
                "summary counts the arrival");

        Hold y = m.placeHold(yukon.id, "Person B");
        m.confirmHold(y);
        m.noShow(y);
        String ySummary = m.shelterDaySummary(yukon.id);
        check("T4 shelter-side no-show lands in the summary and the turn-away log",
                ySummary.contains("no-show 1") && m.turnAways().size() == 1
                        && m.turnAways().get(0).startsWith("NSHW"),
                "turn-aways: " + m.turnAways());

        BedPool powell = byName(pools, "Powell Place Shelter For Women");
        check("T5 an untouched shelter summaries to zero",
                m.shelterDaySummary(powell.id).contains("0 placed"),
                "no cross-shelter leakage");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
