package staylocal;

import java.util.Arrays;
import java.util.List;

/**
 * Phase 25 check main for language and safety on holds: the
 * shelter's one-line brief leads with the language (and flags an
 * interpreter for anything but English), carries the safety flags,
 * and still reads "no arrival needs noted" when nothing was noted.
 * Run this file in NetBeans: every test should print PASS.
 */
public class LanguageCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) {
        List<BedPool> pools = ShelterData.allShelters();
        HoldManager m = new HoldManager(pools, 90);
        BedPool beacon = null;
        for (BedPool p : pools) {
            if (p.siteName.equals("The Beacon")) {
                beacon = p;
            }
        }

        Hold h = m.placeHold(beacon.id, "Person A", 1,
                Arrays.asList("Gloves / socks / warm clothing"), "", false,
                "Punjabi", Arrays.asList("Fleeing violence - discreet arrival"));
        String brief = h.needsSummary();
        check("T1 the brief leads with language, interpreter and safety",
                brief.contains("Language: Punjabi - interpreter needed")
                        && brief.contains("Safety: Fleeing violence - discreet arrival")
                        && brief.contains("Gloves / socks / warm clothing"),
                brief);

        Hold e = m.placeHold(beacon.id, "Person B", 1, null, "", false, "English", null);
        check("T2 English is noted without an interpreter flag",
                e.needsSummary().contains("Language: English")
                        && !e.needsSummary().contains("interpreter"),
                e.needsSummary());

        Hold plain = m.placeHold(beacon.id, "Person C");
        check("T3 a hold with nothing noted still reads plainly",
                "no arrival needs noted".equals(plain.needsSummary()) && !plain.hasNeeds(),
                plain.needsSummary());

        Hold s = m.placeHold(beacon.id, "Person D", 1, null, "", false, "",
                Arrays.asList("Service animal arriving"));
        check("T4 a safety-only hold counts as something the shelter must know",
                s.hasNeeds() && s.needsSummary().contains("Safety: Service animal arriving")
                        && s.describe(System.currentTimeMillis()).contains("prepare:"),
                s.needsSummary());

        check("T5 the language catalogue covers Metro Vancouver's most common",
                Hold.LANGUAGES.length >= 10
                        && Arrays.asList(Hold.LANGUAGES).contains("Punjabi")
                        && Arrays.asList(Hold.LANGUAGES).contains("Cantonese")
                        && Arrays.asList(Hold.LANGUAGES).contains("Tagalog"),
                Hold.LANGUAGES.length + " languages offered");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
