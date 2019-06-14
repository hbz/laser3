<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<%@ page import="static com.k_int.kbplus.SubscriptionController.*"%>

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
        <g:if test="${isRenewSub}">
            ${message(code: 'subscription.details.renewals.renew_sub.label')}
        </g:if>
        <g:else>
            ${message(code: 'subscription.details.copyElementsIntoSubscription.label')}
        </g:else>
    </h1>

    <semui:messages data="${flash}"/>

    <% Map params = [id: params.id];
        if (sourceSubscriptionId) params << [sourceSubscriptionId: sourceSubscriptionId];
        if (targetSubscriptionId) params << [targetSubscriptionId: targetSubscriptionId];
        if (isRenewSub) params << [isRenewSub: isRenewSub];
    %>
    <g:if test="${isRenewSub}">
        <div class="ui tablet stackable steps">
            <div class="${workFlowPart == WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''} step">
                <div class="content">
                    <div class="content" >
                        <div class="title">Rahmendaten</div>
                        <div class="description">
                            <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                            <i class="balance scale icon"></i>${message(code: 'license')}
                            <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                        </div>
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : ''} step">
                <div class="content" >
                    <div class="title">Bestand</div>
                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'package')}
                        <i class="book icon"></i>${message(code: 'title')}
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''} step">
                <div class="content">
                    <div class="title">Anhänge</div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_PROPERTIES ? 'active' : ''} step">
                <div class="content">
                    <div class="title">${message(code: 'properties')}</div>
                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    <g:else>
        <semui:subNav>
            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 1]}" workFlowPart="1" >
                <div class="content" >
                    <div class="title">Rahmendaten</div>
                    <div class="description">
                        <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                        <i class="balance scale icon"></i>${message(code: 'license')}
                        <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 5]}" workFlowPart="5" >
                <div class="content" >
                    <div class="title">Bestand</div>
                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'package')}
                        <i class="book icon"></i>${message(code: 'title')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 2]}"  workFlowPart="2">
                <div class="content">
                    <div class="title">Anhänge</div>
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
                        %{--${message(code: 'consortium.subscriber')}--}%
                    %{--</div>--}%
                %{--</div>--}%
            %{--</semui:complexSubNavItem>--}%

            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_YODA">
                <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 4]}"  workFlowPart="4">
                    <div class="content">
                        <div class="title">${message(code: 'properties')}</div>
                        <div class="description">
                            <i class="tags icon"></i>${message(code: 'properties')}
                        </div>

                    </div>
                </semui:complexSubNavItem>
            </sec:ifAnyGranted>
        </semui:subNav>
    </g:else>
    <br>

    <g:if test="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <g:render template="copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == WORKFLOW_SUBSCRIBER}">
        <g:render template="copySubscriber" />
    </g:elseif>
    <g:elseif test="${workFlowPart == WORKFLOW_PROPERTIES}">
        <g:render template="copyPropertiesCompare" />
    </g:elseif>
    <g:elseif test="${workFlowPart == WORKFLOW_PACKAGES_ENTITLEMENTS}">
        <g:render template="copyPackagesAndIEs" />
    </g:elseif>
    %{--<g:elseif test="${workFlowPart == WORKFLOW_DATES_OWNER_RELATIONS}">--}%
    <g:else>
        <g:render template="copyElements" />
    </g:else>
    <r:script>
        function jsConfirmation() {
            if ($( "td input[data-action='delete']" ).is( ":checked" )){
                return confirm('Wollen Sie wirklich diese(s) Element(e) in der Ziellizenz löschen?')
            }
        }
        function toggleAllCheckboxes(source) {
            var action = $(source).attr("data-action")
            var checkboxes = document.querySelectorAll('input[data-action="'+action+'"]');
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i] != source){
                    checkboxes[i].checked = source.checked;
                }
            }
        }
    </r:script>
</body>
</html>
