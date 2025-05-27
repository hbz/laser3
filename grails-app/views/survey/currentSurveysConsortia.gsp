<%@ page import="de.laser.survey.SurveyConfigPackage; de.laser.survey.SurveyConfigSubscription; de.laser.survey.SurveyConfigVendor; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.survey.SurveyConfig; de.laser.RefdataCategory; de.laser.survey.SurveyResult; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem; de.laser.storage.RDConstants" %>

<laser:htmlStart message="currentSurveys.label"/>

<ui:breadcrumbs>
    <ui:crumb message="currentSurveys.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
%{--    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="survey" action="currentSurveysConsortia"
                    params="${params + [exportXLSX: true]}">${message(code: 'survey.exportSurveys')}</g:link>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:link class="item" controller="survey" action="currentSurveysConsortia"
                    params="${params + [exportXLSX: true, surveyCostItems: true]}">${message(code: 'survey.exportSurveyCostItems')}</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>--}%
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="currentSurveys.label" type="Survey" total="${surveysCount}" floated="true"/>

<ui:messages data="${flash}"/>

<ui:filter>
    <g:form action="currentSurveysConsortia" controller="survey" method="post" class="ui small form">
        <div class="four fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.name}"/>
                </div>
            </div>


            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}"/>
            </div>

            <div class="field">
                <label for="validOnYear">${message(code: 'default.valid_onYear.label')}</label>
                <select id="validOnYear" name="validOnYear" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <option value="all" <%=("all" in params.list('validOnYear')) ? 'selected="selected"' : ''%>>
                        ${message(code: 'default.select.all.label')}
                    </option>

                    <g:each in="${surveyYears}" var="surveyYear">
                        <option <%=(params.list('validOnYear').contains(surveyYear.toString())) ? 'selected="selected"' : ''%>
                                value="${surveyYear}" title="${surveyYear}">
                            ${surveyYear}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="filterStatus">${message(code: 'default.status.label')}</label>
                <select id="filterStatus" name="filterStatus" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SURVEY_STATUS)}"
                            var="status">
                        <option <%=Params.getLongList(params, 'filterStatus').contains(status.id) ? 'selected="selected"' : ''%>
                                value="${status.id}" title="${status.getI10n('value')}">
                            ${status.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>

        <div class="four fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.type.label')}</label>
                <ui:select class="ui dropdown clearable" name="type"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SURVEY_TYPE)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.type}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label for="filterPvd">${message(code: 'menu.my.providers')}</label>
                <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${providers.sort { it.name }}" var="provider">
                        <option <%=Params.getLongList(params, 'filterPvd').contains(provider.id) ? 'selected="selected"' : ''%>
                                value="${provider.id}">
                            ${provider.name}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="filterSub">${message(code: 'menu.my.subscriptions')}</label>
                <select id="filterSub" name="filterSub" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${subscriptions}" var="sub">
                        <option <%=(params.list('filterSub').contains(sub)) ? 'selected="selected"' : ''%>
                                value="${sub}">
                            ${sub}
                        </option>
                    </g:each>
                </select>

            </div>

            <laser:render template="/templates/properties/genericFilter"
                          model="[propList: propList, hideFilterProp: true, label: message(code: 'subscription.property.search')]"/>

        </div>

        <div class="one fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.options')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkMandatory">${message(code: 'surveyInfo.isMandatory.filter')}</label>
                            <input id="checkMandatory" name="mandatory" type="checkbox"
                                   <g:if test="${params.mandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkNoMandatory">${message(code: 'surveyInfo.isNotMandatory.filter')}</label>
                            <input id="checkNoMandatory" name="noMandatory" type="checkbox"
                                   <g:if test="${params.noMandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubSurveyUseForTransfer">${message(code: 'surveyconfig.subSurveyUseForTransfer.label')}</label>
                            <input id="checkSubSurveyUseForTransfer" name="checkSubSurveyUseForTransfer" type="checkbox"
                                   <g:if test="${params.checkSubSurveyUseForTransfer}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkPackageSurvey">${message(code: 'surveyconfig.packageSurvey.short')}</label>
                            <input id="checkPackageSurvey" name="checkPackageSurvey" type="checkbox"
                                   <g:if test="${params.checkPackageSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkVendorSurvey">${message(code: 'surveyconfig.vendorSurvey.short')}</label>
                            <input id="checkVendorSurvey" name="checkVendorSurvey" type="checkbox"
                                   <g:if test="${params.checkVendorSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkInvoicingInformation">${message(code: 'surveyconfig.invoicingInformation.short')}</label>
                            <input id="checkInvoicingInformation" name="checkInvoicingInformation" type="checkbox"
                                   <g:if test="${params.checkInvoicingInformation}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubscriptionSurvey">${message(code: 'surveyconfig.subscriptionSurvey.label')}</label>
                            <input id="checkSubscriptionSurvey" name="checkSubscriptionSurvey" type="checkbox"
                                   <g:if test="${params.checkSubscriptionSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <g:link controller="survey" action="currentSurveysConsortia"
                        class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</g:link>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</ui:filter>



<g:if test="${surveys}">

    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>

            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>

            <g:sortableColumn params="${params}" property="surInfo.name"
                              title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>
            <th rowspan="2" scope="col">${message(code: 'surveyInfo.type.label')}</th>
            <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                              property="surInfo.startDate"
                              title="${message(code: 'default.startDate.label.shy')}"/>
            <th rowspan="2" scope="col">${message(code: 'default.status.label')}</th>
            <th rowspan="2" scope="col">${message(code: 'surveyProperty.plural.label')}</th>


            <th rowspan="2" scope="col">
                <a href="#" class="la-popup-tooltip" data-content="${message(code: 'surveyConfigDocs.label')}" data-position="top center">
                    <i class="${Icon.DOCUMENT} large"></i>
                </a>
            </th>

            <th scope="col" rowspan="2">
                <a href="#" class="la-popup-tooltip" data-content="${message(code: 'surveyParticipants.label')}" data-position="top center">
                    <i class="${Icon.ATTR.SURVEY_PARTICIPANTS} large"></i>
                </a>
            </th>

            <th scope="col" rowspan="2">
                <a href="#" class="la-popup-tooltip" data-content="${message(code: 'package.plural')}" data-position="top center">
                    <i class="${Icon.PACKAGE} large"></i>
                </a>
            </th>

            <th scope="col" rowspan="2">
                <a href="#" class="la-popup-tooltip" data-content="${message(code: 'vendor.plural')}" data-position="top center">
                    <i class="${Icon.VENDOR} large"></i>
                </a>
            </th>

            <th scope="col" rowspan="2">
                <a href="#" class="la-popup-tooltip" data-content="${message(code: 'subscription.plural')}" data-position="top center">
                    <i class="${Icon.SUBSCRIPTION} large"></i>
                </a>
            </th>

            <th scope="col" rowspan="2">
                <a href="#" class="la-popup-tooltip" data-content="${message(code: 'surveyCostItems.label')}" data-position="top center">
                    <i class="${Icon.FNC.COST} large"></i>
                </a>
            </th>

            <th rowspan="2" scope="col">${message(code: 'surveyResult.label')}</th>

            %{--<th rowspan="2" scope="col">${message(code: 'surveyInfo.finished')}</th>--}%
            <th rowspan="2" scope="col" class="center aligned">
                <ui:optionsIcon />
            </th>
        </tr>
        <tr>
            <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                              property="surInfo.endDate"
                              title="${message(code: 'default.endDate.label.shy')}"/>
        </tr>
        </thead>
        <g:each in="${surveys}" var="survey" status="i">

            <g:set var="surveyInfo" value="${survey[0]}"/>

            <g:set var="surveyConfig" value="${survey[1]}"/>

        %{--<g:set var="participantsFinish"
               value="${SurveyResult.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).participant?.flatten()?.unique { a, b -> a.id <=> b.id }}"/>

        <g:set var="participantsTotal"
               value="${surveyConfig.orgs}"/>--}%

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>

                <td class="la-th-column">
                    <g:if test="${surveyConfig.invoicingInformation}">
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${message(code: "surveyconfig.invoicingInformation.short")}">
                            <i class="dollar icon la-list-icon"></i>
                        </span>
                    </g:if>
                    <g:link controller="survey" action="show" id="${surveyInfo.id}" class="la-main-object">
                        ${surveyConfig ? surveyConfig.getSurveyName() : surveyInfo.name}
                    </g:link>
                    <g:if test="${surveyConfig?.subscription}">
                        <g:set var="providers" value="${surveyConfig.subscription.getProviders()}"/>
                        <g:if test="${providers}">

                            <div class="la-flexbox la-minor-object">
                                (<g:each in="${providers}" var="provider">${provider.name}</g:each>)
                            </div>
                        </g:if>
                    </g:if>
                </td>

                <td class="center aligned">
                    <div class="ui label survey-${surveyInfo.type.value}">
                        ${surveyInfo.type.getI10n('value')}
                    </div>

                    <g:if test="${surveyInfo.isMandatory}">
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                            <i class="${Icon.TOOLTIP.IMPORTANT} yellow"></i>
                        </span>
                    </g:if>
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.startDate}"/>
                    <br/>
                    <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label.shy')}:">
                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/>
                    </span>
                </td>

                <td class="center aligned">
                    ${surveyInfo.status.getI10n('value')}
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig}">
                        <g:link controller="survey" action="show" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]">
                            <div class="ui circular ${surveyConfig.configFinish ? "green" : ""} label">
                                %{--Titel-Umfrage kann keine Umfrage-Merkmale haben--}%
                                ${surveyConfig.surveyProperties?.size() ?: 0}
                            </div>
                        </g:link>
                    </g:if>
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig}">
                        <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]">
                            <ui:bubble count="${surveyConfig.getCurrentDocs().size()}"/>
                        </g:link>
                    </g:if>
                </td>


                <td class="center aligned">
                    <g:if test="${surveyConfig}">
                        <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]">
                            <div class="ui circular ${surveyConfig.configFinish ? "green" : ""} label">
                                ${surveyConfig.orgs?.size() ?: 0}
                            </div>
                        %{--<div class="ui circular ${participantsFinish.size() == participantsTotal.size() ? "green" : surveyConfig.configFinish ? "yellow" : ""} label">
                            ${participantsFinish.size() ?: 0} / ${surveyConfig.orgs?.size() ?: 0}
                        </div>--}%
                        </g:link>
                    </g:if>
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig.packageSurvey}">
                        <g:link controller="survey" action="surveyPackages" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]">
                            <div class="ui circular label">
                                ${SurveyConfigPackage.countBySurveyConfig(surveyConfig)}
                            </div>
                        </g:link>
                    </g:if>
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig.vendorSurvey}">
                        <g:link controller="survey" action="surveyVendors" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]">
                            <div class="ui circular label">
                                ${SurveyConfigVendor.countBySurveyConfig(surveyConfig)}
                            </div>
                        </g:link>
                    </g:if>
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig.subscriptionSurvey}">
                        <g:link controller="survey" action="surveySubscriptions" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]">
                            <div class="ui circular label">
                                ${SurveyConfigSubscription.countBySurveyConfig(surveyConfig)}
                            </div>
                        </g:link>
                    </g:if>
                </td>



                <td class="center aligned">
                    <g:link controller="survey" action="surveyCostItems" id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]">
                        <div class="ui circular ${surveyConfig.costItemsFinish ? "green" : ""} label">
                            ${surveyConfig.getSurveyConfigCostItems().size() ?: 0}
                        </div>
                    </g:link>
                </td>

                <td class="center aligned">
                %{--<g:if test="${surveyConfig && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION && !surveyConfig.pickAndChoose}">
                    <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]"
                            class="${Btn.BASIC_ICON}">
                            --}%%{--<div class="ui circular ${(participantsFinish.size() == participantsTotal.size()) ? "green" : (participantsFinish.size() > 0) ? "yellow" : ""} label">
                            <g:if
                                    test="${participantsFinish && participantsTotal}">
                                <g:formatNumber
                                        number="${(participantsFinish.size() / participantsTotal.size()) * 100}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2"/>%
                            </g:if>
                            <g:else>
                                0%
                            </g:else>--}%%{--
                            <i class="${Icon.SURVEY} blue"></i>
                        </div>
                    </g:link>
                </g:if>--}%
                    <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]"
                            class="${Btn.MODERN.SIMPLE}">
                        <i class="${Icon.SURVEY}"></i>
                        </div>
                    </g:link>
                </td>
                <td>
                    <g:link controller="survey" action="show" id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]"
                            class="${Btn.MODERN.SIMPLE}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                        <i class="${Icon.CMD.EDIT}"></i>
                    </g:link>

                    <g:if test="${editable}">
                        <g:link controller="survey" action="copySurvey" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id, q: surveyConfig?.subscription?.providers ? surveyConfig.subscription.providers[0].name : '']"
                                class="${Btn.MODERN.SIMPLE}">
                            <i class="${Icon.CMD.COPY}"></i>
                            </div>
                        </g:link>
                    </g:if>
                </td>

            </tr>

        </g:each>
    </table>

</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "survey.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object" args="${[message(code: "survey.plural")]}"/></strong>
    </g:else>
</g:else>



<g:if test="${surveysCount}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                 max="${max}" total="${surveysCount}"/>
</g:if>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>

<laser:htmlEnd/>
