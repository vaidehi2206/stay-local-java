package staylocal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Phase 23: the directory's age, stated plainly. The shelter list
 * in this app is a snapshot of the BC 211 Shelter List retrieved on
 * November 5, 2026. BC 211 itself only updates that list twice a
 * weekday, so even a fresh copy ages fast — the app must say how
 * old its copy is, everywhere it matters, instead of letting old
 * numbers pose as tonight's truth.
 *
 * The refresh path is honest about what exists: there is no public
 * BC 211 API to poll, so refreshing means (1) a person opens the
 * live 211 list, (2) shelters keep their own counts live in the
 * console, and (3) anyone who just called a shelter can mark its
 * listing verified, which resets its freshness.
 */
public class DirectoryInfo {

    public static final LocalDate SNAPSHOT_DATE = LocalDate.of(2026, 11, 5);
    public static final String SNAPSHOT_TEXT = "November 5, 2026";
    public static final String LIST_URL = "https://bc.211.ca/shelter-lists/";

    public static long ageDays(LocalDate today) {
        return ChronoUnit.DAYS.between(SNAPSHOT_DATE, today);
    }

    /** A snapshot more than a week old is called out, not quietly trusted. */
    public static boolean isStale(LocalDate today) {
        return ageDays(today) > 7;
    }

    public static String ageText(LocalDate today) {
        long age = ageDays(today);
        if (age <= 0) {
            return "retrieved today";
        }
        return age == 1 ? "1 day old" : age + " days old";
    }

    public static String statusLine(LocalDate today) {
        return "Directory: BC 211 Shelter List, retrieved " + SNAPSHOT_TEXT
                + " (" + ageText(today) + "). 211 updates Mon-Fri about 11:30 AM and 7:30 PM - "
                + "bed counts here are a starting point; counts a shelter reports in this app are live.";
    }
}
