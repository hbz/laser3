<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.systemConfiguration')} </title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.systemConfiguration" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">${message(code:'menu.yoda.systemConfiguration')}</h1>

<%
    Set<String> dubs = []
    Set<String> sortedKeySet = currentConfig.keySet().sort()

    sortedKeySet.each { it1 ->
        sortedKeySet.each { it2 ->
            if (it2.startsWith(it1 + '.')) {
                dubs.add(it1)
                return
            }
        }
    }
    Set<String> keySet = sortedKeySet - dubs

    Map<String, String> colors = [
            dataSource: '#8b008b',
            grails: '#ffa500',
            grailsPlugin: '#dc143c',
            java: '#1e90ff',
            spring: '#228b22',
    ]
%>

<span class="ui label"><i class="icon certificate" style="color:${colors['dataSource']}"></i>dataSource</span>
<span class="ui label"><i class="icon certificate" style="color:${colors['grailsPlugin']}"></i>grails.plugin(s)</span>
<span class="ui label"><i class="icon certificate" style="color:${colors['grails']}"></i>grails</span>
<span class="ui label"><i class="icon certificate" style="color:${colors['java']}"></i>java</span>
<span class="ui label"><i class="icon certificate" style="color:${colors['spring']}"></i>spring</span>

<table class="ui sortable celled la-js-responsive-table la-hover-table la-table compact table">
    <thead>
    <tr>
        <th></th>
        <th></th>
        <th></th>
        <th></th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${keySet}" var="key" status="i">
            <%
                String color = ''
                if (key.startsWith('grails.plugin'))        { color = colors['grailsPlugin'] }
                else if (key.startsWith('grails'))          { color = colors['grails'] }
                else if (key.startsWith('dataSource'))      { color = colors['dataSource'] }
                else if (key.startsWith('java'))            { color = colors['java'] }
                else if (key.startsWith('spring'))          { color = colors['spring'] }

                if (color) { color = 'color:' + color }
            %>
            <tr>
                <td>${i+1}</td>
                <td>${key}</td>
                <td>
                    <g:if test="${blacklist.contains(key)}">
                        <span style="color:orange"> == C O N C E A L E D === </span>
                    </g:if>
                    <g:else>
                        ${currentConfig.get(key)}
                    </g:else>
                </td>
                <td>
                    <g:if test="${color}">
                        <i class="icon certificate" style="${color}"></i>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

</body>
</html>
