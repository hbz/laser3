<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.utils.DateUtils;" %>

<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextService.getOrg())}"/>
<div class="ui stackable grid">
    <div class="eleven wide column">
        <div class="la-inline-lists">

            <g:if test="${contextService.getOrg().isCustomerType_Consortium_Pro() && surveyOrg}">
                <dl>
                    <dt class="control-label">
                        ${message(code: 'surveyOrg.ownerComment.label', args: [contextService.getOrg().sortname])}
                    </dt>
                    <dd><ui:xEditable owner="${surveyOrg}" field="ownerComment" type="textarea"/></dd>

                </dl>
            </g:if>

            <div class="ui card">
                <div class="content">

                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label', args: [i + 1])}
                            </dt>
                            <dd>
                                <ui:xEditable owner="${surveyUrl}" field="url" type="text" overwriteEditable="${false}"/>

                                <g:if test="${surveyUrl.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${surveyUrl.urlComment}">
                                        <i class="${Icon.TOOLTIP.INFO}"></i>
                                    </span>
                                </g:if>
                                <ui:linkWithIcon href="${surveyUrl.url}"/>
                            </dd>
                        </dl>
                    </g:each>

                    <g:if test="${subscription}">
                        <div class="ui form la-padding-left-07em">
                            <div class="field">
                                <label>
                                    <g:message code="surveyConfigsInfo.comment"/>
                                </label>
                                <g:if test="${surveyConfig.comment}">
                                    <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                        <div id="comment-wrapper-${surveyConfig.id}">
                                            <article id="comment-${surveyConfig.id}" class="ui segment trumbowyg-editor trumbowyg-reset-css"
                                                     style="margin:0; padding:0.5em 1em; box-shadow:none;">
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
                    </g:if>
                    <g:else>
                        <div class="ui form la-padding-left-07em">
                            <div class="field">
                                <label>
                                    <g:message code="surveyConfigsInfo.comment"/>
                                </label>
                                <g:if test="${surveyConfig.commentForNewParticipants}">
                                    <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                        <div id="commentForNewParticipants-wrapper-${surveyConfig.id}">
                                            <article id="commentForNewParticipants-${surveyConfig.id}" class="ui segment trumbowyg-editor trumbowyg-reset-css"
                                                     style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                                ${raw(surveyConfig.commentForNewParticipants)}
                                            </article>
                                            <laser:script file="${this.getGroovyPageFileName()}">
                                                wysiwyg.analyzeNote_TMP( $("#commentForNewParticipants-${surveyConfig.id}"), $("#commentForNewParticipants-wrapper-${surveyConfig.id}"), true );
                                            </laser:script>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <textarea class="la-textarea-resize-vertical" readonly="readonly"
                                                  rows="1">${surveyConfig.commentForNewParticipants}</textarea>
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

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">
                <laser:render template="/templates/survey/generalSurvey"/>
            </g:if>

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">
                <laser:render template="/templates/survey/entitlementSurvey"/>
            </g:if>

        </div>
    </div>
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">

            <div id="container-links">
                <laser:render template="/survey/surveyLinkCard"/>
            </div>

            <div id="container-documents">
                <laser:render template="/survey/cardDocuments"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>

        </div>
    </aside>

    <div class="sixteen wide column">
        <div class="la-inline-lists">
            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">
                <laser:render template="/templates/survey/costsWithSub"/>

                <laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>
            </g:if>

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">
                <laser:render template="/templates/survey/costsGeneralSurvey"/>

                <laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>
            </g:if>

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">

                <g:if test="${subscription}">
                    <laser:render template="/templates/survey/costsWithSub"/>
                </g:if>

                <laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>
            </g:if>
        </div>

%{--        <g:if test="${surveyConfig.invoicingInformation}">
            <g:link class="${Btn.SIMPLE} left floated"
                    controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                    params="${parame+[viewTab: 'invoicingInformation']}">
                ${message(code: 'default.edit.label', args:  [message(code: 'surveyOrg.invoicingInformation')])}
            </g:link>
        </g:if>--}%

    </div>
</div>



<laser:script file="${this.getGroovyPageFileName()}">

    $('textarea').each(function () {
        this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    });

</laser:script>