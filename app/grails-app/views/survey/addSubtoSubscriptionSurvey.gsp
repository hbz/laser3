<%@ page import="de.laser.SurveyConfig; com.k_int.kbplus.RefdataValue; de.laser.RefdataCategory" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'createSubscriptionSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>

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
            <input type="text" name="name" placeholder="" value="${subscription?.name ?: params.name}" required />
        </div>

        <div class="two fields ">
            <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                              value="${params.startDate}" required="" />

            <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                              value="${params.endDate}" />
        </div>

        <g:if test="${!(SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransfer(subscription, true))}">
            <div class="field">
                <label>${message(code: 'surveyConfig.subSurveyUseForTransfer.label.info')}</label>
                <div>
                    <input type="checkbox" id="subSurveyUseForTransfer" name="subSurveyUseForTransfer"  ${params.subSurveyUseForTransfer? 'checked':''}>
                </div>
            </div>
        </g:if><g:else>

        <div class="field">
            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'surveyConfig.subSurveyUseForTransfer.label.info3')}">
            <label>${message(code: 'surveyConfig.subSurveyUseForTransfer.label.info')} <i class="question circle icon"></i></label>
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

            <textarea name="comment">${params.comment}</textarea>
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
