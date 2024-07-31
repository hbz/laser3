<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.Person; de.laser.Doc; de.laser.Subscription; de.laser.FormService" %>
<laser:serviceInjection />


    <g:set var="isInstAdm" value="${contextService.isInstAdm_or_ROLEADMIN()}"/>

    <g:if test="${!fromSurvey && !copyObject}">
        <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject: sourceObject,
                targetObject: targetObject,
                allObjects_readRights: allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

%{--<pre>--}%
%{--copyObject          : ${copyObject}--}%
%{--targetObject        : ${targetObject}--}%
%{--isConsortialObjects : ${isConsortialObjects}--}%
%{--</pre>--}%

    <g:form action="${actionName}" controller="${controllerName}" id="${params.id ?: params.sourceObjectId}" data-confirm-id="copyElements_form"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey, copyObject: copyObject]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <table class="ui celled table la-js-responsive-table la-table">
            <thead>
                <tr>
                    <th class="six wide">
                        <g:if test="${sourceObject}"><g:link controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="show" id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <g:if test="${isConsortialObjects}">
                        <th class="one wide center aligned">
                            <g:message code="copyElementsIntoObject.share"/> / <g:message code="copyElementsIntoObject.audit"/>
                        </th>
                    </g:if>
                    <th class="one wide center aligned" data-label="${message(code:'responsive.table.selectElement')}">
                        <input type="checkbox" data-action="copy" onClick="JSPC.app.toggleAllCheckboxes(this)" checked />
                    </th>
                    <g:if test="${!copyObject}">
                                <th class="six wide">
                                    <g:if test="${targetObject}"><g:link controller="${targetObject.getClass().getSimpleName().toLowerCase()}" action="show" id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link></g:if>
                                </th>
                                <th class="one wide center aligned" data-label="${message(code:'responsive.table.selectElement')}">
                                    <g:if test="${targetObject}">
                                        <input class="setDeletionConfirm" type="checkbox" data-action="delete" onClick="JSPC.app.toggleAllCheckboxes(this)" />
                                    </g:if>
                                </th>
                    </g:if>
                </tr>
            </thead>
            <tbody class="top aligned">
            <g:each in="${['notes', 'tasks', 'docs', 'workflows']}" var="tmpl_cfg_tr">
                <g:if test="${tmpl_cfg_tr == 'docs'}">
                %{--DOCUMENTS:--}%
                <tr data-element="copyObject.takeDocs">
                    <td data-element="source">
                        <div class="la-min-height"><strong><i class="${Icon.DOCUMENT}"></i>&nbsp;${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeDocs")}:</strong></div>
                        <g:each in="${sourceDocuments}" var="docctx">
                            <g:if test="${(docctx.isDocAFile() && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                <div data-id="${docctx.id}" class="la-element la-min-height">
                                    <label>
                                        <g:link controller="docstore" id="${docctx.owner.uuid}" target="_blank">
                                            <g:if test="${docctx.owner?.title}">
                                                ${docctx.owner.title}
                                            </g:if>
                                            <g:else>
                                                <g:if test="${docctx.owner?.filename}">
                                                    ${docctx.owner.filename}
                                                </g:if>
                                                <g:else>
                                                    ${message(code: 'template.documents.missing')}
                                                </g:else>
                                            </g:else>
                                        </g:link>
                                        <g:if test="${docctx.getDocType()}">(${docctx.getDocType().getI10n("value")})</g:if>
                                    </label>
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                            <div class="la-min-height"></div>
                            <g:each in="${sourceDocuments}" var="docctx">
                                <g:if test="${(docctx.isDocAFile() && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                    <div class="ui checkbox la-toggle-radio la-inherit la-min-height">
                                        <g:checkBox name="copyObject.toggleShare" value="${docctx.id}"
                                                    checked="${docctx.isShared ? 'true' : 'false'}"/>
                                    </div>
                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <div class="la-min-height"></div>
                        <g:each in="${sourceDocuments}" var="docctx">
                            <g:if test="${(docctx.isDocAFile() && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace la-min-height">
                                    <g:checkBox name="copyObject.takeDocIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                    <g:if test="${!copyObject && targetObject}">
                        <td data-element="target">
                            <div class="la-min-height"><strong><i class="${Icon.DOCUMENT}"></i>&nbsp;${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeDocs")}:</strong><br /></div>
                            <g:if test="${targetObject}">
                                    <g:each in="${targetDocuments}" var="docctx">
                                        <g:if test="${(docctx.isDocAFile() && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                            <div data-id="${docctx.id}" class="la-element la-min-height">
                                                <g:link controller="docstore" id="${docctx.owner.uuid}" target="_blank">
                                                    <g:if test="${docctx.owner?.title}">
                                                        ${docctx.owner.title}
                                                    </g:if>
                                                    <g:else>
                                                        <g:if test="${docctx.owner?.filename}">
                                                            ${docctx.owner.filename}
                                                        </g:if>
                                                        <g:else>
                                                            ${message(code: 'template.documents.missing')}
                                                        </g:else>
                                                    </g:else>
                                                </g:link>
                                                <g:if test="${docctx.getDocType()}">(${docctx.getDocType().getI10n("value")})</g:if>
                                                <g:if test="${isConsortialObjects}">
                                                    <g:if test="${docctx.isShared}">
                                                        <span data-position="top left" class="la-popup-tooltip la-float-right" data-content="${message(code:'property.share.tooltip.on')}">
                                                            <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                                        </span>
                                                    </g:if>
                                                    <g:else>
                                                        <span data-position="top left" class="la-popup-tooltip la-float-right" data-content="${message(code:'property.share.tooltip.off')}">
                                                            <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                                        </span>
                                                    </g:else>
                                                </g:if>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </g:if>
                        </td>
                        %{--DELETE:--}%
                        <td>
                            <div class="la-min-height"></div>
                            <g:each in="${targetDocuments}" var="docctx">
                                <g:if test="${(docctx.isDocAFile() && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                    %{--<div class="ui checkbox">--}%
                                    <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm la-min-height">
                                        <g:checkBox name="copyObject.deleteDocIds" value="${docctx?.id}" data-action="delete" checked="${false}"/>
                                    </div>
                                    %{--</div>--}%
                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                    <g:if test="${!copyObject && !targetObject}">%{-- tmp fixed markup --}%
                        <td></td>
                        <td></td>
                    </g:if>
                </tr>
                </g:if>
                <g:if test="${tmpl_cfg_tr == 'notes'}">
                %{--ANNOUNCEMENTS:--}%
                <tr data-element="copyObject.takeAnnouncements">
                    <td data-element="source">
                        <div class="la-min-height"><strong><i class="${Icon.SYM.NOTE}"></i>&nbsp;${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeAnnouncements")}:</strong></div>
                        <g:each in="${sourceDocuments}" var="docctx">
                            <g:if test="${docctx.isDocANote() && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id}">
                                <div data-id="${docctx.id}" class="la-element la-min-height ">
                                    <label>
                                        <g:if test="${docctx.owner.title}">
                                            <strong>${docctx.owner.title}</strong>
                                        </g:if>
                                        <g:else>
                                            <strong>Ohne Titel</strong>
                                        </g:else>
                                        (${message(code: 'template.notes.created')}
                                        <g:formatDate
                                                format="${message(code: 'default.date.format.notime')}"
                                                date="${docctx.owner.dateCreated}"/>)
                                    </label>
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                            <div class="la-min-height"></div>
                            <g:each in="${sourceDocuments}" var="docctx">
                                <g:if test="${docctx.isDocANote() && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id}">
                                    <div class="ui checkbox la-toggle-radio la-inherit la-min-height">
                                        <g:checkBox name="copyObject.toggleShare" value="${docctx.id}"
                                                    checked="${docctx.isShared ? 'true' : 'false'}"/>
                                    </div>
                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <div class="la-min-height"></div>
                        <g:each in="${sourceDocuments}" var="docctx">
                            <g:if test="${docctx.isDocANote() && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id}">
                                %{--<div data-id="${docctx.id} " class="la-element">--}%
                                    %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace la-min-height">
                                        <g:checkBox name="copyObject.takeAnnouncementIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                    %{--</div>--}%
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                        <g:if test="${!copyObject && targetObject}">
                                    <td data-element="target">
                                        <div class="la-min-height"><strong><i class="${Icon.SYM.NOTE}"></i>&nbsp;${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeAnnouncements")}:</strong></div>
                                        <g:if test="${targetObject}">
                                                <g:each in="${targetDocuments}" var="docctx">
                                                    <g:if test="${docctx.isDocANote() && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id}">
                                                        <div data-id="${docctx.id}" class="la-element la-min-height">
                                                            <g:if test="${docctx.owner.title}">
                                                                <strong>${docctx.owner.title}</strong>
                                                            </g:if>
                                                            <g:else>
                                                                <strong>Ohne Titel</strong>
                                                            </g:else>
                                                            (${message(code: 'template.notes.created')}
                                                            <g:formatDate
                                                                    format="${message(code: 'default.date.format.notime')}"
                                                                    date="${docctx.owner.dateCreated}"/>)
                                                            <g:if test="${isConsortialObjects}">
                                                                <g:if test="${docctx.isShared}">
                                                                    <span data-position="top left" class="la-popup-tooltip la-float-right" data-content="${message(code:'property.share.tooltip.on')}">
                                                                        <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                                                    </span>
                                                                </g:if>
                                                                <g:else>
                                                                    <span data-position="top left" class="la-popup-tooltip la-float-right" data-content="${message(code:'property.share.tooltip.off')}">
                                                                        <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                                                    </span>
                                                                </g:else>
                                                            </g:if>
                                                        </div>
%{--                                                        <g:if test="${isConsortialObjects}">
                                                            <div class="right aligned wide column">
                                                                <g:if test="${docctx.isShared}">
                                                                    <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                                                        <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                                                    </span>
                                                                </g:if>
                                                                <g:else>
                                                                    <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.off')}">
                                                                        <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                                                    </span>
                                                                </g:else>

                                                            </div>
                                                        </g:if>--}%
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                    </td>
                                    %{--DELETE:--}%
                                    <td>
                                        <div class="la-min-height"></div>
                                            <g:if test="${targetObject}">
                                                <g:each in="${targetDocuments}" var="docctx">
                                                    <g:if test="${docctx.isDocANote() && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id}">
                                                        %{--<div class="ui checkbox">--}%
                                                        <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm la-min-height">
                                                            <g:checkBox name="copyObject.deleteAnnouncementIds" value="${docctx?.id}" data-action="delete"  checked="${false}"/>
                                                        </div>
                                                        %{--</div>--}%
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                    </td>
                        </g:if>
                </tr>
                </g:if>

                <g:if test="${tmpl_cfg_tr == 'tasks'}">
                %{--TASKS:--}%
                <tr data-element="copyObject.takeTasks">
                    <td data-element="source">
                        <div class="la-min-height"><strong><i class="${Icon.TASK}"></i>&nbsp;${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeTasks")}:</strong></div>
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element la-min-height">
                                <label>
                                    <strong>${tsk?.title}</strong> (${message(code: 'task.endDate.label')}
                                    <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)
                                </label>
                            </div>
                        </g:each>
                    </td>
                    <g:if test="${copyObject}">%{-- tmp fixed markup --}%
                        <td></td>
                    </g:if>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <div class="la-min-height"></div>
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace la-min-height">
                                        <g:checkBox name="copyObject.takeTaskIds" value="${tsk?.id}" data-action="copy"  />
                                </div>
                                %{--</div>--}%
                            </div>
                        </g:each>
                    </td>
                    <g:if test="${!copyObject && !(actionName == 'copyMyElements' && targetObject)}">%{-- todo: fix markup --}%
                        <td></td>
                    </g:if>
                    <g:if test="${!copyObject && targetObject}">
                                <td data-element="target">
                                    <div class="la-min-height"><strong><i class="${Icon.TASK}"></i>&nbsp;${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeTasks")}:</strong></div>
                                    <g:each in="${targetTasks}" var="tsk">
                                        <div data-id="${tsk?.id}" class="la-element la-min-height">
                                        <strong>${tsk?.title}</strong> (${message(code: 'task.endDate.label')}
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk?.endDate}"/>)
                                        </div>
                                    </g:each>
                                </td>
                                %{--DELETE:--}%
                                <td>
                                    <div class="la-min-height"></div>
                                    <g:each in="${targetTasks}" var="tsk">
                                        <g:if test="${tsk.creator.id == userId || isInstAdm}">
                                            %{--<div class="ui checkbox">--}%
                                            <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm la-min-height">
                                                <g:checkBox name="copyObject.deleteTaskIds" value="${tsk?.id}" data-action="delete"  checked="${false}" />
                                            </div>
                                            %{--</div>--}%
                                        </g:if>
                                        <g:else><br /></g:else>
                                    </g:each>
                                </td>
                    </g:if>
                    <g:if test="${!copyObject && !targetObject}">%{-- tmp fixed markup --}%
                        <td></td>
                    </g:if>
                </tr>
                </g:if>


                <g:if test="${tmpl_cfg_tr == 'workflows'}">
                %{--WORKFLOWS:--}%
                    <tr data-element="copyObject.takeWorkflows">
                        <td data-element="source">
                            <div class="la-min-height"><strong><i class="${Icon.WORKFLOW}"></i>&nbsp;${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeWorkflows")}:</strong></div>
                            <g:each in="${sourceWorkflows}" var="wf">
                                <div data-id="${wf.id}" class="la-element la-min-height">
                                    <label>
                                        <strong>${wf.title}</strong>
                                        (${message(code:'default.lastUpdated.label')} <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${wf.lastUpdated}"/>,
                                        ${message(code:'default.dateCreated.label')} <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${wf.dateCreated}"/>)
                                    </label>
                                </div>
                            </g:each>
                        </td>
                        <g:if test="${copyObject}">%{-- tmp fixed markup --}%
                            <td></td>
                        </g:if>
                        %{--COPY:--}%
                        <td class="center aligned">
                            <div class="la-min-height"></div>
                            <g:each in="${sourceWorkflows}" var="wf">
                                <div data-id="${wf.id}" class="la-element">
                                    %{--<div class="ui checkbox">--}%
                                    <div class="ui checkbox la-toggle-radio la-replace la-min-height">
                                        <g:checkBox name="copyObject.takeWorkflowIds" value="${wf.id}" data-action="copy"  />
                                    </div>
                                    %{--</div>--}%
                                </div>
                            </g:each>
                        </td>
                        <g:if test="${!copyObject && !(actionName == 'copyMyElements' && targetObject)}">%{-- todo: fix markup --}%
                            <td></td>
                        </g:if>
                        <g:if test="${!copyObject && targetObject}">
                            <td data-element="target">
                                <div class="la-min-height"><strong><i class="${Icon.WORKFLOW}"></i>&nbsp;${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeWorkflows")}:</strong></div>
                                <g:each in="${targetWorkflows}" var="wf">
                                    <div data-id="${wf.id}" class="la-element la-min-height">
                                        <strong>${wf.title}</strong>
                                        (${message(code:'default.lastUpdated.label')} <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${wf.lastUpdated}"/>,
                                        ${message(code:'default.dateCreated.label')} <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${wf.dateCreated}"/>)
                                    </div>
                                </g:each>
                            </td>
                        %{--DELETE:--}%
                            <td>
                                <div class="la-min-height"></div>
                                <g:each in="${targetWorkflows}" var="wf">
                                    <g:if test="${wf.owner.id == contextService.getOrg().id || isInstAdm}">
                                    %{--<div class="ui checkbox">--}%
                                        <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm la-min-height">
                                            <g:checkBox name="copyObject.deleteWorkflowIds" value="${wf.id}" data-action="delete" checked="${false}" />
                                        </div>
                                    %{--</div>--}%
                                    </g:if>
                                    <g:else><br /></g:else>
                                </g:each>
                            </td>
                        </g:if>
                        <g:if test="${!copyObject && !targetObject}">%{-- tmp fixed markup --}%
                            <td></td>
                        </g:if>
                    </tr>
                </g:if>



            </g:each>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'copyElementsIntoObject.copyDocsAndTasksAndWorkflows.button')}" />

        <g:if test="${!fromSurvey && !copyObject}">
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceObject && targetObject)? '' : 'disabled'}"/>
                <input type="submit" id="copyElementsSubmit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${submitButtonText}" data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete" ${submitDisabled}/>
            </div>
        </g:if>
        <g:elseif test="${copyObject}">
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'default.button.copy.label')}"/>
            </div>
        </g:elseif>
        <g:else>
            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <g:set var="surveyConfig" value="${SurveyConfig.get(Long.valueOf(fromSurvey))}"/>
                    <g:if test="${surveyConfig.isSubSurveyUseForTransfer()}">
                        <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" class="${Btn.SIMPLE_CLICKCONTROL}">
                            <g:message code="renewalEvaluation.back"/>
                        </g:link>
                    </g:if>
                </div>
                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled" value="${(sourceObject && targetObject)? '' : 'disabled'}"/>
                    <input type="submit" id="copyElementsSubmit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${submitButtonText}" data-confirm-id="copyElements"
                           data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                           data-confirm-term-how="delete" ${submitDisabled}/>
                </div>
            </div>
        </g:else>
    </g:form>

<g:if test="${!copyObject}">
    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.subCopyController = {

            checkboxes : {
                $takeDocIds:            $('input[name="copyObject.takeDocIds"]'),
                $deleteDocIds:          $('input[name="copyObject.deleteDocIds"]'),
                $takeAnnouncementIds:   $('input[name="copyObject.takeAnnouncementIds"]'),
                $deleteAnnouncementIds: $('input[name="copyObject.deleteAnnouncementIds"]'),
                $takeTaskIds:           $('input[name="copyObject.takeTaskIds"]'),
                $deleteTaskIds:         $('input[name="copyObject.deleteTaskIds"]'),
                $takeWorkflowIds:       $('input[name="copyObject.takeWorkflowIds"]'),
                $deleteWorkflowIds:     $('input[name="copyObject.deleteWorkflowIds"]')
            },

            init: function (elem) {
                let scc = JSPC.app.subCopyController

                scc.checkboxes.$takeDocIds.change( function(event) { scc.takeDocIds(this); } ).trigger('change')
                scc.checkboxes.$deleteDocIds.change( function(event) { scc.deleteDocIds(this); } ).trigger('change')
                scc.checkboxes.$takeAnnouncementIds.change( function(event) { scc.takeAnnouncementIds(this); } ).trigger('change')
                scc.checkboxes.$deleteAnnouncementIds.change( function(event) { scc.deleteAnnouncementIds(this); } ).trigger('change')
                scc.checkboxes.$takeTaskIds.change( function(event) { scc.takeTaskIds(this); } ).trigger('change')
                scc.checkboxes.$deleteTaskIds.change( function(event) { scc.deleteTaskIds(this); } ).trigger('change')
                scc.checkboxes.$takeWorkflowIds.change( function(event) { scc.takeWorkflowIds(this); } ).trigger('change')
                scc.checkboxes.$deleteWorkflowIds.change( function(event) { scc.deleteWorkflowIds(this); } ).trigger('change')
            },

            takeDocIds: function(elem) {
                JSPC.app.subCopyController._handleTake(elem, 'takeDocs', 'takeDocIds')
            },
            deleteDocIds: function(elem) {
                JSPC.app.subCopyController._handleDeleted(elem, 'takeDocs')
            },

            takeAnnouncementIds: function(elem) {
                JSPC.app.subCopyController._handleTake(elem, 'takeAnnouncements', 'takeAnnouncementIds')
            },
            deleteAnnouncementIds: function(elem) {
                JSPC.app.subCopyController._handleDeleted(elem, 'takeAnnouncements')
            },

            takeTaskIds: function(elem) {
                JSPC.app.subCopyController._handleTake(elem, 'takeTasks', 'takeTaskIds')
            },
            deleteTaskIds: function(elem) {
                JSPC.app.subCopyController._handleDeleted(elem, 'takeTasks')
            },

            takeWorkflowIds: function(elem) {
                JSPC.app.subCopyController._handleTake(elem, 'takeWorkflows', 'takeWorkflowIds')
            },
            deleteWorkflowIds: function(elem) {
                JSPC.app.subCopyController._handleDeleted(elem, 'takeWorkflows')
            },

            _handleTake: function(elem, identifier, counterId) {
                if (elem.checked) {
                    $('.table tr[data-element="copyObject.' + identifier + '"] td[data-element="source"] div.la-element[data-id="' + elem.value + '"]').addClass('willStay');
                    $('.table tr[data-element="copyObject.' + identifier + '"] td[data-element="target"] div.la-element').addClass('willStay');
                }
                else {
                    $('.table tr[data-element="copyObject.' + identifier + '"] td[data-element="source"] div.la-element[data-id="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.' + counterId) < 1){
                        $('.table tr[data-element="copyObject.' + identifier + '"] td[data-element="target"] div.la-element').removeClass('willStay');
                    }
                }
            },
            _handleDeleted: function(elem, identifier) {
                if (elem.checked) {
                    $('.table tr[data-element="copyObject.' + identifier + '"] td[data-element="target"] div.la-element[data-id="' + elem.value + '"]').addClass('willBeReplaced');
                }
                else {
                    $('.table tr[data-element="copyObject.' + identifier + '"] td[data-element="target"] div.la-element[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
                }
            },

            getNumberOfCheckedCheckboxes: function(inputElementName) {
                var checkboxes = document.querySelectorAll('input[name="' + inputElementName + '"]');
                var numberOfChecked = 0;
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked) {
                        numberOfChecked++;
                    }
                }
                return numberOfChecked;
            }
        }

        JSPC.app.subCopyController.init()
    </laser:script>
</g:if>
