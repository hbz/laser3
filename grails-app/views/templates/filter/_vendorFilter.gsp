<%@ page import="de.laser.wekb.Provider; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.utils.LocaleUtils; de.laser.I10nTranslation; de.laser.*; de.laser.auth.Role; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.storage.RDStore" %>

<%
    String lang = LocaleUtils.getCurrentLang()
    String getAllRefDataValuesForCategoryQuery = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv.value_" + lang
%>

<g:each in="${tmplConfigShow}" var="row">

    <g:set var="numberOfFields" value="${row.size()}" />
    <% if (row.contains('country&region')) { numberOfFields++ } %>
    <% if (row.contains('property&value')) { numberOfFields++ } %>

    <g:if test="${numberOfFields > 1}">
        <div class="${numberOfFields==5 ? 'five fields' : numberOfFields==4 ? 'four fields' : numberOfFields==3 ? 'three fields' : numberOfFields==2 ? 'two fields' : ''}">
    </g:if>

        <g:each in="${row}" var="field" status="fieldCounter">

            <g:if test="${field.equalsIgnoreCase('name')}">
                <div class="field">
                    <label for="nameContains">
                        <g:message code="org.search.vendor.contains"/>
                        <span data-position="right center" data-variation="tiny" class="la-popup-tooltip" data-content="${message(code:'org.search.vendor.contains.tooltip')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </label>
                    <input type="text" id="nameContains" name="nameContains"
                           placeholder="${message(code:'default.search.ph')}"
                           value="${params.nameContains}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('isMyX')}">
                <div class="field">
                    <label for="isMyX">
                        <g:message code="filter.isMyX.label" />
                    </label>
                    <%
                        List<Map> isMyXOptions = []

                        if (actionName == 'list'  || showAllIsMyXOptions) {
                            isMyXOptions.add([ id: 'wekb_exclusive',    value: "${message(code:'filter.wekb.exclusive')}" ])
                            isMyXOptions.add([ id: 'wekb_not',          value: "${message(code:'filter.wekb.not')}" ])
                            isMyXOptions.add([ id: 'ismyx_exclusive',   value: "${message(code:'filter.isMyX.exclusive', args:["${message(code:'menu.my.vendors')}"])}" ])
                            isMyXOptions.add([ id: 'ismyx_not',         value: "${message(code:'filter.isMyX.not')}" ])
                        }
                        else if (actionName == 'currentVendors') {
                            isMyXOptions.add([ id: 'wekb_exclusive',    value: "${message(code:'filter.wekb.exclusive')}" ])
                            isMyXOptions.add([ id: 'wekb_not',          value: "${message(code:'filter.wekb.not')}" ])
                        }
                    %>
                    <select id="isMyX" name="isMyX" class="ui selection fluid dropdown" multiple="">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${isMyXOptions}" var="opt">
                            <option <%=(params.list('isMyX').contains(opt.id)) ? 'selected="selected"' : '' %> value="${opt.id}">${opt.value}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('property&value')}">
                <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('privateContacts')}">
                <div class="field">
                    <label for="privateContact">
                        <g:message code="contact.name"/>
                        <span data-position="right center" data-variation="tiny" class="la-popup-tooltip" data-content="${message(code:'org.search.contact.tooltip')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </label>
                    <input id="privateContact" name="privateContact" type="text" placeholder="${message(code: 'default.search.ph')}" value="${params.privateContact}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('venStatus')}">
                <div class="field">
                    <label for="venStatus">${message(code: 'default.status.label')}</label>
                    <select id="venStatus" name="venStatus" multiple="" class="ui selection fluid dropdown">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${RefdataCategory.getAllRefdataValues([RDConstants.VENDOR_STATUS])-RDStore.VENDOR_STATUS_REMOVED}" var="venStatus">
                            <option <%=Params.getLongList(params, 'venStatus').contains(venStatus.id) ? 'selected="selected"' : ''%> value="${venStatus.id}">${venStatus.getI10n('value')}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('curatoryGroup')}">
                <div class="field">
                    <label for="curatoryGroup">${message(code: 'package.curatoryGroup.label')}</label>
                    <g:select class="ui fluid search select dropdown" name="curatoryGroup"
                              from="${curatoryGroups.sort{it.name.toLowerCase()}}"
                              optionKey="name"
                              optionValue="name"
                              value="${params.curatoryGroup}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('curatoryGroupType')}">
                <div class="field">
                    <label for="curatoryGroupType">${message(code: 'package.curatoryGroup.type')}</label>
                    <g:select class="ui fluid search select dropdown" name="curatoryGroupType"
                              from="${curatoryGroupTypes}"
                              optionKey="value"
                              optionValue="name"
                              value="${params.curatoryGroupType}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                    />
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subStatus')}">
                <div class="field">
                    <label for="subStatus">${message(code:'subscription.status.label')}</label>
                    <select id="subStatus" name="subStatus" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <option value="${RDStore.GENERIC_NULL_VALUE.id}" <%=Params.getLongList(params, 'subStatus').contains(RDStore.GENERIC_NULL_VALUE.id) ? 'selected="selected"' : '' %>>${message(code:'subscription.status.noSubscription')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="subStatus">
                            <option <%=Params.getLongList(params, 'subStatus').contains(subStatus.id) ? 'selected="selected"' : '' %> value="${subStatus.id}">${subStatus.getI10n('value')}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subPerpetualAccess')}">
                <div class="field">
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkSubPerpetual">${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                                <input id="checkSubPerpetual" name="subPerpetual" type="checkbox" <g:if test="${params.subPerpetual == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('supportedLibrarySystems')}">
                <div class="field">
                    <label for="qp_supportedLibrarySystems">${message(code: 'vendor.ordering.supportedLibrarySystems.label')}</label>
                    <select name="qp_supportedLibrarySystems" id="qp_supportedLibrarySystems" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUPPORTED_LIBRARY_SYSTEM)}" var="sls">
                            <option <%=Params.getLongList(params, 'qp_supportedLibrarySystems').contains(sls.id) ? 'selected=selected"' : ''%> value="${sls.id}">
                                ${sls.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('electronicBillings')}">
                <div class="field">
                    <label for="qp_electronicBillings">${message(code: 'vendor.invoicing.formats.label')}</label>
                    <select name="qp_electronicBillings" id="qp_electronicBillings" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_INVOICING_FORMAT)}" var="invoicingFormat">
                            <option <%=Params.getLongList(params, 'qp_electronicBillings').contains(invoicingFormat.id) ? 'selected=selected"' : ''%> value="${invoicingFormat.id}">
                                ${invoicingFormat.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('invoiceDispatchs')}">
                <div class="field">
                    <label for="qp_invoiceDispatchs">${message(code: 'vendor.invoicing.dispatch.label')}</label>
                    <select name="qp_invoiceDispatchs" id="qp_invoiceDispatchs" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_INVOICING_DISPATCH)}" var="invoiceDispatch">
                            <option <%=Params.getLongList(params, 'qp_invoiceDispatchs').contains(invoiceDispatch.id) ? 'selected=selected"' : ''%> value="${invoiceDispatch.id}">
                                ${invoiceDispatch.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('providers')}">
                <div class="field">
                    <label for="qp_providers">${message(code: 'provider.plural')}</label>
                    <select name="qp_providers" id="qp_providers" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${Provider.findAllByStatusNotEqual(RDStore.PROVIDER_STATUS_DELETED).sort {it.name}}" var="provider">
                            <option <%=Params.getLongList(params, 'qp_providers').contains(provider.id) ? 'selected=selected"' : ''%> value="${provider.id}">
                                ${provider.name}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('hasSubscription')}">
                <div class="field">
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkHasSubscription">${message(code: 'surveyEvaluation.filter.hasSubscription')}</label>
                                <input id="checkHasSubscription" name="hasSubscription" type="checkbox" <g:if test="${params.hasSubscription == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkHasNotSubscription">${message(code: 'surveyEvaluation.filter.hasNotSubscription')}</label>
                                <input id="checkHasNotSubscription" name="hasNotSubscription" type="checkbox" <g:if test="${params.hasNotSubscription == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${field.equals('')}">
                <div class="field"></div>
            </g:if>

        </g:each>
    <g:if test="${numberOfFields > 1}">
        </div><!-- .fields -->
    </g:if>

</g:each>

<div class="field la-field-right-aligned">

    <g:if test="${surveyConfig && participant && controllerName == 'survey'}">
        <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: params.viewTab]}"/>
        <g:set var="participant" value="${participant}"/>
    </g:if>
    <g:elseif test="${surveyConfig}">
        <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, viewTab: params.viewTab]}"/>
    </g:elseif>

    <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="${parame}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</g:link>

        <input name="filterSet" type="hidden" value="true">
        <g:if test="${tmplConfigFormFilter}">
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="${Btn.PRIMARY}" onclick="JSPC.app.formFilter(event)" />
            <laser:script file="${this.getGroovyPageFileName()}">
                JSPC.app.formFilter = function (e) {
                    e.preventDefault()

                    var form = $(e.target).parents('form')
                    $(form).find(':input').filter(function () {
                        return !this.value
                    }).attr('disabled', 'disabled')

                    form.submit()
                }
            </laser:script>
        </g:if>
        <g:else>
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="${Btn.PRIMARY}"/>
        </g:else>

</div>


