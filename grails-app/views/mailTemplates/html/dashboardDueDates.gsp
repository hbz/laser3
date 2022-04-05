<%@ page import="de.laser.UserSetting; de.laser.Person; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.storage.RDStore; com.k_int.kbplus.*; de.laser.*" %>
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
                    <g:each in="${dueDates}" var="dashDueDateRow">
                        <tr>
                            <td>
                                ${dashDueDateRow.valueDate}
                            </td>
                            <td>
                                ${dashDueDateRow.importance}
                            </td>
                            <td>
                                ${dashDueDateRow.classLabel}<g:if test="${dashDueDateRow.link}">: <a href="${dashDueDateRow.link}">${dashDueDateRow.objLabel}</a></g:if>
                            </td>
                        </tr>
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
