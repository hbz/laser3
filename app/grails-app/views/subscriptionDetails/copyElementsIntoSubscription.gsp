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

    <% Map params = [id: params.id];
        if (sourceSubscriptionId) params << [sourceSubscriptionId: sourceSubscriptionId];
        if (targetSubscriptionId) params << [targetSubscriptionId: targetSubscriptionId];
    %>
    <semui:subNav>
        <semui:complexSubNavItem controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 1]}" workFlowPart="1" >
            <div class="content" >
                <div class="title">
                    Auswahl Eigenschaften
                </div>
                <div class="description">
                    <i class="calendar alternate outline icon"></i>Datum
                    <i class="balance scale icon"></i>Vertrag
                    <i class="university icon"></i>Organisationen
                    <i class="gift icon"></i>Pakete
                    <i class="newspaper icon"></i>Titel
                </div>
            </div>
        </semui:complexSubNavItem>

        <semui:complexSubNavItem controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 2]}"  workFlowPart="2">
            <div class="content">
                <div class="title">
                    weitere Lizenzeigenschaften
                </div>
                <div class="description">
                    <i class="file outline icon"></i>Dokumente
                    <i class="sticky note outline icon"></i>Anmerkungen
                    <i class="checked calendar icon"></i>Aufgaben
                </div>
            </div>
        </semui:complexSubNavItem>

        <semui:complexSubNavItem controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 3]}"  workFlowPart="3">
            <i class="big university icon"></i>
            <div class="content">
                <div class="title">
                    Teilnehmer
                </div>
            </div>
        </semui:complexSubNavItem>

        <semui:complexSubNavItem controller="subscriptionDetails" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 4]}"  workFlowPart="4">
            <i class="big tags icon"></i>
            <div class="content">
                <div class="title">
                   Merkmale
                </div>
            </div>
        </semui:complexSubNavItem>
    </semui:subNav>
    <% /*<div class="ui steps">
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
    */ %>
    <g:if test="${workFlowPart == '2'}">
        <g:render template="copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == '3'}">
        <g:render template="copySubscriber" />
    </g:elseif>
    <g:elseif test="${workFlowPart == '4'}">
        <g:render template="copyProperties" />
    </g:elseif>
    %{--workFlowPart == '1'--}%
    <g:else>
        <g:render template="copyElements" model="${[source_validSubChilds: source_validSubChilds, target_validSubChilds: target_validSubChilds]}"/>
    </g:else>
</body>
</html>
