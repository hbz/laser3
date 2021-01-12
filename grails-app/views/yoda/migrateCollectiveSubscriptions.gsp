<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Migrate Collective Subscriptions</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb text="Migrate Collective Subscriptions" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Migrate Collective Subscriptions</h1>

    <h2 class="ui header">Gefunden (OrgRole=Subscriber): ${subRoles.size()}</h2>

    <table class="ui table">
        <thead>
        <tr>
            <th>Lizenz</th>
            <th>Typ</th>
            <th>Org</th>
        </tr>
        </thead>
    <g:each in="${subRoles}" var="${c}">
        <tr>
            <td><g:link controller="subscription" action="show" id="${c[0].id}">${c[0]}</g:link></td>
            <td>${c[0]._getCalculatedType()}</td>
            <td><g:link controller="organisation" action="show" id="${c[1].org.id}">${c[1].org}</g:link></td>
        </tr>
    </g:each>
    </table>

    <h2 class="ui header">Gefunden (OrgRole=Subscriber_Consortial): ${subConsRoles.size()}</h2>

    <table class="ui table">
        <thead>
        <tr>
            <th>Lizenz</th>
            <th>Typ</th>
            <th>Org</th>
        </tr>
        </thead>
        <g:each in="${subConsRoles}" var="${c}">
            <tr>
                <td><g:link controller="subscription" action="show" id="${c[0].id}">${c[0]}</g:link></td>
                <td>${c[0]._getCalculatedType()}</td>
                <td><g:link controller="organisation" action="show" id="${c[1].org.id}">${c[1].org}</g:link></td>
            </tr>
        </g:each>
    </table>

    <br />
    <g:link class="ui button red" controller="yoda" action="migrateCollectiveSubscriptions" params="${[cmd:'migrate']}">Migriere Subscriber <%-- todo: IGNORED for 0.20 /Subscriber_Consortial --%> zu 'Subscription Collective'</g:link>

</body>
</html>
