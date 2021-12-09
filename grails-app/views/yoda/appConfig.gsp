<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.appConfig')} </title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.appConfig" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">${message(code:'menu.yoda.appConfig')}</h1>

%{--<h2 class="ui header">${message(code:'sys.properties')}</h2>--}%

<table class="ui sortable celled la-table compact table">
    <thead>
    <tr>
        <th></th>
        <th>Property</th>
        <th>Value</th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${currentconf.keySet().sort()}" var="key" status="i">
            <tr>
                <td>${i+1}.</td>
                <td>${key}</td>
                <td>
                    <g:if test="${blacklist.contains(key)}">
                        <span style="color:orange"> == C O N C E A L E D === </span>
                    </g:if>
                    <g:else>
                        ${currentconf.get(key)}
                    </g:else>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

</body>
</html>
