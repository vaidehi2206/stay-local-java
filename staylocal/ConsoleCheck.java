package staylocal;

import java.util.List;

/**
 * Phase 6 check main for the shelter-console controls on HoldManager
 * (setOpenCount / setActive), running on the real BC 211 directory.
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class ConsoleCheck {

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

        boolean ok = m.setOpenCount(beacon.id, 2);
        check("T1 console sets open beds", ok && beacon.openCount() == 2
                        && beacon.physical == 2,
                "open " + beacon.openCount() + ", total " + beacon.physical);

        Hold h = m.placeHold(beacon.id, "Test profile");
        ok = m.setOpenCount(beacon.id, 0);
        check("T2 set open 0 keeps beds already spoken for", ok && h != null
                        && beacon.pendingHolds == 1 && beacon.openCount() == 0
                        && beacon.physical == 1,
                "pending " + beacon.pendingHolds + ", total " + beacon.physical
                        + ", open " + beacon.openCount());

        ok = m.setOpenCount(beacon.id, 3);
        check("T3 reopening beds adjusts the total around holds", ok
                        && beacon.openCount() == 3 && beacon.physical == 4,
                "open " + beacon.openCount() + ", total " + beacon.physical);

        ok = m.setOpenCount(beacon.id, -1);
        check("T4 negative open count refused", !ok && beacon.openCount() == 3,
                "open still " + beacon.openCount());

        // EWR: Kiwassa's listing states Any: 20 max but 211 showed it
        // CLOSED, so it starts inactive; the console can open the site.
        BedPool kiwassa = byName(pools, "Kiwassa Neighbourhood House - EWR");
        Hold before = m.placeHold(kiwassa.id, "Test profile");
        m.setActive(kiwassa.id, true);
        Hold after = m.placeHold(kiwassa.id, "Test profile");
        check("T5 console opens a closed EWR site", before == null && after != null
                        && kiwassa.openCount() == 19,
                "hold before " + (before != null) + ", after " + (after != null)
                        + ", open " + kiwassa.openCount());

        m.setActive(beacon.id, false);
        Hold closedHold = m.placeHold(beacon.id, "Test profile");
        check("T6 console closing a site stops new holds", closedHold == null
                        && beacon.openCount() == 0,
                "active " + beacon.active + ", open " + beacon.openCount());

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
