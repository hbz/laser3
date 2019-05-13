<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Doc; com.k_int.kbplus.Subscription" %>
<%@ page import="static com.k_int.kbplus.SubscriptionController.COPY" %>
<%@ page import="static com.k_int.kbplus.SubscriptionController.REPLACE" %>
<%@ page import="static com.k_int.kbplus.SubscriptionController.DO_NOTHING" %>
<semui:form>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    <hr>
    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId]" method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-table">
            <thead>
                <tr>
                    <th class="center aligned">${message(code: 'default.copy.label')}</th>
                    <th class="center aligned">${message(code: 'default.replace.label')}</th>
                    <th class="center aligned">${message(code: 'default.doNothing.label')}</th>
                    <th class="six wide">
                    <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                    </th>
                    <th class="six wide">
                    <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                    </th>
                </tr>
            </thead>
            <tbody class="top aligned">
                <tr><td colspan="5"></td></tr>
                %{--<tr><th colspan="5"><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}</th></tr>--}%
                <tr>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeDocs" value="${COPY}" /></div></td>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeDocs" value="${REPLACE}" /></div></td>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeDocs" value="${DO_NOTHING}" checked /></div></td>
                    <td  name="subscription.takeDocs.source">
                        <b><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}:</b><br />
                        <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
                                <div data-id="${docctx.id}" class="la-element">
                                    <div class="ui checkbox">
                                        <g:checkBox name="subscription.takeDocIds" value="${docctx.id}" checked="${false}" />
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
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    <td  name="subscription.takeDocs.target">
                        <b><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}:</b><br />
                        <div>
                            <g:if test="${targetSubscription}">
                                <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                                    <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
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
                                        <br />
                                    </g:if>
                                </g:each>
                            </g:if>
                        </div>
                    </td>
                </tr>

                %{--<tr><th colspan="5"><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}</th></tr>--}%

                <tr>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeAnnouncements" value="${COPY}" /></div></td>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeAnnouncements" value="${REPLACE}" /></div></td>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeAnnouncements" value="${DO_NOTHING}" checked /></div></td>
                    <td  name="subscription.takeAnnouncements.source">
                        <b><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}:</b><br />
                        <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                                <div data-id="${docctx.id} " class="la-element">
                                    <div class="ui checkbox">

                                        <g:checkBox name="subscription.takeAnnouncementIds" value="${docctx.id}" checked="${false}" />
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
                                </div>
                            </g:if>
                        </g:each>
                    </td>
                    <td  name="subscription.takeAnnouncements.target">
                        <b><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}:</b><br />
                        <div>
                            <g:if test="${targetSubscription}">
                                <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                                    <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
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
                                        <br />
                                    </g:if>
                                </g:each>
                            </g:if>
                        </div>
                    </td>
                </tr>

                %{--<tr><th colspan="5"><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}</th></tr>--}%

                <tr>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeTasks" value="${COPY}" /></div></td>
                    <td class="center aligned"></td>
                    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeTasks" value="${DO_NOTHING}" checked /></div></td>
                    <td  name="subscription.takeTasks.source">
                        <b><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}:</b><br />
                        <g:each in="${sourceTasks}" var="tsk">
                            <div data-id="${tsk?.id}" class="la-element">
                                <div class="ui checkbox">
                                    <g:checkBox name="subscription.takeTaskIds" value="${tsk?.id}" checked="${false}" />
                                    <label>
                                        <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)
                                    </label>
                                </div>
                            </div>
                        </g:each>
                    </td>
                    <td  name="subscription.takeTasks.target">
                        <b><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}:</b><br />
                        <div>
                            <g:each in="${targetTasks}" var="tsk">
                                <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                                <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk?.endDate}"/>)
                                <br />
                            </g:each>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
        <div class="sixteen wide field" style="text-align: right;">
            <input type="submit" class="ui button js-click-control" value="${message(code: 'subscription.details.copyElementsIntoSubscription.copyDocsAndTasks.button')}" onclick="return jsConfirmation() "/>
        </div>
    </g:form>
</semui:form>
<r:script>
    $('input:radio[name="subscription.takeDocs"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takeDocs.target"] div').addClass('willStay');
            $('.table tr td[name="subscription.takeDocs.target"] div').removeClass('willBeReplaced');
        }
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeDocs.target"] div').addClass('willBeReplaced');
            $('.table tr td[name="subscription.takeDocs.target"] div').removeClass('willStay');
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takeDocs.source"] div').removeClass('willStay');
            $('.table tr td[name="subscription.takeDocs.target"] div').removeClass('willStay');
            $('.table tr td[name="subscription.takeDocs.target"] div').removeClass('willBeReplaced');
            $('.table tr input[name="subscription.takeDocIds"]').prop("checked", false);
        }
    })

    $('input[name="subscription.takeDocIds"]').change( function(event) {
        var id = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takeDocs.source"] div[data-id="' + id + '"]').addClass('willStay');
        } else {
            $('.table tr td[name="subscription.takeDocs.source"] div[data-id="' + id + '"]').removeClass('willStay');
        }
    })

    $('input:radio[name="subscription.takeAnnouncements"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takeAnnouncements.target"] div').addClass('willStay');
            $('.table tr td[name="subscription.takeAnnouncements.target"] div').removeClass('willBeReplaced');
        }
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeAnnouncements.target"] div').addClass('willBeReplaced');
            $('.table tr td[name="subscription.takeAnnouncements.target"] div').removeClass('willStay');
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takeAnnouncements.source"] div').removeClass('willStay');
            $('.table tr td[name="subscription.takeAnnouncements.target"] div').removeClass('willStay');
            $('.table tr td[name="subscription.takeAnnouncements.target"] div').removeClass('willBeReplaced');
            $('.table tr input[name="subscription.takeAnnouncementIds"]').prop("checked", false);
        }
    })

    $('input[name="subscription.takeAnnouncementIds"]').change( function(event) {
        var id = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takeAnnouncements.source"] div[data-id="' + id + '"]').addClass('willStay');
        } else {
            $('.table tr td[name="subscription.takeAnnouncements.source"] div[data-id="' + id + '"]').removeClass('willStay');
        }
    })

    $('input:radio[name="subscription.takeTasks"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takeTasks.target"] div').addClass('willStay');
            $('.table tr td[name="subscription.takeTasks.target"] div').removeClass('willBeReplaced');
        }
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeTasks.target"] div').addClass('willBeReplaced');
            $('.table tr td[name="subscription.takeTasks.target"] div').removeClass('willStay');
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takeTasks.source"] div').removeClass('willStay');
            $('.table tr td[name="subscription.takeTasks.target"] div').removeClass('willStay');
            $('.table tr td[name="subscription.takeTasks.target"] div').removeClass('willBeReplaced');
            $('.table tr input[name="subscription.takeTaskIds"]').prop("checked", false);
        }
    })

    $('input[name="subscription.takeTaskIds"]').change( function(event) {
        var id = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takeTasks.source"] div[data-id="' + id + '"]').addClass('willStay');
        } else {
            $('.table tr td[name="subscription.takeTasks.source"] div[data-id="' + id + '"]').removeClass('willStay');
        }
    })

</r:script>
