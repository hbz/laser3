package de.laser.utils

import groovy.util.logging.Slf4j

import org.apache.commons.text.RandomStringGenerator

@Slf4j
class RandomUtils {

    static String getRandomAlphabetic(int length = 16) {
        RandomStringGenerator rsg = new RandomStringGenerator.Builder()
                .selectFrom("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray())
                .build()
        rsg.generate(length)
    }

    static getRandom( int length = 16, char[] dict) {
        RandomStringGenerator rsg = new RandomStringGenerator.Builder()
                .selectFrom(dict)
                .build()
        rsg.generate(length)
    }

    static String getRandomUCID() {
        getRandomAlphabetic(8).toUpperCase()
    }
}
