<%@ page import="de.laser.Subscription" %>
<laser:htmlStart message="myinst.renewalSearch.label" />

    <ui:breadcrumbs>
        <ui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
        <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
        <ui:crumb message="menu.institutions.gen_renewals" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.institutions.gen_renewals" />

    <g:form class="ui form" action="renewalsSearch" method="get" params="${params}">
        <input type="hidden" name="offset" value="${params.offset}"/>

        <div class="ui la-filter segment">
            <div class="field">
                <label>${message(code: 'package.show.pkg_name')}</label>
                <input name="pkgname" value="${params.pkgname}"/>
            </div>

            <div class="field">
                <button class="ui primary button" type="submit" name="search"
                        value="yes">${message(code: 'default.button.search.label')}</button>
                <g:if test="${params.search == 'yes'}">
                    <button class="ui button" type="submit" name="searchreset"
                            value="yes">${message(code: 'default.button.searchreset.label')}</button>
                </g:if>
            </div>
        </div>
    </g:form>

    <ui:msg class="info" header="${message(code: 'myinst.renewalSearch.workflowinfo')}" message="myinst.renewalSearch.workflow" noClose="true" />

<hr />

<g:if test="${basket.size() <= 1}">

    <ui:msg class="info" header="Info" message="myinst.renewalSearch.auswahl" noClose="true" />

    <hr />
</g:if>

<div class="field">
    <g:if test="${Subscription.get(sub_id).packages}">
    <g:link class="ui button" controller="myInstitution" params="${[sub_id: sub_id]}"
            action="renewalsnoPackageChange">${message(code: 'myinst.renewalSearch.uploadnopackageschange')}</g:link>
    </g:if>
        <g:link class="ui button" controller="myInstitution" params="${[sub_id: sub_id]}"
                    action="renewalswithoutPackage">${message(code: 'myinst.renewalSearch.uploadwithoutpackage')}</g:link>
    <g:link class="ui button" controller="myInstitution"
            action="renewalsUpload">${message(code: 'menu.institutions.imp_renew')}</g:link>

</div>
<hr />

<g:form class="ui form" action="renewalsSearch" method="get" params="${params}">
    <div class="ui grid">

        <div class="four wide column facetFilter">
            <div class="ui card">
                <div class="content">
                    <div class="header"><g:message code="default.filter.label" /></div>
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
                <table class="ui celled la-js-responsive-table la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'package.show.pkg_name')}</th>
                        %{--<th>${message(code: 'consortium.label')}</th>--}%
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${hits}" var="hit">
                        <tr>
                            <td><i class="gift icon"></i><g:link controller="package" action="show"
                                                                 id="${hit.getSource().dbId}"
                                                                 target="_blank">${hit.getSource().name}</g:link></td>
                            %{--<td>${hit.getSource().consortiaName}</td>--}%
                            <td>
                                <button type="submit" class="ui button" name="addBtn"
                                        value="${hit.getSource().dbId}">${message(code: 'myinst.renewalSearch.addBtn')}</button>
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
                                  next="${message(code: 'default.paginate.next')}"
                                  prev="${message(code: 'default.paginate.prev')}" maxsteps="10"
                                  total="${resultsTotal}"/></span>
            </g:if>
        </div>
    </div>

    <aside class="four wide column">
        <div class="ui segment">
            <g:if test="${basket}">

                <button class="ui button" type="submit" name="generate"
                        value="yes">${message(code: 'myinst.renewalSearch.generate')}</button><br /><br />
            %{--<button class="ui button" type="button" name=""--}%
            %{--value="">${message(code: 'myinst.renewalSearch.generateonline')}</button>--}%

            </g:if>
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th><h4 class="ui header">${message(code: 'myinst.renewalSearch.basket')}</h4>
                    </th>
                    <th><g:if test="${basket}">
                        <button class="ui icon basic negative button" type="submit" name="clearBasket"
                                value="yes">${message(code: 'myinst.renewalSearch.clearBasket')}<i
                                class="trash icon"></i></button>
                    </g:if></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${basket}" var="itm">
                    <tr>
                    <td><g:if test="${itm.getClass().getName() != Subscription.class.name}">
                        <i class="gift icon"></i>${itm.name}
                    </g:if><g:else>
                        <i class="clipboard outline icon"></i>${itm.name}<hr />
                    </g:else>
                    </td>
                    <td><g:if test="${itm.getClass().getName() != Subscription.class.name}">
                        <button type="submit" class="ui icon basic negative button" name="clearOnlyoneitemBasket"
                                value="${itm.id}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash icon"></i></button>
                    </g:if>
                    </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </aside>
</g:form>

<laser:htmlEnd />
