<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.yoda.activityProfiler')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.activityProfiler" class="active"/>
</semui:breadcrumbs>
<br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.activityProfiler')}</h1>

    <h3 class="ui header">Aktivität</h3>

    <table class="ui celled la-table la-table-small table">
        <thead>
            <tr>
                <th>Zeitraum</th>
                <th></th>
                <th>min(user)</th>
                <th>max(user)</th>
                <th>avg(user)</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${activity}" var="aa">
                <tr>
                    <td>
                        ${(new java.text.SimpleDateFormat(message(code:'default.date.format.notime'))).format(aa[0])}
                    </td>
                    <td>
                        ${(new java.text.SimpleDateFormat(message(code:'default.date.format.onlytime'))).format(aa[0])}
                        -
                        ${(new java.text.SimpleDateFormat(message(code:'default.date.format.onlytime'))).format(aa[1])}
                    </td>
                    <td>${aa[2]}</td><%-- //min() --%>
                    <td>${aa[3]}</td><%-- //max() --%>
                    <td>${((double) aa[4]).round(2)}</td><%-- //avg() --%>
                </tr>
            </g:each>
        </tbody>
    </table>

</body>
</html>