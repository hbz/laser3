<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.RefdataValue" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>
    <r:require module="annotations" />
    <g:javascript src="properties.js"/>
</head>
<body>
    <semui:breadcrumbs>
        <semui:crumb message="menu.institutions.all_orgs" controller="organisations" action="index" />
        <g:message code="default.show.label" args="[entityName]" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon/>
        <g:if test="${personInstance?.contactType == RefdataValue.getByValueAndCategory('Functional contact', 'Person Contact Type')}">
            ${personInstance.contactType?.getI10n('value') + ': ' + personInstance?.last_name}
        </g:if>
        <g:elseif test="${personInstance?.contactType == RefdataValue.getByValueAndCategory('Personal contact', 'Person Contact Type')}">
            ${personInstance.contactType?.getI10n('value') + ': ' + personInstance}
        </g:elseif>
        <g:else>
            ${personInstance}
        </g:else>
    </h1>

    <g:render template="nav" contextPath="." />

    <div class="ui grid">

        <div class="sixteen wide column">

            <semui:messages data="${flash}" />

            <div class="la-inline-lists">

                <g:each in="${authorizedOrgs}" var="authOrg">

                    <div class="ui card">
                        <div class="content">

                            <div id="custom_props_div_${authOrg.id}">
                                <h5 class="ui header">${message(code:'org.properties.private')} ${authOrg.name}</h5>

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

                        </div>
                    </div><!-- .card -->

                </g:each>

            </div>

        </div>
    </div>

</body>
</html>
