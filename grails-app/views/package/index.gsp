<%@ page import="de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.storage.RDConstants; de.laser.utils.DateUtils; de.laser.Org; de.laser.Package; de.laser.Platform; de.laser.RefdataValue; java.text.SimpleDateFormat" %>
<laser:htmlStart message="package.show.all" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="package.show.all" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="package.show.all" total="${recordsCount}" floated="true" />

<ui:messages data="${flash}"/>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter"/>
</g:if>

<g:if test="${error}">
    <div class="ui icon error message">
        <i class="exclamation triangle icon"></i>
        <i class="close icon"></i>

        <div class="content">
            <div class="header">
                ${message(code: 'message.attention')}
            </div>

            <p>${error}</p>
        </div>
    </div>
</g:if>

<div class="twelve wide column la-clear-before">
    <div>
        <g:if test="${records}">
            <table class="ui sortable celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'sidewide.number')}</th>
                    <g:sortableColumn property="name" title="${message(code: 'package.show.pkg_name')}" params="${params}"/>
                    <th>${message(code: 'package.status.label')}</th>
                    <g:sortableColumn property="titleCount" title="${message(code: 'package.compare.overview.tipps')}" params="${params}"/>
                    <g:sortableColumn property="providerName" title="${message(code: 'package.content_provider')}" params="${params}"/>
                    <g:sortableColumn property="nominalPlatformName" title="${message(code: 'platform.label')}" params="${params}"/>
                    <th>${message(code: 'package.curatoryGroup.label')}</th>
                    <th>${message(code: 'package.source.automaticUpdates')}</th>
                    <g:sortableColumn property="lastUpdatedDisplay" title="${message(code: 'package.lastUpdated.label')}" params="${params}" defaultOrder="desc"/>
                    <g:if test="${AppUtils.isPreviewOnly()}">
                        <th class="center aligned">
                            <ui:markerIcon type="WEKB_CHANGES" />
                        </th>
                    </g:if>
                    <sec:ifAllGranted roles="ROLE_YODA">
                        <th class="x center aligned">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="left center" data-content="${message(code: 'menu.yoda.reloadPackages')}">
                                <g:link class="ui icon button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: 'menu.yoda.reloadPackages.confirm')}"
                                        data-confirm-term-how="ok"
                                        controller="yoda" action="reloadPackages">
                                            <i class="icon cloud download alternate" style="color:white"></i>
                                </g:link>
                            </span>
                        </th>
                    </sec:ifAllGranted>
                </tr>
                </thead>
                <tbody>
                <g:each in="${records}" var="entry" status="jj">
                    <g:if test="${entry._source}">
                        <g:set var="record" value="${entry._source}"/>
                    </g:if>
                    <g:else>
                        <g:set var="record" value="${entry}"/>
                    </g:else>
                    <tr>
                        <g:set var="pkg" value="${Package.findByGokbId(record.uuid)}"/>
                        <g:set var="org" value="${Org.findByGokbId(record.providerUuid)}"/>
                        <g:set var="plat" value="${Platform.findByGokbId(record.nominalPlatformUuid)}"/>
                        <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                        <td>
                        <%--UUID: ${record.uuid} --%>
                        <%--Package: ${Package.findByGokbId(record.uuid)} --%>
                            <g:if test="${pkg}">
                                <g:link controller="package" action="show" id="${pkg.id}">${record.name}</g:link>
                            </g:if>
                            <g:else>
                                ${record.name} <a target="_blank"
                                                  href="${editUrl ? editUrl + '/public/packageContent/?id=' + record.uuid : '#'}"><i
                                        title="we:kb Link" class="external alternate icon"></i></a>
                            </g:else>
                        </td>
                        <td>
                            ${RefdataValue.getByValueAndCategory(record.status, RDConstants.PACKAGE_STATUS)?.getI10n("value")}
                        </td>
                        <td>
                            <g:if test="${record.currentTippCount}">
                                ${record.currentTippCount}
                            </g:if>
                            <g:else>
                                0
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${org}">
                                <g:if test="${org.gokbId}">
                                    <ui:wekbIconLink type="org" gokbId="${org.gokbId}" />
                                </g:if>
                                <g:link controller="organisation" action="show" id="${org.id}">${record.providerName}</g:link>
                            </g:if>
                            <g:else>${record.providerName}</g:else>
                        </td>
                        <td>
                            <g:if test="${plat}">
                                <g:if test="${plat.gokbId}">
                                    <ui:wekbIconLink type="platform" gokbId="${plat.gokbId}" />
                                </g:if>
                                <g:link controller="platform" action="show" id="${plat.id}">${record.nominalPlatformName}</g:link>
                            </g:if>
                            <g:else>${record.nominalPlatformName}</g:else></td>
                        <td>
                            <g:if test="${record.curatoryGroups}">
                                <g:each in="${record.curatoryGroups}" var="curatoryGroup">
                                    <ui:wekbIconLink type="curatoryGroup" gokbId="${curatoryGroup.curatoryGroup}" />
                                    ${curatoryGroup.name}
%{--                                    <g:link url="${editUrl.endsWith('/') ? editUrl : editUrl+'/'}resource/show/${curatoryGroup.curatoryGroup}" target="_blank">--}%
%{--                                        <i class="icon external alternate"></i>--}%
%{--                                    </g:link>--}%
                                    <br />
                                </g:each>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${record.source?.automaticUpdates}">
                                <g:message code="package.index.result.automaticUpdates"/>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${record.source.frequency}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <g:message code="package.index.result.noAutomaticUpdates"/>
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${record.lastUpdatedDisplay}">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${DateUtils.parseDateGeneric(record.lastUpdatedDisplay)}"/>
                            </g:if>
                        </td>
                        <g:if test="${AppUtils.isPreviewOnly()}">
                            <td class="center aligned">
                                <g:if test="${pkg && pkg.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                                    <ui:markerIcon type="WEKB_CHANGES" color="purple" />
                                </g:if>
                            </td>
                        </g:if>
                        <sec:ifAllGranted roles="ROLE_YODA">
                            <td class="x">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'menu.yoda.reloadPackage')}">
                                    <g:link controller="yoda" action="reloadPackage" params="${[packageUUID: record.uuid]}" class="ui icon button">
                                        <i class="icon cloud download alternate"></i>
                                    </g:link>
                                </span>
                                <g:if test="${pkg}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'menu.yoda.retriggerPendingChanges')}">
                                    <g:link controller="yoda" action="matchPackageHoldings" params="${[pkgId: pkg.id]}" class="ui icon button">
                                        <i class="icon wrench"></i>
                                    </g:link>
                                </span>
                                </g:if>
%{--                                <g:link class="ui button" controller="yoda" action="reloadPackage"--}%
%{--                                        params="${[packageUUID: record.uuid]}"><g:message--}%
%{--                                        code="menu.yoda.reloadPackage"/></g:link>--}%
%{--                                <g:link class="ui button" controller="yoda" action="retriggerPendingChanges"--}%
%{--                                        params="${[packageUUID: record.uuid]}"><g:message--}%
%{--                                        code="menu.yoda.retriggerPendingChanges"/></g:link>--}%
                            </td>
                        </sec:ifAllGranted>
                    </tr>
                </g:each>
                </tbody>
            </table>


            <ui:paginate action="index" controller="package" params="${params}"
                            max="${max}" total="${recordsCount}"/>

        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br/><strong><g:message code="filter.result.empty.object"
                                        args="${[message(code: "package.plural")]}"/></strong>
            </g:if>
            <g:elseif test="${!error}">
                <br/><strong><g:message code="result.empty.object"
                                        args="${[message(code: "package.plural")]}"/></strong>
            </g:elseif>
        </g:else>
    </div>
</div>

<laser:htmlEnd />
