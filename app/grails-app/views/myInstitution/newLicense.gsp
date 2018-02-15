<%@ page import="com.k_int.kbplus.License" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'license', default: 'License')}" />
    <title><g:message code="default.create.label" args="[entityName]" /></title>
  </head>
  <body>

        <h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

        <semui:errors bean="${licenseInstance}" />

        <fieldset>
          <g:form class="ui form" action="newLicense">
            <fieldset>
              

              <div class="control-group ">
                <label class="control-label" for="reference">License Reference / Name</label>
                  <div class="controls">
                    <g:textField name="reference" value="${fieldValue(bean: licenseInstance, field: 'reference')}" class="large" />
                 </div>
              </div>

              <div class="ui form-actions">
                <button type="submit" class="ui button">
                  <i class="checkmark icon"></i>
                  <g:message code="default.button.create.label" default="Create" />
                </button>
              </div>
            </fieldset>
          </g:form>
        </fieldset>

  </body>
</html>
