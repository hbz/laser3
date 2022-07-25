<%@ page import="de.laser.RefdataValue;de.laser.storage.RDConstants" %>
<laser:htmlStart message="menu.public.all_titles" />

    <ui:breadcrumbs>
      <ui:crumb message="menu.public.all_titles" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.public.all_titles" total="${resultsTotal >= 10000 ? '10000+' : (resultsTotal > 0 ? resultsTotal : 0)}" floated="true" />

    <ui:filter showFilterButton="true" addFilterJs="true">
      <g:form action="index" role="form" class="ui form" method="get" params="${params}">
        <input type="hidden" name="offset" value="${params.offset}"/>
        <div class="three fields">
          <div class="field">
            <label for="q">${message(code: 'title.search')}</label>
            <input id="q" type="text" name="q" placeholder="${message(code: 'title.search.ph')}" value="${params.q}"/>
          </div>
          <div class="field">
            <label for="filter">${message(code: 'title.search_in')}</label>
            <g:select class="ui dropdown" id="filter" name="filter" from="${[[key:'name',value:"${message(code: 'default.title.label')}"],[key:'publishers',value:"${message(code:'tipp.publisher')}"],[key:'',value:"${message(code: 'default.all')}"]]}" optionKey="key" optionValue="value" value="${params.filter}"/>
          </div>
            <div class="field la-field-right-aligned">
              <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
              <button class="ui secondary button" type="submit" name="search" value="yes">${message(code: 'default.button.filter.label')}</button>
            </div>
        </div>
      </g:form>
    </ui:filter>

    <div class="ui grid la-clear-before">
        <div class="sixteen wide column">

             <g:if test="${hits && resultsTotal > 0}" >
               <ui:messages data="${flash}" />

               <div class="ui icon info message">
                 <i class="exclamation triangle icon"></i>
                 <i class="close icon"></i>
                 <div class="content">
                   <div class="header">
                     ${message(code: 'message.attention')}
                   </div>
                   <p>${message(code: 'message.attention.needTime')}</p>
                 </div>
               </div>

                <div id="resultsarea" class="la-clear-before">
                  <table class="ui sortable celled la-js-responsive-table la-table table">
                    <thead>
                      <tr>
                          <th>${message(code:'sidewide.number')}</th>
                      <g:sortableColumn property="sortname.keyword" title="${message(code: 'default.title.label')}" params="${params}" />
                      <g:sortableColumn property="type.value" title="${message(code: 'title.type.label')}" params="${params}" />
                      <g:sortableColumn property="publishers.name" style="white-space:nowrap" title="${message(code: 'tipp.publisher')}" params="${params}" />
                      <th style="white-space:nowrap"><g:message code="title.identifiers.label" /></th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit" status="jj">
                        <tr>
                          <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                          <td>
                            <%-- ? needed because there are TIPP residuals from TitleInstance era which have no type set --%>
                            <ui:listIcon type="${hit.getSourceAsMap().type?.value}"/>
                            <strong><g:link controller="tipp" action="show" id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link></strong>
                          </td>
                          <td>
                            ${hit.getSourceAsMap().type ? RefdataValue.getByValueAndCategory(hit.getSourceAsMap().type.value, RDConstants.TITLE_MEDIUM)?.getI10n('value') : message(code: 'default.title.label')}
                          </td>
                          <td>
                            <g:if test="${hit.getSourceAsMap().publisher}">
                              <g:link controller="organisation" action="show" id="${hit.getSourceAsMap().publisher.id}">
                                ${hit.getSourceAsMap().publisher.name}
                              </g:link>
                            </g:if>

                          </td>
                          <td>
                            <g:each in="${hit.getSourceAsMap().identifiers?.sort{it.type}}" var="id">
                              <div style="white-space:nowrap"><span>${id.type}:</span> <span>${id.value}</span></div>
                            </g:each>
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                </div><!-- #resultsarea -->
             </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"title.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object" args="${[message(code:"title.plural")]}"/></strong>
                </g:else>
            </g:else>

              <g:if test="${hits}" >
                <ui:paginate controller="title" action="index" params="${params}" next="${message(code: 'default.paginate.next')}" prev="${message(code: 'default.paginate.prev')}" maxsteps="10" total="${resultsTotal}" />
              </g:if>

        </div><!-- .sixteen -->
      </div><!-- .grid -->
<laser:htmlEnd />
