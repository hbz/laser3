<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Migrate Collective Subscriptions</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb text="Migrate Collective Subscriptions" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header"><semui:headerIcon />Migrate Collective Subscriptions</h1>

    <h4>Gefunden (OrgRole=Subscriber): ${subs1.size()}</h4>

    <g:each in="${subs1}" var="${c}">
        <g:link controller="subscription" action="show" id="${c.id}">${c}</g:link> -- ${c.getCalculatedType()}<br />
    </g:each>

    <br />
    <g:link class="ui button red" controller="yoda" action="migrateCollectiveSubscriptions" params="${[cmd:'migrateOrgRoles']}">Migriere 'Subscriber' zu 'Subscription Collective'</g:link>

    <h4>Gefunden (Subscription.Type=Collective Subscription): ${subs2.size()}</h4>

    <g:each in="${subs2}" var="${c}">
        <g:link controller="subscription" action="show" id="${c.id}">${c}</g:link> -- ${c.getCalculatedType()}<br />
    </g:each>

    <br />
    <g:link class="ui button red" controller="yoda" action="migrateCollectiveSubscriptions" params="${[cmd:'migrateSubscriptionType']}">Migriere 'Collective Subscription' zu 'Local license'</g:link>


</body>
</html>
