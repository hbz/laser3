<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title><g:message code="default.edit.label" args="[entityName ?: message(code:'license.label')]" /></title>
  </head>
  <body>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'license.create.label', default:'New Template License')}</h1>

        <semui:messages data="${flash}" />

        <p>${message(code:'license.create.note')}</p>

        <semui:simpleForm action="processNewTemplateLicense" method="get">
            <div class="field">
                <label>${message(code:'license.create.ref')}</label>
                <input type="text" name="reference"/>
            </div>
            <div class="field">
                <input class="ui button js-click-control" type="submit" value="${message(code:'default.button.create.label', default:'Create')}"/>
            </div>
        </semui:simpleForm>

  </body>
</html>
