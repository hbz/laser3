<semui:actionsDropdown>
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <g:if test="${actionName == 'currentSurveysConsortia'}">
            <semui:actionsDropdownItem controller="survey" action="createSurvey"
                                       message="createSurvey.label"/>
        </g:if>
        <g:else>

            <g:if test="${surveyInfo && surveyInfo.status?.id == com.k_int.kbplus.RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])?.id}">

                <semui:actionsDropdownItem controller="survey" action="allSubscriptions" params="[id: params.id]"
                                           message="survey.SurveySub.add.label"/>

                <semui:actionsDropdownItem controller="survey" action="allSurveyProperties"
                                           params="[id: params.id, addSurveyConfigs: true]"
                                           message="survey.SurveyProp.add.label"/>

                <div class="ui divider"></div>
                <g:if test="${surveyInfo && surveyInfo.checkOpenSurvey() && surveyInfo.status?.id == com.k_int.kbplus.RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])?.id}">
                    <semui:actionsDropdownItem controller="survey" action="processOpenSurvey" params="[id: params.id]"
                                               message="openSurvey.button"/>
                </g:if>
                <g:else>
                    <semui:actionsDropdownItemDisabled controller="survey" action="processOpenSurvey"
                                                       params="[id: params.id]"
                                                       message="openSurvey.button"/>
                </g:else>
                <div class="ui divider"></div>
            </g:if>


            <semui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id]"
                                       message="survey.SurveyProp.all"/>



            <div class="ui divider"></div>

            <g:set var="orgs" value="${com.k_int.kbplus.Org.findAllByIdInList(surveyInfo?.surveyConfigs?.orgs?.org?.flatten().unique{ a, b -> a?.id <=> b?.id }.id)?.sort{it.sortname}}"/>
            <g:set var="orgIds" value="${orgs?.collect{it.id}.join(',')} "/>

            <g:link class="item trigger-modal" data-targetId="copyEmailaddresses_ajaxModal1" data-orgIdList="${orgIds}">
                <g:message code="survey.copyEmailaddresses.participants"/>
            </g:link>

        </g:else>

    </g:if>
</semui:actionsDropdown>

<g:javascript>

$('.trigger-modal').on('click', function(e) {
    e.preventDefault();
    var orgIdList = $(this).attr('data-orgIdList');
    var targetId = $(this).attr('data-targetId');

    if (orgIdList && targetId) {
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
                    r2d2.initDynamicSemuiStuff('#' + targetId);
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
        })
    }
})

</g:javascript>
