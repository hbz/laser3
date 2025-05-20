<%@ page import="de.laser.utils.DateUtils; de.laser.ui.Icon" %>
<ui:modal id="modalEditComment" text="${commentTyp == 'comment' ? message(code: 'surveyconfig.comment.label') : message(code: 'surveyconfig.commentForNewParticipants.label')}" isEditModal="true">

    <g:form action="setSurveyConfigComment" controller="survey" method="post" class="ui form"
            params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id, commentTyp: commentTyp]">
        <div class="field">
                <g:if test="${commentTyp == 'comment'}">
                    <label for="comment">${message(code: "surveyconfig.comment.comment")}:</label>
                    <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                        <div id="commentDiv">
                            <div id="comment">${raw(surveyConfig.comment)}</div>

                            <laser:script file="${this.getGroovyPageFileName()}">
                                wysiwyg.initEditor('#commentDiv #comment');
                            </laser:script>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="ui form">
                            <div class="field">
                                <textarea class="la-textarea-resize-vertical" name="comment"
                                          rows="15">${surveyConfig.comment}</textarea>
                            </div>
                        </div>
                    </g:else>
                </g:if>
                <g:if test="${commentTyp == 'commentForNewParticipants'}">
                    <label for="comment">${message(code: "surveyconfig.commentForNewParticipants.comment")}:</label>
                    <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                        <div id="commentForNewParticipantsDiv">
                            <div id="commentForNewParticipants">${raw(surveyConfig.commentForNewParticipants)}</div>

                            <laser:script file="${this.getGroovyPageFileName()}">
                                wysiwyg.initEditor('#commentForNewParticipantsDiv #commentForNewParticipants');
                            </laser:script>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="ui form">
                            <div class="field">
                                <textarea class="la-textarea-resize-vertical" name="commentForNewParticipants"
                                          rows="15">${surveyConfig.commentForNewParticipants}</textarea>
                            </div>
                        </div>
                    </g:else>
                </g:if>
        </div>
    </g:form>
</ui:modal>
