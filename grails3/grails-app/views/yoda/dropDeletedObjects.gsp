<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Datenbereinigung</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Datenbereinigung" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui header la-clear-before la-noMargin-top">Lizenzen - ${subscriptions.size()}, Verträge - ${licenses.size()}</h1>

    <h2 class="ui header">Lizenzen - ${subscriptions.size()}</h2>

    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'subscription']}">Lösche die nächsten 10 Lizenzen</g:link>
    <br />
    <br />

    <g:each in="${subscriptions}" var="sub">
        <a href="<g:createLink controller="subscription" action="delete" params="${[id: sub.id]}"/>" target="_blank">
            <g:if test="${sub.derivedSubscriptions}">
                <span class="ui label red">${sub.id} [ ${sub.derivedSubscriptions.size()} ] </span>
            </g:if>
            <g:else>
                <span class="ui label green">${sub.id}</span>
            </g:else>
        </a>
        > ${sub} > ${sub.status.getI10n('value')}
        <br />
    </g:each>

    <br />
    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'subscription', option:'reverse']}">Lösche die letzten 10 Lizenzen</g:link>

    <br />
    <br />

    <h2 class="ui header">Verträge - ${licenses.size()}</h2>

    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'license']}">Lösche die nächsten 10 Verträge</g:link>
    <br />
    <br />

    <g:each in="${licenses}" var="lic">
        <a href="<g:createLink controller="license" action="delete" params="${[id: lic.id]}"/>" target="_blank">
            <g:if test="${lic.derivedLicenses}">
                <span class="ui label red">${lic.id} [ ${lic.derivedLicenses.size()} ] </span>
            </g:if>
            <g:else>
                <span class="ui label green">${lic.id}</span>
            </g:else>
        </a>
        > ${lic} <br />
    </g:each>

    <br />
    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'license', option:'reverse']}">Lösche die letzten 10 Verträge</g:link>
    <br />
    <br />

</body>
</html>
