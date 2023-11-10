<%@ page import="de.laser.CustomerTypeService; de.laser.OrgRole; grails.converters.JSON;de.laser.storage.RDStore;de.laser.finance.CostItem" %>
<laser:htmlStart message="myinst.financeImport.post.title" serviceInjection="true"/>

        <ui:breadcrumbs>
            <ui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <ui:crumb message="menu.institutions.financeImport" class="active"/>
        </ui:breadcrumbs>
        <br />
        <ui:messages data="${flash}" />
        <h2 class="ui header">${message(code:'myinst.financeImport.post.header2')}</h2>
        <g:form name="costItemParameter" action="importCostItems" controller="finance" method="post">
            <g:hiddenField name="subId" value="${subId}"/>
            <g:hiddenField name="candidates" value="${candidates.keySet() as JSON}"/>
            <g:hiddenField name="budgetCodes" value="${budgetCodes as JSON}"/>
            <table class="ui table celled">
                <thead>
                    <tr>
                        <g:each in="${headerRow}" var="headerCol">
                            <th>${headerCol}</th>
                        </g:each>
                        <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                            <th>${message(code:'myinst.financeImport.post.makeVisibleForSubscribers')}<br>${message(code:'myinst.financeImport.post.all.visible')} <g:checkBox name="allVisibleForSubscriber" checked="true"/></th>
                        </g:if>
                        <th>${message(code:'myinst.financeImport.post.takeItem')}<br>${message(code:'myinst.financeImport.post.takeAllItems')} <g:checkBox name="takeAll" checked="true"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${candidates.entrySet()}" var="row" status="r">
                        <g:set var="ci" value="${row.getKey()}"/>
                        <g:set var="errors" value="${row.getValue()}"/>
                        <tr>
                            <g:each in="${headerRow}" var="headerCol">
                                <td>
                                    <g:if test="${headerCol.toLowerCase().trim() in ["bezeichnung", "title"]}">
                                        <g:message code="myinst.financeImport.title"/>: ${ci.costTitle}
                                    </g:if>
                                    <g:elseif test="${headerCol.toLowerCase().trim() == "element"}">
                                        <g:message code="myinst.financeImport.element"/>: ${ci.costItemElement?.getI10n('value')}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["kostenvorzeichen", "cost item sign"]}">
                                        <g:message code="myinst.financeImport.elementSign"/>: ${ci.costItemElementConfiguration?.getI10n('value')}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() == "budgetcode"}">
                                        <g:message code="myinst.financeImport.budgetCode"/>:
                                        <g:if test="${budgetCodes.containsKey(r)}">
                                            <ul>
                                                <li>${budgetCodes.get(r)}</li>
                                            </ul>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["referenz/codes", "reference/codes"]}">
                                        <g:message code="myinst.financeImport.referenceCodes"/>: ${ci.reference}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() == "status"}">
                                        <g:message code="myinst.financeImport.costItemStatus"/>: ${ci.costItemStatus?.getI10n('value')}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["rechnungssumme", "invoice total"]}">
                                        <g:message code="myinst.financeImport.invoiceTotal"/>: ${ci.costInBillingCurrency}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["währung", "waehrung", "currency"]}">
                                        <g:message code="default.currency.label"/>: ${ci.billingCurrency?.value}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["umrechnungsfaktor", "exchange rate"]}">
                                        <g:message code="myinst.financeImport.exchangeRate"/>: ${ci.currencyRate}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["steuerbar", "tax type"]}">
                                        <g:message code="myinst.financeImport.taxType"/>: ${ci.taxKey?.taxType?.getI10n('value')}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["steuersatz", "tax rate"]}">
                                        <g:if test="${ci.taxKey != CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}"><g:message code="myinst.financeImport.taxRate"/> ${ci.taxKey?.taxRate}</g:if>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["wert", "endpreis", "value"]}">
                                        <g:message code="myinst.financeImport.value"/>: ${ci.costInLocalCurrency}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["lizenz", "subscription"]}">
                                        <g:message code="default.subscription.label"/>: <g:if test="${ci.sub}">${ci.sub.refresh()}${ci.sub.dropdownNamingConvention()}</g:if>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["paket", "package"]}">
                                        <g:message code="package.label"/>: ${ci.subPkg?.pkg?.name}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["einzeltitel", "single title"]}">
                                        <g:message code="myinst.financeImport.issueEntitlement"/>: ${ci.issueEntitlement?.tipp?.name}
                                    </g:elseif>
                                    <%-- TODO issue entitlement group! --%>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["gezahlt am", "date paid"]}">
                                        <g:message code="myinst.financeImport.datePaid"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.datePaid}"/>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["haushaltsjahr", "financial year"]}">
                                        <g:message code="myinst.financeImport.financialYear"/>: ${ci.financialYear}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["datum von", "date from"]}">
                                        <g:message code="myinst.financeImport.dateFrom"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.startDate}"/>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["datum bis", "date to"]}">
                                        <g:message code="myinst.financeImport.dateTo"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.endDate}"/>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["rechnungsdatum", "invoice date"]}">
                                        <g:message code="myinst.financeImport.invoiceDate"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.invoiceDate}"/>
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["anmerkung", "description"]}">
                                        <g:message code="myinst.financeImport.description"/>: ${ci.costDescription}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["rechnungsnummer", "invoice number"]}">
                                        <g:message code="myinst.financeImport.invoiceNumber"/>: ${ci.invoice?.invoiceNumber}
                                    </g:elseif>
                                    <g:elseif test="${headerCol.toLowerCase().trim() in ["auftragsnummer", "order number"]}">
                                        <g:message code="myinst.financeImport.orderNumber"/>: ${ci.order?.orderNumber}
                                    </g:elseif>
                                </td>
                            </g:each>
                            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                <td>
                                    <g:if test="${ci.sub && OrgRole.executeQuery('select oo from OrgRole oo where oo.org = :org and oo.sub = :sub and oo.roleType = :roleType and oo.sub.instanceOf is not null',[org: ci.owner,sub: ci.sub,roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA])}">
                                        ${message(code:'myinst.financeImport.post.visible')} <g:checkBox name="visibleForSubscriber${r}" class="visibleForSubscriber" checked="true"/>
                                    </g:if>
                                </td>
                            </g:if>
                            <td>
                                <%-- TODO: restructure! --%>
                                <g:each in="${errors}" var="error">
                                    <g:if test="${error.getKey() in criticalErrors}">
                                        <g:set var="withCriticalErrors" value="true"/>
                                    </g:if>
                                    <li <g:if test="${withCriticalErrors}">style="color: #BB1600"</g:if>>${message(code:"myinst.financeImport.post.error.${error.getKey()}",args:[error.getValue()])}</li>
                                </g:each>
                                <g:if test="${!withCriticalErrors}">
                                    <g:checkBox name="take${r}" class="ciSelect" checked="true"/>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="3">
                            <input type="submit" class="ui button primary" value="${message(code:'default.button.save.label')}">
                            <g:link action="financeImport" class="ui button"><g:message code="default.button.back"/></g:link>
                        </td>
                    </tr>
                </tfoot>
            </table>
        </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        $("#allVisibleForSubscriber").change(function(){
            if($(this).is(":checked")) {
                $(".visibleForSubscriber").prop('checked',true);
            }
            else {
                $(".visibleForSubscriber").prop('checked',false);
            }
        });

        $("#takeAll").change(function(){
            if($(this).is(":checked")) {
                $(".ciSelect").prop('checked',true);
            }
            else {
                $(".ciSelect").prop('checked',false);
            }
        });
    </laser:script>
<laser:htmlEnd />