<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'menu.my.consortiaSubscriptions')}</title>
</head>

<body>

<semui:debugInfo>
    <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</semui:debugInfo>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="menu.my.consortiaSubscriptions" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:if test="${filterSet || defaultSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageConsortiaSubscriptions"
                        params="${params+[exportXLS:true]}">
                    ${message(code:'default.button.exports.xls')}
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" controller="myInstitution" action="manageConsortiaSubscriptions" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
            </g:else>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:if test="${filterSet || defaultSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="manageConsortiaSubscriptions"
                        params="${params+[format:'csv']}">
                    ${message(code:'default.button.exports.csv')}
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" controller="myInstitution" action="manageConsortiaSubscriptions" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
            </g:else>
        </semui:exportDropdownItem>

    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui left aligned icon header">
    <semui:headerIcon />${message(code: 'menu.my.consortiaSubscriptions')}
    <semui:totalNumber total="${countCostItems}"/>
</h1>

<semui:messages data="${flash}"/>

<semui:filter>
    <g:form action="manageConsortiaSubscriptions" controller="myInstitution" method="get" class="form-inline ui small form">

        <div class="three fields">
            <div class="field">
                <%--
               <label>${message(code: 'default.search.text', default: 'Search text')}
                   <span data-position="right center" data-variation="tiny" data-tooltip="${message(code:'default.search.tooltip.subscription')}">
                       <i class="question circle icon"></i>
                   </span>
               </label>
               <div class="ui input">
                   <input type="text" name="q"
                          placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                          value="${params.q}"/>
               </div>
               --%>

                <label>${message(code:'myinst.consortiaSubscriptions.consortia')}</label>
                <g:select class="ui search selection dropdown" name="member"
                              from="${filterConsortiaMembers}"
                              optionKey="id"
                              optionValue="${{ it.sortname + ' (' + it.name + ')'}}"
                              value="${params.member}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field fieldcontain">
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>

            <div class="field fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
                <%
                    def fakeList = []
                    fakeList.addAll(RefdataCategory.getAllRefdataValues('Subscription Status'))
                    fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                    fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status'))
                %>
                <laser:select class="ui dropdown" name="status"
                              from="${ fakeList }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">
            <g:render template="../templates/properties/genericFilter" model="[propList: filterPropList]"/>

            <div class="field">
                <label>${message(code:'subscription.form.label')}</label>
                <laser:select class="ui dropdown" name="form"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Form')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label>${message(code:'subscription.resource.label')}</label>
                <laser:select class="ui dropdown" name="resource"
                              from="${RefdataCategory.getAllRefdataValues('Subscription Resource')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.resource}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="subscritionType">${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>

                <fieldset id="subscritionType">
                    <div class="inline fields la-filter-inline">

                        <g:each in="${filterSubTypes}" var="subType">
                            <div class="inline field">
                                <div class="ui checkbox">
                                    <label for="checkSubType-${subType.id}">${subType?.getI10n('value')}</label>
                                    <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                        <g:if test="${params.list('subTypes').contains(subType.id.toString())}"> checked="" </g:if>
                                           tabindex="0">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </fieldset>
            </div>

            <div class="field">
                <div class="two fields">

                    <div class="field">
                    </div>

                    <div class="field la-field-right-aligned">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input name="filterSet" value="true" type="hidden">
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:filter>

<g:if test="${params.member}">
    <g:set var="choosenOrg" value="${com.k_int.kbplus.Org.findById(params.member)}" />
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}" />

    <table class="ui table la-table la-table-small">
        <tbody>
            <tr>
                <td>
                    <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${choosenOrgCPAs}">
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <g:render template="/templates/cpa/person_details" model="${[person: gcp, tmplHideLinkToAddressbook: true, overwriteEditable: false]}" />
                        </g:each>
                    </g:if>
                </td>
            </tr>
        </tbody>
    </table>
</g:if>

<table class="ui celled sortable table table-tworow la-table ignore-floatThead">
    <thead>
        <tr>
            <th rowspan="2" class="center aligned">${message(code:'sidewide.number')}</th>
            <g:sortableColumn property="roleT.org.sortname" params="${params}" title="${message(code:'myinst.consortiaSubscriptions.member')}" rowspan="2" />
            <g:sortableColumn property="subT.name" params="${params}" title="${message(code:'myinst.consortiaSubscriptions.subscription')}" class="la-smaller-table-head" />
            <th rowspan="2">${message(code:'myinst.consortiaSubscriptions.packages')}</th>
            <th rowspan="2">${message(code:'myinst.consortiaSubscriptions.provider')}</th>
            <th rowspan="2">${message(code:'myinst.consortiaSubscriptions.runningTimes')}</th>
            <th rowspan="2">${message(code:'financials.amountFinal')}</th>
            <th rowspan="2">
                <span data-tooltip="${message(code:'financials.costItemConfiguration')}" data-position="top center">
                    <i class="money bill alternate icon"></i>
                </span>&nbsp;/&nbsp;
                <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                    <i class="ui icon eye orange"></i>
                </span>
            </th>
        </tr>
        <tr>
            <g:sortableColumn property="subK.owner.reference" params="${params}" title="${message(code:'myinst.consortiaSubscriptions.license')}" class="la-smaller-table-head" />
        </tr>
    </thead>
    <tbody>
        <g:each in="${costItems}" var="entry" status="jj">
            <%
                com.k_int.kbplus.CostItem ci = entry[0] ?: new CostItem()
                com.k_int.kbplus.Subscription subCons = entry[1]
                com.k_int.kbplus.Org subscr = entry[2]
            %>
            <tr>
                <td>
                    ${ jj + 1 }
                </td>
                <td>
                    <g:link controller="organisation" action="show" id="${subscr.id}">
                        <g:if test="${subscr.sortname}">${subscr.sortname}</g:if>
                        (${subscr.name})
                    </g:link>
                </td>
                <td>
                    <div class="la-flexbox">
                        <i class="icon folder open outline la-list-icon"></i>
                        <g:link controller="subscription" action="show" id="${subCons.id}">${subCons.name}</g:link>
                        <g:if test="${subCons.getCalculatedPrevious()}">
                            <span data-position="top left" data-tooltip="${message(code:'subscription.hasPreviousSubscription')}">
                                <i class="arrow left grey icon"></i>
                            </span>
                        </g:if>
                    </div>
                    <g:if test="${subCons.owner}">
                        <div class="la-flexbox">
                            <i class="icon balance scale la-list-icon"></i>
                            <g:link controller="license" action="show" id="${subCons.owner.id}">${subCons.owner.reference}</g:link>
                        </div>
                    </g:if>
                </td>
                <td>
                    <g:each in="${subCons.packages}" var="subPkg">
                        <div class="la-flexbox">
                            <i class="icon gift la-list-icon"></i>
                            <g:link controller="package" action="show" id="${subPkg.pkg.id}">${subPkg.pkg.name}</g:link>
                        </div>
                    </g:each>
                </td>
                <td>
                    <g:each in="${subCons.providers}" var="p">
                        <g:link controller="organisation" action="show" id="${p.id}">${p.getDesignation()}</g:link> <br/>
                    </g:each>
                </td>
                <td>
                    <g:if test="${ci.id}"> <%-- only existing cost item --%>
                        <g:if test="${ci.getDerivedStartDate()}">
                            <g:formatDate date="${ci.getDerivedStartDate()}" format="${message(code:'default.date.format.notime')}"/>
                            <br />
                        </g:if>
                        <g:if test="${ci.getDerivedEndDate()}">
                            <g:formatDate date="${ci.getDerivedEndDate()}" format="${message(code:'default.date.format.notime')}"/>
                        </g:if>
                    </g:if>
                </td>
                <td>
                    <g:if test="${ci.id}"> <%-- only existing cost item --%>
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}"
                                    type="currency"
                                    currencySymbol="${ci.billingCurrency ?: 'EUR'}" />
                    </g:if>
                </td>

                <%  // TODO .. copied from finance/_result_tab_cons.gsp

                    def elementSign = 'notSet'
                    def icon = ''
                    def dataTooltip = ""
                    if (ci.costItemElementConfiguration) {
                        elementSign = ci.costItemElementConfiguration
                    }

                    switch(elementSign) {
                        case RDStore.CIEC_POSITIVE:
                            dataTooltip = message(code:'financials.costItemConfiguration.positive')
                            icon = '<i class="plus green circle icon"></i>'
                            break
                        case RDStore.CIEC_NEGATIVE:
                            dataTooltip = message(code:'financials.costItemConfiguration.negative')
                            icon = '<i class="minus red circle icon"></i>'
                            break
                        case RDStore.CIEC_NEUTRAL:
                            dataTooltip = message(code:'financials.costItemConfiguration.neutral')
                            icon = '<i class="circle yellow icon"></i>'
                            break
                        default:
                            dataTooltip = message(code:'financials.costItemConfiguration.notSet')
                            icon = '<i class="question circle icon"></i>'
                            break
                    }
                %>

                <td>
                    <g:if test="${ci.id}">
                        <span data-position="top left" data-tooltip="${dataTooltip}">${raw(icon)}</span>
                    </g:if>

                    <g:if test="${ci.isVisibleForSubscriber}">
                        <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="ui icon eye orange"></i>
                        </span>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>

    <tfoot>
        <tr>
            <th colspan="8">
                ${message(code:'financials.totalCostOnPage')}
            </th>
        </tr>
        <g:each in="${finances}" var="entry">
            <tr>
                <td colspan="6">
                    ${message(code:'financials.sum.billing')} ${entry.key}<br>
                </td>
                <td class="la-exposed-bg">
                    <g:formatNumber number="${entry.value}" type="currency" currencySymbol="${entry.key}"/>
                </td>
                <td colspan="1">

                </td>
            </tr>
        </g:each>

    </tfoot>
</table>

<semui:paginate action="manageConsortiaSubscriptions" controller="myInstitution" params="${params}"
                next="${message(code:'default.paginate.next', default:'Next')}"
                prev="${message(code:'default.paginate.prev', default:'Prev')}"
                max="${max}" total="${countCostItems}" />

</body>
</html>
