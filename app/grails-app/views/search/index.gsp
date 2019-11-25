<%@ page import="com.k_int.kbplus.SurveyConfig; de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue; java.text.SimpleDateFormat;" %>
<laser:serviceInjection/>
<%-- r:require module="annotations" / --%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'search.global')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="search.global" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><i class="circular icon inverted blue search"></i> ${message(code: 'search.global')}
</h1>

<%
    def addFacet = { params, facet, val ->
        def newparams = [:]
        newparams.putAll(params)
        newparams.remove('offset');
        newparams.remove('max');
        def current = newparams[facet]
        if (current == null) {
            newparams[facet] = val
        } else if (current instanceof String[]) {
            newparams.remove(current)
            newparams[facet] = current as List
            newparams[facet].add(val);
        } else {
            newparams[facet] = [current, val]
        }
        newparams
    }
    def removeFacet = { params, facet, val ->
        def newparams = [:]
        newparams.putAll(params)
        def current = newparams[facet]
        newparams.remove('offset');
        newparams.remove('max');
        if (current == null) {
        } else if (current instanceof String[]) {
            newparams.remove(current)
            newparams[facet] = current as List
            newparams[facet].remove(val);
        } else if (current?.equals(val.toString())) {
            newparams.remove(facet);
        }
        newparams
    }
%>


<semui:form>
    <g:form action="index" controller="search" method="get" class="ui form">

        <g:each in="${['typ', 'endYear', 'startYear', 'consortiaName', 'providerName']}" var="facet">
            <g:each in="${params.list(facet)}" var="selected_facet_value"><input type="hidden" name="${facet}"
                                                                                 value="${selected_facet_value}"/></g:each>
        </g:each>
        <div class="field">
            <label><g:message code="home.search.text" default="Search Text"/> :</label>
            <input type="text" placeholder="Search Text" name="q" value="${params.q}"/>
        </div>

        <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}"
               class="ui reset primary button">${message(code: 'default.button.searchreset.label')}</a>
            <button name="search" type="submit" value="true" class="ui secondary button">
                <g:message code="home.search.button" default="Search"/>
            </button>
        </div>

    </g:form>
</semui:form>


<g:if test="${hits}">

    <p>
        <g:each in="${['type', 'endYear', 'startYear', 'consortiaName', 'providerName']}" var="facet">
            <g:each in="${params.list(facet)}" var="fv">

                <span class="ui facet-${facet} label"><g:message code="facet.so.${facet}"/>: ${fv}
                    <g:link controller="search" action="index" params="${removeFacet(params, facet, fv)}">
                        <i class="delete icon"></i>
                    </g:link>
                </span>
            </g:each>
        </g:each>
    </p>

    <div class="ui info message">
        <g:message code="home.search.result" default="Your search found ${resultsTotal} records"
                   args="${resultsTotal}"/>
    </div>
    <br>


        <div class="ui segment">
            <div class="ui left dividing rail">
                <div class="ui segment">

                    <h2>Filter:</h2>

                    <g:each in="${facets}" var="facet">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h3 class="ui header"><g:message code="facet.so.${facet.key}"
                                                                 default="${facet.key}"/></h3>
                            </div>

                            <div class="panel-body">
                                <ul>
                                    <g:each in="${facet.value.sort { message(code:"spotlight.${it.display.toLowerCase()}") }}" var="v">
                                        <li>

                                            <g:if test="${params.list(facet.key).contains(v.term.toString())}">
                                                ${(facet.key == type) ? message(code:"spotlight.${v.display.toLowerCase()}") : v.display} (${v.count})
                                            </g:if>
                                            <g:else>
                                                <g:link controller="search" action="index" params="${addFacet(params, facet.key, v.term)}">
                                                    ${(facet.key == type) ? message(code:"spotlight.${v.display.toLowerCase()}") : v.display}
                                                </g:link> (${v.count})
                                            </g:else>
                                        </li>
                                    </g:each>
                                </ul>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>
            <div class="ui stackable grid">
            <div class="sixteen wide column">

                <table class="ui celled sortable table table-tworow la-table">
                    <tr><th></th><th>Title/Name</th><th>${message(code: 'home.search.additionalinfo', default: "Additional Info")}</th>
                    </tr>
                    <g:each in="${hits}" var="hit">
                        <tr>
                            <td>
                                ${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}
                            </td>
                            <g:if test="${hit.getSource().rectype == 'Organisation'}">
                                <td><g:link controller="organisation" action="show"
                                            id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td></td>
                            </g:if>
                            <g:if test="${hit.getSource().rectype == 'Title'}">
                                <td><g:link controller="title" action="show"
                                            id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td>
                                    <g:each in="${hit.getSource().identifiers}" var="id">
                                        ${id.type}:${id.value} &nbsp;
                                    </g:each>
                                </td>
                            </g:if>
                            <g:if test="${hit.getSource().rectype == 'Package'}">
                                <td><g:link controller="package" action="show"
                                            id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td></td>
                            </g:if>
                            <g:if test="${hit.getSource().rectype == 'Platform'}">
                                <td><g:link controller="platform" action="show"
                                            id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td></td>
                            </g:if>
                            <g:if test="${hit.getSource().rectype == 'Subscription'}">
                                <td><g:link controller="subscription" action="show"
                                            id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td>${hit.getSource().identifier}</td>
                            </g:if>
                            <g:if test="${hit.getSource().rectype == 'License'}">
                                <td><g:link controller="license" action="show"
                                            id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td></td>
                            </g:if>

                            <g:if test="${hit.getSource().rectype == 'Survey'}">
                                <td><g:link controller="survey" action="show" id="${SurveyConfig.get(hit.getSource().dbId).surveyInfo.id}" params="[surveyConfigID: hit.getSource().dbId]">${hit.getSource().name}</g:link></td>
                                <td></td>
                            </g:if>
                            <g:if test="${hit.getSource().rectype == 'ParticipantSurvey'}">
                                <td><g:link controller="myInstitution" action="surveyInfos" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link></td>
                                <td></td>
                            </g:if>
                        </tr>
                    </g:each>
                </table>

                <semui:paginate action="index" controller="search" params="${params}"
                                next="${message(code: 'default.paginate.next', default: 'Next')}"
                                prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                                total="${resultsTotal}"/>

            </div>
        </div>
    </div>
</g:if>

</body>
</html>
