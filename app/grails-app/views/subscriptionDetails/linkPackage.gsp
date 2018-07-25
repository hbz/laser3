<!doctype html>
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

%>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
            <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" text="${subscriptionInstance.name}" />
            <semui:crumb class="active" text="${message(code:'subscription.details.linkPackage.heading', default:'Link Subscription to Packages')}" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

        <h1 class="ui header"><semui:headerIcon />${subscriptionInstance.name} : ${message(code:'subscription.details.linkPackage.heading', default:'Link Subscription to Packages')}</h1>

        <g:render template="nav" contextPath="." />

    <semui:filter>
        <g:form name="LinkPackageForm" action="linkPackage" method="get" params="${params}" class="ui form">
            <input type="hidden" name="offset" value="${params.offset}"/>
            <input type="hidden" name="id" value="${params.id}"/>
                <div class="field">
                    <label>${message(code:'package.show.pkg_name', default:'Package Name')}</label>
                    <input name="q" value="${params.q}"/>
                </div>
                <div class="field">
                    <button type="submit" name="search" value="yes" class="ui secondary button">${message(code:'default.button.filter.label', default:'Filter')}</button>
                    <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.filterreset.label')}</a>
                </div>
        </g:form>
    </semui:filter>


        <div class="ui modal" id="durationAlert">
        <div class="ui message icon">

            <i class="notched circle loading icon"></i>
            <div class="content">
                <div class="header">
                    <g:message code="globalDataSync.requestProcessing" />
                </div>
                <g:message code="globalDataSync.requestProcessingInfo" />

            </div>
        </div>
        </div>

        <r:script language="JavaScript">
            function toggleAlert() {
                $('#durationAlert').toggle();
            }
        </r:script>

      <div class="ui grid">

          <div class="sixteen wide column">
              <g:each in="${['type','endYear','startYear','consortiaName','cpname']}" var="facet">
                <g:each in="${params.list(facet)}" var="fv">
                  <span class="badge alert-info">${facet}:${fv} &nbsp; <g:link controller="${controller}" action="linkPackage" params="${removeFacet(params,facet,fv)}"><i class="icon-remove icon-white"></i></g:link></span>
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
        </div>

        <div class="eight wide column">
          <div>
             <g:if test="${hits}">
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
                  <table class="ui celled la-table table">
                    <thead>
                      <tr>
                          <th>${message(code:'package.show.pkg_name', default:'Package Name')}</th>
                          <%--<th>${message(code:'consortium.label', default:'Consortium')}</th>--%>
                          <th>${message(code:'default.action.label', default:'Action')}</th></tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit" >
                          <g:if test="${!params.esgokb}">
                          <tr>
                              <td><g:link controller="packageDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name} </g:link>(${hit.getSource()?.titleCount?:'0'} ${message(code:'title.plural', default:'Titles')})</td>
                              <%--<td>${hit.getSource().consortiaName}</td>--%>
                              <td>
                                <g:if test="${editable && (!pkgs || !pkgs.contains(hit.getSource().dbId.toLong()))}">
                                  <g:link action="linkPackage"
                                      id="${params.id}"
                                      params="${[addId:hit.getSource().dbId,addType:'Without']}"
                                      style="white-space:nowrap;"
                                      onClick="return confirm('${message(code:'subscription.details.link.no_ents.confirm', default:'Are you sure you want to add without entitlements?')}');">${message(code:'subscription.details.link.no_ents', default:'Link (no Entitlements)')}</g:link>
                                  <br/>
                                  <g:link action="linkPackage"
                                      id="${params.id}"
                                      params="${[addId:hit.getSource().dbId,addType:'With']}"
                                      style="white-space:nowrap;"
                                      onClick="return confirm('${message(code:'subscription.details.link.with_ents.confirm', default:'Are you sure you want to add with entitlements?')}');">${message(code:'subscription.details.link.with_ents', default:'Link (with Entitlements)')}</g:link>
                                </g:if>
                                <g:else>
                                  <span></span>
                                </g:else>
                              </td>
                            </tr>
                          </g:if><g:else>
                          <tr>
                              <td>${hit.getSource().name}
                                  <br><b>(${hit.getSource().tippsCountCurrent?:'0'} ${message(code:'title.plural', default:'Titles')})</b>
                              </td>

                              <td>
                                  <g:if test="${editable && (!pkgs || !pkgs.contains(hit.id))}">
                                      <g:link action="linkPackage"
                                              id="${params.id}"
                                              params="${[addId:hit.id, addType:'Without', esgokb: 'Package']}"
                                              style="white-space:nowrap;"
                                              onClick="return confirm('${message(code:'subscription.details.link.no_ents.confirm', default:'Are you sure you want to add without entitlements?')}'); toggleAlert();">${message(code:'subscription.details.link.no_ents', default:'Link (no Entitlements)')}</g:link>
                                      <br/>
                                      <g:link action="linkPackage"
                                              id="${params.id}"
                                              params="${[addId:hit.id, addType:'With', esgokb: 'Package']}"
                                              style="white-space:nowrap;"
                                              onClick="return confirm('${message(code:'subscription.details.link.with_ents.confirm', default:'Are you sure you want to add with entitlements?')}'); toggleAlert();">${message(code:'subscription.details.link.with_ents', default:'Link (with Entitlements)')}</g:link>
                                  </g:if>
                                  <g:else>
                                      <span></span>
                                  </g:else>
                              </td>
                          </tr>
                      </g:else>
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
          <g:if test="${hits}" >
              <semui:paginate action="linkPackage" controller="subscriptionDetails" params="${params}"
                              next="${message(code: 'default.paginate.next', default: 'Next')}"
                              prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                              total="${resultsTotal}"/>
          </g:if>
    </div>
  </div>

  <div class="four wide column">
      <div class="ui card">
          <div class="content">
              <div class="header">${message(code:'subscription.details.linkPackage.current', default:'Current Links')}</div>
          </div>
          <div class="content">
              <g:each in="${subscriptionInstance.packages}" var="sp">
                  <div class="item"><g:link controller="packageDetails" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link></div><hr>
              </g:each>
          </div>
      </div>
  </div>
</div>

<!-- ES Query String: ${es_query} -->
</body>
</html>
