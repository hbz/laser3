<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:htmlStart message="createGeneralSurvey.label" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" message="currentSurveys.label"/>
    <ui:crumb message="createGeneralSurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="createGeneralSurvey.label" type="Survey" />

<ui:messages data="${flash}"/>

<ui:form controller="survey" action="processCreateGeneralSurvey">
        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label')}  <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="two fields ">
            <ui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${params.startDate}" required="" />

            <ui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${params.endDate}" />
        </div>

        <div class="field ">
            <label>${message(code: 'surveyInfo.isMandatory.label.info')}</label>
            <div class="ui checkbox">
                <input type="checkbox" name="mandatory" ${params.mandatory? 'checked':''}>
            </div>
        </div>

        <div class="field ">
            <label>${message(code: 'surveyInfo.comment.label')}</label>

            <textarea class="la-textarea-resize-vertical" name="comment">${params.comment}</textarea>
        </div>

        <br />

        <input type="submit" class="ui button" value="${message(code: 'createGeneralSurvey.create')}"/>
</ui:form>

<laser:htmlEnd />
