package staylocal;

/**
 * A soft, time-boxed hold on one bed.
 * State machine: HELD -> CONFIRMED -> CHECKED_IN / NO_SHOW
 *                HELD -> REJECTED
 *                HELD -> EXPIRED (auto, when the countdown reaches zero)
 *                HELD / CONFIRMED -> CANCELLED (the requester backs out)
 * Adapted from Tucked In's Hold, with CANCELLED added for this app.
 */
public class Hold {

    public static final String HELD = "HELD";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String REJECTED = "REJECTED";
    public static final String CHECKED_IN = "CHECKED_IN";
    public static final String NO_SHOW = "NO_SHOW";
    public static final String EXPIRED = "EXPIRED";
    public static final String CANCELLED = "CANCELLED";
    public static final String FAILED_ARRIVAL = "FAILED_ARRIVAL"; // Phase 17: reached the shelter, not admitted

    public int id;
    public int beds = 1;                 // Phase 15: how many beds this hold takes
    public java.util.List<String> needs = new java.util.ArrayList<String>(); // Phase 15: arrival needs
    public String needsNote = "";        // Phase 15: free-text note for the shelter
    public boolean urgent = false;       // Phase 15: needs attention on arrival
    public String language = "";         // Phase 25: language they speak best ("" = not noted)
    public java.util.List<String> safety = new java.util.ArrayList<String>(); // Phase 25: safety flags

    /** Phase 25: languages a worker can pick in one tap (Metro Vancouver's most common). */
    public static final String[] LANGUAGES = {
        "English", "Punjabi", "Cantonese", "Mandarin", "Tagalog", "Spanish",
        "Vietnamese", "Korean", "Hindi", "Farsi", "Arabic", "Other - see note"
    };
    public int etaMinutes = 0;           // Phase 16: worker's stated arrival time; 0 = not set
    public String poolId;
    public String siteName;
    public String profileLabel;
    public String status;
    public long expiresAtMillis;
    public String rejectReason;

    public long secondsLeft(long nowMillis) {
        long diff = (expiresAtMillis - nowMillis) / 1000;
        return Math.max(diff, 0);
    }

    /** True when the worker noted anything the shelter should prepare. */
    public boolean hasNeeds() {
        return urgent || !needs.isEmpty() || !needsNote.isEmpty()
                || !language.isEmpty() || !safety.isEmpty();
    }

    /** One line for the shelter: what to have ready when they arrive. */
    /**
     * One line for the shelter: the language they speak (and whether
     * an interpreter is needed), any safety flags, then what to have
     * ready when they arrive.
     */
    public String needsSummary() {
        StringBuilder sb = new StringBuilder();
        if (urgent) {
            sb.append("URGENT - ");
        }
        if (!hasNeeds()) {
            return sb.append("no arrival needs noted").toString();
        }
        java.util.List<String> parts = new java.util.ArrayList<String>();
        if (!language.isEmpty()) {
            parts.add("Language: " + language
                    + ("English".equals(language) ? "" : " - interpreter needed"));
        }
        if (!safety.isEmpty()) {
            parts.add("Safety: " + String.join(", ", safety));
        }
        if (!needs.isEmpty()) {
            parts.add(String.join(", ", needs));
        }
        if (!needsNote.isEmpty()) {
            parts.add(needsNote);
        }
        sb.append(String.join("  -  ", parts));
        return sb.toString();
    }

    public String describe(long nowMillis) {
        String base = "Hold #" + id + " - " + siteName + " - for: " + profileLabel + " - " + status
                + " - " + beds + (beds == 1 ? " bed" : " beds");
        if (etaMinutes > 0) {
            base += " - ETA ~" + etaMinutes + " min";
        }
        if (hasNeeds()) {
            base += " - prepare: " + needsSummary();
        }
        if (HELD.equals(status)) {
            long s = secondsLeft(nowMillis);
            base += " (" + (s / 60) + ":" + String.format("%02d", s % 60) + " left)";
        }
        if (REJECTED.equals(status) && rejectReason != null) {
            base += " - reason: " + rejectReason;
        }
        return base;
    }
}
