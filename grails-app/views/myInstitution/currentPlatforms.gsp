<%@ page import="de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.wekb.Platform; de.laser.storage.RDStore" %>
<laser:htmlStart message="menu.my.platforms" />

<ui:breadcrumbs>
    <ui:crumb message="menu.my.platforms" class="active" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.platforms" total="${platformInstanceTotal}" floated="true" />

<ui:messages data="${flash}" />

<laser:render template="/templates/filter/platformFilter"/>

<g:if test="${platformInstanceList}">

    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <g:sortableColumn property="p.normname" title="${message(code: 'default.name.label')}" />
            <th>${message(code:'default.url.label')}</th>
            <th>${message(code:'provider.label')}</th>
            <%--<th>${message(code:'accessPoint.plural')}</th>--%>
            <th>${message(code:'myinst.currentPlatforms.assignedSubscriptions')}</th>
            <th class="center aligned"><ui:markerIcon type="WEKB_CHANGES" /></th>
            <%--<th>${message(code:'org.isWekbCurated.label')}</th>--%>
        </tr>
        </thead>
        <tbody>
        <g:each in="${platformInstanceList}" var="platformInstance" status="jj">
            <tr>
                <td>
                    ${ (params.int('offset') ?: 0)  + jj + 1 }
                </td>
                <th scope="row" class="la-th-column">
                    <g:link class="la-main-object"  controller="platform" action="show" id="${platformInstance.id}">${fieldValue(bean: platformInstance, field: "name")}</g:link>
                </th>
                <td>
                    <g:if test="${platformInstance.primaryUrl}">
                        ${platformInstance.primaryUrl} <ui:linkWithIcon href="${platformInstance.primaryUrl}"/>
                    </g:if>
                </td>
                <td>
                    <g:if test="${platformInstance.provider}">
                        <g:if test="${platformInstance.provider.gokbId != null}">
                            <ui:wekbIconLink type="provider" gokbId="${platformInstance.provider.gokbId}" />
                        </g:if>
                        <g:link controller="provider" action="show" id="${platformInstance.provider.id}">${platformInstance.provider.name}</g:link>
                    </g:if>
                </td>
                <%--<td>
                    <g:each in="${platformInstance.getContextOrgAccessPoints(contextService.getOrg())}" var="oap" >
                        <g:link controller="accessPoint" action="edit_${oap.accessMethod.value.toLowerCase()}" id="${oap.id}">${oap.name} (${oap.accessMethod.getI10n('value')})</g:link> <br />
                    </g:each>
                </td>--%>
                <td>
                    <g:if test="${subscriptionMap.get('platform_' + platformInstance.id)}">
                            <g:each in="${subscriptionMap.get('platform_' + platformInstance.id)}" var="sub">
                                    <%
                                        String period = sub.startDate ? g.formatDate(date: sub.startDate, format: message(code: 'default.date.format.notime'))  : ''
                                        period = sub.endDate ? period + ' - ' + g.formatDate(date: sub.endDate, format: message(code: 'default.date.format.notime'))  : ''
                                        period = period ? '('+period+')' : ''
                                    %>
                                    <div class="la-flexbox">
                                        <g:if test="${subscriptionMap.get('platform_' + platformInstance.id).size() > 1}">
                                            <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                        </g:if>
                                        <g:link controller="subscription" action="show" id="${sub.id}">${sub} ${period}</g:link>
                                    </div>
                                    <%--
                                    <g:if test="${sub.packages}">
                                        <g:each in="${sub.deduplicatedAccessPointsForOrgAndPlatform(contextService.getOrg(), platformInstance)}" var="orgap">
                                            <div class="la-flexbox">
                                                <span class="la-popup-tooltip" data-position="top right" data-content="${message(code: 'myinst.currentPlatforms.tooltip.thumbtack.content')}">
                                                    <i class="${Icon.SIG.INHERITANCE_OFF} la-list-icon"></i>
                                                </span>
                                                <g:link controller="accessPoint" action="edit_${orgap.accessMethod.value.toLowerCase()}"
                                                        id="${orgap.id}">${orgap.name} (${orgap.accessMethod.getI10n('value')})</g:link>
                                            </div>
                                        </g:each>
                                    </g:if>
                                    --%>
                            </g:each>
                    </g:if>
                </td>
                <td class="center aligned">
                    <g:if test="${platformInstance.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                        <ui:cbItemMarkerAction platform="${platformInstance}" type="${Marker.TYPE.WEKB_CHANGES}" simple="true"/>
                    </g:if>
                </td>
                <%--<td>
                    <g:if test="${platformInstance.provider}">
                        <div class="la-flexbox">
                            <g:if test="${platformInstance.provider.gokbId != null}">
                                <ui:wekbButtonLink type="platform" gokbId="${platformInstance.gokbId}" />
                            </g:if>
                        </div>
                    </g:if>
                </td>
                <td class="center aligned">
                </td>--%>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "platform.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object"
                                args="${[message(code: "platform.plural")]}"/></strong>
    </g:else>
</g:else>

<laser:htmlEnd />
