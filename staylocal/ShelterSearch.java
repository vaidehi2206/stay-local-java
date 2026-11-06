package staylocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Directory search over the BC 211 shelter listings — the Booking-style
 * search form (city, who it's for, needs, sort) applied to shelters.
 *
 * This is a DIRECTORY search, not the eligibility engine: it answers
 * "which listings fit this person, as far as 211's listing states it".
 * The hard eligibility rules (intake windows, freshness, live holds)
 * belong to the hold flow and to Eligibility, and they need live data
 * a twice-a-day directory snapshot cannot honestly supply.
 *
 * Serves matching uses the app categories derived from 211's "Serves":
 *   WOMEN  -> women-only, any-gender and family pools can take a woman
 *   MEN    -> men-only and any-gender pools
 *   YOUTH  -> youth pools only (adult pools state their own age bands)
 *   FAMILY -> family pools only
 * CLOSED-badged sites (mostly EWR) are excluded unless includeClosed,
 * because 211 was showing them closed at capture time.
 *
 * Phase 7: EVERY field on a 211 listing is also a filter, so a health
 * or outreach worker can narrow by the person's actual necessities,
 * not just by availability:
 *   listing field        filter
 *   -------------        ------
 *   City                 city
 *   Serves               serves (above)
 *   Ages                 age (the pool's stated band must contain it)
 *   Beds Available       bedsStatus: OPEN (a number is stated),
 *                        FULL (listed "Full"), UNKNOWN (211 stated
 *                        "No information available") — plus openOnly
 *   Features             petFriendly, wheelchair, largeItems
 *                        ("Takes Large Items")
 *   Type / tags          type: "24 Hour", "Overnight", "Seasonal",
 *                        "Extreme Weather", "First Come-First Serve"
 *   Address              confidentialOnly (safety-hidden sites)
 *   Updated              updatedWithinMin (listing freshness)
 *   Name + Notes         keyword (e.g. "harm reduction", "storage",
 *                        "sober" — the necessity is often in the notes)
 * The full listing still displays as the description on the detail
 * screen; the filters only narrow which listings are shown.
 */
public class ShelterSearch {

    public static final String SORT_DIRECTORY = "Directory order";
    public static final String SORT_NAME = "Name A-Z";
    public static final String SORT_MOST_BEDS = "Most beds listed";
    public static final String SORT_RECENT = "Most recently updated";
    public static final String SORT_CITY = "City, then name";
    public static final String SORT_NEAREST = "Nearest first";

    public String city = "";            // "" = any city
    public String serves = "ANY";       // ANY, WOMEN, MEN, YOUTH, FAMILY
    public String party = "";           // "", single, couple, family (Phase 12)
    public int age = 0;                 // 0 = any age
    public boolean petFriendly = false;
    public boolean wheelchair = false;
    public boolean openOnly = false;    // only listings stating open beds
    public boolean includeClosed = false;
    public String sort = SORT_DIRECTORY;
    public String nearZone = "";        // Phase 24: area to measure distance from

    // Phase 7: the rest of the listing, as filters
    public String keyword = "";         // matches name + notes, case-insensitive
    public boolean largeItems = false;  // "Takes Large Items" feature
    public String type = "";            // "" = any, else a tag from the listing
    public String bedsStatus = "ANY";   // ANY, OPEN, FULL, UNKNOWN
    public int updatedWithinMin = -1;   // -1 = any, else max minutes since 211 updated it
    public boolean confidentialOnly = false; // address-confidential sites only
    public boolean intakeOpenNow = false; // Phase 14: hide only listings whose KNOWN window is closed now
    public boolean lowBarrierOnly = false; // Phase 19: no sobriety program required
    public boolean harmReductionMentioned = false; // Phase 19: listing's own words mention harm reduction
    public boolean healthMentioned = false; // Phase 19: listing's own words mention health services

    public static boolean acceptsCategory(String poolCategory, String servesNeed) {
        if ("ANY".equals(servesNeed)) {
            return true;
        }
        if ("WOMEN".equals(servesNeed)) {
            return "WOM".equals(poolCategory) || "MIX".equals(poolCategory)
                    || "FAM".equals(poolCategory);
        }
        if ("MEN".equals(servesNeed)) {
            return "MEN".equals(poolCategory) || "MIX".equals(poolCategory);
        }
        if ("YOUTH".equals(servesNeed)) {
            return "YOU".equals(poolCategory);
        }
        if ("FAMILY".equals(servesNeed)) {
            return "FAM".equals(poolCategory);
        }
        return true;
    }

    public static boolean matches(BedPool p, ShelterSearch c) {
        if (p.onHold) {
            return false; // an admin hold hides the listing from workers entirely
        }
        if (!c.includeClosed && !p.active) {
            return false;
        }
        if (!c.city.isEmpty() && !c.city.equals(p.city)) {
            return false;
        }
        if (!acceptsCategory(p.category, c.serves)) {
            return false;
        }
        if (!c.party.isEmpty() && !p.partyTypes.contains(c.party)) {
            return false; // the listing does not take this party (couple, family...)
        }
        if (c.age > 0) {
            if (p.ageMin != -1 && c.age < p.ageMin) {
                return false;
            }
            if (p.ageMax != -1 && c.age > p.ageMax) {
                return false;
            }
        }
        if (c.petFriendly && !p.attributes.contains("PET")) {
            return false;
        }
        if (c.wheelchair && !p.attributes.contains("ACC")) {
            return false;
        }
        if (c.openOnly && !(p.active && p.physical > 0)) {
            return false;
        }
        if (c.largeItems && !p.featuresText.contains("Takes Large Items")) {
            return false;
        }
        if (c.confidentialOnly && !p.hiddenSite) {
            return false;
        }
        if (!c.type.isEmpty() && !p.shelterType.contains(c.type)) {
            return false;
        }
        if ("OPEN".equals(c.bedsStatus) && !(p.active && p.physical > 0)) {
            return false;
        }
        if ("FULL".equals(c.bedsStatus) && !"Full".equals(p.bedsText)) {
            return false;
        }
        if ("UNKNOWN".equals(c.bedsStatus)
                && !p.bedsText.toLowerCase().startsWith("no information")) {
            return false;
        }
        if (c.updatedWithinMin >= 0 && p.lastUpdatedMinAgo > c.updatedWithinMin) {
            return false;
        }
        if (c.lowBarrierOnly && "program-required".equals(p.sobrietyStance)) {
            return false;
        }
        if (c.harmReductionMentioned && !ShelterFlags.mentionsHarmReduction(p)) {
            return false;
        }
        if (c.healthMentioned && !ShelterFlags.mentionsHealth(p)) {
            return false;
        }
        if (c.intakeOpenNow && p.intakeKnown) {
            java.time.LocalTime now = java.time.LocalTime.now();
            if (!p.intakeOpenAt(now.getHour() * 60 + now.getMinute())) {
                return false;
            }
        }
        if (!c.keyword.isEmpty()) {
            String haystack = (p.siteName + " " + p.notes + " " + p.intakeText).toLowerCase();
            if (!haystack.contains(c.keyword.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public static List<BedPool> search(List<BedPool> pools, ShelterSearch c) {
        List<BedPool> out = new ArrayList<BedPool>();
        for (BedPool p : pools) {
            if (matches(p, c)) {
                out.add(p);
            }
        }
        if (SORT_NAME.equals(c.sort)) {
            Collections.sort(out, new Comparator<BedPool>() {
                public int compare(BedPool a, BedPool b) {
                    return a.siteName.compareTo(b.siteName);
                }
            });
        } else if (SORT_MOST_BEDS.equals(c.sort)) {
            Collections.sort(out, new Comparator<BedPool>() {
                public int compare(BedPool a, BedPool b) {
                    if (a.physical != b.physical) {
                        return b.physical - a.physical;
                    }
                    return a.siteName.compareTo(b.siteName);
                }
            });
        } else if (SORT_RECENT.equals(c.sort)) {
            Collections.sort(out, new Comparator<BedPool>() {
                public int compare(BedPool a, BedPool b) {
                    if (a.lastUpdatedMinAgo != b.lastUpdatedMinAgo) {
                        return a.lastUpdatedMinAgo - b.lastUpdatedMinAgo;
                    }
                    return a.siteName.compareTo(b.siteName);
                }
            });
        } else if (SORT_NEAREST.equals(c.sort) && !c.nearZone.isEmpty()) {
            final String near = c.nearZone;
            Collections.sort(out, new Comparator<BedPool>() {
                public int compare(BedPool a, BedPool b) {
                    double da = ShelterZones.distanceKm(ShelterZones.zoneOf(a), near);
                    double db = ShelterZones.distanceKm(ShelterZones.zoneOf(b), near);
                    if (da < 0 && db < 0) {
                        return 0;
                    }
                    if (da < 0) {
                        return 1;
                    }
                    if (db < 0) {
                        return -1;
                    }
                    return Double.compare(da, db);
                }
            });
        } else if (SORT_CITY.equals(c.sort)) {
            Collections.sort(out, new Comparator<BedPool>() {
                public int compare(BedPool a, BedPool b) {
                    int byCity = a.city.compareTo(b.city);
                    if (byCity != 0) {
                        return byCity;
                    }
                    return a.siteName.compareTo(b.siteName);
                }
            });
        }
        return out;
    }

    /** Distinct cities in the directory, alphabetical — for the search form. */
    public static List<String> cities(List<BedPool> pools) {
        TreeSet<String> set = new TreeSet<String>();
        for (BedPool p : pools) {
            set.add(p.city);
        }
        return new ArrayList<String>(set);
    }

    /** Display label for a pool category code. */
    public static String categoryLabel(String category) {
        if ("WOM".equals(category)) {
            return "Women";
        }
        if ("MEN".equals(category)) {
            return "Men";
        }
        if ("YOU".equals(category)) {
            return "Youth";
        }
        if ("FAM".equals(category)) {
            return "Families";
        }
        return "Any gender";
    }
}
