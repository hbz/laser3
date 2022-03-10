<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.serverDifferences')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb message="menu.admin.serverDifferences" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="menu.admin.serverDifferences"/></h1>

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>Jira</th>
            <th>${message(code:'default.description.label')}</th>
            <th>DEV</th>
            <th>QA</th>
            <th>PROD</th>
            <th>${message(code:'default.date.label')}</th>
        </tr>
        </thead>
        <tbody>
            <!--
            <tr>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
            -->
            <tr>
                <td>????</td>
                <td>Startseite: Barrierefreiheit -> Link: Feedback</td>
                <td>ausgeblendet</td>
                <td>ausgeblendet</td>
                <td>sichtbar</td>
                <td>24.09.2019</td>
            </tr>
        </tbody>
    </table>

</body>
</html>
