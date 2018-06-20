<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} ${message(code: 'myinst.renewalSearch.label', default: 'Renewals Generation - Search')}</title>
</head>

<body>
    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
        <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
        <semui:crumb message="menu.institutions.gen_renewals" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon />${message(code:'menu.institutions.gen_renewals')}</h1>

    <g:form class="ui form" action="renewalsSearch" method="get" params="${params}">
        <input type="hidden" name="offset" value="${params.offset}"/>

        <div class="ui la-filter segment">
            <div class="field">
                <label>${message(code: 'package.show.pkg_name', default: 'Package Name')}</label>
                <input name="pkgname" value="${params.pkgname}"/>
            </div>

            <div class="field">
                <button class="ui secondary button" type="submit" name="search"
                        value="yes">${message(code: 'default.button.search.label', default: 'Search')}</button>
                <g:if test="${params.search == 'yes'}">
                    <button class="ui button" type="submit" name="searchreset"
                            value="yes">${message(code: 'default.button.searchreset.label', default: 'Search Reset')}</button>
                </g:if>
            </div>
        </div>
    </g:form>

    <div class="ui info message">
        <div class="header">${message(code: 'myinst.renewalSearch.workflowinfo', default: 'Renewal Workflow')}</div>

        <p>${message(code: 'myinst.renewalSearch.workflow', default: '')}</p>
    </div>

<hr/>

<g:if test="${basket.size() <= 1}">

    <div class="ui info message">
        <div class="header">Info</div>

        <p>${message(code: 'myinst.renewalSearch.auswahl', default: 'In your selection at least one package must be deposited!')}</p>
    </div>
    <hr/>
</g:if>

<div class="field">
    <g:if test="${com.k_int.kbplus.Subscription.get(sub_id).packages}">
    <g:link class="ui button" controller="myInstitution" params="${[sub_id: sub_id]}"
            action="renewalsnoPackageChange">${message(code: 'myinst.renewalSearch.uploadnopackageschange', default: 'Import Renewals with packages')}</g:link>
    </g:if>
        <g:link class="ui button" controller="myInstitution" params="${[sub_id: sub_id]}"
                    action="renewalswithoutPackage">${message(code: 'myinst.renewalSearch.uploadwithoutpackage', default: 'Import Renewals without packages')}</g:link>
    <g:link class="ui button" controller="myInstitution"
            action="renewalsUpload">${message(code: 'menu.institutions.imp_renew', default: 'Import Renewals')}</g:link>

</div>
<hr/>

<g:form class="ui form" action="renewalsSearch" method="get" params="${params}">
    <div class="ui grid">

        <div class="four wide column facetFilter">
            <div class="ui card">
                <div class="content">
                    <div class="header"><g:message code="default.filter.label" default="Filter"/></div>
                </div>
                <div class="content">
                    <div class="ui relaxed list">

                        <g:each in="${facets}" var="facet">

                            <g:if test="${facet.key != 'consortiaName' && facet.key != 'type'}">
                                <div class="item">
                                    <h4 class="header"><g:message code="facet.so.${facet.key}" default="${facet.key}"/></h4>
                                    %{--<input type="hidden" name="search" value="yes">--}%
                                    <g:each in="${facet.value.sort { it.display }}" var="fe">
                                        <g:if test="${fe.display.toString().length() > 3}">
                                            <g:set var="facetname" value="fct:${facet.key}:${fe.display}"/>
                                            <div class="ui checkbox">
                                                <g:checkBox class="hidden" name="${facetname}" value="${params[facetname]}"
                                                            onchange="submit()"/>
                                                <label>${fe.display} (${fe.count})</label>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>
                            </g:if>

                        </g:each>
                    </div>
                </div>
            </div>
        </div>

    <div class="eight wide column">
        <g:if test="${hits}">

            <div id="resultsarea">
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'package.show.pkg_name', default: 'Package Name')}</th>
                        %{--<th>${message(code: 'consortium.label', default: 'Consortium')}</th>--}%
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${hits}" var="hit">
                        <tr>
                            <td><i class="gift icon"></i><g:link controller="packageDetails" action="show"
                                                                 id="${hit.getSource().dbId}"
                                                                 target="_blank">${hit.getSource().name}</g:link></td>
                            %{--<td>${hit.getSource().consortiaName}</td>--}%
                            <td>
                                <button type="submit" class="ui button" name="addBtn"
                                        value="${hit.getSource().dbId}">${message(code: 'myinst.renewalSearch.addBtn', default: 'Add to<br/>basket')}</button>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>

        <div class="paginateButtons" style="text-align:center">
            <g:if test="${params.int('offset')}">
                ${message(code: 'default.search.offset.text', args: [(params.int('offset') + 1), (resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))), resultsTotal])}
            </g:if>
            <g:elseif test="${resultsTotal && resultsTotal > 0}">
                ${message(code: 'default.search.no_offset.text', args: [(resultsTotal < params.int('max') ? resultsTotal : params.int('max')), resultsTotal])}
            </g:elseif>
            <g:else>
                ${message(code: 'default.search.no_pagiantion.text', args: [resultsTotal])}
            </g:else>
        </div>

        <div class="paginateButtons" style="text-align:center">
            <g:if test="${hits}">
                <span><g:paginate controller="myInstitution" action="renewalsSearch" params="${params}"
                                  next="${message(code: 'default.paginate.next', default: 'Next')}"
                                  prev="${message(code: 'default.paginate.prev', default: 'Prev')}" maxsteps="10"
                                  total="${resultsTotal}"/></span>
            </g:if>
        </div>
    </div>

    <aside class="four wide column">
        <div class="ui segment">
            <g:if test="${basket}">

                <button class="ui button" type="submit" name="generate"
                        value="yes">${message(code: 'myinst.renewalSearch.generate', default: 'Generate Comparison Sheet')}</button><br><br>
            %{--<button class="ui button" type="button" name=""--}%
            %{--value="">${message(code: 'myinst.renewalSearch.generateonline', default: 'Generate Comparison Sheet Online')}</button>--}%

            </g:if>
            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th><h4 class="ui header">${message(code: 'myinst.renewalSearch.basket', default: 'Basket')}</h4>
                    </th>
                    <th><g:if test="${basket}">
                        <button class="ui icon basic negative button" type="submit" name="clearBasket"
                                value="yes">${message(code: 'myinst.renewalSearch.clearBasket', default: 'all')}<i
                                class="trash icon"></i></button>
                    </g:if></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${basket}" var="itm">
                    <tr></tr>
                    <td><g:if test="${itm.getClass().getName() != 'com.k_int.kbplus.Subscription'}">
                        <i class="gift icon"></i>${itm.name}
                    </g:if><g:else>
                        <i class="folder open outline icon"></i>${itm.name}<hr/>
                    </g:else>
                    </td>
                    <td><g:if test="${itm.getClass().getName() != 'com.k_int.kbplus.Subscription'}">
                        <button type="submit" class="ui icon basic negative button" name="clearOnlyoneitemBasket"
                                value="${itm.id}"><i class="trash icon"></i></button>
                    </g:if>
                    </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </aside>
</g:form>
</body>
</html>
