<laser:htmlStart message="task.plural" />

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
    <semui:anualRings object="${subscription}" controller="subscription" action="tasks" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <laser:render template="message" />
    </g:if>

    <semui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,     taskInstanceCount: taskInstanceCount,
            myTaskInstanceList: myTaskInstanceList, myTaskInstanceCount: myTaskInstanceCount
    ]}"/>

%{--    <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList, taskInstanceCount:taskInstanceCount]}"/>--}%
%{--    <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList, taskInstanceCount:myTaskInstanceCount]}"/>--}%

<laser:htmlEnd />

