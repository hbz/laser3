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
            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DATES_OWNER_RELATIONS]}" workFlowPart="${WORKFLOW_DATES_OWNER_RELATIONS}" >
                <div class="content" >
                    <div class="title">Rahmendaten</div>
                    <div class="description">
                        <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                        <i class="balance scale icon"></i>${message(code: 'license')}
                        <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_PACKAGES_ENTITLEMENTS]}" workFlowPart="${WORKFLOW_PACKAGES_ENTITLEMENTS}" >
                <div class="content" >
                    <div class="title">Bestand</div>
                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'package')}
                        <i class="book icon"></i>${message(code: 'title')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}"  workFlowPart="${WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
                <div class="content">
                    <div class="title">Anhänge</div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: 3]}"  workFlowPart="3">
                <div class="content">
                    <div class="title">
                        ${message(code: 'consortium.subscriber')}
                    </div>
                    <div class="description">
                        <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_PROPERTIES]}"  workFlowPart="${WORKFLOW_PROPERTIES}">
                <div class="content">
                    <div class="title">${message(code: 'properties')}</div>
                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>

                </div>
            </semui:complexSubNavItem>
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
                if (source.checked && ! checkboxes[i].checked) {
                    $(checkboxes[i]).trigger('click')
                }
                else if (! source.checked && checkboxes[i].checked) {
                    $(checkboxes[i]).trigger('click')
                }
            }
        }

        // ONLY FOR PROPERIES
        var takeProperty = $('input[name="subscription.takeProperty"]');
        var deleteProperty = $('input[name="subscription.deleteProperty"]');

        function selectAllTake(source) {
            var table = $(source).closest('table');
            var thisBulkcheck = $(table).find(takeProperty);
            $( thisBulkcheck ).each(function( index, elem ) {
                elem.checked = source.checked;
                markAffectedTake($(this));
            })
        }
        function selectAllDelete(source) {
            var table = $(source).closest('table');
            var thisBulkcheck = $(table).find(deleteProperty);
            $( thisBulkcheck ).each(function( index, elem ) {
                elem.checked = source.checked;
                markAffectedDelete($(this));
            })
        }

        $(takeProperty).change( function() {
            markAffectedTake($(this));
        });
        $(deleteProperty).change( function() {
            markAffectedDelete($(this));
        });

        markAffectedTake = function (that) {
            var multiPropertyIndex = ($(that).closest ('.la-copyElements-flex-container').index()) ;

            if ($(that).is(":checked") ||  $(that).parents('tr').find('input[name="subscription.deleteProperty"]').is(':checked')) {
                $(that).parents('.la-replace').parents('.la-copyElements-flex-container').addClass('willStay');
                // Mehrfach zu vergebende Merkmale bekommen keine Löschmarkierung, da sie nicht überschreiben sondern kopiert werden
                if ($(that).attr('data-multipleOccurrence') != 'true') {
                    $(that).parents('td').next('td').children('.la-copyElements-flex-container:nth-child(' + (multiPropertyIndex + 1) + ')').addClass('willBeReplaced');
                }
            }
            else {
                $(that).parents('.la-replace').parents('.la-copyElements-flex-container').removeClass('willStay');
                $(that).parents('td').next('td').children('.la-copyElements-flex-container:nth-child(' + (multiPropertyIndex + 1) + ')').removeClass('willBeReplaced');
            }
        }
        markAffectedDelete = function (that) {
            if ($(that).is(":checked") ||  $(that).parents('tr').find('input[name="subscription.takeProperty"]').is(':checked')) {
                $(that).parents('.la-replace').parents('.la-copyElements-flex-container').removeClass('willStay');
                $(that).parents('.la-noChange').parents('.la-copyElements-flex-container').addClass('willBeReplaced');
            }
            else {
                $(that).parents('.la-noChange').parents('.la-copyElements-flex-container').removeClass('willBeReplaced');
            }
        }

        // $(takeProperty).each(function( index, elem ) {
        //     if (elem.checked){
        //         markAffectedTake(elem)
        //     }
        // });

    </r:script>
</body>
</html>
