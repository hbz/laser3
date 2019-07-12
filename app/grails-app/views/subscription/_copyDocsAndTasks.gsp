<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Doc; com.k_int.kbplus.Subscription" %>
<laser:serviceInjection />
<semui:form>
    <g:set var="isInstAdm" value="${contextService.getUser().hasAffiliation("INST_ADM")}"/>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId, isRenewSub: isRenewSub]" method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-table">
            <thead>
            %{--DOCUMENTS:--}%
                <tr>
                    <th class="six wide">
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked />
                    </th>
                    <th class="six wide">
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <g:if test="${targetSubscription}">
                            <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
                        </g:if>
                    </th>
                </tr>
            </thead>
            <tbody class="top aligned">
                <tr>
                    <td  name="subscription.takeDocs.source">
                        <b><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}:</b><br />
                        <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
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
                                                    ${message(code: 'template.documents.missing', default: 'Missing title and filename')}
                                                </g:else>
                                            </g:else>
                                        </g:link>(${docctx.owner.type.getI10n("value")})
                                    </label>
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="subscription.takeDocIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                    <td  name="subscription.takeDocs.target">
                        <b><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}:</b><br />
                        <div>
                            <g:if test="${targetSubscription}">
                                <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                                    <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
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
                                                        ${message(code: 'template.documents.missing', default: 'Missing title and filename')}
                                                    </g:else>
                                                </g:else>
                                            </g:link>(${docctx.owner.type.getI10n("value")})
                                        </div>
                                    </g:if>
                                </g:each>
                            </g:if>
                        </div>
                    </td>
                    %{--DELETE:--}%
                    <td>
                        <g:each in="${targetSubscription?.documents?.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="subscription.deleteDocIds" value="${docctx?.id}" data-action="delete" checked="${false}"/>
                                </div>
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                </tr>

                %{--ANNOUNCEMENTS:--}%
                <tr>
                    <td name="subscription.takeAnnouncements.source">
                        <b><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}:</b><br />
                        <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
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
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                                %{--<div data-id="${docctx.id} " class="la-element">--}%
                                    %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="subscription.takeAnnouncementIds" value="${docctx.id}" data-action="copy" checked="${true}" />
                                </div>
                                    %{--</div>--}%
                                %{--</div>--}%
                            </g:if>
                        </g:each>
                    </td>
                    <td  name="subscription.takeAnnouncements.target">
                        <b><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}:</b><br />
                        <div>
                            <g:if test="${targetSubscription}">
                                <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
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
                                    </g:if>
                                </g:each>
                            </g:if>
                        </div>
                    </td>
                    %{--DELETE:--}%
                    <td>
                        <div>
                            <g:if test="${targetSubscription}">
                                <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                                        %{--<div class="ui checkbox">--}%
                                        <div class="ui checkbox la-toggle-radio la-noChange">
                                            <g:checkBox name="subscription.deleteAnnouncementIds" value="${docctx?.id}" data-action="delete"  checked="${false}"/>
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
                    <td name="subscription.takeTasks.source">
                        <b><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}:</b><br />
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
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="subscription.takeTaskIds" value="${tsk?.id}" data-action="copy"  />
                                </div>
                                %{--</div>--}%
                            </div>
                        </g:each>
                    </td>
                    <td  name="subscription.takeTasks.target">
                        <b><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}:</b><br />
                        <g:each in="${targetTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                            <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                            <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk?.endDate}"/>)
                            </div>
                        </g:each>
                    </td>
                    %{--DELETE:--}%
                    <td>
                        <g:each in="${targetTasks}" var="tsk">
                            <g:if test="${tsk.creator.id == userId || isInstAdm}">
                                %{--<div class="ui checkbox">--}%
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="subscription.deleteTaskIds" value="${tsk?.id}" data-action="delete"  checked="${false}" />
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
                message(code: 'subscription.details.copyElementsIntoSubscription.copyDocsAndTasks.button')}" />
        <div class="sixteen wide field" style="text-align: right;">
            <g:set var="submitDisabled" value="${(sourceSubscription && targetSubscription)? '' : 'disabled'}"/>
            <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()"  ${submitDisabled}/>
        </div>
    </g:form>
</semui:form>
<r:script>

    var subCopyController = {

        checkboxes : {
            $takeDocIds: $('input[name="subscription.takeDocIds"]'),
            $deleteDocIds: $('input[name="subscription.deleteDocIds"]'),
            $takeAnnouncementIds: $('input[name="subscription.takeAnnouncementIds"]'),
            $deleteAnnouncementIds: $('input[name="subscription.deleteAnnouncementIds"]'),
            $takeTaskIds: $('input[name="subscription.takeTaskIds"]'),
            $deleteTaskIds: $('input[name="subscription.deleteTaskIds"]')
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
                $('.table tr td[name="subscription.takeDocs.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeDocs.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeDocs.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeDocIds') < 1) {
                    $('.table tr td[name="subscription.takeDocs.target"] div').removeClass('willStay');
                }
            }
        },

        deleteDocIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeDocs.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeDocs.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        takeAnnouncementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeAnnouncements.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeAnnouncementIds') < 1) {
                    $('.table tr td[name="subscription.takeAnnouncements.target"] div').removeClass('willStay');
                }
            }
        },

        deleteAnnouncementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        takeTaskIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeTasks.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeTasks.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeTasks.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeTaskIds') < 1){
                    $('.table tr td[name="subscription.takeTasks.target"] div').removeClass('willStay');
                }
            }
        },

        deleteTaskIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeTasks.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeTasks.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
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
