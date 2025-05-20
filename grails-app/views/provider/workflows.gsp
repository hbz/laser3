<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="workflow.plural" />

    <laser:render template="breadcrumb" model="${[provider:provider, params:params]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[provider:provider, user:user]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${provider.name}" >
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyProvider:isMyProvider]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <laser:render template="/templates/workflow/table" model="${[target:provider, workflows:workflows, checklists:checklists]}"/>

<laser:htmlEnd />