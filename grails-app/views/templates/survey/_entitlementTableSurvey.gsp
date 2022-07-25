<%@ page import="de.laser.titles.BookInstance; de.laser.storage.RDStore; de.laser.remote.ApiSource" %>
<div class="sixteen wide column">
    <g:set var="counter" value="${offset + 1}"/>
    <g:set var="sumlistPrice" value="${0}"/>
    <g:set var="sumlocalPrice" value="${0}"/>


    <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header" id="surveyEntitlements">
        <thead>
        <tr>
            <th>
                <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>
            </th>
            <th>${message(code: 'sidewide.number')}</th>
            <g:sortableColumn class="ten wide" params="${params}" property="sortname"
                              title="${message(code: 'title.label')}"/>
            <th class="two wide"><g:message code="tipp.price"/></th>
            <th class="two wide"><g:message code="default.actions.label"/></th>
        </tr>
        </thead>
        <tbody>

        <g:each in="${ies.sourceIEs}" var="ie">
            <g:set var="tipp" value="${ie.tipp}"/>
            <g:set var="ieInNewSub"
                   value="${surveyService.titleContainedBySubscription(newSub, tipp)}"/>
            <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                <g:set var="participantPerpetualAccessToTitle"
                       value="${surveyService.hasParticipantPerpetualAccessToTitle(subscriberSubs, tipp)}"/>
                <g:set var="allowedToSelect"
                       value="${!(participantPerpetualAccessToTitle) && (!ieInNewSub || (ieInNewSub && (ieInNewSub.acceptStatus == RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION || contextOrg.id == surveyConfig.surveyInfo.owner.id)))}"/>
            </g:if>
            <g:else>
                <g:set var="allowedToSelect"
                       value="${!ieInNewSub || (ieInNewSub && (ieInNewSub.acceptStatus == RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION || contextOrg.id == surveyConfig.surveyInfo.owner.id))}"/>
            </g:else>
            <tr data-gokbId="${tipp.gokbId}" data-tippId="${tipp.id}" data-ieId="${ie.id}" data-index="${counter}" class="${checkedCache ? (checkedCache[ie.id.toString()] ? 'positive' : '') : ''}">
                <td>

                    <g:if test="${(params.tab == 'previousIEs' || params.tab == 'allIEs' || params.tab == 'toBeSelectedIEs' || params.tab == 'currentIEs') && (editable && !ieInNewSub && allowedToSelect)}">
                        <input type="checkbox" name="bulkflag" class="bulkcheck" ${checkedCache ? checkedCache[ie.id.toString()] : ''}>
                    </g:if>
                    <g:elseif test="${editable && allowedToSelect && params.tab == 'selectedIEs'}">
                        <input type="checkbox" name="bulkflag" class="bulkcheck" ${checkedCache ? checkedCache[ie.id.toString()] : ''}>
                    </g:elseif>

                </td>
                <td>${counter++}</td>
                <td class="titleCell">

                    <g:if test="${params.tab != 'currentIEs' && ieInNewSub && ieInNewSub.acceptStatus == RDStore.IE_ACCEPT_STATUS_FIXED}">
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
                        <ui:ieAcceptStatusIcon status="${ieInNewSub.acceptStatus}"/>
                    </g:if>

                    <!-- START TEMPLATE -->
                        <laser:render template="/templates/title_short"
                                  model="${[ie: ie, tipp: ie.tipp,
                                            showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false, overwriteEditable: false, participantPerpetualAccessToTitle: (participantPerpetualAccessToTitle)]}"/>
                    <!-- END TEMPLATE -->
                </td>
                <td>
                    <g:if test="${ieInNewSub?.priceItems}">
                            <g:each in="${ieInNewSub.priceItems}" var="priceItem" status="i">
                                <g:message code="tipp.price.listPrice"/>:%{-- <ui:xEditable field="listPrice"
                                                                                           owner="${priceItem}"
                                                                                           format=""
                                                                                           overwriteEditable="${false}"/>
                                <ui:xEditableRefData
                                        field="listCurrency" owner="${priceItem}"
                                        config="Currency"
                                        overwriteEditable="${false}"/>--}% <g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/><br/>

                                <g:if test="${priceItem.localCurrency}">
                                    <g:message code="tipp.price.localPrice"/>: %{--<ui:xEditable field="localPrice"
                                                                                                owner="${priceItem}"
                                                                                                overwriteEditable="${false}"/>
                                    <ui:xEditableRefData
                                            field="localCurrency" owner="${priceItem}"
                                            config="Currency"
                                            overwriteEditable="${false}"/>--}% <g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>
                                <%--<ui:xEditable field="startDate" type="date"
                                                 owner="${priceItem}"/><ui:dateDevider/><ui:xEditable
                                    field="endDate" type="date"
                                    owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                </g:if>
                                <g:if test="${i < ieInNewSub.priceItems.size() - 1}"><hr></g:if>
                                <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                <g:set var="sumlocalPrice" value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                            </g:each>
                    </g:if>
                    <g:else>
                        <g:if test="${ie?.priceItems}">
                            <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                <g:message code="tipp.price.listPrice"/>: %{--<ui:xEditable field="listPrice"
                                                                                           owner="${priceItem}"
                                                                                           format=""
                                                                                           overwriteEditable="${false}"/> <ui:xEditableRefData
                                    field="listCurrency" owner="${priceItem}"
                                    config="Currency"
                                    overwriteEditable="${false}"/>--}% <g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/><br/>

                                <g:if test="${priceItem.localCurrency}">
                                    <g:message code="tipp.price.localPrice"/>: %{--<ui:xEditable field="localPrice"
                                                                                                owner="${priceItem}"
                                                                                                overwriteEditable="${false}"/> <ui:xEditableRefData
                                        field="localCurrency" owner="${priceItem}"
                                        config="Currency"
                                        overwriteEditable="${false}"/>--}% <g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>
                                <%--<ui:xEditable field="startDate" type="date"
                                                 owner="${priceItem}"/><ui:dateDevider/><ui:xEditable
                                    field="endDate" type="date"
                                    owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                </g:if>
                                <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                <g:set var="sumlocalPrice" value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                            </g:each>
                        </g:if>
                    </g:else>
                </td>
                <td>
                    <g:if test="${(params.tab == 'allIEs' || params.tab == 'selectedIEs' || params.tab == 'toBeSelectedIEs') && editable && ieInNewSub && allowedToSelect}">
                        <g:link class="ui icon positive button la-popup-tooltip la-delay"
                                action="processRemoveIssueEntitlementsSurvey"
                                params="${[id: newSub.id, singleTitle: ieInNewSub.id, packageId: packageId, surveyConfigID: surveyConfig?.id]}"
                                data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                            <i class="check icon"></i>
                        </g:link>
                    </g:if>


                    <g:if test="${(params.tab == 'allIEs'|| params.tab == 'currentIEs' || params.tab == 'toBeSelectedIEs') && editable && !ieInNewSub && allowedToSelect }">
                        <g:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay"
                                action="processAddIssueEntitlementsSurvey"
                                params="${[id: newSub.id, singleTitle: ie.id, surveyConfigID: surveyConfig?.id]}"
                                data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                            <i class="plus icon"></i>
                        </g:link>
                    </g:if>
                </td>
            </tr>

        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <th></th>
            <th></th>
            <th></th>
            <th><g:message code="financials.export.sums"/> <br />
                <g:message code="tipp.price.listPrice"/>: <g:formatNumber number="${sumlistPrice}" type="currency"/><br />
                %{--<g:message code="tipp.price.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
            </th>
            <th></th>
        </tr>
        </tfoot>
    </table>
</div>
