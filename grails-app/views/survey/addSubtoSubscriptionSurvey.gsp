<%@ page import="de.laser.survey.SurveyConfig; de.laser.RefdataValue; de.laser.RefdataCategory" %>

<laser:htmlStart message="createSubscriptionSurvey.label" serviceInjection="true" />

<ui:breadcrumbs>

    <ui:crumb controller="survey" action="workflowsSurveysConsortia" message="currentSurveys.label"/>
    <ui:crumb message="createSubscriptionSurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="createSubscriptionSurvey.label" type="Survey" />

<ui:messages data="${flash}"/>

<ui:form>
    <g:form action="processCreateSubscriptionSurvey" controller="survey" method="post" class="ui form">
        <g:hiddenField id="sub_id_${subscription?.id}" name="sub" value="${subscription?.id}"/>
        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label')} ${message(code: 'messageRequiredField')}</label>
            <input type="text" name="name" placeholder="" value="${subscription?.name ?: params.name}" required />
        </div>

        <div class="two fields ">
            <ui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${params.startDate}" required="" />

            <ui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${params.endDate}" />
        </div>

        <g:if test="${!(SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransfer(subscription, true))}">
            <div class="field">
                <label>${message(code: 'surveyconfig.subSurveyUseForTransfer.label.info')}</label>
                <div>
                    <input type="checkbox" id="subSurveyUseForTransfer" name="subSurveyUseForTransfer"  ${params.subSurveyUseForTransfer? 'checked':''}>
                </div>
            </div>
        </g:if><g:else>

        <div class="field">
            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'surveyconfig.subSurveyUseForTransfer.label.info3')}">
            <label>${message(code: 'surveyconfig.subSurveyUseForTransfer.label.info')} <i class="question circle icon"></i></label>
            </span>
            <div class="field disabled">
                <input type="checkbox" name="subSurveyUseForTransfer">
            </div>
        </div>
    </g:else>

        <div class="field">
            <label>${message(code: 'surveyInfo.isMandatory.label.info')}</label>
            <div>
                <input type="checkbox" id="mandatory" name="mandatory" ${params.mandatory? 'checked':''}>
            </div>
        </div>

        <div class="field ">
            <label>${message(code: 'surveyInfo.comment.label')}</label>

            <textarea class="la-textarea-resize-vertical" name="comment">${params.comment}</textarea>
        </div>

        <input type="submit" class="ui button"
               value="${message(code: 'createSubscriptionSurvey.create')}"/>

    </g:form>
</ui:form>

<laser:script file="${this.getGroovyPageFileName()}">
        $('#subSurveyUseForTransfer').click(function () {
            if ($(this).prop('checked')) {
                $('#mandatory').prop('checked', true)
            }
        })
</laser:script>

<laser:htmlEnd />
