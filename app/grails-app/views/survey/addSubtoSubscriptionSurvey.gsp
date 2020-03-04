<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'createSubscriptionSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
    <semui:crumb message="createSubscriptionSurvey.label" class="active"/>
</semui:breadcrumbs>
<br>
<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon type="Survey"/>${message(code: 'createSubscriptionSurvey.label')}</h1>

<semui:messages data="${flash}"/>

<semui:form>
    <g:form action="processCreateSubscriptionSurvey" controller="survey" method="post" class="ui form">
        <g:hiddenField name="sub" value="${subscription?.id}"/>
        <div class="field required ">
            <label>${message(code: 'surveyInfo.name.label')}</label>
            <input type="text" name="name" placeholder="" value="${subscription?.name}" required />
        </div>

        <div class="two fields ">
            <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${surveyInfo?.startDate}" required="" />

            <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${surveyInfo?.endDate}" required="" />
        </div>

        <g:if test="${!(com.k_int.kbplus.SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransfer(subscription, true))}">
            <div class="field">
                <label>${message(code: 'surveyConfig.subSurveyUseForTransfer.label.info')}</label>
                <div>
                    <input type="checkbox" id="subSurveyUseForTransfer" name="subSurveyUseForTransfer" ${disableSubSurveyUseForTransfer} >
                </div>
            </div>
        </g:if>

        <div class="field">
            <label>${message(code: 'surveyInfo.isMandatory.label.info')}</label>
            <div>
                <input type="checkbox" id="mandatory" name="mandatory" >
            </div>
        </div>

        <div class="field ">
            <label>${message(code: 'surveyInfo.comment.label')}</label>

            <textarea name="comment"></textarea>
        </div>

        <input type="submit" class="ui button"
               value="${message(code: 'createSubscriptionSurvey.create')}"/>

    </g:form>
</semui:form>

<script language="JavaScript">
        $('#subSurveyUseForTransfer').click(function () {
            if ($(this).prop('checked')) {
                $('#mandatory').prop('checked', true)
            }
        })
</script>

</body>
</html>
