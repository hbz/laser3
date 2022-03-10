<%@ page import="de.laser.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName ?: message(code:'license.label')]" /></title>
  </head>
  <body>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'license.create.label')}</h1>

        <semui:messages data="${flash}" />

        <p>${message(code:'license.create.note')}</p>

    <%--
        <semui:simpleForm action="processNewTemplateLicense" method="get">
            <div class="field">
                <label>${message(code:'license.create.ref')}</label>
                <input type="text" name="reference"/>
            </div>
            <div class="field">
                <input class="ui button js-click-control" type="submit" value="${message(code:'default.button.create.label')}"/>
            </div>
        </semui:simpleForm>
    --%>

  </body>
</html>
