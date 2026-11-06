package staylocal;

import java.util.Arrays;
import java.util.List;

/**
 * Phase 15 check main: a hold now takes a NUMBER of beds (a couple
 * or family is held together, all-or-nothing, never split) and
 * carries the arrival needs the shelter should prepare — supplies,
 * accessibility, medical. Counts move in beds at every transition.
 * Run this file in NetBeans: every test should print PASS.
 */
public class NeedsCheck {

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

        Hold family = m.placeHold(beacon.id, "Family of three", 3,
                Arrays.asList("Gloves / socks / warm clothing", "Deaf / hard of hearing"),
                "Arriving by taxi", true);
        check("T1 a 3-bed hold with needs takes 3 beds at once",
                family != null && family.beds == 3 && beacon.pendingHolds == 3
                        && beacon.openCount() == 2
                        && family.needsSummary().startsWith("URGENT")
                        && family.needsSummary().contains("Deaf / hard of hearing")
                        && family.needsSummary().contains("Arriving by taxi"),
                "open " + beacon.openCount() + ", prepare: " + (family == null ? "-" : family.needsSummary()));

        Hold tooBig = m.placeHold(beacon.id, "Another family", 3, null, "", false);
        check("T2 all-or-nothing: 3 more beds refused when only 2 are open",
                tooBig == null && beacon.openCount() == 2,
                "open still " + beacon.openCount());

        m.confirmHold(family);
        boolean t3a = beacon.confirmedHolds == 3 && beacon.pendingHolds == 0;
        m.checkIn(family);
        check("T3 confirm + check-in move beds, not holds",
                t3a && beacon.checkedIn == 3 && beacon.openCount() == 2,
                "checked in " + beacon.checkedIn + ", open " + beacon.openCount());

        Hold pair = m.placeHold(beacon.id, "A couple", 2, null, "", false);
        boolean t4a = pair != null && beacon.openCount() == 0;
        m.cancelHold(pair);
        check("T4 cancelling a 2-bed hold frees both beds",
                t4a && beacon.openCount() == 2,
                "open " + beacon.openCount());

        Hold legacy = m.placeHold(beacon.id, "Solo (old-style call)");
        check("T5 the original 1-bed call still works, no needs noted",
                legacy != null && legacy.beds == 1 && !legacy.hasNeeds()
                        && "no arrival needs noted".equals(legacy.needsSummary()),
                "beds " + (legacy == null ? -1 : legacy.beds));

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
