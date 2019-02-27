<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.copyElementsIntoSubscription.label')}</title>
</head>
<body>
<g:render template="breadcrumb" model="${[params: params]}"/>

<h1 class="ui left aligned icon header"><semui:headerIcon />
${message(code: 'subscription.details.copyElementsIntoSubscription.label')}
</h1>

<semui:messages data="${flash}"/>

%{--TODO wieder entfernen, nur fürs Testen gedacht--}%
<% workFlowPart ?: 2 %>
<% Map params = [id: params.id];
    if (sourceSubscriptionId) params << [sourceSubscriptionId: sourceSubscriptionId];
    if (targetSubscriptionId) params << [targetSubscriptionId: targetSubscriptionId];
%>
<div class="ui steps">
    <div class="${workFlowPart == '1' ? 'active' : ''} step">
        <div class="content">
            <div class="title">
                <g:link controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 1]}" message="myinst.copyElementsIntoSubscription" >
                    Auswahl Eigenschaften
                </g:link>
            </div>
            <div class="description">
                <i class="calendar alternate outline icon"></i>Datum
                <i class="university icon"></i>Organisationen
                <i class="newspaper icon"></i>Titel
            </div>
        </div>
    </div>
    <div class="${workFlowPart == '2' ? 'active' : ''} step" >
        <div class="content">
            <div class="title">
                <g:link controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 2]}" message="myinst.copyElementsIntoSubscription" >
                Weitere Lizenzeigenschaften
                </g:link>
            </div>
            <div class="description">
                <i class="file outline icon"></i>Dokumente
                <i class="sticky note outline icon"></i>Anmerkungen
                <i class="checked calendar icon"></i>Aufgaben
            </div>
        </div>
    </div>
    <div class="${workFlowPart == '3' ? 'active' : 'disabled'} step">
        <i class="university icon"></i>
        <div class="content">
            <div class="title">
                <g:link controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 3]}" message="myinst.copyElementsIntoSubscription" >
                    Teilnehmer
                </g:link>
            </div>
        </div>
    </div>
    <div class="${workFlowPart == '4' ? 'active' : ''} step">
        <i class="tags icon"></i>
        <div class="content">
            <div class="title">
                <g:link controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 4]}" message="myinst.copyElementsIntoSubscription" >
                    Merkmale
                </g:link>
            </div>
        </div>
    </div>
</div>
</tbody>
</table>
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == '1'}">
                <g:render template="copyElements" model="${[source_validSubChilds: source_validSubChilds, target_validSubChilds: target_validSubChilds]}"/>
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == '2'}">
                <g:render template="copyDocsAndTasks" model="${[validSubChilds: validSubChilds]}"/>


                %{--<g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}"--}%
                        %{--params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscription?.id]" method="post" class="ui form newLicence">--}%
                    %{--<table class="ui celled table">--}%
                        %{--<tbody>--}%
                            %{--<tr>--}%
                                %{--<th>${message(code: 'default.select.label', default: 'Select')}</th>--}%
                                %{--<th>${message(code: 'subscription.property', default: 'Subscription Properties')}</th>--}%
                                %{--<th>${message(code: 'default.value.label', default: 'Value')}</th>--}%
                            %{--</tr>--}%

                            %{--<g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">--}%
                                %{--<g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">--}%
                                    %{--<tr>--}%
                                        %{--<th><g:checkBox name="subscription.takeDocs" value="${docctx.id}" checked="${true}"/></th>--}%
                                        %{--<th>${message(code: 'subscription.takeDocs', default: 'Take Documents from Subscription')}</th>--}%
                                        %{--<td>--}%
                                            %{--<g:link controller="docstore" id="${docctx.owner.uuid}">--}%
                                                %{--<g:if test="${docctx.owner?.title}">--}%
                                                    %{--${docctx.owner.title}--}%
                                                %{--</g:if>--}%
                                                %{--<g:else>--}%
                                                    %{--<g:if test="${docctx.owner?.filename}">--}%
                                                        %{--${docctx.owner.filename}--}%
                                                    %{--</g:if>--}%
                                                    %{--<g:else>--}%
                                                        %{--${message(code: 'template.documents.missing', default: 'Missing title and filename')}--}%
                                                    %{--</g:else>--}%
                                                %{--</g:else>--}%
                                            %{--</g:link>(${docctx.owner.type.getI10n("value")})--}%
                                        %{--</td>--}%
                                    %{--</tr>--}%
                                %{--</g:if>--}%
                            %{--</g:each>--}%
                            %{--<tr></tr><tr></tr>--}%
                            %{--<g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">--}%
                                %{--<g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">--}%
                                    %{--<tr>--}%
                                        %{--<th><g:checkBox name="subscription.takeAnnouncements" value="${docctx.id}" checked="${true}"/></th>--}%
                                        %{--<th>${message(code: 'subscription.takeAnnouncements')}</th>--}%
                                        %{--<td>--}%
                                            %{--<g:if test="${docctx.owner.title}">--}%
                                                %{--<b>${docctx.owner.title}</b>--}%
                                            %{--</g:if>--}%
                                            %{--<g:else>--}%
                                                %{--<b>Ohne Titel</b>--}%
                                            %{--</g:else>--}%
                                            %{--(${message(code: 'template.notes.created')}--}%
                                            %{--<g:formatDate--}%
                                                    %{--format="${message(code: 'default.date.format.notime')}"--}%
                                                    %{--date="${docctx.owner.dateCreated}"/>)--}%
                                        %{--</td>--}%
                                    %{--</tr>--}%
                                %{--</g:if>--}%
                            %{--</g:each>--}%
                            %{--<tr></tr><tr></tr>--}%
                            %{--<g:each in="${tasks}" var="tsk">--}%
                                %{--<tr>--}%
                                    %{--<th><g:checkBox name="subscription.takeTasks" value="${tsk.id}" checked="${true}"/></th>--}%
                                    %{--<th>${message(code: 'subscription.takeTasks', default: 'Take Tasks from Subscription')}</th>--}%

                                    %{--<td>--}%
                                        %{--<b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}--}%
                                        %{--<g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tsk.endDate}"/>)--}%

                                    %{--</td>--}%
                                %{--</tr>--}%
                            %{--</g:each>--}%
                    %{--</tbody>--}%
                    %{--</table>--}%
                %{--</g:form>--}%
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == '3'}">
                <g:render template="copySubscriber" model="${[validSubChilds: validSubChilds]}"/>
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == '4'}">
                <g:render template="copyProperties" model="${[validSubChilds: validSubChilds]}"/>
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
</body>
</html>
