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
    <semui:crumb controller="survey" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="survey" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>

<g:render template="steps"/>
<br>

<semui:messages data="${flash}"/>

<br>
<semui:form>
    <div class="ui grid">
        <div class="middle aligned row">

            <div class="fourteen wide column">
                <g:form action="locSurveyInfo" controller="survey" method="post" class="ui form">

                    <g:hiddenField name="id" value="${surveyInfo?.id}"/>

                    <div class="field required ${editable ? '': 'disabled'}">
                        <label>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</label>
                        <input type="text" name="name" placeholder="" value="${surveyInfo?.name}" required/>
                    </div>

                    <div class="two fields ${editable ? '': 'disabled'}">
                        <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                                          value="${surveyInfo?.startDate}"/>

                        <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                                          value="${surveyInfo?.endDate}"/>
                    </div>


                    <div class="field required ${editable ? '': 'disabled'}">
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
                    <br/>

                    <g:if test="${editable}">
                        <input type="submit" class="ui button"
                               value="${message(code: 'showSurveyInfo.save', default: 'Save')}"/>

                    </g:if>
                </g:form>
            </div>

            <div class="two wide column">
                <g:if test="${surveyInfo}">

                    <g:link controller="survey" action="showSurveyConfig" id="${surveyInfo.id}"
                            class="ui huge button"><i class="angle right icon"></i></g:link>

                </g:if>
            </div>
        </div>

    </div>
</semui:form>

</body>
</html>
