<%@ page import="de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractProperty" %>
<laser:serviceInjection />
<%@ page contentType="text/html" %>
<html>
<body>
    <g:if test="${dueDates}">
        <g:message code="profile.dashboardReminderEmailText" default="In the next {0} days no dates are due!"
                   args="${[user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14), user, org]}"/>
        <hr>
        <g:render template="/user/dueDatesView"
                  model="[user: contextService.user,
                          dueDates: dueDates]"/>
    </g:if>
    <g:else>
        <g:message code="profile.noDashboardReminderDates" default="In the next {0} days no dates are due!"
                   args="${user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}"/>
    </g:else>
</body>
</html>
