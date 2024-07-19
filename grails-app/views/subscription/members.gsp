<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.CustomerTypeService; de.laser.storage.BeanStore; de.laser.finance.CostItem; de.laser.Links; de.laser.Person; de.laser.interfaces.CalculatedType; de.laser.storage.RDStore; de.laser.Subscription" %>

<laser:htmlStart text="${BeanStore.getContextService().getOrg().isCustomerType_Consortium() ? message(code:'subscription.details.consortiaMembers.label') : ''}" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>

    <ui:controlButtons>
        <ui:exportDropdown>
            <%--<ui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscription" action="members"
                            params="${params+[exportXLS:true]}">
                        ${message(code:'default.button.exports.xls')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="members" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                </g:else>
            </ui:exportDropdownItem>--%>
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.SUBSCRIPTIONS_MEMBERS]"/>
            </ui:exportDropdownItem>
            <%--
            <ui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscription" action="members"
                            params="${params+[format:'csv']}">
                        ${message(code:'default.button.exports.csv')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="members" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                </g:else>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscription" action="members"
                            params="${params+[exportIPs:true]}">
                        ${message(code:'subscriptionDetails.members.exportIPs')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="members" params="${params+[exportIPs:true]}">${message(code:'subscriptionDetails.members.exportIPs')}</g:link>
                </g:else>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscription" action="members"
                            params="${params+[exportProxys:true]}">
                        ${message(code:'subscriptionDetails.members.exportProxys')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="members" params="${params+[exportProxys:true]}">${message(code:'subscriptionDetails.members.exportProxys')}</g:link>
                </g:else>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscription" action="members"
                            params="${params+[exportEZProxys:true]}">
                        ${message(code:'subscriptionDetails.members.exportEZProxys')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="members" params="${params+[exportEZProxys:true]}">${message(code:'subscriptionDetails.members.exportEZProxys')}</g:link>
                </g:else>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscription" action="members"
                            params="${params+[exportShibboleths:true]}">
                        ${message(code:'subscriptionDetails.members.exportShibboleths')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="members" params="${params+[exportShibboleths:true]}">${message(code:'subscriptionDetails.members.exportShibboleths')}</g:link>
                </g:else>
            </ui:exportDropdownItem>
            --%>
        </ui:exportDropdown>
        <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" visibleProviders="${providerRoles}">
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:totalNumber class="la-numberHeader" total="${filteredSubChilds.size() ?: 0}"/>

    <ui:anualRings object="${subscription}" controller="subscription" action="members" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="${customerTypeService.getNavTemplatePath()}" />

    <ui:filter>
        <g:form action="members" controller="subscription" params="${[id:params.id]}" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                  model="[
                      tmplConfigShow: [['name', 'identifier', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['subRunTimeMultiYear']],
                      tmplConfigFormFilter: true
                  ]"/>
        </g:form>
    </ui:filter>

    <ui:messages data="${flash}" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>

    <g:if test="${filteredSubChilds}">
        <table class="ui celled monitor sortable stackable la-js-responsive-table la-table table">
            <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code:'default.sortname.label')}</th>
                <g:sortableColumn params="${params}" property="o.sortname" title="${message(code:'subscriptionDetails.members.members')}"/>
                <th class="center aligned">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${message(code: 'default.previous.label')}">
                        <i class="${Icon.LNK.PREV}"></i>
                    </span>
                </th>
                <g:sortableColumn params="${params}" property="sub.startDate" title="${message(code:'default.startDate.label.shy')}"/>
                <g:sortableColumn params="${params}" property="sub.endDate" title="${message(code:'default.endDate.label.shy')}"/>
                <th class="center aligned">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${message(code: 'default.next.label')}">
                        <i class="${Icon.LNK.NEXT}"></i>
                    </span>
                </th>
                <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()}">
                    <th class="center aligned la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center" data-content="${message(code: 'subscription.linktoLicense')}">
                            <i class="${Icon.LICENSE}"></i>
                        </span>
                    </th>
                </g:if>
                <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                    <th class="center aligned la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center" data-content="${message(code: 'subscription.packages.label')}">
                            <i class="${Icon.PACKAGE}"></i>
                        </span>
                    </th>
                </g:if>
                <th>${message(code:'default.status.label')}</th>
                <th class="center aligned la-no-uppercase">
                    <ui:multiYearIcon isConsortial="true" />
                </th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubChilds}" status="i" var="row">
                <g:set var="sub" value="${row.sub}"/>
                <tr>
                    <g:set var="subscr" value="${row.orgs}" />
                    <td>${i + 1}</td>
                    <%--<g:each in="${filteredSubscribers}" var="subscr">--%>
                        <td>
                            ${subscr.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved}">
                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.isSlaved.tooltip')}">
                                    <i class="grey la-thumbtack-regular icon"></i>
                                </span>
                            </g:if>

                            <ui:customerTypeProIcon org="${subscr}" />

                            <div class="ui list">
                                <g:each in="${Person.getPublicByOrgAndFunc(subscr, 'General contact person')}" var="gcp">
                                    <div class="item">
                                        ${gcp}
                                        (${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')})
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', contextService.getOrg())}" var="gcp">
                                    <div class="item">
                                        ${gcp}
                                        (${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')} <i class="${Icon.UI.ACP_PRIVATE}" style="display:inline-block"></i>)
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPublicByOrgAndObjectResp(subscr, sub, 'Specific subscription editor')}" var="sse">
                                    <div class="item">
                                        ${sse}
                                        (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')})
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', contextService.getOrg())}" var="sse">
                                    <div class="item">
                                        ${sse}
                                        (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')} <i class="${Icon.UI.ACP_PRIVATE}" style="display:inline-block"></i>)
                                    </div>
                                </g:each>
                            </div>
                        </td>
                    <%--</g:each>--%>
                    <g:if test="${! sub.getAllSubscribers()}">
                        <td></td>
                        <td></td>
                    </g:if>
                    <%
                        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(sub,false)
                        Long navPrevSubMember = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                        Long navNextSubMember = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
                    %>
                    <td class="center aligned">
                        <g:if test="${navPrevSubMember}">
                            <g:link controller="subscription" action="show" id="${navPrevSubMember}"><i class="${Icon.LNK.PREV}"></i></g:link>
                        </g:if>
                        <g:elseif test="${(navPrevSubscription?.size() > 0) && Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.org = :subscriber',[parent:navPrevSubscription[0],subscriber:subscr])}">
                            <g:link controller="subscription" class="ui icon js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.linkPrevMemberSub")}"
                                    data-confirm-term-how="ok"
                                    action="linkNextPrevMemberSub"
                                    id="${subscription.id}"
                                    params="[prev: true, memberOrg: subscr.id, memberSubID: sub.id]"><i class="${Icon.LNK.PREV} grey"></i></g:link>
                        </g:elseif>
                    </td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <td class="center aligned">
                        <g:if test="${navNextSubMember}">
                            <g:link controller="subscription" action="show" id="${navNextSubMember}"><i class="${Icon.LNK.NEXT}"></i></g:link>
                        </g:if>
                        <g:elseif test="${(navNextSubscription?.size() > 0) && Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.org = :subscriber',[parent:navNextSubscription[0],subscriber:subscr])}">
                            <g:link controller="subscription" class="ui icon js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.linkNextMemberSub")}"
                                    data-confirm-term-how="ok"
                                    action="linkNextPrevMemberSub"
                                    id="${subscription.id}"
                                    params="[next: true, memberOrg: subscr.id, memberSubID: sub.id]"><i class="${Icon.LNK.NEXT} grey"></i></g:link>
                        </g:elseif>
                    </td>
                    <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()}">
                        <td class="center aligned">
                            <g:set var="license" value="${Links.executeQuery('select li.id from Links li where li.destinationSubscription = :destination and li.linkType = :linktype',[destination:sub,linktype:RDStore.LINKTYPE_LICENSE])}"/>
                            <g:if test="${!license}">
                                <g:link controller="subscription" action="membersSubscriptionsManagement" params="[tab: 'linkLicense']" id="${subscription.id}" class="ui icon">
                                    <i class="circular la-light-grey inverted minus icon"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="subscription" action="membersSubscriptionsManagement" params="[tab: 'linkLicense']" id="${subscription.id}" class="ui icon">
                                    <i class="circular la-license icon"></i>
                                </g:link>
                            </g:else>
                        </td>
                    </g:if>
                    <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                        <td class="center aligned">
                            <g:if test="${!sub.packages}">
                                <g:link controller="subscription" action="membersSubscriptionsManagement" params="[tab: 'linkPackages']" id="${subscription.id}" class="ui icon">
                                    <i class="circular la-light-grey inverted minus icon"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="subscription" action="membersSubscriptionsManagement" params="[tab: 'linkPackages']" id="${subscription.id}" class="ui icon">
                                    <i class="circular la-package icon"></i>
                                </g:link>
                            </g:else>
                        </td>
                    </g:if>
                    <td>${sub.status.getI10n('value')}</td>
                    <td>
                        <g:if test="${sub.isMultiYear}">
                            <ui:multiYearIcon isConsortial="true" color="orange" />
                        </g:if>
                    </td>
                    <td class="x">

                        <g:link controller="subscription" action="show" id="${sub.id}" class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                        </g:link>

                        <g:if test="${contextService.getUser().isYoda()}">
                            <g:if test="${sub._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION] && sub.instanceOf._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE}">
                                <g:if test="${sub.orgRelations.find{it.roleType == RDStore.OR_SUBSCRIBER_CONS_HIDDEN}}">
                                    <g:link class="ui icon button la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.hiddenForSubscriber')}" controller="ajax" action="toggleOrgRole" params="${[id:sub.id]}">
                                        <i class="ui icon eye"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link class="ui icon orange button la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.hideToSubscriber')}" controller="ajax" action="toggleOrgRole" params="${[id:sub.id]}">
                                        <i class="ui icon eye"></i>
                                    </g:link>
                                </g:else>
                            </g:if>
                        </g:if>

                            <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci.id from CostItem ci where ci.sub = :sub and ci.costItemStatus != :deleted and ci.owner = :context',[sub:sub,deleted:RDStore.COST_ITEM_DELETED,context:institution])}"/>
                            <g:if test="${!hasCostItems}">
                                <g:link class="${Btn.MODERN.NEGATIVE_ICON}" controller="subscription" action="delete" params="${[id:sub.id]}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <span class="la-popup-tooltip" data-content="${message(code:'subscription.delete.existingCostItems')}">
                                    <button class="${Btn.MODERN.NEGATIVE_ICON} disabled"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </button>
                                </span>
                            </g:else>

                        <ui:xEditableAsIcon owner="${sub}" class="ui icon center aligned" iconClass="info circular inverted" field="comment" type="textarea" emptyTooltip="${message(code: 'subscription.details.internalComment')}"/>
                    </td>
                </tr>
            </g:each>
        </tbody>
        </table>
                <laser:render template="/templates/copyEmailaddresses" model="[orgList: filteredSubChilds?.collect {it.orgs}?:[]]"/>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscriptionDetails.members.members")]}"/></strong>
                </g:if>
                <g:else>
                <br /><strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
                </g:else>
            </g:else>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#orgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        });
    </laser:script>

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />

