<%@ page import="de.laser.finance.CostItem; de.laser.survey.SurveyInfo; de.laser.TitleInstancePackagePlatform; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Address; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.Contact; de.laser.remote.ApiSource" %>

<laser:htmlStart message="menu.institutions.org.info" serviceInjection="true">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<laser:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView, consortialView: consortialView]}"/>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[org: orgInstance, user: user]}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${orgInstance.name}" >
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>

<ui:anualRings object="${orgInstance}" navPrev="${navPrevOrg}" navNext="${navNextOrg}" controller="organisation" action="show" />

<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, isProviderOrAgency: isProviderOrAgency]}"/>

<ui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<ui:messages data="${flash}"/>

<laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" />

        <h2 class="ui header" style="color:#fff;background-color:#f00;padding:0.5em 1em;margin:2em 0">DEMO</h2>

        <div class="ui five statistics">
            <div class="statistic stats-toggle" data-target="stat_subscriptions">
                <span class="value"> ${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id).size()} </span>
                <span class="label"> ${message(code: 'subscription.plural.current')} </span>
            </div>
            <div class="statistic stats-toggle" data-target="stat_licenses">
                <span class="value"> ${licenseMap.get(RDStore.LICENSE_CURRENT.id).size()} </span>
                <span class="label"> ${message(code: 'license.plural.current')} </span>
            </div>
            <div class="statistic stats-toggle" data-target="stat_providers">
                <span class="value"> ${providerMap.get(RDStore.SUBSCRIPTION_CURRENT.id).collect{it[0]}.unique().size()} </span>
                <span class="label"> ${message(code:'default.provider.label')} (${message(code: 'subscription.plural.current')}) </span>
            </div>
            <div class="statistic stats-toggle" data-target="stat_surveys">
                <span class="value"> ${surveyMap2.get('open').size()} </span>
                <span class="label"> Offene Umfragen </span>
            </div>
            <div class="statistic stats-toggle" data-target="stat_costs">
                <span class="value"> ${costs.costItems.size()} </span>
                <span class="label"> Kosten (${message(code: 'subscription.plural.current')}) </span>
            </div>
        </div>

    <br />
    <br />

<style>
    .statistics > .stats-toggle.active {
        background-color: rgba(0,0,0, 0.045);
    }
    .statistics > .stats-toggle.active > span {
        color: #1b1c1d !important;
    }
    .statistics > .statistic > span {
        color: #015591 !important;
    }
    .statistics > .statistic:hover > span {
        color: #1b1c1d !important;
    }
    .statistics > .statistic:hover {
        cursor: pointer;
        background-color: rgba(0,0,0, 0.1);
    }

    .stats-content {
        display: none;
    }

    h3.header > i.icon {
        vertical-align: baseline !important;
    }
    .ui.table > tfoot > tr > td {
        background-color: #fff;
    }
</style>

    <div id="stat_subscriptions" class="stats-content">
%{--        <h3 class="ui right aligned header">--}%
%{--            ${message(code:'subscription.plural')} <i class="icon clipboard" aria-hidden="true"></i>--}%
%{--        </h3>--}%

                <div class="chartWrapper" id="cw-subscriptions" style="width:100%; min-height:300px"></div>

                <div class="ui secondary la-tab-with-js menu">
                    <g:each in="${subscriptionMap}" var="subStatus,subList">
                        <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                        <a href="#" class="item ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="sub-${subStatusRdv.id}">
                            ${subStatusRdv.getI10n('value')} <span class="ui blue circular label">${subList.size()}</span>
                        </a>
                    </g:each>
                </div>

                <g:each in="${subscriptionMap}" var="subStatus,subList">
                    <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                    <div class="ui tab right attached segment ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="sub-${subStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                                <tr>
                                    <th class="nine wide">${message(code:'subscription.label')}</th>
                                    <th class="one wide">${message(code:'subscription.referenceYear.label.shy')}</th>
                                    <th class="two wide">${message(code:'subscription.isMultiYear.label.shy')}</th>
                                    <th class="two wide">${message(code:'subscription.startDate.label')}</th>
                                    <th class="two wide">${message(code:'subscription.endDate.label')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each in="${subList}" var="subId">
                                    <g:set var="sub" value="${Subscription.get(subId)}" />
                                    <tr>
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon clipboard la-list-icon"></i>
                                                <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                                            </div>
                                        </td>
                                        <td> ${sub.referenceYear} </td>
                                        <td> ${sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")} </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>

                    </div>
                </g:each>
    </div>

    <div id="stat_licenses" class="stats-content">
%{--        <h3 class="ui right aligned header">--}%
%{--            ${message(code:'license.plural')} <i class="icon balance scale" aria-hidden="true"></i>--}%
%{--        </h3>--}%
                <div class="chartWrapper" id="cw-licenses" style="width:100%; min-height:300px"></div>

                <div class="ui secondary la-tab-with-js menu">
                    <g:each in="${licenseMap}" var="subStatus,licList">
                        <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                        <a href="#" class="item ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="lic-${subStatusRdv.id}">
                            ${subStatusRdv.getI10n('value')} <span class="ui blue circular label">${licList.size()}</span>
                        </a>
                    </g:each>
                </div>



                <g:each in="${licenseMap}" var="subStatus,licList">
                    <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                    <div class="ui tab right attached segment ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="lic-${subStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="ten wide">${message(code:'license.label')}</th>
                                <th class="two wide">${message(code:'license.openEnded.label')}</th>
                                <th class="two wide">${message(code:'license.startDate.label')}</th>
                                <th class="two wide">${message(code:'license.endDate.label')}</th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${licList}" var="licId">
                                    <g:set var="lic" value="${License.get(licId)}" />
                                    <tr>
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon balance scale la-list-icon"></i>
                                                <g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link>
                                            </div>
                                        </td>
                                        <td> ${lic.openEnded?.getI10n('value')} </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${lic.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${lic.endDate}"/> </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>

                    </div>
                </g:each>
    </div>

    <div id="stat_providers" class="stats-content">
%{--        <h3 class="ui right aligned header">--}%
%{--            ${message(code:'default.provider.label')} <i class="icon university" aria-hidden="true"></i>--}%
%{--        </h3>--}%

                <div class="ui secondary la-tab-with-js menu">
                    <g:each in="${providerMap}" var="subStatus,provList">
                        <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                        <a href="#" class="item ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="prov-${subStatusRdv.id}">
                            ${subStatusRdv.getI10n('value')} <span class="ui blue circular label">${provList.collect{it[0]}.unique().size()}</span>
                        </a>
                    </g:each>
                </div>

                <div>
                    <span class="ui checkbox">
                        <label for="provider-toggle-subscriptions">Lizenzen anzeigen</label>
                        <input type="checkbox" id="provider-toggle-subscriptions">
                    </span>
                </div>

                <br />

                <g:each in="${providerMap}" var="subStatus,provList">
                    <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                    <div class="ui tab right attached segment ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="prov-${subStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="eight wide">${message(code:'default.provider.label')}</th>
                                <th class="one wide">${message(code:'subscription.plural')}</th>
                                <th class="one wide"></th>
                                <th class="two wide"></th>
                                <th class="two wide"></th>
                                <th class="two wide"></th>
                            </tr>
                            <tr data-ctype="provider-subsciption" style="display:none;">
                                <th class="eight wide">${message(code:'subscription.label')}</th>
                                <th class="one wide"></th>
                                <th class="one wide">${message(code:'subscription.referenceYear.label.shy')}</th>
                                <th class="two wide">${message(code:'subscription.isMultiYear.label.shy')}</th>
                                <th class="two wide">${message(code:'subscription.startDate.label')}</th>
                                <th class="two wide">${message(code:'subscription.endDate.label')}</th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${provList.collect{it[0]}.unique()}" var="provId">
                                    <g:set var="prov" value="${Org.get(provId)}" />
                                    <tr>
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon university la-list-icon"></i>
                                                <g:link controller="org" action="show" id="${prov.id}" target="_blank">${prov.name}</g:link>
                                            </div>
                                        </td>
                                        <td>${provList.findAll{it[0] == provId}.size()}</td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <g:each in="${provList}" var="provStruct">
                                            <g:if test="${provId == provStruct[0]}">
                                                <g:set var="sub" value="${Subscription.get(provStruct[1])}" />
                                                <tr data-ctype="provider-subsciption" style="display:none;">
                                                    <td style="padding-left:2rem;">
                                                        <div class="la-flexbox la-minor-object">
                                                            <i class="icon clipboard la-list-icon"></i>
                                                            <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                                                        </div>
                                                    </td>
                                                    <td></td>
                                                    <td> ${sub.referenceYear} </td>
                                                    <td> ${sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")} </td>
                                                    <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>
                                                    <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>
                                                </tr>
                                            </g:if>
                                        </g:each>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>

                    </div>
                </g:each>
    </div>

    <div id="stat_surveys" class="stats-content">
%{--        <h3 class="ui right aligned header">--}%
%{--            ${message(code:'survey.plural')} <i class="icon pie chart" aria-hidden="true"></i>--}%
%{--        </h3>--}%

                <div class="ui secondary la-tab-with-js menu">
                    <g:each in="${surveyMap2}" var="surveyStatus,surveyData">
                        <a href="#" class="item ${surveyStatus == 'open' ? 'active' : ''}" data-tab="survey-${surveyStatus}">
                            <%
                                switch (surveyStatus) {
                                    case 'open': print 'Offen'; break
                                    case 'finish': print 'Abgeschlossen'; break
                                    case 'termination': print 'Vorsorglich gekündigt'; break
                                    case 'notFinish': print 'Ausgelaufen'; break
                                    default: print '?'
                                }
                            %>
                            <span class="ui blue circular label">${surveyData.size()}</span>
                        </a>
                    </g:each>
                </div>
                <div>
                    <span class="ui checkbox">
                        <label for="survey-toggle-subscriptions">Lizenzen anzeigen</label>
                        <input type="checkbox" id="survey-toggle-subscriptions">
                    </span>
                </div>

                <br />

                <g:each in="${surveyMap2}" var="surveyStatus,surveyData">
                    <div class="ui tab right attached segment ${surveyStatus == 'open' ? 'active' : ''}" data-tab="survey-${surveyStatus}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="seven wide">${message(code:'survey.label')}</th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide">Teilnahme</th>
                                <th class="one wide">${message(code:'surveyInfo.type.label')}</th>
                                <th class="one wide">${message(code:'default.endDate.label')}</th>
                                <th class="two wide">Status</th>
                            </tr>
                            <tr data-ctype="survey-subsciption" style="display:none;">
                                <th class="seven wide">${message(code:'subscription.label')}</th>
                                <th class="one wide">${message(code:'subscription.referenceYear.label.shy')}</th>
                                <th class="one wide">${message(code:'subscription.isMultiYear.label.shy')}</th>
                                <th class="one wide">${message(code:'subscription.startDate.label')}</th>
                                <th class="one wide">${message(code:'subscription.endDate.label')}</th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="two wide"></th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${surveyData}" var="surveyStruct">
                                <g:set var="surveyInfo" value="${surveyStruct[0]}" />
                                <g:set var="surveyConfig" value="${surveyStruct[1]}" />
                                <g:set var="surveyOrg" value="${surveyStruct[2]}" />

                                <tr>
                                    <td>
                                        <div class="la-flexbox la-minor-object">
                                            <i class="icon pie chart la-list-icon"></i>
                                            <g:link controller="survey" action="show" id="${surveyInfo.id}" target="_blank">${surveyInfo.name}</g:link>
                                        </div>
                                    </td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td>
                                        <g:if test="${surveyOrg.finishDate}">
                                            <g:formatDate formatName="default.date.format.notime" date="${surveyOrg.finishDate}"/>
                                        </g:if>
                                    </td>
                                    <td>
                                        <span class="ui label survey-${surveyInfo.type.value}">${surveyInfo.type.getI10n('value')}</span>
                                    </td>
                                    <td>
                                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/>
                                    </td>
                                    <td>
                                        ${surveyInfo.status.getI10n('value')}
                                    </td>
                                </tr>

                                <g:if test="${surveyConfig.subscription}">
                                    <g:set var="sub" value="${surveyConfig.subscription}" />
                                    <tr data-ctype="survey-subsciption" style="display:none;">
                                        <td style="padding-left:2rem;">
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon clipboard la-list-icon"></i>
                                                <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                                            </div>
                                        </td>
                                        <td> ${sub.referenceYear} </td>
                                        <td> ${sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")} </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </g:if>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:each>
    </div>

    <div id="stat_costs" class="stats-content">
%{--        <h3 class="ui right aligned header">--}%
%{--            ${message(code:'subscription.costItems.label')} <i class="icon euro" aria-hidden="true"></i>--}%
%{--        </h3>--}%

        <div class="ui segment">
            <table class="ui table la-table celled very compact sortable">
                <thead>
                    <tr>
                        <th scope="col" rowspan="2">${message(code:'financials.newCosts.costTitle')}</th>
                            <th scope="col" class="la-smaller-table-head">${message(code:'default.subscription.label')}</th>
                            <th scope="col" rowspan="2" class="la-no-uppercase">
                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center">
                                    <i class="money bill alternate icon"></i>
                                </span>
                            </th>
                            <th scope="col" rowspan="2">${message(code:'default.currency.label')}</th>
                            <th scope="col" rowspan="2">${message(code:'financials.invoice_total')}</th>
                            <th scope="col" rowspan="2">${message(code:'financials.taxRate')}</th>
                            <th scope="col" rowspan="2">${message(code:'financials.amountFinal')}</th>
                            <th scope="col" rowspan="2">${message(code:'financials.newCosts.value')}</th>
                            <th scope="col" class="la-smaller-table-head">${message(code:'financials.dateFrom')}</th>
                            <th scope="col" rowspan="2">${message(code:'financials.costItemElement')}</th>
                        </tr>
                         <tr>
                             <th scope="col" class="la-smaller-table-head">${message(code:'financials.subscriptionRunningTime')}</th>
                             <th scope="col" class="la-smaller-table-head">${message(code:'financials.dateTo')}</th>
                         </tr>
                    </thead>
                    <tbody>
                        <g:each in="${costs.costItems}" var="ci" status="jj">
                            <%
                                String icon         = '<i class="question circle icon"></i>'
                                String dataTooltip  = message(code:'financials.costItemConfiguration.notSet')

                                switch (ci.costItemElementConfiguration) {
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
                                }
                            %>
                            <tr>
                                <td>
                                    <g:each in="${ci.sub.orgRelations}" var="or">
                                        <g:if test="${[RDStore.OR_SUBSCRIBER_CONS.id,RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id].contains(or.roleType.id)}">
                                            <g:link mapping="subfinance" params="[sub:ci.sub.id]">${or.org.designation}</g:link>
                                            <g:if test="${ci.isVisibleForSubscriber}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                                                    <i class="ui icon eye orange"></i>
                                                </span>
                                            </g:if>
                                        </g:if>
                                    </g:each>
                                    <br />
                                    ${ci.costTitle}
                                </td>
                                <td>
                                    <g:if test="${ci.sub}">
%{--                                        <g:if test="${ci.sub.instanceOf}">--}%
%{--                                            <g:link controller="subscription" action="show" id="${ci.sub.instanceOf.id}">${ci.sub.instanceOf.name}</g:link>--}%
%{--                                            <br />--}%
%{--                                            (${formatDate(date:ci.sub.instanceOf.startDate, format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.instanceOf.endDate, format: message(code: 'default.date.format.notime'))})--}%
%{--                                        </g:if>--}%
%{--                                        <g:else>--}%
                                            <g:link controller="subscription" action="show" id="${ci.sub.id}" target="_blank">${ci.sub.name}</g:link>
                                            <br />
                                            (${formatDate(date:ci.sub.startDate, format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.endDate, format: message(code: 'default.date.format.notime'))})
%{--                                        </g:else>--}%
                                    </g:if>
                                    <g:else>
                                        ${message(code:'financials.clear')}
                                    </g:else>
                                </td>
                                <td>
                                    <span class="la-popup-tooltip la-delay" data-position="right center" data-content="${dataTooltip}">${raw(icon)}</span>
                                </td>
                                <td>
                                    ${ci.billingCurrency ?: 'EUR'}
                                </td>
                                <td>
                                    <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencySymbol="" />
                                </td>
                                <td>
                                    <g:if test="${ci.taxKey && ci.taxKey.display}">
                                        ${ci.taxKey.taxRate+'%'}
                                    </g:if>
                                    <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                        ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                    </g:elseif>
                                    <g:elseif test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                                        ${ci.taxKey.taxType.getI10n("value")}
                                    </g:elseif>
                                    <g:elseif test="${!ci.taxKey}">
                                        <g:message code="financials.taxRate.notSet"/>
                                    </g:elseif>
                                </td>
                                <td>
                                    <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="" />
                                </td>
                                <td>
                                    <g:formatNumber number="${ci.costInLocalCurrency ?: 0.0}" type="currency" currencySymbol="EUR" />
                                    <br />
                                    <span class="la-secondHeaderRow" data-label="${message(code:'costItem.costInLocalCurrencyAfterTax.label')}:">
                                        <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="EUR" />
                                    </span>
                                </td>
                                <td>
                                    ${formatDate(date:ci.startDate, format:message(code: 'default.date.format.notime'))}
                                    <br />
                                    <span class="la-secondHeaderRow" data-label="${message(code:'financials.dateTo')}:">
                                        ${formatDate(date:ci.endDate, format:message(code: 'default.date.format.notime'))}
                                    </span>
                                </td>
                                <td>
                                    ${ci.costItemElement?.getI10n("value")}
                                </td>
                            </tr>
                        </g:each>
                        <tr>
                            <td colspan="10">&nbsp;</td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <g:if test="${costs.costItems.size() > 0 && costs.sums.billingSums}">
                            <tr>
                                <th class="control-label" colspan="10">${message(code:'financials.totalCost')}</th>
                            </tr>
                            <g:each in="${costs.sums.billingSums}" var="entry">
                                <tr>
                                    <td colspan="2"></td>
                                    <td colspan="2">${message(code:'financials.sum.billing')} ${entry.currency}</td>
                                    <td class="la-exposed-bg"><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                                    <td>${message(code:'financials.sum.billingAfterTax')}</td>
                                    <td class="la-exposed-bg"><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                                    <td colspan="3"></td>
                                </tr>
                            </g:each>
                            <tr>
                                <td colspan="6"></td>
                                <td>
                                    ${message(code:'financials.sum.local')}
                                    <br />
                                    ${message(code:'financials.sum.localAfterTax')}
                                </td>
                                <td class="la-exposed-bg">
                                    <g:formatNumber number="${costs.sums.localSums.localSum}" type="currency" currencySymbol="" currencyCode="EUR"/>
                                    <br />
                                    <g:formatNumber number="${costs.sums.localSums.localSumAfterTax}" type="currency" currencySymbol="" currencyCode="EUR"/>
                                </td>
                                <td colspan="2"></td>
                            </tr>
                        </g:if>
                        <g:elseif test="${costs.costItems.size() > 0 && !costs.sums.billingSums}">
                            <tr>
                                <td class="control-label" colspan="10">${message(code:'financials.noCostsConsidered')}</td>
                            </tr>
                        </g:elseif>
                    </tfoot>
                </table>
        </div>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.stats-toggle').on('click', function() {
            $('.stats-content').hide()
            $('.stats-toggle').removeClass('active')
            $(this).addClass('active')
            $('#' + $(this).attr('data-target')).show()

            if (JSPC.app.info && JSPC.app.info.charts) {
                JSPC.app.info.charts.echart1.resize()
                JSPC.app.info.charts.echart2.resize()
            }
       })

        $('.stats-toggle').first().trigger('click')

        $('#provider-toggle-subscriptions').checkbox({
            onChange: function() {
                $('table *[data-ctype=provider-subsciption]').toggle()
            }
        })
        $('#survey-toggle-subscriptions').checkbox({
            onChange: function() {
                $('table *[data-ctype=survey-subsciption]').toggle()
            }
        })
    </laser:script>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.info = {
        config1: {
            tooltip: {
                trigger: 'item'
            },
            series: [
                {
                    type: 'bar',
                    color: '#5470c6',
                    data: [${subscriptionTimelineMap.values().collect{ it.size() }.join(', ')}]
                },
            ],
            xAxis: {
                type: 'category',
                data: [${subscriptionTimelineMap.keySet().join(', ')}]
%{--                axisLabel: {--}%
%{--                    formatter: function(id, idx) {--}%
%{--                        return JSPC.app.info.config.series[1].data[idx].name--}%
%{--                    }--}%
%{--                },--}%
            },
            yAxis: { gridIndex: 0 },
            grid:  { left: '5%', right: '5%', top: '5%', bottom: '15%' },
        },
        config2: {
            tooltip: {
                trigger: 'item'
            },
            series: [
                {
                    type: 'bar',
                    color: '#5470c6',
                    data: [${licenseTimelineMap.values().collect{ it.size() }.join(', ')}]
                },
            ],
            xAxis: {
                type: 'category',
                data: [${licenseTimelineMap.keySet().join(', ')}]
        %{--                axisLabel: {--}%
        %{--                    formatter: function(id, idx) {--}%
        %{--                        return JSPC.app.info.config.series[1].data[idx].name--}%
        %{--                    }--}%
        %{--                },--}%
            },
            yAxis: { gridIndex: 0 },
            grid:  { left: '5%', right: '5%', top: '5%', bottom: '15%' },
        }
    };

JSPC.app.info.charts = {}

JSPC.app.info.charts.echart1 = echarts.init ($('#cw-subscriptions')[0]);
JSPC.app.info.charts.echart1.setOption (JSPC.app.info.config1);

JSPC.app.info.charts.echart2 = echarts.init ($('#cw-licenses')[0]);
JSPC.app.info.charts.echart2.setOption (JSPC.app.info.config2);
</laser:script>


<laser:htmlEnd />