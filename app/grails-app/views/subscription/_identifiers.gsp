<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Doc; com.k_int.kbplus.Subscription" %>
<laser:serviceInjection />
<semui:form>
    <g:set var="isInstAdm" value="${contextService.getUser().hasAffiliation("INST_ADM")}"/>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId, isRenewSub: isRenewSub]"
            method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-table">
            <thead>
                <tr>
                    <th class="six wide">
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked />
                    </th>
                    <th class="six wide">
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <g:if test="${targetSubscription}">
                            <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
                        </g:if>
                    </th>
                </tr>
            </thead>
            %{--IDENTIFIERS:--}%
            <tbody class="top aligned">
                <tr>
                    <td name="subscription.takeTasks.source">
                        <g:each in="${sourceIdentifiers}" var="ident">
                            <b>${ident.ns.ns}:</b>&nbsp${ident.value}<br />
                        </g:each>
                    </td>
                    %{--COPY:--}%
                    <td class="center aligned">
                        <g:each in="${sourceIdentifiers}" var="ident">
                            <g:if test="${ident.ns?.isUnique}">unique</g:if>
                            <div data-id="${ident.id}" class="la-element">
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="subscription.takeIdentifierIds" value="${ident.id}" data-action="copy"  />
                                </div>
                            </div>
                        </g:each>
                    </td>
                    <td name="subscription.takeTasks.target">
                        <g:each in="${targetIdentifiers}" var="ident">
                            <b>${ident.ns.ns}:</b>&nbsp${ident.value}<br />
                        </g:each>
                    </td>
                    %{--DELETE:--}%
                    <td>
                    <g:each in="${targetIdentifiers}" var="ident">
                            <div data-id="${ident.id}" class="la-element">
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="subscription.deleteIdentifierIds" value="${ident.id}" data-action="delete" checked="${false}" />
                                </div>
                            </div>
                        </g:each>
                    </td>
                </tr>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'subscription.details.copyElementsIntoSubscription.copyDocsAndTasks.button')}" />
        <div class="sixteen wide field" style="text-align: right;">
            <g:set var="submitDisabled" value="${(sourceSubscription && targetSubscription)? '' : 'disabled'}"/>
            <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()"  ${submitDisabled}/>
        </div>
    </g:form>
</semui:form>
<r:script>

    var subCopyController = {

        checkboxes : {
            $takeDocIds: $('input[name="subscription.takeDocIds"]'),
            $deleteDocIds: $('input[name="subscription.deleteDocIds"]'),
            $takeAnnouncementIds: $('input[name="subscription.takeAnnouncementIds"]'),
            $deleteAnnouncementIds: $('input[name="subscription.deleteAnnouncementIds"]'),
            $takeTaskIds: $('input[name="subscription.takeTaskIds"]'),
            $deleteTaskIds: $('input[name="subscription.deleteTaskIds"]')
        },

        init: function (elem) {
            var ref = subCopyController.checkboxes

            ref.$takeDocIds.change( function(event) {
                subCopyController.takeDocIds(this);
            }).trigger('change')

            ref.$deleteDocIds.change( function(event) {
                subCopyController.deleteDocIds(this);
            }).trigger('change')

            ref.$takeAnnouncementIds.change( function(event) {
                subCopyController.takeAnnouncementIds(this);
            }).trigger('change')

            ref.$deleteAnnouncementIds.change( function(event) {
                subCopyController.deleteAnnouncementIds(this);
            }).trigger('change')

            ref.$takeTaskIds.change( function(event) {
                subCopyController.takeTaskIds(this);
            }).trigger('change')

            ref.$deleteTaskIds.change( function(event) {
                subCopyController.deleteTaskIds(this);
            }).trigger('change')
        },

        takeDocIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeDocs.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeDocs.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeDocs.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeDocIds') < 1) {
                    $('.table tr td[name="subscription.takeDocs.target"] div').removeClass('willStay');
                }
            }
        },

        deleteDocIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeDocs.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeDocs.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        takeAnnouncementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeAnnouncements.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeAnnouncements.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeAnnouncementIds') < 1) {
                    $('.table tr td[name="subscription.takeAnnouncements.target"] div').removeClass('willStay');
                }
            }
        },

        deleteAnnouncementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeAnnouncements.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        takeTaskIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeTasks.source"] div[data-id="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeTasks.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeTasks.source"] div[data-id="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeTaskIds') < 1){
                    $('.table tr td[name="subscription.takeTasks.target"] div').removeClass('willStay');
                }
            }
        },

        deleteTaskIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeTasks.target"] div[data-id="' + elem.value + '"]').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeTasks.target"] div[data-id="' + elem.value + '"]').removeClass('willBeReplaced');
            }
        },

        getNumberOfCheckedCheckboxes: function(inputElementName) {
            var checkboxes = document.querySelectorAll('input[name="' + inputElementName + '"]');
            var numberOfChecked = 0;
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].checked) {
                    numberOfChecked++;
                }
            }
            return numberOfChecked;
        }
    }

    subCopyController.init()
</r:script>
