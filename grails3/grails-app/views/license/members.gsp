<%@ page import="de.laser.License; de.laser.RefdataCategory; de.laser.helper.RDStore; de.laser.RefdataValue; de.laser.helper.RDConstants; de.laser.Person; de.laser.Subscription" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
  <meta name="layout" content="laser">
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

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="members" controller="license" params="${[id:params.id]}" method="get" class="ui form">
        <div class="three fields">
            <div class="field">
                <label for="subscription">${message(code:'subscription')}</label>
                <select id="subscription" name="subscription" multiple="" class="ui selection fluid dropdown">
                    <option value="">${message(code:'default.select.choose.label')}</option>
                    <g:each in="${subscriptionsForFilter}" var="sub">
                        <option <%=(params.list('subscription').contains(sub.id.toString())) ? 'selected="selected"' : '' %> value="${sub.id}">${sub.dropdownNamingConvention()}</option>
                    </g:each>
                </select>
            </div>
            <div class="field">
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>

            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="two fields">
            <div class="field">
                <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                            <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox" <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" <g:if test="${params.subRunTime}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input name="filterSet" type="hidden" value="true">
                <input type="submit" value="${message(code:'default.button.filter.label')}" class="ui secondary button"/>
            </div>
        </div>
    </g:form>
</semui:filter>

<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'license.member')}</th>
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
            <th>${message(code: 'license.subs.count')}</th>
        </tr>
    </thead>
    <tbody>

        <g:each in="${validMemberLicenses}" status="i" var="row">
            <g:set var="lic" value="${row.license}"/>
            <%
                LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(lic)
                License navPrevLicense = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                License navNextLicense = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
            %>
            <tr>
                <td>${i + 1}</td>
                <td>${lic.reference}</td>
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
                    <g:link controller="license" action="show" id="${lic.id}" class="ui icon button blue la-modern-button"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                        <i aria-hidden="true" class="write icon"></i></g:link>
                </td>
                <td>
                    <g:link action="linkMemberLicensesToSubs" id="${lic.id}"><semui:totalNumber total="${row.subs ?: 0}"/></g:link>
                </td>
            </tr>
        </g:each>

    </tbody>
</table>

</body>
</html>
