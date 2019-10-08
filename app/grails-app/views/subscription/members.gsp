<%@ page import="com.k_int.kbplus.CostItem; com.k_int.kbplus.Person; de.laser.helper.RDStore; de.laser.interfaces.TemplateSupport" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} :
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
                            data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscriptionDetails" action="members"
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
                            data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" controller="subscriptionDetails" action="members"
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

    <h1 class="ui icon header"><semui:headerIcon />
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
    <semui:filter>
        <g:form action="members" controller="subscription" params="${[id:params.id]}" method="get" class="ui form">
            <%
                List<List<String>> tmplConfigShow
                if(accessService.checkPerm("ORG_CONSORTIUM"))
                    tmplConfigShow = [['name', 'identifier', 'libraryType'], ['federalState', 'libraryNetwork','property']]
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
                <th>${message(code:'default.startDate.label')}</th>
                <th>${message(code:'default.endDate.label')}</th>
                <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                    <th>${message(code: 'subscription.linktoLicense')}</th>
                </g:if>
                <th>${message(code:'subscription.details.status')}</th>
                <th class="la-action-info">${message(code:'default.actions')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubChilds}" status="i" var="zeile">
                <g:set var="sub" value="${zeile.sub}"/>
                <tr>
                    <td>${i + 1}</td>
                    <g:set var="filteredSubscribers" value="${zeile.orgs}" />
                    <g:each in="${filteredSubscribers}" var="subscr">
                        <td>${subscr.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved}">
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.isSlaved.tooltip')}">
                                    <i class="thumbtack blue icon"></i>
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

                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <td class="center aligned">
                            <g:if test="${sub?.owner?.id}">
                                <g:link controller="license" action="show" id="${sub?.owner?.id}"><i class=" inverted circular balance scale green link icon"></i></g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="subscription" action="linkLicenseMembers" id="${sub.id}" class="ui icon button"><i class="write icon"></i></g:link>
                            </g:else>
                        </td>
                    </g:if>
                    <td>${sub.status.getI10n('value')}</td>
                    <td class="x">
                        <g:link controller="subscription" action="show" id="${sub.id}" class="ui icon button"><i class="write icon"></i></g:link>
                        <g:if test="${editable}">
                            <g:if test="${sub.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_ADMINISTRATIVE}">
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
                            <g:link class="ui icon negative button" controller="subscription" action="delete" params="${[id:sub.id]}">
                                <i class="trash alternate icon"></i>
                            </g:link>

                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
        </table>
                <g:render template="../templates/copyEmailaddresses" model="[orgList: filteredSubChilds?.collect {it.orgs}?:[]]"/>
            </g:if>
            <g:else>
                <br><strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
            </g:else>

        </body>
        </html>

