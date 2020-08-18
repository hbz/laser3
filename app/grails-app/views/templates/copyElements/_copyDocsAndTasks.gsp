<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Doc; com.k_int.kbplus.Subscription; de.laser.FormService;" %>
<laser:serviceInjection />

<g:set var="formService" bean="formService"/>

<semui:form>
    <g:set var="isInstAdm" value="${contextService.getUser().hasAffiliation("INST_ADM")}"/>

    <g:if test="${!fromSurvey}">
        <g:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject: sourceObject,
                targetObject: targetObject,
                allObjects_readRights: allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form action="${actionName}" controller="${controllerName}" id="${params.id ?: params.sourceObjectId}"
            params="[workFlowPart: workFlowPart, sourceObjectId: GenericOIDService.getOID(sourceObject), targetObjectId: GenericOIDService.getOID(targetObjectId), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <table class="ui celled table table-tworow la-table">
            <thead>
            %{--DOCUMENTS:--}%
                <tr>
                    <th class="six wide">
                        <g:if test="${sourceObject}"><g:link controller="subscription" action="show" id="${sourceObject?.id}">${sourceObject?.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked />
                    </th>
                    <th class="six wide">
                        <g:if test="${targetObject}"><g:link controller="subscription" action="show" id="${targetObject?.id}">${targetObject?.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <g:if test="${targetObject}">
                            <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
                        </g:if>
                    </th>
                </tr>
            </thead>
            <tbody class="top aligned">
                <tr>
                    <td  name="object.takeDocs.source">
                        <b><i class="file outline icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeDocs")}:</b><br />
                        <g:each in="${sourceObject.documents.sort { it.owner?.title?.toLowerCase()}}" var="docctx">
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
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
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="object.takeDocIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                    <td  name="object.takeDocs.target">
                        <b><i class="file outline icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeDocs")}:</b><br />
                        <div>
                            <g:if test="${targetObject}">
                                <g:each in="${targetObject?.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                    <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
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
                                    </g:if>
                                </g:each>
                            </g:if>
                        </div>
                    </td>
                    %{--DELETE:--}%
                    <td>
                        <br />
                        <g:each in="${targetObject?.documents?.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="object.deleteDocIds" value="${docctx?.id}" data-action="delete" checked="${false}"/>
                                </div>
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                </tr>

                %{--ANNOUNCEMENTS:--}%
                <tr>
                    <td name="object.takeAnnouncements.source">
                        <b><i class="sticky note outline icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeAnnouncements")}:</b><br />
                        <g:each in="${sourceObject.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
                                <div data-id="${docctx.id}" class="la-element">
                                    <label>
                                        <g:if test="${docctx.owner.title}">
                                            <b>${docctx.owner.title}</b>
                                        </g:if>
                                        <g:else>
                                            <b>Ohne Titel</b>
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
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
                                %{--<div data-id="${docctx.id} " class="la-element">--}%
                                    %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="object.takeAnnouncementIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                    %{--</div>--}%
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                    <td  name="object.takeAnnouncements.target">
                        <b><i class="sticky note outline icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeAnnouncements")}:</b><br />
                        <div>
                            <g:if test="${targetObject}">
                                <g:each in="${targetObject?.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
                                        <div data-id="${docctx.id}" class="la-element">
                                            <g:if test="${docctx.owner.title}">
                                                <b>${docctx.owner.title}</b>
                                            </g:if>
                                            <g:else>
                                                <b>Ohne Titel</b>
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
                                <g:each in="${targetObject?.documents.sort { it.owner?.title?.toLowerCase() }}" var="docctx">
                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') && docctx.owner?.owner?.id == contextService.org.id)}">
                                        %{--<div class="ui checkbox">--}%
                                        <div class="ui checkbox la-toggle-radio la-noChange">
                                            <g:checkBox name="object.deleteAnnouncementIds" value="${docctx?.id}" data-action="delete"  checked="${false}"/>
                                        </div>
                                        %{--</div>--}%
                                    </g:if>
                                </g:each>
                            </g:if>
                        </div>
                    </td>
                </tr>

                %{--TASKS:--}%
                <tr>
                    <td name="object.takeTasks.source">
                        <b><i class="checked calendar icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeTasks")}:</b><br />
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                                <label>
                                    <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
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
                                        <g:checkBox name="object.takeTaskIds" value="${tsk?.id}" data-action="copy"  />
                                </div>
                                %{--</div>--}%
                            </div>
                        </g:each>
                    </td>
                    <td  name="object.takeTasks.target">
                        <b><i class="checked calendar icon"></i>&nbsp${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.takeTasks")}:</b><br />
                        <g:each in="${targetTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                            <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
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
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="object.deleteTaskIds" value="${tsk?.id}" data-action="delete"  checked="${false}" />
                                </div>
                                %{--</div>--}%
                            </g:if>
                            <g:else><br></g:else>
                        </g:each>
                    </td>
                </tr>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'copyElementsIntoObject.copyDocsAndTasks.button')}" />

        <g:if test="${!fromSurvey}">
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceObject && targetObject)? '' : 'disabled'}"/>
                <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()"  ${submitDisabled}/>
            </div>
        </g:if>
        <g:else>
            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(Subscription.get(sourceObjectId), true)}" />
                    <g:link controller="survey" action="renewalWithSurvey" id="${surveyConfig?.surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]" class="ui button js-click-control">
                        <g:message code="renewalWithSurvey.back"/>
                    </g:link>
                </div>
                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled" value="${(sourceObject && targetObject)? '' : 'disabled'}"/>
                    <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()"  ${submitDisabled}/>
                </div>
            </div>
        </g:else>
    </g:form>
</semui:form>
<r:script>

    var subCopyController = {

        checkboxes : {
            $takeDocIds: $('input[name="object.takeDocIds"]'),
            $deleteDocIds: $('input[name="object.deleteDocIds"]'),
            $takeAnnouncementIds: $('input[name="object.takeAnnouncementIds"]'),
            $deleteAnnouncementIds: $('input[name="object.deleteAnnouncementIds"]'),
            $takeTaskIds: $('input[name="object.takeTaskIds"]'),
            $deleteTaskIds: $('input[name="object.deleteTaskIds"]')
        },

        init: function (elem) {
            var ref = subCopyController.checkboxes

            ref.$takeDocIds.change( function(event) {
                subCopyController.takeDocIds(this);
            }).trigger('change')

            ref.$deleteDocIds.change( function(event) {
                subCopyController.deleteDocIds(this);
            }).trigger('change')

            ref.$takeAnnouncementIds.change( function(event) {
                subCopyController.takeAnnouncementIds(this);
            }).trigger('change')

            ref.$deleteAnnouncementIds.change( function(event) {
                subCopyController.deleteAnnouncementIds(this);
            }).trigger('change')

            ref.$takeTaskIds.change( function(event) {
                subCopyController.takeTaskIds(this);
            }).trigger('change')

            ref.$deleteTaskIds.change( function(event) {
                subCopyController.deleteTaskIds(this);
            }).trigger('change')
        },

        takeDocIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeDocs.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="object.takeDocs.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="object.takeDocs.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('object.takeDocIds') < 1) {
                    $('.table tr td[name="object.takeDocs.target"] div').removeClass('willStay');
                }
            }
        },

        deleteDocIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeDocs.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="object.takeDocs.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        takeAnnouncementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="object.takeAnnouncements.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="object.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('object.takeAnnouncementIds') < 1) {
                    $('.table tr td[name="object.takeAnnouncements.target"] div').removeClass('willStay');
                }
            }
        },

        deleteAnnouncementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="object.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        takeTaskIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeTasks.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="object.takeTasks.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="object.takeTasks.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('object.takeTaskIds') < 1){
                    $('.table tr td[name="object.takeTasks.target"] div').removeClass('willStay');
                }
            }
        },

        deleteTaskIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeTasks.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="object.takeTasks.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
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

    subCopyController.init()
</r:script>
