<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDConstants; de.laser.survey.SurveyConfig;de.laser.RefdataValue;de.laser.finance.CostItem;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition; de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyCostItems.label')})"/>

<ui:breadcrumbs>
%{--    <ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>--}%
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
    %{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"--}%
    %{--                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}"/>--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}"/>
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
    <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}"
                       href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}"/>

<ui:messages data="${flash}"/>

<br/>

<g:if test="${surveyConfig}">

    <g:if test="${afterEnrichment}">
        <g:if test="${unknownCharsetError}">
            <ui:msg showIcon="true" class="error" message="financials.enrichment.unknownCharsetError"/>
        </g:if>
        <g:elseif test="${wrongSeparator}">
            <ui:msg showIcon="true" class="error" message="financials.enrichment.wrongSeparator"/>
        </g:elseif>
        <g:else>
            <g:if test="${matchCounter > 0}">
                <ui:msg showIcon="true" class="success" message="financials.enrichment.result" args="[matchCounter, totalRows]"/>
            </g:if>
            <g:else>
                <ui:msg showIcon="true" class="warning" message="financials.enrichment.emptyResult" args="[totalRows]"/>
            </g:else>
            <g:if test="${missing || wrongIdentifiers}">
                <ui:msg showIcon="true" class="error">
                    <g:if test="${missing}">
                       <g:message code="financials.enrichment.missingPrices"/>
                    </g:if>
                    <g:if test="${wrongIdentifiers}">
                        <g:message code="financials.enrichment.invalidIDs" args="[wrongIdentifierCounter]"/><br>
                        <g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, fileformat: fileformat]"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                    </g:if>
                </ui:msg>
            </g:if>
        </g:else>
    </g:if>



    <g:if test="${costItemsByCostItemElementOfSubs}">
        <g:render template="costItemsByCostItemElementTable"
                  model="${[costItemsByCTE: costItemsByCostItemElementOfSubs, header: g.message(code: 'costItem.label') + ' in ' + g.message(code: 'subscription.label')]}"/>
    </g:if>

    <g:render template="costItemsByCostItemElementTable"
              model="${[costItemsByCTE: costItemsByCostItemElement, header: g.message(code: 'costItem.label') + ' in ' + g.message(code: 'survey.label')]}"/>

    <br/>
    <br/>

    <ui:filter>
        <g:form action="surveyCostItems" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, selectedCostItemElementID: selectedCostItemElementID]">
            <laser:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['hasSubscription', 'subRunTimeMultiYear'], ['subscriptionAdjustDropdown']],
                                  tmplConfigFormFilter: true
                          ]"/>
        </g:form>
    </ui:filter>

    <br/>
    <g:if test="${participants.size() > 0}">
        <div class="field" style="text-align: right;">
            <button id="bulkCostItems-toggle"
                    class="${Btn.SIMPLE}"><g:message code="financials.bulkCostItems.show"/></button>
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
    </g:if>

    <g:form action="processSurveyCostItemsBulk" data-confirm-id="processSurveyCostItemsBulk_form" name="editCost_${idSuffix}" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, bulkSelectedCostItemElementID: selectedCostItemElementID]">

        <div id="bulkCostItems" class="hidden">
            <g:if test="${countCostItems == 0}">

                <div class="ui info message icon la-clear-before" style="display:flex">
                    <i class="info icon" aria-hidden="true"></i>

                    <div class="content">
                        <p><g:message code="surveyCostItems.bulkOption.info"/></p>
                    </div>
                </div>

            </g:if>
            <g:else>
                <h3 class="ui header"><span class="la-long-tooltip la-popup-tooltip"
                                            data-position="right center"
                                            data-content="${message(code: 'surveyCostItems.bulkOption.info')}">
                    ${message(code: 'surveyCostItems.bulkOption.label')}
                    <i class="${Icon.TOOLTIP.HELP}"></i>
                </span></h3>

                <div class="ui basic segment">

                    <laser:render template="costItemInputSurvey" model="[bulkCostItems: true]"/>

                    <g:if test="${surveyConfig.subscription}">
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
                        <button class="${Btn.SIMPLE}"
                                type="submit">${message(code: 'default.button.save_changes')}</button>
                    </div>

                    <div class="eight wide field" style="text-align: right;">
                    </div>
                </div>
            </g:else>

        </div>
        <g:if test="${surveyConfig.subscription}">
            <g:set var="tmplConfigShow" value="['lineNumber', 'sortname', 'name', 'surveySubInfo', 'surveySubCostItem', 'surveyCostItem']"/>
        </g:if>
        <g:else>
            <g:set var="tmplConfigShow" value="['lineNumber', 'sortname', 'name', 'surveyCostItem']"/>
        </g:else>



        <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

        <g:set var="surveyParticipantsHasAccess"
               value="${participants?.findAll { it.hasInstAdmin() }}"/>

        <div class="four wide column">
            <g:if test="${surveyParticipantsHasAccess}">
                <a data-ui="modal" class="${Btn.SIMPLE} right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}"
                   href="#copyEmailaddresses_static">
                    <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                </a>
            </g:if>

            <br/>
            <br/>

            <laser:render template="/templates/filter/orgFilterTable"
                          model="[orgList         : surveyParticipantsHasAccess,
                                  tmplShowCheckbox: editable,
                                  tmplConfigShow  : tmplConfigShow,
                                  tableID         : 'costTable'
                          ]"/>

        </div>

        <g:set var="surveyParticipantsHasNotAccess"
               value="${participants?.findAll { !it.hasInstAdmin() }}"/>

        <g:if test="${surveyParticipantsHasNotAccess}">
            <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>


            <div class="four wide column">

                <a data-ui="modal" class="${Btn.SIMPLE} right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}"
                   href="#copyEmailaddresses_static">
                    <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                </a>

                <br/>
                <br/>

                <laser:render template="/templates/filter/orgFilterTable"
                              model="[orgList         : surveyParticipantsHasNotAccess,
                                      tmplShowCheckbox: editable,
                                      tmplConfigShow  : tmplConfigShow,
                                      tableID         : 'costTable'
                              ]"/>

            </div>

        </g:if>



        <br/>
        <br/>
        <g:if test="${editable && participants}">
            <button name="deleteCostItems" value="true" type="submit" id="processSurveyCostItemsBulk_del_btn"
                    class="${Btn.NEGATIVE_CONFIRM}"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyCostItems")}"
                    data-confirm-term-how="delete"
                    data-confirm-id="processSurveyCostItemsBulk"
                    role="button"
                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                <i class="${Icon.CMD.DELETE}"></i> ${message(code: "surveyCostItems.bulkOption.delete")}
            </button>
        </g:if>

    </g:form>
    <br/>
    <br/>

    <g:if test="${editable}">
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
    </g:if>

</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>



<laser:htmlEnd/>
