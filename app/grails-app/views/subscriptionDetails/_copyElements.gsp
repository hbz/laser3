<%@ page import="com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.SubscriptionDetailsController; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription; com.k_int.kbplus.GenericOIDService "%>
<%@ page import="com.k_int.kbplus.SubscriptionDetailsController" %>
<%@ page import="static com.k_int.kbplus.SubscriptionDetailsController.COPY" %>
<%@ page import="static com.k_int.kbplus.SubscriptionDetailsController.REPLACE" %>
<%@ page import="static com.k_int.kbplus.SubscriptionDetailsController.DO_NOTHING" %>
<laser:serviceInjection />

<semui:form>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>

    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscription?.id]" method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-table">
            <thead>
                <tr>
                    <th class="center aligned">${message(code: 'default.copy.label')}</th>
                    <th class="center aligned">${message(code: 'default.replace.label')}</th>
                    <th class="center aligned">${message(code: 'default.doNothing.label')}</th>
                    <th class="six wide">
                        <g:if test="${sourceSubscription}"><g:link controller="subscriptionDetails" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                    </th>
                    <th class="six wide">
                        <g:if test="${targetSubscription}"><g:link controller="subscriptionDetails" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                    </th>
                </tr>
            </thead>
            <tbody>
            <tr>
                <td class="center aligned" style="vertical-align: top">

                </td>
                <td class="center aligned" style="vertical-align: top">
                    <div class="ui radio checkbox la-toggle-radio la-replace">
                        <input type="radio" name="subscription.takeDates" value="${REPLACE}" />
                    </div>
                </td>
                <td class="center aligned" style="vertical-align: top">
                    <div class="ui radio checkbox la-toggle-radio la-noChange">
                        <input type="radio" name="subscription.takeDates" value="${DO_NOTHING}" checked />
                    </div>
                </td>
                <td style="vertical-align: top" name="subscription.takeDates.source">
                    <div>
                        <g:formatDate date="${sourceSubscription?.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                        ${sourceSubscription?.endDate ? (' - ' + formatDate(date: sourceSubscription?.endDate, format: message(code: 'default.date.format.notime'))) : ''}
                    </div>
                </td>
                <td style="vertical-align: top" name="subscription.takeDates.target">
                    <div>
                        <g:formatDate date="${targetSubscription?.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                        ${targetSubscription?.endDate ? (' - ' + formatDate(date: targetSubscription?.endDate, format: message(code: 'default.date.format.notime'))) : ''}
                    </div>
                </td>
            </tr>
            <tr>
                <td class="center aligned" style="vertical-align: top"></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeOwner" value="${REPLACE}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeOwner" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top" name="subscription.takeOwner.source">
                    <div>
                        <g:if test="${sourceSubscription?.owner}">
                            <b>${message(code: 'license')}:</b>
                            <g:link controller="licenseDetails" action="show" target="_blank" id="${sourceSubscription.owner.id}">
                                ${sourceSubscription.owner}
                            </g:link>
                        </g:if>
                    </div>
                </td>
                <td style="vertical-align: top" name="subscription.takeOwner.target">
                    <div>
                        <g:if test="${targetSubscription?.owner}">
                            <b>${message(code: 'license')}:</b>
                            <g:link controller="licenseDetails" action="show" target="_blank" id="${targetSubscription?.owner?.id}">
                                ${targetSubscription?.owner}
                            </g:link>
                        </g:if>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeOrgRelations" value="${COPY}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeOrgRelations" value="${REPLACE}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeOrgRelations" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top" name="subscription.takeOrgRelations.source">
                    <div>
                        <g:each in="${source_visibleOrgRelations}" var="source_role">
                            <g:if test="${source_role.org}">
                                <b>${source_role?.roleType?.getI10n("value")}:</b>
                                <g:link controller="Organisations" action="show" target="_blank" id="${source_role.org.id}">
                                    ${source_role?.org?.name}
                                </g:link><br>
                            </g:if>
                        </g:each>
                    </div>
                </td>
                <td style="vertical-align: top" name="subscription.takeOrgRelations.target">
                    <div>
                        <g:each in="${target_visibleOrgRelations}" var="target_role">
                            <g:if test="${target_role.org}">
                                <b>${target_role?.roleType?.getI10n("value")}:</b>
                                <g:link controller="Organisations" action="show" target="_blank" id="${target_role.org.id}">
                                    ${target_role?.org?.name}
                                </g:link>
                                <br>
                            </g:if>
                        </g:each>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takePackages" value="${COPY}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takePackages" value="${REPLACE}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takePackages" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top" name="subscription.takePackages.source">
                    <b>${message(code: 'subscription.packages.label')}:</b>
                    <g:each in="${sourceSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <div data-pckOid="${genericOIDService.getOID(sp.pkg)}">
                            <div class="ui checkbox">
                                <g:checkBox name="subscription.takePackageIds" value="${genericOIDService.getOID(sp.pkg)}" data-pckId="${sp.pkg?.id}" checked="false"/>
                                <label>
                                    <g:link controller="package" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link> ${sp.pkg?.id}
                                    <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                                </label>
                            </div>

                        </div>
                    </g:each>
                </td>
                <td style="vertical-align: top" name="subscription.takePackages.target">
                    <b>${message(code: 'subscription.packages.label')}:</b>
                    <div>
                        <g:each in="${targetSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                            <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link> ${sp.pkg?.id}
                            <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                            <br>
                        </g:each>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeEntitlements" value="${COPY}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeEntitlements" value="${REPLACE}" /></div></td>
                <td class="center aligned" style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeEntitlements" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top" name="subscription.takeEntitlements.source">
                    <g:if test="${sourceIEs}">
                        <b>${message(code: 'issueEntitlement.countSubscription')} </b>${sourceIEs?.size()}<br>
                        <g:each in="${sourceIEs}" var="ie">
                            <div data-ieOid="${genericOIDService.getOID(ie)}">
                                <div class="ui checkbox">
                                    <g:checkBox name="subscription.takeEntitlementIds" value="${genericOIDService.getOID(ie)}" checked="false"/>&nbsp
                                    <label>
                                        <semui:listIcon type="${ie.tipp.title.type.getI10n('value')}"/>&nbsp
                                        <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                                        Tipp PkgId: ${ie.tipp.pkg.id}
                                        Tipp ID: ${ie.tipp.id}
                                    </label>
                                </div>
                            </div>
                        </g:each>
                    </g:if>
                </td>
                <td style="vertical-align: top" name="subscription.takeEntitlements.target">
                    <g:if test="${targetIEs}">
                        <b>${message(code: 'issueEntitlement.countSubscription')} </b>${targetIEs?.size()} <br />
                        <g:each in="${targetIEs}" var="ie">
                            <div data-pckId="${ie?.tipp?.pkg?.id}">
                                <semui:listIcon type="${ie.tipp.title.type.getI10n('value')}"/>
                                <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                                Tipp PkgId: ${ie.tipp.pkg.id}
                                Tipp ID: ${ie.tipp.id}
                                Status: ${ie.tipp.status}
                            </div>
                        </g:each>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
        <input type="submit" class="ui button js-click-control" value="Ausgewählte Elemente kopieren/überschreiben" />
    </g:form>
</semui:form>

<r:script>
    // $('input:checkbox').change( function(event) {
    //     // TODO Logik überprüfen, wann gewarnt werden soll!
    //     if (this.checked) {
    //         var pkgId = $(this).attr('data-pckId');
    //         $('.table tr div[data-pckId="' + pkgId + '"]').addClass('willBeReplaced')
    //     } else {
    //         var pkgId = $(this).attr('data-pckId');
    //         $('.table tr div[data-pckId="' + pkgId + '"]').removeClass('willBeReplaced')
    //     }
    // })

    %{--$('input:checkbox').change( function(event) {--}%
%{--// TODO Logik überprüfen, wann gewarnt werden soll!--}%
%{--if (this.checked) {--}%
%{--var prefix = ${this}.name();--}%
%{--$('.table tr[name="' + prefix + '.source"]').addClass('willStay')--}%
%{--$('.table tr[name="' + prefix + '.target"]').addClass('willBeReplaced')--}%
%{--} else {--}%
%{--var prefix = ${this}.name();--}%
%{--$('.table tr[name="' + prefix + '.source"]').removeClass('willStay')--}%
%{--$('.table tr[name="' + prefix + '.target"]').removeClass('willBeReplaced')--}%
%{--}--}%
%{--})--}%
    // FUNKTIONIERT
    $('input:radio[name="subscription.takeDates"]').change( function(event) {
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeDates.source"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeDates.target"] div').addClass('willBeReplaced')
        } else {
            $('.table tr td[name="subscription.takeDates.source"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeDates.target"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeDates.target"] div').removeClass('willBeReplaced')
        }
    })
    $('input:radio[name="subscription.takeOwner"]').change( function(event) {
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeOwner.source"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeOwner.target"] div').addClass('willBeReplaced')
        } else {
            $('.table tr td[name="subscription.takeOwner.source"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeOwner.target"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeOwner.target"] div').removeClass('willBeReplaced')
        }
    })
    $('input:radio[name="subscription.takeOwner"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takeOwner.source"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeOwner.target"] div').removeClass('willBeReplaced')
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takeOwner.source"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeOwner.target"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeOwner.target"] div').removeClass('willBeReplaced')
        }
    })
    $('input:radio[name="subscription.takeOrgRelations"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takeOrgRelations.source"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeOrgRelations.target"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeOrgRelations.target"] div').removeClass('willBeReplaced')
        }
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeOrgRelations.source"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeOrgRelations.target"] div').addClass('willBeReplaced')
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takeOrgRelations.source"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeOrgRelations.target"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeOrgRelations.target"] div').removeClass('willBeReplaced')
        }
    })
    $('input:radio[name="subscription.takePackages"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takePackages.target"] div').addClass('willStay')
            $('.table tr td[name="subscription.takePackages.target"] div').removeClass('willBeReplaced')
        }
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takePackages.target"] div').addClass('willBeReplaced')
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takePackages.source"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takePackages.target"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takePackages.target"] div').removeClass('willBeReplaced')
        }
    })
    $('input[name="subscription.takePackageIds"]').change( function(event) {
        var pckOId = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takePackages.source"] div[data-pckOid="' + pckOId + '"]').addClass('willStay')
        } else {
            $('.table tr td[name="subscription.takePackages.source"] div[data-pckOid="' + pckOId + '"]').removeClass('willStay')
        }
        // $('.table tr div[data-pckId="' + pkgId + '"]').addClass('willBeReplaced')
    })
    $('input:radio[name="subscription.takeEntitlements"]').change( function(event) {
        if (this.checked && this.value=='COPY') {
            $('.table tr td[name="subscription.takeEntitlements.target"] div').addClass('willStay')
            $('.table tr td[name="subscription.takeEntitlements.target"] div').removeClass('willBeReplaced')
        }
        if (this.checked && this.value=='REPLACE') {
            $('.table tr td[name="subscription.takeEntitlements.target"] div').addClass('willBeReplaced')
        }
        if (this.checked && this.value=='DO_NOTHING') {
            $('.table tr td[name="subscription.takeEntitlements.source"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeEntitlements.target"] div').removeClass('willStay')
            $('.table tr td[name="subscription.takeEntitlements.target"] div').removeClass('willBeReplaced')
        }
    })
    $('input[name="subscription.takeEntitlementIds"]').change( function(event) {
        var pckOId = this.value
        if (this.checked) {
            $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieOid="' + pckOId + '"]').addClass('willStay')
        } else {
            $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieOid="' + pckOId + '"]').removeClass('willStay')
        }
    })

    // GEHT LEIDER NICHT
%{--$('input:radio[name="subscription.takeDates"]').change( function(event) {--}%
%{--alert("Enter")--}%
%{--var name = ${this}.name--}%
%{--if (this.checked && this.value=='REPLACE') {--}%
%{--alert("gechecked"+name)--}%
%{--$('.table tr td[name="' + name + '.source"] div').addClass('willStay')--}%
%{--$('.table tr td[name="' + name + '.target"] div').addClass('willBeReplaced')--}%
%{--} else {--}%
%{--alert("nicht gechecked"+name)--}%
%{--$('.table tr td[name="' + name + '.source"] div').removeClass('willStay')--}%
%{--$('.table tr td[name="' + name + '.target"] div').removeClass('willBeReplaced')--}%
%{--}--}%
%{--})--}%
</r:script>


