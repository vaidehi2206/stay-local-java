package staylocal;

import java.util.ArrayList;
import java.util.List;

/**
 * Public information and government updates, posted by the admin and
 * shown at the top of the app so everyone — health care workers and
 * shelter staff alike — sees the newest update first.
 *
 * In this local app the board lives for the session; in production
 * notices are stored on the server, carry the issuing body's name
 * (City, BC Housing, a health authority) and an expiry, and reach
 * every signed-in dashboard. The admin screen prefixes each post
 * with its date before it lands here.
 */
public class NoticeBoard {

    public static final String NO_NOTICES = "No public updates right now.";

    private final List<String> notices = new ArrayList<String>();

    /** Posts an update; blank text is ignored. Newest is always first. */
    public void add(String text) {
        if (text == null) {
            return;
        }
        String t = text.trim();
        if (t.isEmpty()) {
            return;
        }
        notices.add(0, t);
    }

    public String latest() {
        if (notices.isEmpty()) {
            return NO_NOTICES;
        }
        return notices.get(0);
    }

    public List<String> all() {
        return new ArrayList<String>(notices);
    }

    public boolean remove(int index) {
        if (index < 0 || index >= notices.size()) {
            return false;
        }
        notices.remove(index);
        return true;
    }

    public int size() {
        return notices.size();
    }
}
