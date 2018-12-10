<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
    </head>
    <body>
        <g:render template="breadcrumb" model="${[ params:params ]}"/>
        <semui:controlButtons>
                <g:render template="actions" />
        </semui:controlButtons>

        <h1 class="ui left aligned icon header"><semui:headerIcon />
            <semui:xEditable owner="${subscriptionInstance}" field="name" />
            <semui:anualRings object="${subscriptionInstance}" controller="subscriptionDetails" action="notes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
        </h1>

        <g:render template="nav" />

        <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
            <g:render template="message" />
        </g:if>

        <semui:messages data="${flash}" />

        <g:render template="/templates/notes/table" model="${[instance: subscriptionInstance, redirect: 'notes']}"/>

  </body>
</html>
