<%@ page import="com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.Package; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.ApiSource;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyShow.label')}</title>

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
<br>
<g:link controller="subscription" class="ui button" action="index" id="${surveyConfig?.subscription?.id}">
    ${surveyConfig?.subscription?.name}
</g:link>
</div>

</div>


<div class="row">
<div class="column">

    <g:if test="${entitlements?.size() > 0}">
        ${message(code: 'subscription.entitlement.plural')} ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (entitlements?.size())), num_sub_rows])}.     </g:if>
    <g:else>
        ${message(code: 'subscription.details.no_ents', default: 'No entitlements yet')}
    </g:else>
    <g:set var="counter" value="${offset + 1}"/>

    <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header">
        <thead>
        <tr>

            <th>${message(code: 'sidewide.number')}</th>
            <g:sortableColumn class="eight wide" params="${params}" property="tipp.title.sortTitle"
                              title="${message(code: 'title.label', default: 'Title')}"/>
            <th class="one wide">${message(code: 'subscription.details.print-electronic')}</th>
            <th class="four wide">${message(code: 'subscription.details.coverage_dates')}</th>
            <th class="two wide">${message(code: 'subscription.details.access_dates')}</th>
            <th class="two wide"><g:message code="subscription.details.prices"/></th>
            <th class="one wide"></th>
        </tr>
        <tr>
            <th rowspan="2" colspan="4"></th>
            <g:sortableColumn class="la-smaller-table-head" params="${params}" property="startDate"
                              title="${message(code: 'default.from', default: 'Earliest date')}"/>
            <g:sortableColumn class="la-smaller-table-head" params="${params}"
                              property="accessStartDate"
                              title="${message(code: 'default.from', default: 'Earliest date')}"/>

            <th rowspan="2" colspan="2"></th>
        </tr>
        <tr>
            <g:sortableColumn class="la-smaller-table-head" property="endDate"
                              title="${message(code: 'default.to', default: 'Latest Date')}"/>
            <g:sortableColumn class="la-smaller-table-head" params="${params}"
                              property="accessEndDate"
                              title="${message(code: 'default.to', default: 'Latest Date')}"/>
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
                        <semui:listIcon type="${ie.tipp?.title?.type?.value}"/>
                        <g:link controller="issueEntitlement" id="${ie.id}"
                                action="show"><strong>${ie.tipp?.title.title}</strong>
                        </g:link>
                        <g:if test="${ie.tipp?.hostPlatformURL}">
                            <a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                            <%-- data-content="${message(code: 'tipp.tooltip.callUrl')}" --%>
                               data-content="${ie.tipp?.platform.name}"

                               href="${ie.tipp?.hostPlatformURL.contains('http') ? ie.tipp?.hostPlatformURL : 'http://' + ie.tipp?.hostPlatformURL}"
                               target="_blank"><i class="cloud icon"></i></a>
                        </g:if>
                        <br>
                        <!-- START TEMPLATE -->

                        <g:render template="../templates/title"
                                  model="${[item: ie, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]}"/>
                        <!-- END TEMPLATE -->
                    </td>

                    <td>
                        <semui:xEditableRefData owner="${ie}" field="medium" config='IEMedium'
                                                overwriteEditable="${false}"/>
                    </td>
                    <td class="coverageStatements la-tableCard" data-entitlement="${ie.id}">
                        <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance}">

                            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${ie?.tipp?.title?.dateFirstInPrint}"/>
                            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${ie?.tipp?.title?.dateFirstOnline}"/>

                        </g:if>
                        <g:else>
                            <div class="ui cards">
                                <g:each in="${ie.coverages}" var="covStmt">
                                    <div class="ui card">
                                        <g:render template="/templates/tipps/coverageStatement"
                                                  model="${[covStmt: covStmt]}"/>
                                    </div>
                                </g:each>
                            </div><br>
                            <g:link action="addCoverage" params="${[issueEntitlement: ie.id]}"
                                    class="ui compact icon button positive tiny"><i
                                    class="ui icon plus"
                                    data-content="Lizenzzeitraum hinzufügen"></i></g:link>
                        </g:else>

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
                        <g:if test="${ie.priceItem}">
                            <g:message code="tipp.listPrice"/>: <semui:xEditable field="listPrice"
                                                                                 owner="${ie.priceItem}"
                                                                                 overwriteEditable="${false}"/>
                            <semui:xEditableRefData
                                    field="listCurrency" owner="${ie.priceItem}" overwriteEditable="${false}"
                                    config="Currency"/> <%--<g:formatNumber number="${ie.priceItem.listPrice}" type="currency" currencyCode="${ie.priceItem.listCurrency.value}" currencySymbol="${ie.priceItem.listCurrency.value}"/>--%><br>
                            <g:message code="tipp.localPrice"/>: <semui:xEditable field="localPrice"
                                                                                  owner="${ie.priceItem}"
                                                                                  overwriteEditable="${false}"/>
                            <semui:xEditableRefData
                                    field="localCurrency" owner="${ie.priceItem}" overwriteEditable="${false}"
                                    config="Currency"/> <%--<g:formatNumber number="${ie.priceItem.localPrice}" type="currency" currencyCode="${ie.priceItem.localCurrency.value}" currencySymbol="${ie.priceItem.listCurrency.value}"/>--%>
                            (<g:message code="tipp.priceDate"/> <semui:xEditable field="priceDate"
                                                                                 type="date"
                                                                                 owner="${ie.priceItem}"
                                                                                 overwriteEditable="${false}"/> <%--<g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.priceItem.priceDate}"/>--%>)
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
        <semui:paginate action="index" controller="subscription" params="${params}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                        total="${num_sub_rows}"/>
    </g:if>

    <div id="magicArea"></div>
    <r:script>
        $(document).ready(function () {
            $('#finishProcess').progress();
        });
    </r:script>

    </body>
    </html>
