package de.laser.helper

import java.util.concurrent.Future

class FutureHelper {

    static boolean isRunning(Future future) {
        future && ! future.isDone()
    }

    static boolean isDone(Future future) {
        future && future.isDone()
    }

    static boolean isCancelled(Future future) {
        future && future.isCancelled()
    }

    static String getState(Future future) {
        if (future) {
            future.toString().substring(future.toString().indexOf('['))
        } else {
            null
        }
    }
}
