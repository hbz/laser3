<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Event Log</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Event Log" class="active"/>
</semui:breadcrumbs>

<div>
    <table class="ui sortable celled la-table table">
        <thead>
        <tr>
            <g:sortableColumn property="event" title="Event"/>
            <g:sortableColumn property="message" title="Message"/>
            <g:sortableColumn property="tstp" title="Date"/>
        </tr>
        </thead>
        <g:each in="${eventlogs}" var="el">
            <tr>
                <td>
                    ${el.event}
                </td>
                <td>
                    ${el.message}
                </td>
                <td>
                    ${el.tstp}
                </td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>
