<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'createIssueEntitlementsSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
    <semui:crumb message="createIssueEntitlementsSurvey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerTitleIcon type="Survey"/>${message(code: 'createIssueEntitlementsSurvey.label')}</h1>

<semui:messages data="${flash}"/>

<semui:form>
    <g:form action="processCreateIssueEntitlementsSurvey" controller="survey" method="post" class="ui form">
        <g:hiddenField name="sub" value="${subscription?.id}"/>

        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</label>
            <input type="text" name="name" placeholder="" value="${subscription?.name}" required />
        </div>

        <div class="two fields ">
            <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${surveyInfo?.startDate}" required="" />

            <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${surveyInfo?.endDate}" required="" />
        </div>


        <div class="field required ">
            <label>${message(code: 'surveyInfo.type.label')}</label>
            ${com.k_int.kbplus.RefdataValue.getByValueAndCategory('selection','Survey Type').getI10n('value')}
        </div>


        <div class="field ">
            <label>${message(code: 'surveyInfo.comment.label', default: 'New Survey Name')}</label>

            <textarea name="comment"></textarea>
        </div>

        <br/>


        <input type="submit" class="ui button"
               value="${message(code: 'createIssueEntitlementsSurvey.create', default: 'Create')}"/>

    </g:form>
</semui:form>

</body>
</html>
