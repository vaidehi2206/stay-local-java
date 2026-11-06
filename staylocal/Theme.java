package staylocal;

import java.awt.Color;
import java.awt.Font;

/**
 * Stay Local — Java version. Theme constants converted from the web app's
 * css/styles.css custom properties (my own palette: warm, teal + apricot).
 * There is no CSS in Swing, so every colour/font lives here and all
 * panels read from this class.
 */
public class Theme {

    // Colours (from --bg, --card, --ink, ... in styles.css)
    public static final Color BG = new Color(250, 246, 240);          // #faf6f0
    public static final Color CARD = new Color(255, 255, 255);        // #ffffff
    public static final Color INK = new Color(34, 49, 46);            // #22312e
    public static final Color MUTED = new Color(104, 115, 111);       // #68736f
    public static final Color ACCENT = new Color(15, 118, 110);       // #0f766e
    public static final Color ACCENT_DARK = new Color(17, 94, 89);    // #115e59
    public static final Color POP = new Color(245, 158, 11);          // #f59e0b (scores)
    public static final Color LINE = new Color(229, 221, 210);        // #e5ddd2 (borders)
    public static final Color BADGE_BG = new Color(236, 253, 245);    // #ecfdf5
    public static final Color PHOTO_A = new Color(153, 246, 228);     // card photo gradient start
    public static final Color PHOTO_B = new Color(253, 230, 138);     // card photo gradient end

    // Fonts (the web app uses Georgia, serif)
    public static final Font BRAND_FONT = new Font("Georgia", Font.BOLD, 34);
    public static final Font HEADING_FONT = new Font("Georgia", Font.BOLD, 24);
    public static final Font CARD_TITLE_FONT = new Font("Georgia", Font.BOLD, 19);
    public static final Font BODY_FONT = new Font("Georgia", Font.PLAIN, 15);
    public static final Font LABEL_FONT = new Font("Georgia", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Georgia", Font.BOLD, 16);
    public static final Font SMALL_FONT = new Font("Georgia", Font.PLAIN, 13);

    private Theme() {
        // Constants only — never instantiated.
    }
}
