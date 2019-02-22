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
            <th>${message(code: 'default.select.label', default: 'Select')}</th>
            <td>Quelle:
            <g:if test="${sourceSubscription}"><g:link controller="subscriptionDetails" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
            </td>
            <td>Ziel:
            <g:if test="${targetSubscription}"><g:link controller="subscriptionDetails" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
            </td>
        </tr>

        <g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">
            <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                <tr>
                    <th><g:checkBox name="subscription.takeDocs" value="${docctx.id}" checked="${true}"/></th>
                    <td>
                        <i class="file outline icon"></i>
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
                    </td>
                </tr>
            </g:if>
        </g:each>

        <g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">
            <g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                <tr>
                    <th><g:checkBox name="subscription.takeAnnouncements" value="${docctx.id}" checked="${true}"/></th>
                    <td>
                        <i class="sticky note outline icon"></i>
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
                    </td>
                </tr>
            </g:if>
        </g:each>

        <g:each in="${tasks}" var="tsk">
            <tr>
                <th><g:checkBox name="subscription.takeTasks" value="${tsk.id}" checked="${true}"/></th>
                <td>
                    <i class="checked calendar icon"></i>
                    <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)

                </td>
            </tr>
        </g:each>
        </tbody>
        <input type="submit" class="ui button js-click-control"
               value="Ausgewählte Eigenschaften kopieren/überschreiben" />
    </g:form>
</semui:form>
