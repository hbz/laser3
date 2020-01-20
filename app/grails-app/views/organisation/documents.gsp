<%@page import="com.k_int.kbplus.*, de.laser.helper.RDConstants" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
  <laser:serviceInjection />

    <%
      List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License', RDConstants.DOCUMENT_TYPE),
                           RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE),
                           RefdataValue.getByValueAndCategory('Announcement', RDConstants.DOCUMENT_TYPE)]
      List documentTypes = RefdataCategory.getAllRefdataValues(RDConstants.DOCUMENT_TYPE)-notAvailable
    %>
    <semui:breadcrumbs>
      <g:if test="${!inContextOrg}">
        <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
      </g:if>
    </semui:breadcrumbs>

    <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
        <semui:controlButtons>
          <g:render template="actions" model="[org:org]" />
        </semui:controlButtons>
    </g:if>

  <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${orgInstance.name}</h1>

    <g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

    <semui:messages data="${flash}" />

    <g:render template="/templates/documents/table" model="${[inContextOrg: inContextOrg, instance:orgInstance, context:'documents', redirect:'documents', owntp: 'org']}"/>
  </body>
</html>
