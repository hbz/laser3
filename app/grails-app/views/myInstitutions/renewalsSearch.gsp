<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.renewalSearch.label', default:'Renewals Generation - Search')}</title>
  </head>

  <body>

    <laser:breadcrumbs>
        <laser:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
        <laser:crumb message="menu.institutions.gen_renewals" class="active" />
    </laser:breadcrumbs>

    <div class="container">
      <g:form action="renewalsSearch" method="get" params="${params}">
      <input type="hidden" name="offset" value="${params.offset}"/>
      <div class="row">
        <div class="span12">
          <div class="well">
            Package Name: <input name="pkgname" value="${params.pkgname}"/>
            <button type="submit" name="search" value="yes">${message(code:'default.button.search.label', default:'Search')}</button>
            <div class="pull-right">
            <button type="submit" name="clearBasket" value="yes">${message(code:'myinst.renewalSearch.clearBasket', default:'Clear Basket')}</button>
            <button type="submit" name="generate" value="yes">${message(code:'myinst.renewalSearch.generate', default:'Generate Comparison Sheet')}</button>
            </div>
           
          </div>
        </div>
      </div>
      <div class="row">
        <div class="span2">
          <div class="well">
              <g:each in="${facets}" var="facet">
                <h5><g:message code="facet.so.${facet.key}" default="${facet.key}" /></h5>
                    <g:each in="${facet.value.sort{it.display}}" var="fe">
                      <g:set var="facetname" value="fct:${facet.key}:${fe.display}" />
                      <div><g:checkBox class="pull-right" name="${facetname}" value="${params[facetname]}" />${fe.display} (${fe.count})</div>
                    </g:each>
                </li>
              </g:each>
          </div>
        </div>
        <div class="span8">
          <div class="well">
             <g:if test="${hits}" >
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

                <div id="resultsarea">
                  <table class="ui celled striped table">
                    <thead>
                      <tr><th>${message(code:'package.show.pkg_name', default:'Package Name')}</th><th>${message(code:'consortium.label', default:'Consortium')}</th><th style="white-space:nowrap">${message(code:'default.additionalInfo.label', default:'Additional Info')}</th></tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit">
                        <tr>
                          <td><g:link controller="packageDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                          <td>${hit.getSource().consortiaName}</td>
                          <td><button type="submit" class="btn" name="addBtn" value="${hit.getSource().dbId}">${message(code:'myinst.renewalSearch.addBtn', default:'Add to<br/>basket')}</button></td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div>
             </g:if>
             <div class="paginateButtons" style="text-align:center">
                <g:if test="${hits}" >
                  <span><g:paginate controller="myInstitutions" action="renewalsSearch" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${resultsTotal}" /></span>
                </g:if>
              </div>
          </div>
        </div>
        <div class="span2">
          <div class="well">
            <h5>${message(code:'myinst.renewalSearch.basket', default:'Basket')}</h5>
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
    </div>
  </body>
</html>
