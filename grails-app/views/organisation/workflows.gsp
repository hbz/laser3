<laser:htmlStart message="workflow.plural" />

    <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[org:org]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" type="${orgInstance.getCustomerType()}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />
    <ui:messages data="${flash}" />

    <laser:render template="/templates/workflow/table" model="${[target:orgInstance, workflows:workflows, checklists:checklists]}"/>

<laser:htmlEnd />
