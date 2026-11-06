package staylocal;

import java.util.List;

/**
 * Phase 18 check main for demand logging: settled searches are
 * recorded, the ones with no open bed count as unmet demand, the
 * most-repeated unmet searches rank first, and the admin's daily
 * report gains a DEMAND section.
 * Run this file in NetBeans: every test should print PASS.
 */
public class DemandCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) {
        SearchLog log = new SearchLog();
        log.record("Women - Vancouver", 5, 2, "6:01 PM");
        log.record("Family - Burnaby", 3, 0, "6:05 PM");
        log.record("Family - Burnaby", 2, 0, "6:09 PM");
        log.record("Men - Surrey", 4, 0, "6:12 PM");

        check("T1 searches and unmet searches are counted",
                log.totalSearches() == 4 && log.zeroOpenCount() == 3,
                "total " + log.totalSearches() + ", unmet " + log.zeroOpenCount());

        List<java.util.Map.Entry<String, Integer>> top = log.topUnmet(5);
        check("T2 the most-repeated unmet need ranks first",
                top.size() == 2 && "Family - Burnaby".equals(top.get(0).getKey())
                        && top.get(0).getValue() == 2,
                "top: " + top);

        List<BedPool> pools = ShelterData.allShelters();
        HoldManager m = new HoldManager(pools, 90);
        String report = AdminStats.dailyReport(pools, m, "test date", log);
        check("T3 the admin report carries the DEMAND section",
                report.contains("DEMAND (what workers searched for, this session)")
                        && report.contains("Searches recorded: 4")
                        && report.contains("NO open bed (unmet): 3")
                        && report.contains("2 x Family - Burnaby"),
                "demand section present with counts and ranking");

        String legacy = AdminStats.dailyReport(pools, m, "test date");
        check("T4 the original 3-argument report still works",
                legacy.contains("Searches recorded: 0"),
                "legacy callers get an empty demand log");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
