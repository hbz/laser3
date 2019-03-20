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
        <table class="ui celled table">
            <tbody>
            <tr>
                <th>${message(code: 'default.copy.label')}</th>
                <th>${message(code: 'default.replace.label')}</th>
                <th>${message(code: 'default.doNothing.label')}</th>
                <td><b>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}:</b>
                <g:if test="${sourceSubscription}"><g:link controller="subscriptionDetails" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                </td>
                <td><b>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}:</b>
                <g:if test="${targetSubscription}"><g:link controller="subscriptionDetails" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                </td>
            </tr>
            <tr>
                <td style="vertical-align: top">

                </td>
                <td style="vertical-align: top">
                    <div class="ui radio checkbox la-toggle-radio la-replace">
                        <input type="radio" name="subscription.takeDates" value="${REPLACE}" />
                    </div>
                </td>
                <td style="vertical-align: top">
                    <div class="ui radio checkbox la-toggle-radio la-noChange">
                        <input type="radio" name="subscription.takeDates" value="${DO_NOTHING}" checked />
                    </div>
                </td>
                <td style="vertical-align: top"><g:formatDate date="${sourceSubscription?.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                    ${sourceSubscription?.endDate ? (' - ' + formatDate(date: sourceSubscription?.endDate, format: message(code: 'default.date.format.notime'))) : ''}</td>
                <td style="vertical-align: top"><g:formatDate date="${targetSubscription?.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                    ${targetSubscription?.endDate ? (' - ' + formatDate(date: targetSubscription?.endDate, format: message(code: 'default.date.format.notime'))) : ''}</td>
            </tr>
            <tr>
                <td style="vertical-align: top"></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeOwner" value="${REPLACE}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeOwner" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top">
                    %{--<g:each in="${sourceSubscription?.packages?.sort { it.pkg.name }}" var="sp">--}%
                        %{--<b>${message(code: 'subscription.packages.label')}:</b>--}%
                        %{--<g:link controller="package" action="show" target="_blank" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>--}%
                        %{--<g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>--}%
                        %{--<br>--}%
                    %{--</g:each>--}%
                    %{--<br>--}%
                    <g:if test="${sourceSubscription?.owner}">
                        <b>${message(code: 'license')}:</b>
                        <g:link controller="licenseDetails" action="show" target="_blank" id="${sourceSubscription.owner.id}">
                            ${sourceSubscription.owner}
                        </g:link>
                    </g:if>
                </td>
                <td style="vertical-align: top">
                    <g:if test="${targetSubscription?.owner}">
                        <b>${message(code: 'license')}:</b>
                        <g:link controller="licenseDetails" action="show" target="_blank" id="${targetSubscription?.owner?.id}">
                            ${targetSubscription?.owner}
                        </g:link>
                    </g:if>
                </td>
            </tr>
            <tr>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeOrgRelations" value="${COPY}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeOrgRelations" value="${REPLACE}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeOrgRelations" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top">
                    <g:each in="${source_visibleOrgRelations}" var="source_role">
                        <g:if test="${source_role.org}">
                            <b>${source_role?.roleType?.getI10n("value")}:</b>
                            <g:link controller="Organisations" action="show" target="_blank" id="${source_role.org.id}">
                                ${source_role?.org?.name}
                            </g:link><br>
                        </g:if>
                    </g:each>
                </td>
                <td style="vertical-align: top">
                    <g:each in="${target_visibleOrgRelations}" var="target_role">
                        <g:if test="${target_role.org}">
                            <b>${target_role?.roleType?.getI10n("value")}:</b>
                            <g:link controller="Organisations" action="show" target="_blank" id="${target_role.org.id}">
                                ${target_role?.org?.name}
                            </g:link>
                            <br>
                        </g:if>
                    </g:each>
                </td>
            </tr>
            <tr>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takePackages" value="${COPY}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takePackages" value="${REPLACE}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takePackages" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top">
                    <g:each in="${sourceSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        %{--<input type="checkbox" data-pckId="${sp.pkg.id}" >--}%
                        <g:checkBox name="subscription.takePackageIds" value="${genericOIDService.getOID(sp.pkg)}" data-pckId="${sp.pkg?.id}" checked="false"/>
                        <b>${message(code: 'subscription.packages.label')}:</b>
                        <g:link controller="package" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link> ${sp.pkg?.id}
                        <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                        <br>
                    </g:each>
                </td>
                <td style="vertical-align: top">
                    <g:each in="${targetSubscription?.packages?.sort { it.pkg?.name }}" var="sp">
                        <b>${message(code: 'subscription.packages.label')}:</b>
                        <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg?.id}">${sp?.pkg?.name}</g:link> ${sp.pkg?.id}
                        <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                        <br>
                    </g:each>
                </td>
            </tr>
            <tr>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeEntitlements" value="${COPY}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeEntitlements" value="${REPLACE}" /></div></td>
                <td style="vertical-align: top"><div class="ui radio checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeEntitlements" value="${DO_NOTHING}" checked /></div></td>
                <td style="vertical-align: top">
                    <g:if test="${sourceIEs}">
                        <b>${message(code: 'issueEntitlement.countSubscription')} </b>${sourceIEs?.size()}<br>
                        <g:each in="${sourceIEs}" var="ie">
                            <g:checkBox name="subscription.takeEntitlementIds" value="${genericOIDService.getOID(ie)}" checked="false"/>&nbsp
                            <semui:listIcon type="${ie.tipp.title.type.getI10n('value')}"/>&nbsp
                            <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                            ${ie.tipp.pkg.id}
                            <br />
                        </g:each>
                    </g:if>
               </td>
                <td style="vertical-align: top">
                    <g:if test="${targetIEs}">
                        <b>${message(code: 'issueEntitlement.countSubscription')} </b>${targetIEs?.size()} <br />
                        <g:each in="${targetIEs}" var="ie">
                            <div data-pckId="${ie?.tipp?.pkg?.id}">
                                <semui:listIcon type="${ie.tipp.title.type.getI10n('value')}"/>
                                <strong><g:link controller="title" action="show" id="${ie?.tipp.title.id}">${ie.tipp.title.title}</g:link></strong>
                                ${ie.tipp.pkg.id}
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
    $('input:checkbox').change( function(event) {
        // TODO Logik überprüfen, wann gewarnt werden soll!
        if (this.checked) {
            var pkgId = $(this).attr('data-pckId');
            $('.table tr div[data-pckId="' + pkgId + '"]').addClass('trWarning')
        } else {
            var pkgId = $(this).attr('data-pckId');
            $('.table tr div[data-pckId="' + pkgId + '"]').removeClass('trWarning')
        }
    })
</r:script>

<style>
table tr td div.trWarning {
    background-color: tomato !important;
    text-decoration: line-through; !important;
}
</style>
