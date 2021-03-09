<%@ page import="de.laser.titles.BookInstance; de.laser.ApiSource; de.laser.Subscription; de.laser.Package; de.laser.RefdataCategory; de.laser.helper.RDStore;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'surveyShow.label')}</title>

</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>

    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>



<div class="sixteen wide column">

    <div class="la-inline-lists">

        <div class="ui icon positive message">
            <i class="info icon"></i>

            <div class="content">
                <div class="header"></div>

                <p>
                    <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                    <g:message code="showSurveyInfo.pickAndChoose.Package"/>
                </p>
                <br/>
                <g:link controller="subscription" class="ui button" action="index" target="_blank"
                        id="${surveyConfig.subscription.id}">
                    ${surveyConfig.subscription.name} (${surveyConfig.subscription.status.getI10n('value')})
                </g:link>

                <g:link controller="subscription" class="ui button" action="linkPackage" target="_blank"
                        id="${surveyConfig.subscription.id}">
                    <g:message code="subscription.details.linkPackage.label"/>
                </g:link>

                <g:link controller="subscription" class="ui button" action="addEntitlements" target="_blank"
                        id="${surveyConfig.subscription.id}">
                    <g:message code="subscription.details.addEntitlements.label"/>
                </g:link>

            </div>

        </div>


        <div class="row">
            <div class="column">
                <g:render template="/templates/filter/tipp_ieFilter"/>
            </div>
        </div>

        <div class="row">
            <div class="column">

                <g:set var="counter" value="${offset + 1}"/>

                <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header">
                    <thead>
                    <tr>

                        <th>${message(code: 'sidewide.number')}</th>
                        <g:sortableColumn class="eight wide" params="${params}" property="tipp.sortName"
                                          title="${message(code: 'title.label')}"/>
                        <th class="one wide">${message(code: 'subscription.details.print-electronic')}</th>
                        <th class="four wide">${message(code: 'subscription.details.coverage_dates')}</th>
                        <th class="two wide">${message(code: 'subscription.details.access_dates')}</th>
                        <th class="two wide"><g:message code="subscription.details.prices"/></th>
                        <th class="one wide"></th>
                    </tr>
                    <tr>
                        <th rowspan="2" colspan="3"></th>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="startDate"
                                          title="${message(code: 'default.from')}"/>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}"
                                          property="accessStartDate"
                                          title="${message(code: 'default.from')}"/>

                        <th rowspan="2" colspan="2"></th>
                    </tr>
                    <tr>
                        <g:sortableColumn class="la-smaller-table-head" property="endDate"
                                          title="${message(code: 'default.to')}"/>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}"
                                          property="accessEndDate"
                                          title="${message(code: 'default.to')}"/>
                    </tr>
                    <tr>
                        <th colspan="9"></th>
                    </tr>
                    </thead>
                    <tbody>

                    <g:if test="${entitlements}">

                        <g:each in="${entitlements}" var="ie">
                            <tr>

                                <td>${counter++}</td>
                                <td>
                                    <!-- START TEMPLATE -->
                                    <g:render template="/templates/title"
                                              model="${[ie         : ie, tipp: ie.tipp, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true),
                                                        showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false]}"/>
                                    <!-- END TEMPLATE -->
                                </td>

                                <td>
                                    <semui:xEditableRefData owner="${ie}" field="medium"
                                                            config="${de.laser.helper.RDConstants.IE_MEDIUM}"
                                                            overwriteEditable="${false}"/>
                                </td>
                                <td class="coverageStatements la-tableCard" data-entitlement="${ie.id}">

                                    <g:render template="/templates/tipps/coverages"
                                              model="${[ie: ie, tipp: ie.tipp, overwriteEditable: false]}"/>

                                </td>
                                <td>
                                    <!-- von --->

                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ie.accessStartDate}"/>

                                    <semui:dateDevider/>
                                    <!-- bis -->

                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ie.accessEndDate}"/>

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
                                        </g:each>
                                    </g:if>

                                </td>
                                <td class="x">

                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    </tbody>
                </table>

            </div>
        </div><!--.row-->
    </div>
</div>
<g:if test="${entitlements}">
    <semui:paginate action="surveyTitles" controller="survey" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${num_ies_rows}"/>
</g:if>

<div id="magicArea"></div>
<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
</laser:script>

</body>
</html>
