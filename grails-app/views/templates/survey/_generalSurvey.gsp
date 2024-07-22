<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

        <div class="ui card">
            <div class="content">
                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label', args: [i+1])}
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


