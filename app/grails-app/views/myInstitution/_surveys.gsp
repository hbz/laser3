<div class="ui top attached tabular menu">
    <a class="active item" data-tab="firstSurvey"><g:message code="surveys.active"/></a>
    <a class="item" data-tab="secondSurvey"><g:message code="surveys.finish"/></a>

</div>

<div class="ui bottom attached active tab segment" data-tab="firstSurvey">

    <div class="ui divided items">
        <g:each in="${surveys.groupBy { it.surveyConfig.surveyInfo.id }}" var="survey" status="i">

            <g:set var="surveyInfo" value="${com.k_int.kbplus.SurveyInfo.get(survey.key)}"/>

            <div class="item">

                <div class="content">
                    <a class="header"><g:link controller="myInstitution" action="surveyResult"
                                              id="${surveyInfo.id}">${surveyInfo?.name}</g:link></a>

                    <div class="meta">
                        <span><g:message code="surveyInfo.owner.label"/>: ${surveyInfo?.owner}</span>
                    </div>

                    <div class="description">
                        <p>
                            <g:if test="${surveyInfo?.startDate}">
                                <g:message code="surveyInfo.startDate.label"/>: <g:formatDate
                                    date="${surveyInfo?.startDate}" formatName="default.date.format.notime"/>
                            </g:if>

                            <g:if test="${surveyInfo?.endDate}">
                                <g:message code="surveyInfo.endDate.label"/>: <g:formatDate
                                    date="${surveyInfo?.endDate}" formatName="default.date.format.notime"/>
                            </g:if>

                        </p>

                        <p>
                            <g:if test="${surveyInfo?.comment}">
                                <g:message code="surveyInfo.comment.label"/>: ${surveyInfo?.comment}
                            </g:if>
                        </p>
                    </div>

                    <div class="extra">
                        <div class="ui label">${surveyInfo?.status.getI10n('value')}</div>

                        <div class="ui label survey-${surveyInfo?.type.value}">${surveyInfo?.type.getI10n('value')}</div>
                    </div>
                </div>
            </div>

        </g:each>

    </div>
</div>

<div class="ui bottom attached tab segment" data-tab="secondSurvey">

</div>

