<%@ page import="de.laser.IssueEntitlementGroup; de.laser.storage.RDStore; de.laser.remote.ApiSource" %>
<div class="sixteen wide column">
    <g:set var="counter" value="${offset + 1}"/>
    <g:set var="sumlistPriceEuro" value="${0}"/>
    <g:set var="sumlistPriceUSD" value="${0}"/>
    <g:set var="sumlistPriceGBP" value="${0}"/>
    %{-- <g:set var="sumlocalPrice" value="${0}"/>--}%

    <div class="ui accordion la-accordion-showMore" id="surveyEntitlements">
        <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>
        <g:each in="${titlesList}" var="tipp">
            <g:set var="ieInNewSub"
                   value="${surveyService.titleContainedBySubscription(subscriberSub, tipp)}"/>
            <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                <g:set var="participantPerpetualAccessToTitle"
                       value="${surveyService.hasParticipantPerpetualAccessToTitle3(subscriber, tipp)}"/>
                <g:set var="allowedToSelect"
                       value="${!(participantPerpetualAccessToTitle) && (!ieInNewSub)}"/>
            </g:if>
            <g:else>
                <g:set var="allowedToSelect"
                       value="${!ieInNewSub}"/>
            </g:else>

            <div class="ui raised segments la-accordion-segments">

                <div class="ui fluid segment title" >

                    <div class="ui stackable equal width grid la-js-checkItem" data-gokbId="${tipp.gokbId}" data-tippId="${tipp.id}" data-index="${counter}">
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
                            <g:if test="${(params.tab == 'allTipps') && (editable && !ieInNewSub && allowedToSelect)}">
                                <input type="checkbox" name="bulkflag"
                                       class="bulkcheck la-js-notOpenAccordion la-vertical-centered" ${checkedCache ? checkedCache[tipp.id.toString()] : ''}>
                            </g:if>
                        </div>


                        <div class="one wide column">
                            <span class="la-vertical-centered">${counter++}</span>
                        </div>

                        <div class="column">
                            <div class="ui list">
                                <!-- START TEMPLATE -->
                                <laser:render
                                        template="/templates/title_short_accordion"
                                        model="${[tipp: tipp,
                                                  showPackage: true, showPlattform: true, showEmptyFields: false, sub: subscriberSub.id]}"/>
                                <!-- END TEMPLATE -->

                            </div>
                        </div>

                        <div class="column">
                            <laser:render template="/templates/tipps/coverages_accordion"
                                          model="${[tipp: tipp, overwriteEditable: false]}"/>
                        </div>

                        <div class="four wide column">

                            <!-- START TEMPLATE -->
                            <laser:render template="/templates/identifier"
                                          model="${[tipp: tipp]}"/>
                            <!-- END TEMPLATE -->
                        </div>

                        <div class="two wide column">
                                <g:if test="${tipp.priceItems}">
                                    <g:each in="${tipp.priceItems}" var="priceItem" status="i">
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
                                        <g:if test="${priceItem.listPrice && (i < tipp.priceItems.size() - 1)}">
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

                                <div class="ui icon blue button la-modern-button "><i
                                        class="ui angle double down icon"></i>
                                </div>
                                <g:if test="${(params.tab == 'allTipps') && editable && ieInNewSub && de.laser.IssueEntitlementGroupItem.findByIeAndIeGroup(ieInNewSub, de.laser.IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscriberSub))}">
                                    <g:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay"
                                            action="processRemoveIssueEntitlementsSurvey"
                                            params="${[id: subscriberSub.id, singleTitle: ieInNewSub.id, packageId: packageId, surveyConfigID: surveyConfig?.id]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                        <i class="shopping basket icon"></i>
                                    </g:link>
                                </g:if>


                                <g:if test="${(params.tab == 'allTipps') && editable && !ieInNewSub && allowedToSelect}">
                                    <g:link class="ui icon negative button la-modern-button la-popup-tooltip la-delay"
                                            action="processAddIssueEntitlementsSurvey"
                                            params="${[id: subscriberSub.id, singleTitle: tipp.id, surveyConfigID: surveyConfig?.id]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                                        <i class="la-basket-shopping slash icon"></i>
                                    </g:link>
                                </g:if>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                    <div class="ui stackable grid" data-ajaxTarget="true">

                        <laser:render template="/templates/title_long_accordion"
                                      model="${[tipp: tipp,
                                                showPackage: true, showPlattform: true, showEmptyFields: false]}"/>




                        <div class="three wide column">
                            <div class="ui list la-label-list">
                                <g:if test="${tipp.accessStartDate}">
                                    <div class="ui label la-label-accordion">${message(code: 'tipp.access')}</div>

                                    <div class="item">
                                        <div class="content">
                                            <g:formatDate
                                                    format="${message(code: 'default.date.format.notime')}"
                                                    date="${tipp.accessStartDate}"/>
                                        </div>
                                    </div>

                                </g:if>
                                <g:if test="${tipp.accessEndDate}">
                                    <!-- bis -->
                                    <!-- DEVIDER  -->
                                    <ui:dateDevider/>
                                    <div class="item">
                                        <div class="content">
                                            <g:formatDate
                                                    format="${message(code: 'default.date.format.notime')}"
                                                    date="${tipp.accessEndDate}"/>
                                        </div>
                                    </div>
                                </g:if>

                            <%-- Coverage Details START --%>
                                <g:each in="${tipp.coverages}" var="covStmt" status="counterCoverage">
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
                                            <i class="grey icon file alternate right la-popup-tooltip la-delay"
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
               %{-- <div class="item">
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

            </div>
        </div>

</div>
