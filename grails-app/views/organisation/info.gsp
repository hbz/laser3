<%@ page import="de.laser.survey.SurveyInfo; de.laser.TitleInstancePackagePlatform; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Address; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.Contact; de.laser.remote.ApiSource" %>

<laser:htmlStart message="menu.institutions.org.info" serviceInjection="true" />

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

        <h2 class="ui header" style="color:#fff;background-color:#f00;padding:1em 2em;margin:2em 0">DEMO</h2>

        <div class="ui six statistics">
            <div class="statistic"></div>
            <div class="statistic">
                <div class="value"> ${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id).size()} </div>
                <div class="label"> ${message(code: 'subscription.plural.current')} </div>
            </div>
            <div class="statistic">
                <div class="value"> ${licenseMap.get(RDStore.LICENSE_CURRENT.id).size()} </div>
                <div class="label"> ${message(code: 'license.plural.current')} </div>
            </div>
            <div class="statistic">
                <div class="value"> ${providerMap.get(RDStore.SUBSCRIPTION_CURRENT.id).collect{it.value[0]}.unique().size()} </div>
                <div class="label"> ${message(code:'default.provider.label')} (${message(code: 'subscription.plural.current')}) </div>
            </div>
            <div class="statistic">
                <div class="value"> ${surveyMap.get(false).size()} </div>
                <div class="label"> Offene Umfragen </div>
            </div>
            <div class="statistic"></div>
        </div>

        <h3 class="ui right aligned header">
            ${message(code:'subscription.plural')} <i class="icon clipboard" aria-hidden="true"></i>
        </h3>

        <div class="ui grid">
            <div class="four wide column">
                <div class="ui secondary vertical pointing fluid la-tab-with-js menu">
                    <g:each in="${subscriptionMap}" var="subStatus,subList">
                        <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                        <a href="#" class="item ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="sub-${subStatusRdv.id}">
                            ${subStatusRdv.getI10n('value')} <span class="ui blue circular label">${subList.size()}</span>
                        </a>
                    </g:each>
                </div>
            </div>
            <div class="twelve wide stretched column">
                <g:each in="${subscriptionMap}" var="subStatus,subList">
                    <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                    <div class="ui tab right attached segment ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="sub-${subStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                                <tr>
                                    <th class="eleven wide">${message(code:'subscription.label')}</th>
                                    <th class="one wide">${message(code:'subscription.referenceYear.label')}</th>
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
                                                <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link>
                                            </div>
                                        </td>
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
        </div>

        <h3 class="ui right aligned header">
            ${message(code:'license.plural')} <i class="icon balance scale" aria-hidden="true"></i>
        </h3>

        <div class="ui grid">
            <div class="four wide column">
                <div class="ui secondary vertical pointing fluid la-tab-with-js menu">
                    <g:each in="${licenseMap}" var="subStatus,licList">
                        <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                        <a href="#" class="item ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="lic-${subStatusRdv.id}">
                            ${subStatusRdv.getI10n('value')} <span class="ui blue circular label">${licList.size()}</span>
                        </a>
                    </g:each>
                </div>
            </div>
            <div class="twelve wide stretched column">
                <g:each in="${licenseMap}" var="subStatus,licList">
                    <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                    <div class="ui tab right attached segment ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="lic-${subStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="eleven wide">${message(code:'license.label')}</th>
                                <th class="one wide"></th>
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
                                                <g:link controller="license" action="show" id="${lic.id}">${lic.reference}</g:link>
                                            </div>
                                        </td>
                                        <td> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${lic.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${lic.endDate}"/> </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>

                    </div>
                </g:each>
            </div>
        </div>

        <h3 class="ui right aligned header">
            ${message(code:'default.provider.label')} <i class="icon university" aria-hidden="true"></i>
        </h3>

        <div class="ui grid">
            <div class="four wide column">
                <div class="ui secondary vertical pointing fluid la-tab-with-js menu">
                    <g:each in="${providerMap}" var="subStatus,provList">
                        <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                        <a href="#" class="item ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="prov-${subStatusRdv.id}">
                            ${subStatusRdv.getI10n('value')} <span class="ui blue circular label">${provList.collect{it.value[0]}.unique().size()}</span>
                        </a>
                    </g:each>
                </div>

                <div style="text-align: right">
                    <span class="ui checkbox">
                        <label for="provider-toggle-subscriptions">Lizenzen anzeigen</label>
                        <input type="checkbox" id="provider-toggle-subscriptions">
                    </span>
                </div>
            </div>
            <div class="twelve wide stretched column">
                <g:each in="${providerMap}" var="subStatus,provList">
                    <g:set var="subStatusRdv" value="${RefdataValue.get(subStatus)}" />
                    <div class="ui tab right attached segment ${subStatusRdv == RDStore.SUBSCRIPTION_CURRENT ? 'active' : ''}" data-tab="prov-${subStatusRdv.id}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="eleven wide">${message(code:'default.provider.label')}</th>
                                <th class="one wide">
                                    <span data-ctype="provider-subsciption" style="display:none;">${message(code:'subscription.referenceYear.label')}</span>
                                </th>
                                <th class="two wide">
                                    <span data-ctype="provider-subsciption" style="display:none;">${message(code:'subscription.startDate.label')}</span>
                                </th>
                                <th class="two wide">
                                    <span data-ctype="provider-subsciption" style="display:none;">${message(code:'subscription.endDate.label')}</span>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${provList.collect{it.value[0]}.unique()}" var="provId">
                                    <g:set var="prov" value="${Org.get(provId)}" />
                                    <tr>
                                        <td>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon university la-list-icon"></i>
                                                <g:link controller="org" action="show" id="${prov.id}">${prov.name}</g:link>
                                            </div>
                                        </td>
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
                                                            <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link>
                                                        </div>
                                                    </td>
                                                    <td> ${sub.referenceYear} </td>
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
        </div>

        <h3 class="ui right aligned header">
            ${message(code:'survey.plural')} <i class="icon pie chart" aria-hidden="true"></i>
        </h3>

        <div class="ui grid">
            <div class="four wide column">
                <div class="ui secondary vertical pointing fluid la-tab-with-js menu">
                    <g:each in="${surveyMap}" var="isSurveyFinished,surveyData">
                        <a href="#" class="item ${isSurveyFinished ? '' : 'active'}" data-tab="survey-${isSurveyFinished}">
                            ${isSurveyFinished ? 'Abgeschlossen':'Offen'} <span class="ui blue circular label">${surveyData.collect{it[0]}.unique().size()}</span>
                        </a>
                    </g:each>
                </div>
                <div style="text-align: right">
                    <span class="ui checkbox">
                        <label for="survey-toggle-subscriptions">Lizenzen anzeigen</label>
                        <input type="checkbox" id="survey-toggle-subscriptions">
                    </span>
                </div>
            </div>
            <div class="twelve wide stretched column">
                <g:each in="${surveyMap}" var="isSurveyFinished,surveyData">
                    <div class="ui tab right attached segment ${isSurveyFinished ? '' : 'active'}" data-tab="survey-${isSurveyFinished}">

                        <table class="ui table very compact">
                            <thead>
                            <tr>
                                <th class="eight wide">${message(code:'survey.label')}</th>
                                <th class="two wide">${message(code:'surveyInfo.type.label')}</th>
                                <th class="two wide">
                                    Teilnahme
                                    <span data-ctype="survey-subsciption" style="display:none;">/</span>
                                    <span data-ctype="survey-subsciption" style="display:none;">${message(code:'subscription.referenceYear.label')}</span>
                                </th>
                                <th class="two wide">
                                    Status
                                    <span data-ctype="survey-subsciption" style="display:none;">/</span>
                                    <span data-ctype="survey-subsciption" style="display:none;">${message(code:'subscription.startDate.label')}</span>
                                </th>
                                <th class="two wide">
                                    ${message(code:'default.endDate.label')}
                                    <span data-ctype="survey-subsciption" style="display:none;">/</span>
                                    <span data-ctype="survey-subsciption" style="display:none;">${message(code:'subscription.endDate.label')}</span>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${surveyData}" var="surveyStruct">
                                <g:set var="surveyInfo" value="${SurveyInfo.get(surveyStruct[0])}" />
                                <tr data-ctype="${surveyStruct[1] ? 'survey-finished' : 'survey-not-finished'}">
                                    <td>
                                        <div class="la-flexbox la-minor-object">
                                            <i class="icon pie chart la-list-icon"></i>
                                            <g:link controller="survey" action="show" id="${surveyInfo.id}">${surveyInfo.name}</g:link>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="ui label survey-${surveyInfo.type.value}">${surveyInfo.type.getI10n('value')}</span>
                                    </td>
                                    <td>
                                        <g:if test="${surveyStruct[1]}">
                                            <g:formatDate formatName="default.date.format.notime" date="${surveyStruct[1]}"/>
                                        </g:if>
                                    </td>
                                    <td>
                                        ${surveyInfo.status.getI10n('value')}
                                    </td>
                                    <td>
                                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/>
                                    </td>
                                </tr>

                                <g:if test="${surveyStruct[2]}">
                                    <g:set var="sub" value="${Subscription.get(surveyStruct[2])}" />
                                    <tr data-ctype="survey-subsciption" style="display:none;">
                                        <td style="padding-left:2rem;">
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon clipboard la-list-icon"></i>
                                                <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link>
                                            </div>
                                        </td>
                                        <td></td>
                                        <td> ${sub.referenceYear} </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>
                                        <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>
                                    </tr>
                                </g:if>
                            </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:each>
            </div>
        </div>

%{--        <div class="ui grid">--}%
%{--            <div class="four wide column">--}%
%{--                <div class="ui secondary vertical pointing fluid la-tab-with-js menu">--}%
%{--                    <g:each in="${surveyMap}" var="surveyStatus,surveyData">--}%
%{--                        <g:set var="surveyStatusRdv" value="${RefdataValue.get(surveyStatus)}" />--}%
%{--                        <a href="#" class="item ${surveyStatusRdv == RDStore.SURVEY_IN_EVALUATION? 'active' : ''}" data-tab="survey-${surveyStatusRdv.id}">--}%
%{--                            ${surveyStatusRdv.getI10n('value')} <span class="ui blue circular label">${surveyData.collect{it[0]}.unique().size()}</span>--}%
%{--                        </a>--}%
%{--                    </g:each>--}%
%{--                </div>--}%
%{--                <div style="text-align: right">--}%
%{--                    <span class="ui checkbox">--}%
%{--                        <label for="survey-toggle-subscriptions">Lizenzen anzeigen</label>--}%
%{--                        <input type="checkbox" id="survey-toggle-subscriptions">--}%
%{--                    </span>--}%
%{--                </div>--}%
%{--                <div style="text-align: right">--}%
%{--                    <span class="ui checkbox">--}%
%{--                        <label for="survey-toggle-finished">Teilnahme erfolgt</label>--}%
%{--                        <input type="checkbox" id="survey-toggle-finished">--}%
%{--                    </span>--}%
%{--                </div>--}%
%{--                <div style="text-align: right">--}%
%{--                    <span class="ui checkbox">--}%
%{--                        <label for="survey-toggle-not-finished">Teilname offen</label>--}%
%{--                        <input type="checkbox" id="survey-toggle-not-finished">--}%
%{--                    </span>--}%
%{--                </div>--}%
%{--            </div>--}%
%{--            <div class="twelve wide stretched column">--}%
%{--                <g:each in="${surveyMap}" var="surveyStatus,surveyData">--}%
%{--                    <g:set var="surveyStatusRdv" value="${RefdataValue.get(surveyStatus)}" />--}%
%{--                    <div class="ui tab right attached segment ${surveyStatusRdv == RDStore.SURVEY_IN_EVALUATION ? 'active' : ''}" data-tab="survey-${surveyStatusRdv.id}">--}%

%{--                        <table class="ui table very compact">--}%
%{--                            <thead>--}%
%{--                            <tr>--}%
%{--                                <th class="eight wide">${message(code:'survey.label')}</th>--}%
%{--                                <th class="three wide">${message(code:'surveyInfo.type.label')}</th>--}%
%{--                                <th class="one wide">Teilnahme</th>--}%
%{--                                <th class="two wide">${message(code:'default.startDate.label')}</th>--}%
%{--                                <th class="two wide">${message(code:'default.endDate.label')}</th>--}%
%{--                            </tr>--}%
%{--                            </thead>--}%
%{--                            <tbody>--}%
%{--                                <g:each in="${surveyData}" var="surveyStruct">--}%
%{--                                    <g:set var="surveyInfo" value="${SurveyInfo.get(surveyStruct[0])}" />--}%
%{--                                    <tr data-ctype="${surveyStruct[1] ? 'survey-finished' : 'survey-not-finished'}">--}%
%{--                                        <td>--}%
%{--                                            <div class="la-flexbox la-minor-object">--}%
%{--                                                <i class="icon pie chart la-list-icon"></i>--}%
%{--                                                <g:link controller="survey" action="show" id="${surveyInfo.id}">${surveyInfo.name}</g:link>--}%
%{--                                            </div>--}%
%{--                                        </td>--}%
%{--                                        <td> ${surveyInfo.type.getI10n('value')} </td>--}%
%{--                                        <td>--}%
%{--                                            <g:if test="${surveyStruct[1]}">--}%
%{--                                                <g:formatDate formatName="default.date.format.notime" date="${surveyStruct[1]}"/>--}%
%{--                                            </g:if>--}%
%{--                                        </td>--}%
%{--                                        <td> <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.startDate}"/> </td>--}%
%{--                                        <td> <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/> </td>--}%
%{--                                    </tr>--}%

%{--                                    <g:if test="${surveyStruct[2]}">--}%
%{--                                        <g:set var="sub" value="${Subscription.get(surveyStruct[2])}" />--}%
%{--                                        <tr data-ctype="survey-subsciption" style="display:none;">--}%
%{--                                            <td style="padding-left:2rem;">--}%
%{--                                                <div class="la-flexbox la-minor-object">--}%
%{--                                                    <i class="icon clipboard la-list-icon"></i>--}%
%{--                                                    <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link>--}%
%{--                                                </div>--}%
%{--                                            </td>--}%
%{--                                            <td></td>--}%
%{--                                            <td> ${sub.referenceYear} </td>--}%
%{--                                            <td> <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/> </td>--}%
%{--                                            <td> <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/> </td>--}%
%{--                                        </tr>--}%
%{--                                    </g:if>--}%
%{--                                </g:each>--}%
%{--                            </tbody>--}%
%{--                        </table>--}%
%{--                    </div>--}%
%{--                </g:each>--}%
%{--            </div>--}%
%{--        </div>--}%


    <style>
        h3.header > i.icon {
            vertical-align: baseline !important;
        }
    </style>

    <laser:script file="${this.getGroovyPageFileName()}">

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
%{--        $('#survey-toggle-finished').checkbox({--}%
%{--            onChange: function() {--}%
%{--                $('tr[data-ctype=survey-finished]').toggle()--}%
%{--            }--}%
%{--        })--}%
%{--        $('#survey-toggle-not-finished').checkbox({--}%
%{--            onChange: function() {--}%
%{--                $('tr[data-ctype=survey-not-finished]').toggle()--}%
%{--            }--}%
%{--        })--}%
    </laser:script>

<laser:htmlEnd />