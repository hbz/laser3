<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : Datenbereinigung</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Datenbereinigung" class="active"/>
</semui:breadcrumbs>

<h2 class="ui header">Lizenzen - ${subscriptions.size()}</h2>

    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'subscription']}">Lösche die nächsten 10 Lizenzen</g:link>
    <br/>
    <br/>

    <g:each in="${subscriptions}" var="sub">
        <a href="<g:createLink controller="subscription" action="show" params="${[id: sub.id]}"/>">
            <g:if test="${sub.derivedSubscriptions}">
                <span class="ui label red">${sub.id} [ ${sub.derivedSubscriptions.size()} ] </span>
            </g:if>
            <g:else>
                <span class="ui label green">${sub.id}</span>
            </g:else>
        </a>
        > ${sub} > ${sub.status.getI10n('value')}
        <br/>
    </g:each>

    <br />
    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'subscription']}">Lösche die nächsten 10 Lizenzen</g:link>

    <br />
    <br />

    <h2 class="ui header">Verträge - ${licenses.size()}</h2>

    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'license']}">Lösche die nächsten 10 Verträge</g:link>
    <br />
    <br />

    <g:each in="${licenses}" var="lic">
        <a href="<g:createLink controller="license" action="show" params="${[id: lic.id]}"/>">
            <g:if test="${lic.derivedLicenses}">
                <span class="ui label red">${lic.id} [ ${lic.derivedLicenses.size()} ] </span>
            </g:if>
            <g:else>
                <span class="ui label green">${lic.id}</span>
            </g:else>
        </a>
        > ${lic} > ${lic.status.getI10n('value')} <br/>
    </g:each>

    <br />
    <g:link class="ui button red" controller="yoda" action="dropDeletedObjects" params="${[cmd:'license']}">Lösche die nächsten 10 Verträge</g:link>
    <br />
    <br />

</body>
</html>
