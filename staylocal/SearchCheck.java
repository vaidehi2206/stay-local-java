package staylocal;

import java.util.List;

/**
 * Phase 4 check main for ShelterSearch over the real BC 211 directory.
 * Expected counts were computed from the same captured directory data
 * (BC211_Shelter_Directory.json) before this file was written.
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class SearchCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    static ShelterSearch base() {
        return new ShelterSearch();
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();

        List<BedPool> r = ShelterSearch.search(pools, base());
        check("T1 default search (closed excluded)", r.size() == 80,
                "expected 80, got " + r.size());

        ShelterSearch c2 = base();
        c2.includeClosed = true;
        r = ShelterSearch.search(pools, c2);
        check("T2 include closed/EWR", r.size() == 107,
                "expected 107, got " + r.size());

        ShelterSearch c3 = base();
        c3.city = "Vancouver";
        r = ShelterSearch.search(pools, c3);
        check("T3 city = Vancouver", r.size() == 39,
                "expected 39, got " + r.size());

        ShelterSearch c4 = base();
        c4.city = "Vancouver";
        c4.openOnly = true;
        r = ShelterSearch.search(pools, c4);
        check("T4 Vancouver + open beds listed", r.size() == 12,
                "expected 12, got " + r.size());

        ShelterSearch c5 = base();
        c5.serves = "WOMEN";
        r = ShelterSearch.search(pools, c5);
        check("T5 serves = WOMEN", r.size() == 55,
                "expected 55, got " + r.size());

        ShelterSearch c6 = base();
        c6.serves = "MEN";
        r = ShelterSearch.search(pools, c6);
        check("T6 serves = MEN", r.size() == 50,
                "expected 50, got " + r.size());

        ShelterSearch c7 = base();
        c7.serves = "YOUTH";
        r = ShelterSearch.search(pools, c7);
        check("T7 serves = YOUTH", r.size() == 14,
                "expected 14, got " + r.size());

        ShelterSearch c8 = base();
        c8.serves = "FAMILY";
        r = ShelterSearch.search(pools, c8);
        check("T8 serves = FAMILY", r.size() == 6,
                "expected 6, got " + r.size());

        ShelterSearch c9 = base();
        c9.petFriendly = true;
        r = ShelterSearch.search(pools, c9);
        check("T9 pet friendly", r.size() == 41,
                "expected 41, got " + r.size());

        ShelterSearch c10 = base();
        c10.city = "Surrey";
        c10.wheelchair = true;
        r = ShelterSearch.search(pools, c10);
        check("T10 Surrey + wheelchair accessible", r.size() == 6,
                "expected 6, got " + r.size());

        ShelterSearch c11 = base();
        c11.age = 16;
        r = ShelterSearch.search(pools, c11);
        check("T11 age 16", r.size() == 14,
                "expected 14, got " + r.size());

        ShelterSearch c12 = base();
        c12.city = "Vancouver";
        c12.serves = "WOMEN";
        c12.age = 30;
        r = ShelterSearch.search(pools, c12);
        check("T12 Vancouver + WOMEN + age 30", r.size() == 25,
                "expected 25, got " + r.size());

        ShelterSearch c13 = base();
        c13.openOnly = true;
        r = ShelterSearch.search(pools, c13);
        check("T13 open beds listed, any city", r.size() == 22,
                "expected 22, got " + r.size());

        ShelterSearch c14 = base();
        c14.sort = ShelterSearch.SORT_MOST_BEDS;
        r = ShelterSearch.search(pools, c14);
        check("T14 sort by most beds listed",
                r.size() > 1
                        && r.get(0).siteName.startsWith("Foundations Emergency Shelter")
                        && r.get(0).physical == 52
                        && "Anchor Seasonal Shelter".equals(r.get(1).siteName),
                "first: " + (r.isEmpty() ? "none" : r.get(0).siteName + " (" + r.get(0).physical + ")"));

        ShelterSearch c15 = base();
        c15.sort = ShelterSearch.SORT_RECENT;
        r = ShelterSearch.search(pools, c15);
        check("T15 sort by most recently updated",
                !r.isEmpty() && "Arbutus Shelter".equals(r.get(0).siteName),
                "first: " + (r.isEmpty() ? "none" : r.get(0).siteName));

        ShelterSearch c16 = base();
        c16.city = "Vancouver";
        c16.serves = "FAMILY";
        r = ShelterSearch.search(pools, c16);
        check("T16 Vancouver + FAMILY", r.size() == 3,
                "expected 3, got " + r.size());

        List<String> cities = ShelterSearch.cities(pools);
        check("T17 city list for the search form", cities.size() == 15
                        && "Abbotsford".equals(cities.get(0))
                        && "Victoria".equals(cities.get(14)),
                "expected 15 cities, Abbotsford..Victoria, got " + cities.size());

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
