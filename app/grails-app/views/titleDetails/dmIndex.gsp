<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'title.plural', default:'Titles')} - ${message(code:'default.search.label', default:'Search')}</title>
  </head>

  <body>


    <div class="container">
      <ul class="breadcrumb">
        <li><g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span></li>
        <li><g:link controller="titleDetails" action="dmIndex">${message(code:'datamanager.titleView.label', default:'Data Manager Titles View')}</g:link></li>
      </ul>
    </div>

    <div class="container">
      <g:form action="dmIndex" method="get" params="${params}" role="form" class="form-inline">
      <input type="hidden" name="offset" value="${params.offset}"/>

      <div class="row">
        <div class="span12">
          <div class="well container">
            ${message(code:'title.label', default:'Title')} : <input name="q" placeholder="${message(code:'default.search_for.label', args:[message(code:'title.label')], default:'Search title')}" value="${params.q}"/> (${message(code:'datamanager.titleView.search.note', default:'Search on title text and identifiers')})
            ${message(code:'default.status.label', default:'Status')} : <g:select name="status" from="${availableStatuses}" noSelection="${['null': message(code:'datamanager.titleView.status.ph', default:'-Any Status-')]}" value="${params.status}"/>
           
            <button type="submit" name="search" value="yes">${message(code:'default.button.search.label', default:'Search')}</button>
            <div class="pull-right">
            </div>
          </div>
        </div>
      </div>


      <div class="row">

        <div class="span12">
          <div class="well">
             <g:if test="${hits}" >
                <div class="paginateButtons" style="text-align:center">
                  <g:if test="${params.int('offset')}">
                   ${message(code:'default.search.offset.text', args:[(params.int('offset') + 1),(totalHits < (params.int('max') + params.int('offset')) ? totalHits : (params.int('max') + params.int('offset'))),totalHits])}
                  </g:if>
                  <g:elseif test="${totalHits && totalHits > 0}">
                    ${message(code:'default.search.no_offset.text', args:[(totalHits < params.int('max') ? totalHits : params.int('max')),totalHits])}
                  </g:elseif>
                  <g:else>
                    ${message(code:'default.search.no_pagiantion.text', args:[totalHits])}
                  </g:else>
                </div>

                <div id="resultsarea">
                  <table class="ui celled striped table">
                    <thead>
                      <tr>
                      <th style="white-space:nowrap">${message(code:'title.label', default:'Title')}</th>
                      <th style="white-space:nowrap">${message(code:'title.publisher.label', default:'Publisher')}</th>
                      <th style="white-space:nowrap">${message(code:'indentifier.plural', default:'Identifiers')}</th>
                      <th style="white-space:nowrap">${message(code:'default.status.label', default:'Status')}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit">
                        <tr>
                          <td>
                            <g:link controller="titleDetails" action="show" id="${hit.id}">${hit.title}</g:link>
                            <g:if test="${editable}">
                              <g:link controller="titleDetails" action="edit" id="${hit.id}">(Edit)</g:link>
                            </g:if>
                          </td>
                          <td>
                            ${hit.publisher?.name}
                          </td>
                          <td>
                            <ul>
                              <g:each in="${hit.ids}" var="id">
                                <g:if test="${id.identifier.ns.ns == 'originediturl'}">
                                  <li>${id.identifier.ns.ns}: <a href="${id.identifier.value}">GOKb-URL</a></li>
                                </g:if>
                                <g:else>
                                  <li>${id.identifier.ns.ns}: ${id.identifier.value}</li>
                                </g:else>
                              </g:each>
                            </ul>
                          </td>
                          <td>
                            ${hit.status?.value}
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div>
             </g:if>
             <div class="paginateButtons" style="text-align:center">
                <g:if test="${hits}" >
                  <span><g:paginate controller="titleDetails" action="dmIndex" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${totalHits}" /></span>
                </g:if>
              </div>
          </div>
        </div>
      </div>
      </g:form>
    </div>
    <!-- ES Query: ${es_query} -->
  </body>
</html>
