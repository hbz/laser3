<!doctype html>
<%@ page import="java.text.SimpleDateFormat"%>
<%
  def addFacet = { params, facet, val ->
    def newparams = [:]
    newparams.putAll(params)
    def current = newparams[facet]
    if ( current == null ) {
      newparams[facet] = val
    }
    else if ( current instanceof String[] ) {
      newparams.remove(current)
      newparams[facet] = current as List
      newparams[facet].add(val);
    }
    else {
      newparams[facet] = [ current, val ]
    }
    newparams
  }

  def removeFacet = { params, facet, val ->
    def newparams = [:]
    newparams.putAll(params)
    def current = newparams[facet]
    if ( current == null ) {
    }
    else if ( current instanceof String[] ) {
      newparams.remove(current)
      newparams[facet] = current as List
      newparams[facet].remove(val);
    }
    else if ( current?.equals(val.toString()) ) {
      newparams.remove(facet)
    }
    newparams
  }

  def dateFormater = new SimpleDateFormat("yy-MM-dd'T'HH:mm:ss.SSS'Z'")
%>

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'package.plural', default:'Packages')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="package.show.all" class="active"/>
    </semui:breadcrumbs>

  <h1 class="ui header"><semui:headerIcon />${message(code:'package.show.all')}</h1>

  <semui:messages data="${flash}" />


  <semui:filter>
      <g:form action="index" method="get" params="${params}" class="ui form">
        <input type="hidden" name="offset" value="${params.offset}"/>
                <div class="field">
                    <label>${message(code:'package.show.pkg_name', default:'Package Name')}</label>
                    <input name="q" placeholder="" value="${params.q}"/>
                </div>
                <div class="field">
                    <button type="submit" name="search" value="yes" class="ui secondary button">${message(code:'default.button.search.label', default:'Search')}</button>
                    <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.searchreset.label')}</a>
                </div>
      </g:form>
   </semui:filter>

  %{--<div class="ui grid">

      <div class="sixteen wide column">
          <g:each in="${['type','endYear','startYear','consortiaName','cpname']}" var="facet">
            <g:each in="${params.list(facet)}" var="fv">
              <span class="badge alert-info">${facet}:${fv} &nbsp; <g:link controller="packageDetails" action="index" params="${removeFacet(params,facet,fv)}"><i class="icon-remove icon-white"></i></g:link></span>
            </g:each>
          </g:each>
      </div>


  <div class="four wide column facetFilter">
      <div class="ui card">
          <div class="content">
              <div class="header"><g:message code="default.filter.label" default="Filter"/></div>
          </div>
          <div class="content">
              <div class="ui relaxed list">
                  <g:each in="${facets}" var="facet">
                      <g:if test="${!(facet.key in ['consortiaName'])}"><%-- hide consortia filter --%>
                          <div class="item">
                              <h4 class="header"><g:message code="facet.so.${facet.key}" default="${facet.key}" /></h4>

                              <g:each in="${facet.value.sort{it.display}}" var="v">
                                  <g:if test="${v.display.toString().length() > 3}">
                                      <div class="description">
                                          <g:set var="fname" value="facet:${facet.key+':'+v.term}"/>


                                          <g:if test="${params.list(facet.key).contains(v.term.toString())}">
                                              ${v.display} (${v.count})
                                          </g:if>
                                          <g:else>
                                              <g:link controller="${controller}" action="linkPackage" params="${addFacet(params,facet.key,v.term)}">${v.display}</g:link> (${v.count})
                                          </g:else>

                                          <%--<div class="ui checkbox">
                                              <g:checkBox class="hidden" name="${facet.key}" value="${params[fname]}" onchange="submit()"/>
                                              <label>${v.display} (${v.count})</label>
                                          </div>--%>
                                      </div>
                                  </g:if>
                              </g:each>

                          </div>
                      </g:if>
                  </g:each>
         </div>
        </div>
      </div>
  </div>--}%

  <div class="twelve wide column">
      <div>
             <g:if test="${hits}" >
                <div class="paginateButtons" style="text-align:center">

                  <g:if test="${offset && params.int('offset') > 0 }">
                    ${message(code:'default.search.offset.text', args: [(params.int('offset') + 1),(resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))),resultsTotal])}
                  </g:if>
                  <g:elseif test="${resultsTotal && resultsTotal > params.int('max')}">
                    ${message(code:'default.search.no_offset.text', args: [(resultsTotal < params.int('max') ? resultsTotal : params.int('max')),resultsTotal])}
                  </g:elseif>
                  <g:elseif test="${resultsTotal && resultsTotal == 1}">
                    ${message(code:'default.search.single.text')}
                  </g:elseif>
                  <g:else>
                    ${message(code:'default.search.no_pagiantion.text', args:[resultsTotal])}
                  </g:else>
                </div><!-- .paginateButtons -->

                <div id="resultsarea">
                  <table class="ui sortable celled la-table table">
                    <thead>
                      <tr>
                      <g:sortableColumn property="sortname" title="${message(code:'package.show.pkg_name', default:'Package Name')}" params="${params}" />
                      <th>${message(code:'package.compare.overview.tipps')}</th></tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit" status="k">
                        <tr>
                          <td>
                            <g:if test="${com.k_int.kbplus.Package.findByImpId(hit.id)}">
                          <g:link controller="packageDetails" action="show" id="${com.k_int.kbplus.Package.findByImpId(hit.id).id}">${hit.getSource().name}</g:link>
                            </g:if>
                              <g:else>${hit.getSource().name} <a target="_blank" href="#" ><i title="GOKB Link" class="external alternate icon"></i></a></g:else>
                          </td>
                          <td>
                              <g:if test="${tippcount[k]}">
                                <g:if test="${tippcount[k] == 1}">
                                  ${message(code:'packageDetails.index.result.titles.single')}
                                </g:if>
                                <g:else>
                                  ${message(code:'packageDetails.index.result.titles', args: [tippcount[k]])}
                                </g:else>
                              </g:if>
                              <g:else>
                                  ${message(code:'packageDetails.index.result.titles.unknown', default:'Unknown number of TIPPs')}
                              </g:else>
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div><!-- #resultsarea -->

                 <semui:paginate action="index" controller="packageDetails" params="${params}"
                                 next="${message(code: 'default.paginate.next', default: 'Next')}"
                                 prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                                 total="${resultsTotal}"/>

             </g:if>
            <g:else>
              <p><g:message code="default.search.empty" default="No results found"/></p>
            </g:else>
          </div>
    </div>
  </div>
  </body>
</html>
