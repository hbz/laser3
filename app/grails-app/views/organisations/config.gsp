<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>
    <r:require module="annotations" />
    <g:javascript src="properties.js"/>
  </head>
  <body>


    <h1 class="ui header">${orgInstance.name}</h1>

    <g:render template="nav" contextPath="." />

     <h6>${message(code:'org.properties')}</h6>
        <div id="custom_props_div_1" class="span12">
            <g:render template="/templates/properties/custom" model="${[
                    prop_desc: PropertyDefinition.ORG_CONF,
                    ownobj: orgInstance,
                    custom_props_div: "custom_props_div_1" ]}"/>
        </div>
        <r:script language="JavaScript">
        $(document).ready(function(){
            initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_1");
        });
        </r:script>


  </body>
</html>
