<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>KB+ <g:message code="default.show.label" args="[entityName]" /></title>
    <r:require module="annotations" />
    <g:javascript src="properties.js"/>
  </head>
  <body>

    <div class="container">
      <h1>${orgInstance.name}</h1>
      <g:render template="nav" contextPath="." />
    </div>

    <div class="container">
		<g:if test="${flash.message}">
			<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
		</g:if>
      
		<h6>${message(code:'org.private_properties')}</h6>
		<div id="custom_props_div" class="span12">
			<g:render template="/templates/properties/private" model="${[ prop_desc:PropertyDefinition.PRV_ORG_PROP,ownobj:orgInstance ]}"/>
		</div>

		<h3>Private Property Rules</h3>
		<p>These properties are mandatory because of your membership in one ore more organisations.</p>
		
		${ppRules}
		
		<g:each in="${ppRules}" var="ppr">
			<p>
				
			</p>				
		</g:each>
    </div>
    
<r:script language="JavaScript">
window.onload = function() {
	initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>");
}
</r:script>

  </body>
</html>
