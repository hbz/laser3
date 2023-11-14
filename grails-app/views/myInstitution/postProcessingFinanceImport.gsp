<%@ page import="de.laser.CustomerTypeService; de.laser.OrgRole; grails.converters.JSON;de.laser.storage.RDStore;de.laser.finance.CostItem" %>
<laser:htmlStart message="myinst.financeImport.post.title" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <ui:crumb message="menu.institutions.financeImport" class="active"/>
</ui:breadcrumbs>
<br />
<ui:messages data="${flash}" />
<h2 class="ui header">${message(code:'myinst.financeImport.post.header2')}</h2>
<g:if test="${token}">
    <div class="errorFileWrapper">
        <g:render template="/subscription/entitlementProcessResult" model="[token: token, errMess: errMess, errorCount: errorCount]"/>
    </div>
</g:if>
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
                        <g:set var="withCriticalErrors" value="${false}"/>
                        <g:set var="ci" value="${row.getKey()}"/>
                        <g:set var="errors" value="${row.getValue()}"/>
                        <tr>
                            <g:each in="${headerRow}" var="headerCol">
                                <%
                                    String tableCell = "", errMess = null
                                    switch(headerCol.toLowerCase().trim()) {
                                        case ["bezeichnung", "title"]: tableCell = ci.costTitle
                                            break
                                        case "element": tableCell = ci.costItemElement?.getI10n('value')
                                            if(errors.containsKey('noValidElement'))
                                                errMess = message(code:"myinst.financeImport.post.error.noValidElement",args:[errors.get('noValidElement')])
                                            break
                                        case ["kostenvorzeichen", "cost item sign"]: tableCell = ci.costItemElementConfiguration?.getI10n('value')
                                            if(errors.containsKey('noValidSign'))
                                                errMess = message(code:"myinst.financeImport.post.error.noValidElement",args:[errors.get('noValidElement')])
                                            break
                                        case "budgetcode":
                                            if(budgetCodes.containsKey(r))
                                                tableCell += budgetCodes.get(r)
                                            break
                                        case ["referenz/codes", "reference/codes"]: tableCell = ci.reference
                                            break
                                        case "status": tableCell = ci.costItemStatus?.getI10n('value')
                                            if(errors.containsKey('noValidStatus'))
                                                errMess = message(code:"myinst.financeImport.post.error.noValidStatus",args:[errors.get('noValidStatus')])
                                            break
                                        case ["rechnungssumme", "invoice total"]: tableCell = ci.costInBillingCurrency
                                            break
                                        case ["währung", "waehrung", "currency"]: tableCell = ci.billingCurrency?.value
                                            if(errors.containsKey('noCurrencyError')) {
                                                errMess = message(code: "myinst.financeImport.post.error.noCurrencyError", args: [errors.get('noCurrencyError')])
                                                withCriticalErrors = true
                                            }
                                            if(errors.containsKey('invalidCurrencyError')) {
                                                errMess = message(code: "myinst.financeImport.post.error.invalidCurrencyError", args: [errors.get('invalidCurrencyError')])
                                                withCriticalErrors = true
                                            }
                                            break
                                        case ["umrechnungsfaktor", "exchange rate"]: tableCell = ci.currencyRate
                                            if(errors.containsKey('exchangeRateInvalid')) {
                                                errMess = message(code: "myinst.financeImport.post.error.exchangeRateInvalid", args: [errors.get('exchangeRateInvalid')])
                                                withCriticalErrors = true
                                            }
                                            if(errors.containsKey('exchangeRateMissing')) {
                                                errMess = message(code: "myinst.financeImport.post.error.exchangeRateMissing", args: [errors.get('exchangeRateMissing')])
                                                withCriticalErrors = true
                                            }
                                            break
                                        case ["steuerbar", "tax type"]: tableCell = ci.taxKey?.taxType?.getI10n('value')
                                            if(errors.containsKey('invalidTaxType')) {
                                                errMess = message(code: "myinst.financeImport.post.error.invalidTaxType", args: [errors.get('invalidTaxType')])
                                                withCriticalErrors = true
                                            }
                                            break
                                        case ["steuersatz", "tax rate"]:
                                            if(ci.taxKey != CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                                tableCell = ci.taxKey?.taxRate
                                            break
                                        case ["wert", "endpreis", "value"]: tableCell = ci.costInLocalCurrency
                                            break
                                        case ["lizenz", "subscription"]:
                                            if(ci.sub) {
                                                ci.sub.refresh()
                                                tableCell = ci.sub.dropdownNamingConvention()
                                            }
                                            if(errors.containsKey('noValidSubscription'))
                                                errMess = message(code:"myinst.financeImport.post.error.noValidSubscription",args:[errors.get('noValidSubscription')])
                                            if(errors.containsKey('multipleSubError'))
                                                errMess = message(code:"myinst.financeImport.post.error.multipleSubError",args:[errors.get('multipleSubError')])
                                            break
                                        case ["paket", "package"]: tableCell = ci.subPkg?.pkg?.name
                                            if(errors.containsKey('packageWithoutSubscription'))
                                                errMess = message(code:"myinst.financeImport.post.error.packageWithoutSubscription",args:[errors.get('packageWithoutSubscription')])
                                            if(errors.containsKey('noValidPackage'))
                                                errMess = message(code:"myinst.financeImport.post.error.noValidPackage",args:[errors.get('noValidPackage')])
                                            if(errors.containsKey('multipleSubPkgError'))
                                                errMess = message(code:"myinst.financeImport.post.error.multipleSubPkgError",args:[errors.get('multipleSubPkgError')])
                                            if(errors.containsKey('packageNotInSubscription'))
                                                errMess = message(code:"myinst.financeImport.post.error.packageNotInSubscription",args:[errors.get('packageNotInSubscription')])
                                            break
                                        //TODO issue entitlement group!
                                        case ["einzeltitel", "single title"]: tableCell = ci.issueEntitlement?.tipp?.name
                                            if(errors.containsKey('entitlementWithoutPackageOrSubscription'))
                                                errMess = message(code:"myinst.financeImport.post.error.entitlementWithoutPackageOrSubscription",args:[errors.get('entitlementWithoutPackageOrSubscription')])
                                            if(errors.containsKey('noValidEntitlement'))
                                                errMess = message(code:"myinst.financeImport.post.error.noValidEntitlement",args:[errors.get('noValidEntitlement')])
                                            if(errors.containsKey('multipleEntitlementError'))
                                                errMess = message(code:"myinst.financeImport.post.error.multipleEntitlementError",args:[errors.get('multipleEntitlementError')])
                                            break
                                        case ["gezahlt am", "date paid"]: tableCell = formatDate(format: message(code:'default.date.format.notime'), date: ci.datePaid)
                                            break
                                        case ["haushaltsjahr", "financial year"]: tableCell = ci.financialYear
                                            if(errors.containsKey('invalidYearFormat'))
                                                errMess = message(code:"myinst.financeImport.post.error.invalidYearFormat",args:[errors.get('invalidYearFormat')])
                                            break
                                        case ["datum von", "date from"]: tableCell = formatDate(format: message(code:'default.date.format.notime'), date: ci.startDate)
                                            break
                                        case ["datum bis", "date to"]: tableCell = formatDate(format: message(code:'default.date.format.notime'), date: ci.endDate)
                                            break
                                        case ["rechnungsdatum", "invoice date"]: tableCell = formatDate(format: message(code:'default.date.format.notime'), date: ci.invoiceDate)
                                            break
                                        case ["anmerkung", "description"]: tableCell = ci.costDescription
                                            break
                                        case ["rechnungsnummer", "invoice number"]: tableCell = ci.invoice?.invoiceNumber
                                            if(errors.containsKey('multipleInvoiceError'))
                                                errMess = message(code:"myinst.financeImport.post.error.multipleInvoiceError",args:[errors.get('multipleInvoiceError')])
                                            break
                                        case ["auftragsnummer", "order number"]: tableCell = ci.order?.orderNumber
                                            if(errors.containsKey('multipleOrderError'))
                                                errMess = message(code:"myinst.financeImport.post.error.multipleOrderError",args:[errors.get('multipleOrderError')])
                                            break
                                    }
                                %>
                                <td <g:if test="${errMess != null}">class="negative"</g:if>>
                                    <g:if test="${errMess != null}">
                                        ${errMess}
                                    </g:if>
                                    ${tableCell}
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