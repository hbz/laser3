<%@ page import="de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractProperty" %>
<laser:serviceInjection />
<%@ page contentType="text/html" %>
<html>
<body>
    <g:if test="${dueDates}">
        <g:message code="profile.dashboardReminderEmailText" default="In the next {0} days no dates are due!"
                   args="${[user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14), user, org]}"/>
        <br>
        <br>
        <g:each in="${dueDates}" var="dashDueDate">
            <g:set var="obj" value="${genericOIDService.resolveOID(dashDueDate.oid)}"/>

            ${dashDueDate.attribut}:&nbsp
            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${dashDueDate.date}"/>
            <g:if test="${SqlDateUtils.isToday(dashDueDate.date)}">
                &nbsp!
            </g:if>
            <g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.date)}">
                &nbsp!!
            </g:elseif>
            <br>

            <g:if test="${obj instanceof Subscription}">
                Lizenz:&nbsp${obj.name}
            </g:if>
            <g:elseif test="${obj instanceof License}">
                Vertrag:&nbsp${obj.name}
            </g:elseif>
            <g:elseif test="${obj instanceof Task}">
                Aufgabe:&nbsp${obj.title}
                &nbsp(Status: ${obj.status?.getI10n("value")})
            </g:elseif>
            <g:elseif test="${obj instanceof AbstractProperty}">
                Merkmal&nbsp-&nbsp
                <g:if test="${obj.owner instanceof Person}">
                    Person:&nbsp${obj.owner?.first_name}&nbsp${obj.owner?.last_name}
                </g:if>
                <g:elseif test="${obj.owner instanceof Subscription}">
                    Lizenz:&nbsp${obj.owner?.name}
                </g:elseif>
                <g:elseif test="${obj.owner instanceof License}">
                    Vertag:&nbsp${obj.owner?.reference}
                </g:elseif>
                <g:elseif test="${obj.owner instanceof Org}">
                    Organisation:&nbsp${obj.owner?.name}
                </g:elseif>
                <g:else>
                    ${obj.owner?.name}
                </g:else>
            </g:elseif>
            <g:else>
                Not implemented yet!
            </g:else>
            <br>
            <br>
        </g:each>
    </g:if>
    <g:else>
        <g:message code="profile.noDashboardReminderDates" default="In the next {0} days no dates are due!"
                   args="${user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}"/>
    </g:else>
</body>
</html>
