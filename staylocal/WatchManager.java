package staylocal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Keeps the worker's watches and fires availability alerts.
 *
 * A shelter counts as AVAILABLE for a watch when it matches the
 * watch's criteria AND has at least one bed open right now
 * (openCount already respects holds, closures and admin holds).
 * check() compares the available set against the last check:
 * shelters that newly became available produce an alert; shelters
 * that were already available do not re-alert; if a shelter fills
 * and later frees again, it alerts again — because it IS available
 * again, and someone may still be waiting.
 *
 * The app calls check() every couple of seconds (the same tick that
 * expires holds), so alerts arrive whichever dashboard caused the
 * change — shelter console, a cancelled hold, an expired hold, an
 * admin addition — without any of them knowing watches exist.
 */
public class WatchManager {

    public static class Alert {
        public final String watchLabel;
        public final String poolId;
        public final String siteName;
        public final String city;
        public final int bedsOpen;
        public final String timeText;

        public Alert(String watchLabel, BedPool pool, String timeText) {
            this.watchLabel = watchLabel;
            this.poolId = pool.id;
            this.siteName = pool.siteName;
            this.city = pool.city;
            this.bedsOpen = pool.openCount();
            this.timeText = timeText;
        }

        public String describe() {
            return timeText + "  —  " + siteName + " (" + city + ")  —  "
                    + bedsOpen + (bedsOpen == 1 ? " bed" : " beds")
                    + " open  —  for: " + watchLabel;
        }
    }

    private final List<Watch> watches = new ArrayList<Watch>();
    private final List<Alert> alerts = new ArrayList<Alert>();

    public Watch addWatch(String label, ShelterSearch criteria,
            List<BedPool> pools, String timeText) {
        Watch w = new Watch(label, criteria, timeText);
        w.lastAvailableIds.addAll(availableIds(pools, criteria));
        watches.add(w);
        return w;
    }

    /** A stable fingerprint of a search, so the app can tell whether
     *  this exact search already has a notify request. */
    public static String signatureOf(ShelterSearch c) {
        return c.city + "|" + c.serves + "|" + c.party + "|" + c.age + "|"
                + c.petFriendly + "|" + c.wheelchair + "|" + c.largeItems + "|"
                + c.confidentialOnly + "|" + c.includeClosed + "|" + c.openOnly + "|"
                + c.keyword + "|" + c.type + "|" + c.bedsStatus + "|"
                + c.updatedWithinMin + "|" + c.sort + "|"
                + c.intakeOpenNow + "|" + c.lowBarrierOnly + "|"
                + c.harmReductionMentioned + "|" + c.healthMentioned;
    }

    /** The existing request for exactly this search, or null. */
    public Watch findBySignature(String signature) {
        for (Watch w : watches) {
            if (signatureOf(w.criteria).equals(signature)) {
                return w;
            }
        }
        return null;
    }

    /**
     * Points an existing watch at a changed search (the worker edited
     * the filters while the alert checkbox is on). The baseline resets
     * to what is available NOW, so the edit itself never fires an
     * alert — only beds that open afterwards do.
     */
    public void updateWatch(Watch w, String newLabel, ShelterSearch criteria,
            List<BedPool> pools) {
        w.label = newLabel;
        w.criteria = criteria;
        w.lastAvailableIds.clear();
        w.lastAvailableIds.addAll(availableIds(pools, criteria));
    }

    public List<Watch> watches() {
        return new ArrayList<Watch>(watches);
    }

    public boolean removeWatch(Watch w) {
        return watches.remove(w);
    }

    /** Alerts not yet dismissed, oldest first. */
    public List<Alert> alerts() {
        return new ArrayList<Alert>(alerts);
    }

    public boolean dismissAlert(Alert a) {
        return alerts.remove(a);
    }

    public int alertCount() {
        return alerts.size();
    }

    /** How many shelters match this watch with a bed open, right now. */
    public int currentMatches(Watch w, List<BedPool> pools) {
        return availableIds(pools, w.criteria).size();
    }

    private Set<String> availableIds(List<BedPool> pools, ShelterSearch criteria) {
        Set<String> ids = new LinkedHashSet<String>();
        for (BedPool p : pools) {
            if (ShelterSearch.matches(p, criteria) && p.openCount() > 0) {
                ids.add(p.id);
            }
        }
        return ids;
    }

    /**
     * One check across all watches. Returns only the alerts fired by
     * THIS check (also kept in alerts() until dismissed).
     */
    public List<Alert> check(List<BedPool> pools, String timeText) {
        List<Alert> fired = new ArrayList<Alert>();
        for (Watch w : watches) {
            Set<String> now = availableIds(pools, w.criteria);
            for (BedPool p : pools) {
                if (now.contains(p.id) && !w.lastAvailableIds.contains(p.id)) {
                    Alert a = new Alert(w.label, p, timeText);
                    alerts.add(a);
                    fired.add(a);
                }
            }
            w.lastAvailableIds.clear();
            w.lastAvailableIds.addAll(now);
        }
        return fired;
    }
}
