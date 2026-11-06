package staylocal;

import java.util.ArrayList;
import java.util.List;

/**
 * What a worker is searching for, on behalf of a person — the shelter
 * equivalent of a hotel search's guests-and-dates form, except every field
 * is a need, and the match rules are hard filters (see Eligibility).
 * No name is needed to search: a profile is needs only, by design.
 * Adapted from Tucked In's RequestProfile.
 */
public class NeedProfile {

    public String id;
    public String label;
    public String categoryNeeded;          // WOM, MEN, MIX, YOU, FAM, CPL
    public List<String> attributesNeeded;  // subset the pool MUST have
    public int age;                        // 0 = not applicable / not asked
    public String party;                   // "single", "couple", "family"
    public String sobrietyNeed;            // "any" or "must-not-require-sobriety"
    public String referrerType;            // "outreach worker", "211 navigator", "hospital"

    public NeedProfile() {
        attributesNeeded = new ArrayList<String>();
        age = 0;
        sobrietyNeed = "any";
        referrerType = "outreach worker";
    }
}
