package staylocal;

import java.util.List;

/**
 * Phase 12 check main for the "Coming as" filter: who the person is
 * travelling as — on their own, a couple, or a family with children —
 * matched against who each listing takes. Expected counts are the
 * directory's own: family listings take families (6 open), youth
 * listings take individuals only, and women/men/mixed listings take
 * individuals and couples.
 * Run this file in NetBeans: every test should print PASS.
 */
public class PartyCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    static int count(List<BedPool> pools, String party) {
        ShelterSearch c = new ShelterSearch();
        c.party = party;
        return ShelterSearch.search(pools, c).size();
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();

        check("T1 no party filter", count(pools, "") == 80,
                "expected 80, got " + count(pools, ""));
        check("T2 on their own", count(pools, "single") == 74,
                "expected 74, got " + count(pools, "single"));
        check("T3 a couple", count(pools, "couple") == 60,
                "expected 60, got " + count(pools, "couple"));
        check("T4 family with children", count(pools, "family") == 6,
                "expected 6, got " + count(pools, "family"));

        ShelterSearch van = new ShelterSearch();
        van.city = "Vancouver";
        van.party = "family";
        int vanFam = ShelterSearch.search(pools, van).size();
        check("T5 Vancouver + family", vanFam == 3,
                "expected 3, got " + vanFam);

        ShelterSearch women = new ShelterSearch();
        women.serves = "WOMEN";
        women.party = "couple";
        int womenCouple = ShelterSearch.search(pools, women).size();
        check("T6 serves WOMEN + couple", womenCouple == 49,
                "expected 49, got " + womenCouple);

        ShelterSearch vanCouple = new ShelterSearch();
        vanCouple.city = "Vancouver";
        vanCouple.party = "couple";
        int vc = ShelterSearch.search(pools, vanCouple).size();
        check("T7 Vancouver + couple", vc == 29,
                "expected 29, got " + vc);

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
