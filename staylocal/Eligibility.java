package staylocal;

import java.util.ArrayList;
import java.util.List;

/**
 * The eligibility engine — this is what a hotel site's "filters" become
 * when the listing is a shelter bed. Rules are HARD FILTERS, not rankings:
 * a pool that fails any rule is excluded, never just shown lower.
 *
 * A search also explains its exclusions — that explanation is what
 * becomes turn-away data when nothing matches.
 * Adapted from Tucked In's EligibilityEngine, rule for rule.
 */
public class Eligibility {

    /** Returns the reason this pool does NOT match, or null if it matches. */
    public static String exclusionReason(BedPool p, NeedProfile r, int scenarioMinutes) {
        if (!p.active) {
            return "site not activated (weather-response sites open only when activated)";
        }
        if (p.referralOnly && !r.referrerType.equals(p.referralTypeNeeded)) {
            return "referral-only: only " + p.referralTypeNeeded + " referrers can see this pool";
        }
        if (!p.category.equals(r.categoryNeeded)) {
            return "category: pool serves " + p.category + ", request needs " + r.categoryNeeded;
        }
        for (String need : r.attributesNeeded) {
            if (!p.attributes.contains(need)) {
                return "missing attribute: " + need;
            }
        }
        if (r.age > 0) {
            if (p.ageMin != -1 && r.age < p.ageMin) {
                return "age: below this pool's minimum (" + p.ageMin + ")";
            }
            if (p.ageMax != -1 && r.age > p.ageMax) {
                return "age: above this pool's maximum (" + p.ageMax + ")";
            }
        }
        if (!p.partyTypes.contains(r.party)) {
            return "party: pool does not take " + r.party + " parties";
        }
        if ("must-not-require-sobriety".equals(r.sobrietyNeed)
                && "program-required".equals(p.sobrietyStance)) {
            return "sobriety: this is a sobriety-program site, request is low-barrier";
        }
        if (p.intakeKnown && !p.intakeOpenAt(scenarioMinutes)) {
            return "intake closed at this time (" + p.intakeOpen + "-" + p.intakeClose + ")";
        }
        if (!p.holdableFreshness()) {
            return "availability not verified recently - call to verify";
        }
        if (p.openCount() <= 0) {
            return "full: no open " + p.countUnit;
        }
        return null;
    }

    public static List<BedPool> search(List<BedPool> pools, NeedProfile r, int scenarioMinutes) {
        List<BedPool> matches = new ArrayList<BedPool>();
        for (BedPool p : pools) {
            if (exclusionReason(p, r, scenarioMinutes) == null) {
                matches.add(p);
            }
        }
        return matches;
    }

    /**
     * Suggests a turn-away code for a search with no matches, so the
     * failure is recorded as data (taxonomy codes: FULL, ATTR, TIME, AREA).
     */
    public static String suggestTurnAwayCode(List<BedPool> pools, NeedProfile r, int scenarioMinutes) {
        boolean categoryExists = false;
        boolean anyOpen = false;
        boolean intakeClosedOnly = false;
        for (BedPool p : pools) {
            if (!p.active || p.referralOnly) {
                continue;
            }
            if (!p.category.equals(r.categoryNeeded)) {
                continue;
            }
            categoryExists = true;
            if (p.openCount() > 0) {
                anyOpen = true;
            }
            String reason = exclusionReason(p, r, scenarioMinutes);
            if (reason != null && reason.startsWith("intake closed")) {
                intakeClosedOnly = true;
            }
        }
        if (!categoryExists) {
            return "ATTR";
        }
        if (!anyOpen) {
            return "FULL";
        }
        if (intakeClosedOnly) {
            return "TIME";
        }
        return "ATTR";
    }
}
