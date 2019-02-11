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
${message(code: 'subscription.details.copyElementsIntoSubscription.label', args: [subscription?.name])}
</h1>

<semui:messages data="${flash}"/>
%{--TODO wieder entfernen, ist nur für die Entwicklung--}%
<%workFlowPart = 1%>

<div class="ui tablet stackable steps">
    <div class="${workFlowPart == 1 ? 'active' : 'disabled'} step">
        <div class="content">
            <div class="title">Auswahl Eigenschaften</div>
            <div class="description">
                <i class="calendar alternate outline icon"></i>Datum
                <i class="tags icon"></i>Merkmale
                <i class="university icon"></i>Organisationen
                <i class="newspaper icon"></i>Titel
            </div>
        </div>
    </div>
    <div class="${workFlowPart == 2 ? 'active' : 'disabled'} step">
        <div class="content">
            <div class="title">Weitere Lizenzeigenschaften</div>
            <div class="description">
                <i class="checked calendar icon"></i>Aufgaben
                <i class="file outline icon"></i>Dokumente
                <i class="sticky note outline icon"></i>Anmerkungen
            </div>
        </div>
    </div>
    <div class="${workFlowPart == 3 ? 'active' : 'disabled'} step">
        <div class="content">
            <div class="title">Ausahl Teilnehmer</div>
            <div class="description">
                <i class="university circle icon"></i>Teilnehmer
            </div>
        </div>
    </div>
</div>

%{--<semui:form>--}%
    %{--<g:form action="renewSubscriptionConsortia" controller="subscriptionDetails" id="${params.id}"--}%
            %{--params="[workFlowPart: workFlowPart]" method="post" class="ui form newLicence">--}%
        <g:hiddenField name="baseSubscription" value="${params.id}"/>
        <g:hiddenField name="workFlowPartNext" value="${workFlowPartNext}"/>

            %{--<g:if test="${workFlowPart == 2}">--}%
                %{--<br><b>${message(code: 'subscription.renewSubscriptionConsortia.success', default: 'The license has been renewed. You can now apply more license properties to the extended license from the old license.')}</b><br>--}%
            %{--</g:if>--}%
        %{--</div>--}%
        %{--<table class="ui celled table">--}%
            %{--<tbody>--}%
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == 1}">
                <g:render template="copyEigenschaften" model="${[validSubChilds: validSubChilds]}"/>
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
            %{--<g:if test="${workFlowPart == 2}">--}%
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
                            %{--<th><g:checkBox name="subscription.takeAnnouncements" value="${docctx.id}"--}%
                                            %{--checked="${true}"/></th>--}%
                            %{--<th>${message(code: 'subscription.takeAnnouncements', default: 'Take Notes from Subscription')}</th>--}%
                            %{--<td>--}%
                                %{--<g:if test="${docctx.owner.title}">--}%
                                    %{--<b>${docctx.owner.title}</b>--}%
                                %{--</g:if>--}%
                                %{--<g:else>--}%
                                    %{--<b>Ohne Titel</b>--}%
                                %{--</g:else>--}%

                                %{--(${message(code: 'template.notes.created')}--}%
                                %{--<g:formatDate--}%
                                        %{--format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"--}%
                                        %{--date="${docctx.owner.dateCreated}"/>)--}%

                            %{--</td></tr>--}%
                    %{--</g:if>--}%
                %{--</g:each>--}%
                %{--<tr></tr><tr></tr>--}%
                %{--<g:each in="${tasks}" var="tsk">--}%
                    %{--<tr>--}%
                        %{--<th><g:checkBox name="subscription.takeTasks" value="${tsk.id}" checked="${true}"/></th>--}%
                        %{--<th>${message(code: 'subscription.takeTasks', default: 'Take Tasks from Subscription')}</th>--}%

                        %{--<td>--}%
                            %{--<b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}--}%
                        %{--<g:formatDate format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"--}%
                                      %{--date="${tsk.endDate}"/>)--}%

                        %{--</td></tr>--}%
                %{--</g:each>--}%
            %{--</g:if>--}%
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == 3}">
                <g:render template="copyTeilnehmer" model="${[validSubChilds: validSubChilds]}"/>
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
            %{--</tbody>--}%
        %{--</table>--}%
        %{--<g:if test="${workFlowPart >= 1 && workFlowPart <= 2}">--}%
            %{--<input type="submit" class="ui button js-click-control"--}%
                   %{--value="${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep')}"/>--}%
        %{--</g:if>--}%
        %{--<g:if test="${workFlowPart == 3}">--}%
        %{--<input type="submit" class="ui button js-click-control"--}%
               %{--value="${message(code: 'subscription.renewSubscriptionConsortia.finish')}"/>--}%
        %{--</g:if>--}%
    %{--</g:form>--}%
%{--</semui:form>--}%
</body>
</html>
