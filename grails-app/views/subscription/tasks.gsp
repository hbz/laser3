<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="task.plural" serviceInjection="true"/>

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>
    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleOrgRelations="${visibleOrgRelations}">
        <laser:render template="iconSubscriptionIsChild"/>
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="tasks" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <laser:render template="message" />
    </g:if>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,
            myTaskInstanceList: myTaskInstanceList
    ]}"/>

<laser:htmlEnd />

