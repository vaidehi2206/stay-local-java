package staylocal;

import java.util.List;

/**
 * Building new shelter listings for the admin's "Add shelter" action.
 * An admin-added listing is local data the admin typed in (in
 * production it would go through the same verification as any new
 * shelter before it appears); it is labelled as admin-added wherever
 * it shows, and it joins the same live inventory as the 211 listings,
 * so workers can find it and hold its beds immediately.
 */
public class AdminOps {

    /** Creates a pool from the admin's form values (id assigned on add). */
    public static BedPool newShelter(String name, String city, String address,
            String phone, String category, int openBeds) {
        BedPool p = new BedPool();
        p.siteName = name;
        p.city = city;
        p.area = city;
        p.address = address == null ? "" : address;
        p.phone = phone == null ? "" : phone;
        p.hiddenSite = "Address confidential".equals(p.address);
        p.category = category;
        p.servesText = ShelterSearch.categoryLabel(category);
        if ("YOU".equals(category)) {
            p.agesText = "12-24";
            p.ageMin = 12;
            p.ageMax = 24;
        } else {
            p.agesText = "19+";
            p.ageMin = 19;
            p.ageMax = -1;
        }
        p.physical = Math.max(openBeds, 0);
        p.bedsText = p.physical + " beds (added by admin)";
        p.shelterType = "Admin added";
        p.updatedText = "Added by admin (this session)";
        p.lastUpdatedMinAgo = 0;
        p.intakeOpen = "00:00";
        p.intakeClose = "24:00";
        p.sobrietyStance = "tolerant";
        if ("FAM".equals(category)) {
            p.countUnit = "family units";
            p.partyTypes.add("family");
        } else if ("YOU".equals(category)) {
            p.partyTypes.add("single");
        } else {
            p.partyTypes.add("single");
            p.partyTypes.add("couple");
        }
        return p;
    }

    /** Assigns the next admin id and registers the pool everywhere. */
    public static String addShelter(List<BedPool> pools, HoldManager manager, BedPool p) {
        int next = 1;
        for (BedPool existing : pools) {
            if (existing.id != null && existing.id.startsWith("admin-")) {
                try {
                    int n = Integer.parseInt(existing.id.substring("admin-".length()));
                    if (n >= next) {
                        next = n + 1;
                    }
                } catch (NumberFormatException ignored) {
                    // not an admin-numbered id; skip
                }
            }
        }
        p.id = "admin-" + next;
        manager.registerPool(p);
        return p.id;
    }
}
