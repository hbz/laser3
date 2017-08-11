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
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>

  <body>

    <div class="container">
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <g:if test="${subscriptionInstance.subscriber}">
          <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:subscriptionInstance.subscriber.shortcode]}"> ${subscriptionInstance.subscriber.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'default.notes.label', default:'Notes')}</g:link> </li>
        <g:if test="${editable}">
          <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
        </g:if>
      </ul>
    </div>

    <div class="container">
       <h1>${subscriptionInstance.name} : ${message(code:'subscription.details.linkPackage.heading', default:'Link Subscription to Packages')}</h1>
       <g:render template="nav" contextPath="." />
    </div>

    <div class="container">
      <g:form name="LinkPackageForm" action="linkPackage" method="get" params="${params}">
      <input type="hidden" name="offset" value="${params.offset}"/>
      <input type="hidden" name="id" value="${params.id}"/>
      <div class="row">
        <div class="span12">
          <div class="well">
            ${message(code:'package.show.pkg_name', default:'Package Name')}: <input name="q" style="margin-right:10px;" value="${params.q}"/> <button type="submit" name="search" value="yes">${message(code:'default.button.search.label', default:'Search')}</button>
          </div>
        </div>
      </div>
      <div class="row">
      <p>
          <g:each in="${['type','endYear','startYear','consortiaName','cpname']}" var="facet">
            <g:each in="${params.list(facet)}" var="fv">
              <span class="badge alert-info">${facet}:${fv} &nbsp; <g:link controller="${controller}" action="linkPackage" params="${removeFacet(params,facet,fv)}"><i class="icon-remove icon-white"></i></g:link></span>
            </g:each>
          </g:each>
      </p>

        <div class="facetFilter span2">
          <g:each in="${facets}" var="facet">
            <div class="panel panel-default">
              <div class="panel-heading">
                <h3><g:message code="facet.so.${facet.key}" default="${facet.key}" /></h3>
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
                        <g:link controller="${controller}" action="linkPackage" params="${addFacet(params,facet.key,v.term)}">${v.display}</g:link> (${v.count})
                      </g:else>
                    </li>
                  </g:each>
                </ul>
              </div>
            </div>
          </g:each>
        </div>
        <div class="span8">
          <div class="well">
             <g:if test="${hits}" >
                <div class="paginateButtons" style="text-align:center">
                  <g:if test="${params.int('offset')}">
                    ${message(code:'title.search.offset.text', args:[(params.int('offset') + 1),(resultsTotal < (params.int('max') + params.int('offset')) ? resultsTotal : (params.int('max') + params.int('offset'))),resultsTotal])}
                  </g:if>
                  <g:elseif test="${resultsTotal && resultsTotal > 0}">
                    ${message(code:'title.search.no_offset.text', args:[(resultsTotal < params.int('max') ? resultsTotal : params.int('max')),resultsTotal])}
                  </g:elseif>
                  <g:else>
                    ${message(code:'title.search.no_pagiantion.text', args:[resultsTotal])}
                  </g:else>
                </div>

                <div id="resultsarea">
                  <table class="table table-bordered table-striped">
                    <thead>
                      <tr><th>${message(code:'package.show.pkg_name', default:'Package Name')}</th><th>${message(code:'consortium.label', default:'Consortium')}</th><th>${message(code:'default.action.label', default:'Action')}</th></tr>
                    </thead>
                    <tbody>
                      <g:each in="${hits}" var="hit">
                          <tr>
                            <td><g:link controller="packageDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name} </g:link>(${hit.getSource()?.titleCount?:'0'} ${message(code:'title.plural', default:'Titles')})</td>
                            <td>${hit.getSource().consortiaName}</td>
                            <td>
                              <g:if test="${!pkgs || !pkgs.contains(hit.getSource().dbId.toLong())}">
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
                      </g:each>
                    </tbody>
                  </table>
                </div>
             </g:if>
             <div class="paginateButtons" style="text-align:center">
                <g:if test="${hits}" >
                  <span><g:paginate controller="subscriptionDetails" action="linkPackage" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${resultsTotal}" /></span>
                </g:if>
              </div>
          </div>
        </div>
        <div class="span2">
          <div class="well" style="word-break:normal;">
            <h4>${message(code:'subscription.details.linkPackage.current', default:'Current Links')}</h4>
            <hr/>
            <g:each in="${subscriptionInstance.packages}" var="sp">
              <p><g:link controller="packageDetails" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link></p>
            </g:each>
          </div>
        </div>
      </div>
      </g:form>
    </div>    
    <!-- ES Query String: ${es_query} -->
  </body>
</html>
