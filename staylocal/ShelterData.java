package staylocal;

import java.util.ArrayList;
import java.util.List;

/**
 * The real BC 211 shelter directory, as listed on bc.211.ca/shelter-lists/
 * and captured Nov 5, 2026 (the page counter read 'Showing 107 shelters
 * from 15 cities' at capture time — it had grown from 103 earlier that day).
 *
 * Every directory field (name, city, address, phone, serves, ages, beds,
 * features, type/tags, notes, updated) is kept exactly as 211 printed it;
 * where 211 said 'No information available' the app keeps it unknown and
 * never invents a number. Derived app-model fields (category, age band,
 * ACC/PET attributes, hiddenSite for 'Address confidential', active=false
 * for CLOSED-badged sites, a numeric count where 211 stated one) follow
 * the mapping documented in generate_shelter_data.py. Intake windows
 * were not captured per shelter, so pools default to 00:00-24:00 and
 * any intake detail 211 showed lives in the notes, verbatim.
 *
 * 211 refreshes this list twice each weekday, so this snapshot ages:
 * the app must always show the source + retrieval date with it.
 */
public class ShelterData {

    public static final String SOURCE = "BC 211 Shelter List (bc.211.ca)";
    public static final String RETRIEVED = "Nov 5, 2026";

    public static List<BedPool> allShelters() {
        List<BedPool> pools = new ArrayList<BedPool>();
        pools.add(make("bc211-001", "The Beacon", "Vancouver", "108 East Hastings Street", "604-646-6846", "Men", "19+", "Men: 5", "", "24 Hour", "24hr; intake 8:00AM- 11:30PM; curfew 10:30PM", "Updated 6 hours ago", 360, "MEN", 19, -1, true, 5, "beds", "tolerant"));
        pools.add(make("bc211-002", "Guildford Shelter", "Surrey", "14716 104 Avenue", "778-293-0889 Ext 101", "Any Gender", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 6 hours ago", 360, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-003", "Klahowya Tillicum Lalum (Lu’ma Shelter)", "Vancouver", "875 Terminal Avenue", "236-484-0544", "Any Gender", "19+", "Men: 1; Women: 1", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "24 Hour", "", "Updated 6 hours ago", 360, "MIX", 19, -1, true, 2, "beds", "tolerant"));
        pools.add(make("bc211-004", "Arbutus Shelter", "Victoria", "2915 Douglas Street", "250-360-0093", "Any Gender", "19+", "Full", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "24 Hour, Overnight", "No intakes on weekend", "Updated 6 hours ago", 360, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-005", "Brigadier Arthur Cartmell House", "Chilliwack", "45746 Yale Road", "604-792-4486", "Women", "19+", "No Information available", "Pet Friendly", "24 Hour", "", "Updated 6 hours ago", 360, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-006", "The Haven", "Vancouver", "108 East Hastings Street", "604-646-6806", "Men", "19+", "Men: 2", "", "24 Hour", "No holds over phone. Guests are assessed at door. behaviour-based criteria to determine access to, or restriction from the shelter.", "Updated 6 hours ago", 360, "MEN", 19, -1, true, 2, "beds", "tolerant"));
        pools.add(make("bc211-007", "The Crosswalk", "Vancouver", "108 East Hastings Street", "604-646-6875", "Men", "19+", "Men: 4", "", "24 Hour", "", "Updated 6 hours ago", 360, "MEN", 19, -1, true, 4, "beds", "tolerant"));
        pools.add(make("bc211-008", "KEYS Kiwanis Emergency Youth Shelter", "Victoria", "2117 Vancouver Street", "250-386-8282", "Any Gender", "12–18 only", "Any: 10", "", "24 Hour, Overnight", "Youth ages 13-18 only", "Updated 6 hours ago", 360, "YOU", 12, 18, true, 10, "beds", "tolerant"));
        pools.add(make("bc211-009", "Cyrus Centre Abbotsford", "Abbotsford", "2616 Ware Street", "604-859-5773", "Any Gender", "12–18 only, 19–24 only", "Men: 1(12-18); Any: 1 (12-18); Women: (19-24); Men: (19-24)", "", "24 Hour", "Youth ages 1MY, 2FY 12-18 and 2M & 2F (19-24)", "Updated 6 hours ago", 360, "YOU", 12, 24, true, 2, "beds", "tolerant"));
        pools.add(make("bc211-010", "Powell Place Shelter For Women", "Vancouver", "329 Powell Street", "604-606-0403", "Women", "19+", "Full", "", "24 Hour", "", "Updated 6 hours ago", 360, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-011", "Fraserside Emergency Family Shelter", "New Westminster", "Address confidential", "604-525-3929", "Any Gender, Families, With Children", "19+", "Full", "", "24 Hour", "", "Updated 6 hours ago", 360, "FAM", 19, -1, true, 0, "family units", "tolerant"));
        pools.add(make("bc211-012", "Youth Emergency Shelter (YES)", "Maple Ridge", "Address confidential", "236-335-1926", "Any Gender", "12–18 only", "Full", "", "24 Hour", "Youth ages 15-18 only", "Updated 6 hours ago", 360, "YOU", 12, 18, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-013", "Salvation Army ARC Emergency Shelter", "Victoria", "525 Johnson Street", "250-384-3396 Local 1", "Men", "19+", "Men: 1", "", "Overnight", "Pls. note: No intake on Fridays after 2 pm. Substance use is not permitted, and individuals must be sober to be admitted. Limited storage space is available; only 2 medium bags of personal belongings can be accommodated.", "Updated 6 hours ago", 360, "MEN", 19, -1, true, 1, "beds", "program-required"));
        pools.add(make("bc211-014", "Metson Shelter (aka Community Builders)", "Vancouver", "1060 Howe Street", "604-428-3686", "Any Gender", "19+", "No information available.", "Pet Friendly, Takes Large Items", "Overnight", "", "Updated 6 hours ago", 360, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-015", "Cyrus Centre Chilliwack", "Chilliwack", "45845 Wellington Avenue", "604-795-5773", "Any Gender", "12–18 only, 19–24 only", "4Y (12-18)", "Wheelchair Accessible", "24 Hour", "", "Updated 6 hours ago", 360, "YOU", 12, 24, true, 4, "beds", "tolerant"));
        pools.add(make("bc211-016", "Douglas Shelter Progressive Housing Society Shelter", "Burnaby", "2294 Douglas Road", "604-522-9669 Ext 2", "Any Gender", "19+", "Women: 2", "Pet Friendly", "24 Hour", "", "Updated 6 hours ago", 360, "MIX", 19, -1, true, 2, "beds", "tolerant"));
        pools.add(make("bc211-017", "Vi Fineday Family Shelter Society", "Vancouver", "Address confidential", "604-736-2423", "Any Gender, Families, With Children", "19+", "Full", "", "24 Hour", "", "Updated 6 hours ago", 360, "FAM", 19, -1, true, 0, "family units", "tolerant"));
        pools.add(make("bc211-018", "Covenant House Crisis Program - Youth (Female & Male)", "Vancouver", "1280 Seymour Street", "604-968-8523", "Any Gender", "12–18 only, 19–24 only", "Full", "Wheelchair Accessible", "24 Hour", "Youth ages 16-24 only.", "Updated 7 hours ago", 420, "YOU", 12, 24, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-019", "Centre Of Hope - Abbotsford Shelter", "Abbotsford", "34081 Gladys Avenue", "604-852-9305 ext 3", "Any Gender", "19+", "Full", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-020", "Richmond House", "Richmond", "12040 Horseshoe Way", "604-276-2490 Ext 0", "Any Gender", "19+", "Women: 2 upper bunks", "Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 2, "beds", "tolerant"));
        pools.add(make("bc211-021", "SPUDS (Previously Commercial-Hastings Shelter)", "Vancouver", "1660 East Hastings Street", "604-428-2206", "Any Gender", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "First Come-First Serve", "Entrance from the back alley only", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-022", "Sandy Merriman", "Victoria", "809 Burdett Avenue", "250-480-1408", "Women", "19+", "Full", "Pet Friendly", "24 Hour, Overnight", "", "Updated 7 hours ago", 420, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-023", "All Nations Youth Safe House (ANYSH) - Kla-how-eya", "Surrey", "Address confidential", "604-584-2625", "Any Gender", "12–18 only", "Any: 4; Women: 1", "Wheelchair Accessible", "24 Hour", "Youth ages 15-18 only", "Updated 7 hours ago", 420, "YOU", 12, 18, true, 5, "beds", "tolerant"));
        pools.add(make("bc211-024", "Belkin House - Women And Children’s Shelter", "Vancouver", "555 Homer Street", "604-694-6623; 604-681-3405 call Front Desk", "Women, With Children", "19+", "Women: 3", "", "24 Hour", "", "Updated 7 hours ago", 420, "FAM", 19, -1, true, 3, "family units", "tolerant"));
        pools.add(make("bc211-025", "Belkin House - Men’s Shelter", "Vancouver", "555 Homer Street", "604-694-6636", "Men", "19+", "Full", "", "24 Hour", "", "Updated 7 hours ago", 420, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-026", "Springhouse", "Vancouver", "Address confidential", "604-606-0412", "Women, Families, With Children", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour, First Come-First Serve", "", "Updated 7 hours ago", 420, "FAM", 19, -1, true, 0, "family units", "tolerant"));
        pools.add(make("bc211-027", "First United Church Shelter", "Vancouver", "467 Alexander Street", "604-839-6880", "Any Gender", "19+", "Full", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-028", "Covenant House - Sanctuary Shelter", "Vancouver", "1302 Seymour Street", "672-963-3299", "Any Gender", "12–18 only, 19–24 only", "4Y (16-24)", "", "24 Hour", "", "Updated 7 hours ago", 420, "YOU", 12, 24, true, 4, "beds", "tolerant"));
        pools.add(make("bc211-029", "North Shore Housing Centre Shelter", "North Vancouver", "705 2nd Street West", "604-982-9126 Ext 0", "Any Gender", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-030", "Russell Housing Centre", "New Westminster", "740 Carnarvon Street", "604-529-9126 press 0", "Men", "19+", "Full", "", "24 Hour", "", "Updated 7 hours ago", 420, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-031", "Hyland House", "Surrey", "6595 King George Boulevard", "604-599-8900", "Any Gender", "19+", "Full", "Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-032", "Al Mitchell Place (Lookout Downtown)", "Vancouver", "346 Alexander Street", "604-681-9126 Ext 1", "Men", "19+", "Full", "Pet Friendly", "24 Hour", "", "Updated 7 hours ago", 420, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-033", "Gateway Shelter", "Surrey", "10667 135A Street", "604-589-7777", "Any Gender", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-034", "Rosewood Shelter - E. Fry Society", "Surrey", "Address confidential", "604-589-5337", "Women, With Children", "19+", "Mom + 3 x2 so two rooms for mom+3kids", "Wheelchair Accessible", "24 Hour", "family rooms", "Updated 7 hours ago", 420, "FAM", 19, -1, true, 2, "family units", "tolerant"));
        pools.add(make("bc211-035", "Aboriginal Youth Safehouse (ASH)", "Vancouver", "Address confidential", "604-254-5147", "Any Gender", "12–18 only", "Any: 4 (16-18)", "", "24 Hour", "", "Updated 7 hours ago", 420, "YOU", 12, 18, true, 4, "beds", "tolerant"));
        pools.add(make("bc211-036", "Directions Youth Haven", "Vancouver", "Address confidential", "778-800-3300", "Any Gender", "12–18 only, 19–24 only", "2Y (16-18)", "Pet Friendly", "24 Hour", "Youth ages 16-21 only", "Updated 7 hours ago", 420, "YOU", 12, 24, true, 2, "beds", "tolerant"));
        pools.add(make("bc211-037", "Elizabeth Gurney House", "New Westminster", "Address confidential", "604-524-0710", "Women, With Children", "19+", "Full", "Wheelchair Accessible", "24 Hour, Overnight", "", "Updated 7 hours ago", 420, "FAM", 19, -1, true, 0, "family units", "tolerant"));
        pools.add(make("bc211-038", "North Shore Youth Safe House", "North Vancouver", "Address confidential", "604-924-8005", "Any Gender", "12–18 only", "3Y (13-18)", "", "24 Hour", "", "Updated 7 hours ago", 420, "YOU", 12, 18, true, 3, "beds", "tolerant"));
        pools.add(make("bc211-039", "Parkway Shelter", "Surrey", "10666 City Parkway", "604-582-0408", "Any Gender", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-040", "Hornby Street Shelter", "Vancouver", "1401 Hornby Street", "604-620-9253", "Any Gender", "19+", "Full", "Pet Friendly, Takes Large Items", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-041", "Bill Reid Place (Hyland House Cloverdale)", "Surrey", "17752 Colebrook Road", "604-574-4341", "Any Gender", "19+", "Full", "Pet Friendly", "24 Hour", "", "Updated 7 hours ago", 420, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-042", "FSGV Safe House (Directions)", "Vancouver", "Address confidential", "604-877-1234", "Any Gender", "12–18 only", "1M (16-18)", "", "24 Hour", "Youth ages 16-18 only", "Updated 7 hours ago", 420, "YOU", 12, 18, true, 1, "beds", "tolerant"));
        pools.add(make("bc211-043", "Stevenson House For Men", "New Westminster", "32 Elliot Street", "604-526-4783", "Men", "19+", "No information available.", "", "24 Hour", "no intakes after 10pm", "Updated 22 hours ago", 1320, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-044", "Gateway Of Hope - Emergency Shelter", "Langley", "5787 Langley Bypass", "604-514-7375", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 22 hours ago", 1320, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-045", "Rock Bay Landing Emergency Shelter", "Victoria", "535 Ellice Street", "250-383-1951 Ext 1", "Any Gender", "19+", "Full", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "24 Hour, First Come-First Serve", "", "Updated 22 hours ago", 1320, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-046", "House of Hope - Salvation Army Chilliwack", "Chilliwack", "45746 Yale Road", "604-392-8510", "Men", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 22 hours ago", 1320, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-047", "Marc’s Place Underage Safe House", "Vancouver", "Address confidential", "604-261-7827", "Any Gender", "12–18 only", "1Y (13-15)", "", "24 Hour", "Youth ages 13-15 only", "Updated 22 hours ago", 1320, "YOU", 12, 18, true, 1, "beds", "tolerant"));
        pools.add(make("bc211-048", "Yukon Shelter", "Vancouver", "2088 Yukon Street", "604-264-1680 Ext 1", "Any Gender", "19+", "Men: 1", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 22 hours ago", 1320, "MIX", 19, -1, true, 1, "beds", "tolerant"));
        pools.add(make("bc211-049", "Salvation Army Ridge Meadows Ministries", "Maple Ridge", "22188 Lougheed Highway", "604-463-8296 Ext 0", "Any Gender", "19+", "Full", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 22 hours ago", 1320, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-050", "Catholic Charities Men’s Shelter", "Vancouver", "1056 Comox Street", "604-443-3292 Ext 2, 1", "Men", "19+", "No information available.", "", "24 Hour", "", "Updated 22 hours ago", 1320, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-051", "Evelyne Saller Centre", "Vancouver", "404 Alexander Street", "604-665-3075", "Any Gender", "19+", "No information available.", "Wheelchair Accessible", "Seasonal", "Open for the season from November 1 to April 30, 2027. Shelter hours are 11 pm to 7:15 am Monday to Friday and 10 pm to 7:15 am Saturdays and Sundays.", "Updated 1 day ago", 1440, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-052", "Haven In The Hollow", "Mission", "32646 Logan Avenue", "604-820-9008", "Any Gender", "19+", "0", "", "24 Hour", "Temporarily Closed", "Updated 2 days ago", 2880, "MIX", 19, -1, false, 0, "beds", "tolerant"));
        pools.add(make("bc211-053", "Victoria Native Friendship Centre", "Victoria", "231 Regina Avenue", "250-886-1360", "Any Gender", "19+", "No information available.", "", "24 Hour, Overnight", "Shelter hours are 8 pm to 7 am.", "Updated 10 days ago", 14400, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-054", "Anchor Seasonal Shelter", "Vancouver", "119 East Cordova Street", "604-646-6899", "Any Gender", "19+", "40 mats for men and women", "", "Overnight, Seasonal", "The hours of operation are from 9pm-7am; Open September 30, 2026 and operate until the morning of April 1, 2027", "Updated 17 days ago", 24480, "MIX", 19, -1, true, 40, "mats", "tolerant"));
        pools.add(make("bc211-055", "Salvation Army ARC Chapel - EWR", "Victoria", "525 Johnson Street", "250-384-3396", "Any Gender", "19+", "No information available.", "", "Extreme Weather", "8 pm to 7:30 am", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 0, "beds", "tolerant"));
        pools.add(make("bc211-056", "Surrey Welcome Hub - EWR", "Surrey", "13545 King George Boulevard", "604-585-1023", "Any Gender", "not listed", "Any: 15 max", "", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", -1, -1, false, 15, "beds", "tolerant"));
        pools.add(make("bc211-057", "Directions Youth Services Centre - EWR", "Vancouver", "1138 Burrard Street", "604-633-1472 press 1", "Any Gender", "12–18 only, 19–24 only", "Any: 10 max.", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "13-24 years old", "Updated 17 days ago", 24480, "YOU", 12, 24, false, 10, "beds", "tolerant"));
        pools.add(make("bc211-058", "Imouto House - EWR", "Vancouver", "Address confidential", "604-250-1175 cell; 604-215-0369 landline", "Women", "12–18 only, 19–24 only", "Women: 4 max.", "Pet Friendly", "Extreme Weather", "16-24 years old", "Updated 17 days ago", 24480, "YOU", 12, 24, false, 4, "beds", "tolerant"));
        pools.add(make("bc211-059", "Kiwassa Neighbourhood House - EWR", "Vancouver", "2425 Oxford Street", "778-871-5198", "Any Gender", "19+", "Any: 20 max", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 20, "beds", "tolerant"));
        pools.add(make("bc211-060", "Surrey Alliance Church - EWR", "Surrey", "13474 96 Avenue", "672-337-3649 during shelter hours", "Any Gender", "19+", "Any: 30 max", "Takes Large Items, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 30, "beds", "tolerant"));
        pools.add(make("bc211-061", "E-FRY Women’s - EWR", "New Westminster", "Address confidential", "604-523-6749; 778-789-3502", "Women", "19+", "Women: 10 max", "Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "WOM", 19, -1, false, 10, "beds", "tolerant"));
        pools.add(make("bc211-062", "Gateway of Hope - EWR", "Langley", "Address confidential", "604-514-7375", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 0, "beds", "tolerant"));
        pools.add(make("bc211-063", "Shimai Transition House (Atira) - EWR", "Surrey", "Address confidential", "236-592-3272", "Women", "not listed", "Women: 6 max", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "WOM", -1, -1, false, 6, "beds", "tolerant"));
        pools.add(make("bc211-064", "Ladner United Church - EWR", "Delta", "4960 48 Avenue", "604-240-1335; 604-599-8900 24/h", "Any Gender", "not listed", "Any: 9 mx", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", -1, -1, false, 9, "beds", "tolerant"));
        pools.add(make("bc211-065", "OPS Overnight - EWR", "Vancouver", "141 East Hastings Street", "778-952-2015", "Any Gender", "19+", "Any: 20 max", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 20, "beds", "tolerant"));
        pools.add(make("bc211-066", "Marpole Neighbourhood House - EWR", "Vancouver", "8585 Hudson Street", "604-616-9050", "Any Gender", "19+", "Any: 30 max", "Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 30, "beds", "tolerant"));
        pools.add(make("bc211-067", "Commercial Hastings - EWR", "Vancouver", "1726 East Hastings Street", "604-376-8216", "Any Gender", "19+", "Any: 15", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "CLOSED", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 15, "beds", "tolerant"));
        pools.add(make("bc211-068", "North Shore Housing Centre Shelter - EWR", "North Vancouver", "705 2nd Street West", "604-982-9126 Ext 0", "Any Gender", "19+", "Any: 20", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 20, "beds", "tolerant"));
        pools.add(make("bc211-069", "St. Andrew’s United Church EWR", "North Vancouver", "1044 Saint Georges Avenue", "604-319-2809", "Any Gender", "not listed", "Any: 20 max", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", -1, -1, false, 20, "beds", "tolerant"));
        pools.add(make("bc211-070", "Salvation Army Belkin House - EWR", "Vancouver", "555 Homer Street", "604-681-3405", "Any Gender", "19+", "Men: 18 max; Women: 7 max", "", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 25, "beds", "tolerant"));
        pools.add(make("bc211-071", "Aboriginal Front Door - EWR", "Vancouver", "390 Main Street", "604-428-1908", "Any Gender", "19+", "Any: 28", "Wheelchair Accessible", "Extreme Weather", "CLOSED", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 28, "beds", "tolerant"));
        pools.add(make("bc211-072", "South Surrey Recreation & Arts Centre - EWR", "Surrey", "14601 20 Avenue", "604-315-0267", "Any Gender", "19+", "Any: 45 max", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 45, "beds", "tolerant"));
        pools.add(make("bc211-073", "Nightshift Ministries - EWR", "Surrey", "10635 King George Boulevard", "604-581-5172", "Any Gender", "19+", "Any: 16 mx", "", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 16, "beds", "tolerant"));
        pools.add(make("bc211-074", "Pacific Community Resources Society (PCRS) - EWR", "Surrey", "10453 Whalley Boulevard", "604-592-6200 (until 4:30); 236-866-1481 7pm - 7am", "Any Gender", "12–18 only, 19–24 only", "Any: 8 youth max", "", "Extreme Weather", "", "Updated 17 days ago", 24480, "YOU", 12, 24, false, 8, "beds", "tolerant"));
        pools.add(make("bc211-075", "Crossroads United Church - EWR", "Delta", "7655 120 Street", "604-834-7949 (9pm-6:30am); 604-957-1556", "Women", "19+", "Women: 10 max", "Pet Friendly, Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "WOM", 19, -1, false, 10, "beds", "tolerant"));
        pools.add(make("bc211-076", "Burnaby Neighbourhood Church (Southside) - EWR", "Burnaby", "7375 Kingsway", "236-514-5506", "Any Gender", "19+", "Any: 20", "Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 20, "beds", "tolerant"));
        pools.add(make("bc211-077", "Burnaby Westminster Bible Chapel - EWR", "Burnaby", "7540 6th Street", "604-240-1335 Shelter Hours only", "Any Gender", "19+", "Any: 27 mats max", "Wheelchair Accessible", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", 19, -1, false, 27, "mats", "tolerant"));
        pools.add(make("bc211-078", "Our Place Society at Broad View United - EWR", "Victoria", "3703 Saint Aidans Street", "250-507-8421", "Any Gender", "not listed", "Any: 20", "Pet Friendly", "Extreme Weather", "", "Updated 17 days ago", 24480, "MIX", -1, -1, false, 20, "beds", "tolerant"));
        pools.add(make("bc211-079", "Aboriginal Shelter", "Vancouver", "201 Central Street", "604-558-1477", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "Overnight", "", "Updated 29 days ago", 41760, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-080", "Aboriginal Front Door", "Vancouver", "390 Main Street", "604-428-1908", "Any Gender", "19+", "No information available.", "Takes Large Items, Wheelchair Accessible", "First Come-First Serve, Overnight", "", "Updated 30 days ago", 43200, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-081", "Covenant House - Community Support Services Mat Program", "Vancouver", "1302 Seymour Street", "not listed", "Any Gender", "19–24 only", "No information available.", "Wheelchair Accessible", "Seasonal", "Operating November 15th to Sept 30th; General Number 604-685-7474", "Updated 30 days ago", 43200, "YOU", 19, 24, false, 0, "beds", "tolerant"));
        pools.add(make("bc211-082", "Directions Youth Shelter", "Vancouver", "1138 Burrard Street", "604-633-1472 Ext 1", "Any Gender", "19–24 only", "No information available.", "Pet Friendly, Wheelchair Accessible", "Overnight", "Youth 19-24 ages only", "Updated 30 days ago", 43200, "YOU", 19, 24, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-083", "The Gathering Place", "Vancouver", "609 Helmcken Street", "604-257-3846 (8:30pm-7:30am)", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "Overnight", "age 18 and over", "Updated 30 days ago", 43200, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-084", "New Fountain Shelter", "Vancouver", "356 East Hastings Street", "604-669-3306", "Any Gender", "19+", "No information available.", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "Overnight", "Shelter for adults age 25 and older", "Updated 30 days ago", 43200, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-085", "Triage Shelter", "Vancouver", "707 Powell Street", "604-254-3700", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "24 Hour", "", "Updated 30 days ago", 43200, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-086", "The Sisterhood", "Vancouver", "342 Alexander Street", "604-681-7916", "Women", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "24 Hour", "Harm Reduction Shelter", "Updated 30 days ago", 43200, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-087", "House of Hope Shelter", "Hope", "650 Old Hope-Princeton Highway", "604-869-7574", "Any Gender", "19+", "No information available.", "", "24 Hour", "", "Updated 30 days ago", 43200, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-088", "Haven in the Hollow Temporary Winter Shelter", "Mission", "32646 Logan Avenue", "604-820-9008", "Any Gender", "19+", "No information available.", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "Seasonal", "Temporarily Closed", "Updated 30 days ago", 43200, "MIX", 19, -1, false, 0, "beds", "tolerant"));
        pools.add(make("bc211-089", "The Nest", "Abbotsford", "34561 Delair Road", "604-768-7864", "Women", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "Seasonal", "", "Updated 30 days ago", 43200, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-090", "1670 Shelter across VAFCS", "Vancouver", "Address confidential", "604-428-3625", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "24 Hour", "Storage is limited to personal items that can fit into storage bins. Service animals and pets are welcome, but pets need to be on a leash, preferably in a portable cage. Zero tolerance for drugs or alcohol on the property.", "Updated 90 days ago", 129600, "MIX", 19, -1, true, 0, "beds", "program-required"));
        pools.add(make("bc211-091", "Out of the Rain Youth Shelter", "Victoria", "1450 Elford Street", "250-415-3856 during shelter hours", "Any Gender", "12–18 only, 19–24 only", "No information available.", "Pet Friendly, Wheelchair Accessible", "First Come-First Serve", "Youth ages 15-25 old", "Updated 99 days ago", 142560, "YOU", 12, 24, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-092", "UGM Emergency Shelter For Men", "Vancouver", "601 East Hastings Street", "604-253-3323 Ext. 305", "Men", "19+", "No information available.", "Wheelchair Accessible", "Overnight", "", "Updated 108 days ago", 155520, "MEN", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-093", "Walton Shelter (Temporary)", "Vancouver", "261 East Hastings Street", "604-688-9129 Ext. 104", "Any Gender", "19+", "No information available.", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "First Come-First Serve", "Bunk beds and mats", "Updated 108 days ago", 155520, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-094", "Sisters Shelter (Atira)", "Vancouver", "131 Dunlevy Avenue", "604-602-9463", "Women", "19+", "No information available.", "Pet Friendly", "Overnight", "", "Updated 108 days ago", 155520, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-095", "Our Place Shelter", "Victoria", "919 Pandora Avenue", "250-388-7112 local 222", "Any Gender", "19+", "No information available.", "Wheelchair Accessible", "Overnight", "", "Updated 108 days ago", 155520, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-096", "DEWC Women’s Overflow Shelter", "Vancouver", "265 East Hastings Street", "604-564-9331; 604-723-5675", "Women", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "First Come-First Serve", "Open from 11:30 pm to 6 pm seven days a week. Closed 6pm to 11:30 pm", "Updated 108 days ago", 155520, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-097", "DEWC Warming and Respite Space", "Vancouver", "398 Powell Street", "604-423-4216", "Women", "All Ages", "No information available.", "Pet Friendly, Wheelchair Accessible", "First Come-First Serve", "", "Updated 108 days ago", 155520, "WOM", -1, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-098", "Women’s Emergency Shelter (412 Shelter)", "Vancouver", "412 East Cordova Street", "604-423-4807 Loc 0", "Women", "19+", "No information available.", "Wheelchair Accessible", "Overnight", "", "Updated 111 days ago", 159840, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-099", "Sparrow Community Care Society - Older Adults Shelter", "Abbotsford", "Address confidential", "604-991-6937", "Any Gender", "50+ only", "No information available.", "", "24 Hour", "Call for intake - 9am-5pm", "Updated 111 days ago", 159840, "MIX", 50, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-100", "Cloverdale Temporary Shelter - Pacific Community Church", "Surrey", "5337 180 Street", "604-574-4341", "Any Gender", "19+", "No information available.", "Pet Friendly", "Seasonal", "", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-101", "Penny’s Place", "Abbotsford", "Address confidential", "236-668-2590", "Women", "19+", "No information available.", "", "24 Hour", "", "Updated 111 days ago", 159840, "WOM", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-102", "The Osborn", "Vancouver", "27 West Hastings Street", "604-681-2806", "Any Gender", "19+", "No information available.", "Pet Friendly, Takes Large Items", "24 Hour", "", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-103", "Sumas Shelter", "Abbotsford", "1821 Sumas Way", "604-334-4911", "Any Gender", "19+", "No information available.", "Pet Friendly, Wheelchair Accessible", "24 Hour", "Any community service provider can contact the shelter directly to refer clients.", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-104", "Purpose Society Shelter", "New Westminster", "502 Columbia Street", "604-522-6837", "Any Gender", "19+", "No information available.", "Pet Friendly", "24 Hour", "", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-105", "3030 Shelter", "Coquitlam", "3030 Gordon Avenue", "604-474-0435", "Any Gender", "19+", "No information available.", "Pet Friendly, Takes Large Items, Wheelchair Accessible", "24 Hour", "", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-106", "Pillars Program (Shelter) (Previously ReVive Shelter)", "Chilliwack", "46130 Margaret Avenue", "604-795-2322", "Any Gender", "19+", "No information available.", "Wheelchair Accessible", "24 Hour", "No holds over the phone.", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 0, "beds", "tolerant"));
        pools.add(make("bc211-107", "Foundations Emergency Shelter (Previously Pathways Shelter)", "Chilliwack", "46130 Margaret Avenue", "604-795-2322", "Any Gender", "19+", "Any: 52", "Wheelchair Accessible", "First Come-First Serve", "", "Updated 111 days ago", 159840, "MIX", 19, -1, true, 52, "beds", "tolerant"));
        return pools;
    }

    private static BedPool make(String id, String name, String city, String address,
            String phone, String serves, String ages, String beds, String features,
            String tags, String notes, String updated, int updatedMin, String category,
            int ageMin, int ageMax, boolean active, int physical, String countUnit,
            String sobriety) {
        BedPool p = new BedPool();
        p.id = id;
        p.siteName = name;
        p.city = city;
        p.area = city;
        p.address = address;
        p.phone = phone;
        p.servesText = serves;
        p.agesText = ages;
        p.bedsText = beds;
        p.featuresText = features;
        p.shelterType = tags;
        p.notes = notes;
        p.updatedText = updated;
        p.lastUpdatedMinAgo = updatedMin;
        p.category = category;
        p.ageMin = ageMin;
        p.ageMax = ageMax;
        p.active = active;
        p.hiddenSite = "Address confidential".equals(address);
        p.physical = physical;
        p.countUnit = countUnit;
        p.sobrietyStance = sobriety;
        p.intakeOpen = "00:00";
        p.intakeClose = "24:00";
        if (features.contains("Wheelchair Accessible")) { p.attributes.add("ACC"); }
        if (features.contains("Pet Friendly")) { p.attributes.add("PET"); }
        if ("FAM".equals(category)) {
            p.partyTypes.add("family");
        } else if ("YOU".equals(category)) {
            p.partyTypes.add("single");
        } else {
            p.partyTypes.add("single");
            p.partyTypes.add("couple");
        }
        return p;
    }
}
