package staylocal;

import java.util.Random;

/**
 * The shape of the admin sign-in's second factor: a 6-digit one-time
 * code the admin must type before the dashboard opens.
 *
 * LOCAL DEMO ONLY: a real MFA code is generated on a server and sent
 * to the admin's own device, so the person at the keyboard never sees
 * it in advance. Here the app shows the code on screen precisely
 * because there is no server and no second device — it demonstrates
 * the step, it does not provide the security. Never present this as
 * real MFA.
 */
public class MfaDemo {

    public static String newCode(Random random) {
        return String.format("%06d", random.nextInt(1000000));
    }

    public static boolean matches(String entered, String expected) {
        if (entered == null || expected == null) {
            return false;
        }
        return entered.trim().equals(expected.trim());
    }
}
