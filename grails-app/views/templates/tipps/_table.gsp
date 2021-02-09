<%@ page import="de.laser.ApiSource; de.laser.Platform; de.laser.titles.BookInstance" %>

<table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
    <thead>
    <tr>
        <th></th>
        <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortName"
                          title="${message(code: 'title.label')}"/>
        <th class="two wide">${message(code: 'tipp.access')}</th>
        <th class="two wide">${message(code: 'tipp.coverage')}</th>
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
                <semui:listIcon type="${tipp.medium?.value}"/>
                <strong><g:link controller="tipp" action="show"
                                id="${tipp.id}">${tipp.name}</g:link></strong>

                <g:if test="${tipp.hostPlatformURL}">
                    <semui:linkIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
                </g:if>
                <br />
                <div class="la-icon-list">
                    <g:if test="${tipp.titleType.contains('Book') && tipp.volume}">
                        <div class="item">
                            <i class="grey icon la-books la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                            <div class="content">
                                ${tipp.volume}
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${tipp.titleType.contains('Book') && (tipp.firstAuthor || tipp.firstEditor)}">
                        <div class="item">
                            <i class="grey icon user circle la-popup-tooltip la-delay" data-content="${message(code: 'author.slash.editor')}"></i>
                            <div class="content">
                                ${tipp.getEbookFirstAutorOrFirstEditor()}
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${tipp.titleType.contains('Book') && tipp.editionStatement}">
                        <div class="item">
                            <i class="grey icon copy la-popup-tooltip la-delay" data-content="${message(code: 'title.editionStatement.label')}"></i>
                            <div class="content">
                                ${tipp.editionStatement}
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${tipp.titleType.contains('Book') && tipp.summaryOfContent}">
                        <div class="item">
                            <i class="grey icon desktop la-popup-tooltip la-delay" data-content="${message(code: 'title.summaryOfContent.label')}"></i>
                            <div class="content">
                                ${tipp.summaryOfContent}
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${tipp.seriesName}">
                        <div class="item">
                            <i class="grey icon list la-popup-tooltip la-delay" data-content="${message(code: 'title.seriesName.label')}"></i>
                            <div class="content">
                                ${tipp.seriesName}
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${tipp.subjectReference}">
                        <div class="item">
                            <i class="grey icon comment alternate la-popup-tooltip la-delay" data-content="${message(code: 'title.subjectReference.label')}"></i>
                            <div class="content">
                                ${tipp.subjectReference}
                            </div>
                        </div>
                    </g:if>

                </div>

                <g:each in="${tipp.ids?.sort { it.ns.ns }}" var="id">
                    <span class="ui small basic image label">
                        ${id.ns.ns}: <div class="detail">${id.value}</div>
                    </span>
                </g:each>

                <div class="la-icon-list">

                    %{--<g:if test="${tipp.availabilityStatus?.getI10n('value')}">
                        <div class="item">
                            <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.access.label')}"></i>
                            <div class="content">
                                ${tipp.availabilityStatus?.getI10n('value')}
                            </div>
                        </div>
                    </g:if>--}%

                    <g:if test="${tipp.status.getI10n("value")}">
                        <div class="item">
                            <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.status.label')}"></i>
                            <div class="content">
                                ${tipp.status.getI10n("value")}
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${showPackage}">
                        <div class="item">
                            <i class="grey icon gift scale la-popup-tooltip la-delay" data-content="${message(code: 'package.label')}"></i>
                            <div class="content">
                                <g:link controller="package" action="show"
                                        id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${showPlattform}">
                        <div class="item">
                            <i class="grey icon cloud la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.changePlattform')}"></i>
                            <div class="content">
                                <g:if test="${tipp?.platform.name}">
                                    <g:link controller="platform" action="show" id="${tipp?.platform.id}">
                                        ${tipp?.platform.name}
                                    </g:link>
                                </g:if>
                                <g:else>
                                    ${message(code: 'default.unknown')}
                                </g:else>
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${tipp?.id}">
                        <div class="la-title">${message(code: 'default.details.label')}</div>
                        <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                data-content="${message(code: 'laser')}"
                                href="${tipp?.hostPlatformURL.contains('http') ? tipp?.hostPlatformURL : 'http://' + tipp?.hostPlatformURL}"
                                target="_blank"
                                controller="tipp" action="show"
                                id="${tipp?.id}">
                            <i class="book icon"></i>
                        </g:link>
                    </g:if>
                    <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                            var="gokbAPI">
                        <g:if test="${tipp?.gokbId}">
                            <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                               data-content="${message(code: 'gokb')}"
                               href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/resource/show/?id=' + tipp.gokbId : '#'}"
                               target="_blank"><i class="la-gokb  icon"></i>
                            </a>
                        </g:if>
                    </g:each>

                </div>
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
                                        <g:formatDate date="${coverage.startDate}" format="${message(code:'default.date.format.notime')}"/><br />
                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                                        ${coverage.startVolume}<br />
                                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                                        ${coverage.startIssue}
                                        <semui:dateDevider/>
                                        <!-- bis -->
                                        <g:formatDate date="${coverage.endDate}" format="${message(code:'default.date.format.notime')}"/><br />
                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                                        ${coverage.endVolume}<br />
                                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                                        ${coverage.endIssue}
                                    </div>
                                    <div class="la-card-column-with-row">
                                        <div class="la-card-row">
                                            <i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>${coverage.coverageDepth}<br />
                                            <i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageNote')}"></i>${coverage.coverageNote}<br />
                                            <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>${coverage.embargo}<br />
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
                <g:formatDate date="${tipp.accessStartDate}" format="${message(code:'default.date.format.notime')}"/>
                <semui:dateDevider/>
                <!-- bis -->
                <g:formatDate date="${tipp.accessEndDate}" format="${message(code:'default.date.format.notime')}"/>
            </td>
        </tr>

    </g:each>

    </tbody>

</table>