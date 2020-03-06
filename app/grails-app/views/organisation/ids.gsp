<%@ page import="de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition; com.k_int.properties.PropertyDefinitionGroup; com.k_int.kbplus.OrgSettings" %>
<%@ page import="com.k_int.kbplus.Combo;grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="allOrgTypeIds" value="${orgInstance.getallOrgTypeIds()}" />
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

<g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ORG_EDITOR,ROLE_ADMIN')}">
    <semui:controlButtons>
        <g:render template="actions" model="${[org: orgInstance, user: user]}"/>
    </semui:controlButtons>
</g:if>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

<g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

<semui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

%{--<g:if test="${departmentalView == false}">--}%
    %{--<g:render template="/templates/meta/identifier" model="${[object: orgInstance, editable: editable]}"/>--}%
%{--</g:if>--}%

<semui:messages data="${flash}"/>

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
                        <dl>
                            <dt>ISIL</dt>
                            <dd>
                                <g:set var="isils"
                                       value="${orgInstance.ids.findAll { it?.ns?.ns == 'ISIL' }}"/>
                                <g:if test="${isils}">
                                    <div class="ui divided middle aligned selection list la-flex-list">
                                        <g:each in="${isils}" var="isil">
                                            <div class="ui item">

                                                <div class="content la-space-right">
                                                    <semui:xEditable owner="${isil}" field="value"/>
                                                </div>

                                                <div class="content">
                                                    <g:if test="${editable}">
                                                        <g:link controller="ajax" action="deleteIdentifier" class="ui icon mini negative button"
                                                                params='${[owner: "${orgInstance.class.name}:${orgInstance.id}", target: "${isil.class.name}:${isil.id}"]}'>
                                                            <i class="trash alternate icon"></i></g:link>
                                                    </g:if>
                                                </div>

                                            </div>
                                        </g:each>

                                    </div>

                                </g:if>
                                <g:if test="${editable}">
                                    <%-- TODO [ticket=1612] new identifier handling --%>
                                    <g:form controller="ajax" action="addIdentifier" class="ui form">
                                        <input name="owner" type="hidden" value="${orgInstance.class.name}:${orgInstance.id}" />
                                        <input name="namespace" type="hidden" value="com.k_int.kbplus.IdentifierNamespace:${com.k_int.kbplus.IdentifierNamespace.findByNs('ISIL').id}" />

                                        <div class="fields">
                                            <div class="field">
                                                <input name="value" id="identifier" type="text" class="ui" />
                                            </div>
                                            <div class="field">
                                                <button type="submit" class="ui button">Identifikator hinzufügen</button>
                                            </div>
                                        </div>
                                    </g:form>
                            </g:if>
                        </dd>

                    </dl>

                    <dl>
                        <dt>WIB-ID</dt>
                        <dd>
                            <g:set var="wibid" value="${orgInstance.ids.find { it?.ns?.ns == 'wibid' }}"/>
                            <g:if test="${wibid}">
                                <semui:xEditable owner="${wibid}" field="value"/>
                            </g:if>
                        </dd>
                    </dl>
                    <dl>
                        <dt>EZB-ID</dt>
                        <dd>
                            <g:set var="ezb" value="${orgInstance.ids.find { it?.ns?.ns == 'ezb' }}"/>
                            <g:if test="${ezb}">
                                <semui:xEditable owner="${ezb}" field="value"/>
                            </g:if>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->
        </g:if>
        </div>
    </div>
</div>
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
                <colgroup>
                    <col style="width:  30px;">
                    <col style="width: 170px;">
                    <col style="width: 236px;">
                    <col style="width: 277px;">
                    <col style="width: 332px;">
                    <col style="width:  82px;">
                </colgroup>
                <thead>
                    <tr>
                        <th>${message(code:'default.number')}</th>
                        <th>${message(code:'identifier.namespace.label')}</th>
                        <th>${message(code:'identifier')}</th>
                        <th>${message(code:'default.notes.label')}</th>
                        <th>${message(code:'default.aktions')}</th>
                    </tr>
                </thead>
                <tbody>

                <g:set var="isilList" value="${orgInstance.ids.findAll { it?.ns?.ns == 'ISIL' }}"/>
                <g:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, idList:isilList, namespace:message(code:'identifier.namespace.isil.label')]"/>

                <g:set var="wibidList" value="${orgInstance.ids.findAll { it?.ns?.ns == 'wibid' }}"/>
                <g:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, idList:wibidList, namespace:message(code:'identifier.namespace.wib-id.label')]"/>

                <g:set var="ezbList" value="${orgInstance.ids.findAll { it?.ns?.ns == 'ezb' }}"/>
                <g:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, idList:ezbList, namespace:message(code:'identifier.namespace.ezb-id.label')]"/>

                <g:set var="grididList" value="${orgInstance.ids.findAll { it?.ns?.ns == 'gridid' }}"/>
                <g:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, idList:wibidList, namespace:message(code:'identifier.namespace.grid-id.label')]"/>

                <g:set var="dbsidList" value="${orgInstance.ids.findAll { it?.ns?.ns == 'dbsid' }}"/>
                <g:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, idList:dbsidList, namespace:message(code:'identifier.namespace.dbs-id.label')]"/>

                <g:set var="gndnrList" value="${orgInstance.ids.findAll { it?.ns?.ns == 'gndnr' }}"/>
                <g:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:++tableIdentifierRowNr, idList:gndnrList, namespace:message(code:'identifier.namespace.gnd-nr.label')]"/>

                </dd>
                </tbody>
            </table>
        </div>
    </div>
</g:if>

%{--------------CUSTOMER IDENTIFIERS------------------------}%
            <div class="ui card">
                <div class="content">
                    <div class="header"><g:message code="org.customerIdentifier.plural"/></div>
                </div>
                <div class="content">
                    <% int tableCustomerRowNr = 0 %>

                    <table class="ui la-table table">
                        <thead>
                        <tr>
                            <th>${message(code:'default.number')}</th>
                            <th>${message(code:'default.provider.label')} : ${message(code:'platform.label')}</th>
                            <th>${message(code:'org.customerIdentifier')}</th>
                            <th>${message(code:'default.note.label')}</th>
                            %{--<th>${message(code:'default.isPublic.label')}</th>--}%
                            <th>${message(code:'default.aktions')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${customerIdentifier}" var="ci">
                            <g:if test="${ci.isPublic || (ci.owner.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                                <tr>
                                    <td>${++tableCustomerRowNr}</td>
                                    <td>
                                        ${ci.getProvider()} : ${ci.platform}
                                    </td>
                                    %{--<g:if test="${(editable && ci.owner.id == contextService.getOrg().id) || (isComboRelated && ci.owner.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">--}%
                                        %{--<td>--}%
                                            %{--<semui:xEditable owner="${ci}" field="value" overwriteEditable="true" />--}%
                                        %{--</td>--}%
                                        %{--<td>--}%
                                            %{--<semui:xEditable owner="${ci}" field="note" overwriteEditable="true" />--}%
                                        %{--</td>--}%
                                        %{--<td>--}%
                                            %{--<semui:xEditableBoolean owner="${ci}" field="isPublic" overwriteEditable="true" />--}%
                                        %{--</td>--}%
                                        %{--<td>--}%
                                            %{--<g:link controller="organisation" action="settings" id="${orgInstance.id}"--}%
                                                    %{--params="${[deleteCI:ci.class.name + ':' + ci.id]}"--}%
                                                    %{--class="ui button icon red"><i class="trash alternate icon"></i></g:link>--}%
                                        %{--</td>--}%
                                    %{--</g:if>--}%
                                    %{--<g:else>--}%
                                        <td>${ci.value}</td>
                                        <td>${ci.note}</td>
                                        %{--<td>${ci.isPublic ? message(code:'refdata.Yes') : message(code:'refdata.No')}</td>--}%
                                        <td>
                                            <g:if test="${editable}">
                                                TODO: EDIT/Delete
                                                <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
                                                    <i class="write icon"></i>
                                                </g:link>
                                                <g:link controller="organisation" action="settings" id="${orgInstance.id}"
                                                        params="${[deleteCI:ci.class.name + ':' + ci.id]}"
                                                        class="ui button icon red"><i class="trash alternate icon"></i></g:link>
                                            </g:if>
                                        </td>
                                    %{--</g:else>--}%
                                </tr>
                            </g:if>
                        </g:each>
                        </tbody>
                </table>
                </div>
            </div>
        </div>
</body>
</html>
