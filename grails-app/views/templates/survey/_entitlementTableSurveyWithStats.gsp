<%@ page import="de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.remote.ApiSource; de.laser.TitleInstancePackagePlatform; de.laser.base.AbstractReport" %>
<div class="sixteen wide column">
    <g:set var="counter" value="${offset + 1}"/>


    <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header" id="surveyEntitlements">
        <thead>
        <tr>
            <th>
                %{--<g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>--}%
            </th>
            <th>${message(code: 'sidewide.number')}</th>
            <g:if test="${params.tabStat != 'total'}">
            <g:sortableColumn class="ten wide" params="${params}" property="r.title.sortname"
                              title="${message(code: 'title.label')}"/>
            </g:if>
            <g:else>
                <th>${message(code: 'title.label')}</th>
            </g:else>
            <g:sortableColumn title="${message(code:"default.usage.metricType")}" property="r.metricType" params="${params}" class="two wide"/>
            <g:sortableColumn title="${message(code:"default.count.label")}" property="r.reportCount" params="${params}" class="two wide"/>
            <th class="two wide"><g:message code="default.actions.label"/></th>
        </tr>
        </thead>
        <tbody>

        <g:each in="${stats.findAll { AbstractReport rep -> rep.title != null }}" var="stat">
            <g:set var="tipp" value="${TitleInstancePackagePlatform.get(stat.title.id)}"/>
            <g:set var="ie" value="${IssueEntitlement.findByTippAndSubscriptionAndStatusAndAcceptStatus(stat.title, subscription, RDStore.TIPP_STATUS_CURRENT, RDStore.IE_ACCEPT_STATUS_FIXED)}"/>
            <g:set var="ieInNewSub"
                   value="${surveyService.titleContainedBySubscription(newSub, tipp)}"/>
            <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                <g:set var="participantPerpetualAccessToTitle"
                       value="${surveyService.hasParticipantPerpetualAccessToTitle(subscriberSubs, tipp)}"/>
                <g:set var="allowedToSelect"
                       value="${!(participantPerpetualAccessToTitle) && (!ieInNewSub || (ieInNewSub && ieInNewSub.acceptStatus == RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION))}"/>
            </g:if>
            <g:else>
                <g:set var="allowedToSelect"
                       value="${!ieInNewSub || (ieInNewSub && ieInNewSub.acceptStatus == RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION)}"/>
            </g:else>
            <tr data-gokbId="${tipp.gokbId}" data-tippId="${tipp.id}" data-ieId="${ie?.id}" data-index="${counter}" class="${checkedCache ? (checkedCache[ie?.id.toString()] ? 'positive' : '') : ''}">
                <td>

                    <g:if test="${(params.tab in ['allIEsStats', 'holdingIEsStats']) && (editable && !ieInNewSub && allowedToSelect)}">
                        <input type="checkbox" name="bulkflag" class="bulkcheck" ${checkedCache ? checkedCache[ie?.id.toString()] : ''}>
                    </g:if>

                </td>
                <td>${counter++}</td>
                <td class="titleCell">

                    <g:if test="${ieInNewSub && ieInNewSub.acceptStatus == RDStore.IE_ACCEPT_STATUS_FIXED}">
                        <div class="la-inline-flexbox la-popup-tooltip la-delay" data-content="${message(code: 'renewEntitlementsWithSurvey.ie.existsInSub')}" data-position="left center" data-variation="tiny">
                            <i class="icon redo alternate yellow"></i>
                        </div>
                    </g:if>

                    <g:if test="${participantPerpetualAccessToTitle}">
                        <div class="la-inline-flexbox la-popup-tooltip la-delay" data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')}" data-position="left center" data-variation="tiny">
                            <i class="icon redo alternate red"></i>
                        </div>
                    </g:if>

                    <g:if test="${!participantPerpetualAccessToTitle && previousSubscription && surveyService.titleContainedBySubscription(previousSubscription, tipp)?.acceptStatus == RDStore.IE_ACCEPT_STATUS_FIXED}">
                        <div class="la-inline-flexbox la-popup-tooltip la-delay" data-content="${message(code: 'renewEntitlementsWithSurvey.ie.existsInPreviousSubscription')}" data-position="left center" data-variation="tiny">
                            <i class="icon redo alternate orange"></i>
                        </div>
                    </g:if>

                    <g:if test="${ieInNewSub}">
                        <semui:ieAcceptStatusIcon status="${ieInNewSub.acceptStatus}"/>
                    </g:if>

                    <!-- START TEMPLATE -->
                        <laser:render template="/templates/title_short"
                                  model="${[ie: ie, tipp: tipp,
                                            showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false, overwriteEditable: false, participantPerpetualAccessToTitle: participantPerpetualAccessToTitle]}"/>
                    <!-- END TEMPLATE -->
                </td>
                <td>${stat.metricType}</td>
                <td>${stat.reportCount}</td>
                <td>
                    <g:if test="${(params.tab in ['allIEsStats', 'holdingIEsStats']) && editable && ieInNewSub && allowedToSelect}">
                        <g:link class="ui icon positive check button la-popup-tooltip la-delay"
                                action="processRemoveIssueEntitlementsSurvey"
                                params="${[id: newSub.id, singleTitle: ieInNewSub.id, packageId: packageId, surveyConfigID: surveyConfig?.id]}"
                                data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                            <i class="minus icon"></i>
                        </g:link>
                    </g:if>


                    <g:if test="${(params.tab in ['allIEsStats', 'holdingIEsStats']) && editable && !ieInNewSub && allowedToSelect }">
                        <g:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay"
                                action="processAddIssueEntitlementsSurvey"
                                params="${[id: newSub.id, singleTitle: ie?.id, surveyConfigID: surveyConfig?.id]}"
                                data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                            <i class="plus icon"></i>
                        </g:link>
                    </g:if>
                </td>
            </tr>

        </g:each>
        </tbody>
    </table>
</div>
