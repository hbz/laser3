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
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'package.plural', default:'Packages')}</title>
  </head>

  <body>

  <laser:breadcrumbs>
    <laser:crumb message="package.show.all" class="active"/>
  </laser:breadcrumbs>

    <div class="container">
      <g:form action="index" method="get" params="${params}">
      <input type="hidden" name="offset" value="${params.offset}"/>

      <div class="row">
        <div class="span12">
          <div class="well form-horizontal">
            ${message(code:'package.show.pkg_name', default:'Package Name')}: <input name="q" placeholder="${message(code:'packageDetails.index.search.ph')}" value="${params.q}"/>
            ${message(code:'packageDetails.index.search.sort', default:'Sort')}: <select name="sort">
                    <option ${params.sort=='sortname' ? 'selected' : ''} value="sortname">${message(code:'package.show.pkg_name', default:'Package Name')}</option>
                    <option ${params.sort=='_score' ? 'selected' : ''} value="_score">${message(code:'packageDetails.index.search.sort.score', default:'Score')}</option>
                    <option ${params.sort=='lastModified' ? 'selected' : ''} value="lastModified">${message(code:'packageDetails.index.search.sort.modified', default:'Last Modified')}</option>
                  </select>
            ${message(code:'packageDetails.index.search.order', default:'Order')}: <select name="order" value="${params.order}">
                    <option ${params.order=='asc' ? 'selected' : ''} value="asc">${message(code:'default.asc', default:'Ascending')}</option>
                    <option ${params.order=='desc' ? 'selected' : ''} value="desc">${message(code:'default.desc', default:'Descending')}</option>
                  </select>
            <button type="submit" name="search" value="yes">${message(code:'default.button.search.label', default:'Search')}</button>
          </div>
        </div>
      </div>
      </g:form>

      <p>
          <g:each in="${['type','endYear','startYear','consortiaName','cpname']}" var="facet">
            <g:each in="${params.list(facet)}" var="fv">
              <span class="badge alert-info">${facet}:${fv} &nbsp; <g:link controller="packageDetails" action="index" params="${removeFacet(params,facet,fv)}"><i class="icon-remove icon-white"></i></g:link></span>
            </g:each>
          </g:each>
        </p>

      <div class="row">

  
        <div class="facetFilter span2">
          <g:each in="${facets.sort{it.key}}" var="facet">
            <g:if test="${facet.key != 'type'}">
            <div class="panel panel-default">
              <div class="panel-heading">
                <h5><g:message code="facet.so.${facet.key}" default="${facet.key}" /></h5>
              </div>
              <div class="panel-body">
                <ul>
                  <g:each in="${facet.value.sort{it.display}}" var="v">
                    <li>
                      <g:set var="fname" value="facet:${facet.key+':'+v.term}"/>
 
                      <g:if test="${params.list(facet.key).contains(v.term.toString())}">
                        ${v.display} (${v.count})
                      </g:if>
                      <g:else>
                        <g:link controller="${controller}" action="${action}" params="${addFacet(params,facet.key,v.term)}">${v.display}</g:link> (${v.count})
                      </g:else>
                    </li>
                  </g:each>
                </ul>
              </div>
            </div>
            </g:if>
          </g:each>
        </div>


        <div class="span10">
          <div class="well">
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
                </div>

                <div id="resultsarea">
                  <table class="ui celled striped table">
                    <thead>
                      <tr>
                      <g:sortableColumn property="sortname" title="${message(code:'package.show.pkg_name', default:'Package Name')}" params="${params}" />
                      <g:sortableColumn property="consortiaName" title="${message(code:'consortium', default:'Consortium')}" params="${params}" />
                      <th style="word-break:normal">${message(code:'package.show.start_date', default:'Start Date')}</th>
                      <th style="word-break:normal">${message(code:'package.show.end_date', default:'End Date')}</th>
                      <th>${message(code:'package.lastUpdated.label', default:'Last Modified')}</th></tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit">
                        <tr>
                          <td><g:link controller="packageDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>
                            <!--(${hit.score})-->
                            <span style="white-space:nowrap">(
                              <g:if test="${hit.getSource().titleCount}">
                                <g:if test="${hit.getSource().titleCount == 1}">
                                  ${message(code:'packageDetails.index.result.titles.single')}
                                </g:if>
                                <g:else>
                                  ${message(code:'packageDetails.index.result.titles', args: [hit.getSource().titleCount])}
                                </g:else>
                              </g:if>
                              <g:else>
                                  ${message(code:'packageDetails.index.result.titles.unknown', default:'Unknown number of TIPPs')}
                              </g:else>
                            )</span>
                          </td>
                          <td>${hit.getSource().consortiaName}</td>
                          <td style="white-space:nowrap">
                          <g:formatDate formatName="default.date.format.notime" date='${hit.getSource().startDate?dateFormater.parse(hit.getSource().startDate):null}'/>
                          </td>
                          <td style="white-space:nowrap">
                          <g:formatDate formatName="default.date.format.notime" date='${hit.getSource().endDate?dateFormater.parse(hit.getSource().endDate):null}'/>
                          </td>
                          <td style="white-space:nowrap">${hit.getSource().lastModified}</td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div>
                <div class="paginateButtons" style="text-align:center">
                  <span><g:paginate controller="packageDetails" action="index" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" total="${resultsTotal}" /></span>
            </g:if>
            <g:else>
              <p><g:message code="default.search.empty" default="No results found"/></p>
            </g:else>
          </div>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>
