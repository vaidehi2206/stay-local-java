package staylocal;

import java.util.ArrayList;
import java.util.List;

/**
 * One bed pool at one shelter site — the shelter equivalent of a hotel's
 * room type. Adapted from Tucked In's BedPool: same fields, same rules.
 * Directory fields below carry the BC 211 Shelter List data exactly as
 * listed (source: bc.211.ca, retrieved Nov 5, 2026) — see ShelterData.
 *
 * Open count rule (from the project taxonomy):
 *   open = physical - checkedIn - confirmedHolds - walkInReserve - pendingHolds
 * Workers only ever see the open count, never the arithmetic.
 */
public class BedPool {

    public String id;
    public String siteName;
    public String area;
    public String city;               // directory data (BC 211): city
    public String address;            // directory data: street address, or "Address confidential"
    public String phone;              // directory data: contact phone(s), as listed
    public String shelterType;        // directory data: tags as listed, e.g. "Overnight", "Extreme Weather"
    public String bedsText;           // directory data: "Beds Available" exactly as listed
    public String notes;              // directory data: notes exactly as listed
    public String servesText;         // directory data: "Serves" exactly as listed
    public String agesText;           // directory data: age categories exactly as listed
    public String featuresText;       // directory data: features exactly as listed ("" = none shown)
    public String updatedText;        // directory data: "Updated ..." exactly as listed
    public boolean hiddenSite;        // VAW site: name/location never displayed
    public String category;           // WOM, MEN, MIX, YOU, FAM, CPL
    public List<String> attributes;   // ACC, PET, LOW, SOB, MH, MED, IND
    public String countUnit;          // "beds", "family units", "mats"

    public int physical;
    public int checkedIn;
    public int confirmedHolds;
    public int walkInReserve;
    public int pendingHolds;          // holds placed, waiting for shelter to confirm

    public String intakeOpen;         // "15:00"
    public String intakeClose;        // "23:00", "24:00", or overnight "02:00"
    public String intakeText = "";    // Phase 14: Intake Info verbatim from BC 211
    public boolean intakeKnown = false; // Phase 14: true only when 211's text states a usable window
    public int lastUpdatedMinAgo;     // -1 = never updated
    public boolean active;            // EWR pop-up starts inactive
    public boolean onHold;            // admin put this listing on hold (Phase 9)
    public String onHoldNote;         // why, as entered by the admin
    public boolean referralOnly;
    public String referralTypeNeeded; // e.g. "hospital"
    public int ageMin;                // -1 = no minimum
    public int ageMax;                // -1 = no maximum
    public List<String> partyTypes;   // "single", "couple", "family"
    public String sobrietyStance;     // "tolerant" or "program-required"
    public int maxStayNights;

    public BedPool() {
        attributes = new ArrayList<String>();
        partyTypes = new ArrayList<String>();
        active = true;
        countUnit = "beds";
        ageMin = -1;
        ageMax = -1;
        lastUpdatedMinAgo = 0;
        city = "";
        address = "";
        phone = "";
        shelterType = "";
        bedsText = "";
        notes = "";
        servesText = "";
        agesText = "";
        featuresText = "";
        updatedText = "";
        onHold = false;
        onHoldNote = "";
    }

    public int openCount() {
        if (!active || onHold) {
            return 0;
        }
        int open = physical - checkedIn - confirmedHolds - walkInReserve - pendingHolds;
        return Math.max(open, 0);
    }

    public String freshness() {
        if (lastUpdatedMinAgo < 0) {
            return "not updated yet";
        }
        if (lastUpdatedMinAgo <= 30) {
            return "fresh";
        }
        if (lastUpdatedMinAgo <= 60) {
            return "aging";
        }
        if (lastUpdatedMinAgo <= 120) {
            return "stale";
        }
        return "call to verify";
    }

    public boolean holdableFreshness() {
        return lastUpdatedMinAgo >= 0 && lastUpdatedMinAgo <= 120;
    }

    /** Is the given time (minutes after midnight) inside the intake window? */
    public boolean intakeOpenAt(int minutes) {
        int open = toMinutes(intakeOpen);
        int close = toMinutes(intakeClose);
        if (close > open) {
            return minutes >= open && minutes < close;
        }
        // overnight window, e.g. 20:00 - 02:00
        return minutes >= open || minutes < close;
    }

    public static int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return h * 60 + m;
    }
}
