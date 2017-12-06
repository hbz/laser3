<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title><g:message code="default.edit.label" args="[entityName ?: message(code:'license.label')]" /></title>
  </head>
  <body>

        <h1 class="ui header">${message(code:'license.create.label', default:'New Template License')}</h1>

        <semui:messages data="${flash}" />

        <p>${message(code:'license.create.note')}</p>

        <semui:simpleForm action="processNewTemplateLicense" method="get" message="license.create.ref">
            <input type="text" name="reference"/>
            <input class="ui primary button" type="submit" value="${message(code:'default.button.create.label', default:'Create')}"/>
        </semui:simpleForm>

  </body>
</html>
