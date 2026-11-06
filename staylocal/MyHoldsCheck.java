package staylocal;

import java.util.List;

/**
 * Phase 16 check main for the worker's hold management: every hold
 * is listed, a live hold can be extended by one more window, an ETA
 * can be set for the shelter, and neither works once a hold has
 * moved past HELD.
 * Run this file in NetBeans: every test should print PASS.
 */
public class MyHoldsCheck {

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

        Hold h1 = m.placeHold(beacon.id, "Person A");
        Hold h2 = m.placeHold(yukon.id, "Person B", 1,
                java.util.Arrays.asList("Meal on arrival"), "", false);
        check("T1 all session holds are listed",
                m.holds().size() == 2 && h1 != null && h2 != null,
                "holds: " + m.holds().size());

        long before = h1.expiresAtMillis;
        boolean extended = m.extendHold(h1);
        check("T2 extending a live hold adds exactly one window (90 s demo)",
                extended && h1.expiresAtMillis - before == 90000L,
                "added " + (h1.expiresAtMillis - before) + " ms");

        boolean etaOk = m.setEta(h1, 25);
        check("T3 ETA is stored and shows on the hold line",
                etaOk && h1.etaMinutes == 25
                        && h1.describe(System.currentTimeMillis()).contains("ETA ~25 min"),
                "line: " + h1.describe(System.currentTimeMillis()));

        m.confirmHold(h1);
        boolean extendLate = m.extendHold(h1);
        boolean etaConfirmed = m.setEta(h1, 40);
        check("T4 a confirmed hold takes an ETA but no extension",
                !extendLate && etaConfirmed && h1.etaMinutes == 40,
                "extend=" + extendLate + ", eta=" + h1.etaMinutes);

        m.cancelHold(h2);
        boolean etaDead = m.setEta(h2, 10);
        check("T5 a cancelled hold takes neither",
                !etaDead && Hold.CANCELLED.equals(h2.status),
                "eta set on cancelled: " + etaDead);

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
