package staylocal;

/**
 * Phase 26: the full regression runner. The gap queue from the
 * November 5 evaluation is closed (Phases 14-25); this runs every
 * check suite in the project, in build order, so the finished app
 * can be verified end to end in one run. Every suite should end
 * with ALL TESTS PASS.
 *
 * (The hotel-era DataCheck / LogicCheck and the superseded
 * DirectionsCheck were removed with the code they tested.)
 */
public class AllChecks {

    private static void suite(String title, Runnable run) {
        System.out.println();
        System.out.println("===== " + title + " =====");
        run.run();
    }

    public static void main(String[] args) {
        suite("Phase 3 - ShelterCheck (directory + eligibility)", () -> ShelterCheck.main(args));
        suite("Phase 4 - SearchCheck (search + sort)", () -> SearchCheck.main(args));
        suite("Phase 5 - HoldCheck (hold flow)", () -> HoldCheck.main(args));
        suite("Phase 6 - ConsoleCheck (shelter console)", () -> ConsoleCheck.main(args));
        suite("Phase 7 - FilterCheck (every detail a filter)", () -> FilterCheck.main(args));
        suite("Phase 8 - AdminCheck (report + registry + MFA)", () -> AdminCheck.main(args));
        suite("Phase 9 - AdminOpsCheck (admin shelter powers + notices)", () -> AdminOpsCheck.main(args));
        suite("Phase 10 - MapLinksCheck (Google/Apple Maps handoff)", () -> MapLinksCheck.main(args));
        suite("Phase 12 - PartyCheck (coming as)", () -> PartyCheck.main(args));
        suite("Phase 13 - WatchCheck (availability alerts)", () -> WatchCheck.main(args));
        suite("Phase 14 - IntakeCheck (intake windows)", () -> IntakeCheck.main(args));
        suite("Phase 15 - NeedsCheck (beds-needed + arrival needs)", () -> NeedsCheck.main(args));
        suite("Phase 16 - MyHoldsCheck (extend + ETA)", () -> MyHoldsCheck.main(args));
        suite("Phase 17 - TurnAwayCheck (turn-away + failed arrival)", () -> TurnAwayCheck.main(args));
        suite("Phase 18 - DemandCheck (settled-search demand log)", () -> DemandCheck.main(args));
        suite("Phase 19 - SobrietyCheck (low-barrier + mentioned flags)", () -> SobrietyCheck.main(args));
        suite("Phase 20 - HandoutCheck (printable handouts)", () -> HandoutCheck.main(args));
        suite("Phase 21 - ConsoleOpsCheck (reserve + day summary)", () -> ConsoleOpsCheck.main(args));
        suite("Phase 22 - AdminAccountsCheck (account powers)", () -> AdminAccountsCheck.main(args));
        suite("Phase 23 - DirectoryCheck (snapshot age + verified)", () -> DirectoryCheck.main(args));
        suite("Phase 24 - DistanceCheck (area distance)", () -> DistanceCheck.main(args));
        suite("Phase 25 - LanguageCheck (language + safety)", () -> LanguageCheck.main(args));
        System.out.println();
        System.out.println("===== ALL SUITES RUN - every one should say ALL TESTS PASS above =====");
    }
}
