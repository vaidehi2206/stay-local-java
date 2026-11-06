package staylocal;

import java.util.List;

/**
 * Phase 13 check main for availability alerts: a worker's saved
 * search (a watch) must fire exactly when a matching shelter gains
 * an open bed — when the shelter updates its count, and when a
 * cancelled hold frees a bed — and must not fire for availability
 * that was already there, nor re-fire while nothing changed, nor
 * fire after the watch is removed.
 * Run this file in NetBeans: every test should print PASS.
 */
public class WatchCheck {

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
        WatchManager wm = new WatchManager();
        BedPool beacon = byName(pools, "The Beacon");
        BedPool yukon = byName(pools, "Yukon Shelter");

        ShelterSearch beaconSearch = new ShelterSearch();
        beaconSearch.keyword = "Beacon";
        Watch w1 = wm.addWatch("Test watch - Beacon", beaconSearch, pools, "5:00 PM");
        List<WatchManager.Alert> fired = wm.check(pools, "5:01 PM");
        check("T1 no alert for availability that was already there",
                fired.isEmpty() && wm.alertCount() == 0,
                "Beacon starts with " + beacon.openCount() + " open; fired " + fired.size());

        m.setOpenCount(beacon.id, 0);
        fired = wm.check(pools, "5:02 PM");
        boolean t2 = fired.isEmpty();
        m.setOpenCount(beacon.id, 3);
        fired = wm.check(pools, "5:03 PM");
        check("T2 alert when the shelter opens beds again",
                t2 && fired.size() == 1 && wm.alertCount() == 1
                        && "The Beacon".equals(fired.get(0).siteName)
                        && fired.get(0).bedsOpen == 3,
                "after reopening: fired " + fired.size()
                        + ", alert: " + (fired.isEmpty() ? "-" : fired.get(0).describe()));

        fired = wm.check(pools, "5:04 PM");
        check("T3 no re-alert while nothing changed",
                fired.isEmpty() && wm.alertCount() == 1,
                "fired " + fired.size() + " on an unchanged re-check");

        ShelterSearch yukonSearch = new ShelterSearch();
        yukonSearch.keyword = "Yukon";
        wm.addWatch("Test watch - Yukon", yukonSearch, pools, "5:05 PM");
        Hold h = m.placeHold(yukon.id, "Worker demo");
        wm.check(pools, "5:06 PM");
        m.cancelHold(h);
        fired = wm.check(pools, "5:07 PM");
        check("T4 alert when a cancelled hold frees the last bed",
                fired.size() == 1 && "Yukon Shelter".equals(fired.get(0).siteName),
                "fired " + fired.size()
                        + (fired.isEmpty() ? "" : ": " + fired.get(0).describe()));

        WatchManager.Alert first = wm.alerts().get(0);
        boolean dismissed = wm.dismissAlert(first);
        wm.removeWatch(w1);
        m.setOpenCount(beacon.id, 0);
        wm.check(pools, "5:08 PM");
        m.setOpenCount(beacon.id, 2);
        fired = wm.check(pools, "5:09 PM");
        check("T5 dismissal works; a removed watch stays silent",
                dismissed && wm.alertCount() == 1 && fired.isEmpty(),
                "alerts left " + wm.alertCount() + ", fired after removal " + fired.size());

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
