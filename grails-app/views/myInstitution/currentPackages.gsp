<%@ page import="de.laser.RefdataCategory; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.Package; de.laser.RefdataValue;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.my.packages')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.my.packages" class="active" />
</semui:breadcrumbs>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${message(code:'menu.my.packages')}
    <semui:totalNumber total="${packageListTotal}"/>
</h1>

<semui:messages data="${flash}" />

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="currentPackages" method="get" class="ui form">
        <div class="two fields">
            <!-- 1-1 -->
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.pkg_q}"/>
                </div>
            </div>
            <!-- 1-2 -->

            <div class="field">
                <label>${message(code: 'myinst.currentPackages.filter.subStatus.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="two fields">
            <div class="field">
            </div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </g:form>
</semui:filter>

<table class="ui sortable celled la-table table">
    <thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
        <th>${message(code:'package.compare.overview.tipps')}</th>
        <th>${message(code:'default.provider.label')}</th>
        <th>${message(code:'package.nominalPlatform')}</th>
        <th>${message(code:'myinst.currentPackages.assignedSubscriptions')}</th>
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
                ${pkg.getCurrentTipps().size() ?: 0}
            </td>

            <td>
                <g:each in="${pkg.orgs.findAll{it.roleType == RDStore.OR_CONTENT_PROVIDER}.sort{it.org.name}}" var="role">
                    <g:link controller="organisation" action="show" id="${role.org.id}">${role?.org?.name}</g:link><br />
                </g:each>
            </td>

            <td>
                <g:if test="${pkg.nominalPlatform}">
                    <g:link controller="platform" action="show" id="${pkg.nominalPlatform.id}">
                        ${pkg.nominalPlatform.name}
                    </g:link>
                </g:if>
            </td>

            <td>
                <g:each in="${subscriptionMap.get('package_' + pkg.id)}" var="sub">
                    <%
                        String period = sub.startDate ? g.formatDate(date: sub.startDate, format: message(code: 'default.date.format.notime'))  : ''
                        period = sub.endDate ? period + ' - ' + g.formatDate(date: sub.endDate, format: message(code: 'default.date.format.notime'))  : ''
                        period = period ? '('+period+')' : ''
                    %>

                    <g:link controller="subscription" action="show" id="${sub.id}">
                        ${sub.name + ' ' +period}
                    </g:link> <br />
                </g:each>
            </td>
            <%--<td class="x">
            </td>--%>
        </tr>
    </g:each>
    </tbody>
</table>

    <semui:paginate total="${packageListTotal}" params="${params}" max="${max}" offset="${offset}" />

</body>
</html>
