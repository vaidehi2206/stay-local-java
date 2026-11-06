package staylocal;

import java.util.HashSet;
import java.util.Set;

/**
 * One worker's saved search — a "watch". The worker ran a search for
 * a person and nothing (or not enough) was available; instead of the
 * need evaporating, the search stays alive in the app. The moment a
 * shelter matching it has a bed open — a shelter updates its count,
 * a hold expires or is cancelled, a site opens, the admin adds one —
 * the watch fires an availability alert, so the worker can reach
 * back out to the person. No bed sits empty while someone sleeps
 * outside for want of a re-check.
 */
public class Watch {

    public String label;            // auto-summary of the search, e.g. "Women - a couple - Vancouver"
    public ShelterSearch criteria;  // follows the worker's current search while the watch is on
    public final String createdAt;        // display time, e.g. "5:41 PM"

    /** Shelters that matched WITH a bed open at the last check. */
    public final Set<String> lastAvailableIds = new HashSet<String>();

    public Watch(String label, ShelterSearch criteria, String createdAt) {
        this.label = label;
        this.criteria = criteria;
        this.createdAt = createdAt;
    }
}
