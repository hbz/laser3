<%@ page import="de.laser.interfaces.CalculatedType; com.k_int.kbplus.CostItem; com.k_int.kbplus.Links; com.k_int.kbplus.Person; de.laser.helper.RDStore; com.k_int.kbplus.Subscription; com.k_int.kbplus.GenericOIDService" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} :
        <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
            <g:message code="subscription.details.consortiaMembers.label"/>
        </g:if>
        <g:elseif test="${accessService.checkPerm("ORG_INST_COLLECTIVE")}">
            <g:message code="subscription.details.collectiveMembers.label"/>
        </g:elseif>
    </title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

    <semui:controlButtons>
        <semui:exportDropdown>
            <semui:exportDropdownItem>
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
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
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
            </semui:exportDropdownItem>
        </semui:exportDropdown>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${subscriptionInstance}" field="name" />
        <semui:totalNumber total="${filteredSubChilds.size() ?: 0}"/>
    </h1>
    <semui:anualRings object="${subscriptionInstance}" controller="subscription" action="members" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <g:render template="nav" />
    <%--
    <semui:filter>
        <form class="ui form">
            <div class="fields">
                <div class="field">
                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" name="showDeleted" value="Y" ${params.showDeleted?'checked="checked"':''}>
                        <label>Gelöschte Teilnehmer anzeigen</label>
                    </div>
                </div>

                <div class="field">
                    <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
                </div>
            </div>
        </form>
    </semui:filter>
    --%>
    <g:render template="../templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <g:form action="members" controller="subscription" params="${[id:params.id]}" method="get" class="ui form">
            <%
                List<List<String>> tmplConfigShow
                if(accessService.checkPerm("ORG_CONSORTIUM"))
                    tmplConfigShow = [['name', 'identifier', 'libraryType'], ['region', 'libraryNetwork','property'], ['subRunTimeMultiYear']]
                else if(accessService.checkPerm("ORG_INST_COLLECTIVE"))
                    tmplConfigShow = [['name', 'identifier'], ['property']]
            %>
            <g:render template="/templates/filter/orgFilter"
                  model="[
                      tmplConfigShow: tmplConfigShow,
                      tmplConfigFormFilter: true,
                      useNewLayouter: true
                  ]"/>
        </g:form>
    </semui:filter>

    <semui:messages data="${flash}" />

    <g:if test="${filteredSubChilds}">
        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code:'default.sortname.label')}</th>
                <th>${message(code:'subscriptionDetails.members.members')}</th>
                <th>
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${message(code: 'default.previous.label')}">
                        <i class="arrow left icon"></i>
                    </span>
                </th>
                <th>${message(code:'default.startDate.label')}</th>
                <th>${message(code:'default.endDate.label')}</th>
                <th>
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${message(code: 'default.next.label')}">
                        <i class="arrow right icon"></i>
                    </span>
                </th>
                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                    <th>${message(code: 'subscription.linktoLicense')}</th>
                </g:if>
                <th>${message(code:'default.status.label')}</th>
                <th class="la-no-uppercase">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                        <i class="map orange icon"></i>
                    </span>
                </th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubChilds}" status="i" var="zeile">
                <g:set var="sub" value="${zeile.sub}"/>
                <tr>
                    <td>${i + 1}</td>
                    <g:set var="filteredSubscribers" value="${zeile.orgs}" />
                    <g:each in="${filteredSubscribers}" var="subscr">
                        <td>
                            ${subscr.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved}">
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.isSlaved.tooltip')}">
                                    <i class="thumbtack blue icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${subscr.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${subscr.getCustomerTypeI10n()}">
                                    <i class="chess rook grey icon"></i>
                                </span>
                            </g:if>

                            <div class="ui list">
                                <g:each in="${Person.getPublicByOrgAndFunc(subscr, 'General contact person')}" var="gcp">
                                    <div class="item">
                                        <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                        (${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')})
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', contextService.getOrg())}" var="gcp">
                                    <div class="item">
                                        <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                        (${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPublicByOrgAndObjectResp(subscr, sub, 'Specific subscription editor')}" var="sse">
                                    <div class="item">
                                        <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                        (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')})
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', contextService.getOrg())}" var="sse">
                                    <div class="item">
                                        <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                        (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                                    </div>
                                </g:each>
                            </div>
                        </td>
                    </g:each>
                    <g:if test="${! sub.getAllSubscribers()}">
                        <td></td>
                        <td></td>
                    </g:if>
                    <%
                        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(GenericOIDService.getOID(sub))
                        Subscription navPrevSubscription = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                        Subscription navNextSubscription = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
                    %>
                    <td class="center aligned">
                        <g:if test="${navPrevSubscription}">
                            <g:link controller="subscription" action="show" id="${navPrevSubscription.id}"><i class="arrow left icon"></i></g:link>
                        </g:if>
                    </td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <td class="center aligned">
                        <g:if test="${navNextSubscription}">
                            <g:link controller="subscription" action="show" id="${navNextSubscription.id}"><i class="arrow right icon"></i></g:link>
                        </g:if>
                    </td>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <td class="center aligned">
                            <g:set var="licenses" value="${Links.findAllByDestinationAndLinkType(GenericOIDService.getOID(sub),RDStore.LINKTYPE_LICENSE)}"/>
                            <g:each in="${licenses}" var="row">
                                <g:set var="license" value="${genericOIDService.resolveOID(row.source)}"/>
                                <g:link class="ui right labeled icon button green link" controller="license" action="show" id="${license.id}"><i class="balance scale icon"></i>${license.licenseCategory?.getI10n("value")}</g:link>
                            </g:each>
                            <g:if test="${!licenses}">
                                <g:link controller="subscription" action="linkLicenseMembers" id="${subscriptionInstance.id}" class="ui icon button"><i class="write icon"></i></g:link>
                            </g:if>
                        </td>
                    </g:if>
                    <td>${sub.status.getI10n('value')}</td>
                    <td>
                        <g:if test="${sub.isMultiYear}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                <i class="map orange icon"></i>
                            </span>
                        </g:if>
                    </td>
                    <td class="x">
                        <g:link controller="subscription" action="show" id="${sub.id}" class="ui icon button"><i class="write icon"></i></g:link>
                        <g:if test="${sub.isEditableBy(contextService.getUser())}"> <%-- needs to be checked for child subscription because of collective subscriptions! --%>
                            <g:if test="${sub.getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE] && sub.instanceOf.getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE}">
                                <g:if test="${sub.orgRelations.find{it.roleType == RDStore.OR_SUBSCRIBER_CONS_HIDDEN}}">
                                    <span  class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.hiddenForSubscriber')}">
                                        <g:link class="ui icon button" controller="ajax" action="toggleOrgRole" params="${[id:sub.id]}">
                                            <i class="eye orange icon"></i>
                                        </g:link>
                                    </span>
                                </g:if>
                                <g:else>
                                    <span  class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.hideToSubscriber')}">
                                        <g:link class="ui icon orange button" controller="ajax" action="toggleOrgRole" params="${[id:sub.id]}">
                                            <i class="eye icon"></i>
                                        </g:link>
                                    </span>
                                </g:else>
                            </g:if>
                            <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci from CostItem ci where ci.sub = :sub and ci.costItemStatus != :deleted',[sub:sub,deleted:RDStore.COST_ITEM_DELETED])}"/>
                            <g:if test="${!hasCostItems}">
                                <g:link class="ui icon negative button" controller="subscription" action="delete" params="${[id:sub.id]}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <div class="ui icon negative buttons la-popup-tooltip" data-content="${message(code:'subscription.delete.existingCostItems')}">
                                    <button class="ui disabled button la-selectable-button">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                </div>
                            </g:else>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
        </table>
                <g:render template="../templates/copyEmailaddresses" model="[orgList: filteredSubChilds?.collect {it.orgs}?:[]]"/>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscriptionDetails.members.members")]}"/></strong>
                </g:if>
                <g:else>
                <br><strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
                </g:else>
            </g:else>

        </body>
        </html>

