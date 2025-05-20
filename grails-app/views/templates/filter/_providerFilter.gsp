<%@ page import="de.laser.wekb.InvoicingVendor; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.utils.LocaleUtils; de.laser.I10nTranslation; de.laser.*; de.laser.auth.Role; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.storage.RDStore" %>

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
                        <g:message code="org.search.provider.contains"/>
                        <span data-position="right center" data-variation="tiny" class="la-popup-tooltip" data-content="${message(code:'org.search.provider.contains.tooltip')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </label>
                    <input type="text" id="nameContains" name="nameContains"
                           placeholder="${message(code:'default.search.ph')}"
                           value="${params.nameContains}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('identifier')}">
                <div class="field">
                    <label for="identifier">
                        ${message(code: 'default.search.identifier')}
                    </label>
                    <div class="ui input">
                        <input type="text" id="identifier" name="identifier"
                               placeholder="${message(code: 'default.search.identifier.ph')}"
                               value="${params.identifier}"/>
                    </div>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('isMyX')}">
                <div class="field">
                    <label for="isMyX">
                        <g:message code="filter.isMyX.label" />
                    </label>
                    <%
                        List<Map> isMyXOptions = []
                        if (actionName == 'list') {
                            isMyXOptions.add([ id: 'wekb_exclusive',    value: "${message(code:'filter.wekb.exclusive')}" ])
                            isMyXOptions.add([ id: 'wekb_not',          value: "${message(code:'filter.wekb.not')}" ])
                            isMyXOptions.add([ id: 'ismyx_exclusive',   value: "${message(code:'filter.isMyX.exclusive', args:["${message(code:'menu.my.providers')}"])}" ])
                            isMyXOptions.add([ id: 'ismyx_not',         value: "${message(code:'filter.isMyX.not')}" ])
                        }
                        else if (actionName == 'currentProviders') {
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

            <g:if test="${field.equalsIgnoreCase('provStatus')}">
                <div class="field">
                    <label for="provStatus">${message(code: 'default.status.label')}</label>
                    <g:if test="${provStatusSet == null || provStatusSet.isEmpty()}">
                        <g:set var="provStatusSet" value="${RefdataCategory.getAllRefdataValues([RDConstants.PROVIDER_STATUS])-RDStore.PROVIDER_STATUS_REMOVED}" scope="request"/>
                    </g:if>
                    <select id="provStatus" name="provStatus" multiple="" class="ui selection fluid dropdown">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${provStatusSet}" var="provStatus">
                            <option <%=Params.getLongList(params, 'provStatus').contains(provStatus.id) ? 'selected="selected"' : ''%> value="${provStatus.id}">${provStatus.getI10n('value')}</option>
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

            <g:if test="${field.equalsIgnoreCase('inhouseInvoicing')}">
                <div class="field">
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkInhouseInvoicing">${message(code: 'vendor.invoicing.inhouse.label')}</label>
                                <input id="checkInhouseInvoicing" name="inhouseInvoicing" type="checkbox" <g:if test="${params.inhouseInvoicing == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
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

            <g:if test="${field.equalsIgnoreCase('invoicingVendors')}">
                <div class="field">
                    <label for="qp_invoicingVendors">${message(code: 'vendor.invoicing.vendors.label')}</label>
                    <select name="qp_invoicingVendors" id="qp_invoicingVendors" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${InvoicingVendor.executeQuery('select distinct(v) from InvoicingVendor iv join iv.vendor v order by v.name')}" var="invoicingVendors">
                            <option <%=Params.getLongList(params, 'qp_invoicingVendors').contains(invoicingVendors.id) ? 'selected=selected"' : ''%> value="${invoicingVendors.id}">
                                ${invoicingVendors.name}
                            </option>
                        </g:each>
                    </select>
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

        <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>

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


