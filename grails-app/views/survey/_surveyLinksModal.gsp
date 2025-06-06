<%@ page import="de.laser.ui.Icon; de.laser.survey.SurveyLinks;de.laser.survey.SurveyInfo;de.laser.storage.RDStore;" %>
<laser:serviceInjection/>

<ui:modal id="surveyLinks" text="${message(code: 'surveyLinks.link.button')}" msgSave="${message(code: 'default.button.link.label')}">
    <g:form class="ui form" controller="survey" action="setSurveyLink" params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]" method="post">
        <div class="field">
            <div class="ui grid">
                <div class="row">
                    <div class="six wide column">
                        ${message(code: 'surveyLinks.link.info')} <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'surveyLinks.link.info.tooltip')}">
                        <i class="${Icon.TOOLTIP.INFO} blue"></i>
                    </span>
                    </div>
                    <div class="ten wide column">
                        <g:select class="ui dropdown clearable" name="linkSurvey"
                                      from="${SurveyInfo.executeQuery("from SurveyInfo s where s.status in (:status) and s != :surveyInfo and s.owner = :owner and s.id not in (select sl.targetSurvey.id from SurveyLinks sl where sl.sourceSurvey.id = :surveyInfo) and s.id not in (select sl.sourceSurvey.id from SurveyLinks sl where sl.targetSurvey.id = :surveyInfo) order by s.name", [status: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_SURVEY_STARTED], surveyInfo: surveyInfo, owner: contextService.getOrg()])}"
                                      optionKey="id"
                                      optionValue="${{ it.name + ' - ' + it.type.getI10n('value') + ' - ' + ' (' + (it.startDate ? g.formatDate(date: it.startDate,format: message(code: 'default.date.format.notime')) : '') + (it.endDate ? " - " + g.formatDate(date: it.endDate,format: message(code: 'default.date.format.notime')) : '') + ')' }}"
                                      required=""/>
                    </div>
                </div>
                <div class="row">
                    <div class="six wide column">
                        ${message(code: 'surveyLinks.bothDirection')}:
                    </div>
                    <div class="ten wide column">
                        <div class="ui toggle checkbox">
                            <input type="checkbox" name="bothDirection">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</ui:modal>
