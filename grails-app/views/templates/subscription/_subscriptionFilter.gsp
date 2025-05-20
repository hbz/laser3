<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.CustomerTypeService; de.laser.Org; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:serviceInjection />

<ui:filter>
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <g:if test="${license}">
            <input type="hidden" name="id" value="${license.id}"/>
        </g:if>
        <g:if test="${packageInstance}">
            <input type="hidden" name="id" value="${packageInstance.id}"/>
        </g:if>
        <g:if test="${surveyInfo}">
            <input type="hidden" name="id" value="${surveyInfo.id}"/>
            <input type="hidden" name="surveyConfigID" value="${surveyConfig.id}"/>
        </g:if>
        <g:if test="${actionName == 'subscriptionsManagement'}">
            <input type="hidden" name="tab" value="${params.tab}"/>
            <input type="hidden" name="propertiesFilterPropDef" value="${propertiesFilterPropDef}"/>
        </g:if>
        <div class="four fields">
            <% /* 1-1 */ %>
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                    <span data-position="right center" class="la-popup-tooltip" data-content="${message(code:'default.search.tooltip.subscription')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>
            <% /* 1-2 */ %>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" class="la-popup-tooltip" data-content="${message(code:'default.search.tooltip.subscription.identifier')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="identifier" name="identifier"
                           placeholder="${message(code: 'default.search.identifier.ph')}"
                           value="${params.identifier}"/>
                </div>
            </div>
            <% /* 1-3 */ %>
            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>
            <% /* 1-4 */ %>
            <div class="field">
                <label for="referenceYears">${message(code: 'subscription.referenceYear.label')}</label>
                <select id="referenceYears" name="referenceYears" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${referenceYears}" var="referenceYear">
                        <option <%=(params.list('referenceYears').contains(referenceYear.toString())) ? 'selected="selected"' : ''%>
                                value="${referenceYear}">
                            ${referenceYear}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 1-5 */ %>
            <div class="field">
                <label for="status"><g:message code="default.status.label"/></label>
                <select id="status" name="status" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="status">
                        <option <%=Params.getLongList(params, 'status').contains(status.id) ? 'selected="selected"' : ''%>
                                value="${status.id}">
                            ${status.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">
            <% /* 2-1/2 */ %>
            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
            <% /* 2-3 */ %>
            <div class="field">
                <label for="form"><g:message code="subscription.form.label"/></label>
                <select id="form" name="form" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}" var="form">
                        <option <%=Params.getLongList(params, 'form').contains(form.id) ? 'selected="selected"' : ''%>
                        value="${form.id}">
                        ${form.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 2-4 */ %>
            <div class="field">
                <label for="subKinds">${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" var="subKind">
                        <option <%=Params.getLongList(params, 'subKinds').contains(subKind.id) ? 'selected="selected"' : ''%>
                                value="${subKind.id}">
                            ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">
            <% /* 3-1 */ %>
            <div class="field">
                <label>${message(code:'subscription.isPublicForApi.label')}</label>
                <ui:select class="ui fluid dropdown" name="isPublicForApi"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.isPublicForApi}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-2 */ %>
            <div class="field">
                <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPerpetualAccess}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-3 */ %>
            <div class="field">
                <label>${message(code:'subscription.hasPublishComponent.label')}</label>
                <ui:select class="ui fluid dropdown" name="hasPublishComponent"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPublishComponent}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-4 */ %>
            <div class="field">
                <label for="resource"><g:message code="subscription.resource.label"/></label>
                <select id="resource" name="resource" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}" var="resource">
                        <option <%=Params.getLongList(params, 'resource').contains(resource.id) ? 'selected="selected"' : ''%>
                                value="${resource.id}">
                            ${resource.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">
            <% /* 4-1 */ %>
            <div class="field">
                <label for="provider"><g:message code="provider.label"/></label>
                <select id="provider" name="provider" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${providerService.getCurrentProviders(contextService.getOrg())}" var="provider">
                        <option <%=Params.getLongList(params, 'provider').contains(provider.id) ? 'selected="selected"' : ''%>
                                value="${provider.id}">
                            ${provider.name}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 4-2 */ %>
            <div class="field">
                <label for="vendor"><g:message code="vendor.label"/></label>
                <select id="vendor" name="vendor" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${vendorService.getCurrentVendors(contextService.getOrg())}" var="vendor">
                        <option <%=Params.getLongList(params, 'vendor').contains(vendor.id) ? 'selected="selected"' : ''%>
                                value="${vendor.id}">
                            ${vendor.name}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /*4-3 */ %>
            <div class="field">
                <label for="holdingSelection">${message(code: 'subscription.holdingSelection.label')}</label>
                <select id="holdingSelection" name="holdingSelection" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}" var="holdingSelection">
                        <option <%=Params.getLongList(params, 'holdingSelection').contains(holdingSelection.id) ? 'selected="selected"' : ''%>
                                value="${holdingSelection.id}">
                            ${holdingSelection.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 4-4 */ %>
            <div class="field">
                <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                            <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox" <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" <g:if test="${params.subRunTime}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="three fields">
            <% /* 5-1 */ %>
            <g:if test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                <div class="field">
                    <label></label>
                    <fieldset id="subscritionType">
                        <label>${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>
                        <div class="inline fields la-filter-inline">
                            <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE) - RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE}" var="subType">
                                <div class="inline field">
                                    <div class="ui checkbox">
                                        <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                        <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                            <g:if test="${Params.getLongList(params, 'subTypes').contains(subType.id)}"> checked="" </g:if>
                                               tabindex="0">
                                    </div>
                                </div>
                            </g:each>
                        </div>
                    </fieldset>
                </div>
            </g:if>
            <g:else>
                <div class="field"></div>
            </g:else>

            <% /* 5-2 */ %>
            <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
                <div class="field">
                    <fieldset>
                        <legend id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</legend>

                        <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                                  optionKey="${{ Org.class.name + ':' + it.id }}"
                                  optionValue="${{ it.getName() }}"
                                  name="consortia"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                                  value="${params.consortia}"/>
                    </fieldset>
                </div>
            </g:if>
            <g:else>
                <div class="field"></div>
            </g:else>

            <% /* 5-3 */ %>
            <div class="field la-field-right-aligned">
                <a href="${createLink(controller:controllerName,action:actionName,params:[id:params.id,resetFilter:true, tab: params.tab])}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}">
            </div>
        </div>

    </g:form>
</ui:filter>