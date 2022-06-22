<%@ page import="de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.SurveyConfig; de.laser.RefdataCategory;de.laser.properties.PropertyDefinition; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.RDStore;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')} (${message(code: 'surveyCostItems.label')})</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>
    <semui:crumb message="surveyCostItems.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="exportSurCostItems" id="${surveyInfo.id}"
                    params="[exportXLSX: true, surveyConfigID: surveyConfig.id]">${message(code: 'survey.exportSurveyCostItems')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyCostItems"/>




<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyCostItems.label')}
</h2>

<br />

<g:if test="${surveyConfigs}">
    <div class="ui grid">
        %{--<div class="four wide column">
            <div class="ui vertical fluid menu">
        <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

        <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                style="${config?.costItemsFinish ? 'background-color: Lime' : ''}"
                controller="survey" action="surveyCostItems"
                id="${config?.surveyInfo.id}" params="[surveyConfigID: config?.id]">

            <h5 class="ui header">${config?.getConfigNameShort()}</h5>
            ${SurveyConfig.getLocalizedValue(config?.type)}


            <div class="ui floating circular label">${config?.orgs?.size() ?: 0}</div>
        </g:link>
        </g:each>
        </div>
        </div>--}%

        <div class="sixteen wide stretched column">
            <div class="ui top attached stackable tabular la-tab-with-js menu">
                <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'selectedSubParticipants']">
                    ${message(code: 'surveyParticipants.selectedSubParticipants')}
                    <div class="ui floating circular label">${selectedSubParticipants?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'selectedParticipants']">
                    ${message(code: 'surveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants?.size() ?: 0}</div>
                </g:link>

            </div>

        <div class="ui bottom attached tab segment active">

            <div class="four wide column">

                <g:if test="${params.tab == 'selectedSubParticipants'}">
                    <g:link onclick="JSPC.app.addForAllSurveyCostItem([${(selectedSubParticipants?.id)}])"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="surveyCostItems.createInitialCostItem"/>
                    </g:link>
                </g:if>

                <g:if test="${params.tab == 'selectedParticipants'}">
                    <g:link onclick="JSPC.app.addForAllSurveyCostItem([${(selectedParticipants?.id)}])"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="surveyCostItems.createInitialCostItem"/>
                    </g:link>
                </g:if>
            </div>

            <br />
            <br />

            <g:render template="/templates/filter/javascript" />

            <semui:filter showFilterButton="true">
                <g:form action="surveyCostItems" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
                <g:render template="/templates/filter/orgFilter"
                model="[
                                  tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value']],
                                  tmplConfigFormFilter: true
                          ]"/>
                </g:form>
            </semui:filter>

            <br><br>

            <g:form action="processSurveyCostItemsBulk" data-confirm-id="processSurveyCostItemsBulk_form" name="editCost_${idSuffix}" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']">

                <h3 class="ui header"><span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'surveyCostItems.bulkOption.info')}">
                    ${message(code: 'surveyCostItems.bulkOption.label')}
                        <i class="question circle icon"></i>
                    </span></h3>

                <div class="ui basic segment">

                    <g:render template="costItemInputSurvey" />

                    <g:if test="${params.tab == 'selectedSubParticipants' }">
                    <div class="ui horizontal divider"><g:message code="search.advancedSearch.option.OR"/></div>

                    <div class="fields la-forms-grid">
                        <fieldset class="sixteen wide field la-account-currency">
                            <div class="field center aligned">

                                <label>${message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}</label>
                                <div class="ui right labeled input">
                                <input type="number"
                                       name="percentOnOldPrice"
                                       id="percentOnOldPrice"
                                       placeholder="${g.message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}"
                                       value="" step="0.01"/>
                                <div class="ui basic label">%</div>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                    </g:if>

                </div>

                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
                    </div>

                    <div class="eight wide field" style="text-align: right;">
                    </div>
                </div>

                <g:if test="${params.tab == 'selectedSubParticipants'}">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedSubParticipants?.findAll { it?.hasAccessOrg() }?.sort {
                               it?.sortname
                           }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>

                        <br />
                        <br />

                        <g:render template="/templates/filter/orgFilterTable"
                                  model="[orgList         : surveyParticipantsHasAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveySubInfoStartEndDate', 'surveySubCostItem', 'surveyCostItem'],
                                          tableID         : 'costTable'
                                  ]"/>

                    </div>

                    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedSubParticipants?.findAll { !it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>

                        <br />
                        <br />

                        <g:render template="/templates/filter/orgFilterTable"
                                  model="[orgList       : surveyParticipantsHasNotAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveySubInfoStartEndDate', 'surveySubCostItem', 'surveyCostItem'],
                                          tableID       : 'costTable'
                                  ]"/>

                    </div>

                </g:if>


                <g:if test="${params.tab == 'selectedParticipants'}">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>


                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedParticipants?.findAll { it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>

                    </div>

                    <br />
                    <br />

                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList       : surveyParticipantsHasAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                      tableID       : 'costTable'
                              ]"/>


                    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedParticipants?.findAll { !it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>
                    </div>


                    <br />
                    <br />

                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList         : surveyParticipantsHasNotAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                      tableID         : 'costTable'
                              ]"/>

                </g:if>

                <br />
                <br />
                <button name="deleteCostItems" value="true" type="submit"
                        class="ui icon negative button js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyCostItems")}"
                        data-confirm-term-how="delete"
                        data-confirm-id="processSurveyCostItemsBulk"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="trash alternate outline icon"></i> ${message(code: "surveyCostItems.bulkOption.delete")}
                </button>

            </g:form>
            <br />
            <br />

            <g:form action="workflowCostItemsFinish" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

                <div class="ui right floated compact segment">
                    <div class="ui checkbox">
                        <input type="checkbox" onchange="this.form.submit()"
                               name="costItemsFinish" ${surveyConfig.costItemsFinish ? 'checked' : ''}>
                        <label><g:message code="surveyconfig.costItemsFinish.label"/></label>
                    </div>
                </div>

            </g:form>

        </div>
    </div>
</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">

JSPC.app.isClicked = false;

JSPC.app.addForAllSurveyCostItem = function(orgsIDs) {
                        event.preventDefault();

                        // prevent 2 Clicks open 2 Modals
                        if (!JSPC.app.isClicked) {
                            JSPC.app.isClicked = true;
                            $('.ui.dimmer.modals > #modalSurveyCostItem').remove();
                            $('#dynamicModalContainer').empty()

                           $.ajax({
                                url: "<g:createLink controller='survey' action='addForAllSurveyCostItem'/>",
                                traditional: true,
                                data: {
                                    id: "${params.id}",
                                    surveyConfigID: "${surveyConfig.id}",
                                    orgsIDs: orgsIDs
                                }
                            }).done(function (data) {
                                $('#dynamicModalContainer').html(data);

                                $('#dynamicModalContainer .ui.modal').modal({
                                    onVisible: function () {
                                        r2d2.initDynamicSemuiStuff('#modalSurveyCostItem');
                                        r2d2.initDynamicXEditableStuff('#modalSurveyCostItem');

                                    },
                                    detachable: true,
                                    closable: false,
                                    transition: 'scale',
                                    onApprove: function () {
                                        $(this).find('.ui.form').submit();
                                        return false;
                                    }
                                }).modal('show');
                            })
                            setTimeout(function () {
                                JSPC.app.isClicked = false;
                            }, 800);
                        }
                    }

</laser:script>

</body>
</html>
