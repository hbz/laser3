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
    <semui:breadcrumbs>
        <semui:crumb message="menu.institutions.all_orgs" controller="organisations" action="index" />
        <g:message code="default.show.label" args="[entityName]" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon/>
    ${personInstance?.contactType == com.k_int.kbplus.RefdataValue.getByValueAndCategory('Functional contact', 'Person Contact Type') ? personInstance?.contactType.getI10n('value') + ': ' + personInstance?.last_name : personInstance?.contactType.getI10n('value') + ': ' + personInstance?.first_name?:"" + ' ' + personInstance?.last_name}
    </h1>
    <g:render template="nav" contextPath="." />

    <div class="ui grid">

        <div class="sixteen wide column">

            <semui:messages data="${flash}" />

            <g:each in="${authorizedOrgs}" var="authOrg">
                <br />

                <div id="custom_props_div_${authOrg.id}">
                    <h5 class="ui header">@ ${authOrg.name}</h5>

                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.PRS_PROP,
                            ownobj: personInstance,
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
    </div>

</body>
</html>
