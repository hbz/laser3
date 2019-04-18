<div class="ui five tablet stackable steps">
    <g:if test="${surveyInfo}">
        <g:link controller="survey" action="showSurveyInfo" id="${surveyInfo.id}"
                class="step ${actionName == 'showSurveyInfo' ? 'active' : ''}">

            <i class="info icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.first.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.first.description')}</div>
            </div>

        </g:link>
    </g:if>
    <g:else>
        <div class="step ${actionName == 'showSurveyInfo' ? 'active' : ''}">
            <i class="info icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.first.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.first.description')}</div>
            </div>
        </div>
    </g:else>

    <g:if test="${surveyInfo}">
        <g:link controller="survey" action="showSurveyConfig" id="${surveyInfo.id}"
                class="step ${actionName == 'showSurveyConfig' ? 'active' : ''}">

            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.second.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.second.description')}</div>
            </div>

        </g:link>
    </g:if>
    <g:else>
        <div class="step ${actionName == 'showSurveyConfig' ? 'active' : ''}">
            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.second.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.second.description')}</div>
            </div>
        </div>
    </g:else>


    <g:if test="${surveyInfo && surveyConfigs.size() > 0}">
        <g:link controller="survey" action="showSurveyConfigDocs" id="${surveyInfo.id}"
                class="step ${actionName == 'showSurveyConfigDocs' ? 'active' : ''}">

            <i class="file alternate outline icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.third.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.third.description')}</div>
            </div>
        </g:link>

    </g:if>
    <g:else>

        <div class="step ${actionName == 'showSurveyConfigDocs' ? 'active' : ''}">
            <i class="file alternate outline icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.third.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.third.description')}</div>
            </div>
        </div>
    </g:else>


    <g:if test="${surveyInfo && surveyConfigs.size() > 0}">
        <g:link controller="survey" action="showSurveyParticipants" id="${surveyInfo.id}"
                class="step ${actionName == 'showSurveyParticipants' ? 'active' : ''}">

            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.fourthly.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.fourthly.description')}</div>
            </div>
        </g:link>

    </g:if>
    <g:else>

        <div class="step ${actionName == 'showSurveyParticipants' ? 'active' : ''}">
            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.fourthly.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.fourthly.description')}</div>
            </div>
        </div>
    </g:else>

    <g:if test="${surveyInfo.status != com.k_int.kbplus.RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])}">
        <g:link controller="survey" action="openSurvey" id="${surveyInfo.id}"
                class="step ${actionName == 'openSurvey' ? 'active' : ''}">

            <i class="tasks icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.fifth.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.fifth.description')}</div>
            </div>
        </g:link>

    </g:if>
    <g:else>

        <div class="step ${actionName == 'openSurvey' ? 'active' : ''}">
            <i class="chart pie icon"></i>

            <div class="content">
                <div class="title">${message(code: 'showSurveyInfo.step.fifth.title')}</div>

                <div class="description">${message(code: 'showSurveyInfo.step.fifth.description')}</div>
            </div>
        </div>
    </g:else>
</div>
