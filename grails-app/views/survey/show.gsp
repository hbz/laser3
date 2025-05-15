<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.survey.SurveyResult" %>
<laser:htmlStart message="surveyShow.label"/>

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>surveyInfo.dateCreated: ${surveyInfo.dateCreated}</p>

        <p>surveyInfo.lastUpdated: ${surveyInfo.lastUpdated}</p>
    </div>
</ui:debugInfo>


<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
    <ui:xEditable owner="${surveyInfo}" field="name"/>
</ui:h1HeaderWithIcon>
<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}"
                       href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}"/>

<ui:messages data="${flash}"/>

<g:if test="${surveyLinksMessage}">
    <ui:msg class="error" showIcon="true">
        <g:each in="${surveyLinksMessage}" var="msg">
            &bullet; ${msg} <br/>
        </g:each>
    </ui:msg>
</g:if>

<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.startDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                    <ui:xEditable owner="${surveyInfo}" field="startDate" type="date"/>
                                </g:if><g:else>
                                    <ui:xEditable owner="${surveyInfo}" field="startDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.endDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id, RDStore.SURVEY_SURVEY_STARTED.id]}">
                                    <ui:xEditable owner="${surveyInfo}" field="endDate" type="date"/>
                                </g:if><g:else>
                                    <ui:xEditable owner="${surveyInfo}" field="endDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.comment.label')}</dt>
                            <dd><ui:xEditable owner="${surveyInfo}" field="comment" type="textarea"/></dd>

                        </dl>

                        <dl>
                            <dt class="control-label">
                                <div class="ui icon la-popup-tooltip"
                                     data-content="${message(code: "surveyconfig.internalComment.comment")}">
                                    ${message(code: 'surveyconfig.internalComment.label')}
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </div>
                            </dt>
                            <dd><ui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                        </dl>

                        <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

                        <g:if test="${surveyConfig.subscription}">
                            <dl>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.subOrgs.label')}
                                </dt>
                                <dd>
                                    <g:link controller="subscription" action="members"
                                            id="${subscription.id}">
                                        <ui:bubble count="${countParticipants.subMembers}"/>
                                    </g:link>
                                </dd>
                            </dl>
                        </g:if>
                        <g:if test="${surveyConfig.subscription && countParticipants.subMembersWithMultiYear > 0}">
                            <dl>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')}
                                </dt>
                                <dd>
                                    <g:link controller="subscription" action="members"
                                            id="${subscription.id}" params="[subRunTimeMultiYear: 'on']">
                                        <ui:bubble count="${countParticipants.subMembersWithMultiYear}"/>
                                    </g:link>
                                </dd>
                            </dl>
                        </g:if>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'default.status.label')}</dt>
                            <dd>
                                ${surveyInfo.status.getI10n('value')}
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.type.label')}</dt>
                            <dd>
                                <div class="ui label survey-${surveyInfo.type.value}">
                                    ${surveyInfo.type.getI10n('value')}
                                </div>
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.isMandatory')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id] && !surveyInfo.isSubscriptionSurvey}">
                                    <ui:xEditableBoolean owner="${surveyInfo}" field="isMandatory"/>
                                </g:if><g:else>
                                    <ui:xEditableBoolean owner="${surveyInfo}" field="isMandatory" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>

                        <g:if test="${surveyInfo.isSubscriptionSurvey && surveyInfo.surveyConfigs.size() >= 1}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.subSurveyUseForTransfer.label')}</dt>
                                <dd>
                                    ${surveyInfo.surveyConfigs[0].subSurveyUseForTransfer ? message(code: 'refdata.Yes') : message(code: 'refdata.No')}
                                </dd>

                            </dl>

                        </g:if>

                        <g:if test="${surveyInfo.type != RDStore.SURVEY_TYPE_TITLE_SELECTION}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.packageSurvey.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="packageSurvey"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="packageSurvey" overwriteEditable="false"/>
                                    </g:else>

                                </dd>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.vendorSurvey.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="vendorSurvey"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="vendorSurvey" overwriteEditable="false"/>
                                    </g:else>

                                </dd>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.invoicingInformation.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="invoicingInformation"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="invoicingInformation" overwriteEditable="false"/>
                                    </g:else>
                                </dd>
                            </dl>
                            <g:if test="${surveyInfo.type == RDStore.SURVEY_TYPE_INTEREST}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.subscriptionSurvey.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="subscriptionSurvey"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="subscriptionSurvey" overwriteEditable="false"/>
                                    </g:else>

                                </dd>
                            </dl>
                            </g:if>
                        </g:if>

                        <g:if test="${surveyInfo.type == RDStore.SURVEY_TYPE_TITLE_SELECTION}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.pickAndChoosePerpetualAccess.label')}</dt>
                                <dd>
                                    ${surveyInfo.surveyConfigs[0].pickAndChoosePerpetualAccess ? message(code: 'refdata.Yes') : message(code: 'refdata.No')}
                                </dd>

                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.new')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditable owner="${surveyInfo.surveyConfigs[0]}" field="issueEntitlementGroupName"/>
                                    </g:if><g:else>
                                        <ui:xEditable owner="${surveyInfo.surveyConfigs[0]}" field="issueEntitlementGroupName" overwriteEditable="false"/>
                                    </g:else>
                                </dd>

                            </dl>

                        </g:if>

                    </div>
                </div>
            </div>

            <g:set var="finish"
                   value="${SurveyOrg.countBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>
            <g:set var="total"
                   value="${SurveyOrg.countBySurveyConfig(surveyConfig)}"/>

            <g:set var="finishProcess" value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
            <g:if test="${finishProcess > 0 || surveyInfo.status?.id == RDStore.SURVEY_SURVEY_STARTED.id}">
                <div class="ui card">

                    <div class="content">
                        <div class="ui indicating progress" id="finishProcess2" data-percent="${finishProcess}">
                            <div class="bar">
                            </div>

                            <div class="label"
                                 style="background-color: transparent"><g:formatNumber number="${finishProcess}"
                                                                                       type="number"
                                                                                       maxFractionDigits="2"
                                                                                       minFractionDigits="2"/>% <g:message
                                    code="surveyInfo.finished"/></div>
                        </div>
                    </div>
                </div>
            </g:if>


            <br/>
            <g:if test="${surveyConfig}">

                <div class="ui top attached stackable tabular la-tab-with-js menu">
                    <g:link class="item ${params.viewTab == 'overview' ? 'active' : ''}"
                            controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                            params="${params + [viewTab: 'overview']}">

                        ${message(code: 'default.overview.label')}
                    </g:link>

                    <g:if test="${surveyConfig.subscription}">
                        <g:link class="item ${params.viewTab == 'additionalInformation' ? 'active' : ''}"
                                controller="${controllerName}" action="${actionName}" id="${surveyInfo.id}"
                                params="${params + [viewTab: 'additionalInformation']}">

                            ${message(code: 'surveyOrg.additionalInformation')}
                        </g:link>
                    </g:if>

                </div>

                <div class="ui bottom attached tab segment active">
                    <g:if test="${params.viewTab == 'overview'}">
                        <div class="ui stackable grid">
                            <div class="eleven wide column">
                                <ui:card message="surveyconfig.url.plural.label" href="#surveyUrls" editable="${editable}">
                                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                                        <dl>
                                            <dt class="control-label">
                                                ${message(code: 'surveyconfig.url.label', args: [i + 1])}
                                            </dt>
                                            <dd>
                                                <ui:xEditable owner="${surveyUrl}" field="url" type="url"/>
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
                                                    <span class="la-popup-tooltip"
                                                          data-content="${message(code: 'default.button.delete.label')}">
                                                        <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [message(code: 'surveyconfig.url.label', args: [i + 1])])}"
                                                                data-confirm-term-how="delete"
                                                                controller="survey" action="addSurveyUrl"
                                                                params="${[deleteSurveyUrl: surveyUrl.id, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                            <i class="${Icon.CMD.DELETE}"></i>
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
                                    <div class="ui top attached tabular menu">
                                        <g:if test="${surveyConfig.subscription}">
                                            <a class="item ${commentTab != 'commentForNewParticipants' ? 'active' : ''}" data-tab="comment">
                                                <div class="ui icon la-popup-tooltip"
                                                     data-content="${message(code: "surveyconfig.comment.comment")}">
                                                    ${message(code: 'surveyconfig.comment.label')}
                                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                                </div>
                                                <br>
                                            </a>
                                        </g:if>
                                        <a class="item ${commentTab == 'commentForNewParticipants' ? 'active' : ''}"
                                           data-tab="commentForNewParticipants">
                                            <div class="ui icon la-popup-tooltip"
                                                 data-content="${message(code: "surveyconfig.commentForNewParticipants.comment")}">
                                                ${message(code: 'surveyconfig.commentForNewParticipants.label')}
                                                <i class="${Icon.TOOLTIP.HELP}"></i>
                                            </div>
                                        </a>
                                    </div>
                                    <g:if test="${surveyConfig.subscription}">
                                        <div class="ui bottom attached tab segment ${commentTab != 'commentForNewParticipants' ? 'active' : ''}"
                                             data-tab="comment">
                                            <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                                <g:if test="${editable}">
                                                    <a class="${Btn.MODERN.SIMPLE} right floated"
                                                       onclick="JSPC.app.editComment(${surveyInfo.id}, ${surveyConfig.id}, 'comment');"
                                                       role="button"
                                                       aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                        <i class="${Icon.CMD.EDIT}"></i>
                                                    </a>
                                                    <br>
                                                </g:if>

                                                <div id="comment-wrapper-${surveyConfig.id}">
                                                    <article id="comment-${surveyConfig.id}" class="trumbowyg-editor trumbowyg-reset-css"
                                                             style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                                        ${raw(surveyConfig.comment)}
                                                    </article>
                                                    <laser:script file="${this.getGroovyPageFileName()}">
                                                        wysiwyg.analyzeNote_TMP( $("#comment-${surveyConfig.id}"), $("#comment-wrapper-${surveyConfig.id}"), true );
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
                                        </div>
                                    </g:if>

                                    <div class="ui bottom attached tab segment ${commentTab == 'commentForNewParticipants' ? 'active' : ''}"
                                         data-tab="commentForNewParticipants">
                                        <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                            <g:if test="${editable}">
                                                <a class="${Btn.MODERN.SIMPLE} right floated"
                                                   onclick="JSPC.app.editComment(${surveyInfo.id}, ${surveyConfig.id}, 'commentForNewParticipants');"
                                                   role="button"
                                                   aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                    <i class="${Icon.CMD.EDIT}"></i>
                                                </a>
                                                <br>
                                            </g:if>

                                            <div id="commentForNewParticipants-wrapper-${surveyConfig.id}">
                                                <article id="commentForNewParticipants-${surveyConfig.id}" class="trumbowyg-editor trumbowyg-reset-css"
                                                         style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                                    ${raw(surveyConfig.commentForNewParticipants)}
                                                </article>
                                                <laser:script file="${this.getGroovyPageFileName()}">
                                                    wysiwyg.analyzeNote_TMP( $("#commentForNewParticipants-${surveyConfig.id}"), $("#commentForNewParticipants-wrapper-${surveyConfig.id}"), true );
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
                                    </div>

                                </div>

                                <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                                    <div class="ui card">
                                        <laser:render template="/survey/linksProviderOrLicense"
                                                      model="[linkType: 'License', surveyInfo: surveyInfo, editable: editable, surveyConfig: surveyConfig]"/>
                                    </div>

                                    <div class="ui card">
                                        <laser:render template="/survey/linksProviderOrLicense"
                                                      model="[linkType: 'Provider', surveyInfo: surveyInfo, editable: editable, surveyConfig: surveyConfig]"/>
                                    </div>
                                </g:if>

                                <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">
                                    <laser:render template="/templates/survey/costsGeneralSurvey"/>
                                </g:if>
                                <g:if test="${surveyConfig.subscription}">
                                    <laser:render template="/templates/survey/costsWithSub"/>
                                </g:if>

                            </div>

                            <aside class="five wide column la-sidekick">
                                <div class="ui one cards">

                                    <div id="container-links">
                                        <laser:render template="/survey/surveyLinkCard"/>
                                    </div>

                                    <div id="container-notes">
                                        <laser:render template="/templates/notes/card"
                                                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_PRO)]}"/>
                                    </div>

                                    <div id="container-tasks">
                                        <laser:render template="/templates/tasks/card"
                                                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
                                    </div>


                                    <div id="container-documents">
                                        <laser:render template="/survey/cardDocuments"
                                                      model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
                                    </div>

                                </div>
                            </aside>
                        </div>

                        <laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>

                    </g:if>


                    <g:if test="${params.viewTab == 'additionalInformation' && surveyConfig.subscription}">
                        <div class="sixteen wide column">
                            <div class="la-inline-lists">
                                <laser:render template="/templates/survey/subscriptionSurvey" model="[visibleProviders: providerRoles]"/>
                            </div>
                        </div>
                    </g:if>
                </div>
            </g:if>
            <g:else>
                <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
            </g:else>

        </div>

        <br/>
        <br/>
        <g:if test="${editable}">
            <g:form action="setSurveyWorkFlowInfos" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, setSurveyWorkFlowInfo: 'setSurveyConfigFinish']">

                <div class="ui right floated compact segment">
                    <div class="ui checkbox">
                        <input type="checkbox" onchange="this.form.submit()"
                               name="configFinish" ${surveyConfig.configFinish ? 'checked' : ''}>
                        <label><g:message code="surveyconfig.configFinish.label"/></label>
                    </div>
                </div>

            </g:form>
        </g:if>

    </div><!-- .twelve -->

</div><!-- .grid -->


<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
    $('#finishProcess2').progress();

    JSPC.app.editComment = function (surveyID, surveyConfigID, commentTyp) {
            $.ajax({
                url: '<g:createLink controller="survey" action="editComment"/>?id=' + surveyID+'&surveyConfigID='+surveyConfigID+'&commentTyp='+commentTyp,
                success: function(result){
                    $('#dynamicModalContainer').empty();
                    $('#modalEditComment').remove();

                    $('#dynamicModalContainer').html(result);
                    $('#dynamicModalContainer .ui.modal').modal({
                        autofocus: false,
                        closable: false,
                        onVisible: function() {
                            r2d2.helper.focusFirstFormElement(this);
                        }
                    }).modal('show');
                }
            });
        }
</laser:script>


<laser:htmlEnd/>
