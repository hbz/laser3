<%@ page import="de.laser.properties.PropertyDefinition; de.laser.SurveyConfig; de.laser.helper.RDStore; de.laser.Person; de.laser.Doc; de.laser.Subscription; de.laser.FormService" %>
<laser:serviceInjection />

<g:set var="formService" bean="formService"/>

<semui:form>
    <g:set var="isInstAdm" value="${contextService.getUser().hasAffiliation("INST_ADM")}"/>

    <g:if test="${!fromSurvey && !copyObject}">
        <g:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject: sourceObject,
                targetObject: targetObject,
                allObjects_readRights: allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form action="${actionName}" controller="${controllerName}" id="${params.id ?: params.sourceObjectId}" data-confirm-id="copyElements_form"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey, copyObject: copyObject]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <table class="ui celled table table-tworow la-js-responsive-table la-table">
            <thead>
            %{--DOCUMENTS:--}%
                <tr>
                    <th class="six wide">
                        <g:if test="${sourceObject}"><g:link controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="show" id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned" data-label="${message(code:'responsive.table.selectElement')}">
                        <input type="checkbox"  data-action="copy" onClick="JSPC.app.toggleAllCheckboxes(this)" checked />
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
                <tr>
                    <td  name="copyObject.takeDocs.source">
                        <strong><i class="file outline icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeDocs")}:</strong><br />
                        <g:each in="${sourceObject.documents.sort { it.owner?.title?.toLowerCase()}}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                <div data-id="${docctx.id}" class="la-element">
                                    <label>
                                        <g:link controller="docstore" id="${docctx.owner.uuid}">
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
                                        </g:link>(${docctx.owner.type.getI10n("value")})
                                    </label>
                                    <g:if test="${isConsortialObjects}">
                                        <div class="right aligned wide column">
                                            <g:if test="${docctx.isShared}">
                                                <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                <i class="la-share icon la-js-editmode-icon"></i>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                <i class="la-share slash icon la-js-editmode-icon"></i>
                                                </span>
                                            </g:else>

                                        </div>
                                    </g:if>
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <br />
                        <g:each in="${sourceObject.documents.sort { it.owner?.title?.toLowerCase()}}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="copyObject.takeDocIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                %{--</div>--}%
                                <br />
                            </g:if>
                        </g:each>
                    </td>
                    <g:if test="${!copyObject && targetObject}">
                        <td  name="copyObject.takeDocs.target">
                            <strong><i class="file outline icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeDocs")}:</strong><br />
                            <div>
                                <g:if test="${targetObject}">
                                    <g:each in="${targetObject.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                        <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                            <div data-id="${docctx.id}" class="la-element">
                                                <g:link controller="docstore" id="${docctx.owner.uuid}">
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
                                                </g:link>(${docctx.owner.type.getI10n("value")})
                                            </div>
                                            <g:if test="${isConsortialObjects}">
                                                <div class="right aligned wide column">
                                                    <g:if test="${docctx.isShared}">
                                                        <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                            <i class="la-share icon la-js-editmode-icon"></i>
                                                        </span>
                                                    </g:if>
                                                    <g:else>
                                                        <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                            <i class="la-share slash icon la-js-editmode-icon"></i>
                                                        </span>
                                                    </g:else>

                                                </div>
                                            </g:if>
                                            <br />
                                        </g:if>
                                    </g:each>
                                </g:if>
                            </div>
                        </td>
                        %{--DELETE:--}%
                        <td>
                            <br />
                            <g:each in="${targetObject.documents?.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (docctx.status?.value != 'Deleted') && (docctx.owner?.owner?.id == contextService.getOrg().id))}">
                                    %{--<div class="ui checkbox">--}%
                                    <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                        <g:checkBox name="copyObject.deleteDocIds" value="${docctx?.id}" data-action="delete" checked="${false}"/>
                                    </div>
                                    %{--</div>--}%
                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                </tr>

                %{--ANNOUNCEMENTS:--}%
                <tr>
                    <td name="copyObject.takeAnnouncements.source">
                        <strong><i class="sticky note outline icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeAnnouncements")}:</strong><br />
                        <g:each in="${sourceObject.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id)}">
                                <div data-id="${docctx.id}" class="la-element">
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

                                    <g:if test="${isConsortialObjects}">
                                        <div class="right aligned wide column">
                                            <g:if test="${docctx.isShared}">
                                                <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                    <i class="la-share icon la-js-editmode-icon"></i>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                    <i class="la-share slash icon la-js-editmode-icon"></i>
                                                </span>
                                            </g:else>

                                        </div>
                                    </g:if>
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    %{--COPY:--}%
                    <td class="center aligned">
                    <br />
                        <g:each in="${sourceObject.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id)}">
                                %{--<div data-id="${docctx.id} " class="la-element">--}%
                                    %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="copyObject.takeAnnouncementIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                    %{--</div>--}%
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                        <g:if test="${!copyObject && targetObject}">
                                    <td  name="copyObject.takeAnnouncements.target">
                                        <strong><i class="sticky note outline icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeAnnouncements")}:</strong><br />
                                        <div>
                                            <g:if test="${targetObject}">
                                                <g:each in="${targetObject.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id)}">
                                                        <div data-id="${docctx.id}" class="la-element">
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
                                                        </div>
                                                        <g:if test="${isConsortialObjects}">
                                                            <div class="right aligned wide column">
                                                                <g:if test="${docctx.isShared}">
                                                                    <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                                        <i class="la-share icon la-js-editmode-icon"></i>
                                                                    </span>
                                                                </g:if>
                                                                <g:else>
                                                                    <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                                                    </span>
                                                                </g:else>

                                                            </div>
                                                        </g:if>
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                        </div>
                                    </td>
                                    %{--DELETE:--}%
                                    <td>
                                    <br />
                                        <div>
                                            <g:if test="${targetObject}">
                                                <g:each in="${targetObject.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.getOrg().id)}">
                                                        %{--<div class="ui checkbox">--}%
                                                        <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                                            <g:checkBox name="copyObject.deleteAnnouncementIds" value="${docctx?.id}" data-action="delete"  checked="${false}"/>
                                                        </div>
                                                        %{--</div>--}%
                                                    </g:if>
                                                </g:each>
                                            </g:if>
                                        </div>
                                    </td>
                        </g:if>
                </tr>

                %{--TASKS:--}%
                <tr>
                    <td name="copyObject.takeTasks.source">
                        <strong><i class="checked calendar icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.takeTasks")}:</strong><br />
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                                <label>
                                    <strong>${tsk?.title}</strong> (${message(code: 'task.endDate.label')}
                                    <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)
                                </label>
                            </div>
                        </g:each>
                    </td>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <br />
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="copyObject.takeTaskIds" value="${tsk?.id}" data-action="copy"  />
                                </div>
                                %{--</div>--}%
                            </div>
                        </g:each>
                    </td>
                    <g:if test="${!copyObject && targetObject}">
                                <td  name="copyObject.takeTasks.target">
                                    <strong><i class="checked calendar icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeTasks")}:</strong><br />
                                    <g:each in="${targetTasks}" var="tsk">
                                        <div data-id="${tsk?.id}" class="la-element">
                                        <strong>${tsk?.title}</strong> (${message(code: 'task.endDate.label')}
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk?.endDate}"/>)
                                        </div>
                                    </g:each>
                                </td>
                                %{--DELETE:--}%
                                <td>
                                    <br />
                                    <g:each in="${targetTasks}" var="tsk">
                                        <g:if test="${tsk.creator.id == userId || isInstAdm}">
                                            %{--<div class="ui checkbox">--}%
                                            <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                                <g:checkBox name="copyObject.deleteTaskIds" value="${tsk?.id}" data-action="delete"  checked="${false}" />
                                            </div>
                                            %{--</div>--}%
                                        </g:if>
                                        <g:else><br /></g:else>
                                    </g:each>
                                </td>
                    </g:if>
                </tr>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'copyElementsIntoObject.copyDocsAndTasks.button')}" />

        <g:if test="${!fromSurvey && !copyObject}">
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceObject && targetObject)? '' : 'disabled'}"/>
                <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}" data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete" ${submitDisabled}/>
            </div>
        </g:if>
        <g:elseif test="${copyObject}">
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.copy.label')}"/>
            </div>
        </g:elseif>
        <g:else>
            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <g:set var="surveyConfig" value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(genericOIDService.resolveOID(sourceObjectId), true)}" />
                    <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig?.surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]" class="ui button js-click-control">
                        <g:message code="renewalEvaluation.back"/>
                    </g:link>
                </div>
                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled" value="${(sourceObject && targetObject)? '' : 'disabled'}"/>
                    <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}" data-confirm-id="copyElements"
                           data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                           data-confirm-term-how="delete" ${submitDisabled}/>
                </div>
            </div>
        </g:else>
    </g:form>
</semui:form>
<g:if test="${!copyObject}">
    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.subCopyController = {

            checkboxes : {
                $takeDocIds: $('input[name="copyObject.takeDocIds"]'),
                $deleteDocIds: $('input[name="copyObject.deleteDocIds"]'),
                $takeAnnouncementIds: $('input[name="copyObject.takeAnnouncementIds"]'),
                $deleteAnnouncementIds: $('input[name="copyObject.deleteAnnouncementIds"]'),
                $takeTaskIds: $('input[name="copyObject.takeTaskIds"]'),
                $deleteTaskIds: $('input[name="copyObject.deleteTaskIds"]')
            },

            init: function (elem) {
                var ref = JSPC.app.subCopyController.checkboxes

                ref.$takeDocIds.change( function(event) {
                    JSPC.app.subCopyController.takeDocIds(this);
                }).trigger('change')

                ref.$deleteDocIds.change( function(event) {
                    JSPC.app.subCopyController.deleteDocIds(this);
                }).trigger('change')

                ref.$takeAnnouncementIds.change( function(event) {
                    JSPC.app.subCopyController.takeAnnouncementIds(this);
                }).trigger('change')

                ref.$deleteAnnouncementIds.change( function(event) {
                    JSPC.app.subCopyController.deleteAnnouncementIds(this);
                }).trigger('change')

                ref.$takeTaskIds.change( function(event) {
                    JSPC.app.subCopyController.takeTaskIds(this);
                }).trigger('change')

                ref.$deleteTaskIds.change( function(event) {
                    JSPC.app.subCopyController.deleteTaskIds(this);
                }).trigger('change')
            },

            takeDocIds: function(elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeDocs.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeDocs.target"] div').addClass('willStay');
                }
                else {
                    $('.table tr td[name="copyObject.takeDocs.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeDocIds') < 1) {
                        $('.table tr td[name="copyObject.takeDocs.target"] div').removeClass('willStay');
                    }
                }
            },

            deleteDocIds: function(elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeDocs.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
                }
                else {
                    $('.table tr td[name="copyObject.takeDocs.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
                }
            },

            takeAnnouncementIds: function(elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeAnnouncements.target"] div').addClass('willStay');
                }
                else {
                    $('.table tr td[name="copyObject.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeAnnouncementIds') < 1) {
                        $('.table tr td[name="copyObject.takeAnnouncements.target"] div').removeClass('willStay');
                    }
                }
            },

            deleteAnnouncementIds: function(elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
                }
                else {
                    $('.table tr td[name="copyObject.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
                }
            },

            takeTaskIds: function(elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeTasks.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeTasks.target"] div').addClass('willStay');
                }
                else {
                    $('.table tr td[name="copyObject.takeTasks.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeTaskIds') < 1){
                        $('.table tr td[name="copyObject.takeTasks.target"] div').removeClass('willStay');
                    }
                }
            },

            deleteTaskIds: function(elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeTasks.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
                }
                else {
                    $('.table tr td[name="copyObject.takeTasks.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
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
