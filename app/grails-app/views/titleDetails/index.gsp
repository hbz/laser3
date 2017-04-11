<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>KB+ ${message(code: 'title.plural')} - ${message(code: 'default.button.search.label')}</title>
  </head>

  <body>


    <div class="container">
      <ul class="breadcrumb">
        <li><g:link controller="home" action="index">Home</g:link> <span class="divider">/</span></li>
        <li><g:link controller="titleDetails" action="index">${message(code: 'menu.institutions.all_titles')}</g:link></li>
      </ul>
    </div>

    <div class="container">
      <div class="well">
      <g:form action="index" role="form" class="form-inline" method="get" params="${params}">

        <input type="hidden" name="offset" value="${params.offset}"/>

        <label for="q" class="control-label">${message(code: 'title.search')} :</label>
        <input id="q" type="text" name="q" placeholder="${message(code: 'title.search.ph')}" value="${params.q}"/>
       
        <label for="filter" class="control-label">${message(code: 'title.search_in')} :</label>
        <g:select id="filter" name="filter" from="${[[key:'title',value:"${message(code: 'title.title.label')}"],[key:'publisher',value:"${message(code:'title.publisher.label')}"],[key:'',value:"${message(code: 'title.all.label')}"]]}" optionKey="key" optionValue="value" value="${params.filter}"/>
       
        <button type="submit" name="search" value="yes">${message(code: 'default.button.search.label')}</button>
      </g:form>
      </div>
    </div>
    
    <div class="container">
      <div class="row">

        <div class="span12">
          <div class="well">
             <g:if test="${hits}" >
                <div class="paginateButtons" style="text-align:center">
                  <g:if test="${params.int('offset')}">
                    <g:set var="curOffset" value="${params.int('offset') + 1}" />
                    <g:set var="pageMax" value="${resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))}" />
                    ${message(code: 'title.search.offset.text', args: [curOffset,pageMax,resultsTotal])}
                  </g:if>
                  <g:elseif test="${resultsTotal && resultsTotal > 0}">
                    <g:set var="pageMax" value="${resultsTotal < params.int('max') ? resultsTotal : params.int('max')}" />
                    ${message(code: 'title.search.no_offset.text', args: [pageMax,resultsTotal])}
                  </g:elseif>
                  <g:else>
                    ${message(code: 'title.search.no_pagination.text', args: [resultsTotal])}
                  </g:else>
                </div>

                <div id="resultsarea">
                  <table class="table table-bordered table-striped">
                    <thead>
                      <tr>
                      <g:sortableColumn property="sortTitle" title="${message(code: 'title.title.label', default: 'Title')}" params="${params}" />
                      <g:sortableColumn property="publisher" style="white-space:nowrap" title="${message(code: 'title.publisher.label', default: 'Publisher')}" params="${params}" />
                      <th style="white-space:nowrap"><g:message code="title.identifiers.label" /></th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit">
                        <tr>
                          <td>
                            <g:link controller="titleDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().title}</g:link>
                            <g:if test="${editable}">
                              <g:link controller="titleDetails" action="edit" id="${hit.getSource().dbId}">(${message(code: 'default.button.edit.label')})</g:link>
                            </g:if>
                          </td>
                          <td>
                            ${hit.getSource().publisher?:''}
                          </td>
                          <td>
                            <g:each in="${hit.getSource().identifiers}" var="id">
                              <div style="white-space:nowrap"><span>${id.type}:</span><span>${id.value}</span></div>
                            </g:each>
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div>
             </g:if>
             <div class="paginateButtons" style="text-align:center">
                <g:if test="${hits}" >
                  <span><g:paginate controller="titleDetails" action="index" params="${params}" next="${message(code: 'default.paginate.next')}" prev="${message(code: 'default.paginate.prev')}" maxsteps="10" total="${resultsTotal}" /></span>
                </g:if>
              </div>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>
