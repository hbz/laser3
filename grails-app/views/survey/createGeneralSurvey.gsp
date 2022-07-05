<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'createGeneralSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>

    <semui:crumb controller="survey" action="workflowsSurveysConsortia" message="currentSurveys.label"/>
    <semui:crumb message="createGeneralSurvey.label" class="active"/>
</semui:breadcrumbs>

<semui:h1HeaderWithIcon message="createGeneralSurvey.label" type="Survey" />

<semui:messages data="${flash}"/>

<semui:form>
    <g:form action="processCreateGeneralSurvey" controller="survey" method="post" class="ui form">
        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label')}  <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="two fields ">
            <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${params.startDate}" required="" />

            <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
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

            <textarea name="comment">${params.comment}</textarea>
        </div>

        <br />


        <input type="submit" class="ui button"
               value="${message(code: 'createGeneralSurvey.create')}"/>

    </g:form>
</semui:form>

</body>
</html>
