package staylocal;

/**
 * Phase 10 (revised) check main for MapLinks: the Google Maps and
 * Apple Maps directions URLs the detail screen opens.
 * Run this file in NetBeans: every test should print PASS.
 */
public class MapLinksCheck {

    static int failures = 0;

    static void check(String label, boolean ok, String detail) {
        System.out.println(label + (ok ? " PASS" : " FAIL") + "  " + detail);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) {
        String dest = "108 East Hastings Street, Vancouver, BC";

        String g = MapLinks.google("Waterfront Station", dest, "walking");
        check("T1 Google Maps link with origin",
                g.equals("https://www.google.com/maps/dir/?api=1"
                        + "&origin=Waterfront+Station"
                        + "&destination=108+East+Hastings+Street%2C+Vancouver%2C+BC"
                        + "&travelmode=walking"),
                g);

        String g2 = MapLinks.google("", dest, "transit");
        check("T2 Google Maps link, blank origin = current location",
                !g2.contains("origin=") && g2.contains("&travelmode=transit")
                        && g2.contains("destination=108+East+Hastings"),
                g2);

        String a = MapLinks.apple("Waterfront Station", dest, "walking");
        check("T3 Apple Maps link, walking",
                a.startsWith("https://maps.apple.com/?saddr=Waterfront+Station&daddr=108+East")
                        && a.endsWith("&dirflg=w"),
                a);

        boolean modes = MapLinks.apple("", dest, "transit").endsWith("&dirflg=r")
                && MapLinks.apple("", dest, "driving").endsWith("&dirflg=d")
                && !MapLinks.apple("  ", dest, "driving").contains("saddr=");
        check("T4 Apple Maps mode flags + blank origin", modes,
                "transit r, driving d, blank origin omitted");

        if (failures == 0) {
            System.out.println("ALL TESTS PASS");
        } else {
            System.out.println(failures + " TEST(S) FAILED");
        }
    }
}
