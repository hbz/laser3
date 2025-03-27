package de.laser.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.text.RandomStringGenerator

@Slf4j
@CompileStatic
class RandomUtils {

    public static final String DICT_ALPHABETIC      = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
    public static final String DICT_ALPHANUMERIC    = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    public static final String DICT_PASSWORD        = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!$%&@#^+*~=:;'

    static String getAlphabetic(int length = 16) {
        getRandom(DICT_ALPHABETIC.toCharArray(), length)
    }

    static String getAlphaNumeric(int length = 16) {
        getRandom(DICT_ALPHANUMERIC.toCharArray(), length)
    }

    static String getRandom(char[] dict, int length = 16) {
        RandomStringGenerator rsg = new RandomStringGenerator.Builder()
                .selectFrom(dict)
                .build()
        rsg.generate(length)
    }

    static String getUUID() {
        java.util.UUID.randomUUID().toString()
    }

    static String getHtmlID() {
        (getAlphabetic(1) + getRandom(DICT_ALPHANUMERIC.drop(26).toCharArray(), 7)).toUpperCase()
    }
}
