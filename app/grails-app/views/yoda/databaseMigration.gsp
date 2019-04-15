<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Database Migration</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
      <semui:crumb text="Database Migration" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <h3 class="ui header">${candidates.size()} Kandidaten; keine Datenbankänderungen!</h3>

    <g:each in="${candidates}" var="spp">
        <p>
        SubscriptionPrivateProperty: ${spp.id} ${spp} <br />
        PropertyDefinition: ${spp.type.id} ${spp.type.description} ${spp.type.name} <br />
        PropertyDefinition.tenant: ${spp.type.tenant.id} ${spp.type.tenant} <br />
        Subscription: ${spp.owner.id} <g:link controller="subscriptionDetails" action="show" id="${spp.owner.id}">${spp.owner}</g:link> <br />
        </p>
    </g:each>

  </body>
</html>
