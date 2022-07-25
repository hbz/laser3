<%@page import="de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:htmlStart message="default.documents.label" serviceInjection="true" />

    <%
      List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>
    <ui:breadcrumbs>
      <g:if test="${!inContextOrg}">
        <ui:crumb text="${orgInstance.getDesignation()}" class="active"/>
      </g:if>
    </ui:breadcrumbs>

    <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
        <ui:controlButtons>
          <laser:render template="actions" model="[org:org]" />
        </ui:controlButtons>
    </g:if>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[inContextOrg: inContextOrg, instance:orgInstance, context:'documents', redirect:'documents', owntp: 'org']}"/>
<laser:htmlEnd />
