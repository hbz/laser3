<%@ page import="de.laser.storage.RDStore;" %>

<laser:htmlStart message="default.notes.label" serviceInjection="true"/>

        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
                <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleOrgRelations="${visibleOrgRelations}">
            <laser:render template="iconSubscriptionIsChild"/>
            <ui:xEditable owner="${subscription}" field="name" />
        </ui:h1HeaderWithIcon>
        <ui:anualRings object="${subscription}" controller="subscription" action="notes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


        <laser:render template="${customerTypeService.getNavTemplatePath()}" />

        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
            <laser:render template="message" />
        </g:if>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: subscription, redirect: 'notes']}"/>

<laser:htmlEnd />
