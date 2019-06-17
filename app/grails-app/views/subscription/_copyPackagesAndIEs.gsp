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
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned"><i class="ui icon angle double right"></i><input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked/>
                    <th class="six wide">
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <i class="ui icon trash alternate outline"></i>
                        <g:if test="${targetSubscription}">
                            <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
                            <br />
                        </g:if>
                    </th>
                </tr>
            </thead>
            <tbody class="top aligned">
            <tr>
                <td name="subscription.takePackages.source">
                    <b>${message(code: 'subscription.packages.label')}: ${sourceSubscription?.packages?.size()}</b>
                    <g:each in="${sourceSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-element">
                                <label>
                                    <i class="gift icon"></i>
                                    <g:link controller="package" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link>
                                    <semui:debugInfo>PkgId: ${sp.pkg?.id}</semui:debugInfo>
                                    <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                                </label>
                        </div>
                    </g:each>
                </td>
                %{--COPY:--}%
                <td class="center aligned">
                    <i class="ui icon angle double right" title="${message(code:'default.copy.label')}"></i>
                    <g:each in="${sourceSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-element">
                            <g:checkBox name="subscription.takePackageIds" value="${genericOIDService.getOID(sp.pkg)}" data-pkgid="${sp.pkg?.id}" data-action="copy" checked="${true}"/>
                            <br />
                        </div>
                    </g:each>
                </td>
                <td name="subscription.takePackages.target">
                    <b>${message(code: 'subscription.packages.label')}: ${targetSubscription?.packages?.size()}</b>
                    <div>
                        <g:each in="${targetSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-element">
                                <i class="gift icon"></i>
                                <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link>
                                <semui:debugInfo>PkgId: ${sp.pkg?.id}</semui:debugInfo>
                                <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                                <br>
                            </div>
                        </g:each>
                    </div>
                </td>
                %{--DELETE--}%
                <td class="center aligned">
                    <i class="ui icon trash alternate outline"></i>
                    <g:each in="${targetSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-element">
                            <g:checkBox name="subscription.deletePackageIds" value="${genericOIDService.getOID(sp)}" data-pkgid="${genericOIDService.getOID(sp.pkg)}" data-action="delete" checked="${false}"/>
                            <br />
                        </div>
                    </g:each>
                </td>
            </tr>
            <tr>
                <td name="subscription.takeEntitlements.source">
                    <b>${message(code: 'issueEntitlement.countSubscription')} </b>${sourceSubscription? sourceIEs?.size() : ""}<br>
                    <g:each in="${sourceIEs}" var="ie">
                        <div class="la-element" data-ieoid="${genericOIDService.getOID(ie)}">
                                <label>
                                    <semui:listIcon hideTooltip="true" type="${ie.tipp.title.type.getI10n('value')}"/>
                                    <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                                    <semui:debugInfo>Tipp PkgId: ${ie.tipp.pkg.id}, Tipp ID: ${ie.tipp.id}</semui:debugInfo>
                                </label>
                        </div>
                    </g:each>
                </td>
                %{--COPY:--}%
                <td class="center aligned">
                    <i class="ui icon angle double right" title="${message(code:'default.copy.label')}"></i>
                    <br />
                    <g:each in="${sourceIEs}" var="ie">
                        <g:checkBox name="subscription.takeEntitlementIds" value="${genericOIDService.getOID(ie)}" data-action="copy" checked="${true}"/>
                        <br />
                    </g:each>
                </td>
                <td name="subscription.takeEntitlements.target">
                    <b>${message(code: 'issueEntitlement.countSubscription')} </b>${targetSubscription? targetIEs?.size(): ""} <br />
                    <g:each in="${targetIEs}" var="ie">
                        <div class="la-element" data-pkgoid="${genericOIDService.getOID(ie?.tipp?.pkg)}" data-ieoid="${genericOIDService.getOID(ie)}">
                            <semui:listIcon hideTooltip="true" type="${ie.tipp.title.type.getI10n('value')}"/>
                            <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                            <semui:debugInfo>Tipp PkgId: ${ie.tipp.pkg.id}, Tipp ID: ${ie.tipp.id}</semui:debugInfo>
                        </div>
                    </g:each>
                </td>
                %{--DELETE--}%
                <td class="center aligned">
                    <i class="ui icon trash alternate outline"></i>
                    <br />
                    <g:each in="${targetIEs}" var="ie">
                        <g:checkBox name="subscription.deleteEntitlementIds" value="${genericOIDService.getOID(ie)}" data-action="delete" checked="${false}"/>
                        <br />
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
    $('input[name="subscription.takePackageIds"]').change( function(event) {
        var pkgoid = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takePackages.source"] div[data-pkgoid="' + pkgoid + '"]').addClass('willStay');
            $('.table tr td[name="subscription.takePackages.target"] div').addClass('willStay');
        } else {
            $('.table tr td[name="subscription.takePackages.source"] div[data-pkgoid="' + pkgoid + '"]').removeClass('willStay');
            if (getNumberOfCheckedCheckboxes('subscription.takePackageIds') < 1){
                $('.table tr td[name="subscription.takePackages.target"] div').removeClass('willStay');
            }
        }
    })

    $('input[name="subscription.deletePackageIds"]').change( function(event) {
        var subPkgOid = this.value;
        var pkgOid = $(this).attr('data-pkgid');
        if (this.checked) {
            $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"]').addClass('willBeReplacedStrong');
            $('.table tr td[name="subscription.takeEntitlements.target"] div[data-pkgoid="' + pkgOid + '"]').addClass('willBeReplacedStrong');
        } else {
            $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"]').removeClass('willBeReplacedStrong');
            $('.table tr td[name="subscription.takeEntitlements.target"] div[data-pkgoid="' + pkgOid + '"]').removeClass('willBeReplacedStrong');
        }
    })

    $('input[name="subscription.takeEntitlementIds"]').change( function(event) {
        var ieoid = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieoid="' + ieoid + '"]').addClass('willStay');
            $('.table tr td[name="subscription.takeEntitlements.target"] div').addClass('willStay');
        } else {
            $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieoid="' + ieoid + '"]').removeClass('willStay');
            if (getNumberOfCheckedCheckboxes('subscription.takeEntitlementIds') < 1){
                $('.table tr td[name="subscription.takeEntitlements.target"] div').removeClass('willStay');
            }
        }
    })

    $('input[name="subscription.deleteEntitlementIds"]').change( function(event) {
        var ieoid = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takeEntitlements.target"] div[data-ieoid="' + ieoid + '"]').addClass('willBeReplacedStrong');
        } else {
            $('.table tr td[name="subscription.takeEntitlements.target"] div[data-ieoid="' + ieoid + '"]').removeClass('willBeReplacedStrong');
        }
    })

    function getNumberOfCheckedCheckboxes(inputElementName){
        var checkboxes = document.querySelectorAll('input[name="'+inputElementName+'"]');
        var numberOfChecked = 0;
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                numberOfChecked++;
            }
        }
        return numberOfChecked;
    }
</r:script>




