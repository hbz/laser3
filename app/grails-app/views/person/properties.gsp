<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>

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
        <semui:crumb message="menu.public.all_orgs" controller="organisation" action="index" />
        <g:message code="default.show.label" args="[entityName]" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
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

                <div class="ui card">
                      <div class="content">
                           <% def org = contextService.getOrg() %>
                           <div id="custom_props_div_${org.id}">
                                <h5 class="ui header">${message(code:'org.properties.private')} ${org.name}</h5>
                                <g:render template="/templates/properties/private" model="${[
                                        prop_desc: PropertyDefinition.PRS_PROP,
                                        ownobj: personInstance,
                                        custom_props_div: "custom_props_div_${org.id}",
                                        tenant: org]}"/>
                                <r:script language="JavaScript">
                                        $(document).ready(function(){
                                            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${org.id}", ${org.id});
                                        });
                                </r:script>
                           </div>
                      </div>
                </div><!-- .card -->

            </div>

        </div>
    </div>

</body>
</html>
