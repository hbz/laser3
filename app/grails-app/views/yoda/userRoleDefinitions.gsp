<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.yoda.userRoleDefinitions')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.userRoleDefinitions" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.yoda.userRoleDefinitions')}</h1>

<br />

<table class="ui table la-table">
    <thead>
        <tr>
            <th>Role</th>
            <th></th>
            <th width="70%"></th>
        </tr>
    </thead>
    <tbody>

        <tr>
            <td>INST_USER</td>
            <td>org (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>INST_EDITOR</td>
            <td>org (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>INST_ADM</td>
            <td>org (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_USER</td>
            <td>global (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_DATAMANAGER</td>
            <td>global (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_ADMIN</td>
            <td>global (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_YODA</td>
            <td>global (h)</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_GLOBAL_DATA</td>
            <td>global</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_ORG_EDITOR</td>
            <td>global</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_PACKAGE_EDITOR</td>
            <td>global</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_STATISTICS_EDITOR</td>
            <td>global</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_TICKET_EDITOR</td>
            <td>global</td>
            <td>
            </td>
        </tr>

        <tr>
            <td>ROLE_API</td>
            <td>global</td>
            <td>
            </td>
        </tr>

    </tbody>
</table>

</body>
</html>