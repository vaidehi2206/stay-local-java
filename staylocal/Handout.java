package staylocal;

import java.util.List;

/**
 * Phase 20: the printable handout. BC 211 lets you print its list;
 * a person walking to a shelter tonight deserves the same — one
 * sheet with the shelter, the address, the phone, the intake
 * window, and (for a hold) what is being kept for them. Printed
 * through the system print dialog, so "Print to PDF" works too.
 *
 * Confidentiality rule: a hidden site's address is never printed —
 * the handout says to call, exactly like the app does.
 */
public class Handout {

    private static final String RULE =
            "------------------------------------------------------------";

    private static String addressLine(BedPool p) {
        if (p.hiddenSite) {
            return "Address confidential - call the shelter or ask your worker";
        }
        return p.address + ", " + p.city;
    }

    private static String whoCanCome(BedPool p) {
        StringBuilder sb = new StringBuilder();
        if (p.partyTypes.contains("single")) {
            sb.append("individuals");
        }
        if (p.partyTypes.contains("couple")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("couples");
        }
        if (p.partyTypes.contains("family")) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("families with children");
        }
        return sb.toString();
    }

    private static String footer() {
        return RULE + "\n"
                + "Listings: BC 211 Shelter List, retrieved Nov 5, 2026.\n"
                + "Availability changes fast - call ahead to confirm.\n"
                + "If this shelter is full, call 2-1-1 (free, 24/7) for the next option.\n";
    }

    /** One shelter, for the person travelling to it. */
    public static String forShelter(BedPool p, String dateText) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHELTER HANDOUT - ").append(dateText).append("\n");
        sb.append(RULE).append("\n");
        sb.append(p.siteName).append(" (").append(p.city).append(")\n");
        sb.append("Address: ").append(addressLine(p)).append("\n");
        sb.append("Phone: ").append(p.phone).append("\n");
        sb.append("Beds open right now (in this app): ").append(p.openCount()).append("\n");
        sb.append("Serves: ").append(p.servesText).append("   Ages: ").append(p.agesText).append("\n");
        sb.append("Who can come: ").append(whoCanCome(p)).append("\n");
        if (p.intakeKnown) {
            sb.append("Intake window: ").append(p.intakeOpen).append(" - ").append(p.intakeClose).append("\n");
        }
        if (!p.intakeText.isEmpty()) {
            sb.append("Intake info (BC 211): ").append(p.intakeText).append("\n");
        }
        sb.append("Sobriety: ").append("program-required".equals(p.sobrietyStance)
                ? "sobriety program - guests must be sober" : "no sobriety program required").append("\n");
        if (!p.notes.isEmpty()) {
            sb.append("Notes: ").append(p.notes).append("\n");
        }
        sb.append(footer());
        return sb.toString();
    }

    /** A confirmed/held bed, for the person carrying it to the door. */
    public static String forHold(Hold h, BedPool p, String dateText, long nowMillis) {
        StringBuilder sb = new StringBuilder();
        sb.append("YOUR BED HOLD - ").append(dateText).append("\n");
        sb.append(RULE).append("\n");
        sb.append("Shelter: ").append(h.siteName).append("\n");
        sb.append("Hold #").append(h.id).append("   Status: ").append(h.status)
                .append("   Beds: ").append(h.beds).append("\n");
        sb.append("Held for: ").append(h.profileLabel).append("\n");
        if (Hold.HELD.equals(h.status)) {
            long s = h.secondsLeft(nowMillis);
            sb.append("Please arrive within: ").append(s / 60).append(" min ")
                    .append(String.format("%02d", s % 60)).append(" sec (demo timing)\n");
        }
        if (h.etaMinutes > 0) {
            sb.append("Shelter told to expect you in about ").append(h.etaMinutes).append(" min\n");
        }
        sb.append("Shelter is preparing: ").append(h.needsSummary()).append("\n");
        if (p != null) {
            sb.append("Address: ").append(addressLine(p)).append("\n");
            sb.append("Phone: ").append(p.phone).append("\n");
            if (p.intakeKnown) {
                sb.append("Intake window: ").append(p.intakeOpen).append(" - ").append(p.intakeClose).append("\n");
            }
        }
        sb.append(footer());
        return sb.toString();
    }

    /** The current search results, 211-style: a list to work through. */
    public static String forResults(String searchSummary, List<BedPool> results, String dateText) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHELTERS MATCHING: ").append(searchSummary).append(" - ").append(dateText).append("\n");
        sb.append(RULE).append("\n");
        int shown = Math.min(results.size(), 15);
        for (int i = 0; i < shown; i++) {
            BedPool p = results.get(i);
            sb.append(i + 1).append(". ").append(p.siteName).append(" (").append(p.city).append(")")
                    .append(" - beds open: ").append(p.openCount())
                    .append(" - ").append(p.phone).append("\n");
        }
        if (results.size() > shown) {
            sb.append("... and ").append(results.size() - shown).append(" more in the app.\n");
        }
        if (results.isEmpty()) {
            sb.append("No shelters matched. Call 2-1-1 - a navigator will help.\n");
        }
        sb.append(footer());
        return sb.toString();
    }
}
