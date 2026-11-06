package staylocal;

import java.util.List;

/**
 * Phase 5 check main for the hold flow (Hold + HoldManager) running on
 * the real BC 211 directory pools. Counts start from each listing's
 * stated open beds, exactly as captured Nov 5, 2026.
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class HoldCheck {

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
        // T1-T3: the full happy path on The Beacon (listing states Men: 5)
        List<BedPool> pools = ShelterData.allShelters();
        HoldManager m = new HoldManager(pools, 90);
        BedPool beacon = byName(pools, "The Beacon");
        Hold h = m.placeHold(beacon.id, "Test profile");
        check("T1 place hold takes one bed", h != null && Hold.HELD.equals(h.status)
                        && beacon.pendingHolds == 1 && beacon.openCount() == 4,
                "open 5 -> " + beacon.openCount() + ", status " + (h == null ? "null" : h.status));

        m.confirmHold(h);
        check("T2 confirm moves pending -> confirmed", beacon.pendingHolds == 0
                        && beacon.confirmedHolds == 1 && beacon.openCount() == 4
                        && Hold.CONFIRMED.equals(h.status),
                "pending " + beacon.pendingHolds + ", confirmed " + beacon.confirmedHolds
                        + ", open " + beacon.openCount());

        m.checkIn(h);
        check("T3 check-in moves confirmed -> checked in", beacon.confirmedHolds == 0
                        && beacon.checkedIn == 1 && beacon.openCount() == 4
                        && Hold.CHECKED_IN.equals(h.status),
                "checkedIn " + beacon.checkedIn + ", open " + beacon.openCount());

        // T4: a pool listed Full takes no hold
        BedPool guildford = byName(pools, "Guildford Shelter");
        Hold full = m.placeHold(guildford.id, "Test profile");
        check("T4 full listing refuses a hold", full == null && guildford.openCount() == 0,
                "Guildford beds as listed: " + guildford.bedsText);

        // T5: the last-bed race — Yukon Shelter states exactly Men: 1
        BedPool yukon = byName(pools, "Yukon Shelter");
        Hold first = m.placeHold(yukon.id, "First worker");
        Hold second = m.placeHold(yukon.id, "Second worker");
        check("T5 last bed goes to exactly one hold", first != null && second == null
                        && yukon.openCount() == 0,
                "first " + (first != null) + ", second refused " + (second == null));

        // T6: cancel returns the bed (The Crosswalk states Men: 4)
        BedPool crosswalk = byName(pools, "The Crosswalk");
        Hold c = m.placeHold(crosswalk.id, "Test profile");
        int afterHold = crosswalk.openCount();
        m.cancelHold(c);
        check("T6 cancel releases the bed", afterHold == 3 && crosswalk.openCount() == 4
                        && Hold.CANCELLED.equals(c.status),
                "open after hold " + afterHold + ", after cancel " + crosswalk.openCount());

        // T7: reject returns the bed and keeps the reason (The Haven, Men: 2)
        BedPool haven = byName(pools, "The Haven");
        Hold rj = m.placeHold(haven.id, "Test profile");
        m.rejectHold(rj, "No holds over phone - walk in");
        check("T7 reject releases the bed, reason kept", haven.openCount() == 2
                        && Hold.REJECTED.equals(rj.status)
                        && rj.rejectReason != null && rj.rejectReason.contains("walk in"),
                "open " + haven.openCount() + ", status " + rj.status);

        // T8: expiry — a separate manager whose holds live 0 seconds
        List<BedPool> pools2 = ShelterData.allShelters();
        HoldManager m2 = new HoldManager(pools2, 0);
        BedPool beacon2 = byName(pools2, "The Beacon");
        Hold ex = m2.placeHold(beacon2.id, "Test profile");
        int expired = m2.expireHolds();
        check("T8 expired hold returns its bed", expired == 1
                        && Hold.EXPIRED.equals(ex.status) && beacon2.openCount() == 5,
                "expired " + expired + ", open back to " + beacon2.openCount());

        // T9: no-show releases the bed and logs a turn-away (NSHW)
        List<BedPool> pools3 = ShelterData.allShelters();
        HoldManager m3 = new HoldManager(pools3, 90);
        BedPool beacon3 = byName(pools3, "The Beacon");
        Hold ns = m3.placeHold(beacon3.id, "Test profile");
        m3.confirmHold(ns);
        m3.noShow(ns);
        check("T9 no-show releases bed + logs NSHW", beacon3.openCount() == 5
                        && Hold.NO_SHOW.equals(ns.status) && m3.turnAways().size() == 1,
                "open " + beacon3.openCount() + ", turn-aways " + m3.turnAways().size());

        // T10: a CLOSED-badged EWR site takes no hold even though its
        // listing states a capacity (Kiwassa: Any: 20 max, but inactive)
        BedPool kiwassa = byName(pools, "Kiwassa Neighbourhood House - EWR");
        Hold closed = m.placeHold(kiwassa.id, "Test profile");
        check("T10 closed EWR site refuses a hold", closed == null
                        && kiwassa.openCount() == 0,
                "active " + kiwassa.active + ", beds as listed: " + kiwassa.bedsText);

        // T11: hold ids are sequential and the event log recorded the story
        check("T11 sequential ids + event log", h.id == 1 && first.id == 2
                        && m.eventLog().size() >= 6,
                "ids " + h.id + ", " + first.id + "; events " + m.eventLog().size());

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
