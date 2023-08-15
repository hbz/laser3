<%@ page import="de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Package; de.laser.RefdataValue" %>
<laser:htmlStart message="menu.my.packages" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.my.packages" class="active" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.packages" total="${packageListTotal}" floated="true" />

<ui:messages data="${flash}" />

<ui:filter>
    <g:form action="currentPackages" method="get" class="ui form">
        <div class="two fields">
            <!-- 1-1 -->
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>
            <!-- 1-2 -->

            <div class="field">
                <label>${message(code: 'myinst.currentPackages.filter.subStatus.label')}</label>
                <ui:select class="ui dropdown" name="status"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="two fields">
            <div class="field">
                <label for="ddc">${message(code: 'package.ddc.label')}</label>

                <select name="ddc" id="ddc" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${ddcs}" var="ddc">
                        <option <%=(params.list('ddc')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                value="${ddc.id}">
                            ${ddc.value} - ${ddc.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>
            <div class="field la-field-right-aligned">
                <input type="hidden" name="isSiteReloaded" value="yes"/>
                <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </g:form>
</ui:filter>

<g:if test="${packageList}">
    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
            <th>${message(code:'package.compare.overview.tipps')}</th>
            <th>${message(code:'default.provider.label')}</th>
            <th>${message(code:'platform.label')}</th>
            <th>${message(code:'myinst.currentPackages.assignedSubscriptions')}</th>
            <g:if test="${AppUtils.isPreviewOnly()}">
                <th class="center aligned">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code: 'myInst.marker.wekbchanges')}"><i class="icon bookmark"></i></span>
                </th>
            </g:if>
        </tr>
        </thead>
        <tbody>
        <g:each in="${packageList}" var="pkg" status="jj">
            <tr>
                <td>
                    ${ (params.int('offset') ?: 0)  + jj + 1 }
                </td>
                <th scope="row" class="la-th-column">
                    <g:link class="la-main-object"  controller="package" action="show" id="${pkg.id}">${fieldValue(bean: pkg, field: "name")}</g:link>
                </th>

                <td>
                    ${packageService.getCurrentTippIDs(pkg).size()}
                </td>

                <td>
                    <g:each in="${pkg.orgs.findAll{it.roleType == RDStore.OR_CONTENT_PROVIDER}.sort{it.org.name}}" var="role">
                        <g:if test="${role.org.gokbId}">
                            <ui:wekbIconLink type="org" gokbId="${role.org.gokbId}" />
                        </g:if>
                        <g:link controller="organisation" action="show" id="${role.org.id}">${role.org.name}</g:link>
                        <br />
                    </g:each>
                </td>

                <td>
                    <g:if test="${pkg.nominalPlatform}">
                        <g:if test="${pkg.nominalPlatform.gokbId}">
                            <ui:wekbIconLink type="platform" gokbId="${pkg.nominalPlatform.gokbId}" />
                        </g:if>
                        <g:link controller="platform" action="show" id="${pkg.nominalPlatform.id}">${pkg.nominalPlatform.name}</g:link>
                    </g:if>
                </td>

                <td>
                    <ul class="la-simpleList">
                    <g:each in="${subscriptionMap.get('package_' + pkg.id)}" var="sub">
                        <%
                            String period = sub.startDate ? g.formatDate(date: sub.startDate, format: message(code: 'default.date.format.notime'))  : ''
                            period = sub.endDate ? period + ' - ' + g.formatDate(date: sub.endDate, format: message(code: 'default.date.format.notime'))  : ''
                            period = period ? '('+period+')' : ''
                        %>
                        <li>
                            <g:link controller="subscription" action="show" id="${sub.id}">${sub.name + ' ' +period}</g:link>
                        </li>
                    </g:each>
                    </ul>
                </td>
                <%--<td class="center aligned">
                </td>--%>
                <g:if test="${AppUtils.isPreviewOnly()}">
                    <td class="center aligned">
                        <g:if test="${pkg.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                            <i class="icon purple bookmark"></i>
                        </g:if>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
    </table>

    <ui:paginate total="${packageListTotal}" params="${params}" max="${max}" offset="${offset}" />
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

<laser:htmlEnd />
