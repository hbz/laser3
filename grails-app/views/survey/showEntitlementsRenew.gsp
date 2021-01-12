<%@ page import="de.laser.titles.BookInstance; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.Org" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}" />
        <semui:crumb class="active" controller="survey" action="surveyTitlesEvaluation" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" message="surveyTitlesEvaluation.label" />
    </g:if>

</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="showEntitlementsRenew" id="${surveyConfig.id}"
                    params="${[exportKBart: true, participant: participant?.id]}">KBART Export</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="showEntitlementsRenew" id="${surveyConfig.id}"
                    params="${[exportXLSX: true, participant: participant?.id]}">${message(code: 'default.button.exports.xls')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    <semui:actionsDropdown>
        <g:if test="${surveyOrg.finishDate && surveyInfo && surveyInfo.status?.id == RDStore.SURVEY_SURVEY_STARTED.id}">
            <semui:actionsDropdownItem controller="survey" action="openIssueEntitlementsSurveyAgain"
                                       params="[id: surveyConfig?.id, participant: participant?.id]"
                                       message="openIssueEntitlementsSurveyAgain.label"/>
        </g:if>

        <g:if test="${surveyInfo && surveyInfo.status?.id in [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_COMPLETED.id]}">
            <semui:actionsDropdownItem controller="survey" action="completeIssueEntitlementsSurveyforParticipant"
                                       params="[id: surveyConfig?.id, participant: participant?.id]"
                                       message="completeIssueEntitlementsSurvey.forParticipant.label"/>
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled tooltip="${message(code: 'renewEntitlementsWithSurvey.noCompleted')}" controller="survey"
                                               action="completeIssueEntitlementsSurveyforParticipant"
                                               message="completeIssueEntitlementsSurvey.forParticipant.label"/>
        </g:else>

    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon type="Survey"/>
<g:message code="issueEntitlementsSurvey.label"/>: <g:link controller="subscription" action="index"
                                                           id="${subscriptionParticipant?.id}">${surveyConfig?.surveyInfo.name}</g:link>
</h1>


<g:if test="${flash}">
    <semui:messages data="${flash}"/>
</g:if>

<g:if test="${participant}">
    <g:set var="choosenOrg" value="${Org.findById(participant?.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <table class="ui table la-table compact">
        <tbody>
        <tr>
            <td>
                <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                ${choosenOrg?.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <g:render template="/templates/cpa/person_details"
                                  model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
            </td>
        </tr>
        </tbody>
    </table>
</g:if>

%{--<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant)?.finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info"/>
            </p>
        </div>
    </div>
</g:if>--}%

<semui:form>

    <h2 class="ui header left aligned aligned"><g:message
            code="renewEntitlementsWithSurvey.currentEntitlements"/> (${ies.size() ?: 0})</h2>

    <div class="ui grid">
        <div class="sixteen wide column">
            <g:set var="counter" value="${1}"/>
            <g:set var="sumlistPrice" value="${0}"/>
            <g:set var="sumlocalPrice" value="${0}"/>


            <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header">
                <thead>
                <tr>
                    <th>${message(code: 'sidewide.number')}</th>
                    <th><g:message code="title.label"/></th>
                    <th><g:message code="tipp.coverage"/></th>
                    <th class="two wide"><g:message code="tipp.price"/></th>

                </tr>
                </thead>
                <tbody>

                <g:each in="${ies}" var="ie">
                    <g:set var="tipp" value="${ie.tipp}"/>
                    <tr>
                    <td>${counter++}</td>
                    <td class="titleCell">
                        <semui:ieAcceptStatusIcon status="${ie?.acceptStatus}"/>

                        <semui:listIcon type="${ie.tipp.title.class.name}"/>
                        <strong><g:link controller="title" action="show"
                                        id="${tipp.title.id}">${tipp.title.title}</g:link></strong>

                        <g:if test="${tipp.hostPlatformURL}">
                            <semui:linkIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
                        </g:if>
                        <br />

                        <div class="la-icon-list">
                            <g:if test="${tipp.title instanceof BookInstance && tipp.title.volume}">
                                <div class="item">
                                    <i class="grey icon la-books la-popup-tooltip la-delay"
                                       data-content="${message(code: 'tipp.volume')}"></i>

                                    <div class="content">
                                        ${tipp.title.volume}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${tipp.title instanceof BookInstance && (tipp.title.firstAuthor || tipp.title.firstEditor)}">
                                <div class="item">
                                    <i class="grey icon user circle la-popup-tooltip la-delay"
                                       data-content="${message(code: 'author.slash.editor')}"></i>

                                    <div class="content">
                                        ${tipp.title.getEbookFirstAutorOrFirstEditor()}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${tipp.title instanceof BookInstance && tipp.title.editionStatement}">
                                <div class="item">
                                    <i class="grey icon copy la-popup-tooltip la-delay"
                                       data-content="${message(code: 'title.editionStatement.label')}"></i>

                                    <div class="content">
                                        ${tipp.title.editionStatement}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${tipp.title instanceof BookInstance && tipp.title.summaryOfContent}">
                                <div class="item">
                                    <i class="grey icon desktop la-popup-tooltip la-delay"
                                       data-content="${message(code: 'title.summaryOfContent.label')}"></i>

                                    <div class="content">
                                        ${tipp.title.summaryOfContent}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${tipp.title.seriesName}">
                                <div class="item">
                                    <i class="grey icon list la-popup-tooltip la-delay" data-content="${message(code: 'title.seriesName.label')}"></i>
                                    <div class="content">
                                        ${tipp.title.seriesName}
                                    </div>
                                </div>
                            </g:if>

                            <g:if test="${tipp.title.subjectReference}">
                                <div class="item">
                                    <i class="grey icon comment alternate la-popup-tooltip la-delay" data-content="${message(code: 'title.subjectReference.label')}"></i>
                                    <div class="content">
                                        ${tipp.title.subjectReference}
                                    </div>
                                </div>
                            </g:if>

                        </div>

                        <g:each in="${tipp.title.ids?.sort { it.ns.ns }}" var="id">
                            <g:if test="${id.ns.ns == 'originEditUrl'}">
                            <%--<span class="ui small blue image label">
                                ${id.ns.ns}: <div class="detail"><a
                                    href="${id.value}">${message(code: 'package.show.openLink')}</a>
                            </div>
                            </span>
                            <span class="ui small blue image label">
                                ${id.ns.ns}: <div class="detail"><a
                                    href="${id.value.toString().replace("resource/show", "public/packageContent")}">${message(code: 'package.show.openLink')}</a>
                            </div>
                            </span>--%>
                            </g:if>
                            <g:else>
                                <span class="ui small blue image label">
                                    ${id.ns.ns}: <div class="detail">${id.value}</div>
                                </span>
                            </g:else>
                        </g:each>

                        <div class="la-icon-list">

                        %{-- <g:if test="${tipp.availabilityStatus?.getI10n('value')}">
                             <div class="item">
                                 <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.access.label')}"></i>
                                 <div class="content">
                                     ${tipp.availabilityStatus?.getI10n('value')}
                                 </div>
                             </div>
                         </g:if>--}%

                            <g:if test="${tipp.status.getI10n("value")}">
                                <div class="item">
                                    <i class="grey key icon la-popup-tooltip la-delay"
                                       data-content="${message(code: 'default.status.label')}"></i>

                                    <div class="content">
                                        ${tipp.status.getI10n("value")}
                                    </div>
                                </div>
                            </g:if>


                            <div class="item">
                                <i class="grey icon gift scale la-popup-tooltip la-delay"
                                   data-content="${message(code: 'package.label')}"></i>

                                <div class="content">
                                    <g:link controller="package" action="show"
                                            id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                                </div>
                            </div>

                            <div class="item">
                                <i class="grey icon cloud la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.tooltip.changePlattform')}"></i>

                                <div class="content">
                                    <g:if test="${tipp?.platform.name}">
                                        <g:link controller="platform" action="show" id="${tipp?.platform.id}">
                                            ${tipp?.platform.name}
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        ${message(code: 'default.unknown')}
                                    </g:else>
                                </div>
                            </div>


                            <g:if test="${tipp?.id}">
                                <div class="la-title">${message(code: 'default.details.label')}</div>
                                <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                        data-content="${message(code: 'laser')}"
                                        href="${tipp?.hostPlatformURL.contains('http') ? tipp?.hostPlatformURL : 'http://' + tipp?.hostPlatformURL}"
                                        target="_blank"
                                        controller="tipp" action="show"
                                        id="${tipp?.id}">
                                    <i class="book icon"></i>
                                </g:link>
                            </g:if>
                            <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                                    var="gokbAPI">
                                <g:if test="${tipp?.gokbId}">
                                    <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                       data-content="${message(code: 'gokb')}"
                                       href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}"
                                       target="_blank"><i class="la-gokb  icon"></i>
                                    </a>
                                </g:if>
                            </g:each>

                        </div>
                    </td>
                    <td>
                        <g:if test="${tipp.title instanceof BookInstance}">
                        <%-- TODO contact Ingrid! ---> done as of subtask of ERMS-1490 --%>
                            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${tipp.title.dateFirstInPrint}"/>
                            <br />
                            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${tipp.title.dateFirstOnline}"/>
                        </g:if>
                        <g:else>
                        <%-- TODO: FOR JOURNALS --%>
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${ie.priceItem}">
                            <g:formatNumber number="${ie?.priceItem?.listPrice}" type="currency"
                                            currencySymbol="${ie?.priceItem?.listCurrency}"
                                            currencyCode="${ie?.priceItem?.listCurrency}"/><br />
                            <g:formatNumber number="${ie?.priceItem?.localPrice}" type="currency"
                                            currencySymbol="${ie?.priceItem?.localCurrency}"
                                            currencyCode="${ie?.priceItem?.localCurrency}"/><br />
                        %{--<semui:datepicker class="ieOverwrite" name="priceDate" value="${ie?.priceItem?.priceDate}" placeholder="${message(code:'tipp.priceDate')}"/>--}%

                            <g:set var="sumlistPrice" value="${sumlistPrice + (ie?.priceItem?.listPrice ?: 0)}"/>
                            <g:set var="sumlocalPrice" value="${sumlocalPrice + (ie?.priceItem?.localPrice ?: 0)}"/>

                        </g:if>
                    </td>

                </g:each>
                </tbody>
                <tfoot>
                <tr>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th><g:message code="financials.export.sums"/> <br />
                        <g:message code="tipp.listPrice"/>: <g:formatNumber number="${sumlistPrice}"
                                                                            type="currency"/><br />
                        %{--<g:message code="tipp.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
                    </th>
                    <th></th>
                </tr>
                </tfoot>
            </table>
        </div>

    </div>

</semui:form>

</body>
</html>
