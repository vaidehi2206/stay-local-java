package staylocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The admin dashboard's numbers: a daily statistics report built from
 * the two live sources the admin oversees — the shelter directory
 * (what shelters list) and the session activity (what health care
 * workers and shelters actually did: holds, confirmations, check-ins,
 * rejections, no-shows, turn-aways).
 *
 * In production this report is generated daily from the server's data
 * across every user. In this local app the directory figures are the
 * real BC 211 snapshot and the activity figures are this session's.
 */
public class AdminStats {

    /** Beds a listing states as open, summed over open (active) sites. */
    public static int statedOpenBeds(List<BedPool> pools) {
        int total = 0;
        for (BedPool p : pools) {
            if (p.active) {
                total += p.physical;
            }
        }
        return total;
    }

    /** Beds open right now, after this session's holds and check-ins. */
    public static int sessionOpenBeds(List<BedPool> pools) {
        int total = 0;
        for (BedPool p : pools) {
            total += p.openCount();
        }
        return total;
    }

    public static String dailyReport(List<BedPool> pools, HoldManager manager, String dateText) {
        return dailyReport(pools, manager, dateText, new SearchLog());
    }

    public static String dailyReport(List<BedPool> pools, HoldManager manager, String dateText,
            SearchLog demand) {
        int openSites = 0, full = 0, noInfo = 0, pet = 0, acc = 0, statingOpen = 0;
        Map<String, Integer> byCategory = new HashMap<String, Integer>();
        Map<String, Integer> byCity = new HashMap<String, Integer>();
        for (BedPool p : pools) {
            if (p.active) {
                openSites++;
                if (p.physical > 0) {
                    statingOpen++;
                }
            }
            if ("Full".equals(p.bedsText)) {
                full++;
            }
            if (p.bedsText.toLowerCase().startsWith("no information")) {
                noInfo++;
            }
            if (p.attributes.contains("PET")) {
                pet++;
            }
            if (p.attributes.contains("ACC")) {
                acc++;
            }
            byCategory.put(p.category, byCategory.getOrDefault(p.category, 0) + 1);
            byCity.put(p.city, byCity.getOrDefault(p.city, 0) + 1);
        }

        int held = 0, confirmed = 0, checkedIn = 0, rejected = 0,
                cancelled = 0, expired = 0, noShow = 0, failedArrival = 0;
        List<Hold> holds = manager.holds();
        for (Hold h : holds) {
            if (Hold.HELD.equals(h.status)) {
                held++;
            } else if (Hold.CONFIRMED.equals(h.status)) {
                confirmed++;
            } else if (Hold.CHECKED_IN.equals(h.status)) {
                checkedIn++;
            } else if (Hold.REJECTED.equals(h.status)) {
                rejected++;
            } else if (Hold.CANCELLED.equals(h.status)) {
                cancelled++;
            } else if (Hold.EXPIRED.equals(h.status)) {
                expired++;
            } else if (Hold.NO_SHOW.equals(h.status)) {
                noShow++;
            } else if (Hold.FAILED_ARRIVAL.equals(h.status)) {
                failedArrival++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Stay Local - Daily Statistics Report\n");
        sb.append("Date: ").append(dateText).append("\n");
        sb.append("Data: ").append(ShelterData.SOURCE).append(", retrieved ")
                .append(ShelterData.RETRIEVED).append(", plus this app session's activity.\n\n");

        sb.append("DIRECTORY (what shelters list)\n");
        sb.append("  Snapshot: ").append(DirectoryInfo.SNAPSHOT_TEXT).append(" (")
                .append(DirectoryInfo.ageDays(java.time.LocalDate.now()))
                .append(" days old) - refresh via the BC 211 Shelter List;\n")
                .append("  counts shelters report inside this app are live.\n");
        sb.append("Total shelters listed: ").append(pools.size()).append("\n");
        sb.append("Cities covered: ").append(byCity.size()).append("\n");
        sb.append("Open sites: ").append(openSites).append("\n");
        sb.append("Closed sites: ").append(pools.size() - openSites).append("\n");
        sb.append("By who it serves: Women ").append(byCategory.getOrDefault("WOM", 0))
                .append(", Men ").append(byCategory.getOrDefault("MEN", 0))
                .append(", Any gender ").append(byCategory.getOrDefault("MIX", 0))
                .append(", Youth ").append(byCategory.getOrDefault("YOU", 0))
                .append(", Families ").append(byCategory.getOrDefault("FAM", 0)).append("\n");
        sb.append("Pet friendly: ").append(pet).append("\n");
        sb.append("Wheelchair accessible: ").append(acc).append("\n");
        sb.append("Shelters stating open beds: ").append(statingOpen).append("\n");
        sb.append("Total beds stated open: ").append(statedOpenBeds(pools)).append("\n");
        sb.append("Listed full: ").append(full).append("\n");
        sb.append("No bed information stated: ").append(noInfo).append("\n");
        sb.append("Shelters by city:\n");
        List<Map.Entry<String, Integer>> cities = new ArrayList<Map.Entry<String, Integer>>(byCity.entrySet());
        Collections.sort(cities, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                if (!a.getValue().equals(b.getValue())) {
                    return b.getValue() - a.getValue();
                }
                return a.getKey().compareTo(b.getKey());
            }
        });
        for (Map.Entry<String, Integer> e : cities) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }

        sb.append("\nACTIVITY (health care worker + shelter interactions, this session)\n");
        sb.append("Holds placed: ").append(holds.size()).append("\n");
        sb.append("  Held now: ").append(held).append("\n");
        sb.append("  Confirmed: ").append(confirmed).append("\n");
        sb.append("  Checked in: ").append(checkedIn).append("\n");
        sb.append("  Rejected: ").append(rejected).append("\n");
        sb.append("  Cancelled: ").append(cancelled).append("\n");
        sb.append("  Expired: ").append(expired).append("\n");
        sb.append("  No-show: ").append(noShow).append("\n");
        sb.append("  Failed arrivals: ").append(failedArrival).append("\n");
        java.util.List<String> tas = manager.turnAways();
        sb.append("Turn-aways logged: ").append(tas.size()).append("\n");
        int taSearch = 0, taDoor = 0, taFarv = 0, taNshw = 0;
        for (String t : tas) {
            if (t.startsWith("FARV")) {
                taFarv++;
            } else if (t.startsWith("NSHW")) {
                taNshw++;
            } else if (t.startsWith("DOOR")) {
                taDoor++;
            } else {
                taSearch++;
            }
        }
        if (!tas.isEmpty()) {
            sb.append("  From worker searches (no bed found): ").append(taSearch).append("\n");
            sb.append("  At the shelter door (walk-ins): ").append(taDoor).append("\n");
            sb.append("  Failed arrivals after a hold: ").append(taFarv).append("\n");
            sb.append("  No-shows after confirmation: ").append(taNshw).append("\n");
            for (String t : tas) {
                sb.append("  - ").append(t).append("\n");
            }
        }
        sb.append("Events logged: ").append(manager.eventLog().size()).append("\n");
        sb.append("Beds open in this session right now: ").append(sessionOpenBeds(pools)).append("\n");
        sb.append("\nDEMAND (what workers searched for, this session)\n");
        sb.append("Searches recorded: ").append(demand.totalSearches()).append("\n");
        sb.append("  Found an open bed: ").append(demand.totalSearches() - demand.zeroOpenCount()).append("\n");
        sb.append("  NO open bed (unmet): ").append(demand.zeroOpenCount()).append("\n");
        if (demand.zeroOpenCount() > 0) {
            sb.append("Most-searched unmet needs:\n");
            for (Map.Entry<String, Integer> e : demand.topUnmet(5)) {
                sb.append("  ").append(e.getValue()).append(" x ").append(e.getKey()).append("\n");
            }
        }
        sb.append("(A search is recorded once it settles - 3 s after the last filter change;\n");
        sb.append(" back-to-back identical searches count once.)\n");
        sb.append("\nNote: in production this report is generated daily from all users'\n");
        sb.append("activity on the server. Here it reflects this device's local session.\n");
        return sb.toString();
    }
}
