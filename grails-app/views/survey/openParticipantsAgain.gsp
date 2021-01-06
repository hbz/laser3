<%@ page import="de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.SurveyOrg; de.laser.SurveyConfig; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.Org" %>

<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                     text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>
    <g:if test="${params.tab == 'participantsViewAllFinish'}">
        <semui:crumb message="openParticipantsAgain.label" class="active"/>
    </g:if>
    <g:else>
        <semui:crumb message="openParticipantsAgain.reminder" class="active"/>
    </g:else>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
        </g:link>
    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>:

    <g:if test="${params.tab == 'participantsViewAllFinish'}">
        ${message(code: 'openParticipantsAgain.label')}
    </g:if>
    <g:else>
        ${message(code: 'openParticipantsAgain.reminder')}
    </g:else>
</h2>
<br />

<div class="ui grid">

    <div class="sixteen wide stretched column">
        <div class="ui top attached tabular menu">

            <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
                    controller="survey" action="openParticipantsAgain"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllFinish']">
                ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                <div class="ui floating circular label">${participantsFinishTotal}</div>
            </g:link>

            <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
                    controller="survey" action="openParticipantsAgain"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
                ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                <div class="ui floating circular label">${participantsNotFinishTotal}</div>
            </g:link>

        </div>

        <semui:form>

            <semui:filter>
                <g:form action="openParticipantsAgain" method="post" class="ui form"
                        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
                    <g:render template="/templates/filter/orgFilter"
                              model="[
                                      tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value']],
                                      tmplConfigFormFilter: true
                              ]"/>
                </g:form>
            </semui:filter>

            <g:form action="processOpenParticipantsAgain" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
                <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

                <g:set var="surveyParticipantsHasAccess"
                       value="${surveyResult.findAll { it.participant.hasAccessOrg() }}"/>

                <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.participant.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>
                </div>

                <br />
                <br />

                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th>
                            <g:if test="${surveyParticipantsHasAccess}">
                                <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                            </g:if>
                        </th>
                        <th class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <g:sortableColumn params="${params}" title="${message(code: 'default.name.label')}"
                                          property="surResult.participant.sortname"/>
                    </th>
                        <g:each in="${surveyParticipantsHasAccess.groupBy {
                            it.type.id
                        }.sort { it.value[0].type.name }}" var="property">
                            <g:set var="surveyProperty" value="${PropertyDefinition.get(property.key)}"/>
                            <semui:sortableColumn params="${params}" title="${surveyProperty.getI10n('name')}"
                                                  property="surResult.${surveyProperty.getImplClassValueProperty()}, surResult.participant.sortname ASC">
                                <g:if test="${surveyProperty.getI10n('expl')}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyProperty.getI10n('expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>
                            </semui:sortableColumn>
                        </g:each>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyParticipantsHasAccess.groupBy { it.participant.id }}" var="result" status="i">

                        <g:set var="participant" value="${Org.get(result.key)}"/>
                        <tr>
                            <td>
                                <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                            </td>
                            <td>
                                ${i + 1}
                            </td>
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys"
                                        id="${participant.id}">
                                    ${participant.sortname}
                                </g:link>
                                <br />
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    (${fieldValue(bean: participant, field: "name")})
                                </g:link>

                                <div class="ui grid">
                                    <div class="right aligned wide column">



                                        <g:if test="${!surveyConfig.pickAndChoose}">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                                <g:link controller="survey" action="evaluationParticipant"
                                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]" class="ui icon button">
                                                    <i class="chart pie icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>

                                        <g:if test="${surveyConfig.pickAndChoose}">
                                            <g:link controller="survey" action="surveyTitlesSubscriber"
                                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                                    class="ui icon button"><i
                                                    class="chart pie icon"></i>
                                            </g:link>
                                        </g:if>

                                        <g:if test="${!surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.newOrg')}">
                                                <i class="star black large  icon"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.processedOrg')}">
                                                <i class="edit green icon"></i>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                                <i class="edit red icon"></i>
                                            </span>
                                        </g:else>

                                        <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.finishOrg')}">
                                                <i class="check green icon"></i>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                                <i class="x red icon"></i>
                                            </span>
                                        </g:else>
                                    </div>
                                </div>
                            </td>
                            <g:set var="resultPropertyParticipation"/>
                            <g:each in="${result.value.sort { it.type.name }}" var="resultProperty">
                                <td>
                                    <g:set var="surveyOrg"
                                           value="${SurveyOrg.findBySurveyConfigAndOrg(resultProperty.surveyConfig, participant)}"/>

                                    <g:if test="${resultProperty.surveyConfig.subSurveyUseForTransfer && surveyOrg.existsMultiYearTerm()}">

                                        <g:message code="surveyOrg.perennialTerm.available"/>

                                        <g:if test="${resultProperty.comment}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="right center"
                                                  data-content="${resultProperty.comment}">
                                                <i class="question circle icon"></i>
                                            </span>
                                        </g:if>
                                    </g:if>
                                    <g:else>

                                        <g:if test="${resultProperty.type.name == "Participation"}">
                                            <g:set var="resultPropertyParticipation" value="${resultProperty}"/>
                                        </g:if>

                                        <g:if test="${resultProperty.type.isIntegerType()}">
                                            <semui:xEditable owner="${resultProperty}" type="text" field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${resultProperty.type.isStringType()}">
                                            <semui:xEditable owner="${resultProperty}" type="text" field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isBigDecimalType()}">
                                            <semui:xEditable owner="${resultProperty}" type="text" field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isDateType()}">
                                            <semui:xEditable owner="${resultProperty}" type="date" field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isURLType()}">
                                            <semui:xEditable owner="${resultProperty}" type="url" field="urlValue"
                                                             overwriteEditable="${overwriteEditable}"
                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${resultProperty.urlValue}">
                                                <semui:linkIcon/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isRefdataValueType()}">
                                            <semui:xEditableRefData owner="${resultProperty}" type="text"
                                                                    field="refValue"
                                                                    config="${resultProperty.type.refdataCategory}"/>
                                        </g:elseif>
                                        <g:if test="${resultProperty.comment}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="right center"
                                                  data-content="${resultProperty.comment}">
                                                <i class="question circle icon"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${resultProperty.type.id == RDStore.SURVEY_PROPERTY_PARTICIPATION.id && resultProperty.getResult() == RDStore.YN_NO.getI10n('value')}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="top right"
                                                  data-variation="tiny"
                                                  data-content="${message(code: 'surveyResult.particiption.terminated')}">
                                                <i class="minus circle big red icon"></i>
                                            </span>
                                        </g:if>

                                    </g:else>

                                </td>
                            </g:each>

                        </tr>

                    </g:each>
                    </tbody>
                </table>

                <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

                <g:set var="surveyParticipantsHasNotAccess"
                       value="${surveyResult.findAll { !it.participant.hasAccessOrg() }}"/>

                <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.participant.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>
                </div>

                <br />
                <br />

                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th>
                            <g:if test="${surveyParticipantsHasNotAccess}">
                                <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                            </g:if>
                        </th>
                        <th class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <g:sortableColumn params="${params}" title="${message(code: 'default.name.label')}"
                                          property="surResult.participant.sortname"/>
                        <g:each in="${surveyParticipantsHasNotAccess.groupBy {
                            it.type.id
                        }.sort { it.value[0].type.name }}" var="property">
                            <g:set var="surveyProperty" value="${PropertyDefinition.get(property.key)}"/>
                            <semui:sortableColumn params="${params}" title="${surveyProperty.getI10n('name')}"
                                                  property="surResult.${surveyProperty.getImplClassValueProperty()}, surResult.participant.sortname ASC">
                                <g:if test="${surveyProperty.getI10n('expl')}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyProperty.getI10n('expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>
                            </semui:sortableColumn>
                        </g:each>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyParticipantsHasNotAccess.groupBy { it.participant.id }}" var="result"
                            status="i">

                        <g:set var="participant" value="${Org.get(result.key)}"/>
                        <tr>
                            <td>
                                <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                            </td>
                            <td>
                                ${i + 1}
                            </td>
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys"
                                        id="${participant.id}">
                                    ${participant.sortname}
                                </g:link>
                                <br />
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    (${fieldValue(bean: participant, field: "name")})
                                </g:link>

                                <div class="ui grid">
                                    <div class="right aligned wide column">


                                        <g:if test="${!surveyConfig.pickAndChoose}">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                                <g:link controller="survey" action="evaluationParticipant"
                                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]" class="ui icon button">
                                                    <i class="chart pie icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>

                                        <g:if test="${surveyConfig.pickAndChoose}">
                                            <g:link controller="survey" action="surveyTitlesSubscriber"
                                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                                    class="ui icon button"><i
                                                    class="chart pie icon"></i>
                                            </g:link>
                                        </g:if>

                                        <g:if test="${!surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.newOrg')}">
                                                <i class="star black large  icon"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.processedOrg')}">
                                                <i class="edit green icon"></i>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                                <i class="edit red icon"></i>
                                            </span>
                                        </g:else>

                                        <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.finishOrg')}">
                                                <i class="check green icon"></i>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                                <i class="x red icon"></i>
                                            </span>
                                        </g:else>
                                    </div>
                                </div>
                            </td>
                            <g:set var="resultPropertyParticipation"/>
                            <g:each in="${result.value.sort { it.type.name }}" var="resultProperty">
                                <td>
                                    <g:set var="surveyOrg"
                                           value="${SurveyOrg.findBySurveyConfigAndOrg(resultProperty.surveyConfig, participant)}"/>

                                    <g:if test="${resultProperty.surveyConfig.subSurveyUseForTransfer && surveyOrg.existsMultiYearTerm()}">

                                        <g:message code="surveyOrg.perennialTerm.available"/>

                                        <g:if test="${resultProperty.comment}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="right center"
                                                  data-content="${resultProperty.comment}">
                                                <i class="question circle icon"></i>
                                            </span>
                                        </g:if>
                                    </g:if>
                                    <g:else>

                                        <g:if test="${resultProperty.type.name == "Participation"}">
                                            <g:set var="resultPropertyParticipation" value="${resultProperty}"/>
                                        </g:if>

                                        <g:if test="${resultProperty.type.isIntegerType()}">
                                            <semui:xEditable owner="${resultProperty}" type="text" field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${resultProperty.type.isStringType()}">
                                            <semui:xEditable owner="${resultProperty}" type="text" field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isBigDecimalType()}">
                                            <semui:xEditable owner="${resultProperty}" type="text" field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isDateType()}">
                                            <semui:xEditable owner="${resultProperty}" type="date" field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isURLType()}">
                                            <semui:xEditable owner="${resultProperty}" type="url" field="urlValue"
                                                             overwriteEditable="${overwriteEditable}"
                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${resultProperty.urlValue}">
                                                <semui:linkIcon/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${resultProperty.type.isRefdataValueType()}">
                                            <semui:xEditableRefData owner="${resultProperty}" type="text"
                                                                    field="refValue"
                                                                    config="${resultProperty.type.refdataCategory}"/>
                                        </g:elseif>
                                        <g:if test="${resultProperty.comment}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="right center"
                                                  data-content="${resultProperty.comment}">
                                                <i class="question circle icon"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${resultProperty.type.id == RDStore.SURVEY_PROPERTY_PARTICIPATION.id && resultProperty.getResult() == RDStore.YN_NO.getI10n('value')}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                  data-position="top right"
                                                  data-variation="tiny"
                                                  data-content="${message(code: 'surveyResult.particiption.terminated')}">
                                                <i class="minus circle big red icon"></i>
                                            </span>
                                        </g:if>

                                    </g:else>

                                </td>
                            </g:each>
                        </tr>

                    </g:each>
                    </tbody>
                </table>

                <br />

                <div class="content">
                    <div class="ui form twelve wide column">
                        <div class="two fields">
                            <g:if test="${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}">
                                <div class="eight wide field" style="text-align: left;">
                                    <button name="openOption" type="submit" value="ReminderMail" class="ui button">
                                        ${message(code: 'openParticipantsAgain.reminder')}
                                    </button>
                                </div>
                            </g:if>
                            <g:else>

                                <div class="eight wide field" style="text-align: left;">
                                    <button name="openOption" type="submit" value="OpenWithoutMail" class="ui button">
                                        ${message(code: 'openParticipantsAgain.openWithoutMail.button')}
                                    </button>
                                </div>

                                <div class="eight wide field" style="text-align: right;">
                                    <button name="openOption" type="submit" value="OpenWithMail" class="ui button">
                                        ${message(code: 'openParticipantsAgain.openWithMail.button')}
                                    </button>
                                </div>
                            </g:else>
                        </div>
                    </div>
                </div>

            </g:form>

        </semui:form>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#orgListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
        }
    })
</laser:script>

</body>
</html>
