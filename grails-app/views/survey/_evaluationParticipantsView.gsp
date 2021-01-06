<%@ page import="de.laser.Org; de.laser.helper.RDConstants; de.laser.RefdataValue; de.laser.properties.PropertyDefinition;de.laser.helper.RDStore;de.laser.RefdataCategory;de.laser.SurveyConfig;de.laser.SurveyOrg" %>

<g:if test="${surveyConfig}">

    <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

    <g:if test="${surveyConfig.subscription}">
        <div class="ui horizontal segments">
            <div class="ui segment center aligned">
                <g:link controller="subscription" action="members" id="${subscription.id}">
                    <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

                    <div class="ui circular label">
                        ${countParticipants.subMembers}
                    </div>
                </g:link>
            </div>

            <div class="ui segment center aligned">
                <g:link controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

                    <div class="ui circular label">${countParticipants.surveyMembers}</div>
                </g:link>

                <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                    ( ${countParticipants.subMembersWithMultiYear}
                    ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
                </g:if>
            </div>
        </div>
    </g:if>

    <g:if test="${!surveyConfig.subscription}">
        <div class="ui horizontal segments">

            <div class="ui segment left aligned">
                <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
                <g:link controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <div class="ui circular label">${countParticipants.surveyMembers}</div>
                </g:link>
            </div>
        </div>
    </g:if>

</g:if>

<semui:form>

    <semui:filter>
        <g:form action="surveyEvaluation" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:form action="processTransferParticipants" controller="survey" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">

        <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

        <g:set var="surveyParticipantsHasAccess"
               value="${surveyResult.findAll { it.participant.hasAccessOrg() }}"/>

        <div class="four wide column">
            <g:if test="${surveyParticipantsHasAccess}">
                <a data-semui="modal" class="ui icon button right floated"
                   data-orgIdList="${(surveyParticipantsHasAccess.participant.id)?.join(',')}"
                   href="#copyEmailaddresses_static">
                    <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                </a>
            </g:if>
        </div>

        <br />
        <br />

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <g:if test="${showCheckbox}">
                    <th>
                        <g:if test="${surveyResult}">
                            <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                        </g:if>
                    </th>
                </g:if>
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
                <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                        <i class="question circle icon"></i>
                    </span>
                </th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${surveyParticipantsHasAccess.groupBy { it.participant.id }}" var="result" status="i">

                <g:set var="participant" value="${Org.get(result.key)}"/>
                <tr>
                    <g:if test="${showCheckbox}">
                        <td>
                            <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                        </td>
                    </g:if>
                    <td>
                        ${i + 1}
                    </td>
                    <td>
                        <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant.id}">
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
                                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                                class="ui icon button">
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
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
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
                                    <semui:xEditableRefData owner="${resultProperty}" type="text" field="refValue"
                                                            config="${resultProperty.type.refdataCategory}"/>
                                </g:elseif>
                                <g:if test="${resultProperty.comment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${resultProperty.comment}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>

                                <g:if test="${resultProperty.type.id == RDStore.SURVEY_PROPERTY_PARTICIPATION.id && resultProperty.getResult() == RDStore.YN_NO.getI10n('value')}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right"
                                          data-variation="tiny"
                                          data-content="${message(code: 'surveyResult.particiption.terminated')}">
                                        <i class="minus circle big red icon"></i>
                                    </span>
                                </g:if>

                            </g:else>

                        </td>
                    </g:each>
                    <td>
                        <semui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                    </td>
                </tr>

            </g:each>
            </tbody>
        </table>

        <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

        <g:set var="surveyParticipantsHasNotAccess"
               value="${surveyResult.findAll { !it.participant.hasAccessOrg() }}"/>

        <div class="four wide column">
            <g:if test="${surveyParticipantsHasNotAccess}">
                <a data-semui="modal" class="ui icon button right floated"
                   data-orgIdList="${(surveyParticipantsHasNotAccess.participant.id)?.join(',')}"
                   href="#copyEmailaddresses_static">
                    <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                </a>
            </g:if>
        </div>

        <br />
        <br />

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
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
                <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                        <i class="question circle icon"></i>
                    </span>
                </th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${surveyParticipantsHasNotAccess.groupBy { it.participant.id }}" var="result" status="i">

                <g:set var="participant" value="${Org.get(result.key)}"/>
                <tr>
                    <g:if test="${showCheckbox}">
                        <td>
                            <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                        </td>
                    </g:if>
                    <td>
                        ${i + 1}
                    </td>
                    <td>
                        <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant.id}">
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
                                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                                class="ui icon button">
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
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
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
                                    <semui:xEditableRefData owner="${resultProperty}" type="text" field="refValue"
                                                            config="${resultProperty.type.refdataCategory}"/>
                                </g:elseif>
                                <g:if test="${resultProperty.comment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${resultProperty.comment}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>

                                <g:if test="${resultProperty.type.id == RDStore.SURVEY_PROPERTY_PARTICIPATION.id && resultProperty.getResult() == RDStore.YN_NO.getI10n('value')}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right"
                                          data-variation="tiny"
                                          data-content="${message(code: 'surveyResult.particiption.terminated')}">
                                        <i class="minus circle big red icon"></i>
                                    </span>
                                </g:if>

                            </g:else>

                        </td>
                    </g:each>
                    <td>
                        <semui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                    </td>
                </tr>

            </g:each>
            </tbody>
        </table>

        <g:if test="${showTransferFields}">
            <br />
            <br />
            <semui:form>
            <div class="ui form">
            <h3 class="ui header">${message(code: 'surveyTransfer.info.label')}:</h3>
                <div class="two fields">
                    <div class="ui field">
                         <div class="field">
                            <label>${message(code: 'filter.status')}</label>
                            <laser:select class="ui dropdown" name="status" id="status"
                                          from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                          optionKey="id"
                                          optionValue="value"
                                          multiple="true"
                                          value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          onchange="JSPC.app.adjustDropdown()"/>
                        </div>
                        <br />
                        <br id="element-vor-target-dropdown" />
                        <br />

                    </div>
                    <div class="field">
                        <semui:datepicker label="subscription.startDate.label" id="startDate" name="startDate" value=""/>

                        <semui:datepicker label="subscription.endDate.label" id="endDate" name="endDate" value=""/>
                    </div>
                </div>

                <input class="ui button" type="submit" value="${message(code: 'surveyTransfer.button')}">
            </semui:form>
            </div>

        </g:if>
    </g:form>

</semui:form>

<laser:script file="${this.getGroovyPageFileName()}">
<g:if test="${showCheckbox}">
        $('#orgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        })
</g:if>
<g:if test="${showTransferFields}">
    JSPC.app.adjustDropdown = function () {

        var url = '<g:createLink controller="ajaxJson" action="adjustSubscriptionList"/>'

        var status = $("select#status").serialize()
        if (status) {
            url = url + '?' + status
        }

        $.ajax({
            url: url,
            success: function (data) {
                var select = '';
                for (var index = 0; index < data.length; index++) {
                    var option = data[index];
                    var optionText = option.text;
                    var optionValue = option.value;
                    var count = index + 1
                    // console.log(optionValue +'-'+optionText)

                    select += '<div class="item" data-value="' + optionValue + '">'+ count + ': ' + optionText + '</div>';
                }

                select = ' <div class="ui fluid search selection dropdown la-filterProp">' +
    '   <input type="hidden" id="subscription" name="targetSubscriptionId">' +
    '   <i class="dropdown icon"></i>' +
    '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
    '   <div class="menu">'
    + select +
    '</div>' +
    '</div>';

                $('#element-vor-target-dropdown').next().replaceWith(select);

                $('.la-filterProp').dropdown({
                    duration: 150,
                    transition: 'fade',
                    clearable: true,
                    forceSelection: false,
                    selectOnKeydown: false,
                    onChange: function (value, text, $selectedItem) {
                        value.length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
                    }
                });
            }, async: false
        });
    }

    JSPC.app.adjustDropdown()
</g:if>
</laser:script>

