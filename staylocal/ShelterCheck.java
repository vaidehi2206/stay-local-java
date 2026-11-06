package staylocal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phase 3 check main for the REAL BC 211 directory data (ShelterData).
 * Run this file in NetBeans: every test should print PASS and the run
 * should end with ALL TESTS PASS.
 */
public class ShelterCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) { failures++; }
    }

    static BedPool byName(List<BedPool> pools, String name) {
        for (BedPool p : pools) {
            if (p.siteName.equals(name)) { return p; }
        }
        return null;
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();
        System.out.println(pools.size() + " shelters loaded (" + ShelterData.SOURCE
                + ", retrieved " + ShelterData.RETRIEVED + ")");
        for (BedPool p : pools) {
            System.out.println(p.id + "  " + p.siteName + "  " + p.city
                    + "  beds as listed: " + p.bedsText);
        }

        check("T1 total shelters", pools.size() == 107, "expected 107");
        Set<String> cities = new HashSet<String>();
        for (BedPool p : pools) { cities.add(p.city); }
        check("T2 distinct cities", cities.size() == 15, "expected 15, got " + cities.size());
        int vancouver = 0, closed = 0, hidden = 0, full = 0, noInfo = 0;
        int pet = 0, acc = 0, wom = 0, men = 0, mix = 0, you = 0, fam = 0;
        for (BedPool p : pools) {
            if ("Vancouver".equals(p.city)) { vancouver++; }
            if (!p.active) { closed++; }
            if (p.hiddenSite) { hidden++; }
            if ("Full".equals(p.bedsText)) { full++; }
            if (p.bedsText.toLowerCase().startsWith("no information")) { noInfo++; }
            if (p.attributes.contains("PET")) { pet++; }
            if (p.attributes.contains("ACC")) { acc++; }
            if ("WOM".equals(p.category)) { wom++; }
            if ("MEN".equals(p.category)) { men++; }
            if ("MIX".equals(p.category)) { mix++; }
            if ("YOU".equals(p.category)) { you++; }
            if ("FAM".equals(p.category)) { fam++; }
        }
        check("T3 Vancouver shelters", vancouver == 48, "expected 48, got " + vancouver);
        check("T4 CLOSED-badged (inactive)", closed == 27, "expected 27, got " + closed);
        check("T5 confidential addresses (hidden)", hidden == 19, "expected 19, got " + hidden);
        check("T6 listed as Full", full == 25, "expected 25, got " + full);
        check("T7 no-information listings kept unknown", noInfo == 37, "expected 37, got " + noInfo);
        check("T8 pet friendly / wheelchair accessible", pet == 55 && acc == 62, "expected PET 55 ACC 62, got PET " + pet + " ACC " + acc);
        check("T9 categories WOM/MEN/MIX/YOU/FAM", wom == 13 && men == 11 && mix == 59 && you == 18 && fam == 6, "expected 13/11/59/18/6, got " + wom + "/" + men + "/" + mix + "/" + you + "/" + fam);
        BedPool beacon = byName(pools, "The Beacon");
        check("T10 spot-check The Beacon", beacon != null
                && "Vancouver".equals(beacon.city)
                && "604-646-6846".equals(beacon.phone)
                && "Men: 5".equals(beacon.bedsText)
                && "MEN".equals(beacon.category) && beacon.openCount() == 5,
                beacon == null ? "not found" : beacon.city + ", " + beacon.phone + ", beds: " + beacon.bedsText);
        BedPool fraserside = byName(pools, "Fraserside Emergency Family Shelter");
        check("T11 spot-check Fraserside (family, confidential)", fraserside != null
                && fraserside.hiddenSite && "FAM".equals(fraserside.category),
                fraserside == null ? "not found" : "hidden=" + fraserside.hiddenSite + ", category=" + fraserside.category);
        BedPool kiwassa = byName(pools, "Kiwassa Neighbourhood House - EWR");
        check("T12 spot-check EWR site starts inactive", kiwassa != null
                && !kiwassa.active && kiwassa.openCount() == 0,
                kiwassa == null ? "not found" : "active=" + kiwassa.active);
        BedPool last = pools.get(pools.size() - 1);
        check("T13 last entry is Foundations (Chilliwack)",
                "Foundations Emergency Shelter (Previously Pathways Shelter)".equals(last.siteName)
                && "Any: 52".equals(last.bedsText),
                last.siteName + ", beds: " + last.bedsText);

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
