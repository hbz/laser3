<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.helper.RDStore; de.laser.ApiSource" %>
<laser:serviceInjection/>
<semui:form>
    <table class="ui selectable celled table la-table la-ignore-fixed">
        <thead>
        <tr>
            <th rowspan="2">${message(code: 'default.compare.title')}</th>
            <g:each in="${objects}" var="object">
                <th colspan="3">
                    <g:if test="${object}"><g:link
                            controller="${object.getClass().getSimpleName().toLowerCase()}" action="show"
                            id="${object.id}">${object.dropdownNamingConvention()}</g:link></g:if>
                </th>
            </g:each>
        </tr>
        <tr>
            <g:each in="${objects}" var="object">
                <th>${message(code: 'subscription.details.coverage_dates')}</th>
                <th>${message(code: 'subscription.details.access_dates')}</th>
                <th>${message(code: 'tipp.price')}</th></g:each>
        </tr>
        </thead>
        <tbody>
        <g:each in="${ies}" var="ie">
            <%
                TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) genericOIDService.resolveOID(ie.getKey())
            %>
            <tr>
                <td>
                    <semui:listIcon type="${tipp.title.class.name}"/>
                    <strong><g:link controller="title" action="show"
                                    id="${tipp.title.id}">${tipp.title.title}</g:link></strong>

                    <g:if test="${tipp.hostPlatformURL}">
                        <semui:linkIcon
                                href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
                    </g:if>
                    <br />

                    <div class="la-icon-list">
                        <g:if test="${tipp.title instanceof BookInstance && tipp.title.volume}">
                            <div class="item">
                                <i class="grey icon la-books la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.volume')}"></i>

                                <div class="content">
                                    ${tipp.title.volume}
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${tipp.title instanceof BookInstance && (tipp.title.firstAuthor || tipp.title.firstEditor)}">
                            <div class="item">
                                <i class="grey icon user circle la-popup-tooltip la-delay"
                                   data-content="${message(code: 'author.slash.editor')}"></i>

                                <div class="content">
                                    ${tipp.title.getEbookFirstAutorOrFirstEditor()}
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${tipp.title instanceof BookInstance && tipp.title.editionStatement}">
                            <div class="item">
                                <i class="grey icon copy la-popup-tooltip la-delay"
                                   data-content="${message(code: 'title.editionStatement.label')}"></i>

                                <div class="content">
                                    ${tipp.title.editionStatement}
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${tipp.title instanceof BookInstance && tipp.title.summaryOfContent}">
                            <div class="item">
                                <i class="grey icon desktop la-popup-tooltip la-delay"
                                   data-content="${message(code: 'title.summaryOfContent.label')}"></i>

                                <div class="content">
                                    ${tipp.title.summaryOfContent}
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${tipp.title.seriesName}">
                            <div class="item">
                                <i class="grey icon list la-popup-tooltip la-delay"
                                   data-content="${message(code: 'title.seriesName.label')}"></i>

                                <div class="content">
                                    ${tipp.title.seriesName}
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${tipp.title.subjectReference}">
                            <div class="item">
                                <i class="grey icon comment alternate la-popup-tooltip la-delay"
                                   data-content="${message(code: 'title.subjectReference.label')}"></i>

                                <div class="content">
                                    ${tipp.title.subjectReference}
                                </div>
                            </div>
                        </g:if>

                    </div>

                    <g:each in="${tipp.title.ids?.sort { it.ns.ns }}" var="id">
                        <span class="ui small blue image label">
                            ${id.ns.ns}: <div class="detail">${id.value}</div>
                        </span>
                    </g:each>

                    <div class="la-icon-list">

                        <g:if test="${tipp.status.getI10n("value")}">
                            <div class="item">
                                <i class="grey key icon la-popup-tooltip la-delay"
                                   data-content="${message(code: 'default.status.label')}"></i>

                                <div class="content">
                                    ${tipp.status.getI10n("value")}
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${showPackage}">
                            <div class="item">
                                <i class="grey icon gift scale la-popup-tooltip la-delay"
                                   data-content="${message(code: 'package.label')}"></i>

                                <div class="content">
                                    <g:link controller="package" action="show"
                                            id="${tipp.pkg.id}">${tipp.pkg.name}</g:link>
                                </div>
                            </div>
                        </g:if>
                        <g:if test="${showPlattform}">
                            <div class="item">
                                <i class="grey icon cloud la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.tooltip.changePlattform')}"></i>

                                <div class="content">
                                    <g:if test="${tipp.platform.name}">
                                        <g:link controller="platform" action="show" id="${tipp.platform.id}">
                                            ${tipp.platform.name}
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        ${message(code: 'default.unknown')}
                                    </g:else>
                                </div>
                            </div>
                        </g:if>

                        <g:if test="${tipp.id}">
                            <div class="la-title">${message(code: 'default.details.label')}</div>
                            <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                    data-content="${message(code: 'laser')}"
                                    href="${tipp.hostPlatformURL.contains('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"
                                    target="_blank"
                                    controller="tipp" action="show"
                                    id="${tipp.id}">
                                <i class="book icon"></i>
                            </g:link>
                        </g:if>
                        <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                                var="gokbAPI">
                            <g:if test="${tipp.gokbId}">
                                <a role="button"
                                   class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                   data-content="${message(code: 'gokb')}"
                                   href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp.gokbId : '#'}"
                                   target="_blank"><i class="la-gokb  icon"></i>
                                </a>
                            </g:if>
                        </g:each>

                    </div>
                </td>
                <g:each in="${objects}" var="object">
                    <g:set var="ieValues" value="${ie.getValue()}"/>
                    <g:if test="${ieValues.containsKey(object)}">
                            <g:each var="ieValue" in="${ieValues.get(object)}">
                                <td class="coverageStatements la-tableCard" >
                                    <g:if test="${ieValue.tipp.title instanceof BookInstance}">

                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ieValue.tipp.title.dateFirstInPrint}"/>
                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ieValue.tipp.title.dateFirstOnline}"/>

                                    </g:if>
                                    <g:elseif test="${ieValue.tipp.title instanceof JournalInstance}">
                                        <div class="ui cards">
                                            <g:each in="${ieValue.coverages}" var="covStmt">
                                                <div class="ui card">
                                                    <g:render template="/templates/tipps/coverageStatement" model="${[covStmt: covStmt]}"/>
                                                </div>
                                            </g:each>
                                        </div>
                                    </g:elseif>
                                </td>
                                <td>
                                    <!-- von --->
                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ieValue.accessStartDate}"/>
                                    <semui:dateDevider/>
                                    <!-- bis -->
                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ieValue.accessEndDate}"/>
                                </td>
                                <td>
                                <g:if test="${ieValue.priceItem}">
                                    <g:message code="tipp.listPrice"/>: <g:formatNumber number="${ieValue.priceItem.listPrice}" type="currency"
                                                    currencySymbol="${ieValue.priceItem.listCurrency}"
                                                    currencyCode="${ieValue.priceItem.listCurrency}"/>
                                    <br />
                                    <g:message code="tipp.localPrice"/>: <g:formatNumber number="${ieValue.priceItem.localPrice}" type="currency"
                                                    currencySymbol="${ieValue.priceItem.localCurrency}"
                                                    currencyCode="${ieValue.priceItem.localCurrency}"/><br />
                                    (<g:message code="tipp.priceDate"/> <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ieValue.priceItem.priceDate}"/>)

                                </g:if>
                                </td>
                            </g:each>
                    </g:if><g:else>
                    <td class="center aligned" colspan="3">
                        <a class="ui circular label la-popup-tooltip la-delay"
                           data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                    </td>
                </g:else>
                </g:each>
            </tr>
        </g:each></tbody>
    </table>
</semui:form>
