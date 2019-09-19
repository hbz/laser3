<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform" %>

<table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
    <thead>
    <tr>
        <th></th>
        <g:sortableColumn class="ten wide" params="${params}" property="tipp.title.sortTitle"
                          title="${message(code: 'title.label', default: 'Title')}"/>

        <th class="two wide">${message(code: 'tipp.coverage')}</th>
        <th class="two wide">${message(code: 'tipp.access')}</th>
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
                <semui:listIcon type="${tipp.title?.type?.value}"/>
                <strong><g:link controller="title" action="show"
                                id="${tipp.title.id}">${tipp.title.title}</g:link></strong>

                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && tipp?.title?.volume}">
                    (${message(code: 'title.volume.label')} ${tipp?.title?.volume})
                </g:if>

                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && (tipp?.title?.firstAuthor || tipp?.title?.firstEditor)}">
                    <br><b>${tipp?.title?.getEbookFirstAutorOrFirstEditor()}</b>
                </g:if>

                <br>
                <g:link controller="tipp" action="show"
                        id="${tipp.id}">${message(code: 'platform.show.full_tipp', default: 'Full TIPP Details')}</g:link>
            &nbsp;&nbsp;&nbsp;
                <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                        var="gokbAPI">
                    <g:if test="${tipp?.gokbId}">
                        <a target="_blank"
                           href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}"><i
                                title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                    </g:if>
                </g:each>
                <br>

                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">
                    <div class="item"><b>${message(code: 'title.editionStatement.label')}:</b> ${tipp?.title?.editionStatement}
                    </div>
                </g:if>

                <g:if test="${tipp.hostPlatformURL}">
                    <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.tooltip.callUrl')}"
                       href="${tipp.hostPlatformURL.contains('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"
                       target="_blank"><i class="share square icon"></i></a>
                </g:if>

                <g:each in="${tipp?.title?.ids?.sort { it?.identifier?.ns?.ns }}" var="id">
                    <g:if test="${id.identifier.ns.ns == 'originEditUrl'}">
                        <%--<span class="ui small teal image label">
                            ${id.identifier.ns.ns}: <div class="detail"><a
                                href="${id.identifier.value}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                        </div>
                        </span>
                        <span class="ui small teal image label">
                            ${id.identifier.ns.ns}: <div class="detail"><a
                                href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                        </div>
                        </span>--%>
                    </g:if>
                    <g:else>
                        <span class="ui small teal image label">
                            ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                        </span>
                    </g:else>
                </g:each>


                <div class="ui list">
                    <div class="item"
                         title="${tipp.availabilityStatusExplanation}"><b>${message(code: 'default.access.label', default: 'Access')}:</b> ${tipp.availabilityStatus?.getI10n('value')}
                    </div>


                    <div class="item"><b>${message(code: 'default.status.label', default: 'Status')}:</b>  ${tipp.status.getI10n("value")}</div>

                    <g:if test="${showPackage}">
                        <div class="item"><b>${message(code: 'tipp.package', default: 'Package')}:</b>

                            <div class="la-flexbox">
                                <i class="icon gift scale la-list-icon"></i>
                                <g:link controller="package" action="show"
                                        id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${showPlattform}">
                        <div class="item"><b>${message(code: 'tipp.platform', default: 'Platform')}:</b>
                            <g:if test="${tipp?.platform.name}">
                                ${tipp?.platform.name}
                            </g:if>
                            <g:else>${message(code: 'default.unknown')}</g:else>
                            <g:if test="${tipp?.platform.name}">
                                <g:link class="ui icon mini  button la-url-button la-popup-tooltip la-delay"
                                        data-content="${message(code: 'tipp.tooltip.changePlattform')}"
                                        controller="platform" action="show" id="${tipp?.platform.id}"><i
                                        class="pencil alternate icon"></i></g:link>
                            </g:if>
                            <g:if test="${tipp?.platform?.primaryUrl}">
                                <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.tooltip.callUrl')}"
                                   href="${tipp?.platform?.primaryUrl?.contains('http') ? tipp?.platform?.primaryUrl : 'http://' + tipp?.platform?.primaryUrl}"
                                   target="_blank"><i class="share square icon"></i></a>
                            </g:if>
                        </div>
                    </g:if>

                </div>
            </td>

            <td>

                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">

                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp?.title?.dateFirstInPrint}"/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp?.title?.dateFirstOnline}"/>

                </g:if>
                <g:else>
                    <g:each in="${tipp.coverages}" var="coverage">
                        <!-- von -->
                        <g:formatDate date="${coverage.startDate}" format="${message(code:'default.date.format.notime')}"/><br>
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                        ${coverage.startVolume}<br>
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                        ${coverage.startIssue}
                        <semui:dateDevider/>
                        <!-- bis -->
                        <g:formatDate date="${coverage.endDate}" format="${message(code:'default.date.format.notime')}"/><br>
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                        ${coverage.endVolume}<br>
                        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
                        ${coverage.endIssue}<br>
                        <g:message code="tipp.coverageDepth"/>: ${coverage.coverageDepth}<br>
                        <g:message code="tipp.coverageNote"/>: ${coverage.coverageNote}<br>
                        <g:message code="tipp.embargo"/>: ${coverage.embargo}
                    </g:each>
                </g:else>
            </td>
            <td>
                <!-- von -->
                ${tipp.accessStartDate}
                <semui:dateDevider/>
                <!-- bis -->
                ${tipp.accessEndDate}
            </td>
        </tr>

    </g:each>

    </tbody>

</table>