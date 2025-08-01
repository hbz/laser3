<%@ page import="de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.storage.PropertyStore; de.laser.properties.SubscriptionProperty; de.laser.Subscription; de.laser.storage.RDStore; de.laser.ui.Btn; de.laser.finance.CostItem; de.laser.ui.Icon" %>
<laser:serviceInjection/>
<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
        <table class="ui celled sortable table la-table la-js-responsive-table">
            <thead>
            <tr>
                <g:if test="${tmplShowCheckbox}">
                    <th scope="col" rowspan="2" class="center aligned">
                        <g:checkBox name="subListToggler" id="subListToggler" checked="false"/>
                    </th>
                </g:if>
                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <th class="center aligned" rowspan="2" scope="col">
                    <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                          data-content="${message(code: 'default.previous.label')}">
                        <i class="${Icon.LNK.PREV}"></i>
                    </span>
                </th>
                <g:sortableColumn params="${params}" property="s.name"
                                  title="${message(code: 'subscription.slash.name')}"
                                  rowspan="2" scope="col"/>
                <th class="center aligned" rowspan="2" scope="col">
                    <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                          data-content="${message(code: 'default.next.label')}">
                        <i class="${Icon.LNK.NEXT}"></i>
                    </span>
                </th>
                <th rowspan="2" scope="col">
                    ${message(code: 'package.plural')}
                </th>

                <g:sortableColumn scope="col" params="${params}" property="provider" title="${message(code: 'provider.label')}" rowspan="2"/>
                <g:sortableColumn scope="col" params="${params}" property="vendor" title="${message(code: 'vendor.label')}" rowspan="2"/>
                <th rowspan="2" class="center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code: 'subscription.invoice.processing')}" data-position="top right">
                        <i class="${Icon.ATTR.SUBSCRIPTION_INVOICE_PROCESSING} large"></i>
                    </span>
                </th>

                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate"
                                  title="${message(code: 'subscription.startDate.label')}"/>


                <th scope="col" rowspan="2" class="center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code: 'subscription.numberOfLicenses.label')}" data-position="top center">
                        <i class="${Icon.AUTH.ORG_INST} large"></i>
                    </span>
                </th>
                <th scope="col" rowspan="2" class="center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code: 'subscription.numberOfCostItems.label')}" data-position="top center">
                        <i class="${Icon.FNC.COST} large"></i>
                    </span>
                </th>

                <th rowspan="2" class="two wide center aligned">
                    <ui:optionsIcon/>
                </th>
            </tr>

            <tr>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate"
                                  title="${message(code: 'subscription.endDate.label')}"/>
            </tr>
            </thead>
            <g:each in="${subscriptions}" var="s" status="i">
                <tr>
                    <g:if test="${tmplShowCheckbox}">
                        <td class="center aligned">
                            <g:checkBox id="selectedSubs_${s.id}" name="selectedSubs" value="${s.id}" checked="false"/>
                        </td>
                    </g:if>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <%
                        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(s, false)
                        Long navPrevSub = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                        Long navNextSub = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
                    %>
                    <td class="center aligned">
                        <g:if test="${navPrevSub}">
                            <g:link controller="subscription" action="show" id="${navPrevSub}"><i class="${Icon.LNK.PREV}"></i></g:link>
                        </g:if>
                    </td>
                    <th scope="row" class="la-th-column">
                        <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                            <g:if test="${s.name}">
                                ${s.name}
                                <g:if test="${s?.referenceYear}">
                                    ( ${s.referenceYear} )
                                </g:if>
                            </g:if>
                            <g:else>
                                -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                            </g:else>
                        </g:link>
                        <g:each in="${allLinkedLicenses}" var="row">
                            <g:if test="${s == row.destinationSubscription}">
                                <g:set var="license" value="${row.sourceLicense}"/>
                                <div class="la-flexbox la-minor-object">
                                    <i class="${Icon.LICENSE} la-list-icon"></i>
                                    <g:link controller="license" action="show" id="${license.id}">
                                        ${license.reference}
                                    </g:link><br/>
                                </div>
                            </g:if>
                        </g:each>
                    </th>
                    <td class="center aligned">
                        <g:if test="${navNextSub}">
                            <g:link controller="subscription" action="show" id="${navNextSub}"><i class="${Icon.LNK.NEXT}"></i></g:link>
                        </g:if>
                    </td>
                    <td>
                    <!-- packages -->
                        <g:each in="${s.packages}" var="sp" status="ind">
                            <g:if test="${ind < 10}">
                                <div class="la-flexbox">
                                    <g:if test="${s.packages.size() > 1}">
                                        <i class="${Icon.PACKAGE} la-list-icon"></i>
                                    </g:if>
                                    <g:link controller="subscription" action="index" id="${s.id}" params="[pkgfilter: sp.pkg.id]"
                                            title="${sp.pkg.provider?.name}">
                                        ${sp.pkg.name}
                                    </g:link>
                                </div>
                            </g:if>
                        </g:each>

                        <g:if test="${s.packages.size() > 10}">
                            <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                        </g:if>
                    <!-- packages -->
                    </td>
                    <td>
                        <g:each in="${s.providers}" var="provider">
                            <g:link controller="provider" action="show" id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                                <g:if test="${provider.abbreviatedName}">
                                    <br/> (${fieldValue(bean: provider, field: "abbreviatedName")})
                                </g:if>
                            </g:link><br/>
                        </g:each>
                    </td>

                    <td>
                        <g:each in="${s.vendors}" var="vendor">
                            <g:link controller="vendor" action="show" id="${vendor.id}">
                                ${fieldValue(bean: vendor, field: "name")}
                                <g:if test="${vendor.abbreviatedName}">
                                    <br/> (${fieldValue(bean: vendor, field: "abbreviatedName")})
                                </g:if>
                            </g:link><br/>
                        </g:each>
                    </td>

                    <td>
                        <% SubscriptionProperty invoicingProp = SubscriptionProperty.findByOwnerAndType(s, PropertyStore.SUB_PROP_INVOICE_PROCESSING) %>
                        <g:if test="${invoicingProp}">
                            <g:if test="${invoicingProp.refValue == RDStore.INVOICE_PROCESSING_CONSORTIUM}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${invoicingProp.getValueInI10n()}"><i
                                        class="${Icon.AUTH.ORG_CONSORTIUM}"></i></span>
                            </g:if>
                            <g:elseif test="${invoicingProp.refValue == RDStore.INVOICE_PROCESSING_PROVIDER}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${invoicingProp.getValueInI10n()}"><i
                                        class="${Icon.PROVIDER}"></i></span>
                            </g:elseif>
                            <g:elseif test="${invoicingProp.refValue == RDStore.INVOICE_PROCESSING_PROVIDER_OR_VENDOR}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${invoicingProp.getValueInI10n()}"><i
                                        class="${Icon.PROVIDER}"></i> / <i class="${Icon.VENDOR}"></i></span>
                            </g:elseif>
                            <g:elseif test="${invoicingProp.refValue == RDStore.INVOICE_PROCESSING_VENDOR}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${invoicingProp.getValueInI10n()}"><i
                                        class="${Icon.VENDOR}"></i></span>
                            </g:elseif>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                        <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label.shy')}:"><g:formatDate
                                formatName="default.date.format.notime" date="${s.endDate}"/></span>
                    </td>

                    <g:set var="childSubIds" value="${Subscription.executeQuery('select s.id from Subscription s where s.instanceOf = :parent', [parent: s])}"/>
                    <td>
                        <g:if test="${childSubIds.size() > 0}">
                            <g:link controller="subscription" action="members" params="${[id: s.id]}">
                                <ui:bubble count="${childSubIds.size()}"/>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="subscription" action="addMembers" params="${[id: s.id]}">
                                <ui:bubble count="${childSubIds.size()}"/>
                            </g:link>
                        </g:else>
                    </td>
                    <td>
                        <g:link mapping="subfinance" controller="finance" action="index" params="${[sub: s.id]}">
                            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                <ui:bubble
                                        count="${childSubIds.isEmpty() ? 0 : CostItem.executeQuery('select count(*) from CostItem ci where ci.sub.id in (:subs) and ci.owner = :context and ci.costItemStatus != :deleted', [subs: childSubIds, context: contextService.getOrg(), deleted: RDStore.COST_ITEM_DELETED])[0]}"/>
                            </g:if>
                        </g:link>
                    </td>
                    <td class="x">
                        <g:if test="${actionName == 'createSubscriptionSurvey' && editable && contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO )}">
                            <g:link class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                    data-content="${message(code: 'survey.toggleSurveySub.add.label', args:[SurveyConfig.countBySubscriptionAndSubSurveyUseForTransfer(s, true), SurveyConfig.countBySubscriptionAndSubSurveyUseForTransfer(s, false)])}"
                                    controller="survey" action="addSubtoSubscriptionSurvey"
                                    params="[sub: s.id]">
                                <i class="${Icon.CMD.EDIT}"></i>
                            </g:link>

                        </g:if>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <g:if test="${surveySubscriptionsCount == 0}">
            <strong><g:message code="surveySubscriptions.addSubscriptionsOverPencil"/></strong>
        </g:if>
        <g:elseif test="${filterSet}">
            <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
        </g:elseif>
        <g:else>
            <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
        </g:else>
    </g:else>
</div>

<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#subListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
            }
        })
    </laser:script>

</g:if>