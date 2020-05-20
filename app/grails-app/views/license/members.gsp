<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.License; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.Person" %>
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
        <semui:totalNumber total="${validMemberLicenses.size() ?: 0}"/>
    </h1>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

<g:render template="nav" />

<g:render template="../templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="members" controller="license" params="${[id:params.id]}" method="get" class="ui form">
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

<table class="ui celled la-table table">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'default.sortname.label')}</th>
            <th>${message(code:'subscriptionDetails.members.members')}</th>
            <th class="la-no-uppercase">
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'default.previous.label')}">
                    <i class="arrow left icon"></i>
                </span>
            </th>
            <th>${message(code:'default.startDate.label')}</th>
            <th>${message(code:'default.endDate.label')}</th>
            <th class="la-no-uppercase">
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'default.next.label')}">
                    <i class="arrow right icon"></i>
                </span>
            </th>
            <th>${message(code: 'license')}</th>
            <%-- for owned subscriptions!!!! --%>
            <th>${message(code: 'default.subscription.label')} (${message(code:'default.status.label')})</th>
        </tr>
    </thead>
    <tbody>

        <g:each in="${validMemberLicenses}" status="i" var="row">
            <g:set var="lic" value="${row.license}"/>
            <g:set var="subscr" value="${row.org}"/>
            <%
                LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(License.class.name, lic.id)
                License navPrevLicense = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                License navNextLicense = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
            %>
            <tr>
                <td>${i + 1}</td>
                <td>${subscr.sortname}</td>
                <td>
                    <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                    <g:if test="${lic.isSlaved}">
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
                        <g:each in="${Person.getPublicByOrgAndObjectResp(subscr, license, 'Specific license editor')}" var="sle">
                            <div class="item">
                                <g:link controller="person" action="show" id="${sle.id}">${sle}</g:link>
                                (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')})
                            </div>
                        </g:each>
                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, license, 'Specific license editor', institution)}" var="sle">
                            <div class="item">
                                <g:link controller="person" action="show" id="${sle.id}">${sle}</g:link>
                                (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                            </div>
                        </g:each>
                    </div>
                </td>
                <td class="center aligned">
                    <g:if test="${navPrevLicense}">
                        <g:link controller="license" action="show" id="${navPrevLicense.id}"><i class="arrow left icon"></i></g:link>
                    </g:if>
                </td>
                <td><g:formatDate formatName="default.date.format.notime" date="${lic.startDate}"/></td>
                <td><g:formatDate formatName="default.date.format.notime" date="${lic.endDate}"/></td>
                <td class="center aligned">
                    <g:if test="${navNextLicense}">
                        <g:link controller="license" action="show" id="${navNextLicense.id}"><i class="arrow right icon"></i></g:link>
                    </g:if>
                </td>
                <td>
                    <g:link controller="license" action="show" id="${lic.id}" class="ui icon button"><i class="write icon"></i></g:link>
                </td>
                <td>
                    <g:each in="${row.subs}" var="sub">
                        <g:link controller="subscription" action="show" id="${sub.id}">${sub.name} (${sub.status.getI10n("value")})</g:link>
                        <g:if test="${sub.isMultiYear}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                <i class="map orange icon"></i>
                            </span>
                        </g:if>
                        <g:if test="${editable}">
                            <g:link class="ui icon negative button" controller="license" action="unlinkSubscription" params="${[subscription:sub.id,license:lic.id]}">
                                <i class="unlink alternate icon"></i>
                            </g:link>
                        </g:if>
                        <br>
                    </g:each>
                </td>
            </tr>
        </g:each>

    </tbody>
</table>

</body>
</html>
