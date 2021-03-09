<%@ page import="de.laser.ApiSource; de.laser.SurveyOrg; de.laser.helper.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.titles.BookInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb controller="myInstitution" action="surveyInfosIssueEntitlements"
                 id="${surveyConfig?.id}" message="issueEntitlementsSurvey.label"/>
    <semui:crumb controller="subscription" action="index" id="${subscription.id}"
                 text="${subscription.name}"/>
    <semui:crumb class="active" text="${message(code: 'subscription.details.renewEntitlements.label')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="showEntitlementsRenewWithSurvey" id="${surveyConfig?.id}"
                    params="${[exportKBart: true]}">KBART Export</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="showEntitlementsRenewWithSurvey" id="${surveyConfig?.id}"
                                          params="${[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon type="Survey"/>
<g:message code="issueEntitlementsSurvey.label"/>: ${surveyConfig?.surveyInfo.name}
</h1>


<g:if test="${flash}">
    <semui:messages data="${flash}"/>
</g:if>

<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)?.finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info"/>
            </p>
        </div>
    </div>
</g:if>



<semui:form>
    <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
            id="${surveyConfig?.id}"
            class="ui button">
        <g:message code="surveyInfo.backToSurvey"/>
    </g:link>
    <br />

    <h2 class="ui header left aligned aligned"><g:message
            code="renewEntitlementsWithSurvey.currentEntitlements"/> (${ies.size() ?: 0})</h2>

    <div class="ui grid">
        <div class="sixteen wide column">
            <g:set var="counter" value="${1}"/>
            <g:set var="sumlistPrice" value="${0}"/>
            <g:set var="sumlocalPrice" value="${0}"/>


            <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header">
                <thead>
                <tr>
                    <th>${message(code: 'sidewide.number')}</th>
                    <th><g:message code="title.label"/></th>
                    <th><g:message code="tipp.coverage"/></th>
                    <th class="two wide"><g:message code="tipp.price"/></th>

                </tr>
                </thead>
                <tbody>

                <g:each in="${ies}" var="ie">
                    <g:set var="tipp" value="${ie.tipp}"/>
                    <tr>
                    <td>${counter++}</td>
                    <td class="titleCell">
                        <semui:ieAcceptStatusIcon status="${ie?.acceptStatus}"/>

                        <!-- START TEMPLATE -->
                        <g:render template="/templates/title"
                                  model="${[ie: ie, tipp: tipp, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true),
                                            showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false]}"/>
                        <!-- END TEMPLATE -->
                    </td>
                    <td class="coverageStatements la-tableCard">

                        <g:render template="/templates/tipps/coverages" model="${[ie: ie, tipp: tipp]}"/>

                    </td>
                    <td>
                        <g:if test="${ie.priceItems}">
                            <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                <g:message code="tipp.price.listPrice"/>: <semui:xEditable field="listPrice"
                                                                                     owner="${priceItem}"
                                                                                     format=""/> <semui:xEditableRefData
                                    field="listCurrency" owner="${priceItem}"
                                    config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                <g:message code="tipp.price.localPrice"/>: <semui:xEditable field="localPrice"
                                                                                      owner="${priceItem}"/> <semui:xEditableRefData
                                    field="localCurrency" owner="${priceItem}"
                                    config="Currency"/> <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                                <semui:xEditable field="startDate" type="date"
                                                 owner="${priceItem}"/><semui:dateDevider/><semui:xEditable
                                    field="endDate" type="date"
                                    owner="${priceItem}"/>  <%--<g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                <g:set var="sumlocalPrice" value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                            </g:each>
                        </g:if>
                    </td>

                </g:each>
                </tbody>
                <tfoot>
                <tr>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th><g:message code="financials.export.sums"/> <br />
                        <g:message code="tipp.price.listPrice"/>: <g:formatNumber number="${sumlistPrice}"
                                                                            type="currency"/><br />
                        %{--<g:message code="tipp.price.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
                    </th>
                </tr>
                </tfoot>
            </table>
        </div>

    </div>
    <br />
    <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
            id="${surveyConfig?.id}"
            class="ui button">
        <g:message code="surveyInfo.backToSurvey"/>
    </g:link>
</semui:form>

</body>
</html>
