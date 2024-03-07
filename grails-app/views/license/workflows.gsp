<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="workflow.plural" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon visibleOrgRelations="${visibleOrgRelations}">
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${license}" controller="license" action="workflows" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/workflow/table" model="${[target:license, workflows:workflows, checklists:checklists]}"/>

<laser:htmlEnd />
