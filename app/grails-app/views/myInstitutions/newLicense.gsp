<%@ page import="com.k_int.kbplus.License" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'license', default: 'License')}" />
    <title><g:message code="default.create.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row-fluid">
      
      <div class="span11">

        <h1 class="ui header"><g:message code="default.create.label" args="[entityName]" /></h1>

        <semui:messages data="${flash}" />

        <g:hasErrors bean="${licenseInstance}">
        <bootstrap:alert class="alert-error">
        <ul>
          <g:eachError bean="${licenseInstance}" var="error">
          <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
          </g:eachError>
        </ul>
        </bootstrap:alert>
        </g:hasErrors>

        <fieldset>
          <g:form class="ui form"
                  action="newLicense" 
                  params="${[shortcode:params.shortcode]}">
            <fieldset>
              

              <div class="control-group ">
                <label class="control-label" for="reference">License Reference / Name</label>
                  <div class="controls">
                    <g:textField name="reference" value="${fieldValue(bean: licenseInstance, field: 'reference')}" class="large" />
                 </div>
              </div>

              <div class="ui segment form-actions">
                <button type="submit" class="ui primary button">
                  <i class="icon-ok icon-white"></i>
                  <g:message code="default.button.create.label" default="Create" />
                </button>
              </div>
            </fieldset>
          </g:form>
        </fieldset>
        
      </div>

    </div>
  </body>
</html>
