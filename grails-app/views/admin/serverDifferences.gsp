<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code:'menu.admin.serverDifferences')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.serverDifferences" class="active"/>
</semui:breadcrumbs>

<br />
<br />
<br />

<div>
    <table class="ui celled la-table table">
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
</div>
</body>
</html>
