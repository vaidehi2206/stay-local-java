package staylocal;

import java.util.List;

/**
 * Phase 24 check main for area-based distance: anchor math is
 * sane, areas are assigned from the printed address, a
 * confidential site is never placed, and "Nearest first" really
 * does bring the closest area to the top.
 * Run this file in NetBeans: every test should print PASS.
 */
public class DistanceCheck {

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

        double dtesSurrey = ShelterZones.distanceKm("Downtown Eastside", "Surrey");
        check("T1 anchor distance DTES to Surrey is about 21 km",
                dtesSurrey > 17 && dtesSurrey < 26,
                "computed " + String.format("%.1f", dtesSurrey) + " km");

        BedPool beacon = byName(pools, "The Beacon");
        BedPool marpole = byName(pools, "Marpole Neighbourhood House - EWR");
        BedPool kiwassa = byName(pools, "Kiwassa Neighbourhood House - EWR");
        check("T2 areas come from the printed address",
                "Downtown Eastside".equals(ShelterZones.zoneOf(beacon))
                        && "Marpole".equals(ShelterZones.zoneOf(marpole))
                        && "Hastings-Sunrise".equals(ShelterZones.zoneOf(kiwassa)),
                "Beacon=" + ShelterZones.zoneOf(beacon) + ", Marpole NH="
                        + ShelterZones.zoneOf(marpole) + ", Kiwassa=" + ShelterZones.zoneOf(kiwassa));

        BedPool hidden = byName(pools, "1670 Shelter across VAFCS");
        check("T3 a confidential site is never placed",
                ShelterZones.zoneOf(hidden).isEmpty()
                        && ShelterZones.distanceKm(ShelterZones.zoneOf(hidden), "Surrey") == -1,
                "no area, no distance - by design");

        BedPool victoria = null;
        for (BedPool p : pools) {
            if ("Victoria".equals(p.city) && !p.hiddenSite) {
                victoria = p;
                break;
            }
        }
        check("T4 outside Vancouver the area is the city",
                victoria != null && "Victoria".equals(ShelterZones.zoneOf(victoria)),
                victoria == null ? "none found" : victoria.siteName);

        ShelterSearch c = new ShelterSearch();
        c.sort = ShelterSearch.SORT_NEAREST;
        c.nearZone = "Surrey";
        List<BedPool> results = ShelterSearch.search(pools, c);
        int beaconIdx = -1;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i) == beacon) {
                beaconIdx = i;
                break;
            }
        }
        check("T5 nearest-first puts Surrey on top, Vancouver far down",
                !results.isEmpty() && "Surrey".equals(results.get(0).city) && beaconIdx > 20,
                "first: " + results.get(0).siteName + " (" + results.get(0).city
                        + "), Beacon at #" + (beaconIdx + 1) + " of " + results.size());

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
