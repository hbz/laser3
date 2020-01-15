<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<%@ page import="static com.k_int.kbplus.SubscriptionController.*"%>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <g:if test="${isRenewSub}">
        <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.renewals.renew_sub.label')}</title>
    </g:if>
    <g:else>
        <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.copyElementsIntoSubscription.label')}</title>
    </g:else>
</head>
<body>
    <g:render template="breadcrumb" model="${[params: params]}"/>
    <br>
    <g:if test="${isRenewSub}">
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'subscription.details.renewals.renew_sub.label')} </h1>
    </g:if>
    <g:else>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'subscription.details.copyElementsIntoSubscription.label')} </h1>
    </g:else>

    <semui:messages data="${flash}"/>

    <% Map params = [id: params.id];
        if (sourceSubscriptionId)   params << [sourceSubscriptionId: sourceSubscriptionId];
        if (targetSubscriptionId)   params << [targetSubscriptionId: targetSubscriptionId];
        if (isRenewSub)             params << [isRenewSub: isRenewSub];
    %>
    <g:if test="${isRenewSub}">
        <div class="ui tablet stackable steps la-clear-before">
            <div class="${workFlowPart == WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''} step">
                <div class="content">
                    <div class="content" >
                        <div class="title">
                            <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: null]}">
                                ${message(code: 'subscription.details.copyElementsIntoSubscription.general_data.label')}
                            </g:link>
                        </div>
                        <div class="description">
                            <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                            <i class="balance scale icon"></i>${message(code: 'license')}
                            <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                        </div>
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_IDENTIFIERS ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        %{--<g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_PACKAGES_ENTITLEMENTS]}">--}%
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DATES_OWNER_RELATIONS]}">
                            ${message(code: 'default.identifiers.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="barcode icon"></i>${message(code: 'default.identifiers.label')}
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : ''} step">
                <div class="content" >
                    <div class="title">
                        %{--<g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DATES_OWNER_RELATIONS]}">--}%
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_IDENTIFIERS]}">
                            ${message(code: 'subscription.details.copyElementsIntoSubscription.inventory.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'package')}
                        <i class="book icon"></i>${message(code: 'title')}
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_PACKAGES_ENTITLEMENTS]}">
                            ${message(code: 'subscription.details.copyElementsIntoSubscription.attachements.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </div>

            <g:if test="${isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM_SURVEY,ORG_CONSORTIUM,ORG_INST_COLLECTIVE", "INST_USER")}">
                <div class="${workFlowPart == WORKFLOW_SUBSCRIBER ? 'active' : ''} step">
                    <div class="content">
                        <div class="title">
                            <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}">
                                ${message(code: 'consortium.subscriber')}
                            </g:link>
                        </div>
                        <div class="description">
                            <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
                </div>
            </g:if>
            <div class="${workFlowPart == WORKFLOW_PROPERTIES ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_SUBSCRIBER]}">
                            ${message(code: 'properties')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    <g:else>
        <semui:subNav>
            <semui:complexSubNavItem  class="${workFlowPart == WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DATES_OWNER_RELATIONS]}" >
                <div class="content" >
                    <div class="title">${message(code: 'subscription.details.copyElementsIntoSubscription.general_data.label')}</div>
                    <div class="description">
                        <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                        <i class="balance scale icon"></i>${message(code: 'license')}
                        <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem class="${workFlowPart == WORKFLOW_IDENTIFIERS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_IDENTIFIERS]}" >
                <div class="content">
                    <div class="title">${message(code: 'default.identifiers.label')}</div>
                    <div class="description">
                        <i class="barcode icon"></i>${message(code: 'default.identifiers.label')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem  class="${workFlowPart == WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_PACKAGES_ENTITLEMENTS]}" >
                <div class="content" >
                    <div class="title">${message(code: 'subscription.details.copyElementsIntoSubscription.inventory.label')}</div>
                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'package')}
                        <i class="book icon"></i>${message(code: 'title')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem class="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}" >
                <div class="content">
                    <div class="title">${message(code: 'subscription.details.copyElementsIntoSubscription.attachements.label')}</div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <g:if test="${isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM_SURVEY,ORG_CONSORTIUM,ORG_INST_COLLECTIVE", "INST_USER")}">
                <semui:complexSubNavItem class="${workFlowPart == WORKFLOW_SUBSCRIBER ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_SUBSCRIBER]}" >
                    <div class="content">
                        <div class="title">
                            ${message(code: 'consortium.subscriber')}
                        </div>
                        <div class="description">
                            <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
                </semui:complexSubNavItem>
            </g:if>

            <semui:complexSubNavItem class="${workFlowPart == WORKFLOW_PROPERTIES ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: WORKFLOW_PROPERTIES]}" >
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
    <div class="la-legend">
        <span class="la-key"><strong>${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.key')}: </strong></span>
        <span class="la-added">${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.willStay')}</span>
        <span class="la-removed">${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.willBeReplaced')}</span>
    </div>
    <g:if test="${workFlowPart == WORKFLOW_IDENTIFIERS}">
        <g:render template="identifiers" />
    </g:if>
    <g:elseif test="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <g:render template="copyDocsAndTasks" />
    </g:elseif>
    <g:elseif test="${workFlowPart == WORKFLOW_SUBSCRIBER && isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM_SURVEY,ORG_CONSORTIUM,ORG_INST_COLLECTIVE", "INST_USER")}">
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

            var sourceElem = $(that).parents('.la-replace').parents('.la-copyElements-flex-container');
            var targetElem = $(that).parents('td').next('td').children('.la-copyElements-flex-container:nth-child(' + (multiPropertyIndex + 1) + ')');

            if ($(that).is(":checked")) {
                sourceElem.addClass('willStay');
                // Properties with multipleOccurence do not receive a deletion mark because they are not overwritten but copied
                if ($(that).attr('data-multipleOccurrence') == 'true') {
                } else {
                    targetElem.addClass('willBeReplaced');
                }
            } else {
                sourceElem.removeClass('willStay');
                if ( (that).parents('tr').find('input[name="subscription.deleteProperty"]').is(':checked')){
                } else {
                    targetElem.removeClass('willBeReplaced');
                }
            }
        }
        markAffectedDelete = function (that) {
            var sourceElem = $(that).parents('.la-replace').parents('.la-copyElements-flex-container');
            var targetElem = $(that).parents('.la-noChange').parents('.la-copyElements-flex-container');
            if ($(that).is(":checked")) {
                if ($(that).parents('tr').find('input[name="subscription.takeProperty"]').is(':checked')) {
                    if ($(that).attr('data-multipleOccurrence') == 'true') {
                        targetElem.addClass('willBeReplaced')
                    } else {
                        targetElem.addClass('willBeReplaced')
                    }
                } else {
                     targetElem.addClass('willBeReplaced');
                }
            } else {
                if ($(that).parents('tr').find('input[name="subscription.takeProperty"]').is(':checked')) {
                    if ($(that).attr('data-multipleOccurrence') == 'true') {
                        targetElem.removeClass('willBeReplaced');
                    } else {
                    }
                } else {
                    targetElem.removeClass('willBeReplaced');
                }
            }
        }

        $(takeProperty).each(function( index, elem ) {
             if (elem.checked){
                 markAffectedTake(elem)
             }
         });

    </r:script>
</body>
</html>
