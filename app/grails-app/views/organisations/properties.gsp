<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <r:require module="annotations" />
    <g:javascript src="properties.js"/>
  </head>
  <body>

      <h1 class="ui header">${orgInstance.name}</h1>

      <g:render template="nav" contextPath="." />

        <semui:messages data="${flash}" />

        <g:each in="${authorizedOrgs}" var="authOrg">
            <h6 class="ui header">@ ${authOrg.name}</h6>

            <div id="custom_props_div_${authOrg.shortcode}" class="span12">
                <g:render template="/templates/properties/private" model="${[
                        prop_desc: PropertyDefinition.ORG_PROP,
                        ownobj: orgInstance,
                        custom_props_div: "custom_props_div_${authOrg.shortcode}",
                        tenant: authOrg]}"/>

                <r:script language="JavaScript">
                    $(document).ready(function(){
                        initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.shortcode}", ${authOrg.id});
                    });
                </r:script>
            </div>
		</g:each>


  </body>
</html>
