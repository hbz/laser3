<ui:modal id="surveyFinishModal" modalSize="tiny"
          msgSave="${message(code: 'confirm.dialog.concludeBinding')}"
          msgClose="${error ? message(code: 'surveyInfo.backToSurvey') : message(code: 'default.button.cancel')}"
          hideSubmitButton="${error ? true : false}">

    <g:form action="surveyInfoFinish" controller="myInstitution" class="ui form" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" method="post">

        <div class="ui header">
            <g:if test="${message}">
                ${message}
            </g:if>
            <g:if test="${error}">
                ${error}
            </g:if>
        </div>

        <g:if test="${surveyConfig.subSurveyUseForTransfer && noParticipation}">
            <div class="field">
                <label for="surveyResultComment">${message(code: 'surveyResult.noParticipation.info')}:</label>

                <g:textArea id="surveyResultComment" name="surveyResultComment" value="${surveyResult.comment}" rows="5" cols="40"/>
            </div>
        </g:if>

    </g:form>
</ui:modal>