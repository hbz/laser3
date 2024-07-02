<%@ page import="de.laser.helper.Icons; de.laser.Combo; de.laser.CustomerIdentifier; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Org; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart message="${isProviderOrAgency ? 'org.nav.ids' : 'org.nav.idsCids.shy'}" serviceInjection="true" />

    <g:set var="isGrantedOrgRoleAdminOrOrgEditor" value="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}" />

<laser:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

%{--<g:if test="${editable_identifier || editable_customeridentifier}">--}%
    <ui:controlButtons>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[
                org: orgInstance,
                user: user,
                editable: (editable_identifier || editable_customeridentifier),
                editable_identifier: editable_identifier,
                editable_customeridentifier: editable_customeridentifier,
                hasAccessToCustomeridentifier: hasAccessToCustomeridentifier
        ]}"/>
    </ui:controlButtons>
%{--</g:if>--}%

<ui:h1HeaderWithIcon text="${orgInstance.name}" >
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

<ui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<ui:messages data="${flash}"/>

<ui:tabs actionName="ids">
    <ui:tabsItem controller="org" action="ids" params="[id: orgInstance.id, tab: 'identifier']" tab="identifier" text="${message(code:'default.identifiers.label')}"/>
    <g:if test="${hasAccessToCustomeridentifier}">
        <ui:tabsItem controller="org" action="ids" params="[id: orgInstance.id, tab: 'customerIdentifiers']" tab="customerIdentifiers" text="${message(code:'org.customerIdentifier.plural')}"/>
    </g:if>
</ui:tabs>

%{---------------IDENTIFIERS-----------------------}%
<div class="ui bottom attached tab active segment">

    <g:if test="${params.tab == 'identifier'}">
    <%-- orgInstance.hasPerm(CustomerTypeService.PERMS_ORG_PRO_CONSORTIUM_BASIC) && ((!fromCreate) || isGrantedOrgRoleAdminOrOrgEditor) --%>
        <table class="ui table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="one wide">${message(code:'default.number')}</th>
                <th class="five wide">${message(code:'identifier.namespace.label')}</th>
                <th class="four wide">${message(code:'identifier')}</th>
                <th class="four wide">${message(code:'default.notes.label')}</th>
                <th class="two wide">${message(code:'default.actions')}</th>
            </tr>
            </thead>
            <tbody>
            <laser:render template="idTableRow"
                      model="[orgInstance:orgInstance, tableRowNr:1, showGlobalUid:true, editable:false]"
            />
            <g:if test="${orgInstance.gokbId}">
                <laser:render template="idTableRow" model="[orgInstance:orgInstance, tableRowNr:2, showWekbId:true, editable:false]"/>
                <g:set var="globalCount" value="${2}"/>
            </g:if>
            <g:else>
                <g:set var="globalCount" value="${1}"/>
            </g:else>
            <g:each in="${orgInstance.ids?.toSorted{it.ns?.ns?.toLowerCase()}}" var="id" status="rowno">
                <g:if test="${rowno == 0}"><g:set var="rowno" value="${rowno+=globalCount}"/></g:if>
                <laser:render template="idTableRow"
                          model="[orgInstance:orgInstance, tableRowNr:rowno+1, id:id, editable:editable_identifier]"
                />
            </g:each>
            </tbody>
        </table>
    </g:if>

%{--------------CUSTOMER IDENTIFIERS------------------------}%
        <g:if test="${params.tab == 'customerIdentifiers'}">
            <ui:filter>
                <g:form controller="organisation" action="ids" class="ui small form" method="get">
                    <g:hiddenField name="tab" value="customerIdentifiers"/>
                    <g:hiddenField name="id" value="${orgInstance.id}"/>
                    <div class="two fields">
                        <div class="field">
                            <label for="customerIdentifier">${message(code: 'org.customerIdentifier')}</label>

                            <div class="ui input">
                                <input type="text" id="customerIdentifier" name="customerIdentifier"
                                       value="${params.customerIdentifier}"/>
                            </div>
                        </div>
                        <div class="field">
                            <label for="requestorKey">${message(code: 'org.requestorKey')}</label>

                            <div class="ui input">
                                <input type="text" id="requestorKey" name="requestorKey"
                                       value="${params.requestorKey}"/>
                            </div>
                        </div>
                    </div>
                    <div class="two fields">
                        <div class="field">
                            <label for="ciPlatform">${message(code:'provider.label')} : ${message(code:'platform.label')}</label>
                            <g:select id="ciPlatform" name="ciPlatform"
                                      from="${allPlatforms}"
                                      value="${params.ciPlatform}"
                                      class="ui search dropdown"
                                      optionKey="id"
                                      optionValue="${{ it.provider.name + (it.provider.sortname ? " (${it.provider.sortname})" : '') + ' : ' + it.name}}"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                            />
                        </div>
                        <div class="field la-field-right-aligned">
                            <a href="${createLink(controller:controllerName,action:actionName,params:[id:orgInstance.id,tab:'customerIdentifiers'])}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
                            <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}">
                        </div>
                    </div>
                </g:form>
            </ui:filter>
            <table class="ui la-js-responsive-table la-table table">
                <thead>
                    <tr>
                        <th class="one wide">${message(code:'default.number')}</th>
                        <g:sortableColumn title="${message(code:'provider.label')}" property="platform.org.name" class="three wide" params="[tab: 'customerIdentifiers']"/>
                        <g:sortableColumn title="${message(code:'platform.label')}" property="platform.name" class="two wide" params="[tab: 'customerIdentifiers']"/>
                        <th class="three wide">${message(code:'org.customerIdentifier')}</th>
                        <th class="three wide">${message(code:'org.requestorKey')}</th>
                        <th class="two wide">${message(code:'default.note.label')}</th>
                        %{--<th>${message(code:'default.isPublic.label')}</th>--}%
                        <th class="two wide">${message(code:'default.actions')}</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${customerIdentifier}" var="ci" status="rowno">
                        <g:if test="${ci.isPublic || (ci.owner.id == contextService.getOrg().id) || isGrantedOrgRoleAdminOrOrgEditor}">
                            <%  boolean editable_this_ci = (ci.customer.id == institution.id || isComboRelated) %>
                            <tr>
                                <td>${rowno+1}</td>
                                <td>
                                    ${ci.getProvider()}
                                </td>
                                <td>${ci.platform}</td>
                                <td><ui:xEditable owner="${ci}" field="value" overwriteEditable="${editable_customeridentifier && editable_this_ci}" /></td>
                                <td><ui:xEditable owner="${ci}" field="requestorKey" overwriteEditable="${editable_customeridentifier && editable_this_ci}" /></td>
                                <td><ui:xEditable owner="${ci}" field="note" overwriteEditable="${editable_customeridentifier && editable_this_ci}" /></td>
                                <td>
                                    %{-- TODO: erms-5495 --}%
                                    <g:if test="${editable_customeridentifier && editable_this_ci}">
                                        %{--}<button class="ui icon button blue la-modern-button" onclick="JSPC.app.IdContoller.editCustomerIdentifier(${ci.id});"
                                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                            <i aria-hidden="true" class="${Icons.CMD_EDIT}"></i>
                                        </button>--}%
                                        <g:link controller="organisation"
                                                action="deleteCustomerIdentifier"
                                                id="${orgInstance.id}"
                                                params="${[deleteCI:ci.id]}"
                                                class="ui button icon red la-modern-button js-open-confirm-modal"
                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.customeridentifier", args: [""+ci.getProvider()+" : "+ci.platform+" "+ci.value])}"
                                                data-confirm-term-how="delete"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="${Icons.CMD_DELETE}"></i>
                                        </g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                </tbody>
            </table>
        </g:if>
    </div>

<g:if test="${actionName == 'ids'}">
    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.IdContoller =  {
            createIdentifier : function(id) {
                var urlString = '<g:createLink controller="organisation" action="createIdentifier"/>?id='+id;
                JSPC.app.IdContoller._doAjax(urlString);
            },
            createCustomerIdentifier : function(id) {
                var urlString = '<g:createLink controller="organisation" action="createCustomerIdentifier"/>?id='+id;
                JSPC.app.IdContoller._doAjax(urlString);
            },
            editIdentifier : function(identifier) {
                var urlString = '<g:createLink controller="organisation" action="editIdentifier"/>?identifier='+identifier;
                JSPC.app.IdContoller._doAjax(urlString);
            },
            editCustomerIdentifier : function(customeridentifier) {
                var urlString = '<g:createLink controller="organisation" action="editCustomerIdentifier"/>?customeridentifier='+customeridentifier;
                JSPC.app.IdContoller._doAjax(urlString);
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
                                r2d2.initDynamicUiStuff('#modalCreateCustomerIdentifier');
                                r2d2.initDynamicXEditableStuff('#modalCreateCustomerIdentifier');
                            }
                        }).modal('show');
                    }
                });
            }
        }
    </laser:script>
</g:if>

<laser:htmlEnd />