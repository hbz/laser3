<%@page import="de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:htmlStart message="default.documents.label" />

    <%
      List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>

    <laser:render template="breadcrumb"
              model="${[provider: provider]}"/>

    <ui:controlButtons>
      <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="[provider: provider]" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${provider.name}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyProvider: isMyProvider]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[provider: provider]}"/>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[instance: provider, context:'documents', redirect:'documents', owntp: 'provider']}"/>
<laser:htmlEnd />
