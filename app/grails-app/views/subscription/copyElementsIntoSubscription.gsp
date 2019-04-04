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
        <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 1]}" workFlowPart="1" >
            <div class="content" >
                <div class="description">
                    <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                    <i class="balance scale icon"></i>${message(code: 'license')}
                    <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                </div>
            </div>
        </semui:complexSubNavItem>

        <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 5]}" workFlowPart="5" >
            <div class="content" >
                <div class="description">
                    <i class="gift icon"></i>${message(code: 'package')}
                    <i class="book icon"></i>${message(code: 'title')}
                </div>
            </div>
        </semui:complexSubNavItem>

        <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 2]}"  workFlowPart="2">
            <div class="content">
                <div class="description">
                    <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                    <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                    <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                </div>
            </div>
        </semui:complexSubNavItem>

        %{--TODO: Teilnehmer ist noch nicht fertig implementiert, wird später als wieder eingeblendet--}%
        %{--<semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 3]}"  workFlowPart="3">--}%
            %{--<i class="university icon"></i>--}%
            %{--<div class="content">--}%
                %{--<div class="title">--}%
                    %{--Teilnehmer--}%
                %{--</div>--}%
            %{--</div>--}%
        %{--</semui:complexSubNavItem>--}%

        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_YODA">
            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 4]}"  workFlowPart="4">
                <i class="tags icon"></i>
                <div class="content">
                    <div class="title">
                       Merkmale
                    </div>
                </div>
            </semui:complexSubNavItem>
        </sec:ifAnyGranted>
    </semui:subNav>

    <br>

    <g:if test="${workFlowPart == '2'}">
        <g:render template="copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == '3'}">
        <g:render template="copySubscriber" />
    </g:elseif>
    <g:elseif test="${workFlowPart == '4'}">
        <g:render template="copyProperties" />
    </g:elseif>
    <g:elseif test="${workFlowPart == '5'}">
        %{--<g:render template="copyPackagesAndIEs" model="${[source_validSubChilds: source_validSubChilds, target_validSubChilds: target_validSubChilds]}"/>--}%
        <g:render template="copyPackagesAndIEs" />
    </g:elseif>
    %{--workFlowPart == '1'--}%
    <g:else>
        %{--<g:render template="copyElements" model="${[source_validSubChilds: source_validSubChilds, target_validSubChilds: target_validSubChilds]}"/>--}%
        <g:render template="copyElements" />
    </g:else>
    <r:script>
        function jsConfirmation() {
            if ($( "input[value ='REPLACE']" ).is( ":checked" )){
                return confirm('Wollen Sie wirklich diese(s) Element(e) in der Ziellizenz löschen?')
            }
        }
    </r:script>
</body>
</html>
