<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.all_titles')}</title>
  </head>
  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.institutions.all_titles" class="active" />
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.institutions.all_titles')}
      <semui:totalNumber total="${resultsTotal}"/>
    </h1>

    <semui:filter>
      <g:form action="index" role="form" class="ui form" method="get" params="${params}">
        <input type="hidden" name="offset" value="${params.offset}"/>
        <div class="three fields">
          <div class="field">
            <label for="q">${message(code: 'title.search')}</label>
            <input id="q" type="text" name="q" placeholder="${message(code: 'title.search.ph')}" value="${params.q}"/>
          </div>
          <div class="field">
            <label for="filter">${message(code: 'title.search_in')}</label>
            <g:select class="ui dropdown" id="filter" name="filter" from="${[[key:'title',value:"${message(code: 'title.title.label')}"],[key:'publisher',value:"${message(code:'title.publisher.label')}"],[key:'',value:"${message(code: 'title.all.label')}"]]}" optionKey="key" optionValue="value" value="${params.filter}"/>
          </div>
            <div class="field la-field-right-aligned">
              <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
              <button class="ui secondary button" type="submit" name="search" value="yes">${message(code: 'default.button.filter.label')}</button>
            </div>
        </div>
      </g:form>
    </semui:filter>

    <div class="ui grid">
        <div class="sixteen wide column">

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
                </div><!-- .paginateButtons -->

               <div class="ui icon negative message">
                 <i class="exclamation triangle icon"></i>
                 <i class="close icon"></i>
                 <div class="content">
                   <div class="header">
                     ${message(code: 'message.attantion')}
                   </div>
                   <p>${message(code: 'message.attantion.needTime')}</p>
                 </div>
               </div>

                <div id="resultsarea">
                  <table class="ui sortable celled la-table table">
                    <thead>
                      <tr>
                          <th>${message(code:'sidewide.number')}</th>
                      <g:sortableColumn property="sortTitle" title="${message(code: 'title.title.label', default: 'Title')}" params="${params}" />
                      <g:sortableColumn property="typTitle" title="${message(code: 'title.type.label')}" params="${params}" />
                      <g:sortableColumn property="publisher" style="white-space:nowrap" title="${message(code: 'title.publisher.label', default: 'Publisher')}" params="${params}" />
                      <th style="white-space:nowrap"><g:message code="title.identifiers.label" /></th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit" status="jj">
                        <tr>
                          <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                          <td>
                            <semui:listIcon type="${hit.getSource().typTitle}"/>
                            <strong><g:link controller="title" action="show" id="${hit.getSource().dbId}">${hit.getSource().title}</g:link></strong>
                          </td>
                          <td>
                            ${com.k_int.kbplus.RefdataValue.getByValueAndCategory(hit.getSource().typTitle, 'Title Type').getI10n('value')}
                          </td>
                          <td>
                            ${hit.getSource().publisher?:''}
                          </td>
                          <td>
                            <g:each in="${hit.getSource().identifiers.sort{it.type}}" var="id">
                              <g:if test="${id.type != 'originediturl'}">
                                <div style="white-space:nowrap"><span>${id.type}:</span> <span>${id.value}</span></div>
                              </g:if>
                            </g:each>
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div><!-- #resultsarea -->
             </g:if>

              <g:if test="${hits}" >
                <semui:paginate controller="title" action="index" params="${params}" next="${message(code: 'default.paginate.next')}" prev="${message(code: 'default.paginate.prev')}" maxsteps="10" total="${resultsTotal}" />
              </g:if>

        </div><!-- .sixteen -->
      </div><!-- .grid -->
  </body>
</html>
