<%@ page import="de.laser.wekb.Provider; de.laser.ui.Icon; de.laser.Provider; java.time.Year; de.laser.finance.CostItem; de.laser.RefdataValue; de.laser.survey.SurveyInfo; de.laser.TitleInstancePackagePlatform; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Address; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.Contact; de.laser.remote.ApiSource" %>

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

    <div class="ui grid" style="margin-top:1em">
        <div class="four wide column">
            <laser:render template="info/partial" model="${[context: 'inst']}"/>

            <div class="stats_subscription stats-menu">
                <div class="ui tiny header">${message(code: 'subscription.periodOfValidity.label')}</div>
                <div class="ui secondary wrapping menu">
                    <g:each in="${subscriptionTimelineMap.keySet()}" var="year">
                        <a href="#" class="item" data-tab="year-${year}"> ${year} </a>
                    </g:each>
%{--                    <a href="#" class="item" data-tab="year-*"> Alle </a>--}%
                </div>

                <div class="ui tiny header">${message(code: 'subscription.status.label')}</div>
                <div class="ui secondary wrapping menu la-tab-with-js">
                    <g:each in="${subscriptionMap}" var="subStatusId,subList">
                        <g:set var="subStatus" value="${RefdataValue.get(subStatusId)}" />
                        <a href="#" class="item ${subStatus == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="subscription-${subStatus.id}">
                            ${subStatus.getI10n('value')} <span class="ui blue circular tiny label">${subList.size()}</span>
                        </a>
                    </g:each>
                </div>
            </div>

            <div class="stats_license stats-menu">
                <div class="ui tiny header">${message(code: 'subscription.periodOfValidity.label')}</div>
                <div class="ui secondary wrapping menu">
                    <g:each in="${licenseTimelineMap.keySet()}" var="year">
                        <a href="#" class="item" data-tab="year-${year}"> ${year} </a>
                    </g:each>
%{--                    <a href="#" class="item" data-tab="year-*"> Alle </a>--}%
                </div>

                <div class="ui tiny header">${message(code: 'license.status.label')}</div>
                <div class="ui secondary wrapping menu la-tab-with-js">
                    <g:each in="${licenseMap}" var="licStatus,licList">
                        <g:set var="licStatusRdv" value="${RefdataValue.get(licStatus)}" />
                        <a href="#" class="item ${licStatusRdv == RDStore.LICENSE_CURRENT ? 'active' : ''}" data-tab="license-${licStatusRdv.id}">
                            ${licStatusRdv.getI10n('value')} <span class="ui blue circular tiny label">${licList.size()}</span>
                        </a>
                    </g:each>
                </div>
            </div>

            <div class="stats_provider stats-menu">
                <div class="ui tiny header">${message(code: 'subscription.periodOfValidity.label')}</div>
                <div class="ui secondary wrapping menu">
                    <g:each in="${providerTimelineMap.keySet()}" var="year">
                        <a href="#" class="item" data-tab="year-${year}"> ${year} </a>
                    </g:each>
%{--                    <a href="#" class="item" data-tab="year-*"> Alle </a>--}%
                </div>

                <div class="ui tiny header">${message(code: 'provider.label')}</div>
                <div class="ui secondary wrapping menu la-tab-with-js">
                    <g:each in="${providerMap}" var="prov,subList" status="i">
                        <g:set var="provider" value="${Provider.get(prov)}" />
                        <a href="#" class="item ${i == 0 ? 'active' : ''}" data-tab="provider-${provider.id}">
                            ${provider.sortname ?: provider.name} <span class="ui blue circular tiny label">${subList.size()}</span>
                        </a>
                    </g:each>
                </div>
            </div>

%{--            <div class="stats_survey stats-menu">--}%
%{--                <div class="ui tiny header">${message(code: 'subscription.periodOfValidity.label')}</div>--}%
%{--                <div class="ui secondary wrapping menu">--}%
%{--                    <g:each in="${surveyTimelineMap.keySet()}" var="year">--}%
%{--                        <a href="#" class="item" data-tab="year-${year}"> ${year} </a>--}%
%{--                    --}%%{--                        <a href="#" class="item ${year == Year.now().toString() ? 'active' : ''}" data-tab="year-${year}"> ${year} </a>--}%
%{--                    </g:each>--}%
%{--                <a href="#" class="item" data-tab="year-*"> Alle </a>--}%
%{--                </div>--}%

%{--                <div class="ui tiny header">${message(code: 'default.status.label')}</div>--}%
%{--                <div class="ui secondary wrapping menu la-tab-with-js">--}%
%{--                    <g:each in="${surveyMap}" var="surveyStatus,surveyData">--}%
%{--                        <a href="#" class="item ${surveyStatus == 'open' ? 'active' : ''}" data-tab="survey-${surveyStatus}">--}%
%{--                            <uiSurvey:virtualState status="${surveyStatus}" />--}%
%{--                            <span class="ui blue circular tiny label">${surveyData.size()}</span>--}%
%{--                        </a>--}%
%{--                    </g:each>--}%
%{--                </div>--}%

%{--                <div>--}%
%{--                    <span class="ui checkbox">--}%
%{--                        <label for="survey-toggle-subscriptions">Lizenzen anzeigen</label>--}%
%{--                        <input type="checkbox" id="survey-toggle-subscriptions">--}%
%{--                    </span>--}%
%{--                </div>--}%
%{--            </div>--}%
        </div>
        <div class="twelve wide column">

            <g:set var="areStatsAvailableCache" value="[:]" />

            <div class="stats_subscription stats-content">
                <div class="chartWrapper" id="cw-subscription"></div>

                <g:each in="${subscriptionMap}" var="subStatusId,subList">
                    <g:set var="subStatus" value="${RefdataValue.get(subStatusId)}" />
                    <div class="ui tab segment ${subStatus == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="subscription-${subStatus.id}">

                        <table class="ui table very compact">
                            <thead>
                                <tr>
                                    <g:if test="${subStatus != RDStore.SUBSCRIPTION_CURRENT}">
                                        <th class="six wide">${message(code:'subscription.label')}</th>
                                        <th class="four wide">${message(code:'consortium.label')}</th>
                                    </g:if>
                                    <g:else>
                                        <th class="five wide">${message(code:'subscription.label')}</th>
                                        <th class="four wide">${message(code:'consortium.label')}</th>
                                        <th class="one wide"><ui:usageIcon /></th>
                                    </g:else>
                                    <th class="one wide"><ui:multiYearIcon /></th>
                                    <th class="one wide">${message(code:'subscription.referenceYear.label.shy')}</th>
                                    <th class="one wide">${message(code:'subscription.startDate.label')}</th>
                                    <th class="one wide">${message(code:'subscription.endDate.label')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each in="${subList}" var="subId">
                                    <g:set var="sub" value="${Subscription.get(subId)}" />
                                    <g:set var="orgCons" value="${sub.getConsortium()}" />
                                    <tr data-id="${subId}" data-referenceYear="${sub.referenceYear}">
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                                            </div>
                                        </td>
                                        <td>
                                            <g:if test="${orgCons}">
                                                <div class="la-flexbox la-minor-object">
                                                    <i class="${Icon.ORG} la-list-icon"></i>
                                                    <g:link controller="org" action="show" id="${orgCons.id}" target="_blank">${orgCons.name}</g:link>
                                                </div>
                                            </g:if>
                                        </td>
                                        <g:if test="${subStatus == RDStore.SUBSCRIPTION_CURRENT}">
                                            <td>
                                                <% if (! areStatsAvailableCache.containsKey(sub.id.toString())) {
                                                    areStatsAvailableCache.putAt(sub.id.toString(), sub.packages ? subscriptionService.areStatsAvailable(sub) : false)
                                                } %>

                                                <g:if test="${areStatsAvailableCache.get(sub.id.toString())}">
                                                    <g:link controller="subscription" action="stats" id="${sub.id}" target="_blank">${RDStore.YN_YES.getI10n('value')}</g:link>
                                                </g:if>
                                                <g:else>
                                                    ${RDStore.YN_NO.getI10n('value')}
                                                </g:else>
                                            </td>
                                        </g:if>
                                        <td> ${sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")} </td>
                                        <td> ${sub.referenceYear} </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>

                    </div>
                </g:each>
            </div>

            <div class="stats_license stats-content">
                <div class="chartWrapper" id="cw-license"></div>

                <g:each in="${licenseMap}" var="licStatus,licList">
                    <g:set var="licStatusRdv" value="${RefdataValue.get(licStatus)}" />
                    <div class="ui tab segment ${licStatusRdv == RDStore.LICENSE_CURRENT ? 'active' : ''}" data-tab="license-${licStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="six wide">${message(code:'license.label')}</th>
                                <th class="six wide">${message(code:'consortium.label')}</th>
                                <th class="two wide">${message(code:'license.openEnded.label')}</th>
                                <th class="one wide">${message(code:'license.startDate.label')}</th>
                                <th class="one wide">${message(code:'license.endDate.label')}</th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${licList}" var="licId">
                                    <g:set var="lic" value="${License.get(licId)}" />
                                    <g:set var="orgCons" value="${lic.getLicensingConsortium()}" />
                                    <tr data-id="${licId}">
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="${Icon.LICENSE} la-list-icon"></i>
                                                <g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link>
                                            </div>
                                        </td>
                                        <td>
                                            <g:if test="${orgCons}">
                                                <div class="la-flexbox la-minor-object">
                                                    <i class="${Icon.ORG} la-list-icon"></i>
                                                    <g:link controller="org" action="show" id="${orgCons.id}" target="_blank">${orgCons.name}</g:link>
                                                </div>
                                            </g:if>
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

            <div class="stats_provider stats-content">
                <div class="chartWrapper" id="cw-provider"></div>

                <g:each in="${providerMap}" var="prov,subList" status="i">
                    <g:set var="provider" value="${Provider.get(prov)}" />
                    <div class="ui tab segment ${i == 0 ? 'active' : ''}" data-tab="provider-${provider.id}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="five wide">${message(code:'subscription.label')}</th>
                                <th class="four wide">${message(code:'consortium.label')}</th>
                                <th class="two wide">${message(code:'default.status.label')}</th>
                                <th class="one wide"><ui:usageIcon /></th>
                                <th class="one wide"><ui:multiYearIcon /></th>
                                <th class="one wide">${message(code:'subscription.referenceYear.label.shy')}</th>
                                <th class="one wide">${message(code:'subscription.startDate.label')}</th>
                                <th class="one wide">${message(code:'subscription.endDate.label')}</th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${subList}" var="subId">
                                    <g:set var="sub" value="${Subscription.get(subId)}" />
                                    <g:set var="orgCons" value="${sub.getConsortium()}" />
                                    <tr data-id="${subId}" data-referenceYear="${sub.referenceYear}">
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                                            </div>
                                        </td>
                                        <td>
                                            <g:if test="${orgCons}">
                                                <div class="la-flexbox la-minor-object">
                                                    <i class="${Icon.ORG} la-list-icon"></i>
                                                    <g:link controller="org" action="show" id="${orgCons.id}" target="_blank">${orgCons.name}</g:link>
                                                </div>
                                            </g:if>
                                        </td>
                                        <td>
                                            ${sub.status.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:if test="${sub.status == RDStore.SUBSCRIPTION_CURRENT}">
                                                <% if (! areStatsAvailableCache.containsKey(sub.id.toString())) {
                                                    areStatsAvailableCache.putAt(sub.id.toString(), sub.packages ? subscriptionService.areStatsAvailable(sub) : false)
                                                } %>

                                                <g:if test="${areStatsAvailableCache.get(sub.id.toString())}">
                                                    <g:link controller="subscription" action="stats" id="${sub.id}" target="_blank">${RDStore.YN_YES.getI10n('value')}</g:link>
                                                </g:if>
                                                <g:else>
                                                    ${RDStore.YN_NO.getI10n('value')}
                                                </g:else>
                                            </g:if>
                                        </td>
                                        <td> ${sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")} </td>
                                        <td> ${sub.referenceYear} </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>

                    </div>
                </g:each>
            </div>

            <div class="stats_survey stats-content">
                <div class="chartWrapper" id="cw-survey"></div>

                <g:each in="${surveyMap}" var="surveyStatus,surveyData">
                    <div class="ui tab segment ${surveyStatus == 'open' ? 'active' : ''}" data-tab="survey-${surveyStatus}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="six wide">${message(code:'survey.label')}</th>
                                <th class="two wide">${message(code:'surveyInfo.owner.label')}</th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide">Teilnahme</th>
                                <th class="one wide">${message(code:'surveyInfo.type.label')}</th>
                                <th class="one wide">${message(code:'default.endDate.label')}</th>
                                <th class="one wide">${message(code:'default.status.label')}</th>
                            </tr>
                            <tr data-ctype="survey-subsciption" class="hidden">
                                <th class="six wide">${message(code:'subscription.label')}</th>
                                <th class="two wide">${message(code:'default.status.label')}</th>
                                <th class="one wide"><ui:multiYearIcon /></th>
                                <th class="one wide">${message(code:'subscription.referenceYear.label.shy')}</th>
                                <th class="one wide">${message(code:'subscription.startDate.label')}</th>
                                <th class="one wide">${message(code:'subscription.endDate.label')}</th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                                <th class="one wide"></th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${surveyData}" var="surveyStruct">
                                <g:set var="surveyInfo" value="${surveyStruct[0]}" />
                                <g:set var="surveyConfig" value="${surveyStruct[1]}" />
                                <g:set var="surveyOrg" value="${surveyStruct[2]}" />
                                <g:set var="orgCons" value="${surveyInfo.owner}" />

                                <tr data-id="${surveyInfo.id}">
                                    <td>
                                        <div class="la-flexbox la-minor-object">
                                            <i class="${Icon.SURVEY} la-list-icon"></i>
                                            <g:link controller="survey" action="show" id="${surveyInfo.id}" target="_blank">${surveyInfo.name}</g:link>
                                        </div>
                                    </td>
                                    <td colspan="5">
                                        <g:if test="${orgCons}">
                                            <div class="la-flexbox la-minor-object">
                                                <i class="${Icon.ORG} la-list-icon"></i>
                                                <g:link controller="org" action="show" id="${orgCons.id}" target="_blank">${orgCons.name}</g:link>
                                            </div>
                                        </g:if>
                                    </td>
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
                                    <tr data-id="${surveyInfo.id}" data-ctype="survey-subsciption" class="hidden sub">
                                        <td style="padding-left:2rem;">
                                            <div class="la-flexbox la-minor-object">
                                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                                            </div>
                                        </td>
                                        <td> ${sub.status.getI10n('value')} </td>
                                        <td> ${sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")} </td>
                                        <td> ${sub.referenceYear} </td>
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

    <div class="stats_cost stats-content">

        <div class="ui segment">
            <table class="ui table la-table celled very compact sortable">
                <thead>
                    <tr>
                        <th scope="col" rowspan="2">${message(code:'financials.newCosts.costTitle')}</th>
                            <th scope="col" class="la-smaller-table-head">${message(code:'default.subscription.label')}</th>
                            <th scope="col" rowspan="2" class="la-no-uppercase">
                                <span class="la-popup-tooltip" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center">
                                    <i class="${Icon.FNC.COST_CONFIG}"></i>
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
                                String icon         = '<i class="' + Icon.FNC.COST_NOT_SET + '"></i>'
                                String dataTooltip  = message(code:'financials.costItemConfiguration.notSet')

                                switch (ci.costItemElementConfiguration) {
                                    case RDStore.CIEC_POSITIVE:
                                        dataTooltip = message(code:'financials.costItemConfiguration.positive')
                                        icon = '<i class="' + Icon.FNC.COST_POSITIVE + '"></i>'
                                        break
                                    case RDStore.CIEC_NEGATIVE:
                                        dataTooltip = message(code:'financials.costItemConfiguration.negative')
                                        icon = '<i class="' + Icon.FNC.COST_NEGATIVE + '"></i>'
                                        break
                                    case RDStore.CIEC_NEUTRAL:
                                        dataTooltip = message(code:'financials.costItemConfiguration.neutral')
                                        icon = '<i class="' + Icon.FNC.COST_NEUTRAL + '"></i>'
                                        break
                                }
                            %>
                            <tr>
                                <td>
                                    <g:each in="${ci.sub.orgRelations}" var="or">
                                        <g:if test="${[RDStore.OR_SUBSCRIBER_CONS.id,RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id].contains(or.roleType.id)}">
                                            <g:link mapping="subfinance" params="[sub:ci.sub.id]" target="_blank">
                                                ${ci.costTitle ?: or.org.designation}
                                            </g:link>
                                            <g:if test="${ci.isVisibleForSubscriber}">
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                                                    <i class="${Icon.SIG.VISIBLE_ON} orange"></i>
                                                </span>
                                            </g:if>
                                        </g:if>
                                    </g:each>
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
                                    <span class="la-popup-tooltip" data-position="right center" data-content="${dataTooltip}">${raw(icon)}</span>
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

        </div>
    </div><!-- .grid -->

    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.info.chart_config = {
            subscription: {
                tooltip: JSPC.app.info.chart_config_helper.tooltip,
                series: [
                    <g:each in="${subscriptionTimelineMap.values().collect{ it.keySet() }.flatten().unique().sort{ RefdataValue.get(it).getI10n('value') }}" var="status">
                        {
                            name    : '${RefdataValue.get(status).getI10n('value')}',
                            type    : 'bar',
                            stack   : 'total',
                            animation : false,
                            data    : [${subscriptionTimelineMap.values().collect{ it[status] ? it[status].size() : 0 }.join(', ')}],
                            raw     : [${subscriptionTimelineMap.values().collect{ it[status] ?: [] }.join(', ')}],
                            color   : <%
                                String color = 'JSPC.colors.hex.grey'
                                switch (RefdataValue.get(status)) {
                                    case RDStore.SUBSCRIPTION_CURRENT:      color = 'JSPC.colors.hex.green'; break;
                                    case RDStore.SUBSCRIPTION_EXPIRED:      color = 'JSPC.colors.hex.blue'; break;
                                    case RDStore.SUBSCRIPTION_INTENDED:     color = 'JSPC.colors.hex.yellow'; break;
                                    case RDStore.SUBSCRIPTION_ORDERED:      color = 'JSPC.colors.hex.ice'; break;
                                    case RDStore.SUBSCRIPTION_TEST_ACCESS:  color = 'JSPC.colors.hex.orange'; break;
                                    case RDStore.SUBSCRIPTION_NO_STATUS:    color = 'JSPC.colors.hex.red'; break;
                                }
                                println color
                                %>
                        },
                    </g:each>
                        {
                            name    : '${message(code: 'subscription.isMultiYear.label')}',
                            type    : 'line',
                            smooth  : true,
                            lineStyle : JSPC.app.info.chart_config_helper.series_lineStyle,
                            animation : false,
                            data    : [<%
                                        List<Long> subsPerYear = subscriptionTimelineMap.values().collect{ it.values().flatten() }
                                        print subsPerYear.collect {
                                            it.collect{ Subscription.get(it).isMultiYear ? 1 : 0 }.sum() ?: 0
                                        }.join(', ')
                                        %>],
                            color   : JSPC.colors.hex.pink
                        },
                        {
                            name    : '${RDStore.SUBSCRIPTION_TYPE_LOCAL.getI10n('value')}',
                            type    : 'line',
                            smooth  : true,
                            lineStyle : JSPC.app.info.chart_config_helper.series_lineStyle,
                            animation : false,
                            data    : [<%
                                print subsPerYear.collect {
                                    it.collect{ Subscription.get(it).type == RDStore.SUBSCRIPTION_TYPE_LOCAL ? 1 : 0 }.sum() ?: 0
                                }.join(', ')
                            %>],
                            color   : JSPC.colors.hex.ice
                        },
                ],
                xAxis: {
                    type: 'category',
                    data: [${subscriptionTimelineMap.keySet().join(', ')}]
                },
                yAxis:  { type: 'value' },
                legend: { bottom: 0 },
                grid:   JSPC.app.info.chart_config_helper.grid,
            },
            license: {
                tooltip: JSPC.app.info.chart_config_helper.tooltip,
                series: [
                    <g:each in="${licenseTimelineMap.values().collect{ it.keySet() }.flatten().unique().sort{ RefdataValue.get(it).getI10n('value') }}" var="status">
                        {
                            name    : '${RefdataValue.get(status).getI10n('value')}',
                            type    : 'bar',
                            stack   : 'total',
                            animation : false,
                            data    : [${licenseTimelineMap.values().collect{ it[status] ? it[status].size() : 0 }.join(', ')}],
                            raw     : [${licenseTimelineMap.values().collect{ it[status] ?: [] }.join(', ')}],
                            color   : <%
                                color = 'JSPC.colors.hex.grey'
                                switch (RefdataValue.get(status)) {
                                    case RDStore.LICENSE_CURRENT:      color = 'JSPC.colors.hex.green'; break;
                                    case RDStore.LICENSE_EXPIRED:      color = 'JSPC.colors.hex.blue'; break;
                                    case RDStore.LICENSE_INTENDED:     color = 'JSPC.colors.hex.yellow'; break;
                                    case RDStore.LICENSE_NO_STATUS:    color = 'JSPC.colors.hex.red'; break;
                                }
                                println color
                            %>
                        },
                    </g:each>
                        {
                            name    : '${message(code: 'license.openEnded.label')}',
                            type    : 'line',
                            smooth  : true,
                            lineStyle : JSPC.app.info.chart_config_helper.series_lineStyle,
                            animation : false,
                            data    : [<%
                                        List<Long> licsPerYear = licenseTimelineMap.values().collect{ it.values().flatten() }
                                        print licsPerYear.collect {
                                            it.collect{ License.get(it).openEnded?.value == RDStore.YN_YES.value ? 1 : 0 }.sum() ?: 0
                                        }.join(', ')
                                        %>],
                            color   : JSPC.colors.hex.pink
                        },
                ],
                xAxis: {
                    type: 'category',
                    data: [${licenseTimelineMap.keySet().join(', ')}]
                },
                yAxis:  { type: 'value' },
                legend: { bottom: 0 },
                grid:   JSPC.app.info.chart_config_helper.grid,
            },
            provider: {
                tooltip: JSPC.app.info.chart_config_helper.tooltip,
                series: [
                <g:each in="${providerTimelineMap.values().collect{ it.keySet() }.flatten().unique().sort{ Provider.get(it).sortname ?: Provider.get(it).name }}" var="provider">
                    {
                        name    : '<% print Provider.get(provider).name %>',
                        type    : 'bar',
                        stack   : 'total',
                        animation : false,
                        data    : [${providerTimelineMap.values().collect{ it[provider] ? it[provider].size() : 0 }.join(', ')}],
                        raw     : [${providerTimelineMap.values().collect{ it[provider] ?: [] }.join(', ')}]
                    },
                </g:each>
                ],
                xAxis: {
                    type: 'category',
                    data: [${providerTimelineMap.keySet().join(', ')}]
                },
                yAxis:  { type: 'value' },
                legend: {
                    bottom: 0,
                    type: 'scroll'
                },
                grid:   JSPC.app.info.chart_config_helper.grid,
            },
%{--            survey: {--}%
%{--                tooltip: JSPC.app.info.chart_config_helper.tooltip,--}%
%{--                series: [--}%
%{--                    <g:each in="${surveyTimelineMap.values().collect{ it.keySet() }.flatten().unique()}" var="status"> --}%%{-- sort --}%
%{--                        {--}%
%{--                            name    : '<uiSurvey:virtualState status="${status}" />',--}%
%{--                            type    : 'bar',--}%
%{--                            stack   : 'total',--}%
%{--                            data    : [${surveyTimelineMap.values().collect{ it[status] ? it[status].size() : 0 }.join(', ')}],--}%
%{--                            raw     : [${surveyTimelineMap.values().collect{ it[status] ? it[status].collect{ it[0].id } : [] }.join(', ')}],--}%
%{--                            color   : <%--}%
%{--                                color = 'JSPC.colors.hex.grey'--}%
%{--                                switch (status) {--}%
%{--                                    case 'open':        color = 'JSPC.colors.hex.green'; break;--}%
%{--                                    case 'finish':      color = 'JSPC.colors.hex.blue'; break;--}%
%{--                                    case 'termination': color = 'JSPC.colors.hex.red'; break;--}%
%{--                                    case 'notFinish':   color = 'JSPC.colors.hex.yellow'; break;--}%
%{--                                }--}%
%{--                                println color--}%
%{--                            %>--}%
%{--                        },--}%
%{--                    </g:each>--}%
%{--                    <g:set var="surveyTypeTimeline" value="${surveyTimelineMap.values().collect{ it.values().collect{ it.collect{ it[0].type }}.flatten()}}" />--}%
%{--                    <g:each in="${surveyTypeTimeline.flatten().unique()}" var="type">--}%
%{--                        {--}%
%{--                            name    : '${type.getI10n('value')}',--}%
%{--                            type    : 'line',--}%
%{--                            smooth  : true,--}%
%{--                            lineStyle : JSPC.app.info.chart_config_helper.series_lineStyle,--}%
%{--                            data    : ${surveyTypeTimeline.collect{ it.findAll{ it2 -> it2 == type }.size() }},--}%
%{--                            color   : "<%--}%
%{--                                color = 'JSPC.colors.hex.grey'--}%
%{--                                switch (type) {--}%
%{--                                    case RDStore.SURVEY_TYPE_INTEREST:          color = '#ff9688'; break;--}%
%{--                                    case RDStore.SURVEY_TYPE_RENEWAL:           color = '#ebff82'; break;--}%
%{--                                    case RDStore.SURVEY_TYPE_SUBSCRIPTION:      color = '#fee8d2'; break;--}%
%{--                                    case RDStore.SURVEY_TYPE_TITLE_SELECTION:   color = '#45b2ff'; break;--}%
%{--                                }--}%
%{--                                print color--}%
%{--                            %>"--}%
%{--                        },--}%
%{--                    </g:each>--}%
%{--                ],--}%
%{--                xAxis: {--}%
%{--                    type: 'category',--}%
%{--                    data: [${surveyTimelineMap.keySet().join(', ')}]--}%
%{--                },--}%
%{--                yAxis:  { type: 'value' },--}%
%{--                legend: { bottom: 0 },--}%
%{--                grid:   JSPC.app.info.chart_config_helper.grid,--}%
%{--            },--}%
        };

        JSPC.app.info.charts = {
            subscription :  echarts.init ($('#cw-subscription')[0]),
            license :       echarts.init ($('#cw-license')[0]),
            provider :      echarts.init ($('#cw-provider')[0]),
%{--            survey :        echarts.init ($('#cw-survey')[0])--}%
        }

        JSPC.app.info.charts.subscription.setOption (JSPC.app.info.chart_config.subscription);
        JSPC.app.info.charts.license.setOption (JSPC.app.info.chart_config.license);
        JSPC.app.info.charts.provider.setOption (JSPC.app.info.chart_config.provider);
%{--        JSPC.app.info.charts.survey.setOption (JSPC.app.info.chart_config.survey);--}%

%{--        $( ['subscription', 'license', 'provider', 'survey'] ).each( function(i) {--}%
        $( ['subscription', 'license', 'provider'] ).each( function(i) {
            let statsId     = '.stats_' + this
            let chart       = JSPC.app.info.charts[this]
            let chartConfig = JSPC.app.info.chart_config[this]

%{--            console.log( statsId )--}%
%{--            console.log( chart )--}%
%{--            console.log( chartConfig )--}%

            chart.on ('click', function (params) {
                let t = statsId.replace('.stats_', '')
                let y = params.dataIndex
                let s = params.seriesIndex

%{--                console.log( statsId + ' -> ' + t + ' : ' + y + ' ' + s)--}%

                $(statsId + ' tr[data-id]').hide()

                $.each( $(statsId + ' .menu .item[data-tab^=' + t + ']'), function(i, e) {
                    if (chartConfig.series[i]) {
                        let yList = chartConfig.series[i].raw[y]
                        JSPC.app.info.setCounter($(e), yList.length)

                        yList.forEach((f) => {
                            $(statsId + ' tr[data-id=' + f + ']').show()
                        })
                    }
                })
                // chart.dispatchAction({ type: 'select', dataIndex: y })
                $($(statsId + ' .menu .item[data-tab^=' + t + ']')[s]).trigger('click')

                $(statsId + ' .menu .item[data-tab^=year-]').removeClass('active')
                $(statsId + ' .menu .item[data-tab=year-' + params.name + ']').addClass('active')
            });

        });

%{--        $( ['subscription', 'license', 'provider', 'survey'] ).each( function(i) {--}%
        $( ['subscription', 'license', 'provider'] ).each( function(i) {
            let statsId = '.stats_' + this
            let chart   = JSPC.app.info.charts[this]
%{--            console.log( statsId + ' ' + chart )--}%

            let $years = $(statsId + ' .menu .item[data-tab^=year-]')
            $years.on ('click', function() {
%{--                console.log(this)--}%
                $years.removeClass('active')
                $(this).addClass('active')

                let y = $(this).attr('data-tab')
                if (y == 'year-*') {
%{--                    $(statsId + ' tr[data-id]').show()--}%
                }
                else {
                    $years.each( function(i, e) {
                        if ($(e).attr('data-tab') == y) {
                            chart.trigger('click', {type: 'click', name: y.replace('year-', ''), dataIndex: i})
                        }
                    })
                }
            });

            $(statsId + ' .menu .item[data-tab=year-${Year.now()}]').trigger('click'); // init
        });

%{--        $('#survey-toggle-subscriptions').on('change', function() {--}%
%{--            if ($(this).prop('checked')) {--}%
%{--                $('table *[data-ctype=survey-subsciption]').removeClass('hidden')--}%
%{--            } else {--}%
%{--                $('table *[data-ctype=survey-subsciption]').addClass('hidden')--}%
%{--            }--}%
%{--        })--}%

    </laser:script>

<laser:htmlEnd />