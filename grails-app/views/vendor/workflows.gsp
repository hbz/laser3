<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="workflow.plural" />

    <laser:render template="breadcrumb" model="${[vendor:vendor, params:params]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[vendor:vendor, user:user]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${vendor.name}" >
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor:isMyVendor]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <laser:render template="/templates/workflow/table" model="${[target:vendor, workflows:workflows, checklists:checklists]}"/>

<laser:htmlEnd />
