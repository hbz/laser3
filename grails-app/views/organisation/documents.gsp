<%@page import="de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
  <laser:serviceInjection />

    <%
      List notAvailable = [ RDStore.DOC_TYPE_ONIXPL, RDStore.DOC_TYPE_NOTE, RDStore.DOC_TYPE_ANNOUNCEMENT ]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>
    <semui:breadcrumbs>
      <g:if test="${!inContextOrg}">
        <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
      </g:if>
    </semui:breadcrumbs>

    <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
        <semui:controlButtons>
          <laser:render template="actions" model="[org:org]" />
        </semui:controlButtons>
    </g:if>

    <semui:headerWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

    <semui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[inContextOrg: inContextOrg, instance:orgInstance, context:'documents', redirect:'documents', owntp: 'org']}"/>
  </body>
</html>
