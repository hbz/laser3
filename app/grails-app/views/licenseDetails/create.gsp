<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title><g:message code="default.edit.label" args="[entityName ?: message(code:'license.label')]" /></title>
  </head>
  <body>
      <div>

            <h1 class="ui header">${message(code:'license.create.label', default:'New Template License')}</h1>

            <semui:messages data="${flash}" />

            <p>${message(code:'license.create.note')}</p>

            <p>
              <g:form action="processNewTemplateLicense"> ${message(code:'license.create.ref', default:'New license Reference')}: <input type="text" name="reference"/>
              <br/><input class="ui primary button" type="submit" value="${message(code:'default.button.create.label', default:'Create')}"/></g:form>
            </p>

      </div>

  </body>
</html>
