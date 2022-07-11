<%@ page import="de.laser.storage.RDStore; de.laser.Org" %>
<laser:serviceInjection/>

<ui:actionsDropdown>
    <g:if test="${contextService.getUser().hasAffiliation("INST_EDITOR")}">
        <g:if test="${actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia'}">

            <laser:render template="actionsCreate"/>

        </g:if>
        <g:else>

            <g:if test="${surveyInfo.type.id != RDStore.SURVEY_TYPE_RENEWAL}">
                <ui:actionsDropdownItem controller="survey" action="copySurvey" params="[id: params.id]"
                                           message="copySurvey.label"/>

                <ui:actionsDropdownItem controller="survey" action="copyElementsIntoSurvey" params="[sourceObjectId: genericOIDService.getOID(surveyConfig)]"
                                           message="survey.copyElementsIntoSurvey"/>
                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_READY.id) && surveyInfo.checkOpenSurvey()}">
                <ui:actionsDropdownItem controller="survey" action="processBackInProcessingSurvey" params="[id: params.id]"
                                           message="backInProcessingSurvey.button"
                                           />
                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id) && surveyInfo.checkOpenSurvey()}">
                <ui:actionsDropdownItem controller="survey" action="processOpenSurvey" params="[id: params.id]"
                                           message="openSurvey.button"
                                           tooltip="${message(code: "openSurvey.button.info2")}"/>
                <ui:actionsDropdownItem controller="survey" action="processOpenSurveyNow"
                                           params="[id: params.id]"
                                           message="openSurveyNow.button"
                                           tooltip="${message(code: "openSurveyNow.button.info2")}"/>
                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id) && !surveyInfo.checkOpenSurvey()}">
                <ui:actionsDropdownItemDisabled controller="survey" action="processOpenSurvey"
                                                   params="[id: params.id]"
                                                   message="openSurvey.button"
                                                   tooltip="${message(code: "openSurvey.button.info")}"/>

                <ui:actionsDropdownItemDisabled controller="survey" action="processOpenSurveyNow"
                                                   params="[id: params.id]"
                                                   message="openSurveyNow.button"
                                                   tooltip="${message(code: "openSurveyNow.button.info")}"/>
                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_SURVEY_STARTED.id}">
                <ui:actionsDropdownItem controller="survey" action="processEndSurvey" params="[id: params.id]"
                                           message="endSurvey.button"
                                           tooltip="${message(code: "endSurvey.button.info")}"/>

                <ui:actionsDropdownItem controller="survey" action="openParticipantsAgain" params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                           message="openParticipantsAgain.button"/>

                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyInfo && surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_SURVEY_COMPLETED.id,RDStore.SURVEY_COMPLETED.id]}">

                <ui:actionsDropdownItem data-ui="modal"
                                           href="#openSurveyAgain"
                                           message="openSurveyAgain.button"/>
                <div class="ui divider"></div>

            </g:if>

            %{-- Only for Survey with Renewal  Beginn --}%
            <g:if test="${surveyInfo && surveyConfig && surveyConfig.subSurveyUseForTransfer
                    && surveyInfo.status.id in [RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_COMPLETED.id]}">

                <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_SURVEY_COMPLETED.id}">
                    <ui:actionsDropdownItem controller="survey" action="setInEvaluation" params="[id: params.id]"
                                               message="evaluateSurvey.button" tooltip=""/>
                    <div class="ui divider"></div>
                </g:if>

                <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_IN_EVALUATION.id}">
                    <ui:actionsDropdownItem controller="survey" action="setCompleted" params="[id: params.id]"
                                               message="completeSurvey.button" tooltip=""/>
                    <div class="ui divider"></div>

                </g:if>
            </g:if>
            %{-- Only for Survey with Renewal End --}%

            <g:if test="${(!surveyConfig.subSurveyUseForTransfer) && surveyInfo && surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_SURVEY_COMPLETED.id]}">
                <ui:actionsDropdownItem controller="survey" action="setCompleted" params="[id: params.id]"
                                           message="completeSurvey.button" tooltip=""/>
                <div class="ui divider"></div>

            </g:if>

            <g:if test="${surveyInfo.type in [RDStore.SURVEY_TYPE_SUBSCRIPTION] && surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_SURVEY_COMPLETED.id]}">
                <ui:actionsDropdownItem controller="survey" action="copySurveyCostItemsToSub" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]"
                                           message="surveyInfo.copySurveyCostItems" tooltip=""/>
                <div class="ui divider"></div>

            </g:if>

            <g:if test="${(surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id)}">
                <ui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id]"
                                           message="survey.SurveyProp.all"/>

                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyConfig.orgs}">
                <ui:actionsDropdownItem data-ui="modal"
                                           href="#copyEmailaddresses_static"
                                           message="survey.copyEmailaddresses.participants"/>

                <g:set var="orgs"
                       value="${Org.findAllByIdInList(surveyConfig.orgs?.org?.flatten().unique { a, b -> a?.id <=> b?.id }.id)?.sort { it.sortname }}"/>

                <laser:render template="/templates/copyEmailaddresses"
                          model="[modalID: 'copyEmailaddresses_static', orgList: orgs ?: null]"/>
            </g:if>
            <g:else>
                <ui:actionsDropdownItemDisabled message="survey.copyEmailaddresses.participants"
                                                   tooltip="${message(code: "survey.copyEmailaddresses.NoParticipants.info")}"/>
            </g:else>

            <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id] && editable}">
                <div class="ui divider"></div>

                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.survey", args: [surveyConfig.getSurveyName()])}"
                        data-confirm-term-how="delete"
                        controller="survey" action="deleteSurveyInfo"
                        id="${surveyInfo.id}"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="trash alternate outline icon"></i> ${message(code:'deletion.survey')}
                </g:link>

            </g:if>
        </g:else>
    </g:if>
</ui:actionsDropdown>

<g:if test="${surveyInfo && surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_COMPLETED.id]}">
    <ui:modal id="openSurveyAgain" text="${message(code:'openSurveyAgain.button')}" msgSave="${message(code:'openSurveyAgain.button')}">

        <g:form class="ui form"
                url="[controller: 'survey', action: 'openSurveyAgain', params: [id: params.id, surveyConfigID: surveyConfig.id], method: 'post']">
            <div class="field">
                <ui:datepicker label="surveyInfo.endDate.new" id="newEndDate" name="newEndDate" placeholder="surveyInfo.endDate.new" />
            </div>
        </g:form>

    </ui:modal>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">

$('.trigger-modal').on('click', function(e) {
    e.preventDefault();
    var orgIdList = $(this).attr('data-orgIdList');
    var targetId = $(this).attr('data-targetId');

    if (orgIdList && targetId) {
        $("html").css("cursor", "wait");

        $.ajax({
            url: "<g:createLink controller='survey' action='copyEmailaddresses'/>",
            data: {
                orgListIDs: orgIdList,
                targetId: targetId
            }
        }).done( function(data) {
            console.log(targetId)
            $('.ui.dimmer.modals > #' + targetId).remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({

                onVisible: function () {
                    console.log(targetId)
                    r2d2.initDynamicUiStuff('#' + targetId);
                    r2d2.initDynamicXEditableStuff('#' + targetId);
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
        }).always( function() {
            $("html").css("cursor", "auto");
        });
    }
})

</laser:script>
