<laser:htmlStart message="task.plural" />

    <laser:render template="breadcrumb"
              model="${[vendor: vendor]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[vendor: vendor]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${vendor.name}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor: isMyVendor]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[cmbTaskInstanceList: cmbTaskInstanceList]}"/>

<laser:htmlEnd />

