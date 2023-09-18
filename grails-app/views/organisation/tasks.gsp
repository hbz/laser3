<laser:htmlStart message="task.plural" serviceInjection="true"/>

    <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

    <ui:controlButtons>
        <laser:render template="actions" model="${[org:org]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${orgInstance.name}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${ypsService.getNavTemplatePath()}" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,
            myTaskInstanceList: myTaskInstanceList
    ]}"/>

<laser:htmlEnd />

