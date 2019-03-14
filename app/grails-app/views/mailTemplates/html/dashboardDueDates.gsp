<%@ page import="de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractProperty; com.k_int.kbplus.UserSettings" %><laser:serviceInjection /><%@ page Content-type: text/plain; charset=utf-8; %><g:set var="userName" value="${raw(user.username)}"/><g:set var="orgName" value="${raw(org.name)}"/><g:set var="language" value="${user.getSetting(UserSettings.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de','Language')).value}"/><g:if test="${dueDates}">----------------------------------------------------------------------------------------------------------------------------------
                          ${message(code: 'email.subject.dueDates', locale: language)}
----------------------------------------------------------------------------------------------------------------------------------

${message(code: 'profile.dashboardReminderEmailText1', locale: language, args:[user?.getSettingsValue(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14),userName])}
${message(code: 'profile.dashboardReminderEmailText2', locale: language, args:[orgName])}

----------------------------------------------------------------------------------------------------------------------------------
<g:each in="${dueDates}" var="dashDueDate">
    <g:set var="obj" value="${genericOIDService.resolveOID(dashDueDate.oid)}"/>
    ${raw(dashDueDate.attribut)}
    <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${dashDueDate.date}"/><g:if test="${SqlDateUtils.isToday(dashDueDate.date)}">   !          </g:if><g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.date)}">   !!         </g:elseif><g:else>              </g:else><g:if test="${obj instanceof Subscription}">${message(code: 'subscription', locale: language)}: ${raw(obj.name)}</g:if><g:elseif test="${obj instanceof License}">${message(code: 'license', locale: language)}: ${raw(obj.name)}</g:elseif><g:elseif test="${obj instanceof Task}">${message(code:'task.label', locale: language)}: ${raw(obj.title)} (Status: ${raw(obj.status?.getI10n("value"))})</g:elseif><g:elseif test="${obj instanceof AbstractProperty}">${message(code:'attribute', locale: language)}: <g:if test="${obj.owner instanceof Person}">${message(code: 'default.person.label', locale: language)}: ${raw(obj.owner?.first_name)} ${raw(obj.owner?.last_name)}</g:if><g:elseif test="${obj.owner instanceof Subscription}">${message(code: 'subscription', locale: language)}: ${raw(obj.owner?.name)}</g:elseif><g:elseif test="${obj.owner instanceof License}">${message(code: 'license', locale: language)}: ${raw(obj.owner?.reference)}</g:elseif><g:elseif test="${obj.owner instanceof Org}">${message(code: 'org.label', locale: language)}: ${raw(obj.owner?.name)}</g:elseif><g:else>${raw(obj.owner?.name)}</g:else></g:elseif><g:else>Not implemented yet!</g:else>
</g:each>
</g:if>
<g:else>
----------------------------------------------------------------------------------------------------------------------------------
                          ${message(code: 'email.subject.noDueDates', locale: language)}
----------------------------------------------------------------------------------------------------------------------------------

<g:message code="profile.noDashboardReminderDates" default="In the next {0} days no dates are due!" args="${user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}"/>

----------------------------------------------------------------------------------------------------------------------------------
</g:else>
----------------------------------------------------------------------------------------------------------------------------------
