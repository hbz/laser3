<%@ page import="de.laser.SurveyLinks;de.laser.SurveyInfo;de.laser.helper.RDStore;" %>
<laser:serviceInjection/>
<g:if test="${editable}">

        <a role="button"
           class="ui button la-modern-button"
           data-semui="modal" href="#surveyLinks"
           class="la-popup-tooltip la-delay">
            <g:message code="surveyLinks.link.button"/>
        </a>

</g:if>

<semui:modal id="surveyLinks" text="${message(code: 'surveyLinks.link.button')}" msgSave="${message(code: 'default.button.link.label')}">
    <g:form class="ui form" controller="survey" action="setSurveyLink" params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]" method="post">
        <div class="field">
            <div class="ui grid">
                <div class="row">
                    <div class="six wide column">
                        ${message(code: 'surveyLinks.link.info')}
                    </div>
                    <div class="ten wide column">
                        <g:set var="surveyLinks" value="${SurveyLinks.findAllBySourceSurvey(surveyInfo)}"/>
                        <g:select class="ui dropdown" name="linkSurvey"
                                      from="${SurveyInfo.executeQuery("from SurveyInfo s where s.status in (:status) and not exists (select sl from SurveyLinks sl where sl.sourceSurvey = (:surveyInfo)) and s != :surveyInfo and s.owner = :owner order by s.name", [status: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_SURVEY_STARTED], surveyInfo: surveyInfo, owner: contextOrg])}"
                                      optionKey="id"
                                      optionValue="name"
                                      required=""/>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:modal>
