<%@ page import="com.k_int.kbplus.SurveyConfig; de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue; java.text.SimpleDateFormat;" %>
<laser:serviceInjection/>
<%-- r:require module="annotations" / --%>

<% SimpleDateFormat sdf = new SimpleDateFormat(message(code: 'default.date.format.notime'))

String period
%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'search.advancedSearch')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="search.advancedSearch" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><i
        class="circular icon inverted blue search"></i> ${message(code: 'search.advancedSearch')}
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
    <g:form action="index" controller="search" method="post" class="ui form" params="[tab: params.tab]">

        <g:each in="${['rectype', 'endYear', 'startYear', 'consortiaName', 'providerName', 'status']}" var="facet">
            <g:each in="${params.list(facet)}" var="selected_facet_value">
                <input type="hidden" name="${facet}" value="${selected_facet_value}"/>
            </g:each>
        </g:each>

        <div class="field">
            <label><g:message code="search.text" default="Search Text"/>:</label>
            <input type="text" placeholder="${message(code: 'search.placeholder')}" name="q" value="${params.q}"/>
        </div>


        <div class="fields">
            <div class="three wide field">
                <g:select class="ui dropdown"
                          from="${['AND', 'OR', 'NOT']}"
                          name="advancedSearchOption"
                          valueMessagePrefix="search.advancedSearch.option"
                          value="${params.advancedSearchOption}"/>
            </div>

            <div class="fourteen wide field">
                <input type="text" placeholder="${message(code: 'search.placeholder')}"
                       name="advancedSearchText" value="${params.advancedSearchText}"/>
            </div>
        </div>

        <div class="fields">
            <div class="three wide field">
                <g:select class="ui dropdown"
                          from="${['AND', 'OR', 'NOT']}"
                          name="advancedSearchOption2"
                          valueMessagePrefix="search.advancedSearch.option"
                          value="${params.advancedSearchOption2}"/>
            </div>

            <div class="fourteen wide field">
                <input type="text" placeholder="${message(code: 'search.placeholder')}"
                       name="advancedSearchText2" value="${params.advancedSearchText2}"/>
            </div>
        </div>


        <div class="three fields">
            <div class="field">
                <label>${message(code: 'search.objects')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioAllObjects" type="radio" value="allObjects" name="searchObjects"
                                   tabindex="0" class="hidden"
                                   <g:if test="${params.searchObjects == 'allObjects'}">checked=""</g:if>>
                            <label for="radioAllObjects">
                                <g:message code="menu.public"/>
                            </label>
                        </div>
                    </div>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioMyObjects" type="radio" value="myObjects" name="searchObjects"
                                   tabindex="0"
                                   class="hidden"
                                   <g:if test="${params.searchObjects == 'myObjects'}">checked=""</g:if>>
                            <label for="radioMyObjects">${message(code: 'menu.my')}</label>
                        </div>
                    </div>

                    %{--<div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioSpecObject" type="radio" value="specObjects" name="searchObjects" tabindex="0" class="hidden"
                                   <g:if test="${params.searchObjects == 'specObjects'}">checked=""</g:if>
                            >
                            <label for="radioSpecObject">${message(code: 'menu.my')}

                            <g:select class="ui dropdown" name="status"
                                      from="${ com.k_int.kbplus.FTControl.findAll() }"
                                      optionKey="id"
                                      optionValue="${{message(code: 'search.object.'+it.domainClassName.replace('com.k_int.kbplus.','').toLowerCase())}}"
                                      value="${params.searchObject}"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"/>

                            </label>
                        </div>
                    </div>--}%
                </div>

            </div>

            <div class="field">

            </div>


            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.searchreset.label')}</a>
                <button name="search" type="submit" value="true" class="ui secondary button">
                    <g:message code="search.button" default="Search"/>
                </button>
            </div>
        </div>

    </g:form>
</semui:form>



<g:if test="${hits}">

    <p>
        <g:each in="${['rectype', 'endYear', 'startYear', 'consortiaName', 'providerName', 'status']}" var="facet">
            <g:each in="${params.list(facet)}" var="fv">

                <span class="ui facet-${facet} label"><g:message code="facet.so.${facet}"/>:

                    <g:if test="${facet == 'rectype'}">
                        ${message(code: "facet.so.${facet}.${fv.toLowerCase()}")}
                    </g:if>
                    <g:elseif test="${facet == 'status'}">
                        ${RefdataValue.findByValue(fv) ? RefdataValue.findByValue(fv).getI10n('value') : fv}
                    </g:elseif>
                    <g:else>
                        ${fv}
                    </g:else>

                    <g:link controller="search" action="index" params="${removeFacet(params, facet, fv)}">
                        <i class="delete icon"></i>
                    </g:link>
                </span>
            </g:each>
        </g:each>
    </p>

    <div class="ui info message">
        <g:message code="search.result" default="Your search found ${resultsTotal} records"
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
                                <g:each in="${facet.value.sort {
                                    message(code: "facet.so.${facet.key}.${it.display.toLowerCase()}")
                                }}" var="v">
                                    <li>

                                        <g:if test="${params.list(facet.key).contains(v.term.toString())}">
                                            <g:if test="${facet.key == 'rectype'}">
                                                ${message(code: "facet.so.${facet.key}.${v.display.toLowerCase()}")} (${v.count})
                                            </g:if>
                                            <g:elseif test="${facet.key == 'status'}">
                                                ${RefdataValue.findByValue(v.display) ? RefdataValue.findByValue(v.display).getI10n('value') : v.display} (${v.count})
                                            </g:elseif>
                                            <g:else>
                                                ${v.display} (${v.count})
                                            </g:else>
                                        </g:if>
                                        <g:else>
                                            <g:link controller="search" action="index"
                                                    params="${addFacet(params, facet.key, v.term)}">
                                                <g:if test="${facet.key == 'rectype'}">
                                                    ${message(code: "facet.so.${facet.key}.${v.display.toLowerCase()}")}
                                                </g:if>
                                                <g:elseif test="${facet.key == 'status'}">
                                                    ${RefdataValue.findByValue(v.display) ? RefdataValue.findByValue(v.display).getI10n('value') : v.display}
                                                </g:elseif>
                                                <g:else>
                                                    ${v.display}
                                                </g:else>
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
                    <tr>
                        <th class="six wide">Title/Name</th>
                        <th class="ten wide ">${message(code: 'search.additionalinfo', default: "Additional Info")}</th>
                    </tr>
                    <g:each in="${hits}" var="hit">
                        <tr>
                            <g:if test="${hit.getSourceAsMap().rectype == 'Organisation'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${(hit.getSourceAsMap().sector == 'Publisher') ? message(code: 'spotlight.provideragency') : message(code: 'spotlight.'+hit.getSourceAsMap().rectype.toLowerCase())}">
                                        <i class="circular icon la-search-${hit.getSourceAsMap().rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="organisation" action="show"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>

                                </td>
                                <td>
                                    <b><g:message code="default.identifiers.label"/></b>:
                                    <g:each in="${hit.getSourceAsMap().identifiers?.sort { it.type }}" var="id">
                                        ${id.type}: ${id.value} &nbsp;
                                    </g:each>
                                    <br>
                                    <b><g:message code="org.platforms.label"/></b>:
                                    <g:each in="${hit.getSourceAsMap().platforms?.sort { it.name }}" var="platform">
                                        <g:link controller="platform" action="show"
                                                id="${platform.id}">${platform.name}</g:link>
                                    </g:each>
                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Title'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().typTitle.toLowerCase()}")}">
                                        <i class="circular icon la-search-${hit.getSourceAsMap().typTitle.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="title" action="show"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>
                                </td>
                                <td>
                                    <b><g:message code="default.identifiers.label"/></b>:
                                    <g:each in="${hit.getSourceAsMap().identifiers?.sort { it.type }}" var="id">
                                        ${id.type}: ${id.value} &nbsp;
                                    </g:each>
                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Package'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon la-search-${hit.getSourceAsMap().rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="package" action="show"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>
                                </td>
                                <td>
                                    <b><g:message code="default.identifiers.label"/></b>:
                                    <g:each in="${hit.getSourceAsMap().identifiers?.sort { it.type }}" var="id">
                                        ${id.type}: ${id.value} &nbsp;
                                    </g:each>
                                    <br>
                                    <b>${message(code: 'package.compare.overview.tipps')}</b>:
                                    <g:link controller="package" action="index"
                                        id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().titleCountCurrent}</g:link>

                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Platform'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon la-search-${hit.getSourceAsMap().rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="platform" action="show"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>
                                </td>
                                <td>
                                    <b>${message(code: 'package.compare.overview.tipps')}</b>:
                                        <g:link controller="platform" action="platformTipps"
                                        id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().titleCountCurrent}</g:link>
                                    <br>
                                    <b>${message(code: 'platform.primaryURL')}</b>: ${hit.getSourceAsMap().primaryUrl}
                                    <br>
                                    <b>${message(code: 'platform.org')}</b>:
                                <g:link controller="organisation" action="show"
                                        id="${hit.getSourceAsMap().orgId}">${hit.getSourceAsMap().orgName}</g:link>

                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Subscription'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon la-search-${hit.getSourceAsMap().rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="subscription" action="show"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <%
                                        period = hit.getSourceAsMap().startDate ? sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().startDate)) : ''
                                        period = hit.getSourceAsMap().endDate ? period + ' - ' + sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().endDate)) : ''
                                        period = period ? period : ''
                                    %>
                                    <b><g:message code="default.identifiers.label"/></b>:
                                    <g:each in="${hit.getSourceAsMap().identifiers?.sort { it.type }}" var="id">
                                        ${id.type}: ${id.value} &nbsp;
                                    </g:each>
                                    <br>

                                    <b>${message(code: 'subscription.status.label')}</b>: ${RefdataValue.findByValue(hit.getSourceAsMap().status) ? RefdataValue.findByValue(hit.getSourceAsMap().status).getI10n('value') : hit.getSourceAsMap().status}
                                    <br>
                                    <b>${message(code: 'subscription.periodOfValidity.label')}</b>: ${period}
                                    <br>
                                    <g:if test="${contextOrg.getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']}">
                                        <b>${message(code: 'subscription.details.consortiaMembers.label')}</b>:
                                        <g:link controller="subscription" action="members"
                                                id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().members}</g:link>
                                    </g:if>
                                    <br>
                                    <b>${message(code: 'facet.so.consortiaName')}</b>: ${hit.getSourceAsMap().consortiaName}
                                </td>
                            </g:if>
                            <g:if test="${hit.getSourceAsMap().rectype == 'License'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon la-search-${hit.getSourceAsMap().rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="license" action="show"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <%
                                        period = hit.getSourceAsMap().startDate ? sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().startDate)) : ''
                                        period = hit.getSourceAsMap().endDate ? period + ' - ' + sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().endDate)) : ''
                                        period = period ? period : ''
                                    %>

                                    <b><g:message code="default.identifiers.label"/></b>:
                                    <g:each in="${hit.getSourceAsMap().identifiers?.sort { it.type }}" var="id">
                                        ${id.type}: ${id.value} &nbsp;
                                    </g:each>
                                    <br>
                                    <b>${message(code: 'license.status')}</b>: ${RefdataValue.findByValue(hit.getSourceAsMap().status) ? RefdataValue.findByValue(hit.getSourceAsMap().status).getI10n('value') : hit.getSourceAsMap().status}
                                    <br>
                                    <b>${message(code: 'subscription.periodOfValidity.label')}</b>: ${period}
                                    <br>
                                    <g:if test="${contextOrg.getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']}">
                                        <b>${message(code: 'subscription.details.consortiaMembers.label')}</b>:
                                        <g:link controller="license" action="members"
                                                id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().members}</g:link>
                                    </g:if>
                                    <br>
                                    <b>${message(code: 'facet.so.consortiaName')}</b>: ${hit.getSourceAsMap().consortiaName}
                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Survey'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted blue chart pie"></i>
                                    </span>

                                    <g:link controller="survey" action="show"
                                            id="${SurveyConfig.get(hit.getSourceAsMap().dbId).surveyInfo.id}"
                                            params="[surveyConfigID: hit.getSourceAsMap().dbId]">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>

                                </td>
                                <td>
                                    <%
                                        period = hit.getSourceAsMap().startDate ? sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().startDate)) : ''
                                        period = hit.getSourceAsMap().endDate ? period + ' - ' + sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().endDate)) : ''
                                        period = period ? period : ''
                                    %>

                                    <b>${message(code: 'surveyInfo.status.label')}</b>: ${RefdataValue.findByValue(hit.getSourceAsMap().status) ? RefdataValue.findByValue(hit.getSourceAsMap().status).getI10n('value') : hit.getSourceAsMap().status}
                                    <br>
                                    <b>${message(code: 'renewalWithSurvey.period')}</b>: ${period}
                                    <br>
                                    <g:if test="${contextOrg.getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']}">
                                        <b>${message(code: 'surveyParticipants.label')}</b>: ${hit.getSourceAsMap().members}
                                    </g:if>
                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'ParticipantSurvey'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted blue chart pie"></i>
                                    </span>

                                    <g:link controller="myInstitution" action="surveyInfos"
                                            id="${hit.getSourceAsMap().dbId}">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <%
                                        period = hit.getSourceAsMap().startDate ? sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().startDate)) : ''
                                        period = hit.getSourceAsMap().endDate ? period + ' - ' + sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().endDate)) : ''
                                        period = period ? period : ''
                                    %>

                                    <b>${message(code: 'surveyInfo.status.label')}</b>: ${RefdataValue.findByValue(hit.getSourceAsMap().status) ? RefdataValue.findByValue(hit.getSourceAsMap().status).getI10n('value') : hit.getSourceAsMap().status}
                                    <br>
                                    <b>${message(code: 'renewalWithSurvey.period')}</b>: ${period}
                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Task'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted green checked calendar"></i>
                                    </span>

                                    <g:link controller="myInstitution" action="tasks"
                                            params="[taskName: hit.getSourceAsMap().name]">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <b>${message(code: 'search.object.'+hit.getSourceAsMap().objectType)}</b>:
                                    <g:link controller="${hit.getSourceAsMap().objectType}" action="show" id="${hit.getSourceAsMap().objectId}">${hit.getSourceAsMap().objectName}</g:link>
                                    <br>
                                    <b>${message(code: 'task.status.label')}</b>: ${RefdataValue.findByValue(hit.getSourceAsMap().status) ? RefdataValue.findByValue(hit.getSourceAsMap().status).getI10n('value') : hit.getSourceAsMap().status}
                                    <br>
                                    <b>${message(code: 'task.endDate.label')}</b>:
                                        <g:if test="${hit.getSourceAsMap()?.endDate}">
                                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().endDate)}"/>
                                        </g:if>
                                </td>
                            </g:if>
                            <g:if test="${hit.getSourceAsMap().rectype == 'Note'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted red sticky note"></i>
                                    </span>

                                    <g:link controller="${hit.getSourceAsMap().objectType}" action="notes" id="${hit.getSourceAsMap().objectId}">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <b>${message(code: 'search.object.'+hit.getSourceAsMap().objectType)}</b>:
                                    <g:link controller="${hit.getSourceAsMap().objectType}" action="show" id="${hit.getSourceAsMap().objectId}">${hit.getSourceAsMap().objectName}</g:link>
                                    <br>

                                </td>
                            </g:if>

                            <g:if test="${hit.getSourceAsMap().rectype == 'Document'}">
                                <td>
                                    <g:set var="docContext" value="${com.k_int.kbplus.DocContext.get(hit.getSourceAsMap().dbId)}"/>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${hit.getSourceAsMap().rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted red sticky note"></i>
                                    </span>


                                    <g:link controller="${hit.getSourceAsMap().objectType}" action="documents" id="${hit.getSourceAsMap().objectId}">${hit.getSourceAsMap().name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${hit.getSourceAsMap().visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate red large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <b>${message(code: 'search.object.'+hit.getSourceAsMap().objectType)}</b>:
                                    <g:link controller="${hit.getSourceAsMap().objectType}" action="show" id="${hit.getSourceAsMap().objectId}">${hit.getSourceAsMap().objectName}</g:link>
                                    <br>
                                    <b>${message(code: 'license.docs.table.type')}</b>: ${docctx.owner?.type?.getI10n('value')}

                                </td>
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
