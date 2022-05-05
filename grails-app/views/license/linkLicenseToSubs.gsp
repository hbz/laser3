<%@ page import="de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:serviceInjection />
<!doctype html>

<html>
    <head>
        <meta name="layout" content="laser" />
        <title>${message(code:'laser')} : ${message(code:'myinst.currentSubscriptions.label')}</title>
    </head>
    <body>

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:messages data="${flash}"/>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
        <semui:totalNumber total="${num_sub_rows}"/>
    </h1>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <laser:render template="/templates/subscription/subscriptionFilter"/>

    <div class="ui buttons">
        <g:link action="linkToSubscription" class="ui button positive" params="${params+[id:license.id,subscription:"all"]}"><g:message code="license.linkAll"/></g:link>
        <div class="or" data-text="${message(code:'default.or')}"></div>
        <g:link action="linkToSubscription" class="ui button negative" params="${params+[id:license.id,unlink:true,subscription:"all"]}"><g:message code="license.unlinkAll"/></g:link>
    </div>
    <laser:render template="/templates/subscription/subscriptionTable"/>

    <semui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>

  </body>
</html>
