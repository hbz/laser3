<%@page import="de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:htmlStart message="default.documents.label" />

    <%
      List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>

    <laser:render template="breadcrumb"
              model="${[vendor: vendor]}"/>

    <ui:controlButtons>
      <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="[vendor: vendor]" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${vendor.name}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor: isMyVendor]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[vendor: vendor]}"/>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[instance: vendor, context:'documents', redirect:'documents', owntp: 'vendor']}"/>
<laser:htmlEnd />
