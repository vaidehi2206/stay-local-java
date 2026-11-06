package staylocal;

import java.util.List;

/**
 * Phase 7 check main: every field on a BC 211 listing works as a
 * filter in ShelterSearch (the worker filters by necessity, not just
 * availability). Expected counts were computed from the captured
 * directory data (BC211_Shelter_Directory.json) before this file was
 * written. Default criteria exclude CLOSED-badged sites, as in the app.
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class FilterCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    static int count(List<BedPool> pools, ShelterSearch c) {
        return ShelterSearch.search(pools, c).size();
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();
        ShelterSearch c;

        c = new ShelterSearch();
        c.keyword = "harm reduction";
        check("T1 keyword in notes: harm reduction", count(pools, c) == 1,
                "expected 1, got " + count(pools, c));

        c = new ShelterSearch();
        c.keyword = "storage";
        check("T2 keyword in notes: storage", count(pools, c) == 2,
                "expected 2, got " + count(pools, c));

        c = new ShelterSearch();
        c.largeItems = true;
        check("T3 feature: takes large items", count(pools, c) == 12,
                "expected 12, got " + count(pools, c));

        c = new ShelterSearch();
        c.type = "Overnight";
        check("T4 type: Overnight", count(pools, c) == 17,
                "expected 17, got " + count(pools, c));

        c = new ShelterSearch();
        c.type = "First Come-First Serve";
        check("T5 type: First Come-First Serve", count(pools, c) == 9,
                "expected 9, got " + count(pools, c));

        c = new ShelterSearch();
        c.type = "Seasonal";
        check("T6 type: Seasonal", count(pools, c) == 4,
                "expected 4, got " + count(pools, c));

        c = new ShelterSearch();
        c.bedsStatus = "FULL";
        check("T7 beds status: listed Full", count(pools, c) == 25,
                "expected 25, got " + count(pools, c));

        c = new ShelterSearch();
        c.bedsStatus = "UNKNOWN";
        check("T8 beds status: no information stated", count(pools, c) == 33,
                "expected 33, got " + count(pools, c));

        c = new ShelterSearch();
        c.bedsStatus = "OPEN";
        check("T9 beds status: open beds stated", count(pools, c) == 22,
                "expected 22, got " + count(pools, c));

        c = new ShelterSearch();
        c.updatedWithinMin = 1440;
        check("T10 updated within 24 hours", count(pools, c) == 51,
                "expected 51, got " + count(pools, c));

        c = new ShelterSearch();
        c.updatedWithinMin = 43200;
        check("T11 updated within 30 days", count(pools, c) == 62,
                "expected 62, got " + count(pools, c));

        c = new ShelterSearch();
        c.confidentialOnly = true;
        check("T12 confidential-address sites only", count(pools, c) == 15,
                "expected 15, got " + count(pools, c));

        c = new ShelterSearch();
        c.city = "Vancouver";
        c.type = "Overnight";
        c.petFriendly = true;
        check("T13 combined: Vancouver + Overnight + pet", count(pools, c) == 6,
                "expected 6, got " + count(pools, c));

        c = new ShelterSearch();
        c.type = "Extreme Weather";
        c.includeClosed = true;
        check("T14 type: Extreme Weather (incl. closed)", count(pools, c) == 24,
                "expected 24, got " + count(pools, c));

        c = new ShelterSearch();
        c.type = "24 Hour";
        check("T15 type: 24 Hour", count(pools, c) == 59,
                "expected 59, got " + count(pools, c));

        // The worker scenario this phase exists for: a woman of 30 with
        // a pet, needing wheelchair access, in Vancouver, and only
        // listings 211 has refreshed within the last day.
        c = new ShelterSearch();
        c.city = "Vancouver";
        c.serves = "WOMEN";
        c.age = 30;
        c.petFriendly = true;
        c.wheelchair = true;
        c.updatedWithinMin = 1440;
        check("T16 worker scenario: woman + pet + wheelchair + fresh, Vancouver",
                count(pools, c) == 5,
                "expected 5, got " + count(pools, c));

        c = new ShelterSearch();
        c.keyword = "youth";
        check("T17 keyword in name/notes: youth", count(pools, c) == 12,
                "expected 12, got " + count(pools, c));

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
