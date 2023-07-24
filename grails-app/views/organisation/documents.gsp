<%@page import="de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:htmlStart message="default.documents.label" serviceInjection="true" />

    <%
      List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>

    <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

    <ui:controlButtons>
      <laser:render template="actions" model="[org:org]" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${orgInstance.name}">
        <g:if test="${isMyOrg}">
            <laser:render template="/templates/iconObjectIsMine"/>
        </g:if>
    </ui:h1HeaderWithIcon>

    <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[inContextOrg: inContextOrg, instance:orgInstance, context:'documents', redirect:'documents', owntp: 'org']}"/>
<laser:htmlEnd />
