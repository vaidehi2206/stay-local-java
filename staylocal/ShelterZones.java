package staylocal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 24: area-based distance. The app has no coordinates for
 * shelters - geocoding 88 street addresses would give false
 * precision for a directory snapshot, and the 19 confidential
 * sites must never be placed on a map at all. So distance works
 * the way outreach workers actually talk: by AREA.
 *
 * Every shelter with a public address is assigned an area (a
 * Vancouver neighbourhood, or its city) from the address BC 211
 * prints, using the street grid. Distances are anchor-to-anchor
 * straight-line kilometres and are always labelled "area
 * estimate". A confidential site's area is never shown - its
 * distance is simply unknown, and it sorts after the known ones.
 */
public class ShelterZones {

    private static final Map<String, double[]> ANCHORS = new LinkedHashMap<String, double[]>();

    static {
        // Vancouver neighbourhoods (anchor = a well-known corner)
        ANCHORS.put("Downtown Eastside", new double[]{49.2814, -123.0953});
        ANCHORS.put("Downtown Vancouver", new double[]{49.2795, -123.1207});
        ANCHORS.put("Fairview / VGH", new double[]{49.2613, -123.1240});
        ANCHORS.put("Mount Pleasant", new double[]{49.2625, -123.1008});
        ANCHORS.put("Commercial Drive", new double[]{49.2647, -123.0693});
        ANCHORS.put("Hastings-Sunrise", new double[]{49.2812, -123.0440});
        ANCHORS.put("Kitsilano", new double[]{49.2684, -123.1676});
        ANCHORS.put("Southeast Vancouver", new double[]{49.2185, -123.0550});
        ANCHORS.put("Marpole", new double[]{49.2098, -123.1300});
        // Cities
        ANCHORS.put("Burnaby", new double[]{49.2276, -123.0076});
        ANCHORS.put("New Westminster", new double[]{49.2057, -122.9110});
        ANCHORS.put("Surrey", new double[]{49.1896, -122.8479});
        ANCHORS.put("Richmond", new double[]{49.1666, -123.1336});
        ANCHORS.put("North Vancouver", new double[]{49.3200, -123.0724});
        ANCHORS.put("Coquitlam", new double[]{49.2838, -122.7932});
        ANCHORS.put("Langley", new double[]{49.1044, -122.6604});
        ANCHORS.put("Maple Ridge", new double[]{49.2193, -122.5980});
        ANCHORS.put("Mission", new double[]{49.1337, -122.3110});
        ANCHORS.put("Abbotsford", new double[]{49.0504, -122.3045});
        ANCHORS.put("Chilliwack", new double[]{49.1577, -121.9509});
        ANCHORS.put("Delta", new double[]{49.0847, -123.0584});
        ANCHORS.put("Hope", new double[]{49.3858, -121.4419});
        ANCHORS.put("Victoria", new double[]{48.4284, -123.3656});
    }

    /** Anchor names, in dropdown order. */
    public static List<String> anchorNames() {
        return new ArrayList<String>(ANCHORS.keySet());
    }

    private static int blockOf(String address) {
        int i = 0;
        while (i < address.length() && Character.isDigit(address.charAt(i))) {
            i++;
        }
        if (i == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(address.substring(0, i));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean has(String a, String... words) {
        for (String w : words) {
            if (a.contains(w)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The shelter's area, or "" when it must not be placed:
     * confidential sites return "" even though their city is public.
     */
    public static String zoneOf(BedPool p) {
        if (p == null || p.hiddenSite) {
            return "";
        }
        if (!"Vancouver".equals(p.city)) {
            return ANCHORS.containsKey(p.city) ? p.city : "";
        }
        String a = p.address.toLowerCase();
        int b = blockOf(p.address);
        if (has(a, "alexander", "cordova", "powell", "water st", "abbott", "carrall",
                "gore", "heatley", "dunlevy", "jackson", "hawks", "keefer", "columbia")) {
            return "Downtown Eastside";
        }
        if (a.contains("hastings") && b >= 0 && b < 700) {
            return "Downtown Eastside";
        }
        if (a.contains("main street") && b >= 0 && b < 700) {
            return "Downtown Eastside";
        }
        if (a.contains("pender") && b >= 0 && b < 500) {
            return "Downtown Eastside";
        }
        if (has(a, "terminal avenue", "central street")) {
            return "Mount Pleasant";
        }
        if (a.contains("oxford street")) {
            return "Hastings-Sunrise";
        }
        if (a.contains("hudson")) {
            return "Marpole";
        }
        if (a.contains("granville") && b >= 5000) {
            return "Marpole";
        }
        if (a.contains("oak street") && b >= 5000) {
            return "Marpole";
        }
        if (has(a, "70th avenue", "57th avenue")) {
            return "Marpole";
        }
        if (a.contains("cambie") && b >= 4000) {
            return "Marpole";
        }
        if (a.contains("yukon") && b >= 1600) {
            return "Marpole";
        }
        if (a.contains("marine drive")) {
            return (a.contains("west") || a.contains("sw") || b > 3000)
                    ? "Marpole" : "Southeast Vancouver";
        }
        if (has(a, "renfrew", "rupert", "wall street", "trinity", "cambridge")) {
            return "Hastings-Sunrise";
        }
        if (a.contains("hastings") && b >= 2000) {
            return "Hastings-Sunrise";
        }
        if (a.contains("hastings") && b >= 700) {
            return "Commercial Drive";
        }
        if (has(a, "commercial drive", "clark drive")) {
            return "Commercial Drive";
        }
        if (a.contains("victoria drive")) {
            return (b >= 0 && b < 2600) ? "Commercial Drive" : "Southeast Vancouver";
        }
        if (a.contains("nanaimo")) {
            return (b >= 0 && b < 2600) ? "Commercial Drive" : "Hastings-Sunrise";
        }
        if (a.contains("kingsway")) {
            return (b >= 0 && b < 600) ? "Mount Pleasant" : "Southeast Vancouver";
        }
        if (a.contains("fraser street")) {
            return (b >= 0 && b < 1400) ? "Mount Pleasant" : "Southeast Vancouver";
        }
        if (has(a, "knight street", "41st avenue", "49th avenue", "54th avenue",
                "elliott", "kerr street", "joyce")) {
            return "Southeast Vancouver";
        }
        if (a.contains("main street")) {
            return "Mount Pleasant";
        }
        if (has(a, "quebec", "ontario street", "great northern")) {
            return "Mount Pleasant";
        }
        if (has(a, "arbutus", "balsam", "vine", "yew street", "macdonald", "alma", "point grey")) {
            return "Kitsilano";
        }
        if (a.contains("4th avenue")) {
            return (b >= 0 && b < 1700) ? "Fairview / VGH" : "Kitsilano";
        }
        if (a.contains("broadway")) {
            if (a.contains("west")) {
                return (b >= 2600) ? "Kitsilano" : "Fairview / VGH";
            }
            return (b >= 600) ? "Commercial Drive" : "Mount Pleasant";
        }
        if (has(a, "oak street", "willow", "heather", "ash street", "laurel",
                "12th avenue", "16th avenue", "10th avenue", "yukon", "cambie")) {
            return "Fairview / VGH";
        }
        return "Downtown Vancouver";
    }

    /** Straight-line km between two areas; -1 when either is unknown. */
    public static double distanceKm(String zoneA, String zoneB) {
        double[] a = ANCHORS.get(zoneA);
        double[] b = ANCHORS.get(zoneB);
        if (a == null || b == null) {
            return -1;
        }
        double p1 = Math.toRadians(a[0]);
        double p2 = Math.toRadians(b[0]);
        double dp = Math.toRadians(b[0] - a[0]);
        double dl = Math.toRadians(b[1] - a[1]);
        double h = Math.sin(dp / 2) * Math.sin(dp / 2)
                + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2 * 6371.0 * Math.asin(Math.sqrt(h));
    }

    /** "Same area as X" / "~3.2 km from X (area estimate)". */
    public static String distanceText(String zone, String nearZone) {
        if (zone == null || zone.isEmpty() || nearZone == null || nearZone.isEmpty()) {
            return "";
        }
        if (zone.equals(nearZone)) {
            return "Same area as " + nearZone;
        }
        double km = distanceKm(zone, nearZone);
        if (km < 0) {
            return "";
        }
        return "~" + String.format("%.1f", km) + " km from " + nearZone + " (area estimate)";
    }
}
