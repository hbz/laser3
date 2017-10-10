<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'license.label', default: 'License')}" />
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="license" default="License"/></title>
    <r:require module="annotations" />
    <g:javascript src="properties.js"/>
</head>
<body>

    <div class="container">
        <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

    <div class="container">
        <h1>
            ${license.licensee?.name}
            ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
            <g:xEditable owner="${license}" field="reference" id="reference"/>
        </h1>
        <g:render template="nav" />
    </div>

<div class="container">
    <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
    </g:if>

    <g:each in="${authorizedOrgs}" var="authOrg">
        <h6>@ ${authOrg.name}</h6>

        <div id="custom_props_div_${authOrg.shortcode}" class="span12">
            <g:render template="/templates/properties/private" model="${[
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    custom_props_div: "custom_props_div_${authOrg.shortcode}",
                    tenant: authOrg]}"/>

            <r:script language="JavaScript">
                $(document).ready(function(){
                    initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.shortcode}");
                });
            </r:script>
        </div>
    </g:each>
</div>

</body>
</html>
