<%@ page import="de.laser.ui.Btn; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:htmlStart message="createGeneralSurvey.label" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" message="currentSurveys.label"/>
    <ui:crumb message="createGeneralSurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="createGeneralSurvey.label" type="Survey" />

<ui:messages data="${flash}"/>

<ui:form controller="survey" action="processCreateGeneralSurvey">
        <div class="field required">
            <label>${message(code: 'surveyInfo.name.label')}  <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="two fields">
            <ui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${params.startDate}" required="" />

            <ui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${params.endDate}" />
        </div>

        <div class="field">
            <label>${message(code: 'surveyInfo.isMandatory.label.info')}</label>
            <div class="ui checkbox">
                <input type="checkbox" name="mandatory" ${params.mandatory? 'checked':''}>
            </div>
        </div>

    <div class="field">
        <label>${message(code: 'surveyconfig.invoicingInformation.short')} <i data-content="${message(code:'surveyconfig.invoicingInformation.info')}" data-position="top center" class="question icon circular la-popup-tooltip"></i></label>
        <div class="ui checkbox">
            <input type="checkbox" id="invoicingInformation" name="invoicingInformation" ${params.invoicingInformation? 'checked':''}>
        </div>
    </div>

    <div class="field">
        <label>${message(code: 'surveyconfig.vendorSurvey.short')} <i data-content="${message(code:'surveyconfig.vendorSurvey.info')}" data-position="top center" class="question icon circular la-popup-tooltip"></i></label>
        <div class="ui checkbox">
            <input type="checkbox" id="vendorSurvey" name="vendorSurvey" ${params.vendorSurvey? 'checked':''}>
        </div>
    </div>

    <div class="field">
        <label>${message(code: 'surveyconfig.subscriptionSurvey.short')} <i data-content="${message(code:'surveyconfig.subscriptionSurvey.info')}" data-position="top center" class="question icon circular la-popup-tooltip"></i></label>
        <div class="ui checkbox">
            <input type="checkbox" id="subscriptionSurvey" name="subscriptionSurvey" ${params.subscriptionSurvey? 'checked':''}>
        </div>
    </div>

    <div class="field">
        <label>${message(code: 'surveyconfig.packageSurvey.short')} <i data-content="${message(code:'surveyconfig.packageSurvey.info')}" data-position="top center" class="question icon circular la-popup-tooltip"></i></label>
        <div class="ui checkbox">
            <input type="checkbox" id="packageSurvey" name="packageSurvey" ${params.packageSurvey? 'checked':''}>
        </div>
    </div>

        <div class="field">
            <label>${message(code: 'surveyInfo.comment.label')}</label>

            <textarea class="la-textarea-resize-vertical" name="comment">${params.comment}</textarea>
        </div>

        <br />

        <input type="submit" class="${Btn.SIMPLE}" value="${message(code: 'createGeneralSurvey.create')}"/>
</ui:form>

<laser:htmlEnd />
