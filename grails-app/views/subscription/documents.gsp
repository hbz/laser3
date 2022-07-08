<laser:htmlStart message="default.documents.label" />

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>
    <semui:controlButtons>
      <laser:render template="actions" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

      <semui:h1HeaderWithIcon>
        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
          <laser:render template="iconSubscriptionIsChild"/>
        </g:if>
        <semui:xEditable owner="${subscription}" field="name" />
      </semui:h1HeaderWithIcon>
      <semui:anualRings object="${subscription}" controller="subscription" action="documents" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
      <laser:render template="message" />
    </g:if>

    <semui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[instance:subscription, context:'documents', redirect:'documents', owntp: 'subscription']}"/>

<laser:htmlEnd />
