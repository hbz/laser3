<%@ page import="de.laser.ExportClickMeService; de.laser.survey.SurveyConfigSubscription; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.Org; de.laser.survey.SurveyConfigPackage;" %>
<laser:serviceInjection/>

<g:if test="${contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
    <g:if test="${subscription}">
        <g:set var="previous" value="${subscription._getCalculatedPrevious()}"/>
        <g:set var="successor" value="${subscription._getCalculatedSuccessor()}"/>
        <laser:render template="/subscription/subscriptionTransferInfo" model="${[calculatedSubList: successor + [subscription] + previous]}"/>
    </g:if>

    <ui:actionsDropdown>
        <g:if test="${(actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia')}">
            <ui:actionsDropdownItem controller="survey" action="createGeneralSurvey" message="createGeneralSurvey.label"/>

            <ui:actionsDropdownItem controller="survey" action="createSubscriptionSurvey" message="createSubscriptionSurvey.label"/>

            <ui:actionsDropdownItem controller="survey" action="createIssueEntitlementsSurvey" message="createIssueEntitlementsSurvey.label"/>
        </g:if>
        <g:else>

            <ui:actionsDropdownItem message="template.addNote" data-ui="modal" href="#modalCreateNote"/>
            <ui:actionsDropdownItem message="task.create.new" data-ui="modal" href="#modalCreateTask"/>
            <ui:actionsDropdownItem message="template.documents.add" data-ui="modal" href="#modalCreateDocument"/>
            <div class="divider"></div>

            <ui:actionsDropdownItem controller="survey" action="copySurvey" params="[id: params.id]"
                                    message="copySurvey.label"/>

            <ui:actionsDropdownItem controller="survey" action="copyElementsIntoSurvey" params="[sourceObjectId: genericOIDService.getOID(surveyConfig)]"
                                    message="survey.copyElementsIntoSurvey"/>

            <g:if test="${surveyInfo.type in [RDStore.SURVEY_TYPE_SUBSCRIPTION] && surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_COMPLETED.id]}">
                <ui:actionsDropdownItem controller="survey" action="copySurveyCostItemsToSub" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]"
                                        message="surveyInfo.copySurveyCostItems"/>
            </g:if>
            <div class="ui divider"></div>

            <g:if test="${(actionName == 'surveyCostItems' || actionName == 'surveyCostItemsPackages' || actionName == 'surveyCostItemsSubscriptions') && (surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED])}">

                <g:if test="${participants.size() > 0}">
                    <g:if test="${actionName == 'surveyCostItems'}">
                        <ui:actionsDropdownItem onclick="JSPC.app.addForAllSurveyCostItem([${(participants?.id)}])" controller="survey"
                                                message="surveyCostItems.createInitialCostItem"/>
                        <ui:actionsDropdownItem data-ui="modal" href="#bulkCostItemsUpload" message="menu.institutions.financeImport"/>
                        <g:if test="${assignedCostItemElements}">
                            <ui:actionsDropdownItem data-ui="modal" id="openFinanceEnrichment" href="#financeEnrichment"
                                                    message="financials.enrichment.menu"/>
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="financials.enrichment.menu"
                                                            tooltip="${message(code: 'financials.enrichment.menu.disabled')}"/>
                        </g:else>
                    </g:if>

                    <g:if test="${actionName == 'surveyCostItemsPackages'}">
                        <g:if test="${SurveyConfigPackage.countBySurveyConfig(surveyConfig)}">
                            <ui:actionsDropdownItem onclick="JSPC.app.addForAllSurveyCostItem([${(participants?.id)}])" controller="survey"
                                                    message="surveyCostItems.createInitialCostItem"/>
                            <ui:actionsDropdownItem data-ui="modal" href="#bulkCostItemsUpload" message="menu.institutions.financeImport"/>
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="surveyCostItems.createInitialCostItem"
                                                            tooltip="${message(code: 'surveyPackages.addCosts.disable')}"/>
                            <ui:actionsDropdownItemDisabled message="menu.institutions.financeImport"
                                                            tooltip="${message(code: 'surveyPackages.addCosts.disable')}"/>
                        </g:else>
                        <g:if test="${assignedCostItemElements && assignedPackages}">
                            <ui:actionsDropdownItem data-ui="modal" id="openFinanceEnrichment" href="#financeEnrichment"
                                                    message="financials.enrichment.menu"/>
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="financials.enrichment.menu"
                                                            tooltip="${message(code: 'financials.enrichment.menu.disabled')}"/>
                        </g:else>
                    </g:if>

                    <g:if test="${actionName == 'surveyCostItemsSubscriptions'}">
                        <g:if test="${SurveyConfigSubscription.countBySurveyConfig(surveyConfig)}">
                            <ui:actionsDropdownItem onclick="JSPC.app.addForAllSurveyCostItem([${(participants?.id)}])" controller="survey"
                                                    message="surveyCostItems.createInitialCostItem"/>
                            <ui:actionsDropdownItem data-ui="modal" href="#bulkCostItemsUpload" message="menu.institutions.financeImport"/>
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="surveyCostItems.createInitialCostItem"
                                                            tooltip="${message(code: 'surveySubscriptions.addCosts.disable')}"/>
                            <ui:actionsDropdownItemDisabled message="menu.institutions.financeImport"
                                                            tooltip="${message(code: 'surveySubscriptions.addCosts.disable')}"/>
                        </g:else>
                        <g:if test="${assignedCostItemElements && assignedSubscriptions}">
                            <ui:actionsDropdownItem data-ui="modal" id="openFinanceEnrichment" href="#financeEnrichment"
                                                    message="financials.enrichment.menu"/>
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="financials.enrichment.menu"
                                                            tooltip="${message(code: 'financials.enrichment.menu.disabled')}"/>
                        </g:else>
                    </g:if>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="surveyCostItems.createInitialCostItem"
                                                    tooltip="${message(code: "survey.copyEmailaddresses.NoParticipants.info")}"/>
                    <ui:actionsDropdownItemDisabled message="menu.institutions.financeImport"
                                                    tooltip="${message(code: "survey.copyEmailaddresses.NoParticipants.info")}"/>
                    <ui:actionsDropdownItemDisabled message="financials.enrichment.menu"
                                                    tooltip="${message(code: 'survey.copyEmailaddresses.NoParticipants.info')}"/>

                </g:else>
                <div class="ui divider"></div>

            </g:if>


            <g:if test="${surveyInfo && (surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED])}">
                <g:if test="${surveyConfig.vendorSurvey}">
                    <ui:actionsDropdownItem controller="survey" action="linkSurveyVendor"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id, initial: true]}"
                                            message="surveyVendors.linkVendor"/>
                </g:if>
                <g:if test="${surveyConfig.packageSurvey}">
                    <ui:actionsDropdownItem controller="survey" action="linkSurveyPackage"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}" message="surveyPackages.linkPackage.plural"/>
                </g:if>

                <g:if test="${surveyConfig.subscriptionSurvey}">
                    <ui:actionsDropdownItem controller="survey" action="linkSurveySubscription"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id, initial: true]}"
                                            message="surveySubscriptions.linkSubscription"/>
                </g:if>

                <g:if test="${surveyConfig.packageSurvey || surveyConfig.vendorSurvey || surveyConfig.subscriptionSurvey}">
                    <div class="ui divider"></div>
                </g:if>

                <ui:actionsDropdownItem controller="survey" action="addSurveyParticipants" params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                        text="${message(code: 'default.add.label', args: [message(code: 'surveyParticipants.label')])}"/>

                <g:if test="${surveyConfig.subscription}">
                    <ui:actionsDropdownItem controller="survey" action="actionSurveyParticipants"
                                            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, actionSurveyParticipants: 'addSubMembersToSurvey']"
                                            message="surveyParticipants.addSubMembersToSurvey"/>
                    <g:if test="${!surveyConfig.subSurveyUseForTransfer}">
                        <ui:actionsDropdownItem controller="survey" action="actionSurveyParticipants"
                                                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, actionSurveyParticipants: 'addMultiYearSubMembersToSurvey']"
                                                message="surveyParticipants.addMultiYearSubMembersToSurvey"/>
                    </g:if>
                </g:if>
                <div class="ui divider"></div>
            </g:if>

            <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_SURVEY_STARTED.id}">
                <ui:actionsDropdownItem controller="survey" action="openParticipantsAgain" params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                        message="openParticipantsAgain.button"/>

                <ui:actionsDropdownItem controller="survey" action="participantsReminder" params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                        message="participantsReminder.button"/>

                <div class="ui divider"></div>
            </g:if>

        %{-- Status Action Begin --}%
            <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_READY.id) && surveyInfo.checkOpenSurvey()}">
                <ui:actionsDropdownItem controller="survey" action="setStatus" params="[id: params.id, newStatus: 'processBackInProcessingSurvey']"
                                        message="backInProcessingSurvey.button"/>
            </g:if>

            <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id)}">
                <g:if test="${surveyInfo.checkOpenSurvey()}">
                    <g:if test="${(surveyConfig.subscription && surveyConfig.comment) || surveyConfig.commentForNewParticipants}">
                        <ui:actionsDropdownItem controller="survey" action="setStatus" params="[id: params.id, newStatus: 'processOpenSurvey']"
                                                message="openSurvey.button"
                                                tooltip="${message(code: "openSurvey.button.info2")}"/>
                        <ui:actionsDropdownItem data-ui="modal"
                                                href="#openSurveyNow"
                                                message="openSurveyNow.button"/>
                    </g:if>
                    <g:else>
                        <ui:actionsDropdownItem data-ui="modal"
                                                href="#openSurveyNoComment"
                                                message="openSurvey.button"
                                                tooltip="${message(code: "openSurvey.button.info2")}"/>
                        <ui:actionsDropdownItem data-ui="modal"
                                                href="#openSurveyNowNoComment"
                                                message="openSurveyNow.button"/>
                    </g:else>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="openSurvey.button" tooltip="${message(code: "openSurvey.button.info")}"/>
                    <ui:actionsDropdownItemDisabled message="openSurveyNow.button" tooltip="${message(code: "openSurveyNow.button.info")}"/>
                </g:else>
            </g:if>


            <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_SURVEY_STARTED.id}">
                <ui:actionsDropdownItem data-ui="modal"
                                        href="#endSurveyNow"
                                        message="endSurvey.button"/>
            </g:if>

            <g:if test="${surveyInfo && surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_COMPLETED.id]}">

                <ui:actionsDropdownItem data-ui="modal"
                                        href="#openSurveyAgain"
                                        message="openSurveyAgain.button"/>

            %{--<ui:actionsDropdownItem controller="survey" action="participantsReminder" params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                    message="participantsReminder.button"/>--}%
            </g:if>

        %{-- Only for Survey with Renewal  Beginn --}%
            <g:if test="${surveyInfo && surveyConfig && surveyConfig.subSurveyUseForTransfer
                    && surveyInfo.status.id in [RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_COMPLETED.id]}">

                <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_SURVEY_COMPLETED.id}">
                    <ui:actionsDropdownItem controller="survey" action="setStatus" params="[id: params.id, newStatus: 'setInEvaluation']"
                                            message="evaluateSurvey.button"/>
                </g:if>

                <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_IN_EVALUATION.id}">
                    <ui:actionsDropdownItem controller="survey" action="setStatus" params="[id: params.id, newStatus: 'setCompleted']"
                                            message="completeSurvey.button"/>
                </g:if>
            </g:if>
        %{-- Only for Survey with Renewal End --}%
        %{-- Status Action End --}%

            <g:if test="${(!surveyConfig.subSurveyUseForTransfer) && surveyInfo && surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_SURVEY_COMPLETED.id]}">
                <ui:actionsDropdownItem controller="survey" action="setStatus" params="[id: params.id, newStatus: 'setCompleted']"
                                        message="completeSurvey.button"/>

            </g:if>
            <div class="ui divider"></div>

            <g:if test="${surveyConfig.subSurveyUseForTransfer && !(surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])}">
                <ui:actionsDropdownItem controller="survey"  action="surveyTransfer"
                                        params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                        message="surveyTransfer.action"/>
                <div class="ui divider"></div>
            </g:if>

            <ui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id]"
                                    message="survey.SurveyProp.all"/>

            <div class="ui divider"></div>


            <g:if test="${surveyConfig.orgs}">
                <ui:actionsDropdownItem data-ui="modal"
                                        href="#copyEmailaddresses_static"
                                        message="survey.copyEmailaddresses.participants"/>

                <g:set var="orgs"
                       value="${Org.findAllByIdInList(surveyConfig.orgs?.org?.flatten().unique { a, b -> a?.id <=> b?.id }.id)?.sort { it.sortname }}"/>

                <laser:render template="/templates/copyEmailaddresses"
                              model="[modalID: 'copyEmailaddresses_static', orgList: orgs ?: null]"/>
            </g:if>
            <g:else>
                <ui:actionsDropdownItemDisabled message="survey.copyEmailaddresses.participants"
                                                tooltip="${message(code: "survey.copyEmailaddresses.NoParticipants.info")}"/>
            </g:else>


            <div class="ui divider"></div>

            <g:link class="item js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.survey", args: [surveyConfig.getSurveyName()])}"
                    data-confirm-term-how="delete"
                    controller="survey" action="deleteSurveyInfo"
                    id="${surveyInfo.id}"
                    role="button"
                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                <i class="${Icon.CMD.DELETE}"></i> ${message(code: 'deletion.survey')}
            </g:link>

        </g:else>
    </ui:actionsDropdown>


    <g:if test="${surveyInfo && surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_COMPLETED.id]}">
        <ui:modal id="openSurveyAgain" text="${message(code: 'openSurveyAgain.button')}" msgSave="${message(code: 'openSurveyAgain.button')}">

            <g:form class="ui form"
                    url="[controller: 'survey', action: 'setStatus', params: [id: params.id, surveyConfigID: surveyConfig.id, newStatus: 'openSurveyAgain'], method: 'post']">
                <div class="field">
                    <ui:datepicker label="surveyInfo.endDate.new" id="newEndDate" name="newEndDate" placeholder="surveyInfo.endDate.new"/>
                </div>
            </g:form>

        </ui:modal>
    </g:if>

    <g:if test="${(actionName == 'surveyCostItems' || actionName == 'surveyCostItemsPackages' || actionName == 'surveyCostItemsSubscriptions') && participants.size() > 0}">
        <laser:render template="/finance/financeEnrichment"/>
    </g:if>

    <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id) && surveyInfo.checkOpenSurvey()}">
        <ui:modal id="openSurveyNow" text="${message(code: 'openSurveyNow.button')}" msgSave="${message(code: 'openSurveyNow.button')}">

            <g:form class="ui form"
                    url="[controller: 'survey', action: 'setStatus', params: [id: params.id, startNow: true, newStatus: 'processOpenSurvey'], method: 'post']">
                <div class="field">
                    <p>${message(code: "openSurveyNow.button.info2")}</p>
                </div>
            </g:form>

        </ui:modal>
    </g:if>

    <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id) && surveyInfo.checkOpenSurvey()}">
        <ui:modal id="openSurveyNowNoComment" text="${message(code: 'openSurveyNow.button')}" msgSave="${message(code: 'openSurveyNow.button')}">

            <g:form class="ui form"
                    url="[controller: 'survey', action: 'setStatus', params: [id: params.id, startNow: true, newStatus: 'processOpenSurvey'], method: 'post']">
                <div class="field">
                    <h3>${message(code: "openSurvey.button.info3")}</h3>
                </div>

                <div class="field">
                    <p>${message(code: "openSurveyNow.button.info2")}</p>
                </div>

            </g:form>

        </ui:modal>
    </g:if>

    <g:if test="${surveyInfo && (surveyInfo.status.id == RDStore.SURVEY_IN_PROCESSING.id) && surveyInfo.checkOpenSurvey()}">
        <ui:modal id="openSurveyNoComment" text="${message(code: 'openSurvey.button')}" msgSave="${message(code: 'openSurvey.button')}">
            <g:form class="ui form"
                    url="[controller: 'survey', action: 'setStatus', params: [id: params.id, newStatus: 'processOpenSurvey'], method: 'post']">
                <div class="field">
                    <h3>${message(code: "openSurvey.button.info4")}</h3>
                </div>
            </g:form>

        </ui:modal>
    </g:if>

    <g:if test="${surveyInfo && surveyInfo.status.id == RDStore.SURVEY_SURVEY_STARTED.id}">
        <ui:modal id="endSurveyNow" text="${message(code: 'endSurvey.button')}"
                  msgSave="${message(code: 'endSurvey.button')}">

            <g:form class="ui form"
                    url="[controller: 'survey', action: 'setStatus', params: [id: params.id, newStatus: 'processEndSurvey'], method: 'post']">
                <div class="field">
                    <p><strong>${message(code: "endSurvey.button.info")}</strong></p>
                </div>
            </g:form>
        </ui:modal>
    </g:if>

    <g:if test="${(actionName != 'currentSurveysConsortia' && actionName != 'workflowsSurveysConsortia')}">
        <laser:render template="/templates/notes/modal_create" model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
        <laser:render template="/templates/tasks/modal_create" model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
        <laser:render template="/templates/documents/modal" model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
    </g:if>

    <g:if test="${(actionName == 'surveyCostItems' || actionName == 'surveyCostItemsPackages' || actionName == 'surveyCostItemsSubscriptions')}">
        <ui:modal id="bulkCostItemsUpload" message="menu.institutions.financeImport"
                  refreshModal="true"
                  msgSave="${g.message(code: 'menu.institutions.financeImport')}">
            <p>
                <g:link class="item" controller="public" action="manual" id="fileImport"
                        target="_blank">${message(code: 'help.technicalHelp.fileImport')}</g:link>
            </p>

            <p>
                <g:link class="csv" controller="survey" action="templateForSurveyCostItemsBulkWithUpload" params="[id: surveyInfo.id, format: ExportClickMeService.FORMAT.CSV, surveyConfigID: surveyConfig.id, costItemsForSurveySubscriptions: actionName == 'surveyCostItemsSubscriptions' ? 'true' : 'false', costItemsForSurveyPackage: actionName == 'surveyCostItemsPackages' ? 'true' : 'false']">${message(code: 'myinst.financeImport.template')}</g:link>
                <g:link class="xls" controller="survey" action="templateForSurveyCostItemsBulkWithUpload" params="[id: surveyInfo.id, format: ExportClickMeService.FORMAT.XLS, surveyConfigID: surveyConfig.id, costItemsForSurveySubscriptions: actionName == 'surveyCostItemsSubscriptions' ? 'true' : 'false', costItemsForSurveyPackage: actionName == 'surveyCostItemsPackages' ? 'true' : 'false']">${message(code: 'myinst.financeImport.template')}</g:link>
            </p>
            <g:render template="/templates/genericFileImportForm" model="[processAction: 'processSurveyCostItemsBulkWithUpload', id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyPackage: (actionName == 'surveyCostItemsPackages'), surveySubscriptions: (actionName == 'surveyCostItemsSubscriptions')]"/>
            %{--
            <g:form action="processSurveyCostItemsBulkWithUpload" controller="survey" method="post" class="ui form" enctype="multipart/form-data"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">
                <g:if test="${actionName == 'surveyCostItemsPackages'}">
                    <g:hiddenField name="costItemsForSurveyPackage" value="true"/>
                </g:if>
                <g:if test="${actionName == 'surveyCostItemsSubscriptions'}">
                    <g:hiddenField name="costItemsForSurveySubscriptions" value="true"/>
                </g:if>

                <br>

                <div class="ui field">
                    <div class="ui action input">
                        <input type="text" readonly="readonly"
                               placeholder="${message(code: 'template.addDocument.selectFile')}">
                        <input type="file" name="costItemsFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                               style="display: none;">

                        <div class="${Btn.ICON.SIMPLE}">
                            <i class="${Icon.CMD.ATTACHMENT}"></i>
                        </div>
                    </div>
                </div>
            </g:form>
            --}%
        </ui:modal>

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
                                    selectPkg: "${actionName == 'surveyCostItemsPackages' ? "true" : "false"}",
                                    selectedPackageID: "${selectedPackageID}",
                                    selectSubscription: "${actionName == 'surveyCostItemsSubscriptions' ? "true" : "false"}",
                                    selectedSurveyConfigSubscriptionID: "${selectedSurveyConfigSubscriptionID}",
                                    selectedCostItemElementID: "${selectedCostItemElementID}"

                                }
                            }).done(function (data) {
                                $('.ui.dimmer.modals > #addForAllSurveyCostItem').remove();
                                $('#dynamicModalContainer').empty().html(data);

                                $('#dynamicModalContainer .ui.modal').modal({
                                    onShow: function () {
                                        r2d2.initDynamicUiStuff('#addForAllSurveyCostItem');
                                        r2d2.initDynamicXEditableStuff('#addForAllSurveyCostItem');

                                    },
                                    detachable: true,
                                    transition: 'scale',
                                    onApprove: function () {
                                        $(this).find('#addForAllSurveyCostItem .ui.form').submit();
                                        return false;
                                    }
                                }).modal('show');
                            })
                            setTimeout(function () {
                                JSPC.app.isClicked = false;
                            }, 800);
                        }
                    };

        </laser:script>
        <g:render template="/templates/genericFileImportJS"/>
    </g:if>
</g:if>