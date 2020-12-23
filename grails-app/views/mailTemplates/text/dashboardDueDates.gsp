<%@ page
        import="de.laser.UserSetting; de.laser.Person; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.helper.RDStore; de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; de.laser.*" %><laser:serviceInjection /><%@ page Content-type: text/plain; charset=utf-8; %><g:set var="userName" value="${raw(user.username)}"/><g:set var="orgName" value="${raw(org.name)}"/><g:set var="language" value="${user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value}"/><g:if test="${dueDates}">----------------------------------------------------------------------------------------------------------------------------------
                          ${message(code: 'email.subject.dueDates', locale: language)}
----------------------------------------------------------------------------------------------------------------------------------

${message(code: 'profile.dashboardReminderEmailText1', locale: language, args:[userName])}
${message(code: 'profile.dashboardReminderEmailText2', locale: language, args:[orgName])}

----------------------------------------------------------------------------------------------------------------------------------
<g:each in="${dueDates}" var="dashDueDate"><g:set var="obj" value="${genericOIDService.resolveOID(dashDueDate.dueDateObject.oid)}"/><g:if test="${obj}">
    ${raw(language == RDStore.LANGUAGE_DE ? escapeService.replaceUmlaute(dashDueDate.dueDateObject.attribute_value_de) :
            escapeService.replaceUmlaute(dashDueDate.dueDateObject.attribute_value_en))}
    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${dashDueDate.dueDateObject.date}"/><g:if test="${SqlDateUtils.isToday(dashDueDate.dueDateObject.date)}">   !          </g:if><g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.dueDateObject.date)}">   !!         </g:elseif><g:else>              </g:else><g:if test="${obj instanceof Subscription}">${message(code: 'subscription', locale: language)}: ${raw(obj.name)}</g:if><g:elseif test="${obj instanceof License}">${message(code: 'license.label', locale: language)}: ${raw(obj.name)}</g:elseif><g:elseif test="${obj instanceof Task}">${message(code:'task.label', locale: language)}: ${raw(obj.title)}</g:elseif><g:elseif test="${obj instanceof de.laser.base.AbstractPropertyWithCalculatedLastUpdated}">${message(code:'attribute', locale: language)}: <g:if test="${obj.owner instanceof Person}">${message(code: 'default.person.label', locale: language)}: ${raw(obj.owner?.first_name)} ${raw(obj.owner?.last_name)}</g:if><g:elseif test="${obj.owner instanceof Subscription}">${message(code: 'subscription', locale: language)}: ${raw(obj.owner?.name)}</g:elseif><g:elseif test="${obj.owner instanceof License}">${message(code: 'license.label', locale: language)}: ${raw(obj.owner?.reference)}</g:elseif><g:elseif test="${obj.owner instanceof Org}">${message(code: 'org.label', locale: language)}: ${raw(obj.owner?.name)}</g:elseif><g:else>${raw(obj.owner?.name)}</g:else></g:elseif><g:elseif test="${obj instanceof SurveyInfo}">${message(code: 'survey', locale: language)}: ${raw(obj.name)}</g:elseif><g:else>Not implemented yet!</g:else>
</g:if></g:each>
</g:if>
<g:else>
----------------------------------------------------------------------------------------------------------------------------------
                          ${message(code: 'email.subject.noDueDates', locale: language)}
----------------------------------------------------------------------------------------------------------------------------------

<g:message code="profile.noDashboardReminderDates" default="There are no due dates for your personal reminder periods." />

----------------------------------------------------------------------------------------------------------------------------------
</g:else>
----------------------------------------------------------------------------------------------------------------------------------
