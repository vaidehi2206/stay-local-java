package staylocal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Directions links that open the route directly in Google Maps or
 * Apple Maps — the shelter is already the destination, the starting
 * point and travel mode are already chosen, so the worker taps once
 * and the maps app takes over with live travel time, transit options
 * and step-by-step guidance. (This is the behaviour the app uses for
 * directions — see the detail screen.)
 */
public class MapLinks {

    /** mode: "walking", "driving" or "transit". Blank origin = their current location. */
    public static String google(String origin, String destination, String mode) {
        StringBuilder u = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        if (origin != null && !origin.trim().isEmpty()) {
            u.append("&origin=").append(enc(origin.trim()));
        }
        u.append("&destination=").append(enc(destination));
        u.append("&travelmode=").append(mode);
        return u.toString();
    }

    /** mode: "walking", "driving" or "transit". Blank origin = their current location. */
    public static String apple(String origin, String destination, String mode) {
        StringBuilder u = new StringBuilder("https://maps.apple.com/?");
        if (origin != null && !origin.trim().isEmpty()) {
            u.append("saddr=").append(enc(origin.trim())).append("&");
        }
        u.append("daddr=").append(enc(destination));
        u.append("&dirflg=").append(
                "walking".equals(mode) ? "w" : "transit".equals(mode) ? "r" : "d");
        return u.toString();
    }

    private static String enc(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }
}
