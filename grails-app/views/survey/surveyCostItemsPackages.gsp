<%@ page import="de.laser.helper.Icons; de.laser.storage.RDConstants; de.laser.survey.SurveyConfig;de.laser.RefdataValue;de.laser.finance.CostItem;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition; de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyCostItems.label')})" serviceInjection="true"/>

<ui:breadcrumbs>
%{--    <ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>--}%
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"--}%
%{--                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}"/>--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyCostItems.label" class="active"/>--}%
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="${Icons.SUBSCRIPTION} bordered inverted orange la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.subscription}">
        <i class="${Icons.SUBSCRIPTION} icon la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyCostItemsPackages.label')}
</h2>

<br />

<g:if test="${surveyConfig}">
    <div class="ui grid">

        <div class="sixteen wide stretched column">
            <div class="ui top attached stackable tabular la-tab-with-js menu">
                <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'selectedSubParticipants', selectedCostItemElementID: selectedCostItemElementID, selectedPackageID: selectedPackageID]">
                    ${message(code: 'surveyParticipants.selectedSubParticipants')}
                    <span class="ui floating blue circular label">${selectedSubParticipants?.size() ?: 0}</span>
                </g:link>

                <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'selectedParticipants', selectedCostItemElementID: selectedCostItemElementID, selectedPackageID: selectedPackageID]">
                    ${message(code: 'surveyParticipants.selectedParticipants')}
                    <span class="ui floating blue circular label">${selectedParticipants?.size() ?: 0}</span>
                </g:link>

            </div>

        <div class="ui bottom attached tab segment active">

            <div class="four wide column">

                <g:if test="${params.tab == 'selectedSubParticipants' && selectedSubParticipants.size() > 0}">
                    <button onclick="JSPC.app.addForAllSurveyCostItem([${(selectedSubParticipants?.id)}])"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="surveyCostItems.createInitialCostItem"/>
                    </button>
                </g:if>

                <g:if test="${params.tab == 'selectedParticipants' && selectedParticipants.size() > 0}">
                    <button onclick="JSPC.app.addForAllSurveyCostItem([${(selectedParticipants?.id)}])"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="surveyCostItems.createInitialCostItem"/>
                    </button>
                </g:if>

                <br>
                <br>
                %{--<a class="ui right floated button" data-ui="modal" href="#bulkCostItemsUpload"><g:message code="menu.institutions.financeImport"/></a>--}%
                <br>
                <br>
            </div



            <br />
            <br />

            <ui:filter>
                <g:form action="surveyCostItems" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, selectedCostItemElementID: selectedCostItemElementID, selectedPackageID: selectedPackageID]">
                <laser:render template="/templates/filter/orgFilter"
                model="[
                                  tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex']],
                                  tmplConfigFormFilter: true
                          ]"/>
                </g:form>
            </ui:filter>

            <g:render template="costItemsByCostItemElementAndPkgTable"/>

            <br/>
            <div class="field" style="text-align: right;">
                <button id="bulkCostItems-toggle"
                        class="ui button"><g:message code="financials.bulkCostItems.show"/></button>
                <laser:script file="${this.getGroovyPageFileName()}">
                    $('#bulkCostItems-toggle').on('click', function () {
                        $('#bulkCostItems').toggleClass('hidden')
                        if ($('#bulkCostItems').hasClass('hidden')) {
                            $(this).text("${g.message(code: 'financials.bulkCostItems.show')}")
                                                    } else {
                                                        $(this).text("${g.message(code: 'financials.bulkCostItems.hidden')}")
                                                    }
                                                })
                </laser:script>
            </div>

            <g:form action="processSurveyCostItemsBulk" data-confirm-id="processSurveyCostItemsBulk_form" name="editCost_${idSuffix}" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, bulkSelectedCostItemElementID: selectedCostItemElementID, selectedPackageID: selectedPackageID]">

                <div id="bulkCostItems" class="hidden">
                    <g:if test="${countCostItems == 0}">
                        <ui:msg icon="info" message="surveyCostItems.bulkOption.info"/>
                    </g:if>
                    <g:else>
                        <h3 class="ui header"><span class="la-long-tooltip la-popup-tooltip la-delay"
                                                    data-position="right center"
                                                    data-content="${message(code: 'surveyCostItems.bulkOption.info')}">
                            ${message(code: 'surveyCostItems.bulkOption.label')}
                            <i class="grey question circle icon"></i>
                        </span></h3>

                        <div class="ui basic segment">

                            <laser:render template="costItemInputSurvey"/>

                            <g:if test="${params.tab == 'selectedSubParticipants'}">
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

                            <div class="ui horizontal divider"><g:message code="search.advancedSearch.option.OR"/></div>

                            <div class="fields la-forms-grid">
                                <fieldset class="sixteen wide field la-account-currency">
                                    <div class="field center aligned">

                                        <label>${message(code: 'surveyCostItems.bulkOption.percentOnSurveyPrice')}</label>

                                        <div class="ui right labeled input">
                                            <input type="number"
                                                   name="percentOnSurveyPrice"
                                                   id="percentOnSurveyPrice"
                                                   placeholder="${g.message(code: 'surveyCostItems.bulkOption.percentOnSurveyPrice')}"
                                                   value="" step="0.01"/>

                                            <div class="ui basic label">%</div>
                                        </div>
                                    </div>
                                </fieldset>
                            </div>

                        </div>

                        <div class="two fields">
                            <div class="eight wide field" style="text-align: left;">
                                <button class="ui button"
                                        type="submit">${message(code: 'default.button.save_changes')}</button>
                            </div>

                            <div class="eight wide field" style="text-align: right;">
                            </div>
                        </div>
                    </g:else>


                </div>



                <g:if test="${params.tab == 'selectedSubParticipants'}">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedSubParticipants?.findAll { it.hasInstAdmin() }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-ui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>

                        <br />
                        <br />

                        <laser:render template="/templates/filter/orgFilterTable"
                                  model="[orgList         : surveyParticipantsHasAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow  : ['lineNumber', 'sortname', 'name', (surveyConfig.subscription ? 'surveySubInfo' : ''), 'surveyCostItemPackage'],
                                          tableID         : 'costTable'
                                  ]"/>

                    </div>

                    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedSubParticipants?.findAll { !it.hasInstAdmin() }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-ui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>

                        <br />
                        <br />

                        <laser:render template="/templates/filter/orgFilterTable"
                                  model="[orgList       : surveyParticipantsHasNotAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow: ['lineNumber', 'sortname', 'name', (surveyConfig.subscription ? 'surveySubInfo' : ''), 'surveyCostItemPackage'],
                                          tableID       : 'costTable'
                                  ]"/>

                    </div>

                </g:if>


                <g:if test="${params.tab == 'selectedParticipants'}">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>


                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedParticipants?.findAll { it.hasInstAdmin() }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-ui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>

                    </div>

                    <br />
                    <br />

                    <laser:render template="/templates/filter/orgFilterTable"
                              model="[orgList       : surveyParticipantsHasAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveyCostItemPackage'],
                                      tableID       : 'costTable'
                              ]"/>


                    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedParticipants?.findAll { !it.hasInstAdmin() }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-ui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>
                    </div>


                    <br />
                    <br />

                    <laser:render template="/templates/filter/orgFilterTable"
                              model="[orgList         : surveyParticipantsHasNotAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveyCostItemPackage'],
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
                    <i class="${Icons.CMD_DELETE} icon"></i> ${message(code: "surveyCostItems.bulkOption.delete")}
                </button>

            </g:form>
            <br />
            <br />

            <g:form action="setSurveyWorkFlowInfos" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, setSurveyWorkFlowInfo: 'workflowCostItemsFinish']">

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
                                    orgsIDs: orgsIDs,
                                    selectedPkg: true,
                                    selectedCostItemElementID: "${selectedCostItemElementID}",
                                    selectedPackageID: "${selectedPackageID}"
                                }
                            }).done(function (data) {
                                $('#dynamicModalContainer').html(data);

                                $('#dynamicModalContainer .ui.modal').modal({
                                    onVisible: function () {
                                        r2d2.initDynamicUiStuff('#modalSurveyCostItem');
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

<laser:htmlEnd />
