<%@ page import="de.laser.utils.DateUtils; de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.remote.ApiSource; de.laser.TitleInstancePackagePlatform; de.laser.base.AbstractReport" %>
<div class="sixteen wide column">
    <g:set var="counter" value="${offset + 1}"/>


    <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header" id="surveyEntitlements">
        <thead>
        <tr>
            <th>
                %{--<g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>--}%
            </th>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'title.label')}</th>
            <th class="two wide"><g:message code="default.usage.metricType"/></th>
            <g:sortableColumn title="${message(code:"default.count.label")}" property="count" params="${params}" class="two wide"/>
            <th class="two wide"><g:message code="default.actions.label"/></th>
        </tr>
        </thead>
        <tbody>
            <g:set var="start" value="${System.currentTimeMillis()}"/>
            <g:each in="${topList}" var="topView">
                <g:set var="tipp" value="${topView.getKey()}"/>
                <g:set var="ie" value="${IssueEntitlement.findByTippAndSubscriptionAndStatus(tipp, subscription, RDStore.TIPP_STATUS_CURRENT)}"/>
                <g:set var="ieInNewSub"
                       value="${surveyService.titleContainedBySubscription(subscriberSub, tipp)}"/>
                <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                    <g:set var="participantPerpetualAccessToTitle"
                           value="${surveyService.hasParticipantPerpetualAccessToTitle3(subscriber, tipp)}"/>
                    <g:set var="allowedToSelect"
                           value="${!(participantPerpetualAccessToTitle)}"/>
                </g:if>
                <g:else>
                    <g:set var="allowedToSelect"
                           value="${!ieInNewSub}"/>
                </g:else>
                <tr data-gokbId="${tipp.gokbId}" data-tippId="${tipp.id}" data-ieId="${ie?.id}" data-index="${counter}" class="${checkedCache ? (checkedCache[ie?.id.toString()] ? 'positive' : '') : ''}">
                    <td>
                        <g:if test="${(params.tab in ['topUsed']) && (editable && !ieInNewSub && allowedToSelect)}">
                            <input type="checkbox" name="bulkflag" class="bulkcheck" ${checkedCache ? checkedCache[ie?.id.toString()] : ''}>
                        </g:if>

                    </td>
                    <td>${counter++}</td>
                    <td class="titleCell">

                        <g:if test="${ieInNewSub}">
                            <div class="la-inline-flexbox la-popup-tooltip la-delay" data-content="${message(code: 'renewEntitlementsWithSurvey.ie.existsInSub')}" data-position="left center" data-variation="tiny">
                                <i class="icon redo alternate yellow"></i>
                            </div>
                        </g:if>

                        <g:if test="${participantPerpetualAccessToTitle}">
                            <g:set var="participantPerpetualAccessToTitleList"
                                   value="${surveyService.listParticipantPerpetualAccessToTitle(subscriber, tipp)}"/>
                            <div class="la-inline-flexbox la-popup-tooltip la-delay" data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')} ${participantPerpetualAccessToTitleList.collect{it.getPermanentTitleInfo(contextOrg)}.join(',')}" data-position="left center" data-variation="tiny">
                                <i class="icon redo alternate red"></i>
                            </div>
                        </g:if>

                        <g:if test="${!participantPerpetualAccessToTitle && previousSubscription}">
                            <div class="la-inline-flexbox la-popup-tooltip la-delay" data-content="${message(code: 'renewEntitlementsWithSurvey.ie.existsInPreviousSubscription')}" data-position="left center" data-variation="tiny">
                                <i class="icon redo alternate orange"></i>
                            </div>
                        </g:if>

                    <!-- START TEMPLATE -->
                        <laser:render template="/templates/title_short"
                                      model="${[ie: ie, tipp: tipp,
                                                showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false, overwriteEditable: false, participantPerpetualAccessToTitle: participantPerpetualAccessToTitle]}"/>
                    <!-- END TEMPLATE -->
                    </td>
                    <g:if test="${params.tab == 'topUsed'}">
                        <g:set var="usage" value="${usages.get(tipp)}"/>
                        <%-- continue here: display k-v pairs without concatination --%>
                        <td><g:each in="${usage.keySet()}" var="metric">${metric}<br></g:each></td>
                        <td><g:each in="${usage.keySet()}" var="metric">${usage.get(metric)}<br></g:each></td>
                    </g:if>
                    <td>

                    </td>
                </tr>
            </g:each>

        </tbody>
    </table>
    <ui:paginate params="${params}" offset="${offset}" max="${max}" total="${total}"/>
</div>
