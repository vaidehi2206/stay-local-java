package staylocal;

import java.util.List;

/**
 * Phase 9 check main for the admin's directory powers — add a
 * shelter, put a listing on hold, remove a shelter — and for the
 * public-updates board (NoticeBoard).
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class AdminOpsCheck {

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

    static int vancouverCount(List<BedPool> pools, boolean includeClosed) {
        ShelterSearch c = new ShelterSearch();
        c.city = "Vancouver";
        c.includeClosed = includeClosed;
        return ShelterSearch.search(pools, c).size();
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();
        HoldManager m = new HoldManager(pools, 90);

        // T1: admin adds a shelter — it joins the live inventory at once
        BedPool added = AdminOps.newShelter("Riverside Demo Shelter", "Vancouver",
                "100 Demo Street", "604-555-0100", "MIX", 6);
        String newId = AdminOps.addShelter(pools, m, added);
        Hold held = m.placeHold(newId, "Worker demo");
        check("T1 admin adds a shelter, beds holdable immediately",
                pools.size() == 108 && "admin-1".equals(newId)
                        && m.pool(newId) != null && held != null
                        && added.openCount() == 5,
                "id " + newId + ", directory " + pools.size()
                        + ", open after 1 hold " + added.openCount());
        m.cancelHold(held);

        // T2: admin puts The Beacon on hold — workers stop seeing it,
        // even with "include closed" on; releasing brings it back
        BedPool beacon = byName(pools, "The Beacon");
        int before = vancouverCount(pools, true);
        m.setOnHold(beacon.id, true, "Inspection - admin demo");
        int during = vancouverCount(pools, true);
        Hold blocked = m.placeHold(beacon.id, "Worker demo");
        m.setOnHold(beacon.id, false, "");
        int after = vancouverCount(pools, true);
        check("T2 listing on hold disappears from worker search",
                before == 49 && during == 48 && after == 49
                        && blocked == null && beacon.openCount() == 5,
                "Vancouver (incl. closed, after T1's addition) " + before
                        + " -> " + during + " -> " + after);

        // T3: eligibility explains the admin hold
        m.setOnHold(beacon.id, true, "Inspection - admin demo");
        NeedProfile profile = new NeedProfile();
        profile.categoryNeeded = "MEN";
        profile.party = "single";
        profile.age = 30;
        String reason = Eligibility.exclusionReason(beacon, profile, 1240);
        m.setOnHold(beacon.id, false, "");
        check("T3 eligibility names the admin hold",
                reason != null && reason.contains("on hold by admin"),
                "reason: " + reason);

        // T4: a shelter with someone checked in / on hold cannot be
        // removed; once the hold is cancelled, removal works
        BedPool yukon = byName(pools, "Yukon Shelter");
        Hold yh = m.placeHold(yukon.id, "Worker demo");
        boolean refused = !m.removePool(yukon.id);
        m.cancelHold(yh);
        boolean removed = m.removePool(yukon.id);
        check("T4 removal refused while a hold is active, then allowed",
                refused && removed && m.pool(yukon.id) == null
                        && pools.size() == 107,
                "refused " + refused + ", removed " + removed
                        + ", directory " + pools.size());

        // T5: the public-updates board — newest first, blanks ignored
        NoticeBoard board = new NoticeBoard();
        boolean emptyOk = NoticeBoard.NO_NOTICES.equals(board.latest());
        board.add("City update: warming centres open tonight.");
        board.add("   ");
        board.add("BC Housing update: winter shelter program extended.");
        boolean orderOk = board.size() == 2
                && board.latest().startsWith("BC Housing update");
        board.remove(0);
        boolean removeOk = board.size() == 1
                && board.latest().startsWith("City update");
        check("T5 notice board: newest first, blank ignored, removable",
                emptyOk && orderOk && removeOk,
                "size " + board.size() + ", latest: " + board.latest());

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
