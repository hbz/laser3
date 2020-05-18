<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.License; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.Person; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
  <meta name="layout" content="semanticUI"/>
  <title>${message(code:'laser')} : ${message(code:'license.details.incoming.childs',args:[message(code:'consortium.subscriber')])}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <g:if test="${accessService.checkMinUserOrgRole(user,institution,"INST_EDITOR")}">
            <g:render template="actions" />
        </g:if>
    </semui:controlButtons>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

<%--<semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>--%>

<g:render template="nav" />

<g:if test="${license.getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && license.getConsortia()?.id == institution.id}">
    <g:render template="../templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <g:form action="subscriptions" controller="license" params="${[id:params.id]}" method="get" class="ui form">
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
</g:if>

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
            <th>${message(code: 'default.subscription.label')}</th>
            <%-- for owned subscriptions!!!! --%>
            <th>${message(code:'default.status.label')}</th>
            <th class="la-no-uppercase">
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                    <i class="map orange icon"></i>
                </span>
            </th>
            <g:if test="${editable}">
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </g:if>
        </tr>
    </thead>
    <tbody>

        <g:each in="${license.subscriptions}" status="i" var="sub">
            <g:set var="subscr" value="${sub.getSubscriber()}"/>
            <%
                /*LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(License.class.name, lic.id)
                Subscription navPrevSubscription = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                Subscription navNextSubscription = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null*/
            %>
            <tr>
                <td>${i + 1}</td>
                <td>${subscr.sortname}</td>
                <td>
                    <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                    <g:if test="${license.isSlaved}">
                        <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'license.details.isSlaved.tooltip')}">
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
                        <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', institution)}" var="gcp">
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
                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', institution)}" var="sse">
                            <div class="item">
                                <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                            </div>
                        </g:each>
                    </div>
                </td>
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
                <td class="center aligned">
                    <g:link controller="subscription" action="show" id="${sub.id}"><i class="inverted circular clipboard orange link icon"></i></g:link>
                </td>
                <td>
                    ${sub.status.getI10n("value")}
                </td>
                <td>
                    <g:if test="${sub.isMultiYear}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </g:if>
                </td>
                <g:if test="${editable}">
                    <td class="x">
                        <g:link class="ui icon negative button" controller="license" action="delete" params="${[id:license.id]}">
                            <i class="trash alternate icon"></i>
                        </g:link>
                    </td>
                </g:if>
            </tr>
        </g:each>

    </tbody>
</table>

</body>
</html>
