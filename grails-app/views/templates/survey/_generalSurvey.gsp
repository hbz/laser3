<%@ page import="de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<div class="ui stackable grid">
    <div class="eleven wide column">
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">
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
        </g:if>

        <div class="ui card ">
            <div class="content">

                <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_PRO) && surveyOrg}">
                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
                        </dt>
                        <dd><ui:xEditable owner="${surveyOrg}" field="ownerComment" type="textarea"/></dd>

                    </dl>
                </g:if>

                <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
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

                    <ui:card message="${message(code: 'surveyconfig.url.plural.label')}" href="#surveyUrls"
                             editable="${editable}">
                        <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                            <dl>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.url.label', args: [i+1])}
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                                    <g:if test="${surveyUrl.url}">
                                        <ui:linkWithIcon href="${surveyUrl.url}"/>
                                    </g:if>

                                </dd>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.urlComment.label', args: [i+1])}
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${surveyUrl}" field="urlComment" type="textarea"/>
                                </dd>

                                <div class="right aligned">
                                    <g:if test="${editable}">
                                        <span class="la-popup-tooltip la-delay"
                                              data-content="${message(code: 'default.button.delete.label')}">
                                            <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [message(code: 'surveyconfig.url.label', args: [i+1])])}"
                                                    data-confirm-term-how="delete"
                                                    controller="survey" action="addSurveyUrl"
                                                    params="${[deleteSurveyUrl: surveyUrl.id, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
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

                    <br />

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

                </g:if>
                <g:else>
                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label', args: [i+1])}
                            </dt>
                            <dd>
                                <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                                overwriteEditable="${false}"/>

                                <g:if test="${surveyUrl.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyUrl.urlComment}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>
                                <ui:linkWithIcon href="${surveyUrl.url}"/>
                            </dd>
                        </dl>
                    </g:each>


                    <div class="ui form la-padding-left-07em">
                        <div class="field">
                            <label>
                                <g:message code="surveyConfigsInfo.comment"/>
                            </label>
                            <g:if test="${surveyConfig.comment}">
                                <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                    <div id="comment-wrapper-${surveyConfig.id}">
                                        <article id="comment-${surveyConfig.id}" class="ui segment trumbowyg-editor trumbowyg-reset-css" style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                            ${raw(surveyConfig.comment)}
                                        </article>
                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            wysiwyg.analyzeNote_TMP( $("#comment-${surveyConfig.id}"), $("#comment-wrapper-${surveyConfig.id}"), true );
                                        </laser:script>
                                    </div>
                                </g:if>
                                <g:else>
                                    <textarea class="la-textarea-resize-vertical" readonly="readonly"
                                              rows="1">${surveyConfig.comment}</textarea>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:message code="surveyConfigsInfo.comment.noComment"/>
                            </g:else>
                        </div>
                    </div>
                </g:else>


            </div>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                      model="[linkType: 'License', surveyInfo: surveyInfo, editable: editable, surveyConfig  : surveyConfig]"/>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                      model="[linkType: 'Provider', surveyInfo: surveyInfo, editable: editable, surveyConfig  : surveyConfig]"/>
        </div>

    </div>
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">

            <div id="container-documents">
                <laser:render template="/survey/surveyLinkCard"/>
            </div>

            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <div id="container-tasks">
                    <laser:render template="/templates/tasks/card"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
                </div>


                <div id="container-notes">
                    <laser:render template="/templates/notes/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: accessService.ctxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_EDITOR')]}"/>
                </div>
            </g:if>

            <div id="container-documents">
                <laser:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>
        </div>
    </aside>

</div><!-- .grid -->

<laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>

<laser:script file="${this.getGroovyPageFileName()}">

    $('textarea').each(function () {
        this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    });

</laser:script>