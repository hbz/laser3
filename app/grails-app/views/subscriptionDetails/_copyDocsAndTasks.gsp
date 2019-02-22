<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<br>
<semui:form>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    <hr>
    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscription?.id]" method="post" class="ui form newLicence">
        <table class="ui celled table">
        <tbody>
        <tr>
            %{--<th>${message(code: 'default.select.label', default: 'Select')}</th>--}%
            <td>Quelle:
            <g:if test="${sourceSubscription}"><g:link controller="subscriptionDetails" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
            </td>
            <td>Ziel:
            <g:if test="${targetSubscription}"><g:link controller="subscriptionDetails" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
            </td>
        </tr>

        <tr><th><i class="file outline icon"></i>&nbsp${message(code: 'subscription.takeDocs')}</th></tr>
        <tr>
            <td>
                <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                        <p>
                            <g:checkBox name="subscription.takeDocs" value="${docctx.id}"  />&nbsp
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
                <g:each in="${targetSubscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
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
            </td>
        </tr>

        <tr><th><i class="sticky note outline icon"></i>&nbsp${message(code: 'subscription.takeAnnouncements')}</th></tr>

        <tr>
            <td>
                <g:each in="${sourceSubscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                        <p>
                            <g:checkBox name="subscription.takeAnnouncements" value="${docctx.id}"  />&nbsp
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
                <g:each in="${targetSubscription.documents.sort { it.owner?.title }}" var="docctx">
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
            </td>
        </tr>

        <tr><th><i class="checked calendar icon"></i>&nbsp${message(code: 'subscription.takeTasks')}</th></tr>

        <tr>
            <td>
                <p>
                    <g:each in="${sourceTasks}" var="tsk">
                        <p>
                            <g:checkBox name="subscription.takeTasks" value="${tsk.id}"  />&nbsp
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
                        <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)<br/>
                    </p>
                </g:each>
            </td>
        </tr>
        </tbody>
        </table>
        <input type="submit" class="ui button js-click-control" value="Ausgewählte Eigenschaften in Ziellizenz kopieren" />
    </g:form>
</semui:form>
