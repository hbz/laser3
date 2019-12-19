<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.activityProfiler')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.activityProfiler" class="active"/>
</semui:breadcrumbs>
<br>
    <h2 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.activityProfiler')}</h2>

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
            <g:each in="${activity}" var="item">
                <tr>
                    <td>
                        ${item.key}
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${slot[1]} - ${slot[2]}</span> <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${slot[3]}</span> <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${slot[4]}</span> <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${((double) slot[5]).round(2)}</span> <br/>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

</body>
</html>