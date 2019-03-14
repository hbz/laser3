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

%{--<g:link action="checkPackageTIPPs" params="${params+[onlyNotEqual: true]}" class="ui button">Show only where Tipps Not Equal</g:link>--}%
<br>
<br>
<div class="ui grid">

        <div>
            <g:if test="${records}">
                <div>
                    <table class="ui celled la-selectable la-table table">
                        <thead>
                        <tr>
                            <th>${message(code:'package.show.pkg_name', default:'Package Name')} in GOKB</th>
                            <th>${message(code:'package.show.pkg_name', default:'Package Name')} in LAS:eR</th>
                            <%--<th>${message(code:'consortium.label', default:'Consortium')}</th>--%>
                            <th>${message(code:'tipp.plural', default:'Tipps')} in GOKB</th>
                             <th>${message(code:'tipp.plural', default:'Tipps')} in LAS:eR</th>
                        </thead>
                        <tbody>

                        <g:each in="${records}" var="hit" >
                            <tr>
                                <td>
                                    ${hit.name} <a target="_blank" href="${es_host_url ? es_host_url+'/gokb/resource/show/'+hit.uuid : '#'}" ><i title="GOKB Link" class="external alternate icon"></i></a>
                                </td>

                                    <g:if test="${com.k_int.kbplus.Package.findByGokbId(hit.uuid)}">
                                        <g:set var="style" value="${(com.k_int.kbplus.Package.findByGokbId(hit.uuid)?.name != hit.name) ? "style=background-color:red;":''}"/>
                                        <td ${style}>
                                        <g:link controller="package" target="_blank" action="show" id="${com.k_int.kbplus.Package.findByGokbId(hit.uuid).id}">${com.k_int.kbplus.Package.findByGokbId(hit.uuid).name}</g:link>
                                        </td>
                                    </g:if>
                                    <g:else>
                                        <td>
                                                No Package in LAS:eR
                                        </td>
                                    </g:else>
                                <td>
                                    <b>${hit.titleCount?:'0'} </b>
                                </td>
                                <g:if test="${com.k_int.kbplus.Package.findByGokbId(hit.uuid)}">
                                    <g:set var="laserTipps" value="${(com.k_int.kbplus.Package.findByGokbId(hit.uuid)?.tipps?.findAll {it.status.value == 'Current'}.size().toString())}" />
                                    <g:set var="style" value="${(laserTipps != hit.titleCount && hit.titleCount != '0') ? "style=background-color:red;":''}"/>
                                    <td ${style}>
                                            <b>${laserTipps ?:'0'} </b>
                                    </td>
                                </g:if>
                                <g:else>
                                        <td>
                                                No Tipps.
                                        </td>
                                </g:else>

                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </div>

                <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                                next="${message(code: 'default.paginate.next', default: 'Next')}"
                                prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                                max="${max}"
                                total="${resultsTotal2}"/>
            </g:if>

        </div>

</div>
</body>
</html>
