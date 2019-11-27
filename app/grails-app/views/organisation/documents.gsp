<%@page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
  <laser:serviceInjection />

    <%
      List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License','Document Type'),
                           RefdataValue.getByValueAndCategory('Note','Document Type'),
                           RefdataValue.getByValueAndCategory('Announcement','Document Type')]
      List documentTypes = RefdataCategory.getAllRefdataValues("Document Type")-notAvailable
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

  <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${orgInstance.name}</h1>

    <g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

    <semui:messages data="${flash}" />

    <g:render template="/templates/documents/table" model="${[inContextOrg: inContextOrg, instance:orgInstance, context:'documents', redirect:'documents', owntp: 'org']}"/>
  </body>
</html>
