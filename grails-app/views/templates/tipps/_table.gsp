<%@ page import="de.laser.ApiSource; de.laser.Platform; de.laser.titles.BookInstance" %>

<table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
    <thead>
    <tr>
        <th></th>
        <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortName"
                          title="${message(code: 'title.label')}"/>
        <th class="two wide">${message(code: 'tipp.coverage')}</th>
        <th class="two wide">${message(code: 'tipp.access')}</th>
        <th class="two wide">${message(code: 'tipp.price')}</th>
    </tr>
    <tr>
        <th colspan="2" rowspan="2"></th>
        <th>${message(code: 'default.from')}</th>
        <th>${message(code: 'default.from')}</th>
    </tr>
    <tr>
        <th>${message(code: 'default.to')}</th>
        <th>${message(code: 'default.to')}</th>
    </tr>
    </thead>
    <tbody>

    <g:set var="counter" value="${(offset ?: 0) + 1}"/>
    <g:each in="${tipps}" var="tipp">
        <tr>
            <td>${counter++}</td>
            <td>
                <!-- START TEMPLATE -->
                <g:render template="/templates/title"
                          model="${[ie: null, tipp: tipp, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true),
                                    showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false]}"/>
                <!-- END TEMPLATE -->
            </td>

            <td class="la-tableCard">
                <g:if test="${tipp.titleType.contains('Book')}">
                    <div class="ui card">
                        <div class="content">
                            <!-- von -->
                            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${tipp.dateFirstInPrint}"/>
                            <semui:dateDevider/>
                            <!-- bis -->
                            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${tipp.dateFirstOnline}"/>
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <div class="ui cards">
                        <g:each in="${tipp.coverages}" var="coverage">
                            <div class="ui card">
                                <div class="content">
                                    <div class="la-card-column">
                                        <!-- von -->
                                        <g:formatDate date="${coverage.startDate}"
                                                      format="${message(code: 'default.date.format.notime')}"/><br/>
                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'tipp.volume')}"></i>
                                        ${coverage.startVolume}<br/>
                                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'tipp.issue')}"></i>
                                        ${coverage.startIssue}
                                        <semui:dateDevider/>
                                        <!-- bis -->
                                        <g:formatDate date="${coverage.endDate}"
                                                      format="${message(code: 'default.date.format.notime')}"/><br/>
                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'tipp.volume')}"></i>
                                        ${coverage.endVolume}<br/>
                                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'tipp.issue')}"></i>
                                        ${coverage.endIssue}
                                    </div>

                                    <div class="la-card-column-with-row">
                                        <div class="la-card-row">
                                            <i class="grey icon file alternate right la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.coverageDepth')}"></i>${coverage.coverageDepth}<br/>
                                            <i class="grey icon quote right la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.coverageNote')}"></i>${coverage.coverageNote}<br/>
                                            <i class="grey icon hand paper right la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.embargo')}"></i>${coverage.embargo}<br/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </g:each>
                    </div>
                </g:else>

            </td>
            <td>
                <!-- von -->
                <g:formatDate date="${tipp.accessStartDate}" format="${message(code: 'default.date.format.notime')}"/>
                <semui:dateDevider/>
                <!-- bis -->
                <g:formatDate date="${tipp.accessEndDate}" format="${message(code: 'default.date.format.notime')}"/>
            </td>
            <td>
                <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                    <g:message code="tipp.listPrice"/>: <semui:xEditable field="listPrice"
                                                                         owner="${priceItem}"
                                                                         format=""/> <semui:xEditableRefData
                        field="listCurrency" owner="${priceItem}"
                        config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                    <semui:xEditable field="startDate" type="date"
                                     owner="${priceItem}"/><semui:dateDevider/><semui:xEditable
                        field="endDate" type="date"
                        owner="${priceItem}"/>  <%--<g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                    <g:if test="${i < tipp.priceItems.size() - 1}"><hr></g:if>
                </g:each>
            </td>
        </tr>

    </g:each>

    </tbody>

</table>