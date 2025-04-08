package de.laser.utils

import groovy.util.logging.Slf4j

@Slf4j
class FileUtils {

    static boolean fileCheck (String pathname) {
        boolean check = false
        try {
            File file = new File(pathname)
            check = file.exists() && file.isFile()
        }
        catch (Exception e) {}
        check
    }
}
