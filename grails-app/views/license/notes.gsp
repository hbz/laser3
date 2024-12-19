<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="license.nav.notes" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon visibleProviders="${visibleProviders}">
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>

    <ui:anualRings object="${license}" controller="license" action="notes" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <laser:render template="/templates/notes/table" model="${[instance: license, redirect: 'notes']}"/>

<laser:htmlEnd />
