<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory;" %>

<laser:htmlStart message="createIssueEntitlementsSurvey.label" serviceInjection="true" />

<ui:breadcrumbs>

    <ui:crumb controller="survey" action="workflowsSurveysConsortia" message="currentSurveys.label"/>
    <ui:crumb message="createIssueEntitlementsSurvey.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="createIssueEntitlementsSurvey.label" type="Survey" />

<ui:messages data="${flash}"/>

<ui:form>
    <g:form action="processCreateIssueEntitlementsSurvey" controller="survey" method="post" class="ui form">
        <g:hiddenField id="sub_id_${subscription.id}" name="sub" value="${subscription.id}"/>

        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${subscription.name ?: params.name}" required />
        </div>

        <div class="two fields ">
            <ui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${params.startDate}" required="" />

            <ui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${params.endDate}" />
        </div>

        <div class="field">
            <label>${message(code: 'surveyInfo.isMandatory.label.info')}</label>
            <div>
                <input type="checkbox" id="mandatory" name="mandatory" ${params.mandatory? 'checked':''}>
            </div>
        </div>

        <div class="field">
            <label>${message(code: 'surveyconfig.pickAndChoosePerpetualAccess.label')}</label>
            <div>
                <input type="checkbox" id="pickAndChoosePerpetualAccess" name="pickAndChoosePerpetualAccess" ${(params.pickAndChoosePerpetualAccess || subscription.hasPerpetualAccess ) ? 'checked':''}>
            </div>
        </div>

        <div class="field ">
            <label>${message(code: 'surveyInfo.comment.label')}</label>

            <textarea name="comment">${params.comment}</textarea>
        </div>

        <input type="submit" class="ui button"
               value="${message(code: 'createIssueEntitlementsSurvey.create')}"/>

    </g:form>
</ui:form>

<laser:htmlEnd />
