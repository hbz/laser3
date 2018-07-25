<!doctype html>
<html>
    <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} - ${message(code:'menu.institutions.sub_work')}</title>
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.sub_work" class="active" />
        </semui:breadcrumbs>

        <g:form action="generateImportWorksheet" method="get" params="${params}" class="ui form">
            <input type="hidden" name="offset" value="${params.offset}"/>

            <div class="field">
              <label>${message(code:'package.show.pkg_name', default:'Package Name')}</label>
              <input name="pkgname" value="${params.pkgname}"/>
            </div>
            <div class="field">
                <button class="ui button" type="submit" name="search" value="yes">${message(code:'default.button.filter.label', default:'Filter')}</button>
            </div>
            <div class="fields">
                <div class="field">
                    <button class="ui button" type="submit" name="clearBasket" value="yes">${message(code:'subscription.import.clearBasket', default:'Clear Basket')}</button>
                </div>
                <div class="field">
                    <button class="ui button" type="submit" name="generate" value="yes">${message(code:'subscription.import.generate', default:'Generate Subscription Sheet')}</button>
                </div>
            </div>

            <div class="ui grid">

                <div class="four wide column">
                  <g:each in="${facets}" var="facet">
                    <g:if test="${facet.key != 'type'}">
                        <div class="ui vertical segment">
                            <h4 class="ui header"><g:message code="facet.so.${facet.key}" default="${facet.key}" /></h4>
                            <g:each in="${facet.value}" var="fe">
                                <g:if test="${fe.display.toString().length() > 3}">
                                    <g:set var="facetname" value="fct:${facet.key}:${fe.display}" />
                                    <div class="ui checkbox">
                                        <g:checkBox class="hidden" name="${facetname}" value="${params[facetname]}" />
                                        <label>${fe.display} (${fe.count})</label>
                                    </div>
                                </g:if>
                            </g:each>
                        </div>
                    </g:if>

                  </g:each>
                </div>

                <div class="eight wide column">
                    <g:if test="${hits}" >

                        <div id="resultsarea">
                          <table class="ui celled la-table table">
                            <thead>
                              <tr>
                                  <th>${message(code:'package.show.pkg_name', default:'Package Name')}</th>
                                  <th>${message(code:'consortium.label', default:'Consortium')}</th>
                                  <th>${message(code:'default.permissionInfo.label', default:'Additional Info')}</th>
                              </tr>
                            </thead>
                            <tbody>
                              <g:each in="${hits}" var="hit">
                                <tr>
                                  <td><g:link controller="packageDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                  <td>${hit.getSource().consortiaName}</td>
                                  <td><button type="submit" class="ui button" name="addBtn" value="${hit.getSource().dbId}">${message(code:'myinst.renewalSearch.addBtn', default:'Add to<br/>basket')}</button></td>
                                </tr>
                              </g:each>
                            </tbody>
                          </table>
                        </div>
                     </g:if>

                    <div class="paginateButtons" style="text-align:center">
                        <g:if test="${params.int('offset')}">
                            ${message(code:'default.search.offset.text', args:[(params.int('offset') + 1),(resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))),resultsTotal])}
                        </g:if>
                        <g:elseif test="${resultsTotal && resultsTotal > 0}">
                            ${message(code:'default.search.no_offset.text', args:[(resultsTotal < params.int('max') ? resultsTotal : params.int('max')),resultsTotal])}
                        </g:elseif>
                        <g:else>
                            ${message(code:'default.search.no_pagiantion.text', args:[resultsTotal])}
                        </g:else>
                    </div>

                     <div class="paginateButtons" style="text-align:center">
                        <g:if test="${hits}" >
                          <span><g:paginate controller="subscriptionImport" action="generateImportWorksheet" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${resultsTotal}" /></span>
                        </g:if>
                      </div>
                </div>

                <div class="four wide column">
                  <div class="ui segment">
                    <h4 class="ui header">${message(code:'subscription.import.basket', default:'Basket')}</h4>
                    <g:each in="${basket}" var="itm">
                      <div>
                        <hr/>
                        ${itm.name}
                      </div>
                    </g:each>
                  </div>
                </div>

            </div>
        </g:form>

    </body>
</html>
