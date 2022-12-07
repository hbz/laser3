<%@ page import="de.laser.survey.SurveyInfo;de.laser.storage.RDStore;" %>
<laser:serviceInjection/>
<g:set var="index" value="${surveyConfig.surveyUrls.size()+1}"/>
<ui:modal id="surveyUrls" text="${message(code: 'default.add.label', args: [message(code: 'surveyconfig.url.label', args: [index])])}" msgSave="${message(code: 'default.button.add.label')}">
    <g:form class="ui form" controller="survey" action="addSurveyUrl" params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]" method="post">
        <div class="field">
            <label for="url">${message(code: 'surveyconfig.url.label', args: [index])}</label>
            <input type="text" name="url" id="url"/>
        </div><!-- .field -->

        <div class="field">
            <label for="urlComment">${message(code: 'surveyconfig.urlComment.label', args: [index])}</label>
            <textarea class="la-textarea-resize-vertical" name="urlComment" id="urlComment"></textarea>
        </div><!-- .field -->
    </g:form>
</ui:modal>
