package staylocal;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import java.awt.Desktop;
import java.net.URI;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

/**
 * Stay Local — shelter edition, Phase 8: search, results, detail, the
 * hold flow (request hold -> confirm -> check in), and the shelter
 * console (the shelter's own screen: set open beds, open/close the
 * site, confirm or reject incoming holds), running on the real
 * BC 211 shelter directory (ShelterData, captured Nov 5, 2026).
 * Phase 8 adds the third dashboard — Admin: an MFA-gated screen with
 * the authorization registry (who may use which dashboard, and who
 * has MFA on) and the daily statistics report generated from the
 * other two dashboards' activity. Authorization and MFA are local
 * simulations (see AdminUsers and MfaDemo); the report is real,
 * computed from the directory and this session's holds.
 * Phase 9 completes the admin's powers: add a shelter, remove a
 * shelter (refused while its beds are spoken for), put a listing on
 * hold (it vanishes from worker search until released) — and post
 * public information / government updates to the notice board that
 * runs across the top of the app, so a new update reaches every
 * dashboard the moment the admin posts it.
 * Phase 10 (revised, per V's direction) puts directions one tap
 * away: on a shelter's detail screen the worker types where the
 * person is now (point A), and the shelter is point B — Open in
 * Google Maps / Open in Apple Maps hand the route to the maps app
 * (MapLinks builds the official directions URLs), which shows the
 * live travel time, transit options and step-by-step guidance.
 * Phase 11 makes search live: results refresh the moment anything
 * changes — a ticked checkbox, a dropdown choice, the age spinner,
 * or each character typed in the keyword field. No Search button
 * press needed (the button stays and still works).
 * Phase 12 adds the missing "Coming as" filter — on their own, a
 * couple, or a family with children — matched against who each
 * listing takes (BedPool.partyTypes, the same acceptance model the
 * eligibility engine uses), and shows that acceptance on the detail
 * screen ("Who can come: ...") so the worker can see it, not guess.
 * Phase 13 adds availability alerts, deliberately NOT as a
 * separate feature: when a search finds shelters but no OPEN bed
 * (or nothing at all), the results themselves ask — "Want an alert
 * the moment one opens?" — on the filters the worker already set.
 * One tap on "Notify me" registers the request (WatchManager; the
 * request names itself from the filters, nothing to type). The same
 * 2-second tick that expires holds re-checks every request; the
 * moment a matching shelter gains an open bed — shelter updates its
 * count, a hold expires or is cancelled, a site opens, the admin
 * adds one — the header badge lights up, a popup offers the shelter,
 * and the alert waits on the Alerts screen until dismissed or the
 * request is cancelled. No bed opens silently; nobody sleeps outside
 * because a search was run an hour too early. */
public class StayLocalApp extends JFrame {

    private final List<BedPool> pools = ShelterData.allShelters();
    private final HoldManager holdManager = new HoldManager(pools, 90);

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);

    private final JComboBox<String> cityBox = new JComboBox<String>();
    private final JComboBox<String> servesBox = new JComboBox<String>(
            new String[]{"Anyone", "Women", "Men", "Youth", "Families"});
    private final JComboBox<String> partyBox = new JComboBox<String>(
            new String[]{"Anyone", "On their own", "A couple", "Family with children"});
    private final JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
    private final JCheckBox petBox = new JCheckBox("Pet friendly");
    private final JCheckBox wheelchairBox = new JCheckBox("Wheelchair accessible");
    private final JCheckBox openOnlyBox = new JCheckBox("Only shelters listing open beds");
    private final JCheckBox intakeNowBox = new JCheckBox("Intake open now");
    private final JCheckBox lowBarrierBox = new JCheckBox("Low-barrier only (no sobriety program)");
    private final JCheckBox harmReductionBox = new JCheckBox("Harm reduction mentioned");
    private final JCheckBox healthBox = new JCheckBox("Health services mentioned");
    private final JCheckBox closedBox = new JCheckBox("Include closed / EWR sites");
    private final JComboBox<String> sortBox = new JComboBox<String>(new String[]{
            ShelterSearch.SORT_DIRECTORY, ShelterSearch.SORT_NAME,
            ShelterSearch.SORT_MOST_BEDS, ShelterSearch.SORT_RECENT,
            ShelterSearch.SORT_CITY, ShelterSearch.SORT_NEAREST});

    // Phase 7: every listing field as a filter
    private final JTextField keywordField = new JTextField(14);
    private final JComboBox<String> typeBox = new JComboBox<String>(new String[]{
            "Any type", "24 Hour", "Overnight", "Seasonal",
            "Extreme Weather", "First Come-First Serve"});
    private final JComboBox<String> bedsBox = new JComboBox<String>(new String[]{
            "Any listing", "Open beds stated", "Listed Full", "No information"});
    private final JComboBox<String> updatedBox = new JComboBox<String>(new String[]{
            "Any time", "Within 24 hours", "Within 7 days", "Within 30 days"});
    private final JCheckBox largeItemsBox = new JCheckBox("Takes large items");
    private final JCheckBox confidentialBox = new JCheckBox("Confidential address only");

    private final JLabel resultsLabel = new JLabel("");
    private final JPanel resultsPanel = new JPanel();
    private final JTextArea detailText = new JTextArea();
    private final JLabel detailOpenLabel = new JLabel("");
    private final JButton detailHoldButton = new JButton("Request a hold on one bed");
    private final JTextField labelField = new JTextField("App user", 14);

    private final JTextArea holdText = new JTextArea();
    private BedPool currentPool = null;
    private Hold currentHold = null;
    private final JLabel directoryStatusLabel = new JLabel("");
    // Phase 24: area-based distance
    private final JComboBox<String> nearBox = new JComboBox<String>();
    private String lastNearZone = "";
    private java.util.List<BedPool> lastResults = new java.util.ArrayList<BedPool>();

    // Phase 15: beds needed + arrival needs on the hold form
    private final JSpinner bedsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    private final java.util.List<JCheckBox> needsBoxes = new java.util.ArrayList<JCheckBox>();
    private final JCheckBox urgentBox = new JCheckBox("URGENT - needs attention on arrival");
    // Phase 25: language + safety on the hold form
    private final JComboBox<String> languageBox = new JComboBox<String>();
    private final java.util.List<JCheckBox> safetyBoxes = new java.util.ArrayList<JCheckBox>();
    private static final String[] SAFETY_FLAGS = {
        "Fleeing violence - discreet arrival", "Prefers female staff",
        "Service animal arriving", "Arriving with a support worker", "Name differs from ID"
    };
    private final JTextField needsNoteField = new JTextField(24);
    private static final String[] ARRIVAL_NEEDS = {
        "Gloves / socks / warm clothing", "Meal on arrival", "Hygiene kit",
        "Wheelchair / mobility help", "Blind / low vision", "Deaf / hard of hearing",
        "Interpreter needed", "Medical attention on arrival",
        "Mental health / ADHD support", "Harm reduction supplies", "Medication storage"
    };

    // Phase 21: console liveness
    private JPanel consoleScreenPanel = null;
    private final JLabel consoleNewHoldLabel = new JLabel("");
    private final JSpinner reserveSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    private int lastSeenHoldCount = 0;

    // Shelter console components (Phase 6)
    private final JComboBox<String> consoleShelterBox = new JComboBox<String>();
    private final JTextArea consoleInfo = new JTextArea();
    private final DefaultListModel<String> consoleHoldsModel = new DefaultListModel<String>();
    private final JList<String> consoleHoldsList = new JList<String>(consoleHoldsModel);
    private final java.util.List<Hold> consoleHoldIndex = new java.util.ArrayList<Hold>();
    private final JSpinner consoleOpenSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JButton consoleToggleButton = new JButton("Close site");
    private final JTextArea consoleLog = new JTextArea();

    // Admin dashboard components (Phase 8)
    private final JTextArea adminReportText = new JTextArea();
    private final JTextArea adminUsersText = new JTextArea();
    // Phase 22: live account registry + audit view
    private final AdminUsers.Registry userRegistry = new AdminUsers.Registry();
    private final DefaultListModel<String> adminUsersModel = new DefaultListModel<String>();
    private final JList<String> adminUsersList = new JList<String>(adminUsersModel);
    private final JTextArea adminAuditText = new JTextArea();
    private final JLabel adminDateLabel = new JLabel("");

    // Phase 9: admin shelter management + public updates
    private final NoticeBoard noticeBoard = new NoticeBoard();
    private final JLabel noticeLabel = new JLabel("");
    private final JComboBox<String> adminShelterBox = new JComboBox<String>();
    private final JButton adminHoldButton = new JButton("Put on hold");
    private final JTextField adminNoticeField = new JTextField(30);
    private final DefaultListModel<String> adminNoticesModel = new DefaultListModel<String>();
    private final JList<String> adminNoticesList = new JList<String>(adminNoticesModel);
    private boolean consoleRefilling = false;
    private boolean liveSearchSuspended = false;

    // Phase 18: demand logging (settled searches)
    private final SearchLog searchLog = new SearchLog();
    private boolean searchLogArmed = false;
    private Timer searchLogTimer = null;
    private String pendingSearchSummary = "";
    private int pendingSearchFound = 0;
    private int pendingSearchOpen = 0;

    // Phase 16: My holds screen
    private final DefaultListModel<String> myHoldsModel = new DefaultListModel<String>();
    private final JList<String> myHoldsList = new JList<String>(myHoldsModel);
    private final java.util.List<Hold> myHoldsView = new java.util.ArrayList<Hold>();
    private final JSpinner etaSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 180, 5));

    // Phase 13: availability alerts (watches)
    private final WatchManager watchManager = new WatchManager();
    private final JButton alertsButton = new JButton("Alerts");
    private final DefaultListModel<String> alertsModel = new DefaultListModel<String>();
    private final JList<String> alertsList = new JList<String>(alertsModel);
    private final DefaultListModel<String> watchesModel = new DefaultListModel<String>();
    private final JList<String> watchesList = new JList<String>(watchesModel);

    // Phase 10 (revised): directions open in Google Maps / Apple Maps
    private final JTextField dirFromField = new JTextField(24);
    private final JComboBox<String> dirModeBox = new JComboBox<String>(
            new String[] {"Walking", "Driving", "Transit"});
    private final JButton googleMapsButton = new JButton("Open in Google Maps");
    private final JButton appleMapsButton = new JButton("Open in Apple Maps");
    private final JTextArea dirText = new JTextArea();

    public StayLocalApp() {
        super("Stay Local — find a shelter bed");
        IntakeData.apply(pools); // Phase 14: BC 211 intake text + verified windows
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 760);
        setLocationRelativeTo(null);

        cityBox.addItem("Any city");
        for (String city : ShelterSearch.cities(pools)) {
            cityBox.addItem(city);
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        cardPanel.setBackground(Theme.BG);
        cardPanel.add(buildSearchScreen(), "search");
        cardPanel.add(buildDetailScreen(), "detail");
        cardPanel.add(buildHoldScreen(), "hold");
        cardPanel.add(buildConsoleScreen(), "console");
        cardPanel.add(buildAdminScreen(), "admin");
        cardPanel.add(buildAlertsScreen(), "alerts");
        cardPanel.add(buildMyHoldsScreen(), "myholds");
        root.add(cardPanel, BorderLayout.CENTER);

        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        // Demo holds expire on their own; sweep every 2 seconds and
        // keep whatever is on screen honest about it.
        Timer sweeper = new Timer(2000, e -> {
            if (holdManager.expireHolds() > 0) {
                refreshDetail();
                refreshHoldScreen();
                refreshConsole();
                refreshMyHolds();
                runSearch();
            }
            java.util.List<WatchManager.Alert> fresh =
                    watchManager.check(pools, nowText());
            if (!fresh.isEmpty()) {
                refreshAlerts();
                showAvailabilityPopup(fresh);
            }
            int holdCountNow = holdManager.holds().size();
            if (consoleScreenPanel != null && consoleScreenPanel.isShowing()) {
                if (holdCountNow > lastSeenHoldCount) {
                    Hold newest = holdManager.holds().get(holdCountNow - 1);
                    consoleNewHoldLabel.setText("New hold arrived: "
                            + newest.describe(System.currentTimeMillis()));
                }
                refreshConsole();
            }
            lastSeenHoldCount = holdCountNow;
        });
        sweeper.start();

        noticeBoard.add("Sample notice: this bar carries public information and "
                + "government updates posted by the admin — warming-centre openings, "
                + "program changes, weather alerts. (Sample wording; the admin posts the real ones.)");
        refreshNoticeBar();
        runSearch();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 4, 24));

        JLabel brand = new JLabel("Stay Local");
        brand.setFont(Theme.BRAND_FONT);
        brand.setForeground(Theme.ACCENT_DARK);

        JLabel tagline = new JLabel("Find a shelter bed tonight — real shelter listings, one search.");
        tagline.setFont(Theme.BODY_FONT);
        tagline.setForeground(Theme.MUTED);

        header.add(brand);
        header.add(tagline);

        // Public information / government updates, posted by the admin —
        // pinned under the header so every dashboard sees the newest one.
        JPanel noticeBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        noticeBar.setBackground(new java.awt.Color(254, 243, 199));
        noticeBar.setBorder(new LineBorder(Theme.POP));
        JLabel noticeTitle = new JLabel("Public updates:");
        noticeTitle.setFont(Theme.LABEL_FONT);
        noticeTitle.setForeground(Theme.INK);
        noticeLabel.setFont(Theme.LABEL_FONT);
        noticeLabel.setForeground(Theme.INK);
        noticeBar.add(noticeTitle);
        noticeBar.add(noticeLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(noticeBar);

        JPanel alertsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        alertsRow.setBackground(Theme.BG);
        alertsButton.setFont(Theme.BODY_FONT);
        alertsButton.addActionListener(e -> {
            refreshAlerts();
            cards.show(cardPanel, "alerts");
        });
        alertsRow.add(alertsButton);
        JLabel alertsHint = new JLabel("— a saved search alerts you the moment a matching bed opens");
        alertsHint.setFont(Theme.BODY_FONT);
        alertsHint.setForeground(Theme.MUTED);
        alertsRow.add(alertsHint);
        header.add(alertsRow);
        return header;
    }

    private JPanel buildSearchScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        screen.add(buildSearchForm(), BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout());
        middle.setBackground(Theme.BG);
        resultsLabel.setFont(Theme.HEADING_FONT);
        resultsLabel.setForeground(Theme.INK);
        resultsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 8, 0));
        middle.add(resultsLabel, BorderLayout.NORTH);

        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Theme.BG);
        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        middle.add(scroll, BorderLayout.CENTER);

        screen.add(middle, BorderLayout.CENTER);
        return screen;
    }

    private JPanel buildSearchForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setBackground(Theme.CARD);
        row1.add(label("City:"));
        row1.add(cityBox);
        row1.add(label("For:"));
        row1.add(servesBox);
        row1.add(label("Coming as:"));
        row1.add(partyBox);
        row1.add(label("Age (0 = any):"));
        row1.add(ageSpinner);
        row1.add(label("Sort:"));
        row1.add(sortBox);
        row1.add(label("Near:"));
        nearBox.addItem("Anywhere");
        for (String anchor : ShelterZones.anchorNames()) {
            nearBox.addItem(anchor);
        }
        row1.add(nearBox);
        form.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setBackground(Theme.CARD);
        petBox.setBackground(Theme.CARD);
        wheelchairBox.setBackground(Theme.CARD);
        openOnlyBox.setBackground(Theme.CARD);
        intakeNowBox.setBackground(Theme.CARD);
        closedBox.setBackground(Theme.CARD);
        largeItemsBox.setBackground(Theme.CARD);
        confidentialBox.setBackground(Theme.CARD);
        row2.add(petBox);
        row2.add(wheelchairBox);
        row2.add(largeItemsBox);
        row2.add(openOnlyBox);
        row2.add(intakeNowBox);
        row2.add(closedBox);

        JPanel row2b = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2b.setBackground(Theme.CARD);
        lowBarrierBox.setBackground(Theme.CARD);
        harmReductionBox.setBackground(Theme.CARD);
        healthBox.setBackground(Theme.CARD);
        row2b.add(label("Needs-based:"));
        row2b.add(lowBarrierBox);
        row2b.add(harmReductionBox);
        row2b.add(healthBox);
        row2.add(confidentialBox);
        form.add(row2);
        form.add(row2b);

        JPanel rowFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        rowFilters.setBackground(Theme.CARD);
        rowFilters.add(label("Keyword (name/notes):"));
        rowFilters.add(keywordField);
        rowFilters.add(label("Type:"));
        rowFilters.add(typeBox);
        rowFilters.add(label("Beds:"));
        rowFilters.add(bedsBox);
        rowFilters.add(label("Updated:"));
        rowFilters.add(updatedBox);
        form.add(rowFilters);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row3.setBackground(Theme.CARD);
        JButton searchButton = new JButton("Search shelters");
        searchButton.setFont(Theme.BUTTON_FONT);
        searchButton.addActionListener(e -> {
            searchLogArmed = true;
            runSearch();
        });
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(Theme.BODY_FONT);
        resetButton.addActionListener(e -> {
            liveSearchSuspended = true;
            cityBox.setSelectedIndex(0);
            servesBox.setSelectedIndex(0);
            partyBox.setSelectedIndex(0);
            ageSpinner.setValue(0);
            petBox.setSelected(false);
            wheelchairBox.setSelected(false);
            openOnlyBox.setSelected(false);
            intakeNowBox.setSelected(false);
            lowBarrierBox.setSelected(false);
            harmReductionBox.setSelected(false);
            healthBox.setSelected(false);
            closedBox.setSelected(false);
            largeItemsBox.setSelected(false);
            confidentialBox.setSelected(false);
            keywordField.setText("");
            typeBox.setSelectedIndex(0);
            bedsBox.setSelectedIndex(0);
            updatedBox.setSelectedIndex(0);
            sortBox.setSelectedIndex(0);
            nearBox.setSelectedIndex(0);
            liveSearchSuspended = false;
            searchLogArmed = true;
            runSearch();
        });
        row3.add(searchButton);
        row3.add(resetButton);
        JButton consoleButton = new JButton("Shelter console");
        consoleButton.setFont(Theme.BODY_FONT);
        consoleButton.addActionListener(e -> {
            refreshConsole();
            cards.show(cardPanel, "console");
        });
        row3.add(consoleButton);
        JButton myHoldsButton = new JButton("My holds");
        myHoldsButton.setFont(Theme.BODY_FONT);
        myHoldsButton.addActionListener(e -> {
            refreshMyHolds();
            cards.show(cardPanel, "myholds");
        });
        row3.add(myHoldsButton);
        JButton printResultsButton = new JButton("Print results");
        printResultsButton.setFont(Theme.BODY_FONT);
        printResultsButton.addActionListener(e -> printHandout(
                Handout.forResults(criteriaSummary(currentCriteria()), lastResults, todayText())));
        row3.add(printResultsButton);
        JButton adminButton = new JButton("Admin dashboard");
        adminButton.setFont(Theme.BODY_FONT);
        adminButton.addActionListener(e -> openAdmin());
        row3.add(adminButton);
        form.add(row3);

        // Live search (Phase 11): every filter change re-runs the search.
        java.awt.event.ActionListener live = e -> {
            searchLogArmed = true;
            runSearch();
        };
        cityBox.addActionListener(live);
        servesBox.addActionListener(live);
        partyBox.addActionListener(live);
        typeBox.addActionListener(live);
        bedsBox.addActionListener(live);
        updatedBox.addActionListener(live);
        sortBox.addActionListener(live);
        nearBox.addActionListener(live);
        petBox.addActionListener(live);
        wheelchairBox.addActionListener(live);
        openOnlyBox.addActionListener(live);
        intakeNowBox.addActionListener(live);
        lowBarrierBox.addActionListener(live);
        harmReductionBox.addActionListener(live);
        healthBox.addActionListener(live);
        closedBox.addActionListener(live);
        largeItemsBox.addActionListener(live);
        confidentialBox.addActionListener(live);
        ageSpinner.addChangeListener(e -> runSearch());
        javax.swing.event.DocumentListener typing = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                runSearch();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                runSearch();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                runSearch();
            }
        };
        keywordField.getDocument().addDocumentListener(typing);
        JPanel dataRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        dataRow.setBackground(Theme.CARD);
        directoryStatusLabel.setFont(Theme.BODY_FONT);
        directoryStatusLabel.setForeground(DirectoryInfo.isStale(LocalDate.now())
                ? Theme.POP : Theme.MUTED);
        directoryStatusLabel.setText(DirectoryInfo.statusLine(LocalDate.now()));
        dataRow.add(directoryStatusLabel);
        JButton bc211Button = new JButton("Open the BC 211 Shelter List");
        bc211Button.setFont(Theme.BODY_FONT);
        bc211Button.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(DirectoryInfo.LIST_URL));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Open " + DirectoryInfo.LIST_URL + " in your browser to see the live list.",
                        "BC 211", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        dataRow.add(bc211Button);
        form.add(dataRow);
        return form;
    }

    private JPanel buildDetailScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBackground(Theme.BG);
        JButton backButton = new JButton("Back to results");
        backButton.setFont(Theme.BODY_FONT);
        backButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        top.add(backButton);
        screen.add(top, BorderLayout.NORTH);

        detailText.setFont(Theme.BODY_FONT);
        detailText.setForeground(Theme.INK);
        detailText.setEditable(false);
        detailText.setLineWrap(true);
        detailText.setWrapStyleWord(true);
        detailText.setBackground(Theme.CARD);
        detailText.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        screen.add(detailText, BorderLayout.CENTER);

        JPanel holdBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        holdBar.setBackground(Theme.BG);
        detailOpenLabel.setFont(Theme.BODY_FONT);
        detailOpenLabel.setForeground(Theme.INK);
        holdBar.add(detailOpenLabel);
        holdBar.add(label("Hold for (initials or first name only):"));
        labelField.setToolTipText("Privacy: initials or a first name only - never a full name.");
        holdBar.add(labelField);
        holdBar.add(label("Beds:"));
        holdBar.add(bedsSpinner);
        detailHoldButton.setFont(Theme.BUTTON_FONT);
        detailHoldButton.addActionListener(e -> requestHold());
        holdBar.add(detailHoldButton);
        JButton printDetailButton = new JButton("Print handout");
        printDetailButton.setFont(Theme.BODY_FONT);
        printDetailButton.addActionListener(e -> {
            if (currentPool != null) {
                printHandout(Handout.forShelter(currentPool, todayText()));
            }
        });
        holdBar.add(printDetailButton);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setBackground(Theme.BG);
        south.add(holdBar);

        JPanel needsPanel = new JPanel();
        needsPanel.setLayout(new BoxLayout(needsPanel, BoxLayout.Y_AXIS));
        needsPanel.setBackground(Theme.BG);
        needsPanel.setBorder(BorderFactory.createEmptyBorder(2, 8, 4, 8));
        needsPanel.add(label("Anything the shelter should have ready when they arrive? (optional)"));
        JPanel needsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        needsGrid.setBackground(Theme.BG);
        for (String need : ARRIVAL_NEEDS) {
            JCheckBox box = new JCheckBox(need);
            box.setFont(Theme.BODY_FONT);
            box.setBackground(Theme.BG);
            needsBoxes.add(box);
            needsGrid.add(box);
        }
        urgentBox.setFont(Theme.BODY_FONT);
        urgentBox.setBackground(Theme.BG);
        needsGrid.add(urgentBox);
        needsPanel.add(needsGrid);
        JPanel noteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        noteRow.setBackground(Theme.BG);
        noteRow.add(label("Note for the shelter:"));
        noteRow.add(needsNoteField);
        needsPanel.add(noteRow);
        JPanel languageRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        languageRow.setBackground(Theme.BG);
        languageRow.add(label("Language they speak best:"));
        languageBox.addItem("Not noted");
        for (String lang : Hold.LANGUAGES) {
            languageBox.addItem(lang);
        }
        languageBox.setFont(Theme.BODY_FONT);
        languageRow.add(languageBox);
        languageRow.add(label("(anything but English flags an interpreter for the shelter)"));
        needsPanel.add(languageRow);
        JPanel safetyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        safetyRow.setBackground(Theme.BG);
        safetyRow.add(label("Safety:"));
        for (String flag : SAFETY_FLAGS) {
            JCheckBox box = new JCheckBox(flag);
            box.setFont(Theme.BODY_FONT);
            box.setBackground(Theme.BG);
            safetyBoxes.add(box);
            safetyRow.add(box);
        }
        needsPanel.add(safetyRow);
        south.add(needsPanel);

        JPanel dirPanel = new JPanel(new BorderLayout(0, 4));
        dirPanel.setBackground(Theme.BG);
        dirPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        JPanel dirRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        dirRow.setBackground(Theme.BG);
        dirRow.add(label("Directions from where they are now:"));
        dirFromField.setFont(Theme.BODY_FONT);
        dirRow.add(dirFromField);
        dirModeBox.setFont(Theme.BODY_FONT);
        dirRow.add(dirModeBox);
        googleMapsButton.setFont(Theme.BODY_FONT);
        googleMapsButton.addActionListener(e -> openMap(true));
        dirRow.add(googleMapsButton);
        appleMapsButton.setFont(Theme.BODY_FONT);
        appleMapsButton.addActionListener(e -> openMap(false));
        dirRow.add(appleMapsButton);
        dirPanel.add(dirRow, BorderLayout.NORTH);
        dirText.setFont(Theme.BODY_FONT);
        dirText.setForeground(Theme.INK);
        dirText.setEditable(false);
        dirText.setLineWrap(true);
        dirText.setWrapStyleWord(true);
        dirText.setBackground(Theme.CARD);
        JScrollPane dirScroll = new JScrollPane(dirText);
        dirScroll.setBorder(new LineBorder(Theme.LINE));
        dirScroll.setPreferredSize(new java.awt.Dimension(600, 118));
        dirPanel.add(dirScroll, BorderLayout.CENTER);
        south.add(dirPanel);

        screen.add(south, BorderLayout.SOUTH);
        return screen;
    }

    private JPanel buildHoldScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JLabel heading = new JLabel("Your hold");
        heading.setFont(Theme.HEADING_FONT);
        heading.setForeground(Theme.INK);
        heading.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
        screen.add(heading, BorderLayout.NORTH);

        holdText.setFont(Theme.BODY_FONT);
        holdText.setForeground(Theme.INK);
        holdText.setEditable(false);
        holdText.setLineWrap(true);
        holdText.setWrapStyleWord(true);
        holdText.setBackground(Theme.CARD);
        holdText.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        screen.add(holdText, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setBackground(Theme.BG);
        JButton confirmButton = new JButton("Confirm hold");
        confirmButton.setFont(Theme.BODY_FONT);
        confirmButton.addActionListener(e -> {
            if (currentHold != null) {
                holdManager.confirmHold(currentHold);
                refreshHoldScreen();
            }
        });
        JButton checkInButton = new JButton("Check in");
        checkInButton.setFont(Theme.BODY_FONT);
        checkInButton.addActionListener(e -> {
            if (currentHold != null) {
                holdManager.checkIn(currentHold);
                refreshHoldScreen();
            }
        });
        JButton cancelButton = new JButton("Cancel hold");
        cancelButton.setFont(Theme.BODY_FONT);
        cancelButton.addActionListener(e -> {
            if (currentHold != null) {
                holdManager.cancelHold(currentHold);
                refreshHoldScreen();
            }
        });
        JButton backButton = new JButton("Back to results");
        backButton.setFont(Theme.BODY_FONT);
        backButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        actions.add(confirmButton);
        actions.add(checkInButton);
        actions.add(cancelButton);
        JButton allHoldsButton = new JButton("See all my holds");
        allHoldsButton.setFont(Theme.BODY_FONT);
        allHoldsButton.addActionListener(e -> {
            refreshMyHolds();
            cards.show(cardPanel, "myholds");
        });
        actions.add(allHoldsButton);
        JButton printHoldButton = new JButton("Print handout");
        printHoldButton.setFont(Theme.BODY_FONT);
        printHoldButton.addActionListener(e -> {
            if (currentHold != null) {
                printHandout(Handout.forHold(currentHold, holdManager.pool(currentHold.poolId),
                        todayText(), System.currentTimeMillis()));
            }
        });
        actions.add(printHoldButton);
        actions.add(backButton);
        screen.add(actions, BorderLayout.SOUTH);
        return screen;
    }

    private JPanel buildConsoleScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        consoleScreenPanel = screen;
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBackground(Theme.BG);
        JLabel heading = new JLabel("Shelter console");
        heading.setFont(Theme.HEADING_FONT);
        heading.setForeground(Theme.INK);
        top.add(heading);
        JButton backTopButton = new JButton("Back to search");
        backTopButton.setFont(Theme.BODY_FONT);
        backTopButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        top.add(backTopButton);
        top.add(label("   Shelter:"));
        for (BedPool p : pools) {
            consoleShelterBox.addItem(p.siteName);
        }
        consoleShelterBox.addActionListener(e -> refreshConsole());
        top.add(consoleShelterBox);
        consoleNewHoldLabel.setFont(Theme.BODY_FONT);
        consoleNewHoldLabel.setForeground(Theme.POP);
        top.add(consoleNewHoldLabel);
        screen.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.BG);

        consoleInfo.setFont(Theme.BODY_FONT);
        consoleInfo.setForeground(Theme.INK);
        consoleInfo.setEditable(false);
        consoleInfo.setLineWrap(true);
        consoleInfo.setWrapStyleWord(true);
        consoleInfo.setBackground(Theme.CARD);
        consoleInfo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        center.add(consoleInfo);

        JLabel holdsLabel = label("Holds at this shelter (select one, then confirm or reject):");
        holdsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(holdsLabel);
        consoleHoldsList.setFont(Theme.BODY_FONT);
        JScrollPane holdsScroll = new JScrollPane(consoleHoldsList);
        holdsScroll.setBorder(new LineBorder(Theme.LINE));
        center.add(holdsScroll);

        JLabel logLabel = label("Session event log:");
        logLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(logLabel);
        consoleLog.setFont(Theme.SMALL_FONT);
        consoleLog.setForeground(Theme.MUTED);
        consoleLog.setEditable(false);
        consoleLog.setBackground(Theme.CARD);
        JScrollPane logScroll = new JScrollPane(consoleLog);
        logScroll.setBorder(new LineBorder(Theme.LINE));
        center.add(logScroll);

        screen.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        JPanel bedsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bedsRow.setBackground(Theme.BG);
        JPanel holdsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        holdsRow.setBackground(Theme.BG);
        actions.add(bedsRow);
        actions.add(holdsRow);
        actions.setBackground(Theme.BG);
        bedsRow.add(label("Open beds:"));
        bedsRow.add(consoleOpenSpinner);
        JButton setOpenButton = new JButton("Set");
        setOpenButton.setFont(Theme.BODY_FONT);
        setOpenButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p != null) {
                holdManager.setOpenCount(p.id, (Integer) consoleOpenSpinner.getValue());
                refreshConsole();
            }
        });
        bedsRow.add(setOpenButton);
        JButton fullButton = new JButton("Mark full");
        fullButton.setFont(Theme.BODY_FONT);
        fullButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p != null) {
                holdManager.setOpenCount(p.id, 0);
                refreshConsole();
            }
        });
        bedsRow.add(fullButton);
        consoleToggleButton.setFont(Theme.BODY_FONT);
        consoleToggleButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p != null) {
                holdManager.setActive(p.id, !p.active);
                refreshConsole();
            }
        });
        bedsRow.add(consoleToggleButton);
        bedsRow.add(label("Walk-in reserve:"));
        bedsRow.add(reserveSpinner);
        JButton reserveButton = new JButton("Set reserve");
        reserveButton.setFont(Theme.BODY_FONT);
        reserveButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p != null && !holdManager.setWalkInReserve(p.id, (Integer) reserveSpinner.getValue())) {
                JOptionPane.showMessageDialog(this,
                        "The reserve cannot exceed the beds not already spoken for.",
                        "Reserve refused", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshConsole();
        });
        bedsRow.add(reserveButton);
        JButton verifiedButton = new JButton("Mark verified (just called)");
        verifiedButton.setFont(Theme.BODY_FONT);
        verifiedButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p != null) {
                holdManager.markVerified(p.id);
                refreshConsole();
            }
        });
        bedsRow.add(verifiedButton);
        JButton confirmButton = new JButton("Confirm selected hold");
        confirmButton.setFont(Theme.BODY_FONT);
        confirmButton.addActionListener(e -> {
            Hold h = selectedConsoleHold();
            if (h != null) {
                holdManager.confirmHold(h);
                refreshConsole();
            }
        });
        holdsRow.add(confirmButton);
        JButton rejectButton = new JButton("Reject selected hold");
        rejectButton.setFont(Theme.BODY_FONT);
        rejectButton.addActionListener(e -> {
            Hold h = selectedConsoleHold();
            if (h != null) {
                holdManager.rejectHold(h, "Shelter rejected from console");
                refreshConsole();
            }
        });
        holdsRow.add(rejectButton);
        JButton backButton = new JButton("Back to search");
        backButton.setFont(Theme.BODY_FONT);
        backButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        JButton doorButton = new JButton("Log door turn-away");
        doorButton.setFont(Theme.BODY_FONT);
        doorButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p == null) {
                return;
            }
            String[] reasons = {"Full - no beds", "Not eligible for this site",
                "Intake closed", "Behaviour / safety"};
            Object picked = JOptionPane.showInputDialog(this,
                    "Someone at the door of " + p.siteName + " could not be admitted. Why?\n"
                            + "It is logged as unmet need in the admin's daily report.",
                    "Log door turn-away", JOptionPane.QUESTION_MESSAGE, null, reasons, reasons[0]);
            if (picked != null) {
                holdManager.logTurnAway("DOOR - " + picked, "Walk-in at " + p.siteName);
                refreshConsole();
            }
        });
        holdsRow.add(doorButton);
        JButton checkInButton = new JButton("Check in selected");
        checkInButton.setFont(Theme.BODY_FONT);
        checkInButton.addActionListener(e -> {
            Hold h = selectedConsoleHold();
            if (h != null) {
                holdManager.checkIn(h);
                if (h == currentHold) {
                    refreshHoldScreen();
                }
                refreshConsole();
            }
        });
        holdsRow.add(checkInButton);
        JButton noShowButton = new JButton("No-show selected");
        noShowButton.setFont(Theme.BODY_FONT);
        noShowButton.addActionListener(e -> {
            Hold h = selectedConsoleHold();
            if (h != null) {
                holdManager.noShow(h);
                if (h == currentHold) {
                    refreshHoldScreen();
                }
                refreshConsole();
            }
        });
        holdsRow.add(noShowButton);
        JButton summaryButton = new JButton("Day summary");
        summaryButton.setFont(Theme.BODY_FONT);
        summaryButton.addActionListener(e -> {
            BedPool p = consolePool();
            if (p != null) {
                JOptionPane.showMessageDialog(this, holdManager.shelterDaySummary(p.id),
                        "Day summary", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        holdsRow.add(summaryButton);
        holdsRow.add(backButton);
        screen.add(actions, BorderLayout.SOUTH);
        return screen;
    }

    private BedPool consolePool() {
        int i = consoleShelterBox.getSelectedIndex();
        if (i < 0 || i >= pools.size()) {
            return null;
        }
        return pools.get(i);
    }

    private Hold selectedConsoleHold() {
        int i = consoleHoldsList.getSelectedIndex();
        if (i < 0 || i >= consoleHoldIndex.size()) {
            return null;
        }
        return consoleHoldIndex.get(i);
    }

    private void refreshConsole() {
        if (!consoleRefilling) {
            // Keep the picker in sync with the directory (Phase 9: the
            // admin may have added or removed shelters since last time).
            String keepId = null;
            int oldIdx = consoleShelterBox.getSelectedIndex();
            if (oldIdx >= 0 && oldIdx < pools.size()) {
                keepId = pools.get(oldIdx).id;
            }
            consoleRefilling = true;
            consoleShelterBox.removeAllItems();
            for (BedPool pool : pools) {
                consoleShelterBox.addItem(pool.siteName + "  ·  " + pool.city);
            }
            int select = 0;
            if (keepId != null) {
                for (int i = 0; i < pools.size(); i++) {
                    if (pools.get(i).id.equals(keepId)) {
                        select = i;
                        break;
                    }
                }
            }
            if (consoleShelterBox.getItemCount() > 0) {
                consoleShelterBox.setSelectedIndex(select);
            }
            consoleRefilling = false;
        }
        BedPool p = consolePool();
        if (p == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(p.siteName).append(" — ").append(p.city).append("\n");
        sb.append("Beds as listed by 211: ").append(p.bedsText)
                .append("   ·   Listing ").append(p.updatedText.toLowerCase()).append("\n");
        sb.append("This session: total ").append(p.physical)
                .append("  ·  open ").append(p.openCount())
                .append("  ·  on hold ").append(p.pendingHolds)
                .append("  ·  confirmed ").append(p.confirmedHolds)
                .append("  ·  checked in ").append(p.checkedIn)
                .append("  ·  walk-in reserve ").append(p.walkInReserve).append("\n");
        sb.append(p.active ? "Site is OPEN in this session."
                : "Site is CLOSED in this session (211 showed it closed, or the console closed it).");
        consoleInfo.setText(sb.toString());
        consoleOpenSpinner.setValue(p.openCount());
        reserveSpinner.setValue(p.walkInReserve);
        consoleToggleButton.setText(p.active ? "Close site" : "Open site");

        int keepHoldId = -1;
        int selIdx = consoleHoldsList.getSelectedIndex();
        if (selIdx >= 0 && selIdx < consoleHoldIndex.size()) {
            keepHoldId = consoleHoldIndex.get(selIdx).id;
        }
        consoleHoldsModel.clear();
        consoleHoldIndex.clear();
        long now = System.currentTimeMillis();
        for (Hold h : holdManager.holds()) {
            if (h.poolId.equals(p.id)) {
                consoleHoldsModel.addElement(h.describe(now));
                consoleHoldIndex.add(h);
            }
        }
        if (consoleHoldIndex.isEmpty()) {
            consoleHoldsModel.addElement("(no holds yet)");
        }
        if (keepHoldId > 0) {
            for (int i = 0; i < consoleHoldIndex.size(); i++) {
                if (consoleHoldIndex.get(i).id == keepHoldId) {
                    consoleHoldsList.setSelectedIndex(i);
                    break;
                }
            }
        }

        StringBuilder log = new StringBuilder();
        for (String event : holdManager.eventLog()) {
            log.append(event).append("\n");
        }
        consoleLog.setText(log.toString());
    }

    private JPanel buildAdminScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBackground(Theme.BG);
        JLabel heading = new JLabel("Admin dashboard");
        heading.setFont(Theme.HEADING_FONT);
        heading.setForeground(Theme.INK);
        top.add(heading);
        adminDateLabel.setFont(Theme.BODY_FONT);
        adminDateLabel.setForeground(Theme.MUTED);
        top.add(adminDateLabel);
        screen.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.BG);

        JLabel reportLabel = label("Daily statistics report (directory + worker/shelter activity):");
        reportLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        center.add(reportLabel);
        adminReportText.setFont(Theme.BODY_FONT);
        adminReportText.setForeground(Theme.INK);
        adminReportText.setEditable(false);
        adminReportText.setBackground(Theme.CARD);
        JScrollPane reportScroll = new JScrollPane(adminReportText);
        reportScroll.setBorder(new LineBorder(Theme.LINE));
        center.add(reportScroll);

        JLabel usersLabel = label("Authorized accounts (demo registry — admin issues and revokes these):");
        usersLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(usersLabel);
        adminUsersText.setFont(Theme.BODY_FONT);
        adminUsersText.setForeground(Theme.INK);
        adminUsersText.setEditable(false);
        adminUsersText.setBackground(Theme.CARD);
        JScrollPane usersScroll = new JScrollPane(adminUsersText);
        usersScroll.setBorder(new LineBorder(Theme.LINE));
        center.add(usersScroll);
        adminUsersList.setFont(Theme.BODY_FONT);
        adminUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane usersListScroll = new JScrollPane(adminUsersList);
        usersListScroll.setBorder(new LineBorder(Theme.LINE));
        center.add(usersListScroll);
        JPanel accountRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        accountRow.setBackground(Theme.BG);
        JButton addAccountButton = new JButton("Add account...");
        addAccountButton.setFont(Theme.BODY_FONT);
        addAccountButton.addActionListener(e -> doAdminAddAccount());
        accountRow.add(addAccountButton);
        JButton revokeButton = new JButton("Revoke selected");
        revokeButton.setFont(Theme.BODY_FONT);
        revokeButton.addActionListener(e -> doAdminSetActive(false));
        accountRow.add(revokeButton);
        JButton reactivateButton = new JButton("Reactivate selected");
        reactivateButton.setFont(Theme.BODY_FONT);
        reactivateButton.addActionListener(e -> doAdminSetActive(true));
        accountRow.add(reactivateButton);
        JButton resetMfaButton = new JButton("Reset MFA (selected)");
        resetMfaButton.setFont(Theme.BODY_FONT);
        resetMfaButton.addActionListener(e -> doAdminResetMfa());
        accountRow.add(resetMfaButton);
        center.add(accountRow);

        JLabel manageLabel = label("Manage shelters (changes apply to every dashboard immediately):");
        manageLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(manageLabel);
        JPanel manageRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        manageRow.setBackground(Theme.BG);
        adminShelterBox.setFont(Theme.BODY_FONT);
        adminShelterBox.setPrototypeDisplayValue("St Rose of Lima Emergency Shelter - Surrey");
        adminShelterBox.addActionListener(e -> updateAdminHoldButton());
        manageRow.add(adminShelterBox);
        adminHoldButton.setFont(Theme.BODY_FONT);
        adminHoldButton.addActionListener(e -> doAdminHoldToggle());
        manageRow.add(adminHoldButton);
        JButton removeShelterButton = new JButton("Remove shelter");
        removeShelterButton.setFont(Theme.BODY_FONT);
        removeShelterButton.addActionListener(e -> doAdminRemove());
        manageRow.add(removeShelterButton);
        JButton addShelterButton = new JButton("Add shelter...");
        addShelterButton.setFont(Theme.BODY_FONT);
        addShelterButton.addActionListener(e -> doAdminAdd());
        manageRow.add(addShelterButton);
        center.add(manageRow);

        JLabel noticesLabel = label("Public information / government updates (shown at the top of the app):");
        noticesLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(noticesLabel);
        JPanel noticeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        noticeRow.setBackground(Theme.BG);
        adminNoticeField.setFont(Theme.BODY_FONT);
        noticeRow.add(adminNoticeField);
        JButton postNoticeButton = new JButton("Post update");
        postNoticeButton.setFont(Theme.BODY_FONT);
        postNoticeButton.addActionListener(e -> doPostNotice());
        noticeRow.add(postNoticeButton);
        JButton removeNoticeButton = new JButton("Remove selected");
        removeNoticeButton.setFont(Theme.BODY_FONT);
        removeNoticeButton.addActionListener(e -> doRemoveNotice());
        noticeRow.add(removeNoticeButton);
        center.add(noticeRow);
        adminNoticesList.setFont(Theme.BODY_FONT);
        adminNoticesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane noticesScroll = new JScrollPane(adminNoticesList);
        noticesScroll.setBorder(new LineBorder(Theme.LINE));
        noticesScroll.setPreferredSize(new java.awt.Dimension(600, 70));
        center.add(noticesScroll);

        JLabel auditLabel = label("Audit / activity log (everything the dashboards did, newest last):");
        auditLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(auditLabel);
        adminAuditText.setFont(Theme.BODY_FONT);
        adminAuditText.setForeground(Theme.INK);
        adminAuditText.setEditable(false);
        adminAuditText.setBackground(Theme.CARD);
        JScrollPane auditScroll = new JScrollPane(adminAuditText);
        auditScroll.setBorder(new LineBorder(Theme.LINE));
        center.add(auditScroll);
        screen.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setBackground(Theme.BG);
        JButton refreshButton = new JButton("Refresh report");
        refreshButton.setFont(Theme.BODY_FONT);
        refreshButton.addActionListener(e -> refreshAdmin());
        actions.add(refreshButton);
        JButton saveButton = new JButton("Save report as file...");
        saveButton.setFont(Theme.BODY_FONT);
        saveButton.addActionListener(e -> saveAdminReport());
        actions.add(saveButton);
        JButton backButton = new JButton("Back to search");
        backButton.setFont(Theme.BODY_FONT);
        backButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        actions.add(backButton);
        screen.add(actions, BorderLayout.SOUTH);
        return screen;
    }

    /** The MFA gate: in production the code arrives on the admin's own
     *  device; in this local demo the app shows it, then asks for it. */
    private void openAdmin() {
        String code = MfaDemo.newCode(new java.util.Random());
        JOptionPane.showMessageDialog(this,
                "Admin sign-in - step 2 of 2 (MFA)\n\n"
                        + "DEMO ONLY: in the real system this 6-digit code is sent to\n"
                        + "the admin's own phone and never shown on this screen.\n\n"
                        + "Your one-time code is:  " + code,
                "MFA code (demo)", JOptionPane.INFORMATION_MESSAGE);
        String entered = JOptionPane.showInputDialog(this,
                "Enter the 6-digit code to open the admin dashboard:",
                "Admin MFA", JOptionPane.PLAIN_MESSAGE);
        if (entered == null) {
            return; // cancelled
        }
        if (MfaDemo.matches(entered, code)) {
            refreshAdmin();
            cards.show(cardPanel, "admin");
        } else {
            JOptionPane.showMessageDialog(this,
                    "That code does not match. The admin dashboard stays closed.",
                    "MFA failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshAdmin() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        adminDateLabel.setText("   Daily report - " + date);
        adminReportText.setText(AdminStats.dailyReport(pools, holdManager, date, searchLog));
        adminReportText.setCaretPosition(0);

        java.util.List<AdminUsers.User> users = userRegistry.users();
        StringBuilder sb = new StringBuilder();
        sb.append(users.size()).append(" authorized accounts (").append(userRegistry.activeCount())
                .append(" active): ")
                .append(AdminUsers.countByRole(users, AdminUsers.ROLE_ADMIN)).append(" admin, ")
                .append(AdminUsers.countByRole(users, AdminUsers.ROLE_WORKER)).append(" health workers, ")
                .append(AdminUsers.countByRole(users, AdminUsers.ROLE_SHELTER)).append(" shelters.  ")
                .append(AdminUsers.withoutMfa(users)).append(" account(s) still without MFA.\n")
                .append("Select an account in the list below to revoke it, reactivate it, or reset its MFA.\n")
                .append("Demo registry: in production this lives on the server, behind real sign-in.");
        adminUsersText.setText(sb.toString());
        adminUsersText.setCaretPosition(0);
        int keepIdx = adminUsersList.getSelectedIndex();
        String keepName = (keepIdx >= 0 && keepIdx < users.size()) ? users.get(keepIdx).username : null;
        adminUsersModel.clear();
        for (AdminUsers.User u : users) {
            adminUsersModel.addElement(u.describe());
        }
        if (keepName != null) {
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).username.equals(keepName)) {
                    adminUsersList.setSelectedIndex(i);
                    break;
                }
            }
        }
        StringBuilder audit = new StringBuilder();
        for (String event : holdManager.eventLog()) {
            audit.append(event).append("\n");
        }
        adminAuditText.setText(audit.toString());
        adminAuditText.setCaretPosition(0);

        // Phase 9: refill the management controls from the live directory.
        String keepPoolId = adminSelectedPoolId();
        adminShelterBox.removeAllItems();
        for (BedPool p : pools) {
            adminShelterBox.addItem(poolItemLabel(p));
        }
        if (keepPoolId != null) {
            for (int i = 0; i < pools.size(); i++) {
                if (pools.get(i).id.equals(keepPoolId)) {
                    adminShelterBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateAdminHoldButton();
        adminNoticesModel.clear();
        for (String n : noticeBoard.all()) {
            adminNoticesModel.addElement(n);
        }
        String keepCity = (String) cityBox.getSelectedItem();
        cityBox.removeAllItems();
        cityBox.addItem("Any city");
        for (String c : ShelterSearch.cities(pools)) {
            cityBox.addItem(c);
        }
        if (keepCity != null) {
            cityBox.setSelectedItem(keepCity);
        }
    }

    private void refreshNoticeBar() {
        noticeLabel.setText(noticeBoard.latest());
    }

    private static String poolItemLabel(BedPool p) {
        return p.siteName + "  ·  " + p.city
                + (p.onHold ? "  ·  ON HOLD" : "")
                + (!p.active ? "  ·  closed" : "");
    }

    private String adminSelectedPoolId() {
        int idx = adminShelterBox.getSelectedIndex();
        if (idx < 0 || idx >= pools.size()) {
            return null;
        }
        return pools.get(idx).id;
    }

    private BedPool adminSelectedPool() {
        String id = adminSelectedPoolId();
        return id == null ? null : holdManager.pool(id);
    }

    private void updateAdminHoldButton() {
        BedPool p = adminSelectedPool();
        adminHoldButton.setText(p != null && p.onHold ? "Release hold" : "Put on hold");
    }

    private void doAdminHoldToggle() {
        BedPool p = adminSelectedPool();
        if (p == null) {
            return;
        }
        if (p.onHold) {
            holdManager.setOnHold(p.id, false, "");
            JOptionPane.showMessageDialog(this,
                    p.siteName + " is back in worker search.",
                    "Hold released", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String note = JOptionPane.showInputDialog(this,
                    "Why is " + p.siteName + " going on hold? (workers see it as unavailable)",
                    "Put listing on hold", JOptionPane.PLAIN_MESSAGE);
            if (note == null) {
                return; // cancelled
            }
            holdManager.setOnHold(p.id, true, note);
            JOptionPane.showMessageDialog(this,
                    p.siteName + " is on hold: hidden from worker search until released.",
                    "Listing on hold", JOptionPane.INFORMATION_MESSAGE);
        }
        refreshAdmin();
    }

    private void doAdminRemove() {
        BedPool p = adminSelectedPool();
        if (p == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove " + p.siteName + " (" + p.city + ") from the directory?\n"
                        + "This applies to this session. A shelter with active holds\n"
                        + "or checked-in guests cannot be removed.",
                "Remove shelter", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (holdManager.removePool(p.id)) {
            JOptionPane.showMessageDialog(this,
                    p.siteName + " removed from the directory.",
                    "Shelter removed", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    p.siteName + " still has beds spoken for (hold, confirmed or checked in)\n"
                            + "and cannot be removed right now.",
                    "Removal refused", JOptionPane.WARNING_MESSAGE);
        }
        refreshAdmin();
    }

    private AdminUsers.User selectedUser() {
        int idx = adminUsersList.getSelectedIndex();
        java.util.List<AdminUsers.User> users = userRegistry.users();
        return (idx >= 0 && idx < users.size()) ? users.get(idx) : null;
    }

    /** Phase 22: issue a new account. New accounts start with MFA off - they enrol at first sign-in. */
    private void doAdminAddAccount() {
        JTextField usernameField = new JTextField(16);
        JTextField nameField = new JTextField(16);
        JTextField orgField = new JTextField(16);
        JComboBox<String> roleBox = new JComboBox<String>(new String[]{
            AdminUsers.ROLE_WORKER, AdminUsers.ROLE_SHELTER, AdminUsers.ROLE_ADMIN});
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 4));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Display name:"));
        form.add(nameField);
        form.add(new JLabel("Role:"));
        form.add(roleBox);
        form.add(new JLabel("Organization:"));
        form.add(orgField);
        int ok = JOptionPane.showConfirmDialog(this, form, "Add account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        AdminUsers.User u = new AdminUsers.User(usernameField.getText().trim(),
                nameField.getText().trim(), (String) roleBox.getSelectedItem(),
                orgField.getText().trim(), false);
        if (!userRegistry.addUser(u)) {
            JOptionPane.showMessageDialog(this,
                    "That username is blank or already registered.",
                    "Account not added", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        holdManager.noteEvent("ACCOUNT ADD  " + u.username + "  " + u.role);
        refreshAdmin();
    }

    /** Phase 22: revoke or reactivate the selected account; the record stays for the audit trail. */
    private void doAdminSetActive(boolean active) {
        AdminUsers.User u = selectedUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select an account in the list first.",
                    "Accounts", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (userRegistry.setActive(u.username, active)) {
            holdManager.noteEvent(active ? "ACCOUNT REACTIVATE  " + u.username
                    : "ACCOUNT REVOKE  " + u.username);
        }
        refreshAdmin();
    }

    /** Phase 22: force the selected account to re-enrol MFA at its next sign-in. */
    private void doAdminResetMfa() {
        AdminUsers.User u = selectedUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Select an account in the list first.",
                    "Accounts", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (userRegistry.resetMfa(u.username)) {
            holdManager.noteEvent("MFA RESET  " + u.username + "  must re-enrol (demo)");
        }
        refreshAdmin();
    }

    private void doAdminAdd() {
        JTextField nameField = new JTextField(24);
        JTextField cityField = new JTextField("Vancouver", 24);
        JTextField addressField = new JTextField(24);
        JTextField phoneField = new JTextField(24);
        JComboBox<String> servesBox = new JComboBox<String>(
                new String[] {"Any gender", "Women", "Men", "Youth", "Families"});
        JSpinner bedsSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 500, 1));
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Shelter name:"));
        form.add(nameField);
        form.add(new JLabel("City:"));
        form.add(cityField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Who it serves:"));
        form.add(servesBox);
        form.add(new JLabel("Beds open now:"));
        form.add(bedsSpinner);
        int choice = JOptionPane.showConfirmDialog(this, form,
                "Add a shelter (admin)", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        String name = nameField.getText().trim();
        String city = cityField.getText().trim();
        if (name.isEmpty() || city.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "A shelter needs at least a name and a city.",
                    "Not added", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String[] codes = {"MIX", "WOM", "MEN", "YOU", "FAM"};
        BedPool p = AdminOps.newShelter(name, city, addressField.getText().trim(),
                phoneField.getText().trim(), codes[servesBox.getSelectedIndex()],
                (Integer) bedsSpinner.getValue());
        String id = AdminOps.addShelter(pools, holdManager, p);
        JOptionPane.showMessageDialog(this,
                name + " added (" + id + "). Health care workers can find and hold its beds now.",
                "Shelter added", JOptionPane.INFORMATION_MESSAGE);
        refreshAdmin();
    }

    private void doPostNotice() {
        String text = adminNoticeField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"));
        noticeBoard.add(date + " — " + text);
        adminNoticeField.setText("");
        refreshNoticeBar();
        refreshAdmin();
    }

    private void doRemoveNotice() {
        int idx = adminNoticesList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        noticeBoard.remove(idx);
        refreshNoticeBar();
        refreshAdmin();
    }

    private void saveAdminReport() {
        JFileChooser chooser = new JFileChooser();
        String stamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        chooser.setSelectedFile(new File("StayLocal_Daily_Report_" + stamp + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(),
                    adminReportText.getText().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this,
                    "Report saved to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                    "Report saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save the report: " + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** "08:00" -> "8:00 AM", "23:30" -> "11:30 PM", "24:00" -> "midnight". */
    private static String intakeTimeLabel(String hhmm) {
        int mins = BedPool.toMinutes(hhmm);
        if (mins >= 1440) {
            return "midnight";
        }
        int h = mins / 60;
        int m = mins % 60;
        String ampm = h < 12 ? "AM" : "PM";
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return h12 + ":" + (m < 10 ? "0" : "") + m + " " + ampm;
    }

    private static String nowText() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
    }

    private JPanel buildAlertsScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JLabel heading = new JLabel("Availability alerts");
        heading.setFont(Theme.HEADING_FONT);
        heading.setForeground(Theme.INK);
        heading.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
        screen.add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.BG);

        center.add(label("New availability — a watched search just matched a shelter with a bed open:"));
        alertsList.setFont(Theme.BODY_FONT);
        alertsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane alertsScroll = new JScrollPane(alertsList);
        alertsScroll.setBorder(new LineBorder(Theme.LINE));
        alertsScroll.setPreferredSize(new java.awt.Dimension(600, 130));
        center.add(alertsScroll);

        JLabel watchesLabel = label("Notify requests — searches you asked to be alerted about:");
        watchesLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        center.add(watchesLabel);
        watchesList.setFont(Theme.BODY_FONT);
        watchesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane watchesScroll = new JScrollPane(watchesList);
        watchesScroll.setBorder(new LineBorder(Theme.LINE));
        watchesScroll.setPreferredSize(new java.awt.Dimension(600, 110));
        center.add(watchesScroll);
        screen.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actions.setBackground(Theme.BG);
        JButton viewButton = new JButton("View shelter");
        viewButton.setFont(Theme.BODY_FONT);
        viewButton.addActionListener(e -> {
            int idx = alertsList.getSelectedIndex();
            java.util.List<WatchManager.Alert> current = watchManager.alerts();
            if (idx >= 0 && idx < current.size()) {
                BedPool p = holdManager.pool(current.get(idx).poolId);
                if (p != null) {
                    showDetail(p);
                    cards.show(cardPanel, "detail");
                }
            }
        });
        actions.add(viewButton);
        JButton dismissButton = new JButton("Dismiss alert");
        dismissButton.setFont(Theme.BODY_FONT);
        dismissButton.addActionListener(e -> {
            int idx = alertsList.getSelectedIndex();
            java.util.List<WatchManager.Alert> current = watchManager.alerts();
            if (idx >= 0 && idx < current.size()) {
                watchManager.dismissAlert(current.get(idx));
                refreshAlerts();
            }
        });
        actions.add(dismissButton);
        JButton stopButton = new JButton("Cancel request");
        stopButton.setFont(Theme.BODY_FONT);
        stopButton.addActionListener(e -> {
            int idx = watchesList.getSelectedIndex();
            java.util.List<Watch> current = watchManager.watches();
            if (idx >= 0 && idx < current.size()) {
                watchManager.removeWatch(current.get(idx));
                refreshAlerts();
            }
        });
        actions.add(stopButton);
        JButton backButton = new JButton("Back to search");
        backButton.setFont(Theme.BODY_FONT);
        backButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        actions.add(backButton);
        screen.add(actions, BorderLayout.SOUTH);
        return screen;
    }

    private void refreshAlerts() {
        alertsModel.clear();
        for (WatchManager.Alert a : watchManager.alerts()) {
            alertsModel.addElement(a.describe());
        }
        if (alertsModel.isEmpty()) {
            alertsModel.addElement("No new availability right now.");
        }
        watchesModel.clear();
        for (Watch w : watchManager.watches()) {
            watchesModel.addElement(w.label + "  —  set " + w.createdAt
                    + "  —  " + watchManager.currentMatches(w, pools)
                    + " matching with a bed open now");
        }
        if (watchesModel.isEmpty()) {
            watchesModel.addElement("No requests yet — when a search finds no open bed, tap \"Notify me\" above the results.");
        }
        int n = watchManager.alertCount();
        alertsButton.setText(n == 0 ? "Alerts" : "Alerts (" + n + " new)");
    }

    /**
     * The ask, inline in the results: the worker's filters found no
     * open bed, so the app offers the alert on those same filters.
     * After one tap it flips to the on-the-list state.
     */
    private JPanel buildNotifyPrompt(final ShelterSearch criteria) {
        JPanel prompt = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        prompt.setBackground(new java.awt.Color(254, 243, 199));
        prompt.setBorder(new LineBorder(Theme.POP));
        if (watchManager.findBySignature(WatchManager.signatureOf(criteria)) != null) {
            JLabel on = new JLabel("No bed is open for this search yet — you're on the notify list. "
                    + "The moment one opens, you'll get an alert.");
            on.setFont(Theme.BODY_FONT);
            prompt.add(on);
        } else {
            JLabel ask = new JLabel("No bed is open for this search right now. "
                    + "Want an alert the moment one opens?");
            ask.setFont(Theme.BODY_FONT);
            prompt.add(ask);
            JButton notifyButton = new JButton("Notify me");
            notifyButton.setFont(Theme.BODY_FONT);
            notifyButton.addActionListener(e -> {
                watchManager.addWatch(criteriaSummary(criteria), criteria, pools, nowText());
                refreshAlerts();
                runSearch();
            });
            prompt.add(notifyButton);
        }
        JButton logTurnButton = new JButton("Log a turn-away");
        logTurnButton.setFont(Theme.BODY_FONT);
        logTurnButton.addActionListener(e -> {
            String[] reasons = {"No beds open (full)", "Not eligible", "Intake closed",
                "Too far / transport", "Person declined", "Other"};
            Object picked = JOptionPane.showInputDialog(this,
                    "Why couldn't this person be placed? It is recorded as unmet need\n"
                            + "in the admin's daily report - that is how missing beds get funded.",
                    "Log a turn-away", JOptionPane.QUESTION_MESSAGE, null, reasons, reasons[0]);
            if (picked != null) {
                holdManager.logTurnAway(picked.toString(), criteriaSummary(criteria));
                JOptionPane.showMessageDialog(this,
                        "Logged. This search now counts as unmet need, not a dead end.",
                        "Turn-away logged", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        prompt.add(logTurnButton);
        return prompt;
    }

    /** "Women - a couple - Vancouver - pet friendly" — the watch names itself. */
    private static String criteriaSummary(ShelterSearch c) {
        java.util.List<String> parts = new java.util.ArrayList<String>();
        if ("WOMEN".equals(c.serves)) {
            parts.add("Women");
        } else if ("MEN".equals(c.serves)) {
            parts.add("Men");
        } else if ("YOUTH".equals(c.serves)) {
            parts.add("Youth");
        } else if ("FAMILY".equals(c.serves)) {
            parts.add("Families");
        } else {
            parts.add("Anyone");
        }
        if ("single".equals(c.party)) {
            parts.add("on their own");
        } else if ("couple".equals(c.party)) {
            parts.add("a couple");
        } else if ("family".equals(c.party)) {
            parts.add("family with children");
        }
        parts.add(c.city.isEmpty() ? "Any city" : c.city);
        if (c.petFriendly) {
            parts.add("pet friendly");
        }
        if (c.wheelchair) {
            parts.add("wheelchair");
        }
        if (c.lowBarrierOnly) {
            parts.add("low-barrier");
        }
        if (c.harmReductionMentioned) {
            parts.add("harm reduction");
        }
        if (c.healthMentioned) {
            parts.add("health services");
        }
        if (!c.keyword.isEmpty()) {
            parts.add("\"" + c.keyword + "\"");
        }
        return String.join(" - ", parts);
    }

    private void showAvailabilityPopup(java.util.List<WatchManager.Alert> fresh) {
        StringBuilder sb = new StringBuilder("A bed just opened for a watched search:\n\n");
        int shown = Math.min(fresh.size(), 3);
        for (int i = 0; i < shown; i++) {
            WatchManager.Alert a = fresh.get(i);
            sb.append("• ").append(a.siteName).append(" (").append(a.city).append(") — ")
                    .append(a.bedsOpen).append(a.bedsOpen == 1 ? " bed" : " beds")
                    .append(" open — for: ").append(a.watchLabel).append("\n");
        }
        if (fresh.size() > shown) {
            sb.append("• …and ").append(fresh.size() - shown).append(" more (see Alerts)\n");
        }
        Object[] options = {"View shelter", "Later"};
        int choice = JOptionPane.showOptionDialog(this, sb.toString(),
                "New availability", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
        if (choice == 0) {
            BedPool p = holdManager.pool(fresh.get(0).poolId);
            if (p != null) {
                showDetail(p);
                cards.show(cardPanel, "detail");
            }
        }
    }

    private JPanel buildMyHoldsScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(Theme.BG);
        screen.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        JLabel heading = new JLabel("My holds");
        heading.setFont(Theme.HEADING_FONT);
        heading.setForeground(Theme.INK);
        heading.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
        screen.add(heading, BorderLayout.NORTH);

        myHoldsList.setFont(Theme.BODY_FONT);
        myHoldsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(myHoldsList);
        scroll.setBorder(new LineBorder(Theme.LINE));
        screen.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBackground(Theme.BG);
        JPanel rowA = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        rowA.setBackground(Theme.BG);
        JPanel rowB = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        rowB.setBackground(Theme.BG);
        actions.add(rowA);
        actions.add(rowB);

        JButton viewButton = new JButton("View shelter");
        viewButton.setFont(Theme.BODY_FONT);
        viewButton.addActionListener(e -> {
            Hold h = selectedMyHold();
            if (h != null) {
                BedPool p = holdManager.pool(h.poolId);
                if (p != null) {
                    showDetail(p);
                    cards.show(cardPanel, "detail");
                }
            }
        });
        rowA.add(viewButton);
        JButton extendButton = new JButton("Extend hold");
        extendButton.setFont(Theme.BODY_FONT);
        extendButton.addActionListener(e -> {
            Hold h = selectedMyHold();
            if (h == null) {
                return;
            }
            if (!holdManager.extendHold(h)) {
                JOptionPane.showMessageDialog(this,
                        "Only a live hold (still HELD) can be extended.",
                        "Cannot extend", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshMyHolds();
        });
        rowA.add(extendButton);
        rowA.add(label("ETA (min):"));
        rowA.add(etaSpinner);
        JButton etaButton = new JButton("Set ETA");
        etaButton.setFont(Theme.BODY_FONT);
        etaButton.addActionListener(e -> {
            Hold h = selectedMyHold();
            if (h == null) {
                return;
            }
            if (!holdManager.setEta(h, (Integer) etaSpinner.getValue())) {
                JOptionPane.showMessageDialog(this,
                        "An ETA can be set on a live or confirmed hold.",
                        "Cannot set ETA", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshMyHolds();
        });
        rowA.add(etaButton);
        JButton cancelButton = new JButton("Cancel hold");
        cancelButton.setFont(Theme.BODY_FONT);
        cancelButton.addActionListener(e -> {
            Hold h = selectedMyHold();
            if (h != null) {
                holdManager.cancelHold(h);
                if (h == currentHold) {
                    refreshHoldScreen();
                }
                refreshMyHolds();
                runSearch();
            }
        });
        rowB.add(cancelButton);
        JButton failedButton = new JButton("Log failed arrival");
        failedButton.setFont(Theme.BODY_FONT);
        failedButton.addActionListener(e -> {
            Hold h = selectedMyHold();
            if (h == null) {
                return;
            }
            String[] reasons = {"Refused at door - eligibility", "Refused at door - behaviour",
                "Arrived after intake closed", "Person did not arrive", "Person went elsewhere"};
            Object picked = JOptionPane.showInputDialog(this,
                    "What happened with hold #" + h.id + " at " + h.siteName + "?\n"
                            + "The bed is released and the failure is logged as unmet need.",
                    "Log failed arrival", JOptionPane.QUESTION_MESSAGE, null, reasons, reasons[0]);
            if (picked != null) {
                if (holdManager.logFailedArrival(h, picked.toString())) {
                    if (h == currentHold) {
                        refreshHoldScreen();
                    }
                    refreshMyHolds();
                    runSearch();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "That hold is already finished - nothing to log against it.",
                            "Hold closed", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        rowB.add(failedButton);
        JButton backButton = new JButton("Back to search");
        backButton.setFont(Theme.BODY_FONT);
        backButton.addActionListener(e -> {
            runSearch();
            cards.show(cardPanel, "search");
        });
        rowB.add(backButton);
        screen.add(actions, BorderLayout.SOUTH);
        return screen;
    }

    private void refreshMyHolds() {
        int selectedId = -1;
        int idx = myHoldsList.getSelectedIndex();
        if (idx >= 0 && idx < myHoldsView.size()) {
            selectedId = myHoldsView.get(idx).id;
        }
        myHoldsView.clear();
        java.util.List<Hold> all = holdManager.holds();
        for (int i = all.size() - 1; i >= 0; i--) {
            myHoldsView.add(all.get(i));
        }
        myHoldsModel.clear();
        long now = System.currentTimeMillis();
        for (Hold h : myHoldsView) {
            myHoldsModel.addElement(h.describe(now));
        }
        if (myHoldsModel.isEmpty()) {
            myHoldsModel.addElement("No holds yet this session - place one from a shelter's page.");
        }
        if (selectedId > 0) {
            for (int i = 0; i < myHoldsView.size(); i++) {
                if (myHoldsView.get(i).id == selectedId) {
                    myHoldsList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private Hold selectedMyHold() {
        int idx = myHoldsList.getSelectedIndex();
        return (idx >= 0 && idx < myHoldsView.size()) ? myHoldsView.get(idx) : null;
    }

    private static String todayText() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    /** Sends a handout to the system print dialog (Print to PDF works too). */
    private void printHandout(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));
        try {
            area.print(new java.text.MessageFormat("Stay Local - shelter handout"),
                    new java.text.MessageFormat("Page {0}"), true, null, null, true);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Printing did not complete: " + ex.getMessage(),
                    "Print", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BG);
        footer.setBorder(BorderFactory.createEmptyBorder(4, 24, 14, 24));

        JLabel note = new JLabel("Listings: " + ShelterData.SOURCE + ", retrieved " + ShelterData.RETRIEVED
                + ". 211 updates the list twice each weekday — always call ahead to confirm a bed. "
                + "For shelter help call or text 2-1-1; in an emergency call 911.");
        note.setFont(Theme.SMALL_FONT);
        note.setForeground(Theme.MUTED);
        footer.add(note, BorderLayout.CENTER);
        return footer;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.LABEL_FONT);
        l.setForeground(Theme.INK);
        return l;
    }

    private ShelterSearch currentCriteria() {
        ShelterSearch c = new ShelterSearch();
        String city = (String) cityBox.getSelectedItem();
        c.city = ("Any city".equals(city) || city == null) ? "" : city;
        String serves = (String) servesBox.getSelectedItem();
        if ("Women".equals(serves)) {
            c.serves = "WOMEN";
        } else if ("Men".equals(serves)) {
            c.serves = "MEN";
        } else if ("Youth".equals(serves)) {
            c.serves = "YOUTH";
        } else if ("Families".equals(serves)) {
            c.serves = "FAMILY";
        } else {
            c.serves = "ANY";
        }
        String party = (String) partyBox.getSelectedItem();
        if ("On their own".equals(party)) {
            c.party = "single";
        } else if ("A couple".equals(party)) {
            c.party = "couple";
        } else if ("Family with children".equals(party)) {
            c.party = "family";
        } else {
            c.party = "";
        }
        c.age = (Integer) ageSpinner.getValue();
        c.petFriendly = petBox.isSelected();
        c.wheelchair = wheelchairBox.isSelected();
        c.openOnly = openOnlyBox.isSelected();
        c.includeClosed = closedBox.isSelected();
        c.intakeOpenNow = intakeNowBox.isSelected();
        c.lowBarrierOnly = lowBarrierBox.isSelected();
        c.harmReductionMentioned = harmReductionBox.isSelected();
        c.healthMentioned = healthBox.isSelected();
        c.sort = (String) sortBox.getSelectedItem();
        c.nearZone = nearBox.getSelectedIndex() <= 0 ? "" : (String) nearBox.getSelectedItem();
        c.keyword = keywordField.getText().trim();
        c.largeItems = largeItemsBox.isSelected();
        c.confidentialOnly = confidentialBox.isSelected();
        String type = (String) typeBox.getSelectedItem();
        c.type = ("Any type".equals(type) || type == null) ? "" : type;
        String beds = (String) bedsBox.getSelectedItem();
        if ("Open beds stated".equals(beds)) {
            c.bedsStatus = "OPEN";
        } else if ("Listed Full".equals(beds)) {
            c.bedsStatus = "FULL";
        } else if ("No information".equals(beds)) {
            c.bedsStatus = "UNKNOWN";
        } else {
            c.bedsStatus = "ANY";
        }
        String updated = (String) updatedBox.getSelectedItem();
        if ("Within 24 hours".equals(updated)) {
            c.updatedWithinMin = 1440;
        } else if ("Within 7 days".equals(updated)) {
            c.updatedWithinMin = 10080;
        } else if ("Within 30 days".equals(updated)) {
            c.updatedWithinMin = 43200;
        } else {
            c.updatedWithinMin = -1;
        }
        return c;
    }

    private void runSearch() {
        if (liveSearchSuspended) {
            return; // a reset is mid-flight; it runs one search at the end
        }
        ShelterSearch criteria = currentCriteria();
        List<BedPool> results = ShelterSearch.search(pools, criteria);
        lastResults = results;
        lastNearZone = criteria.nearZone;
        resultsLabel.setText(results.size() + (results.size() == 1 ? " shelter found" : " shelters found"));
        resultsPanel.removeAll();
        boolean anyOpen = false;
        for (BedPool p : results) {
            if (p.openCount() > 0) {
                anyOpen = true;
                break;
            }
        }
        if (!anyOpen) {
            // Nothing to send them to tonight: offer the alert right
            // here, on the filters they already set. One tap, no form.
            resultsPanel.add(buildNotifyPrompt(criteria));
            resultsPanel.add(Box.createVerticalStrut(10));
        }
        if (results.isEmpty()) {
            JLabel none = new JLabel("No shelters match — try a wider city, or call 2-1-1 for help tonight.");
            none.setFont(Theme.BODY_FONT);
            none.setForeground(Theme.MUTED);
            resultsPanel.add(none);
        }
        for (BedPool p : results) {
            resultsPanel.add(buildCard(p));
            resultsPanel.add(Box.createVerticalStrut(10));
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
        if (searchLogArmed) {
            // Phase 18: record this search once it settles (3 s after
            // the last change), not on every keystroke.
            pendingSearchSummary = criteriaSummary(criteria);
            pendingSearchFound = results.size();
            int open = 0;
            for (BedPool p : results) {
                if (p.openCount() > 0) {
                    open++;
                }
            }
            pendingSearchOpen = open;
            if (searchLogTimer == null) {
                searchLogTimer = new Timer(3000, e -> commitSearchLog());
                searchLogTimer.setRepeats(false);
            }
            searchLogTimer.restart();
        }
    }

    private void commitSearchLog() {
        SearchLog.Entry last = searchLog.lastEntry();
        if (last != null && last.summary.equals(pendingSearchSummary)
                && last.found == pendingSearchFound && last.open == pendingSearchOpen) {
            return; // the same settled search as last time - count it once
        }
        searchLog.record(pendingSearchSummary, pendingSearchFound, pendingSearchOpen, nowText());
    }

    private JPanel buildCard(final BedPool p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE,
                lastNearZone.isEmpty() ? 130 : 150));

        JPanel text = new JPanel(new GridLayout(0, 1, 0, 2));
        text.setBackground(Theme.CARD);

        JLabel title = new JLabel(p.siteName + (p.active ? "" : "  (CLOSED)"));
        title.setFont(Theme.CARD_TITLE_FONT);
        title.setForeground(Theme.ACCENT_DARK);
        text.add(title);

        JLabel meta = new JLabel(p.city + "  ·  " + ShelterSearch.categoryLabel(p.category)
                + (p.shelterType.isEmpty() ? "" : "  ·  " + p.shelterType));
        meta.setFont(Theme.LABEL_FONT);
        meta.setForeground(Theme.MUTED);
        text.add(meta);

        JLabel beds = new JLabel("Beds as listed: " + p.bedsText
                + (p.openCount() != p.physical
                        ? "   ·   open in this session: " + p.openCount() : ""));
        beds.setFont(Theme.BODY_FONT);
        beds.setForeground(p.active && p.openCount() > 0 ? Theme.ACCENT_DARK : Theme.MUTED);
        text.add(beds);

        JLabel extra = new JLabel(
                (p.featuresText.isEmpty() ? "" : p.featuresText + "   ")
                        + p.updatedText + (p.phone.isEmpty() ? "" : "   ·   " + p.phone));
        extra.setFont(Theme.SMALL_FONT);
        extra.setForeground(Theme.MUTED);
        text.add(extra);

        if (!lastNearZone.isEmpty()) {
            String zone = ShelterZones.zoneOf(p);
            JLabel dist = new JLabel(zone.isEmpty()
                    ? "Distance: not shown (confidential address)"
                    : ShelterZones.distanceText(zone, lastNearZone));
            dist.setFont(Theme.SMALL_FONT);
            dist.setForeground(Theme.MUTED);
            text.add(dist);
        }

        card.add(text, BorderLayout.CENTER);

        JButton details = new JButton("Details");
        details.setFont(Theme.BODY_FONT);
        details.addActionListener(e -> showDetail(p));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Theme.CARD);
        btnPanel.add(details);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    /** "individuals, couples" — who a listing takes, in plain words. */
    private static String whoCanCome(BedPool p) {
        java.util.List<String> parts = new java.util.ArrayList<String>();
        if (p.partyTypes.contains("single")) {
            parts.add("individuals");
        }
        if (p.partyTypes.contains("couple")) {
            parts.add("couples");
        }
        if (p.partyTypes.contains("family")) {
            parts.add("families with children");
        }
        return parts.isEmpty() ? "not stated" : String.join(", ", parts);
    }

    private void showDetail(BedPool p) {
        currentPool = p;
        refreshDetail();
        cards.show(cardPanel, "detail");
    }

    private void refreshDetail() {
        if (currentPool == null) {
            return;
        }
        BedPool p = currentPool;
        StringBuilder sb = new StringBuilder();
        sb.append(p.siteName).append(p.active ? "" : "  (CLOSED — as listed by 211)").append("\n\n");
        sb.append("City: ").append(p.city).append("\n");
        sb.append("Address: ").append(p.address).append("\n");
        sb.append("Phone: ").append(p.phone.isEmpty() ? "not listed" : p.phone).append("\n");
        sb.append("Serves: ").append(p.servesText).append("\n");
        sb.append("Who can come: ").append(whoCanCome(p)).append("\n");
        String zone = ShelterZones.zoneOf(p);
        sb.append("Area: ").append(zone.isEmpty() ? "not shown (confidential address)" : zone);
        if (!zone.isEmpty() && !lastNearZone.isEmpty()) {
            sb.append("   ·   ").append(ShelterZones.distanceText(zone, lastNearZone));
        }
        sb.append("\n");
        sb.append("Ages: ").append(p.agesText).append("\n");
        sb.append("Beds available (as listed): ").append(p.bedsText).append("\n");
        sb.append("Features: ").append(p.featuresText.isEmpty() ? "none shown" : p.featuresText).append("\n");
        sb.append("Type: ").append(p.shelterType).append("\n");
        sb.append("Sobriety: ").append("program-required".equals(p.sobrietyStance)
                ? "sobriety program - guests must be sober\n"
                : "no sobriety program required\n");
        String mentions = ShelterFlags.mentionsLine(p);
        if (!mentions.isEmpty()) {
            sb.append(mentions).append("\n");
        }
        if (p.intakeText.isEmpty()) {
            sb.append("Intake info (BC 211): none listed - call ahead.\n");
        } else {
            sb.append("Intake info (BC 211): ").append(p.intakeText).append("\n");
            if (p.intakeKnown) {
                java.time.LocalTime now = java.time.LocalTime.now();
                boolean openNow = p.intakeOpenAt(now.getHour() * 60 + now.getMinute());
                sb.append("Intake window: ").append(intakeTimeLabel(p.intakeOpen)).append(" - ")
                        .append(intakeTimeLabel(p.intakeClose))
                        .append(openNow ? " (intake open now)\n" : " (intake closed right now)\n");
            }
        }
        sb.append("Notes: ").append(p.notes.isEmpty() ? "none" : p.notes).append("\n");
        sb.append("Listing ").append(p.updatedText.toLowerCase()).append(" on BC 211.\n\n");
        sb.append("Source: ").append(ShelterData.SOURCE).append(", retrieved ")
                .append(ShelterData.RETRIEVED).append(".\n");
        sb.append("Availability changes fast and this list updates twice each weekday — "
                + "call the shelter, or call/text 2-1-1, to confirm a bed for tonight.");
        detailText.setText(sb.toString());
        detailText.setCaretPosition(0);

        detailOpenLabel.setText("Open in this session: " + p.openCount()
                + " of " + p.physical + " listed  ·  ");
        detailHoldButton.setEnabled(p.active && p.openCount() > 0);

        if (p.hiddenSite) {
            googleMapsButton.setEnabled(false);
            appleMapsButton.setEnabled(false);
            dirText.setText("This shelter's address is confidential, so there are no "
                    + "directions to give out. Call the shelter at " + p.phone
                    + " and they will guide the person in safely.");
        } else {
            googleMapsButton.setEnabled(true);
            appleMapsButton.setEnabled(true);
            dirText.setText("Type where the person is now — a street corner, a station, "
                    + "or an address — or leave it blank to start from their current "
                    + "location. Pick how they are travelling, then open the route in "
                    + "Google Maps or Apple Maps: live travel time, transit options "
                    + "and step-by-step guidance are all in the maps app.");
        }
    }

    /** Opens the A -> B route in the chosen maps app (see MapLinks). */
    private void openMap(boolean googleMaps) {
        BedPool p = currentPool;
        if (p == null || p.hiddenSite) {
            return;
        }
        String origin = dirFromField.getText().trim();
        String destination = p.address + ", " + p.city + ", BC";
        String[] modes = {"walking", "driving", "transit"};
        String mode = modes[dirModeBox.getSelectedIndex()];
        String url = googleMaps
                ? MapLinks.google(origin, destination, mode)
                : MapLinks.apple(origin, destination, mode);
        String appName = googleMaps ? "Google Maps" : "Apple Maps";
        try {
            Desktop.getDesktop().browse(new URI(url));
            dirText.setText("Opened the route to " + p.siteName + " in " + appName
                    + " - it shows the live travel time and step-by-step guidance. "
                    + "Point A: " + (origin.isEmpty() ? "their current location" : origin)
                    + "  -  Point B: " + destination + ".");
        } catch (Exception ex) {
            dirText.setText("The maps app could not be opened from here. "
                    + "Give them the address instead: " + destination
                    + " - or call the shelter at " + p.phone + ".");
        }
    }

    /** Clears the beds/needs part of the hold form for the next person. */
    private void resetNeedsForm() {
        bedsSpinner.setValue(1);
        for (JCheckBox box : needsBoxes) {
            box.setSelected(false);
        }
        urgentBox.setSelected(false);
        needsNoteField.setText("");
        languageBox.setSelectedIndex(0);
        for (JCheckBox box : safetyBoxes) {
            box.setSelected(false);
        }
    }

    private void requestHold() {
        if (currentPool == null) {
            return;
        }
        String label = labelField.getText().trim();
        if (label.isEmpty()) {
            label = "App user";
        }
        java.util.List<String> needs = new java.util.ArrayList<String>();
        for (JCheckBox box : needsBoxes) {
            if (box.isSelected()) {
                needs.add(box.getText());
            }
        }
        int bedsWanted = (Integer) bedsSpinner.getValue();
        String language = languageBox.getSelectedIndex() <= 0
                ? "" : (String) languageBox.getSelectedItem();
        java.util.List<String> safety = new java.util.ArrayList<String>();
        for (JCheckBox box : safetyBoxes) {
            if (box.isSelected()) {
                safety.add(box.getText());
            }
        }
        Hold h = holdManager.placeHold(currentPool.id, label, bedsWanted,
                needs, needsNoteField.getText().trim(), urgentBox.isSelected(),
                language, safety);
        if (h == null) {
            JOptionPane.showMessageDialog(this,
                    "Not enough open beds for " + bedsWanted + " here, or the last beds were just taken.\n"
                            + "A hold is all-or-nothing, so a couple or family is never split.\n"
                            + "Try another shelter, or call 2-1-1 for help tonight.",
                    "No beds available", JOptionPane.INFORMATION_MESSAGE);
            refreshDetail();
            return;
        }
        currentHold = h;
        resetNeedsForm();
        refreshHoldScreen();
        cards.show(cardPanel, "hold");
    }

    private void refreshHoldScreen() {
        if (currentHold == null) {
            return;
        }
        Hold h = currentHold;
        BedPool p = holdManager.pool(h.poolId);
        StringBuilder sb = new StringBuilder();
        sb.append(h.describe(System.currentTimeMillis())).append("\n\n");
        sb.append("Shelter: ").append(h.siteName).append("\n");
        sb.append("Held for: ").append(h.profileLabel).append("\n");
        sb.append("Beds held: ").append(h.beds).append("\n");
        sb.append("Shelter should prepare: ").append(h.needsSummary()).append("\n");
        sb.append("Status: ").append(h.status).append("\n");
        if (p != null) {
            sb.append("Beds open at this shelter in this session: ").append(p.openCount()).append("\n");
        }
        sb.append("\nDemo rule: an unconfirmed hold expires after 90 seconds and the bed "
                + "returns to the pool.\n");
        sb.append("Production rule: holds last 45 minutes (set by the shelter), and the "
                + "shelter confirms each one — try it from the Shelter console.\n\n");
        sb.append("Nothing here is sent to the shelter or to 211; this is the app's own "
                + "session, starting from the Nov 5, 2026 listing snapshot.");
        holdText.setText(sb.toString());
        holdText.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StayLocalApp().setVisible(true));
    }
}
