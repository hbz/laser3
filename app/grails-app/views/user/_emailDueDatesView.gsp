<%@ page import="de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractProperty" %>
<laser:serviceInjection />
<%@ page Content-type: text/plain; charset=utf-8; %>
<g:if test="${dueDates}"><g:set var="x" value="${raw(user.username)}"/><g:set var="y" value="${raw(org.name)}"/>
----------------------------------------------------------------------------------------------------------------------------------
                          LAS:eR
----------------------------------------------------------------------------------------------------------------------------------

<g:message code="profile.dashboardReminderEmailText1" args="${ [user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14),x]}"/>
<g:message code="profile.dashboardReminderEmailText2" args="${[y]}"/>

----------------------------------------------------------------------------------------------------------------------------------
<g:each in="${dueDates}" var="dashDueDate">
    <g:set var="obj" value="${genericOIDService.resolveOID(dashDueDate.oid)}"/>
${raw(dashDueDate.attribut)}
    <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${dashDueDate.date}"/><g:if test="${SqlDateUtils.isToday(dashDueDate.date)}">   !          </g:if><g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.date)}">   !!         </g:elseif><g:else>              </g:else><g:if test="${obj instanceof Subscription}">Lizenz: ${obj.name}</g:if><g:elseif test="${obj instanceof License}">Vertrag: ${obj.name}</g:elseif><g:elseif test="${obj instanceof Task}">Aufgabe: ${obj.title}(Status: ${obj.status?.getI10n("value")})</g:elseif><g:elseif test="${obj instanceof AbstractProperty}">Merkmal-<g:if test="${obj.owner instanceof Person}">Person: ${obj.owner?.first_name}&nbsp${obj.owner?.last_name}</g:if><g:elseif test="${obj.owner instanceof Subscription}">Lizenz: ${obj.owner?.name}</g:elseif><g:elseif test="${obj.owner instanceof License}">Vertag: ${obj.owner?.reference}</g:elseif><g:elseif test="${obj.owner instanceof Org}">Organisation: ${obj.owner?.name}</g:elseif><g:else>${obj.owner?.name}</g:else></g:elseif><g:else>Not implemented yet!</g:else>
    </g:each>
</g:if>
<g:else>
----------------------------------------------------------------------------------------------------------------------------------
                          LAS:eR
----------------------------------------------------------------------------------------------------------------------------------
<g:message code="profile.noDashboardReminderDates" default="In the next {0} days no dates are due!"
               args="${user?.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}"/>
</g:else>
----------------------------------------------------------------------------------------------------------------------------------
