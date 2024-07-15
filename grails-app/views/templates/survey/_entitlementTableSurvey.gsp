<%@ page import="de.laser.helper.Icons; de.laser.IssueEntitlementGroup; de.laser.storage.RDStore; de.laser.remote.ApiSource" %>
<div class="sixteen wide column">
    <g:set var="counter" value="${offset + 1}"/>
    <g:set var="sumlistPriceEuro" value="${0}"/>
    <g:set var="sumlistPriceUSD" value="${0}"/>
    <g:set var="sumlistPriceGBP" value="${0}"/>
   %{-- <g:set var="sumlocalPrice" value="${0}"/>--}%



    <div class="ui accordion la-accordion-showMore" id="surveyEntitlements">
        <g:if test="${editable && params.tab == 'selectedIEs'}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>
        <g:each in="${ies.sourceIEs}" var="ie">

            <g:set var="tipp" value="${ie.tipp}"/>
            <g:set var="ieInNewSub"
                   value="${listOfStatus ? surveyService.titleContainedBySubscription(subscriberSub, tipp, listOfStatus) : null}"/>
            <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                <g:set var="participantPerpetualAccessToTitle"
                       value="${surveyService.hasParticipantPerpetualAccessToTitle3(subscriber, tipp)}"/>
                %{--<g:set var="allowedToSelect"
                       value="${!(participantPerpetualAccessToTitle) && (!ieInNewSub)}"/>--}%
            </g:if>
           %{-- <g:else>
                <g:set var="allowedToSelect"
                       value="${!ieInNewSub}"/>
            </g:else>--}%

            <div class="ui raised segments la-accordion-segments">

                <div class="ui fluid segment title" >




                    <div class="ui stackable equal width grid la-js-checkItem" data-gokbId="${tipp.gokbId}" data-tippId="${tipp.id}" data-ieId="${ie.id}" data-index="${counter}">
                        <g:if test="${participantPerpetualAccessToTitle}">
                            <g:set var="participantPerpetualAccessToTitleList"
                                   value="${surveyService.listParticipantPerpetualAccessToTitle(subscriber, tipp)}"/>
                            <span class="ui mini left corner label la-perpetualAccess la-popup-tooltip la-delay"
                                  data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')} ${participantPerpetualAccessToTitleList.collect{it.getPermanentTitleInfo(contextOrg)}.join(',')}"
                                  data-position="left center" data-variation="tiny">
                                <i class="star icon"></i>
                            </span>
                        </g:if>
                        <div class="one wide column">
                            <g:if test="${editable && params.tab == 'selectedIEs' && ieInNewSub && de.laser.IssueEntitlementGroupItem.findByIeAndIeGroup(ieInNewSub, de.laser.IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscriberSub))}">
                                <input type="checkbox" name="bulkflag"
                                       class="bulkcheck la-vertical-centered la-js-notOpenAccordion" ${checkedCache ? checkedCache[ie.id.toString()] : ''}>
                            </g:if>
                        </div>


                        <div class="one wide column">
                            <span class="la-vertical-centered">${counter++}</span>
                        </div>

                        <div class="column">
                            <div class="ui list">
                                <!-- START TEMPLATE -->
                                <laser:render
                                        template="/templates/titles/title_short_accordion"
                                        model="${[ie         : ie, tipp: ie.tipp,
                                                  showPackage: true, showPlattform: true, showEmptyFields: false, sub: subscriberSub.id]}"/>
                                <!-- END TEMPLATE -->

                            </div>
                        </div>

                        <div class="column">
                            <laser:render template="/templates/tipps/coverages_accordion"
                                          model="${[ie: null, tipp: ie.tipp, overwriteEditable: false]}"/>
                        </div>

                        <div class="four wide column">

                            <!-- START TEMPLATE -->
                            <laser:render template="/templates/identifier"
                                          model="${[tipp: ie.tipp]}"/>
                            <!-- END TEMPLATE -->
                        </div>

                        <div class="two wide column">
                            <g:if test="${ie.tipp.priceItems}">
                                <g:each in="${ie.tipp.priceItems}" var="priceItem" status="i">
                                    <div class="ui list">
                                        <g:if test="${priceItem.listPrice}">
                                            <div class="item">
                                                <div class="contet">
                                                    <div class="header">
                                                        <g:message code="tipp.price.listPrice"/>
                                                    </div>

                                                    <div class="content">
                                                        <g:formatNumber number="${priceItem.listPrice}" type="currency"
                                                                        currencyCode="${priceItem.listCurrency?.value}"
                                                                        currencySymbol="${priceItem.listCurrency?.value}"/>
                                                    </div>
                                                </div>
                                            </div>
                                        </g:if>
                                    </div>
                                    <g:if test="${priceItem.listPrice && (i < ie.tipp.priceItems.size() - 1)}">
                                        <hr>
                                    </g:if>
                                    <g:if test="${priceItem.listCurrency == RDStore.CURRENCY_EUR}">
                                        <g:set var="sumlistPriceEuro" value="${sumlistPriceEuro + (priceItem.listPrice ?: 0)}"/>
                                    </g:if>
                                    <g:if test="${priceItem.listCurrency == RDStore.CURRENCY_USD}">
                                        <g:set var="sumlistPriceUSD" value="${sumlistPriceUSD + (priceItem.listPrice ?: 0)}"/>
                                    </g:if>
                                    <g:if test="${priceItem.listCurrency == RDStore.CURRENCY_GBP}">
                                        <g:set var="sumlistPriceGBP" value="${sumlistPriceGBP + (priceItem.listPrice ?: 0)}"/>
                                    </g:if>
                                %{--<g:set var="sumlocalPrice"
                                       value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>--}%
                                </g:each>
                            </g:if>
                        </div>

                        <div class="one wide column">
                            <div class="ui right floated buttons">
                                <div class="right aligned wide column">

                                </div>

                                <div class="ui icon blue button la-modern-button">
                                    <i class="${Icons.CMD.SHOW_MORE}"></i>
                                </div>
                                <g:if test="${(params.tab == 'selectedIEs') && editable && ieInNewSub && de.laser.IssueEntitlementGroupItem.findByIeAndIeGroup(ieInNewSub, de.laser.IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscriberSub))}">
                                    <g:link class="ui icon negative button la-modern-button la-popup-tooltip la-delay"
                                            action="processRemoveIssueEntitlementsSurvey"
                                            params="${[id: subscriberSub.id, singleTitle: ieInNewSub.id, packageId: packageId, surveyConfigID: surveyConfig?.id]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                        <i class="la-basket-shopping slash icon"></i>
                                    </g:link>
                                </g:if>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                    <div class="ui stackable grid" data-ajaxTarget="true">

                        <laser:render template="/templates/titles/title_long_accordion"
                                      model="${[ie         : ie, tipp: ie.tipp,
                                                showPackage: true, showPlattform: true, showEmptyFields: false]}"/>




                        <div class="three wide column">
                            <div class="ui list la-label-list">
                                <g:if test="${ie.tipp.accessStartDate}">
                                    <div class="ui label la-label-accordion">${message(code: 'tipp.access')}</div>

                                    <div class="item">
                                        <div class="content">
                                            <g:formatDate
                                                    format="${message(code: 'default.date.format.notime')}"
                                                    date="${ie.tipp.accessStartDate}"/>
                                        </div>
                                    </div>

                                </g:if>
                                <g:if test="${ie.tipp.accessEndDate}">
                                    <!-- bis -->
                                    <!-- DEVIDER  -->
                                    <ui:dateDevider/>
                                    <div class="item">
                                        <div class="content">
                                            <g:formatDate
                                                    format="${message(code: 'default.date.format.notime')}"
                                                    date="${ie.tipp.accessEndDate}"/>
                                        </div>
                                    </div>
                                </g:if>

                            <%-- Coverage Details START --%>
                                <g:each in="${ie.tipp.coverages}" var="covStmt" status="counterCoverage">
                                    <g:if test="${covStmt.coverageNote || covStmt.coverageDepth || covStmt.embargo}">
                                        <div class="ui label la-label-accordion">${message(code: 'tipp.coverageDetails')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
                                    </g:if>
                                    <g:if test="${covStmt.coverageNote}">
                                        <div class="item">
                                            <i class="grey icon quote right la-popup-tooltip la-delay"
                                               data-content="${message(code: 'default.note.label')}"></i>

                                            <div class="content">
                                                <div class="header">
                                                    ${message(code: 'default.note.label')}
                                                </div>

                                                <div class="description">
                                                    ${covStmt.coverageNote}
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                    <g:if test="${covStmt.coverageDepth}">
                                        <div class="item">
                                            <i class="grey ${Icons.TIPP_COVERAGE_DEPTH} right la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.coverageDepth')}"></i>

                                            <div class="content">
                                                <div class="header">
                                                    ${message(code: 'tipp.coverageDepth')}
                                                </div>

                                                <div class="description">
                                                    ${covStmt.coverageDepth}
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                    <g:if test="${covStmt.embargo}">
                                        <div class="item">
                                            <i class="grey icon hand paper right la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.embargo')}"></i>

                                            <div class="content">
                                                <div class="header">
                                                    ${message(code: 'tipp.embargo')}
                                                </div>

                                                <div class="description">
                                                    ${covStmt.embargo}
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                </g:each>
                            <%-- Coverage Details END --%>
                            </div>
                        </div>
                        <%-- My Area START--%>
                        %{--                                <div class="seven wide column">
                                                            <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip la-delay"
                                                               data-content="${message(code: 'tipp.tooltip.myArea')}"></i>

                                                            <div class="ui la-segment-with-icon">

                                                                <laser:render template="/templates/tipps/coverages_accordion"
                                                                              model="${[ie: ie, tipp: ie.tipp]}"/>

                                                                <div class="ui list">
                                                                    <g:if test="${ie}">
                                                                        <div class="item">
                                                                            <i class="grey save icon la-popup-tooltip la-delay"
                                                                               data-content="${message(code: 'issueEntitlement.perpetualAccessBySub.label')}"></i>

                                                                            <div class="content">
                                                                                <div class="header">
                                                                                    ${showCompact ? '' : message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}
                                                                                </div>

                                                                                <div class="description">
                                                                                    <ui:xEditableBoolean owner="${subscription}"
                                                                                                         field="hasPerpetualAccess"/>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                        <div class="item">
                                                                            <i class="grey ${Icons.CMD.EDIT} la-popup-tooltip la-delay"
                                                                               data-content="${message(code: 'issueEntitlement.myNotes')}"></i>
                                                                            <div class="content">
                                                                                <div class="header"><g:message code="issueEntitlement.myNotes"/></div>
                                                                                <div class="description">
                                                                                    <ui:xEditable owner="${ie}" type="text"
                                                                                                  field="notes"/>
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </g:if>

                                                                    <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                                                        <div class="item">
                                                                            <i class="${Icons.FNC.COST_CONFIG} grey la-popup-tooltip la-delay"></i>

                                                                            <div class="content">
                                                                                <div class="header"><g:message
                                                                                        code="tipp.price.localPrice"/>:</div>

                                                                                <div class="description">
                                                                                    <ui:xEditable field="localPrice"
                                                                                                  owner="${priceItem}"/>
                                                                                    <ui:xEditableRefData
                                                                                            field="localCurrency"
                                                                                            owner="${priceItem}"
                                                                                            config="Currency"/>
                                                                                    <g:if test="${editable}">
                                                                                        <span class="right floated">
                                                                                            <g:link controller="subscription"
                                                                                                    action="removePriceItem"
                                                                                                    params="${[priceItem: priceItem.id, id: subscription.id]}"
                                                                                                    class="ui compact icon button tiny"><i
                                                                                                    class="${Icons.CMD.REMOVE}"
                                                                                                    data-content="Preis entfernen"></i></g:link>
                                                                                        </span>
                                                                                    </g:if>
                                                                                </div>

                                                                            </div>
                                                                        </div>

                                                                        <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                                                    </g:each>
                                                                    <g:if test="${editable && ie.priceItems.size() < 1}">
                                                                        <g:link action="addEmptyPriceItem"
                                                                                class="ui tiny button"
                                                                                params="${[ieid: ie.id, id: subscription.id]}">
                                                                            <i class="${Icons.FNC.COST_CONFIG}"></i>${message(code: 'subscription.details.addEmptyPriceItem.info')}
                                                                        </g:link>
                                                                    </g:if>

                                                                <%-- GROUPS START--%>
                                                                    <g:if test="${subscription.ieGroups.size() > 0}">
                                                                        <g:each in="${ie.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                                                                            <div class="item">
                                                                                <i class="grey icon object group la-popup-tooltip la-delay"
                                                                                   data-content="${message(code: 'issueEntitlementGroup.label')}"></i>
                                                                                <div class="content">
                                                                                    <div class="header"><g:message code="subscription.details.ieGroups"/></div>
                                                                                    <div class="description"><g:link controller="subscription" action="index"
                                                                                                                     id="${subscription.id}"
                                                                                                                     params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                                                                                    </div>
                                                                                </div>
                                                                            </div>
                                                                            <g:if test="${editable}">
                                                                                <g:link action="editEntitlementGroupItem"
                                                                                        params="${[cmd: 'edit', ie: ie.id, id: subscription.id]}"
                                                                                        class="ui tiny button">
                                                                                    <i class="object group icon"></i>${message(code: 'subscription.details.ieGroups.edit')}
                                                                                </g:link>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>


                                                                <%-- GROUPS END--%>
                                                                </div>
                                                            </div>
                                                        </div>--}%
                        <%-- My Area END --%>
                    </div><%-- .grid --%>
                </div><%-- .segment --%>
            </div><%--.segments --%>
        </g:each>
    </div><%-- .accordions --%>
    <div class="ui segment grid la-filter">
        <div class="twelve wide column ">
        </div>
        <div class="four wide column ">
            <div class="ui list">
                %{--<div class="item">
                    <div class="contet">
                            <g:message code="renewEntitlementsWithSurvey.totalCostSelected"/> <br/>
                    </div>
                </div>--}%


                <g:if test="${sumlistPriceEuro > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCostOnPage"/>:</strong> <g:formatNumber
                                number="${sumlistPriceEuro}" type="currency" currencyCode="EUR"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${sumlistPriceUSD > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCostOnPage"/>:</strong> <g:formatNumber
                                number="${sumlistPriceUSD}" type="currency" currencyCode="USD"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${sumlistPriceGBP > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCostOnPage"/>:</strong> <g:formatNumber
                                number="${sumlistPriceGBP}" type="currency" currencyCode="GBP"/><br/>
                        </div>
                    </div>
                </g:if>
                %{--<g:message code="tipp.price.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
                <g:if test="${tippsListPriceSumEUR > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCost"/>:</strong> <g:formatNumber
                                number="${tippsListPriceSumEUR}" type="currency" currencyCode="EUR"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${tippsListPriceSumUSD > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCost"/>:</strong> <g:formatNumber
                                number="${tippsListPriceSumUSD}" type="currency" currencyCode="USD"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${tippsListPriceSumGBP > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCost"/>:</strong> <g:formatNumber
                                number="${tippsListPriceSumGBP}" type="currency" currencyCode="GBP"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${iesTotalListPriceSumEUR > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCost"/>:</strong> <g:formatNumber
                                number="${iesTotalListPriceSumEUR}" type="currency" currencyCode="EUR"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${iesTotalListPriceSumUSD > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCost"/>:</strong> <g:formatNumber
                                number="${iesTotalListPriceSumUSD}" type="currency" currencyCode="USD"/><br/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${iesTotalListPriceSumGBP > 0}">
                    <div class="item">
                        <div class="contet">
                            <strong><g:message code="renewEntitlementsWithSurvey.totalCost"/>:</strong> <g:formatNumber
                                number="${iesTotalListPriceSumGBP}" type="currency" currencyCode="GBP"/><br/>
                        </div>
                    </div>
                </g:if>

            </div>
        </div>

</div>
