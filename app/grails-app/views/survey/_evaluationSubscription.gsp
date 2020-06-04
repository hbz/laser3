<%@ page import="de.laser.helper.RDStore; com.k_int.properties.PropertyDefinition;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<g:if test="${surveyConfig}">

    <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
    <div class="ui horizontal segments">
        <div class="ui segment center aligned">
            <b>${message(code: 'surveyConfig.subOrgs.label')}:</b>
            <g:link controller="subscription" action="members" id="${subscriptionInstance.id}">
                <div class="ui circular label">
                    ${countParticipants.subMembers}
                </div>
            </g:link>
        </div>

        <div class="ui segment center aligned">
            <b>${message(code: 'surveyConfig.orgs.label')}:</b>
            <g:link controller="survey" action="surveyParticipants"
                    id="${surveyConfig.surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id]">
                <div class="ui circular label">${countParticipants.surveyMembers}</div>
            </g:link>

            <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                ( ${countParticipants.subMembersWithMultiYear}
                ${message(code: 'surveyConfig.subOrgsWithMultiYear.label')} )
            </g:if>
        </div>
    </div>
</g:if>

<semui:form>

    <h4><g:message code="surveyParticipants.hasAccess"/></h4>


    <g:set var="surveyParticipantsHasAccess"
           value="${surveyResult.findAll { it.participant.hasAccessOrg() }.sort {
               it.participant.sortname
           }}"/>
    <div class="four wide column">
    <g:if test="${surveyParticipantsHasAccess}">
        <g:link onclick="copyEmailAdresses(${surveyParticipantsHasAccess.participant.id})"
                data-targetId="copyEmailaddresses_ajaxModal22"
                class="ui icon button right floated trigger-modal">
            <g:message
                    code="survey.copyEmailaddresses.participantsHasAccess"/>
        </g:link>
    </g:if>
    </div>

    <br>
    <br>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>

            <g:each in="${surveyParticipantsHasAccess.groupBy {
                it.type.id
            }.sort { it.value[0].type.name }}" var="property">
                <th>
                    <g:set var="surveyProperty" value="${PropertyDefinition.get(property.key)}"/>
                    ${surveyProperty.getI10n('name')}

                    <g:if test="${surveyProperty.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyProperty.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
            </g:each>
            <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                      data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                    <i class="question circle icon"></i>
                </span>
            </th>
        </tr>
        </thead>
        <g:each in="${surveyParticipantsHasAccess.groupBy { it.participant.id }}" var="result" status="i">

            <g:set var="participant" value="${Org.get(result.key)}"/>

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant.id}">
                        ${participant.sortname}
                    </g:link>
                    <br>
                    <g:link controller="organisation" action="show"
                            id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>

                    <div class="ui grid">
                        <div class="right aligned wide column">

                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]" class="ui icon button"><i
                                    class="chart pie icon"></i></g:link>

                            <g:if test="${!surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>
                            <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == com.k_int.kbplus.SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
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

                                <g:if test="${resultProperty.type.type == Integer.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="text" field="intValue"/>
                                </g:if>
                                <g:elseif test="${resultProperty.type.type == String.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="text" field="stringValue"/>
                                </g:elseif>
                                <g:elseif test="${resultProperty.type.type == BigDecimal.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="text" field="decValue"/>
                                </g:elseif>
                                <g:elseif test="${resultProperty.type.type == Date.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="date" field="dateValue"/>
                                </g:elseif>
                                <g:elseif test="${resultProperty.type.type == URL.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="url" field="urlValue"
                                                     overwriteEditable="${overwriteEditable}"
                                                     class="la-overflow la-ellipsis"/>
                                    <g:if test="${resultProperty.urlValue}">
                                        <semui:linkIcon/>
                                    </g:if>
                                </g:elseif>
                                <g:elseif test="${resultProperty.type.type == RefdataValue.toString()}">
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
                    <g:if test="${resultPropertyParticipation && (resultPropertyParticipation.ownerComment != "" && resultPropertyParticipation.ownerComment != null)}">
                        <semui:xEditable owner="${resultPropertyParticipation}" type="text" field="ownerComment"/>
                    </g:if>
                    <g:else>
                        <semui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>


    <h4><g:message code="surveyParticipants.hasNotAccess"/></h4>

    <g:set var="surveyParticipantsHasNotAccess"
           value="${surveyResult.findAll { !it.participant.hasAccessOrg() }.sort {
               it.participant.sortname
           }}"/>

    <div class="four wide column">
    <g:if test="${surveyParticipantsHasNotAccess}">
        <g:link onclick="copyEmailAdresses(${surveyParticipantsHasNotAccess.participant.id})"
                data-targetId="copyEmailaddresses_ajaxModal33"
                class="ui icon button right floated trigger-modal">
            <g:message
                    code="survey.copyEmailaddresses.participantsHasNoAccess"/>
        </g:link>
    </g:if>
    </div>

    <br>
    <br>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>
            <g:each in="${surveyParticipantsHasNotAccess.groupBy {
                it.type.id
            }.sort { it.value[0].type.name }}" var="property">
                <th>
                    <g:set var="surveyProperty" value="${PropertyDefinition.get(property.key)}"/>
                    ${surveyProperty.getI10n('name')}

                    <g:if test="${surveyProperty.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyProperty.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
            </g:each>
            <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                      data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                    <i class="question circle icon"></i>
                </span></th>
        </tr>
        </thead>
        <g:each in="${surveyParticipantsHasNotAccess.groupBy { it.participant.id }}" var="result" status="i">

            <g:set var="participant" value="${Org.get(result.key)}"/>

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant.id}">
                        ${participant.sortname}
                    </g:link>
                    <br>
                    <g:link controller="organisation" action="show"
                            id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>

                    <div class="ui grid">
                        <div class="right aligned wide column">

                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]" class="ui icon button"><i
                                    class="chart pie icon"></i></g:link>

                            <g:if test="${!surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>
                            <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == com.k_int.kbplus.SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
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

                            <g:if test="${resultProperty.type.type == Integer.toString()}">
                                <semui:xEditable owner="${resultProperty}" type="text" field="intValue"/>
                            </g:if>
                            <g:elseif test="${resultProperty.type.type == String.toString()}">
                                <semui:xEditable owner="${resultProperty}" type="text" field="stringValue"/>
                            </g:elseif>
                            <g:elseif test="${resultProperty.type.type == BigDecimal.toString()}">
                                <semui:xEditable owner="${resultProperty}" type="text" field="decValue"/>
                            </g:elseif>
                            <g:elseif test="${resultProperty.type.type == Date.toString()}">
                                <semui:xEditable owner="${resultProperty}" type="date" field="dateValue"/>
                            </g:elseif>
                            <g:elseif test="${resultProperty.type.type == URL.toString()}">
                                <semui:xEditable owner="${resultProperty}" type="url" field="urlValue"
                                                 overwriteEditable="${overwriteEditable}"
                                                 class="la-overflow la-ellipsis"/>
                                <g:if test="${resultProperty.urlValue}">
                                    <semui:linkIcon/>
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${resultProperty.type.type == RefdataValue.toString()}">
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
                    <g:if test="${resultPropertyParticipation && (resultPropertyParticipation.ownerComment != "" && resultPropertyParticipation.ownerComment != null)}">
                        <semui:xEditable owner="${resultPropertyParticipation}" type="text" field="ownerComment"/>
                    </g:if>
                    <g:else>
                        <semui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</semui:form>


<g:javascript>

var isClicked = false;

function copyEmailAdresses(orgListIDs) {
            event.preventDefault();
            $.ajax({
                url: "<g:createLink controller='survey' action='copyEmailaddresses'/>",
                                data: {
                                    orgListIDs: orgListIDs.join(','),
                                }
            }).done( function(data) {
                $('.ui.dimmer.modals > #copyEmailaddresses_ajaxModal').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                    onVisible: function () {
                        r2d2.initDynamicSemuiStuff('#copyEmailaddresses_ajaxModal');
                        r2d2.initDynamicXEditableStuff('#copyEmailaddresses_ajaxModal');
                    }
                    ,
                    detachable: true,
                    autofocus: false,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        };

</g:javascript>
