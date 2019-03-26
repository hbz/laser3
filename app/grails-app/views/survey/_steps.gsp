<div class="ui four top attached steps">
    <g:if test="${surveyInfo}">
        <g:link controller="survey" action="showSurveyInfo" id="${surveyInfo.id}" class="step ${actionName == 'showSurveyInfo' ? 'active': ''}">

            <i class="info icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.first.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.first.description')}</div>
            </div>

        </g:link>
    </g:if>
    <g:else>
        <div class="step ${actionName == 'showSurveyInfo' ? 'active': ''}">
            <i class="info icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.first.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.first.description')}</div>
            </div>
        </div>
    </g:else>

    <g:if test="${surveyInfo}">
        <g:link controller="survey" action="showSurveyConfig" id="${surveyInfo.id}" class="step ${actionName == 'showSurveyConfig' ? 'active': ''}">

            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.second.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.second.description')}</div>
            </div>

        </g:link>
    </g:if>
    <g:else>
        <div class="step ${actionName == 'showSurveyConfig' ? 'active': ''}">
            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.second.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.second.description')}</div>
            </div>
        </div>
    </g:else>

    <div class="step">
        <i class="tasks icon"></i>

        <div class="content">
            <div class="title">${message(code: 'showSurveyInfo.step.third.title')}</div>

            <div class="description">${message(code: 'showSurveyInfo.step.third.description')}</div>
        </div>
    </div>

    <div class="step">
        <i class="chart pie icon"></i>

        <div class="content">
            <div class="title">${message(code: 'showSurveyInfo.step.fourthly.title')}</div>

            <div class="description">${message(code: 'showSurveyInfo.step.fourthly.description')}</div>
        </div>
    </div>
</div>