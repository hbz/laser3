<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : Quarz Info</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Quarz Info" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />App Cronjob Info</h1>

<g:each in="${quartz}" var="groupKey, group">
    <h3 class="ui header">${groupKey}</h3>

    <table class="ui celled la-table la-table-small table">
        <thead>
            <tr>
                <th>Job</th>
                <th>Config</th>
                <th>s  m  h  DoM  M  DoW  Y</th>
                <th>Nächste Ausführung</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${group}" var="job">
                <tr>
                    <td>
                        ${job.name}
                    </td>
                    <td>
                        ${job.configFlags}
                    </td>
                    <td>
                        <code>${job.cronEx}</code>
                    </td>
                    <td>
                        ${job.nextFireTime}
                        <%--<g:formatDate format="${message(code:'default.date.format.noZ')}" date="${job.nextFireTime}" />--%>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</g:each>

</body>
</html>