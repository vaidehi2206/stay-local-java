package staylocal;

import java.util.List;

/**
 * Phase 14 check main for BC 211 intake info: every listing carries
 * the verbatim text 211 published (101 of 107; 6 publish none and
 * stay empty), structured windows exist only where the text states
 * them (85), eligibility applies a window only when it is known,
 * and the "intake open now" search filter never hides a listing
 * whose window is unknown.
 * Run this file in NetBeans: every test should print PASS.
 */
public class IntakeCheck {

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
        IntakeData.apply(pools);

        int withText = 0;
        int known = 0;
        for (BedPool p : pools) {
            if (!p.intakeText.isEmpty()) {
                withText++;
            }
            if (p.intakeKnown) {
                known++;
            }
        }
        check("T1 coverage: 107 pools, 101 with intake text, 85 with a verified window",
                pools.size() == 107 && withText == 101 && known == 85,
                "pools " + pools.size() + ", text " + withText + ", known " + known);

        BedPool beacon = byName(pools, "The Beacon");
        check("T2 The Beacon's stated intake window (8:00 AM - 11:30 PM)",
                beacon.intakeKnown && "08:00".equals(beacon.intakeOpen)
                        && "23:30".equals(beacon.intakeClose)
                        && beacon.intakeOpenAt(600) && !beacon.intakeOpenAt(60),
                "window " + beacon.intakeOpen + "-" + beacon.intakeClose);

        BedPool welcomeHub = byName(pools, "Surrey Welcome Hub - EWR");
        check("T3 overnight EWR window wraps midnight (open 11 PM, closed noon)",
                welcomeHub.intakeKnown && welcomeHub.intakeOpenAt(23 * 60)
                        && !welcomeHub.intakeOpenAt(12 * 60),
                "window " + welcomeHub.intakeOpen + "-" + welcomeHub.intakeClose);

        BedPool springhouse = byName(pools, "Springhouse");
        BedPool belkinMen = byName(pools, "Belkin House - Men's Shelter");
        check("T4 text-only stays unknown; no-text stays empty (never guessed)",
                !springhouse.intakeText.isEmpty() && !springhouse.intakeKnown
                        && belkinMen.intakeText.isEmpty() && !belkinMen.intakeKnown,
                "Springhouse known=" + springhouse.intakeKnown
                        + ", Belkin Men text=\"" + belkinMen.intakeText + "\"");

        NeedProfile men = new NeedProfile();
        men.categoryNeeded = "MEN";
        men.age = 30;
        men.party = "single";
        String at10am = Eligibility.exclusionReason(beacon, men, 600);
        String at1am = Eligibility.exclusionReason(beacon, men, 60);
        NeedProfile women = new NeedProfile();
        women.categoryNeeded = "WOM";
        women.age = 30;
        women.party = "single";
        String springAt1am = Eligibility.exclusionReason(springhouse, women, 60);
        check("T5 eligibility enforces KNOWN windows only",
                (at10am == null || !at10am.startsWith("intake closed"))
                        && at1am != null && at1am.startsWith("intake closed")
                        && (springAt1am == null || !springAt1am.startsWith("intake closed")),
                "Beacon 10am: " + at10am + " | Beacon 1am: " + at1am
                        + " | Springhouse 1am: " + springAt1am);

        ShelterSearch c = new ShelterSearch();
        c.keyword = "Springhouse";
        c.intakeOpenNow = true;
        List<BedPool> r = ShelterSearch.search(pools, c);
        ShelterSearch all = new ShelterSearch();
        ShelterSearch filtered = new ShelterSearch();
        filtered.intakeOpenNow = true;
        int allCount = ShelterSearch.search(pools, all).size();
        int filteredCount = ShelterSearch.search(pools, filtered).size();
        check("T6 'intake open now' never hides an unknown-window listing",
                r.size() == 1 && filteredCount <= allCount && filteredCount >= 1,
                "Springhouse found: " + r.size() + ", all " + allCount
                        + " -> intake-open-now " + filteredCount);

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
