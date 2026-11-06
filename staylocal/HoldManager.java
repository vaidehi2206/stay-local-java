package staylocal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the live inventory state and every hold transition.
 *
 * The concurrency rule the whole product depends on:
 * placeHold() is synchronized and checks-and-decrements in one step,
 * so if two workers reach for the last bed in the same moment,
 * exactly one of them gets it. No overselling, ever.
 *
 * Adapted from Tucked In's HoldManager, with cancelHold added.
 * In this app the counts start from the BC 211 snapshot: a hold can
 * only be placed on a pool whose listing states open beds, and the
 * counts move only inside this app session — nothing here writes
 * back to 211 or to the shelter.
 */
public class HoldManager {

    private final List<BedPool> pools;
    private final Map<String, BedPool> byId;
    private final List<Hold> holds;
    private final List<String> eventLog;
    private final List<String> turnAways;
    private int nextHoldId = 1;
    private final int holdSeconds;

    /** holdSeconds is short in the demo (production default is 45 minutes). */
    public HoldManager(List<BedPool> pools, int holdSeconds) {
        this.pools = pools;
        this.holdSeconds = holdSeconds;
        this.byId = new HashMap<String, BedPool>();
        for (BedPool p : pools) {
            byId.put(p.id, p);
        }
        this.holds = new ArrayList<Hold>();
        this.eventLog = new ArrayList<String>();
        this.turnAways = new ArrayList<String>();
    }

    public List<BedPool> pools() {
        return pools;
    }

    public BedPool pool(String id) {
        return byId.get(id);
    }

    public synchronized Hold placeHold(String poolId, String profileLabel) {
        return placeHold(poolId, profileLabel, 1, null, "", false);
    }

    /**
     * Phase 15: a hold takes `beds` beds, all-or-nothing — a couple
     * or a family is never silently split — and carries the arrival
     * needs the shelter should prepare (supplies, accessibility,
     * medical). Counts move in beds, not in holds.
     */
    public synchronized Hold placeHold(String poolId, String profileLabel, int beds,
            java.util.List<String> needs, String needsNote, boolean urgent) {
        return placeHold(poolId, profileLabel, beds, needs, needsNote, urgent, "", null);
    }

    /**
     * Phase 25: a hold also carries the language the person speaks
     * best and any safety flags the shelter must know before the
     * person arrives (e.g. fleeing violence - keep arrival discreet).
     */
    public synchronized Hold placeHold(String poolId, String profileLabel, int beds,
            java.util.List<String> needs, String needsNote, boolean urgent,
            String language, java.util.List<String> safety) {
        BedPool p = byId.get(poolId);
        if (p == null || beds < 1 || p.openCount() < beds) {
            return null; // not enough open beds for the whole party
        }
        p.pendingHolds += beds;
        Hold h = new Hold();
        h.id = nextHoldId++;
        h.poolId = poolId;
        h.siteName = p.hiddenSite ? "Confidential site (relay)" : p.siteName;
        h.profileLabel = profileLabel;
        h.beds = beds;
        if (needs != null) {
            h.needs.addAll(needs);
        }
        h.needsNote = needsNote == null ? "" : needsNote;
        h.urgent = urgent;
        h.language = language == null ? "" : language;
        if (safety != null) {
            h.safety.addAll(safety);
        }
        h.status = Hold.HELD;
        h.expiresAtMillis = System.currentTimeMillis() + holdSeconds * 1000L;
        holds.add(h);
        log("HOLD PLACED  #" + h.id + "  " + h.siteName + "  for: " + profileLabel
                + "  -  " + beds + (beds == 1 ? " bed" : " beds")
                + (h.hasNeeds() ? "  -  prepare: " + h.needsSummary() : ""));
        return h;
    }

    public synchronized void confirmHold(Hold h) {
        if (!Hold.HELD.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.pendingHolds -= h.beds;
        p.confirmedHolds += h.beds;
        h.status = Hold.CONFIRMED;
        log("HOLD CONFIRMED  #" + h.id + "  " + h.siteName);
    }

    public synchronized void rejectHold(Hold h, String reason) {
        if (!Hold.HELD.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.pendingHolds -= h.beds;
        h.status = Hold.REJECTED;
        h.rejectReason = reason;
        log("HOLD REJECTED  #" + h.id + "  " + h.siteName + "  reason: " + reason);
    }

    public synchronized void checkIn(Hold h) {
        if (!Hold.CONFIRMED.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.confirmedHolds -= h.beds;
        p.checkedIn += h.beds;
        h.status = Hold.CHECKED_IN;
        log("CHECKED IN  #" + h.id + "  " + h.siteName);
    }

    public synchronized void noShow(Hold h) {
        if (!Hold.CONFIRMED.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.confirmedHolds -= h.beds;
        h.status = Hold.NO_SHOW;
        turnAways.add("NSHW - " + h.siteName + " - confirmed hold #" + h.id + " did not arrive");
        log("NO-SHOW  #" + h.id + "  " + h.siteName + "  (bed released, logged as NSHW)");
    }

    /** The requester backs out: the bed goes straight back to the pool. */
    public synchronized void cancelHold(Hold h) {
        BedPool p = byId.get(h.poolId);
        if (Hold.HELD.equals(h.status)) {
            p.pendingHolds -= h.beds;
        } else if (Hold.CONFIRMED.equals(h.status)) {
            p.confirmedHolds -= h.beds;
        } else {
            return;
        }
        h.status = Hold.CANCELLED;
        log("HOLD CANCELLED  #" + h.id + "  " + h.siteName + "  (bed released)");
    }

    /**
     * Phase 16: one more hold window on a live hold — the worker is
     * running late and the bed should not fall back into the pool yet.
     * (Demo window 90 s; production window 45 min, shelter-set.)
     */
    public synchronized boolean extendHold(Hold h) {
        if (!Hold.HELD.equals(h.status)) {
            return false;
        }
        h.expiresAtMillis += holdSeconds * 1000L;
        log("HOLD EXTENDED  #" + h.id + "  " + h.siteName);
        return true;
    }

    /** Phase 16: the worker tells the shelter when the person will arrive. */
    public synchronized boolean setEta(Hold h, int etaMinutes) {
        if (etaMinutes <= 0
                || !(Hold.HELD.equals(h.status) || Hold.CONFIRMED.equals(h.status))) {
            return false;
        }
        h.etaMinutes = etaMinutes;
        log("ETA SET  #" + h.id + "  " + h.siteName + "  arriving in ~" + etaMinutes + " min");
        return true;
    }

    /** Expires any HELD hold whose countdown has run out. Returns how many expired. */
    public synchronized int expireHolds() {
        long now = System.currentTimeMillis();
        int expired = 0;
        for (Hold h : holds) {
            if (Hold.HELD.equals(h.status) && now >= h.expiresAtMillis) {
                BedPool p = byId.get(h.poolId);
                p.pendingHolds -= h.beds;
                h.status = Hold.EXPIRED;
                expired++;
                log("HOLD EXPIRED  #" + h.id + "  " + h.siteName + "  (bed returned to the pool)");
            }
        }
        return expired;
    }

    /**
     * Shelter console control: set how many beds are open right now.
     * Beds already spoken for (pending + confirmed + checked in) are
     * never taken away — the pool total adjusts around them, so this
     * can only change the OPEN remainder. Returns false if refused.
     */
    public synchronized boolean setOpenCount(String poolId, int open) {
        BedPool p = byId.get(poolId);
        if (p == null || open < 0) {
            return false;
        }
        int spokenFor = p.checkedIn + p.confirmedHolds + p.pendingHolds + p.walkInReserve;
        p.physical = spokenFor + open;
        log("CONSOLE SET OPEN  " + p.siteName + "  open beds now " + p.openCount());
        return true;
    }

    /**
     * Phase 21: beds kept back for people who walk in without a hold.
     * The reserve can never eat into beds already spoken for.
     */
    public synchronized boolean setWalkInReserve(String poolId, int reserve) {
        BedPool p = byId.get(poolId);
        if (p == null || reserve < 0) {
            return false;
        }
        if (reserve > p.openCount() + p.walkInReserve) {
            return false;
        }
        p.walkInReserve = reserve;
        log("WALK-IN RESERVE  " + p.siteName + "  set to " + reserve);
        return true;
    }

    /**
     * Phase 23: someone (the shelter, or a worker who just called)
     * confirms this listing is current - its freshness resets.
     */
    public synchronized boolean markVerified(String poolId) {
        BedPool p = byId.get(poolId);
        if (p == null) {
            return false;
        }
        p.lastUpdatedMinAgo = 0;
        log("VERIFIED  " + p.siteName + "  listing confirmed current");
        return true;
    }

    /** Phase 21: this shelter's day at a glance, for the console. */
    public synchronized String shelterDaySummary(String poolId) {
        BedPool p = byId.get(poolId);
        if (p == null) {
            return "";
        }
        int placed = 0, held = 0, confirmed = 0, checked = 0, noShow = 0,
                failed = 0, expired = 0, cancelled = 0, rejected = 0;
        for (Hold h : holds) {
            if (!h.poolId.equals(poolId)) {
                continue;
            }
            placed++;
            if (Hold.HELD.equals(h.status)) {
                held++;
            } else if (Hold.CONFIRMED.equals(h.status)) {
                confirmed++;
            } else if (Hold.CHECKED_IN.equals(h.status)) {
                checked++;
            } else if (Hold.NO_SHOW.equals(h.status)) {
                noShow++;
            } else if (Hold.FAILED_ARRIVAL.equals(h.status)) {
                failed++;
            } else if (Hold.EXPIRED.equals(h.status)) {
                expired++;
            } else if (Hold.CANCELLED.equals(h.status)) {
                cancelled++;
            } else if (Hold.REJECTED.equals(h.status)) {
                rejected++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("DAY SUMMARY - ").append(p.siteName).append(" (").append(p.city).append(")\n");
        sb.append("Beds: total ").append(p.physical).append("  -  open ").append(p.openCount())
                .append("  -  walk-in reserve ").append(p.walkInReserve).append("\n");
        sb.append("Holds this session: ").append(placed).append(" placed\n");
        sb.append("  held now ").append(held).append("  -  confirmed ").append(confirmed)
                .append("  -  checked in ").append(checked).append("\n");
        sb.append("  no-show ").append(noShow).append("  -  failed arrivals ").append(failed)
                .append("  -  expired ").append(expired).append("  -  cancelled ").append(cancelled)
                .append("  -  rejected ").append(rejected).append("\n");
        return sb.toString();
    }

    /** Shelter console control: open or close the site (EWR sites start closed). */
    public synchronized void setActive(String poolId, boolean active) {
        BedPool p = byId.get(poolId);
        if (p == null || p.active == active) {
            return;
        }
        p.active = active;
        log("CONSOLE " + (active ? "OPENED" : "CLOSED") + " SITE  " + p.siteName);
    }

    // ---- Admin controls (Phase 9): the admin manages the directory ----

    /** Admin adds a shelter: it joins the live inventory immediately. */
    public synchronized void registerPool(BedPool p) {
        if (!pools.contains(p)) {
            pools.add(p);
        }
        byId.put(p.id, p);
        log("ADMIN ADDED SHELTER  " + p.siteName + "  (" + p.city + ")");
    }

    /**
     * Admin removes a shelter. Refused while anyone is on a hold or
     * checked in there — those beds are spoken for by real people.
     */
    public synchronized boolean removePool(String poolId) {
        BedPool p = byId.get(poolId);
        if (p == null) {
            return false;
        }
        if (p.pendingHolds + p.confirmedHolds + p.checkedIn > 0) {
            return false;
        }
        pools.remove(p);
        byId.remove(poolId);
        log("ADMIN REMOVED SHELTER  " + p.siteName);
        return true;
    }

    /**
     * Admin puts a listing on hold (or releases it): a held listing
     * disappears from worker search and takes no holds, but stays in
     * the directory for the shelter console and the admin.
     */
    public synchronized void setOnHold(String poolId, boolean onHold, String note) {
        BedPool p = byId.get(poolId);
        if (p == null) {
            return;
        }
        p.onHold = onHold;
        p.onHoldNote = note == null ? "" : note;
        log("ADMIN " + (onHold ? "PUT ON HOLD  " : "RELEASED HOLD  ") + p.siteName
                + (onHold && !p.onHoldNote.isEmpty() ? "  note: " + p.onHoldNote : ""));
    }

    /**
     * Phase 17: the worker got the person to the door and they were
     * not admitted (or never arrived). The bed releases immediately
     * and the failure is recorded as unmet need (FARV) — a placement
     * that failed is data, not a dead end.
     */
    public synchronized boolean logFailedArrival(Hold h, String reason) {
        BedPool p = byId.get(h.poolId);
        if (Hold.HELD.equals(h.status)) {
            p.pendingHolds -= h.beds;
        } else if (Hold.CONFIRMED.equals(h.status)) {
            p.confirmedHolds -= h.beds;
        } else {
            return false;
        }
        h.status = Hold.FAILED_ARRIVAL;
        turnAways.add("FARV - " + h.siteName + " - " + reason + " - for: " + h.profileLabel);
        log("FAILED ARRIVAL  #" + h.id + "  " + h.siteName + "  " + reason + "  (bed released)");
        return true;
    }

    public synchronized void logTurnAway(String code, String profileLabel) {
        turnAways.add(code + " - " + profileLabel);
        log("TURN-AWAY LOGGED  " + code + "  " + profileLabel);
    }

    public synchronized List<Hold> holds() {
        return new ArrayList<Hold>(holds);
    }

    public synchronized List<String> eventLog() {
        return new ArrayList<String>(eventLog);
    }

    public synchronized List<String> turnAways() {
        return new ArrayList<String>(turnAways);
    }

    /** Phase 22: other dashboards (admin) record their actions in the shared audit log. */
    public synchronized void noteEvent(String event) {
        log(event);
    }

    private void log(String event) {
        eventLog.add(event);
    }
}
