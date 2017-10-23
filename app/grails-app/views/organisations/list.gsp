<%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="container">

        <div class="page-header">
          <h1><g:message code="default.list.label" args="[entityName]" /></h1>
        </div>

        <div class="well">
          <g:form action="list" method="get" class="form-inline">
            ${message(code: 'org.search.contains')}: <input type="text" name="orgNameContains" value="${params.orgNameContains}"/> ${message(code: 'org.search.restrict')}
            <g:select name="orgRole" noSelection="${['':'Select One...']}" from="${RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Organisational Role'))}" value="${params.orgRole}" optionKey="id" optionValue="value"/>
            <input type="submit" value="${message(code:'default.button.search.label')}" class="btn btn-primary"/> (${orgInstanceTotal} Matches)
          </g:form>
        </div>

        <g:if test="${flash.message}">
          <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>
        
        <table class="table table-bordered table-striped">
          <thead>
            <tr>
              <g:sortableColumn property="name" title="${message(code: 'org.name.label', default: 'Name')}" />
              <g:sortableColumn property="shortcode" title="${message(code: 'org.shortcode.label', default: 'Short Code')}" />
              <g:sortableColumn property="type" title="${message(code: 'org.type.label', default: 'Type')}" />
              <g:sortableColumn property="sector" title="${message(code: 'org.sector.label', default: 'Sector')}" />
              <g:sortableColumn property="scope" title="${message(code: 'org.scope.label', default: 'Scope')}" />
              <g:sortableColumn property="membership" title="${message(code: 'org.membership.label', default: 'Membership')}" />
            </tr>
          </thead>
          <tbody>
          <g:each in="${orgInstanceList}" var="orgInstance">
            <tr>
              <td><g:link  action="show" id="${orgInstance.id}">${fieldValue(bean: orgInstance, field: "name")}</g:link></td>
              <td>${fieldValue(bean: orgInstance, field: "shortcode")}</td>
              <td>${orgInstance?.orgType?.value}</td>
              <td>${orgInstance?.sector?.value}</td>
              <td>${fieldValue(bean: orgInstance, field: "scope")}</td>
              <td>${orgInstance?.membership?.value}</td>
            </tr>
          </g:each>
          </tbody>
        </table>

        <div class="pagination">
          <bootstrap:paginate total="${orgInstanceTotal}" params="${params}" />
        </div>

    </div>
  </body>
</html>
