<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="workflow.plural" />

    <laser:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
        <laser:render template="iconSubscriptionIsChild"/>
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="workflows" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <laser:render template="message"/>

    <laser:render template="/templates/workflow/table" model="${[target:subscription, workflows:workflows, checklists:checklists]}"/>

<laser:htmlEnd />
