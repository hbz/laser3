<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <r:require module="annotations" />
    <g:javascript src="properties.js"/>
</head>
<body>

    <h1 class="ui header">${personInstance.first_name} ${personInstance.middle_name} ${personInstance.last_name}</h1>
    <g:render template="nav" contextPath="." />

    <div class="ui grid">

        <div class="sixteen wide column">

            <semui:messages data="${flash}" />

            <g:each in="${authorizedOrgs}" var="authOrg">
                <br />

                <div id="custom_props_div_${authOrg.shortcode}">
                    <h5 class="ui header">@ ${authOrg.name}</h5>

                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.PRS_PROP,
                            ownobj: personInstance,
                            custom_props_div: "custom_props_div_${authOrg.shortcode}",
                            tenant: authOrg]}"/>

                    <r:script language="JavaScript">
                            $(document).ready(function(){
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.shortcode}", ${authOrg.id});
                            });
                    </r:script>
                </div>

            </g:each>

        </div>
    </div>

</body>
</html>
