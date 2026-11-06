package staylocal;

import java.util.List;

/**
 * Phase 20 check main for the printable handout: a shelter sheet
 * carries the facts a person needs at the door, a hidden site's
 * address is never printed, a hold sheet shows what is being kept,
 * and a results sheet lists the matches.
 * Run this file in NetBeans: every test should print PASS.
 */
public class HandoutCheck {

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
        BedPool beacon = byName(pools, "The Beacon");
        BedPool shelter1670 = byName(pools, "1670 Shelter across VAFCS");

        String sheet = Handout.forShelter(beacon, "November 5, 2026");
        check("T1 shelter handout carries the door facts",
                sheet.contains("The Beacon") && sheet.contains("604-646-6846")
                        && sheet.contains("Intake window: 08:00 - 23:30")
                        && sheet.contains("Who can come:"),
                "name, phone, intake window, who-can-come all present");

        String hidden = Handout.forShelter(shelter1670, "November 5, 2026");
        check("T2 a confidential address is never printed",
                hidden.contains("Address: Address confidential - call the shelter or ask your worker"),
                "the address line says call - no street address appears");

        HoldManager m = new HoldManager(pools, 90);
        Hold h = m.placeHold(beacon.id, "Person A", 2,
                java.util.Arrays.asList("Gloves / socks / warm clothing"), "", false);
        String holdSheet = Handout.forHold(h, beacon, "November 5, 2026",
                System.currentTimeMillis());
        check("T3 hold handout shows the hold and what is being prepared",
                holdSheet.contains("Hold #" + h.id) && holdSheet.contains("Beds: 2")
                        && holdSheet.contains("Gloves / socks / warm clothing"),
                "hold id, beds and needs on the sheet");

        List<BedPool> results = ShelterSearch.search(pools, new ShelterSearch());
        String listSheet = Handout.forResults("Anyone - Any city", results, "November 5, 2026");
        check("T4 results handout lists the matches",
                listSheet.contains("SHELTERS MATCHING") && results.size() == 80
                        && listSheet.contains(results.get(0).siteName),
                results.size() + " matches, first listed: " + results.get(0).siteName);

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
