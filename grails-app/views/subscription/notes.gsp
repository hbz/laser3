<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'default.notes.label')}</title>
    </head>
    <body>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <semui:controlButtons>
                <laser:render template="actions" />
        </semui:controlButtons>

        <semui:h1HeaderWithIcon>
            <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
                <laser:render template="iconSubscriptionIsChild"/>
            </g:if>
            <semui:xEditable owner="${subscription}" field="name" />
        </semui:h1HeaderWithIcon>
        <semui:anualRings object="${subscription}" controller="subscription" action="notes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


        <laser:render template="nav" />

        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
            <laser:render template="message" />
        </g:if>

        <semui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: subscription, redirect: 'notes']}"/>

  </body>
</html>
