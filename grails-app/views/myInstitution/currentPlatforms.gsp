<%@ page import="de.laser.Platform; de.laser.storage.RDStore" %>
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
            <th>${message(code:'default.provider.label')}</th>
            <th>${message(code:'default.url.label')}</th>
            <th>${message(code:'accessPoint.plural')}</th>
            <th>${message(code:'myinst.currentPlatforms.assignedSubscriptions')}</th>
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
                    <g:if test="${platformInstance.org}">
                        <div class="la-flexbox">
                            <g:if test="${platformInstance.org.gokbId != null && RDStore.OT_PROVIDER.id in platformInstance.org.getAllOrgTypeIds()}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                      data-content="${message(code: 'org.isWekbCurated.header.label')}">
                                    <i class="la-gokb icon la-list-icon"></i>
                                </span>
                            </g:if>
                            <g:link controller="organisation" action="show" id="${platformInstance.org.id}">${platformInstance.org.getDesignation()}</g:link>
                        </div>
                    </g:if>
                </td>
                <td>
                    <g:if test="${platformInstance.primaryUrl}">
                        ${platformInstance.primaryUrl}
                        <a href="<g:createLink url="${platformInstance.primaryUrl}" />" target="_blank"><i class="external alternate icon"></i></a>
                    </g:if>
                </td>
                <td>
                    <g:each in="${platformInstance.getContextOrgAccessPoints(contextOrg)}" var="oap" >
                        <g:link controller="accessPoint" action="edit_${oap.accessMethod}" id="${oap.id}">${oap.name} (${oap.accessMethod.getI10n('value')})</g:link> <br />
                    </g:each>
                </td>
                <td>
                    <g:each in="${subscriptionMap.get('platform_' + platformInstance.id)}" var="sub">
                        <g:link controller="subscription" action="show" id="${sub.id}">${sub}<br /></g:link>

                        <g:if test="${sub.packages}">
                            <g:each in="${sub.deduplicatedAccessPointsForOrgAndPlatform(contextOrg, platformInstance)}" var="orgap">
                                <div class="la-flexbox">
                                    <span data-position="top right"
                                          class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'myinst.currentPlatforms.tooltip.thumbtack.content')}">
                                        <i class="icon la-thumbtack slash scale la-list-icon"></i>
                                    </span>
                                    <g:link controller="accessPoint" action="edit_${orgap.accessMethod}"
                                            id="${orgap.id}">${orgap.name} (${orgap.accessMethod.getI10n('value')})</g:link>
                                </div>
                            </g:each>
                        </g:if>
                    </g:each>
                </td>
                <%--<td class="center aligned">
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

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</ui:debugInfo>

<laser:htmlEnd />
