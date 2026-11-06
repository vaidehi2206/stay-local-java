package staylocal;

import java.util.List;

/**
 * Intake information from the BC 211 Shelter List, captured live on
 * Nov 5, 2026 (the same retrieval as the directory itself).
 *
 * 101 of the 107 shelters publish Intake Info; 6 publish none
 * (intakeText stays empty — the app says "call ahead", it does not
 * guess). For 85 shelters the text states a usable arrival window
 * (an explicit intake-hours statement first; for overnight and EWR
 * sites, their opening hours), and that window is applied to the
 * pool's intakeOpen/intakeClose with intakeKnown = true. For the
 * other 16 the text is shown verbatim but NO window is derived —
 * first-come-first-served notes, sign-up draws, weekday splits and
 * start-only times are not a window, and pretending otherwise could
 * send someone to a locked door. Eligibility and search only ever
 * apply a window when intakeKnown is true.
 *
 * A few windows rest on a stated rule rather than a labelled intake
 * line: Metson (overnight site hours), Aboriginal Shelter and The
 * Gathering Place (intake/sign-up start + curfew as last arrival),
 * Our Place Shelter Victoria (sign-up from 7 am, arrive by 8:30 pm).
 * The verbatim text is always shown alongside, so nothing is hidden.
 */
public class IntakeData {

    // id, intakeOpen, intakeClose, windowKnown (1/0), intake text verbatim
    private static final String[][] ROWS = {
        {"bc211-001", "08:00", "23:30", "1", "24hr; intake 8:00AM- 11:30PM; curfew 10:30PM"},
        {"bc211-002", "00:00", "24:00", "1", "24 hr; couples ok."},
        {"bc211-003", "00:00", "24:00", "1", "Low barrier 24hr shelter."},
        {"bc211-004", "", "", "0", "Sign up at 8am. Beds assigned at 5:00 pm."},
        {"bc211-005", "00:00", "24:00", "1", "24 hr"},
        {"bc211-006", "08:00", "23:30", "1", "24hr; intake 8:00AM to 11:30PM; curfew 10:30PM"},
        {"bc211-007", "", "", "0", "No holds over phone. Guests are assessed at door. Behaviour-based criteria to determine access to, or restriction from the shelter."},
        {"bc211-008", "00:00", "24:00", "1", "Open referral, 24 hours a day, seven days a week; a phone call to check bed availability is recommended. Seven night maximum stay."},
        {"bc211-009", "00:00", "24:00", "1", "24 hr"},
        {"bc211-010", "00:00", "24:00", "1", "24 hr"},
        {"bc211-011", "00:00", "24:00", "1", "24 hr, High Barrier"},
        {"bc211-012", "00:00", "24:00", "1", "24 hr. Beds are available on a self-referred basis for up to two weeks at a time; intake is ongoing and preferred by phone ahead of time."},
        {"bc211-013", "13:00", "18:00", "1", "Intake hours are 1 pm to 6 pm. Calling in advance is welcomed."},
        {"bc211-014", "16:00", "10:00", "1", "Overnight shelter, open 4pm-10am with beds saved for returning guests. Intake 11-4 Monday to Friday. Partially accessible for canes and walkers."},
        {"bc211-015", "00:00", "24:00", "1", "24 hr"},
        {"bc211-016", "10:00", "22:00", "1", "24hr. Intakes between 10am-10pm. Please call for availability."},
        {"bc211-017", "00:00", "24:00", "1", "24 hr; High Barrier"},
        {"bc211-018", "00:00", "24:00", "1", "24 hr"},
        {"bc211-019", "15:30", "08:00", "1", "The shelter opens at 3:30 pm and closes at 8 am; check for space availability with Case Management or Outreach worker from 9:30 am to 3:30 pm Monday to Friday"},
        {"bc211-020", "00:00", "24:00", "1", "24 hr"},
        {"bc211-021", "", "", "0", ""},
        {"bc211-022", "", "", "0", "Women can call after 7 am to put their name on the list, and must be on site by 1 pm to be considered. / Curfew is now 3 am to 5 am nightly; returning clients keep their bed if they have arrived by the 2 am curfew. / Pets are accepted but can only accommodate one dog or one cat at a time."},
        {"bc211-023", "00:00", "24:00", "1", "24 hr"},
        {"bc211-024", "08:30", "04:00", "1", "High barrier; 8:30 am-4:00 am; no intake between 4:00 am-8:30 am;"},
        {"bc211-025", "", "", "0", ""},
        {"bc211-026", "", "", "0", "Beds are available on a first-come, first-served basis."},
        {"bc211-027", "08:00", "16:00", "1", "8am to 4pm. Only serves people who have substance use and/or mental health issues."},
        {"bc211-028", "00:00", "24:00", "1", "24hr intake. Only for youth with mental health and substance use issues."},
        {"bc211-029", "00:00", "24:00", "1", "24 hour intake"},
        {"bc211-030", "00:00", "24:00", "1", "24 hr"},
        {"bc211-031", "00:00", "24:00", "1", "24 hr; best to try in AM or after 10PM curfew. Not able to accommodate couples."},
        {"bc211-032", "00:00", "24:00", "1", "24 hr"},
        {"bc211-033", "00:00", "24:00", "1", "24 hr"},
        {"bc211-034", "", "", "0", ""},
        {"bc211-035", "00:00", "24:00", "1", "Referrals accepted from youth or from service providers 24 hours a day, seven days a week"},
        {"bc211-036", "00:00", "24:00", "1", "24hr intake, low barrier shelter"},
        {"bc211-037", "", "", "0", ""},
        {"bc211-038", "00:00", "24:00", "1", "24 hr; curfew 10:00PM"},
        {"bc211-039", "00:00", "24:00", "1", "24 hr"},
        {"bc211-040", "00:00", "24:00", "1", "24 hour intake, please call for availability."},
        {"bc211-041", "00:00", "24:00", "1", "24 hr. May be able to accommodate couples."},
        {"bc211-042", "00:00", "24:00", "1", "24 hr"},
        {"bc211-043", "", "", "0", "Mon-Fri: Call or walk up after 4pm. Sat & Sun: 9am-5pm"},
        {"bc211-044", "", "", "0", ""},
        {"bc211-045", "", "", "0", "Intake is on a first-come, first-served basis; priority is given to new guests. Last intake is at 11 pm; clients are encouraged to call before 3 pm to inquire about space availability. / Also offers a 24-hour mat program with 30 mats available. Intake for mats is separate from beds, and returning guests are prioritized."},
        {"bc211-046", "00:00", "24:00", "1", "24 hrs. Men's shelter but may have single rooms to accommodate co-ed couple."},
        {"bc211-047", "00:00", "24:00", "1", "24 hr"},
        {"bc211-048", "00:00", "24:00", "1", "24 hr"},
        {"bc211-049", "13:00", "05:00", "1", "Intake for Shelter from 1:00PM-5:00AM on a first-come first-serve basis. May be able to accommodate couples."},
        {"bc211-050", "16:00", "22:00", "1", "Overnight shelter operating 4PM-8AM nightly. Beds reserved until 10PM curfew for returning guests. Intake hours 4PM-10PM. Entrance is in alley, near RAAC."},
        {"bc211-051", "", "", "0", "Community members can register for a floor mat, in person only, with the Shelter Coordinator. / Once individuals are registered and put on the list, they have the mat for the season. If registered patrons do not show up two nights in a row, they lose their reserved mat. No admittance after 1 am."},
        {"bc211-052", "00:00", "24:00", "1", "24 hr"},
        {"bc211-053", "14:00", "24:00", "1", "Intake is by phone (no walk-ins) daily between 2 pm and midnight; voicemail will indicate if all beds have been filled. Referral accepted from self only."},
        {"bc211-054", "", "", "0", ""},
        {"bc211-055", "", "", "0", "Staff shortage might mean they will not be able to respond to an EWR alert; call to confirm they are opening up space."},
        {"bc211-056", "22:00", "07:00", "1", "Hours: 10 pm – 7 am. Intake is open until site is full. Can accommodate couples."},
        {"bc211-057", "22:00", "08:00", "1", "10PM- 8AM. Mats available at midnight and can be claimed up until 3AM."},
        {"bc211-058", "20:00", "09:00", "1", "8pm - 9am"},
        {"bc211-059", "20:30", "06:30", "1", "Open 8:30pm to 6:30am. Dinner, breakfast, and snacks served. Can accommodate couples. Site entrance is on the right side of the building (East facing)."},
        {"bc211-060", "21:00", "07:30", "1", "Operates between 9:00pm and 7:30am. Site access through breezeway entrance in rear parking lot. Can accommodate couples"},
        {"bc211-061", "22:00", "03:00", "1", "10 pm to 9 am (no intake after 3 am; call to reserve a mat)"},
        {"bc211-062", "19:00", "09:30", "1", "Priority to Langley residents. 7PM-9:30AM"},
        {"bc211-063", "19:30", "07:30", "1", "7:30 pm to 7:30 am"},
        {"bc211-064", "21:00", "07:00", "1", "Operates between 9:00pm and 7:00am Intake open until site is full, can accommodate couples."},
        {"bc211-065", "23:00", "07:00", "1", "Access via front entrance. Open 11pm-7am. Offers coffee, snacks, and Transit passes. Cart storage available. Can accommodate couples."},
        {"bc211-066", "21:30", "07:30", "1", "Site entrance is through courtyard door (South facing). Open 9:30pm to 7:30am. Dinner, breakfast, and snacks served. Site is wheelchair accessible. No cart storage. No pets. Can accommodate couples."},
        {"bc211-067", "20:30", "07:15", "1", "Intake: 8:30 pm to 7:15 a.m. Entrance through front gate. Couples welcome."},
        {"bc211-068", "21:30", "08:30", "1", "9:30 pm to 8:30 am"},
        {"bc211-069", "19:30", "06:30", "1", "KNOCK FOR ENTRY - Access from back alley, bottom of stairs (not up the ramp) / Open 7:30pm to 6:30am 20 mats"},
        {"bc211-070", "21:00", "07:00", "1", "9PM-7AM. Breakfast is served at 7:30am"},
        {"bc211-071", "23:00", "07:00", "1", "entrance at 205 East Hastings Street / 11 pm to 7 am; doors are locked at 1 am (as to allow guests to sleep however if space allows may take-in late night guests)"},
        {"bc211-072", "22:00", "07:00", "1", "Operates between 10pm and 7am. Entrance will be located near the lower parking lot (southeast corner). Couples welcome."},
        {"bc211-073", "22:00", "07:00", "1", "Operates between 10:00pm and 7:00am. Intake open until site is full."},
        {"bc211-074", "19:00", "23:00", "1", "7 pm to 7 am; no entry after 11 pm. Site will close if no guests arrive by 11."},
        {"bc211-075", "21:00", "06:30", "1", "Operates between 9:00pm and 6:30am 10 inclusive shelter spaces for self identified women only. Enter through North Door down the driveway at the back of the building."},
        {"bc211-076", "20:30", "08:00", "1", "Operates from the 1st to the 15th of the month. 8:30PM-8:00AM"},
        {"bc211-077", "20:30", "08:00", "1", "Operates from the 16th to the end of the month. 8:30PM-8:00AM"},
        {"bc211-078", "", "", "0", "20 mats, Shelter for all genders"},
        {"bc211-079", "14:30", "23:00", "1", "Intake starts at 2:30 pm. Curfew is at 11 pm and shelter closes at 10 am."},
        {"bc211-080", "22:00", "07:00", "1", "Hours are 10 pm to 7 am; line up starts at 9:30 pm, and doors are locked at 1 am"},
        {"bc211-081", "22:00", "09:00", "1", "Intake: 10 pm - 9 a.m. All genders. Hot meals/snacks"},
        {"bc211-082", "21:00", "09:00", "1", "Shelter hours are 9 pm to 9 am, seven days a week; curfew is 11 pm. / There is a draw for available beds at 8 pm each night."},
        {"bc211-083", "20:30", "23:00", "1", "Sign up begins at 8:30pm. Curfew is 11pm."},
        {"bc211-084", "19:00", "09:00", "1", "7:00PM to 9:00AM. Intake at 7:00PM; beds assigned at 10:00PM. Waitlist for the day opens at 7am / Priority is given to returning guests; returning clients must show up before 10 pm to keep a bed."},
        {"bc211-085", "", "", "0", "Bed access is generally through referrals by partner agencies. Only serves individuals with mental health and/or substance use issues."},
        {"bc211-086", "00:00", "24:00", "1", "24 hours, 7 days per week. Partially wheelchair accessible (walkers, canes ok)."},
        {"bc211-087", "10:00", "20:00", "1", "Intake hours are 10 am to 8 pm daily."},
        {"bc211-088", "20:00", "06:30", "1", "8PM-6:30AM. Contact for youth options. May be able to accommodate one wheelchair. pets are case-by-case with maximum 3 pets in space."},
        {"bc211-089", "10:00", "22:00", "1", "No walk-ins; must call in advance or be referred by another agency. Intakes hours are 10 am to 10 pm. Limited number of pets accepted."},
        {"bc211-090", "", "", "0", "by referral only, must call first for intake"},
        {"bc211-091", "21:00", "08:00", "1", "Open 9pm-8am. secure spot by in person @8:30pm"},
        {"bc211-092", "17:00", "24:00", "1", "Intake at 5:00PM; opens at 9:00PM. No intake after 12:00AM."},
        {"bc211-093", "15:00", "07:00", "1", "Open from 3pm-7am daily. Line up starts at 4 pm to be on list, and beds are allocated on a first-come, first-served basis at 7 pm."},
        {"bc211-094", "18:00", "09:00", "1", "6pm to 9am; partially accessible."},
        {"bc211-095", "07:00", "20:30", "1", "Overnight shelter that can accommodate 54 adults, operating from 7:30 pm to 6:20 am. / For intake, call or sign up in person starting at 7 am for that night at the front desk. Clients must arrive no later than 8:30 pm to confirm that a space is available."},
        {"bc211-096", "", "", "0", "First come first serve"},
        {"bc211-097", "14:30", "09:00", "1", "Operates 2:30 pm to 9 am 7 days a week"},
        {"bc211-098", "00:00", "24:00", "1", "24 hrs, call for availability. Women need to show up in person to be put on a waitlist."},
        {"bc211-099", "09:00", "17:00", "1", "Call between 9am and 5pm for screening and intake."},
        {"bc211-100", "22:00", "07:00", "1", "Open nightly from 10pm - 7am"},
        {"bc211-101", "08:00", "22:00", "1", "Intake hours are from 8 am to 10 pm."},
        {"bc211-102", "", "", "0", "Call or show up at 12pm to be put on their list. Intake occurs in person at 5pm daily. May be able to accept couples."},
        {"bc211-103", "09:00", "17:00", "1", "Intake by referral only between 9am-5pm."},
        {"bc211-104", "", "", "0", "Call to check for space availability with Shelter Coordinator before 4pm weekdays. May be able to accommodate couples"},
        {"bc211-105", "", "", "0", "Contact shelter directly to be put on waiting list. Minimum stay is 90 days."},
        {"bc211-106", "00:00", "24:00", "1", "24hr. Please call for availability."},
        {"bc211-107", "", "", "0", "Check-in starts at 8 pm; mats are allocated on a first-come, first-serve basis. Shelter operates 24 hours, but gate closes at 9 pm for the public. Guests are asked not to go in and out between 9 pm and 6 am."}
    };

    /** Applies intake text + verified windows to the pools, by id. */
    public static void apply(List<BedPool> pools) {
        java.util.Map<String, BedPool> byId = new java.util.HashMap<String, BedPool>();
        for (BedPool p : pools) {
            byId.put(p.id, p);
        }
        for (String[] row : ROWS) {
            BedPool p = byId.get(row[0]);
            if (p == null) {
                continue;
            }
            p.intakeText = row[4];
            if ("1".equals(row[3])) {
                p.intakeKnown = true;
                p.intakeOpen = row[1];
                p.intakeClose = row[2];
            }
        }
    }
}
