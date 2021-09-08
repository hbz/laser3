<%@ page import="de.laser.UserSetting; de.laser.Person; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.helper.RDStore; de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; de.laser.*" %>
<laser:serviceInjection />
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <style>
        table{border-collapse:collapse;border-spacing:0;}
        td{padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}
        th{;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}
        </style>
    </head>
    <body>
        <g:set var="userName" value="${raw(user.username)}"/>
        <g:set var="orgName" value="${raw(org.name)}"/>
        <g:set var="language" value="${user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value}"/>
        <table>
            <g:if test="${dueDates}">
                <thead>
                    <tr>
                        <th colspan="3">${message(code: 'email.subject.dueDates', locale: language)}</th>
                    </tr>
                    <tr>
                        <th colspan="3">${message(code: 'profile.dashboardReminderEmailText1', locale: language, args:[userName])} ${message(code: 'profile.dashboardReminderEmailText2', locale: language, args:[orgName])}</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${dueDates}" var="dashDueDate">
                        <g:set var="obj" value="${genericOIDService.resolveOID(dashDueDate.dueDateObject.oid)}"/>
                        <g:if test="${obj}">
                            <tr>
                                <td>
                                    <g:if test="${language == RDStore.LANGUAGE_DE}">
                                        ${escapeService.replaceUmlaute(dashDueDate.dueDateObject.attribute_value_de)}
                                    </g:if>
                                    <g:else>
                                        ${escapeService.replaceUmlaute(dashDueDate.dueDateObject.attribute_value_en)}
                                    </g:else>
                                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${dashDueDate.dueDateObject.date}"/>
                                </td>
                                <td>
                                    <g:if test="${SqlDateUtils.isToday(dashDueDate.dueDateObject.date)}">
                                        !
                                    </g:if>
                                    <g:elseif test="${SqlDateUtils.isBeforeToday(dashDueDate.dueDateObject.date)}">
                                        !!
                                    </g:elseif>
                                    <g:else>

                                    </g:else>
                                </td>
                                <td>
                                    <g:if test="${obj instanceof Subscription}">${message(code: 'subscription', locale: language)}: ${raw(link([controller: "subscription", action: "show", id: obj.id], obj.name))}</g:if>
                                    <g:elseif test="${obj instanceof License}">${message(code: 'license.label', locale: language)}: ${raw(link([controller: "license", action: "show", id: obj.id], obj.reference))}</g:elseif>
                                    <g:elseif test="${obj instanceof Task}">${message(code:'task.label', locale: language)}: ${raw(link(obj.getDisplayArgs(), obj.title))}</g:elseif>
                                    <g:elseif test="${obj instanceof de.laser.base.AbstractPropertyWithCalculatedLastUpdated}">${message(code:'attribute', locale: language)}:
                                        <g:if test="${obj.owner instanceof Person}">${message(code: 'default.person.label', locale: language)}: ${raw(link([controller: "person", action: "show", id: obj.owner?.id], obj.owner?.first_name+' '+obj.owner?.last_name))}</g:if>
                                        <g:elseif test="${obj.owner instanceof Subscription}">${message(code: 'subscription', locale: language)}: ${raw(link([controller: "subscription", action: "show", id: obj.owner?.id], obj.owner?.name))}</></g:elseif>
                                        <g:elseif test="${obj.owner instanceof License}">${message(code: 'license.label', locale: language)}: ${raw(link([controller: "license", action: "show", id: obj.owner?.id], obj.owner?.reference))}></g:elseif>
                                        <g:elseif test="${obj.owner instanceof Org}">${message(code: 'org.label', locale: language)}: ${raw(link([controller: "organisation", action: "show", id: obj.owner?.id], obj.owner?.name))}</g:elseif>
                                        <g:else>${raw(obj.owner?.name)}</g:else>
                                    </g:elseif>
                                    <g:elseif test="${obj instanceof SurveyInfo}">${message(code: 'survey', locale: language)}: ${raw(link([controller: "survey", action: "show", id: obj.id], obj.name))}</g:elseif>
                                    <g:else>Not implemented yet!</g:else>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                </tbody>
            </g:if>
            <g:else>
                <thead>
                    <tr>
                        <th>${message(code: 'email.subject.noDueDates', locale: language)}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><g:message code="profile.noDashboardReminderDates" default="There are no due dates for your personal reminder periods." /></td>
                    </tr>
                </tbody>
            </g:else>
        </table>
    </body>
</html>
