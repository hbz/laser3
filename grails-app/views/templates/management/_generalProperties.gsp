<%@ page import="de.laser.CustomerTypeService; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.FormService; de.laser.Subscription; de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">

    <g:set var="editableOld" value="${editable}"/>

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
%{--            <h3 class="ui header"><g:message code="consortium.superSubscriptionType"/></h3>--}%
            <table class="ui compact monitor stackable la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'subscription')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'subscription.referenceYear.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.kind.label')}</th>
                    <th>${message(code: 'subscription.form.label')}</th>
                    <th>${message(code: 'subscription.resource.label')}</th>
                    <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                    <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                    <th>${message(code: 'subscription.hasPublishComponent.label')}</th>
                    <g:if test="${subscription.packages}">
                        <th>${message(code: 'subscription.holdingSelection.label')}</th>
                    </g:if>
                    <th>${message(code: 'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>${subscription.name}</td>
                    <td>
                        <ui:xEditable owner="${subscription}" field="startDate" type="date" validation="datesCheck"/>
                        <ui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <ui:xEditable owner="${subscription}" field="endDate" type="date" validation="datesCheck" overwriteEditable="${editable && !subscription.isAutomaticRenewAnnually}"/>
                        <ui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        <ui:xEditable owner="${subscription}" field="referenceYear" type="year"/>
                        <ui:auditButton auditable="[subscription, 'referenceYear']"/>
                    </td>
                    <td>
                        <ui:xEditableRefData owner="${subscription}" field="status" config="${RDConstants.SUBSCRIPTION_STATUS}" constraint="removeValue_deleted"/>
                        <ui:auditButton auditable="[subscription, 'status']"/>
                    </td>
                    <td>
                        <ui:xEditableRefData owner="${subscription}" field="kind" config="${RDConstants.SUBSCRIPTION_KIND}"/>
                        <ui:auditButton auditable="[subscription, 'kind']"/>
                    </td>
                    <td>
                        <ui:xEditableRefData owner="${subscription}" field="form" config="${RDConstants.SUBSCRIPTION_FORM}"/>
                        <ui:auditButton auditable="[subscription, 'form']"/>
                    </td>
                    <td>
                        <ui:xEditableRefData owner="${subscription}" field="resource" config="${RDConstants.SUBSCRIPTION_RESOURCE}"/>
                        <ui:auditButton auditable="[subscription, 'resource']"/>
                    </td>
                    <td>
                        <ui:xEditableBoolean owner="${subscription}" field="isPublicForApi"/>
                        <ui:auditButton auditable="[subscription, 'isPublicForApi']"/>
                    </td>
                    <td>
                        <ui:xEditableBoolean owner="${subscription}" field="hasPerpetualAccess"/>
                        <ui:auditButton auditable="[subscription, 'hasPerpetualAccess']"/>
                    </td>
                    <td>
                        <ui:xEditableBoolean owner="${subscription}" field="hasPublishComponent"/>
                        <ui:auditButton auditable="[subscription, 'hasPublishComponent']"/>
                    </td>
                    <td>
                        <ui:xEditableRefData owner="${subscription}" field="holdingSelection" config="${RDConstants.SUBSCRIPTION_HOLDING}"/>
                        <ui:auditButton auditable="[subscription, 'holdingSelection']"/>
                    </td>

                    <td class="x">
                        <g:link controller="subscription" action="show" id="${subscription.id}"
                                class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="write icon"></i></g:link>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

    </g:if>

    <h3 class="ui header">${message(code: 'subscriptionsManagement.info.subscriptionProperty')}</h3>

    <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'generalProperties', id: params.id]" method="post"
            class="ui form propertiesSubscription">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="ui segments">
        <div class="ui segment">
            <g:set var="tmplAddColumns" value="${contextService.getOrg().isCustomerType_Consortium() && controllerName == 'myInstitution'}" />

            <div class="ui ${tmplAddColumns ? 'divided compact grid' : 'grid'}">
                <div class="row">
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <ui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from"/>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select name="audit_valid_from" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <ui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to"/>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown"  name="audit_valid_to" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <ui:datepicker label="subscription.referenceYear.label" id="reference_year" name="reference_year" type="year"/>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_reference_year" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'default.status.label')}</label>
                                <%
                                    def fakeList = []
                                    fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                                    fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
                                %>
                                <ui:select class="ui dropdown" name="process_status" from="${fakeList}" optionKey="id" optionValue="value"
                                           noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_process_status" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.kind.label')}</label>
                                <ui:select class="ui dropdown" name="process_kind"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_process_kind" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.form.label')}</label>
                                <ui:select class="ui dropdown" name="process_form"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_process_form" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.resource.label')}</label>
                                <ui:select class="ui dropdown" name="process_resource"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_process_resource" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.isPublicForApi.label')}</label>
                                <ui:select class="ui dropdown" name="process_isPublicForApi"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_isPublicForApi" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                                <ui:select class="ui dropdown" name="process_hasPerpetualAccess"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_hasPerpetualAccess" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.hasPublishComponent.label')}</label>
                                <ui:select class="ui dropdown" name="process_hasPublishComponent"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_hasPublishComponent" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <div class="${tmplAddColumns ? 'two fields' : 'one field'}">
                            <div class="field">
                                <label>${message(code: 'subscription.holdingSelection.label')}</label>
                                <ui:select class="ui dropdown" name="process_holding"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                            <g:if test="${tmplAddColumns}">
                                <div class="field">
                                    <label><span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.auditable')}"><i class="ui icon thumbtack"></i></span></label>
                                    <ui:select class="ui dropdown" name="audit_process_holding" from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                               optionKey="id" optionValue="value" noSelection="${['': '']}"
                                               value="${['': '']}"/>
                                </div>
                            </g:if>
                        </div>
                    </div>
                    <div class="four wide column">
                        <g:if test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                            <div class="field">
                                <label>${message(code: 'subscription.isAutomaticRenewAnnually.label')}</label>
                                <ui:select class="ui dropdown" name="process_isAutomaticRenewAnnually"
                                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                           optionKey="id" optionValue="value" noSelection="${['': '']}"
                                           value="${['': '']}"/>
                            </div>
                        </g:if>
                    </div>
                </div>
                <div class="row">
                    <div class="column">
                        <button class="ui button" ${!editable ? 'disabled="disabled"' : ''} type="submit" name="processOption"
                                value="changeProperties">${message(code: 'default.button.save_changes')}</button>
                    </div>
                </div>
            </div>
        </div><!-- .segment -->

        <div class="ui segment">
            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber total="${num_sub_rows}"/>
                </g:else>
            </h3>

            <table class="ui compact  monitor stackable la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th class="center aligned">
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                    </g:if>
                    <g:if test="${controllerName == "subscription"}">
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <th>${message(code: 'default.subscription.label')}</th>
                    </g:if>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'subscription.referenceYear.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.kind.label')}</th>
                    <th>${message(code: 'subscription.form.label')}</th>
                    <th>${message(code: 'subscription.resource.label')}</th>
                    <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                    <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                    <th>${message(code: 'subscription.hasPublishComponent.label')}</th>
                    <th>${message(code: 'subscription.holdingSelection.label')}</th>
                    <g:if test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                        <th>${message(code: 'subscription.isAutomaticRenewAnnually.label')}</th>
                    </g:if>
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th>${message(code:'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                    <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                    <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriber() : zeile.orgs}"/>
                    <tr>
                        <g:if test="${editable}">
                            <td>
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}" checked="false"/>
                                    </div>
                                </div>
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "subscription"}">
                            <td>
                                ${subscr.sortname}
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                                <g:if test="${sub.isSlaved}">
                                    <span data-position="top right"
                                          class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                        <i class="grey la-thumbtack-regular icon"></i>
                                    </span>
                                </g:if>

                                <ui:instProIcon org="${subscr}" />
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "myInstitution"}">
                            <td>${sub.name}</td>
                        </g:if>

                        <td>
                            <ui:xEditable owner="${sub}" field="startDate" type="date" overwriteEditable="${editableOld}" validation="datesCheck"/>
                            <ui:auditButton auditable="[sub, 'startDate']"/>
                        </td>
                        <td>
                            <ui:xEditable owner="${sub}" field="endDate" type="date" overwriteEditable="${editableOld}" validation="datesCheck"/>
                            <ui:auditButton auditable="[sub, 'endDate']"/>
                        </td>
                        <td>
                            <ui:xEditable owner="${sub}" field="referenceYear" type="year" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'referenceYear']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="status"
                                                    config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                    constraint="removeValue_deleted"
                                                    overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'status']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="kind" config="${RDConstants.SUBSCRIPTION_KIND}" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'kind']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="form" config="${RDConstants.SUBSCRIPTION_FORM}" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'form']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="resource" config="${RDConstants.SUBSCRIPTION_RESOURCE}" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'resource']"/>
                        </td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="isPublicForApi" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'isPublicForApi']"/>
                        </td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="hasPerpetualAccess" overwriteEditable="${editableOld}"/>
                            <%--<ui:xEditableRefData owner="${sub}" field="hasPerpetualAccess"
                                                    config="${RDConstants.Y_N}"
                                                    overwriteEditable="${editableOld}"/>--%>
                            <ui:auditButton auditable="[sub, 'hasPerpetualAccess']"/>
                        </td>
                        <td>
                            <ui:xEditableBoolean owner="${sub}" field="hasPublishComponent" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'hasPublishComponent']"/>
                        </td>
                        <td>
                            <ui:xEditableRefData owner="${sub}" field="holdingSelection" config="${RDConstants.SUBSCRIPTION_HOLDING}" overwriteEditable="${editableOld}"/>
                            <ui:auditButton auditable="[sub, 'holdingSelection']"/>
                        </td>
                        <g:if test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                            <td>
                                <g:if test="${(sub.type == RDStore.SUBSCRIPTION_TYPE_LOCAL && sub._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                                    <ui:xEditableBoolean owner="${sub}" field="isAutomaticRenewAnnually" overwriteEditable="${editable && sub.isAllowToAutomaticRenewAnnually()}"/>
                                </g:if>
                            </td>
                        </g:if>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                      data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:if>
                        </td>
                        <td class="x">
                            <g:link controller="subscription" action="show" id="${sub.id}"
                                    class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div><!-- .segment -->
        </div><!-- .segments -->
    </g:form>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:else>
</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });

    $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
        if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
            var startDate = Date.parse(JSPC.helper.formatDate($("#valid_from").val()));
            var endDate = Date.parse(JSPC.helper.formatDate($("#valid_to").val()));
            return (startDate < endDate);
        }
        else return true;
    };

    $('.propertiesSubscription').form({
        on: 'blur',
        inline: true,
        fields: {
            valid_from: {
                identifier: 'valid_from',
                rules: [
                    {
                        type: 'endDateNotBeforeStartDate',
                        prompt: '<g:message code="validation.startDateAfterEndDate"/>'
                    }
                ]
            },
            valid_to: {
                identifier: 'valid_to',
                rules: [
                    {
                        type: 'endDateNotBeforeStartDate',
                        prompt: '<g:message code="validation.endDateBeforeStartDate"/>'
                    }
                ]
            },
            noSubscription: {
                identifier: 'selectedSubs',
                rules: [
                    {
                        type: 'checked',
                        prompt: '<g:message code="subscriptionsManagement.noSelectedSubscriptions.table"/>'
                    }
                ]
            }
        }
    });
</laser:script>


