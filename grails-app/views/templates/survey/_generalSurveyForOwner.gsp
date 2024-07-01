<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<div class="ui stackable grid">
    <div class="eleven wide column">

        <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
        <div class="ui horizontal segments">

            <div class="ui segment left aligned">
                <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
                <g:link controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
                </g:link>
            </div>
        </div>

        <dl>
            <dt class="control-label">
                <div class="ui icon la-popup-tooltip la-delay"
                     data-content="${message(code: "surveyconfig.internalComment.comment")}">
                    ${message(code: 'surveyconfig.internalComment.label')}
                    <i class="question small circular inverted icon"></i>
                </div>
            </dt>
            <dd><ui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

        </dl>

        <ui:card message="surveyconfig.url.plural.label" href="#surveyUrls" editable="${editable}">
            <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                <dl>
                    <dt class="control-label">
                        ${message(code: 'surveyconfig.url.label', args: [i + 1])}
                    </dt>
                    <dd>
                        <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                        <g:if test="${surveyUrl.url}">
                            <ui:linkWithIcon href="${surveyUrl.url}"/>
                        </g:if>

                    </dd>
                    <dt class="control-label">
                        ${message(code: 'surveyconfig.urlComment.label', args: [i + 1])}
                    </dt>
                    <dd>
                        <ui:xEditable owner="${surveyUrl}" field="urlComment" type="textarea"/>
                    </dd>

                    <div class="right aligned">
                        <g:if test="${editable}">
                            <span class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'default.button.delete.label')}">
                                <g:link class="ui negative icon button la-modern-button la-selectable-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [message(code: 'surveyconfig.url.label', args: [i + 1])])}"
                                        data-confirm-term-how="delete"
                                        controller="survey" action="addSurveyUrl"
                                        params="${[deleteSurveyUrl: surveyUrl.id, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icons.CMD_DELETE}"></i>
                                </g:link>
                            </span>
                        </g:if>
                    </div>

                </dl>
            </g:each>

            <g:if test="${editable}">
                <laser:render template="/survey/surveyUrlsModal"/>
            </g:if>

        </ui:card>

        <br/>

        <div class="ui la-tab-with-js">
            <g:form action="setSurveyConfigComment" controller="survey" method="post"
                    params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]">
                <div class="ui top attached tabular menu">
                    <a class="item active" data-tab="comment">
                        <div class="ui icon la-popup-tooltip la-delay"
                             data-content="${message(code: "surveyconfig.comment.comment")}">
                            ${message(code: 'surveyconfig.comment.label')}
                            <i class="question small circular inverted icon"></i>
                        </div>
                    </a>
                </div>

                <div class="ui bottom attached tab segment active" data-tab="comment">
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
                    <br>

                    <div class="left aligned">
                        <button type="submit"
                                class="ui button">${message(code: 'default.button.save_changes')}</button>
                    </div>
                </div>
            </g:form>

        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                          model="[linkType: 'License', surveyInfo: surveyInfo, editable: editable, surveyConfig: surveyConfig]"/>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                          model="[linkType: 'Provider', surveyInfo: surveyInfo, editable: editable, surveyConfig: surveyConfig]"/>
        </div>

    </div>


    <aside class="five wide column la-sidekick">
        <div class="ui one cards">

            <div id="#container-links">
                <laser:render template="/survey/surveyLinkCard"/>
            </div>

            <div id="container-tasks">
                    <laser:render template="/templates/tasks/card"
                                  model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>


            <div id="container-notes">
                    <laser:render template="/templates/notes/card"
                                  model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)]}"/>
            </div>

            <div id="container-documents">
                <laser:render template="/survey/cardDocuments"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>
        </div>
    </aside>

</div>

<laser:render template="/templates/survey/costsGeneralSurvey"/>

<laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>

<laser:script file="${this.getGroovyPageFileName()}">

    $('textarea').each(function () {
        this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    });

</laser:script>