<%@ page import="com.k_int.kbplus.CustomerIdentifier; de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition; com.k_int.properties.PropertyDefinitionGroup; com.k_int.kbplus.OrgSettings" %>
<%@ page import="com.k_int.kbplus.Combo;grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    %{--<g:set var="allOrgTypeIds" value="${orgInstance.getallOrgTypeIds()}" />--}%
    <g:set var="isGrantedOrgRoleAdminOrOrgEditor" value="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}" />

    %{--<g:if test="${RDStore.OT_PROVIDER.id in allOrgTypeIds}">--}%
        %{--<g:set var="entityName" value="${message(code: 'default.provider.label')}"/>--}%
    %{--</g:if>--}%
    %{--<g:elseif test="${institutionalView}">--}%
        %{--<g:set var="entityName" value="${message(code: 'org.institution.label')}"/>--}%
    %{--</g:elseif>--}%
    %{--<g:elseif test="${departmentalView}">--}%
        %{--<g:set var="entityName" value="${message(code: 'org.department.label')}"/>--}%
    %{--</g:elseif>--}%
    %{--<g:else>--}%
        %{--<g:set var="entityName" value="${message(code: 'org.label')}"/>--}%
    %{--</g:else>--}%
    <title>${message(code: 'laser')} : ${message(code:'menu.institutions.org_info')}</title>

    <g:javascript src="properties.js"/>
</head>

<body>

<semui:debugInfo>
    <g:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
    <g:render template="/templates/debug/orgRoles" model="[debug: orgInstance.links]"/>
    <g:render template="/templates/debug/prsRoles" model="[debug: orgInstance.prsLinks]"/>
</semui:debugInfo>

<g:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, departmentalView: departmentalView, institutionalView: institutionalView]}"/>

<g:if test="${editable_identifier || editable_customeridentifier}">
    <semui:controlButtons>
        <g:render template="actions" model="${[
                org: orgInstance,
                user: user,
                editable: (editable_identifier || editable_customeridentifier),
                editable_identifier: editable_identifier,
                editable_customeridentifier: editable_customeridentifier,
                hasAccessToCustomeridentifier: hasAccessToCustomeridentifier
        ]}"/>
    </semui:controlButtons>
</g:if>
<g:else>
    <br />
</g:else>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

<g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

<semui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

%{--<g:if test="${departmentalView == false}">--}%
    %{--<g:render template="/templates/meta/identifier" model="${[object: orgInstance, editable: editable_identifier]}"/>--}%
%{--</g:if>--}%

<semui:messages data="${flash}"/>
%{---------------IDENTIFIERS-----------------------}%
<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">

<%-- orgInstance.hasPerm("ORG_INST,ORG_CONSORTIUM") && ((!fromCreate) || isGrantedOrgRoleAdminOrOrgEditor) --%>
<g:if test="${departmentalView == false}">
    <div class="ui card">
        <div class="content">
            <div class="header"><g:message code="default.identifiers.label"/></div>
        </div>

        <div class="content">
            <% int tableIdentifierRowNr = 0 %>
            <table class="ui table la-table">
                <thead>
                    <tr>
                        <th class="one wide">${message(code:'default.number')}</th>
                        <th class="five wide">${message(code:'identifier.namespace.label')}</th>
                        <th class="four wide">${message(code:'identifier')}</th>
                        <th class="four wide">${message(code:'default.notes.label')}</th>
                        <th class="two wide">${message(code:'default.aktions')}</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${orgInstance.ids?.toSorted{it.ns?.ns?.toLowerCase()}}" var="id">
                        <g:render template="idTableRow"
                                  model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, id:id, editable:editable_identifier]"
                         />
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</g:if>

%{--------------CUSTOMER IDENTIFIERS------------------------}%
        <g:if test="${hasAccessToCustomeridentifier}">

            <div class="ui card">
                <div class="content">
                    <div class="header"><g:message code="org.customerIdentifier.plural"/></div>
                </div>
                <div class="content">
                    <% int tableCustomerRowNr = 0 %>

                    <table class="ui la-table table">
                        <thead>
                        <tr>
                            <th class="one wide">${message(code:'default.number')}</th>
                            <th class="five wide">${message(code:'default.provider.label')} : ${message(code:'platform.label')}</th>
                            <th class="four wide">${message(code:'org.customerIdentifier')}</th>
                            <th class="four wide">${message(code:'default.note.label')}</th>
                            %{--<th>${message(code:'default.isPublic.label')}</th>--}%
                            <th class="two wide">${message(code:'default.aktions')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${customerIdentifier}" var="ci">
                            <g:if test="${ci.isPublic || (ci.owner.id == contextService.getOrg().id) || isGrantedOrgRoleAdminOrOrgEditor}">
                                <tr>
                                    <td>${++tableCustomerRowNr}</td>
                                    <td>
                                        ${ci.getProvider()} : ${ci.platform}
                                    </td>
                                    <td>${ci.value}</td>
                                    <td>${ci.note}</td>
                                    <td>
                                        <%  boolean editable_this_ci = (ci.owner.id == contextService.org.id) &&
                                            (ci.customer.id == contextService.org.id ||
                                                    Combo.findByFromOrgAndToOrg(contextservice.org, contextService.org))
                                        %>
                                        <g:if test="${editable_customeridentifier && editable_this_ci}">
                                            <button class="ui icon button" onclick="IdContoller.editCustomerIdentifier(${ci.id});"><i class="write icon"></i></button>
                                            <g:link controller="organisation"
                                                    action="deleteCustomerIdentifier"
                                                    id="${orgInstance.id}"
                                                    params="${[deleteCI:ci.class.name + ':' + ci.id]}"
                                                    class="ui button icon red js-open-confirm-modal"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.customeridentifier", args: [""+ci.getProvider()+" : "+ci.platform+" "+ci.value])}"
                                                    data-confirm-term-how="delete"
                                            >
                                                <i class="trash alternate icon"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:if>
                        </g:each>
                        </tbody>
                </table>
                </div>
            </div>
        </g:if>
</body>
</html>
<g:if test="${actionName == 'ids'}">
    <g:javascript>
        IdContoller =  {
            createIdentifier : function(id) {
                var urlString = '<g:createLink controller="organisation" action="createIdentifier"/>?id='+id;
                IdContoller._doAjax(urlString);
            },
            createCustomerIdentifier : function(id) {
                var urlString = '<g:createLink controller="organisation" action="createCustomerIdentifier"/>?id='+id;
                IdContoller._doAjax(urlString);
            },
            editIdentifier : function(identifier) {
                var urlString = '<g:createLink controller="organisation" action="editIdentifier"/>?identifier='+identifier;
                IdContoller._doAjax(urlString);
            },
            editCustomerIdentifier : function(customeridentifier) {
                var urlString = '<g:createLink controller="organisation" action="editCustomerIdentifier"/>?customeridentifier='+customeridentifier;
                IdContoller._doAjax(urlString);
            },

            _doAjax : function(url) {
                $.ajax({
                    url: url,
                    success: function(result){
                        $("#dynamicModalContainer").empty();
                        $("#modalCreateCustomerIdentifier, #modalCreateIdentifier").remove();

                        $("#dynamicModalContainer").html(result);
                        $("#dynamicModalContainer .ui.modal").modal({
                            onVisible: function () {
                                r2d2.initDynamicSemuiStuff('#modalCreateCustomerIdentifier');
                                r2d2.initDynamicXEditableStuff('#modalCreateCustomerIdentifier');

                                // ajaxPostFunc()
                            }
                        }).modal('show');
                    }
                });
            }
        }
    </g:javascript>
</g:if>
