<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Doc; com.k_int.kbplus.Subscription" %>
<%@ page import="static com.k_int.kbplus.SubscriptionDetailsController.COPY" %>
<%@ page import="static com.k_int.kbplus.SubscriptionDetailsController.REPLACE" %>
<%@ page import="static com.k_int.kbplus.SubscriptionDetailsController.DO_NOTHING" %>
<semui:form>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    <hr>
    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id ?: params.sourceSubscriptionId}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId]" method="post" class="ui form newLicence">
        <table class="ui celled table">
        <tbody>
        <tr>
            <th class="center aligned">${message(code: 'default.copy.label')}</th>
            <th class="center aligned">${message(code: 'default.replace.label')}</th>
            <th class="center aligned">${message(code: 'default.doNothing.label')}</th>
            <td><b>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}:</b>
            <g:if test="${sourceSubscription}"><g:link controller="subscriptionDetails" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
            </td>
            <td><b>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}:</b>
            <g:if test="${targetSubscription}"><g:link controller="subscriptionDetails" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
            </td>
        </tr>

        <tr><th><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}</th></tr>
        <tr>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeDocs" value="${COPY}" /></div></td>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeDocs" value="${REPLACE}" /></div></td>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeDocs" value="${DO_NOTHING}" checked /></div></td>
            <td>
                <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
                        <p>
                            <g:checkBox name="subscription.takeDocIds" value="${docctx.id}" checked="${false}" />&nbsp
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
                        </p>
                    </g:if>
                </g:each>
            </td>
            <td>
                <g:if test="${targetSubscription}">
                    <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                        <g:if test="${(((docctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (docctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (docctx.status?.value != 'Deleted'))}">
                            <p>
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
                            </p>
                        </g:if>
                    </g:each>
                </g:if>
            </td>
        </tr>

        <tr><th><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}</th></tr>

        <tr>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeAnnouncements" value="${COPY}" /></div></td>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeAnnouncements" value="${REPLACE}" /></div></td>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeAnnouncements" value="${DO_NOTHING}" checked /></div></td>
            <td>
                <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                        <p>
                            <g:checkBox name="subscription.takeAnnouncementIds" value="${docctx.id}" checked="${false}" />&nbsp
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
                        </p>
                    </g:if>
                </g:each>
            </td>
            <td>
                <g:if test="${targetSubscription}">
                    <g:each in="${targetSubscription?.documents.sort { it.owner?.title }}" var="docctx">
                        <g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                            <p>
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
                                <br/>
                            </p>
                        </g:if>
                    </g:each>
                </g:if>
            </td>
        </tr>

        <tr><th><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}</th></tr>

        <tr>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeTasks" value="${COPY}" /></div></td>
            <td class="center aligned" style="vertical-align: top"></td>
            <td class="center aligned" style="vertical-align: top"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeTasks" value="${DO_NOTHING}" checked /></div></td>
            <td>
                <p>
                    <g:each in="${sourceTasks}" var="tsk">
                        <p>
                            <g:checkBox name="subscription.takeTaskIds" value="${tsk?.id}" checked="${false}" />&nbsp
                            <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                            <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)<br/>
                        </p>
                    </g:each>
                </p>
            </td>
            <td>
                <g:each in="${targetTasks}" var="tsk">
                    <p>
                        <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk?.endDate}"/>)<br/>
                    </p>
                </g:each>
            </td>
        </tr>
        </tbody>
        </table>
        <input type="submit" class="ui button js-click-control" value="Ausgewählte Elemente in Ziellizenz kopieren" />
    </g:form>
</semui:form>
