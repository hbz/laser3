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
    <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
    <semui:crumb text="Package Tipps LAS:eR and GOKB" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header"> Package Tipps LAS:eR and GOKB</h1>




<g:link action="checkPackageTIPPs" params="${params+[onlyNotEqual: tippsNotEqual]}" class="ui button">Show only where Tipps Not Equal</g:link>
<div class="ui grid">

    <div class="twelve wide column">
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
                    <table class="ui celled la-selectable la-table table">
                        <thead>
                        <tr>
                            <th>${message(code:'package.show.pkg_name', default:'Package Name')} GOKB</th>
                            <th>${message(code:'package.show.pkg_name', default:'Package Name')} LAS:eR</th>
                            <%--<th>${message(code:'consortium.label', default:'Consortium')}</th>--%>
                            <th>${message(code:'tipp.plural', default:'Tipps')} GOKB</th>
                             <th>${message(code:'tipp.plural', default:'Tipps')} LAS:eR</th>
                        </thead>
                        <tbody>

                        <g:each in="${hits}" var="hit" >
                            <tr>
                                <td>
                                    ${hit.getSource().name} <a target="_blank" href="${es_host_url ? es_host_url+'/gokb/resource/show/'+hit.id : '#'}" ><i title="GOKB Link" class="external alternate icon"></i></a>
                                </td>
                                <td>
                                    <g:if test="${com.k_int.kbplus.Package.findByImpId(hit.id)}">
                                        <g:link controller="packageDetails" target="_blank" action="show" id="${com.k_int.kbplus.Package.findByImpId(hit.id).id}">${com.k_int.kbplus.Package.findByImpId(hit.id).name}</g:link>
                                    </g:if>
                                    <g:else>
                                        No Package in LAS:eR
                                    </g:else>
                                </td>
                                <td>
                                    <b>${hit.getSource().tippsCountCurrent?:'0'} ${message(code:'title.plural', default:'Titles')}</b>
                                </td>
                                <g:if test="${com.k_int.kbplus.Package.findByImpId(hit.id)}">
                                    <g:set var="style" value="${(com.k_int.kbplus.Package.findByImpId(hit.id)?.tipps?.size() != hit.getSource().tippsCountCurrent && hit.getSource().tippsCountCurrent != 0) ? "style=background-color:red;":''}"/>
                                    <td ${style}>
                                            <b>${com.k_int.kbplus.Package.findByImpId(hit.id).tipps.size() ?:'0'} ${message(code:'title.plural', default:'Titles')}</b>
                                    </td>
                                </g:if>
                                <g:else>
                                        <td>
                                                No Package in LAS:eR. No Tipps.
                                        </td>
                                </g:else>

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
            <g:if test="${hits}" >
                <semui:paginate action="checkPackageTIPPs"  params="${params}"
                                next="${message(code: 'default.paginate.next', default: 'Next')}"
                                prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                                total="${resultsTotal}"/>
            </g:if>
        </div>
    </div>


<div id="magicArea"></div>

</body>
</html>
