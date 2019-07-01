<%@ page import="com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.SubscriptionController; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription; com.k_int.kbplus.GenericOIDService "%>
<laser:serviceInjection />

<semui:form>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>

    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscription?.id, isRenewSub: isRenewSub]" method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-table">
            <thead>
                <tr>
                    <th class="six wide">
                        <div class="la-copyElements-th-flex-container">
                            <div class="la-copyElements-th-flex-item">
                                <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                            </div>
                            <div>
                                <input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked/>
                            </div>
                        </div>
                    </th>

                    <th class="six wide">
                        <div class="la-copyElements-th-flex-container">
                            <div class="la-copyElements-th-flex-item">
                                <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                            </div>
                            <div>
                                <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
                            </div>
                        </div>
                    </th>
                </tr>
            </thead>
            <tbody class="top aligned">
            <tr>
                <td name="subscription.takePackages.source">
                    <b>${message(code: 'subscription.packages.label')}: ${sourceSubscription?.packages?.size()}</b>
                    <g:each in="${sourceSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <div class="la-copyPack-container la-element">
                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-copyPack-item">
                                    <label>
                                        <i class="gift icon"></i>
                                        <g:link controller="package" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link>
                                        <semui:debugInfo>PkgId: ${sp.pkg?.id}</semui:debugInfo>
                                        <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                                    </label>
                            </div>

                            %{--COPY:--}%

                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}">
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="subscription.takePackageIds" value="${genericOIDService.getOID(sp.pkg)}" data-pkgid="${sp.pkg?.id}" data-action="copy" checked="${true}"/>
                                </div>
                                <br />
                            </div>
                        </div>
                    </g:each>
                </td>



                <td name="subscription.takePackages.target">
                    <b>${message(code: 'subscription.packages.label')}: ${targetSubscription?.packages?.size()}</b>

                    <g:each in="${targetSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <div class="la-copyPack-container la-element">
                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-copyPack-item">
                                <i class="gift icon"></i>
                                <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link>
                                <semui:debugInfo>PkgId: ${sp.pkg?.id}</semui:debugInfo>
                                <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                                <br>
                            </div>

                            %{--DELETE--}%
                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}">
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="subscription.deletePackageIds" value="${genericOIDService.getOID(sp)}" data-pkgid="${genericOIDService.getOID(sp.pkg)}" data-action="delete" checked="${false}"/>
                                </div>
                            </div>
                        </div>
                     </g:each>
                </td>
            </tr>
            <tr>
                <td name="subscription.takeEntitlements.source">
                    <b>${message(code: 'issueEntitlement.countSubscription')} </b>${sourceSubscription? sourceIEs?.size() : ""}<br>
                    <g:each in="${sourceIEs}" var="ie">
                        <div class="la-copyPack-container la-element">
                            <div  data-ieoid="${genericOIDService.getOID(ie)}" class="la-copyPack-item">
                                    <label>
                                        <semui:listIcon hideTooltip="true" type="${ie.tipp.title.type.getI10n('value')}"/>
                                        <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                                        <semui:debugInfo>Tipp PkgId: ${ie.tipp.pkg.id}, Tipp ID: ${ie.tipp.id}</semui:debugInfo>
                                    </label>
                            </div>

                            %{--COPY:--}%
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeEntitlementIds" value="${genericOIDService.getOID(ie)}" data-action="copy" checked="${true}"/>
                            </div>
                        </div>
                    </g:each>
                </td>
                <td name="subscription.takeEntitlements.target">
                    <b>${message(code: 'issueEntitlement.countSubscription')} </b>${targetSubscription? targetIEs?.size(): ""} <br />
                    <g:each in="${targetIEs}" var="ie">
                        <div class="la-copyPack-container la-element">
                            <div data-pkgoid="${genericOIDService.getOID(ie?.tipp?.pkg)}" data-ieoid="${genericOIDService.getOID(ie)}" class=" la-copyPack-item">
                                <semui:listIcon hideTooltip="true" type="${ie.tipp.title.type.getI10n('value')}"/>
                                <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                                <semui:debugInfo>Tipp PkgId: ${ie.tipp.pkg.id}, Tipp ID: ${ie.tipp.id}</semui:debugInfo>
                            </div>

                             %{--DELETE--}%
                            <div class="ui checkbox la-toggle-radio la-noChange">
                                <g:checkBox name="subscription.deleteEntitlementIds" value="${genericOIDService.getOID(ie)}" data-action="delete" checked="${false}"/>
                            </div>
                        </div>
                    </g:each>
                </td>
            </tr>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'subscription.details.copyElementsIntoSubscription.copyPackagesAndIEs.button')}" />
        <div class="sixteen wide field" style="text-align: right;">
            <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()"/>
        </div>
    </g:form>
</semui:form>

<r:script>

    var subCopyController = {

        checkboxes : {
            $takePackageIds: $('input[name="subscription.takePackageIds"]'),
            $deletePackageIds:  $('input[name="subscription.deletePackageIds"]'),
            $takeEntitlementIds: $('input[name="subscription.takeEntitlementIds"]'),
            $deleteEntitlementIds: $('input[name="subscription.deleteEntitlementIds"]')
        },

        init: function(elem) {
            var ref = subCopyController.checkboxes

            ref.$takePackageIds.change( function(event) {
                subCopyController.takePackageIds(this);
            }).trigger('change')

            ref.$deletePackageIds.change( function(event) {
                subCopyController.deletePackageIds(this);
            }).trigger('change')

            ref.$takeEntitlementIds.change( function(event) {
                subCopyController.takeEntitlementIds(this);
            }).trigger('change')

            ref.$deleteEntitlementIds.change( function(event) {
                subCopyController.deleteEntitlementIds(this);
            }).trigger('change')
        },

        takePackageIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takePackages.source"] div[data-pkgoid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takePackages.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takePackages.source"] div[data-pkgoid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takePackageIds') < 1){
                    $('.table tr td[name="subscription.takePackages.target"] div').removeClass('willStay');
                }
            }
        },

        deletePackageIds: function(elem) {
            var pkgOid = $(elem).attr('data-pkgid'); // FEHLER dk !?
            //var pkgOid = $(elem).attr('data-pkgoid'); // dk
            if (elem.checked) {
                $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"]').addClass('willBeReplacedStrong');
                $('.table tr td[name="subscription.takeEntitlements.target"] div[data-pkgoid="' + pkgOid + '"]').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"]').removeClass('willBeReplacedStrong');
                $('.table tr td[name="subscription.takeEntitlements.target"] div[data-pkgoid="' + pkgOid + '"]').removeClass('willBeReplacedStrong');
            }
        },

        takeEntitlementIds: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieoid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeEntitlements.target"] div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieoid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeEntitlementIds') < 1){
                    $('.table tr td[name="subscription.takeEntitlements.target"] div').removeClass('willStay');
                }
            }
        },

        deleteEntitlementIds: function(elem) {
            var ieoid = elem.value // FEHLER dk !?
            //var ieoid = $(elem).attr('data-ieoid'); // dk
            if (elem.checked) {
                $('.table tr td[name="subscription.takeEntitlements.target"] div[data-ieoid="' + ieoid + '"]').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takeEntitlements.target"] div[data-ieoid="' + ieoid + '"]').removeClass('willBeReplacedStrong');
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




