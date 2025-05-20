<laser:htmlStart message="task.plural" />

    <laser:render template="breadcrumb"
              model="${[provider: provider]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[provider: provider]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${provider.name}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyProvider: isMyProvider]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[cmbTaskInstanceList: cmbTaskInstanceList]}"/>

<laser:htmlEnd />

