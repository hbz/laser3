<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="workflow.plural" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <g:set var="visibleOrgRelationsJoin" value="${visibleOrgRelations.findAll{it.roleType != RDStore.OR_SUBSCRIPTION_CONSORTIA}.sort{it.org.sortname}.collect{it.org}.join(' – ')}"/>
    <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" visibleOrgRelationsJoin="${visibleOrgRelationsJoin}">
        <laser:render template="iconSubscriptionIsChild"/>
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="workflows" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />
    <laser:render template="message"/>

    <laser:render template="/templates/workflow/table" model="${[target:subscription, workflows:workflows, checklists:checklists]}"/>

<laser:htmlEnd />
