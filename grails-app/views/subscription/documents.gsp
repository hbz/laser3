<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="default.documents.label" />

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>
    <ui:controlButtons>
      <laser:render template="actions" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" visibleOrgRelations="${visibleOrgRelations}">
    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
      <laser:render template="iconSubscriptionIsChild"/>
    </g:if>
    <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="documents" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
      <laser:render template="message" />
    </g:if>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[instance:subscription, context:'documents', redirect:'documents', owntp: 'subscription']}"/>

<laser:htmlEnd />
