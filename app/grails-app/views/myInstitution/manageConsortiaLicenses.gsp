<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'menu.institutions.myConsortiaLicenses')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="menu.institutions.myConsortiaLicenses" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.institutions.myConsortiaLicenses')} - ${costItems.size()} Treffer</h1>

<h2>* SEITE IN ARBEIT *</h2>

<semui:messages data="${flash}"/>

<semui:filter>
    <g:form action="manageConsortiaLicenses" controller="myInstitution" method="get" class="form-inline ui small form">

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

                <label>Konsorten</label>
                <g:select class="ui dropdown" name="member"
                              from="${filterConsortiaMembers}"
                              optionKey="id"
                              optionValue="${{ it.sortname + ' (' + it.name + ')'}}"
                              value="${params.member}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field fieldcontain">
                <semui:datepicker label="default.valid_on.label" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>

            <div class="field fieldcontain">
                <label>${message(code: 'myinst.currentSubscriptions.filter.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${ RefdataCategory.getAllRefdataValues('Subscription Status') }"
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
                                    <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
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
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:filter>


<table class="ui celled sortable table table-tworow la-table ignore-floatThead">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <g:sortableColumn property="roleT.org.sortname" params="${params}" title="Teilnehmer" />
            <th>Name / Vertrag</th>
            <th>Verknüpfte Pakete</th>
            <th>Anbieter</th>
            <th>Laufzeit von / bis</th>
            <th>${message(code:'financials.amountFinal')}</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${costItems}" var="entry" status="jj">
            <%
                com.k_int.kbplus.CostItem ci = entry[0]
                com.k_int.kbplus.Subscription subCons = entry[1]
                com.k_int.kbplus.Org subscr = entry[2]
            %>
            <tr>
                <td>
                    ${ jj + 1 }
                </td>
                <td>
                    <g:link controller="organisations" action="show" id="${subscr.id}">
                        <g:if test="${subscr.sortname}">${subscr.sortname}</g:if>
                        (${subscr.name})
                    </g:link>
                </td>
                <td>
                    <div class="la-flexbox">
                        <i class="icon balance scale la-list-icon"></i>
                        <g:link controller="subscriptionDetails" action="show" id="${subCons.id}">${subCons}</g:link>
                    </div>
                    <g:if test="${subCons.owner}">
                        <div class="la-flexbox">
                            <i class="icon folder open outline la-list-icon"></i>
                            <g:link controller="licenseDetails" action="show" id="${subCons.owner.id}">${subCons.owner}</g:link>
                        </div>
                    </g:if>
                </td>
                <td>
                    <g:each in="${subCons.packages}" var="subPkg">
                        <div class="la-flexbox">
                            <g:link controller="packageDetails" action="show" id="${subPkg.pkg.id}">${subPkg.pkg.name}</g:link>
                        </div>
                    </g:each>
                </td>
                <td>
                    <g:each in="${subCons.providers}" var="p">
                        <g:link controller="organisations" action="show" id="${p.id}">${p.getDesignation()}</g:link> <br/>
                    </g:each>
                </td>
                <td>
                    <g:if test="${ci.getDerivedStartDate() || ci.getDerivedEndDate()}">
                        <g:formatDate date="${ci.getDerivedStartDate()}"
                                  format="${message(code:'default.date.format.notime')}"/>
                        <br />
                    </g:if>
                    <g:if test="${ci.getDerivedStartDate() || ci.getDerivedEndDate()}">
                        <g:formatDate date="${ci.getDerivedEndDate()}"
                                  format="${message(code:'default.date.format.notime')}"/>
                    </g:if>
                </td>
                <td class="costData"
                    data-costInBillingCurrency="<g:formatNumber number="${ci.costInBillingCurrency}" locale="en" maxFractionDigits="2"/>"
                    data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                >
                    <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}"
                                    type="currency"
                                    currencySymbol="${ci.billingCurrency ?: 'EUR'}" />

                    <g:if test="${ci.isVisibleForSubscriber}">
                        <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="ui icon eye orange"></i>
                        </span>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

</body>
</html>
