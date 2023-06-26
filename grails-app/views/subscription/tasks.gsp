<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="task.plural" />

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>
    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <g:set var="visibleOrgRelationsJoin" value="${visibleOrgRelations.findAll{it.roleType != RDStore.OR_SUBSCRIPTION_CONSORTIA}.sort{it.org.sortname}.collect{it.org}.join(' – ')}"/>
    <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" visibleOrgRelationsJoin="${visibleOrgRelationsJoin}">
        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
            <laser:render template="iconSubscriptionIsChild"/>
        </g:if>
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="tasks" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <laser:render template="message" />
    </g:if>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,
            myTaskInstanceList: myTaskInstanceList
    ]}"/>

<laser:htmlEnd />

