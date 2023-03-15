<%@page import="de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:htmlStart message="menu.my.documents" serviceInjection="true" />

    <%
      List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>

    <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

    <g:if test="${accessService.checkPerm('ORG_PRO,ORG_CONSORTIUM_BASIC')}">
        <ui:controlButtons>
          <laser:render template="actions" model="[org:org]" />
        </ui:controlButtons>
    </g:if>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[inContextOrg: inContextOrg, instance:orgInstance, context:'documents', redirect:'documents', owntp: 'org']}"/>
<laser:htmlEnd />
