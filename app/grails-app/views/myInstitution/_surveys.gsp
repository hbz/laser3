
<h3><g:message code="surveys.active"/></h3>


<div class="ui divided items">
    <g:each in="${surveys}" var="surveyInfo" status="i">

        <div class="item">

            <div class="content">
                <a class="header"><g:link controller="myInstitution" action="surveyInfos"
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
                    <g:set var="surveyResults" value="${com.k_int.kbplus.SurveyResult.findAllByParticipantAndSurveyConfigInList(institution, surveyInfo?.surveyConfigs)}" />

                    <div class="ui label">
                        ${surveyInfo?.status.getI10n('value')}
                    </div>

                    <div class="ui label survey-${surveyInfo?.type.value}">
                        <g:link controller="myInstitution" action="surveyInfos"
                                id="${surveyInfo.id}">${surveyInfo?.type.getI10n('value')}
                        </g:link>
                    </div>

                    <g:if test="${surveyResults && !surveyResults?.finishDate?.contains(null) ? "green" : ""}">
                            <i class="check big green icon"></i>
                    </g:if>
                </div>
            </div>
        </div>

    </g:each>

</div>
