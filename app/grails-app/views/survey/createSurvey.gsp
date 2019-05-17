<%@ page import="com.k_int.kbplus.RefdataCategory;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
    <semui:crumb message="survey" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${message(code: 'createSurvey.label')}</h1>

<semui:messages data="${flash}"/>

<br>

<semui:form>
    <g:form action="processCreateSurvey" controller="survey" method="post" class="ui form">

        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</label>
            <input type="text" name="name" placeholder="" value="${surveyInfo?.name}" required/>
        </div>

        <div class="two fields ">
            <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${surveyInfo?.startDate}"/>

            <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${surveyInfo?.endDate}"/>
        </div>


        <div class="field required ">
            <label>${message(code: 'surveyInfo.type.label')}</label>
            <g:if test="${surveyInfo?.type}">
                <b>${surveyInfo?.type?.getI10n('value')}</b>
            </g:if><g:else>
            <laser:select class="ui dropdown" name="type"
                          from="${RefdataCategory.getAllRefdataValues('Survey Type')}"
                          optionKey="id"
                          optionValue="value"
                          value="${surveyInfo?.type?.id}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}" required=""/>
        </g:else>
        </div>

        <div class="field ">
            <label>${message(code: 'surveyInfo.comment.label', default: 'New Survey Name')}</label>

            <textarea name="comment" rows="4" cols="50">
                ${surveyInfo?.comment}
            </textarea>

        </div>

        <br/>


        <input type="submit" class="ui button"
               value="${message(code: 'createSurvey.create', default: 'Create')}"/>

    </g:form>
</semui:form>

</body>
</html>
