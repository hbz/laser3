<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 07.06.2019
  Time: 12:27
--%>

<%@ page import="grails.converters.JSON;com.k_int.kbplus.OrgRole;de.laser.helper.RDStore" contentType="text/html;charset=UTF-8" %>
<laser:serviceInjection/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'myinst.financeImport.post.title')}</title>
    </head>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
        <semui:crumb message="menu.institutions.financeImport" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <body>
        <h2>${message(code:'myinst.financeImport.post.header2')}</h2>
        <h3>${message(code:'myinst.financeImport.post.header3')}</h3>
        <g:form name="costItemParameter" action="addCostItems" controller="finance" method="post">
            <g:hiddenField name="candidates" value="${candidates.keySet() as JSON}"/>
            <g:hiddenField name="costItemGroups" value="${costItemGroups as JSON}"/>
            <table>
                <tr>
                    <th>${message(code:'myinst.financeImport.post.costItem')}</th>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <th>${message(code:'myinst.financeImport.post.makeVisibleForSubscribers')}</th>
                    </g:if>
                    <th>${message(code:'myinst.financeImport.post.takeItem')}<br>${message(code:'myinst.financeImport.post.takeAllItems')} <g:checkBox name="takeAll"/></th>
                </tr>
                <g:each in="${candidates.entrySet()}" var="row" status="r">
                    <tr>
                        <td>
                            <ul>
                                <g:set var="ci" value="${row.getKey()}"/>
                                <g:set var="errors" value="${row.getValue()}"/>
                                <li><g:message code="myinst.financeImport.institution"/>: ${ci.owner?.sortname}</li>
                                <li><g:message code="myinst.financeImport.subscription"/>: ${ci.sub?.dropdownNamingConvention()}</li>
                                <li><g:message code="myinst.financeImport.package"/>: ${ci.subPkg?.pkg?.name}</li>
                                <li><g:message code="myinst.financeImport.issueEntitlement"/>: ${ci.issueEntitlement?.tipp?.title?.title}</li>
                                <li><g:message code="myinst.financeImport.orderNumber"/>: ${ci.order?.orderNumber}</li>
                                <li><g:message code="myinst.financeImport.invoiceNumber"/>: ${ci.invoice?.invoiceNumber}</li>
                                <li><g:message code="myinst.financeImport.currency"/>: ${ci.billingCurrency?.value}</li>
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
                                <li><g:message code="myinst.financeImport.referenceCodes"/>: ${ci.reference}</li>
                                <li><g:message code="myinst.financeImport.budgetCode"/>:
                                    <ul>
                                        <g:each in="${costItemGroups.get(ci)}" var="cig">
                                            <li>${cig.budgetCode}</li>
                                        </g:each>
                                    </ul>
                                </li>
                                <li><g:message code="myinst.financeImport.datePaid"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.datePaid}"/></li>
                                <li><g:message code="myinst.financeImport.dateFrom"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.startDate}"/></li>
                                <li><g:message code="myinst.financeImport.dateTo"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.endDate}"/></li>
                                <li>Fehler:
                                    <ul>
                                        <g:each in="${errors}" var="error">
                                            <li>${message(code:"myinst.financeImport.post.error.${error.getKey()}",args:[error.getValue()])}</li>
                                        </g:each>
                                    </ul>
                                </li>
                            </ul>
                        </td>
                        <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                            <td>
                                <g:if test="${ci.sub && OrgRole.executeQuery('select oo from OrgRole oo where oo.org = :org and oo.sub = :sub and oo.roleType = :roleType and oo.sub.instanceOf is not null',[org: ci.owner,sub: ci.sub,roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA])}">
                                    ${message(code:'myinst.financeImport.post.visible')} <input name="visibleForSubscriber${r}" type="radio" value="true"><br>${message(code:'myinst.financeImport.post.notVisible')} <input name="visibleForSubscriber${r}" type="radio" value="false" checked>
                                </g:if>
                            </td>
                        </g:if>
                        <td>
                            <g:checkBox name="take${r}" class="ciSelect"/>
                        </td>
                    </tr>
                </g:each>
            </table>
            <input type="submit" class="ui button primary" value="${message(code:'default.button.save.label')}">
            <g:link action="financeImport" class="ui button"><g:message code="default.button.back"/></g:link>
        </g:form>
    </body>
    <r:script>
        $(document).ready(function() {
            $("#takeAll").change(function(){
                if($(this).is(":checked")) {
                    $(".ciSelect").check();
                }
                else {
                    $(".ciSelect").uncheck();
                }
            });
        });
    </r:script>
</html>