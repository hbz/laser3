package de.laser.ui

import de.laser.RefdataValue
import de.laser.annotations.UnstableFeature
import de.laser.storage.RDStore

@UnstableFeature
class EChart {

    static Map<String, String> getColors() {
        // JSPC.colors.palette = []
        // JSPC.colors.hex = [:]

        [
            blue:       '#5470c6',
            green:      '#91cc75',
            yellow:     '#fac858',
            red:        '#ee6666',
            ice:        '#73c0de',
            darkgreen:  '#3ba272',
            orange:     '#fc8452',
            purple:     '#9a60b4',
            pink:       '#ea7ccc',
            grey:       '#d4d4d4'
        ]
    }

    static String getJspcColorBySubscriptionStatus(RefdataValue status) {
        String color = 'JSPC.colors.hex.grey'

        switch (status) {
            case RDStore.SUBSCRIPTION_CURRENT:                      color = 'JSPC.colors.hex.green';    break
            case RDStore.SUBSCRIPTION_EXPIRED:                      color = 'JSPC.colors.hex.blue';     break
            case RDStore.SUBSCRIPTION_INTENDED:                     color = 'JSPC.colors.hex.yellow';   break
            case RDStore.SUBSCRIPTION_ORDERED:                      color = 'JSPC.colors.hex.ice';      break
            case RDStore.SUBSCRIPTION_TEST_ACCESS:                  color = 'JSPC.colors.hex.orange';   break
            case RDStore.SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION:   color = 'JSPC.colors.hex.purple';   break
            case RDStore.SUBSCRIPTION_NO_STATUS:                    color = 'JSPC.colors.hex.red';      break
        }
        color
    }

    static String getJspcColorByLicenseStatus(RefdataValue status) {
        String color = 'JSPC.colors.hex.grey'

        switch (status) {
            case RDStore.LICENSE_CURRENT:      color = 'JSPC.colors.hex.green';     break
            case RDStore.LICENSE_EXPIRED:      color = 'JSPC.colors.hex.blue';      break
            case RDStore.LICENSE_INTENDED:     color = 'JSPC.colors.hex.yellow';    break
            case RDStore.LICENSE_NO_STATUS:    color = 'JSPC.colors.hex.red';       break
        }
        color
    }

    static String getJspcColorBySurveyVirtualStatus(String status) {
        String color = 'JSPC.colors.hex.grey'

        switch (status) {
            case 'open':        color = 'JSPC.colors.hex.green';    break
            case 'finish':      color = 'JSPC.colors.hex.blue';     break
            case 'termination': color = 'JSPC.colors.hex.red';      break
            case 'notFinish':   color = 'JSPC.colors.hex.yellow';   break
        }
        color
    }

    static String getHexColorBySurveyType(RefdataValue status) {
        String color = '#d4d4d4' // JSPC.colors.hex.grey

        switch (status) {
            case RDStore.SURVEY_TYPE_INTEREST:          color = '#ff9688';  break
            case RDStore.SURVEY_TYPE_RENEWAL:           color = '#ebff82';  break
            case RDStore.SURVEY_TYPE_SUBSCRIPTION:      color = '#fee8d2';  break
            case RDStore.SURVEY_TYPE_TITLE_SELECTION:   color = '#45b2ff';  break
        }
        color
    }
}
