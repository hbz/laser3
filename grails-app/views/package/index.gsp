<%@ page import="de.laser.utils.DateUtils; de.laser.Org; de.laser.Package; de.laser.Platform; java.text.SimpleDateFormat;" %>
<laser:htmlStart message="package.show.all" />

<ui:breadcrumbs>
    <ui:crumb message="package.show.all" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="package.show.all" total="${recordsCount}" floated="true" />

<ui:messages data="${flash}"/>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter"/>
</g:if>

<g:if test="${error}">
    <div class="ui icon info error message">
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
                    <g:sortableColumn property="name"
                                      title="${message(code: 'package.show.pkg_name')}"
                                      params="${params}"/>
                    <g:sortableColumn property="titleCount"
                                      title="${message(code: 'package.compare.overview.tipps')}"
                                      params="${params}"/>
                    <g:sortableColumn property="providerName" title="${message(code: 'package.content_provider')}"
                                      params="${params}"/>
                    <g:sortableColumn property="nominalPlatformName" title="${message(code: 'platform.label')}"
                                      params="${params}"/>
                    <th>${message(code: 'package.curatoryGroup.label')}</th>
                    <th>${message(code: 'package.source.label')}</th>
                    <g:sortableColumn property="lastUpdatedDisplay" title="${message(code: 'package.lastUpdated.label')}"
                                      params="${params}" defaultOrder="desc"/>
                    <sec:ifAllGranted roles="ROLE_YODA">
                        <th class="x">
                            <g:link class="ui button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: 'menu.yoda.reloadPackages.confirm')}"
                                    data-confirm-term-how="ok"
                                    controller="yoda" action="reloadPackages">
                                <g:message code="menu.yoda.reloadPackages"/>
                            </g:link>
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
                                <g:link controller="package" action="show"
                                        id="${pkg.id}">${record.name}</g:link>
                            </g:if>
                            <g:else>
                                ${record.name} <a target="_blank"
                                                  href="${editUrl ? editUrl + '/public/packageContent/?id=' + record.uuid : '#'}"><i
                                        title="we:kb Link" class="external alternate icon"></i></a>
                            </g:else>
                        </td>
                        <td>
                            <g:if test="${record.titleCount}">
                                ${record.titleCount}
                            </g:if>
                            <g:else>
                                0
                            </g:else>
                        </td>
                        <td><g:if test="${org}"><g:link
                                controller="organisation" action="show"
                                id="${org.id}">${record.providerName}</g:link></g:if>
                        <g:else>${record.providerName}</g:else>
                        </td>
                        <td><g:if test="${plat}"><g:link
                                controller="platform" action="show"
                                id="${plat.id}">${record.nominalPlatformName}</g:link></g:if>
                            <g:else>${record.nominalPlatformName}</g:else></td>
                        <td>
                            <div class="ui bulleted list">
                                <g:each in="${record.curatoryGroups}" var="curatoryGroup">
                                    <div class="item"><g:link url="${editUrl.endsWith('/') ? editUrl : editUrl+'/'}resource/show/${curatoryGroup.curatoryGroup}">${curatoryGroup.name}</g:link></div>
                                </g:each>
                            </div>
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
                            <td>
                                <g:link class="ui button" controller="yoda" action="reloadPackage"
                                        params="${[packageUUID: record.uuid]}"><g:message
                                        code="menu.yoda.reloadPackage"/></g:link>
                                <g:link class="ui button" controller="yoda" action="retriggerPendingChanges"
                                        params="${[packageUUID: record.uuid]}"><g:message
                                        code="menu.yoda.retriggerPendingChanges"/></g:link>
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
