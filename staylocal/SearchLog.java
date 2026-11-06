package staylocal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 18: the demand log. Every settled worker search is recorded
 * — what was looked for, how many shelters matched, and how many of
 * those had a bed open. A search whose matches have NO open bed is
 * unmet demand: the signal the admin's daily report was missing,
 * because until now it only ever described supply.
 *
 * "Settled" matters: the search re-runs on every keystroke and
 * checkbox tick, so the app only records a search once the worker
 * stops changing it (see StayLocalApp's 3-second settle timer).
 * No person is identified — a search is its filters, nothing more.
 */
public class SearchLog {

    public static class Entry {
        public final String summary;   // the filters, in words
        public final int found;        // shelters that matched
        public final int open;         // of those, with a bed open
        public final String timeText;  // e.g. "6:41 PM"

        public Entry(String summary, int found, int open, String timeText) {
            this.summary = summary;
            this.found = found;
            this.open = open;
            this.timeText = timeText;
        }

        public boolean unmet() {
            return open == 0;
        }

        public String describe() {
            return timeText + "  " + summary + "  ->  " + found + " matched, "
                    + open + " with a bed open" + (unmet() ? "  (UNMET)" : "");
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    public void record(String summary, int found, int open, String timeText) {
        entries.add(new Entry(summary, found, open, timeText));
    }

    public List<Entry> entries() {
        return new ArrayList<Entry>(entries);
    }

    public Entry lastEntry() {
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    public int totalSearches() {
        return entries.size();
    }

    public int zeroOpenCount() {
        int n = 0;
        for (Entry e : entries) {
            if (e.unmet()) {
                n++;
            }
        }
        return n;
    }

    /** The most-repeated unmet searches, most frequent first. */
    public List<Map.Entry<String, Integer>> topUnmet(int limit) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (Entry e : entries) {
            if (e.unmet()) {
                Integer c = counts.get(e.summary);
                counts.put(e.summary, c == null ? 1 : c + 1);
            }
        }
        List<Map.Entry<String, Integer>> out =
                new ArrayList<Map.Entry<String, Integer>>(counts.entrySet());
        Collections.sort(out, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                if (!a.getValue().equals(b.getValue())) {
                    return b.getValue() - a.getValue();
                }
                return a.getKey().compareTo(b.getKey());
            }
        });
        return out.size() > limit ? out.subList(0, limit) : out;
    }
}
