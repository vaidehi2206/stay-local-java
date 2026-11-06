package staylocal;

import java.util.List;

/**
 * Phase 19 check main for needs-based filters: the low-barrier
 * (sobriety) filter uses the directory's structured stance and
 * removes exactly the sobriety-program sites; the harm-reduction
 * and health flags come only from a listing's own words.
 * Run this file in NetBeans: every test should print PASS.
 */
public class SobrietyCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();
        IntakeData.apply(pools);

        ShelterSearch def = new ShelterSearch();
        List<BedPool> defResults = ShelterSearch.search(pools, def);
        int programRequired = 0;
        for (BedPool p : defResults) {
            if ("program-required".equals(p.sobrietyStance)) {
                programRequired++;
            }
        }
        ShelterSearch low = new ShelterSearch();
        low.lowBarrierOnly = true;
        int lowCount = ShelterSearch.search(pools, low).size();
        check("T1 low-barrier filter removes exactly the sobriety-program sites",
                programRequired == 2 && lowCount == defResults.size() - 2,
                "default " + defResults.size() + ", program-required in it "
                        + programRequired + ", low-barrier " + lowCount);

        ShelterSearch k = new ShelterSearch();
        k.keyword = "1670";
        int without = ShelterSearch.search(pools, k).size();
        k.lowBarrierOnly = true;
        int with = ShelterSearch.search(pools, k).size();
        check("T2 the 1670 Shelter (zero tolerance) is filtered out",
                without == 1 && with == 0,
                "without filter " + without + ", with low-barrier " + with);

        ShelterSearch hr = new ShelterSearch();
        hr.includeClosed = true;
        hr.harmReductionMentioned = true;
        List<BedPool> hrResults = ShelterSearch.search(pools, hr);
        check("T3 harm-reduction flag: only The Sisterhood's listing says it",
                hrResults.size() == 1 && "The Sisterhood".equals(hrResults.get(0).siteName),
                "found " + hrResults.size());

        ShelterSearch he = new ShelterSearch();
        he.includeClosed = true;
        he.healthMentioned = true;
        int healthCount = ShelterSearch.search(pools, he).size();
        check("T4 health-services flag: 3 listings mention medical / mental health",
                healthCount == 3,
                "found " + healthCount);

        BedPool arc = null;
        for (BedPool p : pools) {
            if (p.siteName.startsWith("Salvation Army ARC Emergency")) {
                arc = p;
            }
        }
        NeedProfile men = new NeedProfile();
        men.categoryNeeded = "MEN";
        men.age = 30;
        men.party = "single";
        men.sobrietyNeed = "must-not-require-sobriety";
        String reason = Eligibility.exclusionReason(arc, men, 600);
        check("T5 search and eligibility agree on sobriety",
                reason != null && reason.startsWith("sobriety"),
                "eligibility says: " + reason);

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
