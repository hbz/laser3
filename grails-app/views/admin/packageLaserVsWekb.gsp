<%@ page import="de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.storage.RDConstants; de.laser.utils.DateUtils; de.laser.Org; de.laser.Package; de.laser.Platform; de.laser.RefdataValue; java.text.SimpleDateFormat" %>
<laser:htmlStart message="menu.admin.packageLaserVsWekb" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.admin.packageLaserVsWekb" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.packageLaserVsWekb" total="${recordsCount}" floated="true" />

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
                    <th>Laser <br>${message(code: 'package.show.nav.current')}</th>
                    <th>Wekb <br>${message(code: 'package.show.nav.current')}</th>
                    <th>Laser <br>${message(code: 'package.show.nav.planned')}</th>
                    <th>Wekb <br>${message(code: 'package.show.nav.planned')}</th>
                    <th>Laser <br>${message(code: 'package.show.nav.expired')}</th>
                    <th>Wekb <br>${message(code: 'package.show.nav.expired')}</th>
                    <th>${message(code: 'package.curatoryGroup.label')}</th>
                    <th>${message(code: 'package.source.automaticUpdates')}</th>
                    <g:sortableColumn property="lastUpdatedDisplay" title="${message(code: 'package.lastUpdated.label')}" params="${params}" defaultOrder="desc"/>
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
                        <g:set var="laserCurrentTitles" value="${pkg ? pkg.getCurrentTippsCount() : 0}"/>
                        <g:set var="laserRetiredTitles" value="${pkg ? pkg.getRetiredTippsCount() : 0}"/>
                        <g:set var="laserExpectedTitles" value="${pkg ? pkg.getExpectedTippsCount() : 0}"/>
                        <g:set var="wekbCurrentTitles" value="${record.currentTippCount ?: 0}"/>
                        <g:set var="wekbRetiredTitles" value="${record.retiredTippCount ?: 0}"/>
                        <g:set var="wekbExpectedTitles" value="${record.expectedTippCount ?: 0}"/>
                        <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                        <td>
                        <%--UUID: ${record.uuid} --%>
                        <%--Package: ${Package.findByGokbId(record.uuid)} --%>
                            <g:if test="${pkg}">
                                <ui:wekbIconLink type="package" gokbId="${record.uuid}" />
                                <br>
                                <br>
                                <g:link controller="package" action="show" id="${pkg.id}">${record.name}</g:link>
                            </g:if>
                            <g:else>
                                <ui:wekbIconLink type="package" gokbId="${record.uuid}" /> ${record.name}
                            </g:else>
                        </td>
                        <td>
                            ${RefdataValue.getByValueAndCategory(record.status, RDConstants.PACKAGE_STATUS)?.getI10n("value")}
                        </td>
                        <td class=" ${pkg && wekbCurrentTitles != laserCurrentTitles ? 'negative' : ''}">
                            <g:formatNumber number="${laserCurrentTitles}"/>
                        </td>

                        <td>
                            <g:formatNumber number="${wekbCurrentTitles}"/>
                        </td>

                        <td class=" ${pkg && wekbExpectedTitles != laserExpectedTitles ? 'negative' : ''}">
                            <g:formatNumber number="${laserExpectedTitles}"/>
                        </td>

                        <td>
                            <g:formatNumber number="${wekbExpectedTitles}"/>
                        </td>

                        <td class=" ${pkg && wekbRetiredTitles != laserRetiredTitles ? 'negative' : ''}">
                            <g:formatNumber number="${laserRetiredTitles}"/>
                        </td>

                        <td>
                            <g:formatNumber number="${wekbRetiredTitles}"/>
                        </td>

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
                        <sec:ifAllGranted roles="ROLE_YODA">
                            <td class="x">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'menu.yoda.reloadPackage')}">
                                    <g:link controller="yoda" action="reloadPackage" params="${[packageUUID: record.uuid]}" class="ui icon button">
                                        <i class="icon cloud download alternate"></i>
                                    </g:link>
                                </span>
                                <g:if test="${pkg}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'menu.yoda.retriggerPendingChanges')}">
                                    <g:if test="${pkg}">
                                        <g:link controller="yoda" action="matchPackageHoldings" params="${[pkgId: pkg.id]}" class="ui icon button">
                                            <i class="icon wrench"></i>
                                        </g:link>
                                    </g:if>
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

            <ui:paginate action="packageLaserVsWekb" controller="admin" params="${params}" max="${max}" total="${recordsCount}"/>

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
