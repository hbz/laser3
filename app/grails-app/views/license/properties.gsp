<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'license.label')}" />
    <title>${message(code:'laser')} : <g:message code="license" /></title>
    <%-- r:require module="annotations" / --%>
    <g:javascript src="properties.js"/>
</head>
<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <g:if test="${license.type?.value == 'Template'}">${message(code:'license.label')} (${license.type.getI10n('value')}):</g:if>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />

<div>
    <semui:messages data="${flash}" />

    <g:each in="${authorizedOrgs}" var="authOrg">
        <h6 class="ui header">@ ${authOrg.name}</h6>

        <div id="custom_props_div_${authOrg.id}">
            <g:render template="/templates/properties/private" model="${[
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    custom_props_div: "custom_props_div_${authOrg.id}",
                    tenant: authOrg]}"/>

            <r:script language="JavaScript">
                $(document).ready(function(){
                    c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                });
            </r:script>
        </div>
    </g:each>
</div>

</body>
</html>
