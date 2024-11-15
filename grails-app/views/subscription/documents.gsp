<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="default.documents.label" />

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>
    <ui:controlButtons>
      <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
        <laser:render template="iconSubscriptionIsChild"/>
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="documents" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortium()?.id}">
      <laser:render template="message" />
    </g:if>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[instance:subscription, context:'documents', redirect:'documents', owntp: 'subscription']}"/>

<laser:htmlEnd />
