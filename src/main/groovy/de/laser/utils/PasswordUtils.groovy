package de.laser.utils

import groovy.transform.CompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Helper class for managing user-password related tasks
 */
@CompileStatic
class PasswordUtils {

    public static final String USER_PASSWORD_INFO =
            "Das Passwort muss zwischen 8 und 20 Zeichen lang sein. " +
            "Es muss mindestens einen Großbuchstaben, einen Kleinbuchstaben, eine Zahl sowie ein Sonderzeichen aus der Liste ( !\$%&@#^+*~=:; ) enthalten. " +
            "Leerzeichen dürfen nicht vorkommen."

    public static final String USER_PASSWORD_REGEX =
            '^' +
            '(?=.*[A-Z])' +             // an upper case alphabet that must occur at least once
            '(?=.*[a-z])' +             // a lower case alphabet must occur at least once
            '(?=.*[0-9])' +             // a digit must occur at least once
            '(?=.*[!$%&@#^+*~=:;])' +   // a special character that must occur at least once: !$%&@#^+*~=:;
            '(?=\\S+$)' +               // white spaces are not allowed
            '.{8,20}' +                 // 8 - 20 characters
            '$'

    static final Pattern USER_PASSWORD_PATTERN = Pattern.compile(USER_PASSWORD_REGEX)

    /**
     * Collects all valid characters for a random password
     * @return an array of chars containing the ranges defined in {@link #USER_PASSWORD_REGEX}
     */
    static char[] getUserPasswordCharacters() {
        RandomUtils.DICT_PASSWORD.toCharArray()
    }

    /**
     * Creates a randomised initial user password
     * @return a random sequence of chars serving as initial or reset password
     */
    static String getRandomUserPassword() {
        String password = 'here we go'
        char[] range = getUserPasswordCharacters()

        while (! isUserPasswordValid(password)) {
            password = RandomUtils.getRandom(range, 16)
        }
        password
    }

    /**
     * Checks if the user has submitted a valid new password
     * @param password the user's input candidate
     * @return true if the input matches the defined pattern, false otherwise
     */
    static boolean isUserPasswordValid(final String password) {
        Matcher matcher = USER_PASSWORD_PATTERN.matcher(password)
        return matcher.matches()
    }

    /**
     * Test suite for password pattern matching
     */
    static void test() {

        Map<String, Boolean> checks = [
                'abcdefghijklmn'        : false,
                'ABCDEFGHIJKLMN'        : false,
                'abcdef$$$111222'       : false,
                'ABCDEF$$$111222'       : false,
                'ABCDEF$ghijklmn'       : false,
                'abcd@ABCD-123'         : true,
                'abcd@ABCD$123'         : true,
                'abcd=ABCD+123'         : true,
                'abcd@ABCD 123'         : false,
                'abcd@ABCD$1230000000'  : true,
                'abcd@ABCD$12300000000' : false,
                'abcd-ABCD-123'         : false,
                'abcd:ABCD;123'         : true,
                '!$%&@#^+*~======'      : false,
        ]

        checks.each { it ->
            println it.key + ' -> ' + isUserPasswordValid(it.key) + ' ? ' + it.value + ( isUserPasswordValid(it.key) == it.value ? ' -> passed' : ' ==========> FAILED' )
        }
    }

}
