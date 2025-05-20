<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Subscription; de.laser.Org; de.laser.OrgRole; de.laser.FormService; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Links;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.finance.CostItem" %>
<laser:serviceInjection />

<g:if test="${params.member}">
    <g:set var="chosenOrg" value="${Org.findById(params.member)}" />
    <g:if test="${chosenOrg}">
        <g:set var="chosenOrgCPAs" value="${chosenOrg.getGeneralContactPersons(false)}" />
        <table class="ui table la-table compact">
            <tbody>
            <tr>
                <td>
                    <p>
                        <strong>
                            <ui:archiveIcon org="${chosenOrg}" />
                            <g:link controller="organisation" action="show" id="${chosenOrg.id}">${chosenOrg.name}</g:link>
                            <ui:customerTypeOnlyProIcon org="${chosenOrg}" />
                        </strong>
                    </p>
                    ${chosenOrg.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${chosenOrgCPAs}">
                        <g:each in="${chosenOrgCPAs}" var="gcp">
                            <laser:render template="/addressbook/person_details" model="${[person: gcp, tmplHideLinkToAddressbook: true, overwriteEditable: false]}" />
                        </g:each>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </g:if>
</g:if>
<g:if test="${entries}">
    <table class="ui celled sortable table la-js-responsive-table la-table la-ignore-fixed">
        <thead>
        <tr>
            <th rowspan="2" class="center aligned">${message(code:'sidewide.number')}</th>
            <g:sortableColumn property="roleT.org.sortname" params="${params}" title="${message(code:'myinst.consortiaSubscriptions.member')}" rowspan="2" />
            <th class="center aligned la-smaller-table-head"  rowspan="2" >
                <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                      data-content="${message(code: 'default.previous.label')}">
                    <i class="${Icon.LNK.PREV}"></i>
                </span>
            </th>
            <g:sortableColumn property="subT.name" params="${params}" title="${message(code:'default.subscription.label')}" class="la-smaller-table-head" />
            <th class="center aligned la-smaller-table-head" rowspan="2" >
                <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                      data-content="${message(code: 'default.next.label')}">
                    <i class="${Icon.LNK.NEXT}"></i>
                </span>
            </th>
            <g:if test="${'showPackages' in tableConfig}">
                <th rowspan="2">${message(code:'package.plural')}</th>
            </g:if>
            <g:if test="${'showProviders' in tableConfig}">
                <th rowspan="2">${message(code:'myinst.consortiaSubscriptions.provider')}</th>
            </g:if>
            <g:if test="${'showVendors' in tableConfig}">
                <th rowspan="2">${message(code:'myinst.consortiaSubscriptions.vendor')}</th>
            </g:if>
            <th rowspan="2">${message(code:'myinst.consortiaSubscriptions.runningTimes')}</th>
            <g:if test="${'withCostItems' in tableConfig}">
                <th rowspan="2">${message(code:'financials.amountFinal')}</th>
                <th rowspan="2" class="la-no-uppercase center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center">
                        <i class="${Icon.FNC.COST_CONFIG}"></i>
                    </span>&nbsp;/&nbsp;
                    <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                        <i class="${Icon.SIG.VISIBLE_ON}"></i>
                    </span>
                </th>
            </g:if>

            <th rowspan="2" class="la-no-uppercase center aligned">
                <ui:multiYearIcon isConsortial="true" />
            </th>
            <g:if test="${'onlyMemberSubs' in tableConfig}">
                <th class="center aligned" rowspan="2">
                    <ui:optionsIcon />
                </th>
            </g:if>
            <g:if test="${'showInfoFlyout' in tableConfig}">
                <th rowspan="2"></th>
            </g:if>
        </tr>
        <g:if test="${'withCostItems' in tableConfig}">
            <tr>
                <%-- sorting over links table is problematic because of complicated join over oid and n:n relation, needs ticket --%>
                <%--<g:sortableColumn property="licenseName" params="${params}" title="${message(code:'license.label')}" class="la-smaller-table-head" />--%>
                <th class="la-smaller-table-head">${message(code:'license.label')}</th>
            </tr>
        </g:if>
        </thead>
        <tbody>
        <g:each in="${entries}" var="entry" status="jj">
            <%
                CostItem ci
                Subscription subCons
                Org subscr
                if('withCostItems' in tableConfig) {
                    ci = (CostItem) entry.cost
                    subCons = (Subscription) entry.sub
                    subscr = (Org) entry.orgs
                }
                else if('onlyMemberSubs' in tableConfig) {
                    subCons = (Subscription) entry[0]
                    subscr = (Org) entry[1]
                }
            %>
            <tr class="${subscr.isArchived() ? 'warning' : ''}">
                <td>
                    ${ jj + 1 }
                </td>
                <td>
                    <ui:archiveIcon org="${subscr}" />
                    <g:link controller="organisation" action="show" id="${subscr.id}">
                        <g:if test="${subscr.sortname}">${subscr.sortname}</g:if>
                        (${subscr.name})
                    </g:link>
                    <g:if test="${subCons.orgRelations.find { OrgRole oo -> oo.org == subscr && oo.roleType == RDStore.OR_SUBSCRIBER_CONS_HIDDEN}}">
                        <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'financials.isNotVisibleForSubscriber')}">
                            <i class="low vision grey icon"></i>
                        </span>
                    </g:if>

                    <ui:customerTypeOnlyProIcon org="${subscr}" />
                </td>
                <%
                    LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(subCons,false)
                    Long navPrevSubMember = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                    Long navNextSubMember = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
                %>
                <td class="center aligned">
                    <g:if test="${navPrevSubMember}">
                        <g:link controller="subscription" action="show" id="${navPrevSubMember}"><i class="${Icon.LNK.PREV}"></i></g:link>
                    </g:if>
                </td>
                <th scope="row" class="la-th-column">
                    <div class="la-flexbox la-main-object">
                        <g:link controller="subscription" action="show" id="${subCons.id}">${subCons.name}</g:link>
                    </div>
                    <g:each in="${linkedLicenses.get(subCons)}" var="linkedLicense">
                        <div class="la-flexbox la-minor-object">
                            <i class="${Icon.LICENSE} la-list-icon"></i>
                            <g:link controller="license" action="show" id="${linkedLicense.id}">${linkedLicense.reference}</g:link><br />
                        </div>
                    </g:each>
                </th>
                <td class="center aligned">
                    <g:if test="${navNextSubMember}">
                        <g:link controller="subscription" action="show" id="${navNextSubMember}"><i class="${Icon.LNK.NEXT}"></i></g:link>
                    </g:if>
                </td>
                <g:if test="${'showPackages' in tableConfig}">
                    <td>
                        <g:each in="${subCons.packages}" var="subPkg">
                            <div class="la-flexbox">
                                <g:if test="${subCons.packages.size() > 1}">
                                    <i class="${Icon.PACKAGE} la-list-icon"></i>
                                </g:if>
                                <g:link controller="package" action="show" id="${subPkg.pkg.id}">${subPkg.pkg.name}</g:link>
                            </div>
                        </g:each>
                    </td>
                </g:if>
                <g:if test="${'showProviders' in tableConfig}">
                    <td>
                        <g:each in="${subCons.providers}" var="p">
                            <g:link controller="provider" action="show" id="${p.id}">${p.name}</g:link> <br />
                        </g:each>
                    </td>
                </g:if>
                <g:if test="${'showVendors' in tableConfig}">
                    <td>
                        <g:each in="${subCons.vendors}" var="v">
                            <g:link controller="vendor" action="show" id="${v.id}">${v.name}</g:link> <br />
                        </g:each>
                    </td>
                </g:if>
                <g:if test="${'withCostItems' in tableConfig}">
                    <td>
                        <%-- because of right join in query, ci may be missing, those are subscriptions where no cost items exist --%>
                        <g:if test="${ci?.id}">
                            <g:if test="${ci.getDerivedStartDate()}">
                                <g:formatDate date="${ci.getDerivedStartDate()}" format="${message(code:'default.date.format.notime')}"/>
                                <br />
                            </g:if>
                            <g:if test="${ci.getDerivedEndDate()}">
                                <g:formatDate date="${ci.getDerivedEndDate()}" format="${message(code:'default.date.format.notime')}"/>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:if test="${subCons.startDate}">
                                <g:formatDate date="${subCons.startDate}" format="${message(code:'default.date.format.notime')}"/>
                                <br />
                            </g:if>
                            <g:if test="${subCons.endDate}">
                                <g:formatDate date="${subCons.endDate}" format="${message(code:'default.date.format.notime')}"/>
                            </g:if>
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${ci?.id}">
                            <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}"
                                            type="currency"
                                            currencySymbol="${ci.billingCurrency ?: 'EUR'}" />
                        </g:if>
                    </td>
                    <td>
                        <ui:costSign ci="${ci}"/>

                        <g:if test="${ci?.isVisibleForSubscriber}">
                            <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                                <i class="${Icon.SIG.VISIBLE_ON} orange"></i>
                            </span>
                        </g:if>
                    </td>
                </g:if>
                <g:elseif test="${'onlyMemberSubs' in tableConfig}">
                    <td>
                        <g:if test="${subCons.startDate}">
                            <g:formatDate date="${subCons.startDate}" format="${message(code:'default.date.format.notime')}"/>
                            <br />
                        </g:if>
                        <g:if test="${subCons.endDate}">
                            <g:formatDate date="${subCons.endDate}" format="${message(code:'default.date.format.notime')}"/>
                        </g:if>
                    </td>
                </g:elseif>
                <td>
                    <g:if test="${subCons.isMultiYear}">
                        <ui:multiYearIcon isConsortial="true" color="orange" />
                    </g:if>
                </td>
                <g:if test="${'onlyMemberSubs' in tableConfig}">
                    <td>
                        <g:if test="${subCons in linkedSubscriptions}">
                            <g:link class="${Btn.MODERN.NEGATIVE_TOOLTIP}" action="linkToSubscription" data-content="${message(code: 'default.button.unlink.label')}" params="${params+[id:license.id, unlink:true, subscription:subCons.id, (FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}">
                                <i class="${Icon.CMD.REMOVE}"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link class="${Btn.MODERN.POSITIVE_TOOLTIP}" action="linkToSubscription" data-content="${message(code: 'default.button.link.label')}" params="${params+[id:license.id,subscription:subCons.id,(FormService.FORM_SERVICE_TOKEN):formService.getNewToken()]}">
                                <i class="${Icon.CMD.ADD}"></i>
                            </g:link>
                        </g:else>
                    </td>
                </g:if>

                <g:if test="${'showInfoFlyout' in tableConfig}">
                    <td class="center aligned">
                        <a href="#" class="ui button icon la-modern-button infoFlyout-trigger" data-template="org" data-org="${subscr.id}" data-sub="${subCons?.id}">
                            <i class="ui info icon"></i>
                        </a>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
        <g:if test="${'withCostItems' in tableConfig}">
            <tfoot>
            <tr>
                <th class="control-label" colspan="11">
                    ${message(code:'financials.totalCostOnPage')}
                </th>
            </tr>
            <g:each in="${finances}" var="entry">
                <tr>
                    <td colspan="${5 + tableConfig.size()}">
                        ${message(code:'financials.sum.billing')} ${entry.key}<br />
                    </td>
                    <td class="la-exposed-bg">
                        <g:formatNumber number="${entry.value}" type="currency" currencySymbol="${entry.key}"/>
                    </td>
                    <td colspan="2">

                    </td>
                </tr>
            </g:each>

            </tfoot>
        </g:if>
    </table>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br /><strong><g:message code="filter.result.empty"/></strong>
    </g:if>
    <g:else>
        <br /><strong><g:message code="result.empty"/></strong>
    </g:else>
</g:else>

<ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${totalCount}" />

<g:if test="${'showInfoFlyout' in tableConfig}">
    <laser:render template="/info/flyoutWrapper"/>
</g:if>