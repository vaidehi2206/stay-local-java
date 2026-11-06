package staylocal;

/**
 * Phase 19: structured flags read from a listing's OWN words.
 *
 * BC 211 does not publish harm-reduction or health-service fields,
 * so these flags are deliberately modest: a flag is set only when
 * the shelter's own listing text (notes, features, or intake info)
 * actually mentions the service. The wording is "mentioned in the
 * listing", never a promise — the worker still confirms by phone,
 * exactly as they would from the 211 page. Nothing is inferred from
 * a shelter's name, operator, or neighbourhood.
 */
public class ShelterFlags {

    private static String words(BedPool p) {
        return (p.notes + " " + p.featuresText + " " + p.intakeText).toLowerCase();
    }

    public static boolean mentionsHarmReduction(BedPool p) {
        return words(p).contains("harm reduction");
    }

    public static boolean mentionsHealth(BedPool p) {
        String w = words(p);
        return w.contains("medical") || w.contains("nurse")
                || w.contains("health care") || w.contains("mental health");
    }

    /** Detail-screen line, e.g. "Listing mentions: harm reduction, health services". */
    public static String mentionsLine(BedPool p) {
        StringBuilder sb = new StringBuilder();
        if (mentionsHarmReduction(p)) {
            sb.append("harm reduction");
        }
        if (mentionsHealth(p)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("health services");
        }
        return sb.length() == 0 ? "" : "Listing mentions: " + sb.toString();
    }
}
