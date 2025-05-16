package de.laser

import de.laser.storage.RDStore

class DashboardService {

    ContextService contextService

    boolean showTopMenu() {
        return contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_TOPMENU, RDStore.YN_YES).value == RDStore.YN_YES
    }

    boolean showCharts() {
        return contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_CHARTS, RDStore.YN_YES).value == RDStore.YN_YES
    }

    boolean showCurrentTestSubscriptions() {
        return contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_CURRENT_TEST_SUBSCRIPTIONS, RDStore.YN_YES).value == RDStore.YN_YES
    }

    boolean showWekbNews() {
        return contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_WEKBNEWS, RDStore.YN_YES).value == RDStore.YN_YES
    }
}

