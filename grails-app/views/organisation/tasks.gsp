<laser:htmlStart message="task.plural" />

    <ui:breadcrumbs>
        <g:if test="${!inContextOrg}">
            <ui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:if>
    </ui:breadcrumbs>
    <ui:controlButtons>
        <laser:render template="actions" model="${[org:org]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,
            myTaskInstanceList: myTaskInstanceList
    ]}"/>

<laser:htmlEnd />

