<%@ page import="de.laser.CustomerTypeService; de.laser.OrgRole; grails.converters.JSON;de.laser.storage.RDStore" %>
<laser:htmlStart message="myinst.financeImport.post.title" serviceInjection="true"/>

        <ui:breadcrumbs>
            <ui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <ui:crumb message="menu.institutions.financeImport" class="active"/>
        </ui:breadcrumbs>
        <br />
        <ui:messages data="${flash}" />
        <h2 class="ui header">${message(code:'myinst.financeImport.post.header2')}</h2>
        <h3 class="ui header">${message(code:'myinst.financeImport.post.header3')}</h3>
        <g:form name="costItemParameter" action="importCostItems" controller="finance" method="post">
            <g:hiddenField name="candidates" value="${candidates.keySet() as JSON}"/>
            <g:hiddenField name="budgetCodes" value="${budgetCodes as JSON}"/>
            <table class="ui striped table">
                <thead>
                    <tr>
                        <th rowspan="2">${message(code:'myinst.financeImport.post.costItem')}</th>
                        <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                            <th rowspan="2">${message(code:'myinst.financeImport.post.makeVisibleForSubscribers')}</th>
                        </g:if>
                        <th>${message(code:'myinst.financeImport.post.takeItem')}</th>
                    </tr>
                    <tr>
                        <td>${message(code:'myinst.financeImport.post.takeAllItems')} <g:checkBox name="takeAll"/></td>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${candidates.entrySet()}" var="row" status="r">
                        <tr>
                            <td>
                                <ul>
                                    <g:set var="ci" value="${row.getKey()}"/>
                                    <g:set var="errors" value="${row.getValue()}"/>
                                    <%--li><g:message code="myinst.financeImport.institution"/>: ${ci.owner?.sortname}</li--%>
                                    <li><g:message code="default.subscription.label"/>: <g:if test="${ci.sub}">${ci.sub.refresh()}${ci.sub.dropdownNamingConvention()}</g:if></li>
                                    <li><g:message code="package.label"/>: ${ci.subPkg?.pkg?.name}</li>
                                    <li><g:message code="myinst.financeImport.issueEntitlement"/>: ${ci.issueEntitlement?.tipp?.title?.title}</li>
                                    <li><g:message code="myinst.financeImport.orderNumber"/>: ${ci.order?.orderNumber}</li>
                                    <li><g:message code="myinst.financeImport.invoiceNumber"/>: ${ci.invoice?.invoiceNumber}</li>
                                    <li><g:message code="default.currency.label"/>: ${ci.billingCurrency?.value}</li>
                                    <li><g:message code="myinst.financeImport.element"/>: ${ci.costItemElement?.getI10n('value')}</li>
                                    <li><g:message code="myinst.financeImport.elementSign"/>: ${ci.costItemElementConfiguration?.getI10n('value')}</li>
                                    <li><g:message code="myinst.financeImport.taxType"/> (<g:message code="myinst.financeImport.taxRate"/>): ${ci.taxKey?.taxType?.getI10n('value')} (${ci.taxKey?.taxRate} %)</li>
                                    <li><g:message code="myinst.financeImport.invoiceTotal"/>: ${ci.costInBillingCurrency}</li>
                                    <li><g:message code="myinst.financeImport.value"/>: ${ci.costInLocalCurrency}</li>
                                    <li><g:message code="myinst.financeImport.exchangeRate"/>: ${ci.currencyRate}</li>
                                    <li><g:message code="myinst.financeImport.invoiceDate"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.invoiceDate}"/></li>
                                    <li><g:message code="myinst.financeImport.financialYear"/>: ${ci.financialYear}</li>
                                    <li><g:message code="myinst.financeImport.title"/>: ${ci.costTitle}</li>
                                    <li><g:message code="myinst.financeImport.description"/>: ${ci.costDescription}</li>
                                    <li><g:message code="myinst.financeImport.costItemStatus"/>: ${ci.costItemStatus?.getI10n('value')}</li>
                                    <li><g:message code="myinst.financeImport.referenceCodes"/>: ${ci.reference}</li>
                                    <li><g:message code="myinst.financeImport.budgetCode"/>:
                                        <g:if test="${budgetCodes.containsKey(r)}">
                                            <ul>
                                                <li>${budgetCodes.get(r)}</li>
                                            </ul>
                                        </g:if>
                                    </li>
                                    <li><g:message code="myinst.financeImport.datePaid"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.datePaid}"/></li>
                                    <li><g:message code="myinst.financeImport.dateFrom"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.startDate}"/></li>
                                    <li><g:message code="myinst.financeImport.dateTo"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.endDate}"/></li>
                                    <li>Fehler:
                                        <ul>
                                            <g:each in="${errors}" var="error">
                                                <g:if test="${error.getKey() in criticalErrors}">
                                                    <g:set var="withCriticalErrors" value="true"/>
                                                </g:if>
                                                <li>${message(code:"myinst.financeImport.post.error.${error.getKey()}",args:[error.getValue()])}</li>
                                            </g:each>
                                        </ul>
                                    </li>
                                </ul>
                            </td>
                            <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                                <td>
                                    <g:if test="${ci.sub && OrgRole.executeQuery('select oo from OrgRole oo where oo.org = :org and oo.sub = :sub and oo.roleType = :roleType and oo.sub.instanceOf is not null',[org: ci.owner,sub: ci.sub,roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA])}">
                                        ${message(code:'myinst.financeImport.post.visible')} <input name="visibleForSubscriber${r}" type="radio" value="true"><br />${message(code:'myinst.financeImport.post.notVisible')} <input name="visibleForSubscriber${r}" type="radio" value="false" checked>
                                    </g:if>
                                </td>
                            </g:if>
                            <td>
                                <g:if test="${!withCriticalErrors}">
                                    <g:checkBox name="take${r}" class="ciSelect"/>
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