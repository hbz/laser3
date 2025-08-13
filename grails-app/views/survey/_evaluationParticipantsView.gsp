<%@ page import="de.laser.survey.SurveySubscriptionResult; de.laser.survey.SurveyConfigProperties; de.laser.storage.PropertyStore; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyVendorResult; de.laser.survey.SurveyPackageResult; de.laser.Doc; de.laser.DocContext; de.laser.IssueEntitlementGroup; de.laser.config.ConfigMapper; de.laser.survey.SurveyConfig; de.laser.survey.SurveyResult; de.laser.Org; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;de.laser.RefdataCategory; de.laser.survey.SurveyOrg; de.laser.ExportService" %>
<laser:serviceInjection/>

<g:if test="${showOpenParticipantsAgainButtons}">
    <g:set var="mailSubject"
           value="${escapeService.replaceUmlaute(g.message(code: 'email.subject.surveys', args: ["${surveyConfig.surveyInfo.type.getI10n('value')}"]) + " " + surveyConfig.surveyInfo.name + "")}"/>
    <g:set var="mailBody" value="${surveyService.surveyMailHtmlAsString(surveyConfig.surveyInfo)}"/>
    <g:set var="mailString" value=""/>
</g:if>

<div id="downloadWrapper"></div>

<g:if test="${surveyConfig}">

    %{--<g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

    <g:if test="${surveyConfig.subscription}">

        <g:link class="${Btn.SIMPLE} right floated la-inline-labeled" controller="subscription" action="members" id="${subscription.id}">
            <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

            <ui:bubble count="${countParticipants.subMembers}"/>
        </g:link>

        <g:link class="${Btn.SIMPLE} right floated la-inline-labeled" controller="survey" action="surveyParticipants"
                id="${surveyConfig.surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id]">
            <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

            <ui:bubble count="${countParticipants.surveyMembers}"/>
        </g:link>

        <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
            ( ${countParticipants.subMembersWithMultiYear}
            ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
        </g:if>

    </g:if>

    <g:if test="${!surveyConfig.subscription}">
        <g:link class="${Btn.SIMPLE} right floated la-inline-labeled" controller="survey" action="surveyParticipants"
                id="${surveyConfig.surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id]">
            <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
            <ui:bubble count="${countParticipants.surveyMembers}"/>
        </g:link>

    </g:if>
    <br/><br/>--}%



    <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_RENEWAL.id]}">

        <div class="la-inline-lists">

            <g:if test="${propertiesChanged}">
                <h3 class="ui header">${message(code: 'renewalEvaluation.propertiesChanged')}</h3>

                <g:link class="${Btn.SIMPLE} right floated" controller="survey" action="showPropertiesChanged"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: params.tab, exportXLSX: true]">
                    Export ${message(code: 'renewalEvaluation.propertiesChanged')}
                </g:link>
                <br/>
                <br/>


                <table class="ui la-js-responsive-table la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'propertyDefinition.label')}</th>
                        <th>${message(code: 'renewalEvaluation.propertiesChanged')}</th>
                        <th class="center aligned">
                            <ui:optionsIcon />
                        </th>
                    </tr>
                    </thead>
                    <tbody>

                    <g:each in="${propertiesChanged}" var="property" status="i">
                        <g:set var="propertyDefinition"
                               value="${PropertyDefinition.findById(property.key)}"/>
                        <tr>
                            <td class="center aligned">
                                ${i + 1}
                            </td>
                            <td>
                                ${propertyDefinition.getI10n('name')}
                            </td>
                            <td>${property.value.size()}</td>
                            <td>
                                <button class="${Btn.SIMPLE}" onclick="JSPC.app.propertiesChanged(${property.key});">
                                    <g:message code="default.button.show.label"/>
                                </button>
                            </td>
                        </tr>

                    </g:each>
                    </tbody>
                </table>

            </g:if>
        </div>
    </g:if>

</g:if>

<ui:filter>
    <g:set var="filterConfigShowList"
           value="${[['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex']]}"/>
    <g:if test="${surveyConfig.subscription}">
        <g:set var="filterConfigShowList"
               value="${filterConfigShowList << ['hasSubscription', 'subRunTimeMultiYear']}"/>
    </g:if>

    <g:if test="${surveyConfig.packageSurvey}">
        <g:set var="filterConfigShowList"
               value="${filterConfigShowList << ['surveyPackages']}"/>
    </g:if>
    <g:if test="${surveyConfig.vendorSurvey}">
        <g:set var="filterConfigShowList"
               value="${filterConfigShowList << ['surveyVendors']}"/>
    </g:if>
    <g:if test="${surveyConfig.subscriptionSurvey}">
        <g:set var="filterConfigShowList"
               value="${filterConfigShowList << ['surveySubscriptions']}"/>
    </g:if>



    <g:form action="${actionName}" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
        <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow      : filterConfigShowList,
                              tmplConfigFormFilter: true
                      ]"/>
    </g:form>
</ui:filter>



<g:form action="${processAction}" controller="${processController ?: 'survey'}" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab]">
    <br/><br/>

    <g:if test="${processController == 'mail'}">
        <g:hiddenField name="objectType" value="${surveyInfo.class.name}"/>
        <g:hiddenField name="originalAction" value="${actionName}"/>
    </g:if>

  %{--  <div class="ui blue large label">
        <g:message code="surveyEvaluation.participants"/>: <div class="detail">${participants.size()}</div>
    </div>--}%

    <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

    <g:set var="surveyParticipantsHasAccess"
           value="${participants.findAll { it.org.hasInstAdmin() }}"/>


    <g:if test="${surveyParticipantsHasAccess}">
        <laser:render template="/templates/copyEmailaddresses"
                      model="[modalID: 'copyEmailaddresses_participantsWithAccess', orgList: surveyParticipantsHasAccess.org]"/>
        <a data-ui="modal" class="${Btn.SIMPLE} right floated"
           href="#copyEmailaddresses_participantsWithAccess">
            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
        </a>
    </g:if>

    <br/><br/>

    <g:set var="sumListPriceEUR" value="${0}"/>
    <g:set var="sumListPriceUSD" value="${0}"/>
    <g:set var="sumListPriceGBP" value="${0}"/>


    <g:set var="sumBudgetEUR" value="${0}"/>
    <g:set var="sumBudgetUSD" value="${0}"/>
    <g:set var="sumBudgetGBP" value="${0}"/>

    <g:set var="sumDiffEUR" value="${0}"/>
    <g:set var="sumDiffUSD" value="${0}"/>
    <g:set var="sumDiffGBP" value="${0}"/>


    <table class="ui compact celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <g:if test="${showCheckboxForParticipantsHasAccess}">
                <th>
                    <g:if test="${surveyParticipantsHasAccess}">
                        <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                    </g:if>
                </th>
            </g:if>

            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">

                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <th>${message(code: 'sidewide.number')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <th>${message(code: 'default.name.label')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                    <g:each in="${surveyConfig.getSortedProperties()}" var="surveyProperty">
                        <th>${surveyProperty.getI10n('name')}
                            <g:if test="${surveyProperty.getI10n('expl')}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                      data-content="${surveyProperty.getI10n('expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </g:if>
                        </th>
                    </g:each>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                    <th>${message(code: 'surveyResult.commentOnlyForOwnerBreak')}
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                    <th style="white-space:normal">
                        ${message(code: 'surveyEvaluation.titles.currentAndFixedEntitlements')}
                    </th>
                    <th>
                        ${message(code: 'tipp.price.plural')}
                    </th>
                    <th>
                        Budget
                    </th>
                    <th>Diff.</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('uploadTitleListDoc')}">
                    <th class="center aligned one wide">
                        ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}<br>
                        <i class="${Icon.DOCUMENT} large la-popup-tooltip" data-content="${message(code: 'subscriptionsManagement.documents')}" data-position="top center"></i>
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                    <th>
                        ${message(code: 'surveyInfo.finishedDateBreak')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                    <th>
                        ${message(code: 'surveyOrg.reminderMailDate')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('downloadTitleList')}">
                    <th class="center aligned">
                        <i class="la-popup-tooltip ${Icon.CMD.DOWNLOAD} large" data-content="Download ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}"></i>
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyPackages')}">
                    <th>
                        ${message(code: 'surveyPackages.selectedPackages')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsPackages')}">
                    <th>
                        ${message(code: 'surveyCostItemsPackages.label')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubscriptions')}">
                    <th>
                        ${message(code: 'surveySubscriptions.selectedSubscriptions')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsSubscriptions')}">
                    <th>
                        ${message(code: 'surveyCostItemsSubscriptions.label')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendors')}">
                    <th>
                        ${message(code: 'surveyVendors.selectedVendors')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendor')}">
                    <th>
                        ${message(code: 'surveyVendors.selectedVendor')}
                    </th>
                </g:if>
            </g:each>
            <th scope="col" rowspan="2" class="two center aligned">
                <span class="la-popup-tooltip" data-content="${message(code:'default.actions.label')}">
                    <i class="${Icon.SYM.OPTIONS} large"></i>
                </span>
            </th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${surveyParticipantsHasAccess}" var="surveyOrg" status="i">

            <g:set var="participant"
                   value="${surveyOrg.org}"/>
            <g:set var="subParticipant"
                   value="${surveyConfig.subscription?.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
            <g:set var="surResults" value="[]"/>
            <g:each in="${surveyConfig.getSortedProperties()}" var="surveyProperty">
                <% surResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participant, surveyConfig, surveyProperty) %>
            </g:each>
            <tr>
                <g:if test="${showCheckboxForParticipantsHasAccess}">
                    <td>
                        <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                    </td>
                </g:if>
                <g:each in="${tmplConfigShow}" var="tmplConfigItem">

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                        <td>
                            ${i + 1}
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                        <td>
                            <g:link controller="myInstitution" action="manageParticipantSurveys"
                                    id="${participant.id}">
                                ${participant.sortname}
                            </g:link>
                            <br/>
                            <g:link controller="organisation" action="show" id="${participant.id}">
                                (${fieldValue(bean: participant, field: "name")})
                            </g:link>


                            <g:if test="${showIcons}">
                                <g:if test="${surveyConfig.surveyProperties}">
                                    <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                        <span data-position="top right" class="la-popup-tooltip"
                                              data-content="${message(code: 'surveyResult.processedOrg')}">
                                            <i class="${Icon.ATTR.SURVEY_RESULTS_PROCESSED}"></i>
                                        </span>
                                    </g:if>
                                    <g:else>
                                        <span data-position="top right" class="la-popup-tooltip"
                                              data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                            <i class="${Icon.ATTR.SURVEY_RESULTS_NOT_PROCESSED}"></i>
                                        </span>
                                    </g:else>
                                </g:if>

                                <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'surveyResult.finishOrg')}">
                                        <i class="${Icon.SYM.YES} green"></i>
                                    </span>
                                </g:if>
                                <g:else>
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                        <i class="${Icon.SYM.NO} red"></i>
                                    </span>
                                </g:else>
                            </g:if>

                            <g:if test="${surveyConfig.checkOrgTransferred(participant)}">
                                <span data-position="top right" class="la-popup-tooltip"
                                      data-content="${message(code: 'surveyTransfer.transferred')}: ${surveyConfig.getSubscriptionWhereOrgTransferred(participant).collect {it.getLabel()}.join(', ')}">
                                    <i class="${Icon.ATTR.SURVEY_ORG_TRANSFERRED}"></i>
                                </span>
                            </g:if>

                            <g:if test="${propertiesChangedByParticipant && participant.id in propertiesChangedByParticipant.id}">
                                <span data-position="top right" class="la-popup-tooltip"
                                      data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                                    <i class="${Icon.TOOLTIP.IMPORTANT} yellow"></i>
                                </span>
                            </g:if>


                            <g:if test="${surveyConfig.subscription && !surveyConfig.hasOrgSubscription(participant)}">
                                <span data-position="top right" class="la-popup-tooltip"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="la-sparkles large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyOrg.orgInsertedItself}">
                                <span data-position="top right" class="la-popup-tooltip"
                                      data-content="${message(code: 'surveyLinks.newParticipate')}">
                                    <i class="paper plane outline large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${showOpenParticipantsAgainButtons}">
                                <a href="${"mailto:${surveyOrg.org.getMailsOfGeneralContactPersons(false).join(';')}?subject=" + mailSubject +
                                        "&body=" + mailBody}">
                                    <span data-position="right center"
                                          class="la-popup-tooltip"
                                          data-content="Mail senden an Hauptkontakte">
                                        <i class="${Icon.SYM.EMAIL} la-list-icon"></i>
                                    </span>
                                </a>
                            </g:if>

                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                        <g:each in="${surResults}" var="resultProperty">
                            <td>
                                <laser:render template="surveyResult"
                                              model="[surResult: resultProperty, surveyOrg: surveyOrg]"/>
                            </td>
                        </g:each>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">

                        <g:set var="diffEUR" value="${0}"/>
                        <g:set var="diffUSD" value="${0}"/>
                        <g:set var="diffGBP" value="${0}"/>

                        <g:set var="sumListPriceSelectedIEsEUR"
                               value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_EUR)}"/>
                        <g:set var="sumListPriceSelectedIEsUSD"
                               value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_USD)}"/>
                        <g:set var="sumListPriceSelectedIEsGBP"
                               value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_GBP)}"/>

                        <td class="center aligned surveyTitlesCount" data-surveyConfig="${surveyConfig.id}" data-subParticipant="${subParticipant.id}"></td>
                        <td>
                            <g:if test="${sumListPriceSelectedIEsEUR > 0}">
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsEUR}" type="currency" currencyCode="EUR"/>
                                <g:set var="sumListPriceEUR" value="${sumListPriceEUR + sumListPriceSelectedIEsEUR}"/>

                            </g:if>
                            <g:if test="${sumListPriceSelectedIEsUSD > 0}">
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsUSD}" type="currency" currencyCode="USD"/>
                                <g:set var="sumListPriceUSD" value="${sumListPriceUSD + sumListPriceSelectedIEsUSD}"/>

                            </g:if>
                            <g:if test="${sumListPriceSelectedIEsGBP > 0}">
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsGBP}" type="currency" currencyCode="GBP"/>
                                <g:set var="sumListPriceGBP" value="${sumListPriceGBP + sumListPriceSelectedIEsGBP}"/>
                            </g:if>
                        </td>
                        <td>
                            <g:set var="costItemsBudget"
                                   value="${de.laser.finance.CostItem.findAllBySubAndCostItemElementAndCostItemStatusNotEqualAndOwner(subParticipant, RDStore.COST_ITEM_ELEMENT_BUDGET_TITLE_PICK, RDStore.COST_ITEM_DELETED, contextService.getOrg())}"/>

                            <g:each in="${costItemsBudget}"
                                    var="costItem">
                                <g:formatNumber number="${costItem.costInBillingCurrency}" type="currency" currencyCode="${costItem.billingCurrency.value}"/>

                                <g:if test="${sumListPriceSelectedIEsEUR && costItem.billingCurrency == RDStore.CURRENCY_EUR && costItem.costInBillingCurrency > 0}">
                                    <g:set var="diffEUR" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsEUR}"/>
                                </g:if>

                                <g:if test="${sumListPriceSelectedIEsUSD && costItem.billingCurrency == RDStore.CURRENCY_USD && costItem.costInBillingCurrency > 0}">
                                    <g:set var="diffUSD" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsUSD}"/>
                                </g:if>

                                <g:if test="${sumListPriceSelectedIEsGBP && costItem.billingCurrency == RDStore.CURRENCY_GBP && costItem.costInBillingCurrency > 0}">
                                    <g:set var="diffGBP" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsGBP}"/>
                                </g:if>

                                <g:set var="sumBudgetEUR"
                                       value="${costItem.billingCurrency == RDStore.CURRENCY_EUR && costItem.costInBillingCurrency > 0 ? (sumBudgetEUR + costItem.costInBillingCurrency) : sumBudgetEUR}"/>
                                <g:set var="sumBudgetUSD"
                                       value="${costItem.billingCurrency == RDStore.CURRENCY_USD && costItem.costInBillingCurrency > 0 ? (sumBudgetUSD + costItem.costInBillingCurrency) : sumBudgetUSD}"/>
                                <g:set var="sumBudgetGBP"
                                       value="${costItem.billingCurrency == RDStore.CURRENCY_GBP && costItem.costInBillingCurrency > 0 ? (sumBudgetGBP + costItem.costInBillingCurrency) : sumBudgetGBP}"/>

                            </g:each>

                        </td>
                        <td>
                            <g:if test="${diffEUR != 0}">
                                <g:formatNumber
                                        number="${diffEUR}" type="currency" currencyCode="EUR"/>
                                <g:set var="sumDiffEUR" value="${sumDiffEUR + diffEUR}"/>

                            </g:if>
                            <g:if test="${diffUSD != 0}">
                                <g:formatNumber
                                        number="${diffUSD}" type="currency" currencyCode="USD"/>
                                <g:set var="sumDiffUSD" value="${sumDiffUSD + diffUSD}"/>

                            </g:if>
                            <g:if test="${diffGBP != 0}">
                                <g:formatNumber
                                        number="${diffGBP}" type="currency" currencyCode="GBP"/>
                                <g:set var="sumDiffGBP" value="${sumDiffGBP + diffGBP}"/>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('uploadTitleListDoc')}">
                        <td class="js-la-upload-list">
                            <g:if test="${editable}">
                                <button type="button" class="${Btn.MODERN.SIMPLE} tiny"
                                        data-ownerid="${subParticipant.id}"
                                        data-ownerclass="${subParticipant.class.name}"
                                        data-doctype="${RDStore.DOC_TYPE_TITLELIST.value}"
                                        data-ui="modal"
                                        data-href="#modalUploadTitleListDoc">
                                    <i aria-hidden="true" class="${Icon.CMD.ADD} small"></i>
                                </button>
                            </g:if>

                            <%
                                Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: subParticipant, docType: RDStore.DOC_TYPE_TITLELIST, owner: contextService.getOrg()])
                                documentSet = documentSet.sort { it.owner?.title }
                            %>
                            <g:each in="${documentSet}" var="docctx">
                                <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                    <div class="ui small feed content">
                                        <div class="ui middle aligned grid summary">
                                            <div class="eight wide column la-column-right-lessPadding center aligned">
                                                <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <i class="large icons">
                                                    <g:if test="${supportedMimeType}">
                                                        <a href="#documentPreview"
                                                           class="la-popup-tooltip"
                                                           data-content="${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}"
                                                           data-dctx="${docctx.id}">
                                                            <i class="${Icon.DOCUMENT} blue"></i>
                                                        </a>
                                                    </g:if>
                                                    <g:else>
                                                        <i class="${Icon.DOCUMENT} grey la-popup-tooltip" data-content="${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}"></i>
                                                    </g:else>
                                                    <ui:documentIcon doc="${docctx.owner}" showText="false"
                                                                     showTooltip="true"/>
                                                </i>
                                            </div>

                                            <div class="right aligned eight wide column la-column-left-lessPadding la-border-left">

                                                <g:if test="${!(editable)}">
                                                <%-- 1 --%>
                                                    <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                            class="${Btn.MODERN.SIMPLE} tiny"
                                                            target="_blank"><i class="${Icon.CMD.DOWNLOAD} small"></i></g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id}">
                                                    <%-- 1 --%>
                                                        <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                                class="${Btn.MODERN.SIMPLE} tiny"
                                                                target="_blank"><i class="${Icon.CMD.DOWNLOAD} small"></i></g:link>

                                                    <%-- 2 --%>
                                                        <laser:render template="/templates/documents/modal"
                                                                      model="[ownobj: subParticipant, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                        <button type="button"
                                                                class="${Btn.MODERN.SIMPLE} tiny"
                                                                data-ui="modal"
                                                                data-href="#modalEditDocument_${docctx.id}"
                                                                aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                            <i class="${Icon.CMD.EDIT} small"></i>
                                                        </button>
                                                    </g:if>

                                                <%-- 4 --%>
                                                    <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id && !docctx.isShared}">
                                                        <g:link controller="${ajaxCallController ?: controllerName}"
                                                                action="deleteDocuments"
                                                                class="${Btn.MODERN.NEGATIVE_CONFIRM} tiny"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                                data-confirm-term-how="delete"
                                                                params='[surveyConfigID: "${surveyConfig.id}", id: "${surveyInfo.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                            <i class="${Icon.CMD.DELETE} small"></i>
                                                        </g:link>
                                                    </g:if>
                                                </g:else>%{-- (editable || editable2) --}%
                                            </div>
                                        </div>
                                    </div>
                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="finishDate"/>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="reminderMailDate"/>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('downloadTitleList')}">
                        <td>
                            <g:link controller="subscription" action="exportRenewalEntitlements" id="${subParticipant.id}"
                                    params="${[surveyConfigID: surveyConfig.id,
                                               exportConfig  : ExportService.EXCEL,
                                               tab           : 'selectedIEs']}"
                                    class="${Btn.MODERN.SIMPLE_TOOLTIP} normalExport"
                                    data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}" data-position="bottom left"
                                    target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyPackages')}">
                        <td>
                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'packageSurvey', subTab: 'selectPackages']">
                                ${SurveyPackageResult.countByParticipantAndSurveyConfig(participant, surveyConfig)}
                            </g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsPackages')}">
                        <td>
                            <g:set var="costItemSumBySelectSurveyPackageOfParticipant"
                                   value="${surveyService.getCostItemSumBySelectSurveyPackageOfParticipant(surveyConfig, participant)}"/>
                            ${costItemSumBySelectSurveyPackageOfParticipant.sumCostInBillingCurrency} (${costItemSumBySelectSurveyPackageOfParticipant.sumCostInBillingCurrencyAfterTax})
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubscriptions')}">
                        <td>
                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'subscriptionSurvey', subTab: 'selectSubscriptions']">
                                ${SurveySubscriptionResult.countByParticipantAndSurveyConfig(participant, surveyConfig)}
                            </g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsSubscriptions')}">
                        <td>
                            <g:set var="costItemSumBySelectSurveySubscriptionsOfParticipant"
                                   value="${surveyService.getCostItemSumBySelectSurveySubscriptionsOfParticipant(surveyConfig, participant)}"/>
                            ${costItemSumBySelectSurveySubscriptionsOfParticipant.sumCostInBillingCurrency} (${costItemSumBySelectSurveySubscriptionsOfParticipant.sumCostInBillingCurrencyAfterTax})
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendors')}">
                        <td>
                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'vendorSurvey', subTab: 'selectVendors']">
                                ${SurveyVendorResult.countByParticipantAndSurveyConfig(participant, surveyConfig)}
                            </g:link>
                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendor')}">
                        <td>
                            <g:set var="vendorResult" value="${SurveyVendorResult.findByParticipantAndSurveyConfig(participant, surveyConfig)}"/>
                            <g:if test="${vendorResult}">
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'vendorSurvey', subTab: 'selectVendors']">
                                    ${vendorResult.vendor.name}
                                </g:link>
                            </g:if>
                        </td>
                    </g:if>
                </g:each>
                <td class="x">
                    <g:link controller="survey" action="evaluationParticipant"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                            class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                            data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                        <i class="${Icon.SURVEY}"></i>
                    </g:link>

                    <g:if test="${surveyConfig.subscription}">
                        <g:set var="participantSub" value="${surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
                        <g:if test="${participantSub}">
                            <g:link controller="subscription" action="show" id="${participantSub.id}"
                                    class="${Btn.ICON.SIMPLE} orange la-modern-button"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                        </g:if>
                    </g:if>

                    <a href="#" class="ui button icon la-modern-button infoFlyout-trigger" data-template="org" data-org="${participant.id}"
                       data-sub="${surveyConfig.subscription?.id}" data-surveyConfig="${surveyConfig.id}">
                        <i class="ui info icon"></i>
                    </a>
                </td>

            </tr>

        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <g:if test="${showCheckboxForParticipantsHasAccess}">
                <td>
                </td>
            </g:if>
            <g:each in="${tmplConfigShow}" var="tmplConfigItem">
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                    <td></td>
                    <td>
                        <g:if test="${sumListPriceEUR}">
                            <g:formatNumber
                                    number="${sumListPriceEUR}" type="currency" currencyCode="EUR"/>
                            <br>
                        </g:if>
                        <g:if test="${sumListPriceUSD}">
                            <g:formatNumber
                                    number="${sumListPriceUSD}" type="currency" currencyCode="USD"/>
                            <br>
                        </g:if>
                        <g:if test="${sumListPriceGBP}">
                            <g:formatNumber
                                    number="${sumListPriceGBP}" type="currency" currencyCode="GBP"/>
                            <br>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${sumBudgetEUR}">
                            <g:formatNumber
                                    number="${sumBudgetEUR}" type="currency" currencyCode="EUR"/>
                            <br>
                        </g:if>
                        <g:if test="${sumBudgetUSD}">
                            <g:formatNumber
                                    number="${sumBudgetUSD}" type="currency" currencyCode="USD"/>
                            <br>
                        </g:if>
                        <g:if test="${sumBudgetGBP}">
                            <g:formatNumber
                                    number="${sumBudgetGBP}" type="currency" currencyCode="GBP"/>
                            <br>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${sumDiffEUR}">
                            <g:formatNumber
                                    number="${sumDiffEUR}" type="currency" currencyCode="EUR"/>
                            <br>
                        </g:if>
                        <g:if test="${sumDiffUSD}">
                            <g:formatNumber
                                    number="${sumDiffUSD}" type="currency" currencyCode="USD"/>
                            <br>
                        </g:if>
                        <g:if test="${sumDiffGBP}">
                            <g:formatNumber
                                    number="${sumDiffGBP}" type="currency" currencyCode="GBP"/>
                            <br>
                        </g:if>
                    </td>
                </g:if>
                <g:elseif test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                    <g:each in="${surResults}" var="resultProperty">
                        <td></td>
                    </g:each>
                </g:elseif>
                <g:else>
                    <td></td>
                </g:else>
            </g:each>
            <td></td>
        </tr>
        </tfoot>
    </table>

    <g:if test="${showOpenParticipantsAgainButtons}">
        <div class="content">
            <div class="ui form twelve wide column">
            <div class="two fields">
                <g:if test="${actionName == 'participantsReminder'}">
                %{-- <div class="eight wide field" style="text-align: left;">
                     <a data-ui="modal" class="${Btn.SIMPLE}"
                        href="#generateEmailWithAddresses_ajaxModal">
                         ${message(code: 'openParticipantsAgain.reminder.participantsHasAccess')}
                     </a>--}%

                %{--        <laser:render template="generateEmailWithAddresses"
                                      model="[modalID: 'generateEmailWithAddresses_ajaxModal', formUrl: processAction ?  g.createLink([controller: 'survey',action: processAction, params: [id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab]]) : '',
                                              messageCode: 'openParticipantsAgain.reminder.participantsHasAccess',
                                              submitButtonValue: 'ReminderMail',
                                                mailText: surveyService.notificationSurveyAsStringInText(surveyConfig.surveyInfo, true)]"/>--}%

                    </div>

                    <div class="eight wide field" style="text-align: left;">
                        <button name="openOption" type="submit" value="ReminderMail" class="${Btn.SIMPLE}">
                            ${message(code: 'openParticipantsAgain.reminder.participantsHasAccess')}
                        </button>
                    </div>
                </g:if>
            </div>
        </div>
        </div>
    </g:if>
    <br/><br/>

    <g:set var="surveyParticipantsHasNotAccess"
           value="${participants.findAll { !it.org.hasInstAdmin() }}"/>

    <g:if test="${surveyParticipantsHasNotAccess}">

        <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>


        <laser:render template="/templates/copyEmailaddresses"
                      model="[modalID: 'copyEmailaddresses_participantsWithoutAccess', orgList: surveyParticipantsHasNotAccess.org]"/>
        <a data-ui="modal" class="${Btn.SIMPLE} right floated"
           href="#copyEmailaddresses_participantsWithoutAccess">
            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
        </a>


        <g:set var="sumListPriceEUR" value="${0}"/>
        <g:set var="sumListPriceUSD" value="${0}"/>
        <g:set var="sumListPriceGBP" value="${0}"/>


        <g:set var="sumBudgetEUR" value="${0}"/>
        <g:set var="sumBudgetUSD" value="${0}"/>
        <g:set var="sumBudgetGBP" value="${0}"/>

        <g:set var="sumDiffEUR" value="${0}"/>
        <g:set var="sumDiffUSD" value="${0}"/>
        <g:set var="sumDiffGBP" value="${0}"/>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <g:if test="${showCheckboxForParticipantsHasNoAccess}">
                    <th>
                        <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                    </th>
                </g:if>

                <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                        <th>${message(code: 'sidewide.number')}</th>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                        <th>${message(code: 'default.name.label')}</th>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                        <g:each in="${surveyConfig.getSortedProperties()}" var="surveyProperty">
                            <th>${surveyProperty.getI10n('name')}
                                <g:if test="${surveyProperty.getI10n('expl')}">
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${surveyProperty.getI10n('expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </g:if>
                            </th>
                        </g:each>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                        <th>${message(code: 'surveyResult.commentOnlyForOwnerBreak')}
                            <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                        <th>
                            ${message(code: 'surveyEvaluation.titles.currentAndFixedEntitlements')}
                        </th>
                        <th>
                            ${message(code: 'tipp.price.plural')}
                        </th>
                        <th>
                            Budget
                        </th>
                        <th>Diff.</th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('uploadTitleListDoc')}">
                        <th class="center aligned one wide">
                            ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}<br>
                            <i class="${Icon.DOCUMENT} large la-popup-tooltip" data-content="${message(code: 'subscriptionsManagement.documents')}" data-position="top center"></i>
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                        <th>
                            ${message(code: 'surveyInfo.finishedDateBreak')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                        <th>
                            ${message(code: 'surveyOrg.reminderMailDate')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('downloadTitleList')}">
                        <th class="center aligned">
                            <i class="la-popup-tooltip ${Icon.CMD.DOWNLOAD} large" data-content="Download ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}"></i>
                    </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyPackages')}">
                        <th>
                            ${message(code: 'surveyPackages.selectedPackages')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsPackages')}">
                        <th>
                            ${message(code: 'surveyCostItemsPackages.label')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubscriptions')}">
                        <th>
                            ${message(code: 'surveySubscriptions.selectedSubscriptions')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsSubscriptions')}">
                        <th>
                            ${message(code: 'surveyCostItemsSubscriptions.label')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendors')}">
                        <th>
                            ${message(code: 'surveyVendors.selectedVendors')}
                        </th>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendor')}">
                        <th>
                            ${message(code: 'surveyVendors.selectedVendor')}
                        </th>
                    </g:if>

                </g:each>
                <th scope="col" rowspan="2" class="two center aligned">
                    <span class="la-popup-tooltip" data-content="${message(code:'default.actions.label')}">
                        <i class="${Icon.SYM.OPTIONS} large"></i>
                    </span>
                </th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${surveyParticipantsHasNotAccess}" var="surveyOrg" status="i">

                <g:set var="participant"
                       value="${surveyOrg.org}"/>

                <g:set var="surResults" value="[]"/>
                <g:each in="${surveyConfig.getSortedProperties()}" var="surveyProperty">
                    <% surResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participant, surveyConfig, surveyProperty) %>
                </g:each>

                <tr>
                    <g:if test="${showCheckboxForParticipantsHasNoAccess}">
                        <td>
                            <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                        </td>
                    </g:if>
                    <g:each in="${tmplConfigShow}" var="tmplConfigItem">

                        <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                            <td>
                                ${i + 1}
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys"
                                        id="${participant.id}">
                                    ${participant.sortname}
                                </g:link>
                                <br/>
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    (${fieldValue(bean: participant, field: "name")})
                                </g:link>

                            <g:if test="${showIcons}">
                                <g:if test="${surveyConfig.surveyProperties}">
                                    <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                        <span data-position="top right" class="la-popup-tooltip"
                                              data-content="${message(code: 'surveyResult.processedOrg')}">
                                            <i class="${Icon.ATTR.SURVEY_RESULTS_PROCESSED}"></i>
                                        </span>
                                    </g:if>
                                    <g:else>
                                        <span data-position="top right" class="la-popup-tooltip"
                                              data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                            <i class="${Icon.ATTR.SURVEY_RESULTS_NOT_PROCESSED}"></i>
                                        </span>
                                    </g:else>
                                </g:if>

                                <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'surveyResult.finishOrg')}">
                                        <i class="${Icon.SYM.YES} green"></i>
                                    </span>
                                </g:if>
                                <g:else>
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                        <i class="${Icon.SYM.NO} red"></i>
                                    </span>
                                </g:else>
                            </g:if>

                                <g:if test="${propertiesChangedByParticipant && participant.id in propertiesChangedByParticipant.id}">
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                                        <i class="${Icon.TOOLTIP.IMPORTANT} yellow"></i>
                                    </span>
                                </g:if>

                                <g:if test="${surveyConfig.subscription && !surveyConfig.hasOrgSubscription(participant)}">
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'surveyResult.newOrg')}">
                                        <i class="${Icon.SIG.NEW_OBJECT} large"></i>
                                    </span>
                                </g:if>

                                <g:if test="${surveyOrg.orgInsertedItself}">
                                    <span data-position="top right" class="la-popup-tooltip"
                                          data-content="${message(code: 'surveyLinks.newParticipate')}">
                                        <i class="paper plane outline large icon"></i>
                                    </span>
                                </g:if>

                                <g:if test="${showOpenParticipantsAgainButtons}">
                                    <a href="${"mailto:${surveyOrg.org.getMailsOfGeneralContactPersons(false).join(';')}?subject=" + mailSubject +
                                            "&body=" + mailBody}">
                                        <span data-position="right center"
                                              class="la-popup-tooltip"
                                              data-content="Mail senden an Hauptkontakte">
                                            <i class="${Icon.SYM.EMAIL} la-list-icon"></i>
                                        </span>
                                    </a>
                                </g:if>

                            </td>
                        </g:if>

                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                            <g:each in="${surResults}" var="resultProperty">
                                <td>
                                    <laser:render template="surveyResult"
                                                  model="[surResult: resultProperty, surveyOrg: surveyOrg]"/>
                                </td>
                            </g:each>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                            <td>
                                <ui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                            </td>
                        </g:if>

                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                            <g:set var="subParticipant"
                                   value="${surveyConfig.subscription?.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>

                            <g:set var="diffEUR" value="${0}"/>
                            <g:set var="diffUSD" value="${0}"/>
                            <g:set var="diffGBP" value="${0}"/>

                            <g:set var="sumListPriceSelectedIEsEUR"
                                   value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_EUR)}"/>
                            <g:set var="sumListPriceSelectedIEsUSD"
                                   value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_USD)}"/>
                            <g:set var="sumListPriceSelectedIEsGBP"
                                   value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_GBP)}"/>

                            <td class="center aligned surveyTitlesCount" data-surveyConfig="${surveyConfig.id}" data-subParticipant="${subParticipant.id}"></td>
                            <td>
                                <g:if test="${sumListPriceSelectedIEsEUR > 0}">
                                    <br>
                                    <g:formatNumber
                                            number="${sumListPriceSelectedIEsEUR}" type="currency" currencyCode="EUR"/>
                                    <g:set var="sumListPriceEUR" value="${sumListPriceEUR + sumListPriceSelectedIEsEUR}"/>

                                </g:if>
                                <g:if test="${sumListPriceSelectedIEsUSD > 0}">
                                    <br>
                                    <g:formatNumber
                                            number="${sumListPriceSelectedIEsUSD}" type="currency" currencyCode="USD"/>
                                    <g:set var="sumListPriceUSD" value="${sumListPriceUSD + sumListPriceSelectedIEsUSD}"/>

                                </g:if>
                                <g:if test="${sumListPriceSelectedIEsGBP > 0}">
                                    <br>
                                    <g:formatNumber
                                            number="${sumListPriceSelectedIEsGBP}" type="currency" currencyCode="GBP"/>
                                    <g:set var="sumListPriceGBP" value="${sumListPriceGBP + sumListPriceSelectedIEsGBP}"/>
                                </g:if>
                            </td>
                            <td>
                                <g:set var="costItemsBudget"
                                       value="${de.laser.finance.CostItem.findAllBySubAndCostItemElementAndCostItemStatusNotEqualAndOwner(subParticipant, RDStore.COST_ITEM_ELEMENT_BUDGET_TITLE_PICK, RDStore.COST_ITEM_DELETED, contextService.getOrg())}"/>

                                <g:each in="${costItemsBudget}"
                                        var="costItem">
                                    <g:formatNumber number="${costItem.costInBillingCurrency}" type="currency"
                                                    currencyCode="${costItem.billingCurrency.value}"/>

                                    <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_EUR}">
                                        <g:set var="diffEUR" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsEUR}"/>
                                    </g:if>

                                    <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_USD}">
                                        <g:set var="diffUSD" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsUSD}"/>
                                    </g:if>

                                    <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_GBP}">
                                        <g:set var="diffGBP" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsGBP}"/>
                                    </g:if>

                                    <g:set var="sumBudgetEUR"
                                           value="${costItem.billingCurrency == RDStore.CURRENCY_EUR && costItem.costInBillingCurrency > 0 ? (sumBudgetEUR + costItem.costInBillingCurrency) : sumBudgetEUR}"/>
                                    <g:set var="sumBudgetUSD"
                                           value="${costItem.billingCurrency == RDStore.CURRENCY_USD && costItem.costInBillingCurrency > 0 ? (sumBudgetUSD + costItem.costInBillingCurrency) : sumBudgetUSD}"/>
                                    <g:set var="sumBudgetGBP"
                                           value="${costItem.billingCurrency == RDStore.CURRENCY_GBP && costItem.costInBillingCurrency > 0 ? (sumBudgetGBP + costItem.costInBillingCurrency) : sumBudgetGBP}"/>

                                </g:each>

                            </td>
                            <td>
                                <g:if test="${diffEUR != 0}">
                                    <br>
                                    <g:formatNumber
                                            number="${diffEUR}" type="currency" currencyCode="EUR"/>
                                    <g:set var="sumDiffEUR" value="${sumDiffEUR + diffEUR}"/>

                                </g:if>
                                <g:if test="${diffUSD != 0}">
                                    <br>
                                    <g:formatNumber
                                            number="${diffUSD}" type="currency" currencyCode="USD"/>
                                    <g:set var="sumDiffUSD" value="${sumDiffUSD + diffUSD}"/>

                                </g:if>
                                <g:if test="${diffGBP != 0}">
                                    <br>
                                    <g:formatNumber
                                            number="${diffGBP}" type="currency" currencyCode="GBP"/>
                                    <g:set var="sumDiffGBP" value="${sumDiffGBP + diffGBP}"/>
                                </g:if>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('uploadTitleListDoc')}">
                            <td class="js-la-upload-list">
                                <g:if test="${editable}">
                                    <button type="button" class="${Btn.MODERN.SIMPLE} tiny"
                                            data-ownerid="${subParticipant.id}"
                                            data-ownerclass="${subParticipant.class.name}"
                                            data-doctype="${RDStore.DOC_TYPE_TITLELIST.value}"
                                            data-ui="modal"
                                            data-href="#modalUploadTitleListDoc">
                                        <i aria-hidden="true" class="${Icon.CMD.ADD} small"></i>
                                    </button>
                                </g:if>

                                <%
                                    Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: subParticipant, docType: RDStore.DOC_TYPE_TITLELIST, owner: contextService.getOrg()])
                                    documentSet = documentSet2.sort { it.owner?.title }
                                %>
                                <g:each in="${documentSet2}" var="docctx">
                                    <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                        <div class="ui small feed content">
                                            <div class="ui middle aligned grid summary">
                                                <div class="eight wide column la-column-right-lessPadding center aligned">
                                                    <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                    <i class="large icons">
                                                        <g:if test="${supportedMimeType}">
                                                            <a href="#documentPreview"
                                                               class="la-popup-tooltip"
                                                               data-content="${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}"
                                                               data-dctx="${docctx.id}">
                                                                <i class="${Icon.DOCUMENT} blue"></i>
                                                            </a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.DOCUMENT} grey la-popup-tooltip" data-content="${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}"></i>
                                                        </g:else>
                                                        <ui:documentIcon doc="${docctx.owner}" showText="false"
                                                                         showTooltip="true"/>
                                                    </i>
                                                </div>

                                                <div class="right aligned eight wide column la-column-left-lessPadding la-border-left">

                                                    <g:if test="${!(editable)}">
                                                    <%-- 1 --%>
                                                        <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                                class="${Btn.MODERN.SIMPLE} tiny"
                                                                target="_blank"><i class="${Icon.CMD.DOWNLOAD} small"></i></g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id}">
                                                        <%-- 1 --%>
                                                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                                    class="${Btn.MODERN.SIMPLE} tiny"
                                                                    target="_blank"><i
                                                                    class="${Icon.CMD.DOWNLOAD} small"></i></g:link>

                                                        <%-- 2 --%>
                                                            <laser:render template="/templates/documents/modal"
                                                                          model="[ownobj: subParticipant, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                            <button type="button"
                                                                    class="${Btn.MODERN.SIMPLE} tiny"
                                                                    data-ui="modal"
                                                                    data-href="#modalEditDocument_${docctx.id}"
                                                                    aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                                <i class="${Icon.CMD.EDIT} small"></i>
                                                            </button>
                                                        </g:if>

                                                    <%-- 4 --%>
                                                        <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id && !docctx.isShared}">
                                                            <g:link controller="${ajaxCallController ?: controllerName}"
                                                                    action="deleteDocuments"
                                                                    class="${Btn.MODERN.NEGATIVE_CONFIRM} tiny"
                                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                                    data-confirm-term-how="delete"
                                                                    params='[surveyConfigID: "${surveyConfig.id}", id: "${surveyInfo.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                                    role="button"
                                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                <i class="${Icon.CMD.DELETE} small"></i>
                                                            </g:link>
                                                        </g:if>
                                                    </g:else>%{-- (editable || editable2) --}%
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                </g:each>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                            <td>
                                <ui:xEditable owner="${surveyOrg}" type="date" field="finishDate"/>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                            <td>
                                <ui:xEditable owner="${surveyOrg}" type="date" field="reminderMailDate"/>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('downloadTitleList')}">
                            <td>
                                <g:link controller="subscription" action="exportRenewalEntitlements" id="${subParticipant.id}"
                                        params="${[surveyConfigID: surveyConfig.id,
                                                   exportConfig  : ExportService.EXCEL,
                                                   tab           : 'selectedIEs']}"
                                        class="${Btn.MODERN.SIMPLE_TOOLTIP} normalExport"
                                        data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}" data-position="bottom left"
                                        target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyPackages')}">
                            <td>
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'packageSurvey', subTab: 'selectPackages']">
                                    ${SurveyPackageResult.countByParticipantAndSurveyConfig(participant, surveyConfig)}
                                </g:link>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsPackages')}">
                            <td>
                                <g:set var="costItemSumBySelectSurveyPackageOfParticipant"
                                       value="${surveyService.getCostItemSumBySelectSurveyPackageOfParticipant(surveyConfig, participant)}"/>
                                ${costItemSumBySelectSurveyPackageOfParticipant.sumCostInBillingCurrency} (${costItemSumBySelectSurveyPackageOfParticipant.sumCostInBillingCurrencyAfterTax})
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveySubscriptions')}">
                            <td>
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'packageSurvey', subTab: 'selectPackages']">
                                    ${SurveySubscriptionResult.countByParticipantAndSurveyConfig(participant, surveyConfig)}
                                </g:link>
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyCostItemsSubscriptions')}">
                            <td>
                                <g:set var="costItemSumBySelectSurveySubscriptionsOfParticipant"
                                       value="${surveyService.getCostItemSumBySelectSurveySubscriptionsOfParticipant(surveyConfig, participant)}"/>
                                ${costItemSumBySelectSurveySubscriptionsOfParticipant.sumCostInBillingCurrency} (${costItemSumBySelectSurveySubscriptionsOfParticipant.sumCostInBillingCurrencyAfterTax})
                            </td>
                        </g:if>
                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendors')}">
                            <td>
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'vendorSurvey', subTab: 'selectVendors']">
                                    ${SurveyVendorResult.countByParticipantAndSurveyConfig(participant, surveyConfig)}
                                </g:link>
                            </td>
                        </g:if>

                        <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyVendor')}">
                            <td>
                                <g:set var="vendorResult" value="${SurveyVendorResult.findByParticipantAndSurveyConfig(participant, surveyConfig)}"/>
                                <g:if test="${vendorResult}">
                                    <g:link controller="survey" action="evaluationParticipant"
                                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: 'vendorSurvey', subTab: 'selectVendors']">
                                        ${vendorResult.vendor.name}
                                    </g:link>
                                </g:if>
                            </td>
                        </g:if>

                    </g:each>
                    <td>
                        <g:link controller="survey" action="evaluationParticipant"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <i class="${Icon.SURVEY}"></i>
                        </g:link>

                        <g:if test="${surveyConfig.subscription}">
                            <g:set var="participantSub" value="${surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
                            <g:if test="${participantSub}">
                                <g:link controller="subscription" action="show" id="${participantSub.id}"
                                        class="${Btn.ICON.SIMPLE} orange la-modern-button"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                            </g:if>
                        </g:if>

                        <a href="#" class="ui icon la-modern-button infoFlyout-trigger" data-template="org" data-org="${participant.id}"
                           data-sub="${surveyConfig.subscription?.id}" data-surveyConfig="${surveyConfig.id}">
                            <i class="icon info blue inverted"></i>
                        </a>
                    </td>
                </tr>

            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <g:if test="${showCheckboxForParticipantsHasNoAccess}">
                    <td>
                    </td>
                </g:if>
                <g:each in="${tmplConfigShow}" var="tmplConfigItem">
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                        <td></td>
                        <td>
                            <g:if test="${sumListPriceEUR}">
                                <g:formatNumber
                                        number="${sumListPriceEUR}" type="currency" currencyCode="EUR"/>
                                <br>
                            </g:if>
                            <g:if test="${sumListPriceUSD}">
                                <g:formatNumber
                                        number="${sumListPriceUSD}" type="currency" currencyCode="USD"/>
                                <br>
                            </g:if>
                            <g:if test="${sumListPriceGBP}">
                                <g:formatNumber
                                        number="${sumListPriceGBP}" type="currency" currencyCode="GBP"/>
                                <br>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${sumBudgetEUR}">
                                <g:formatNumber
                                        number="${sumBudgetEUR}" type="currency" currencyCode="EUR"/>
                                <br>
                            </g:if>
                            <g:if test="${sumBudgetUSD}">
                                <g:formatNumber
                                        number="${sumBudgetUSD}" type="currency" currencyCode="USD"/>
                                <br>
                            </g:if>
                            <g:if test="${sumBudgetGBP}">
                                <g:formatNumber
                                        number="${sumBudgetGBP}" type="currency" currencyCode="GBP"/>
                                <br>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${sumDiffEUR}">
                                <g:formatNumber
                                        number="${sumDiffEUR}" type="currency" currencyCode="EUR"/>
                                <br>
                            </g:if>
                            <g:if test="${sumDiffUSD}">
                                <g:formatNumber
                                        number="${sumDiffUSD}" type="currency" currencyCode="USD"/>
                                <br>
                            </g:if>
                            <g:if test="${sumDiffGBP}">
                                <g:formatNumber
                                        number="${sumDiffGBP}" type="currency" currencyCode="GBP"/>
                                <br>
                            </g:if>
                        </td>
                    </g:if>
                    <g:elseif test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                        <g:each in="${surResults}" var="resultProperty">
                            <td></td>
                        </g:each>
                    </g:elseif>
                    <g:else>
                        <td></td>
                    </g:else>
                </g:each>
                <td></td>
            </tr>
            </tfoot>
        </table>

    </g:if>

    <g:if test="${showTransferFields}">
        <br/>
        <br/>
        <ui:greySegment>
            <div class="ui form">
            <h3 class="ui header">${message(code: 'surveyTransfer.info.label')}:</h3>
            <div class="two fields">
                <div class="ui field">
                    <div class="field">
                        <label>${message(code: 'filter.status')}</label>
                        <ui:select class="ui dropdown clearable" name="status" id="status"
                                   from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                   optionKey="id"
                                   optionValue="value"
                                   multiple="true"
                                   value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                                   noSelection="${['': message(code: 'default.select.choose.label')]}"
                                   onchange="JSPC.app.adjustDropdown()"/>
                    </div>
                    <div class="field">
                        <label for="targetSubscriptionId">${message(code: 'subscription.label')}</label>
                        <select id="targetSubscriptionId" name="targetSubscriptionId" class="ui fluid search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>
                        </select>
                    </div>

                </div>

                <div class="field">
                    <ui:datepicker label="subscription.startDate.label" id="startDate" name="startDate" value=""/>

                    <ui:datepicker label="subscription.endDate.label" id="endDate" name="endDate" value=""/>
                </div>
            </div>

            <input class="${Btn.SIMPLE}" type="submit" value="${message(code: 'surveyTransfer.button')}">
        </ui:greySegment>
        </div>

    </g:if>

    <g:if test="${showOpenParticipantsAgainButtons}">
        <div class="content">
            <div class="ui form twelve wide column">
                <div class="two fields">
                    <g:if test="${actionName == 'openParticipantsAgain'}">

                        <div class="eight wide field" style="text-align: left;">
                            <button name="openOption" type="submit" value="OpenWithoutMail" class="${Btn.SIMPLE}">
                                ${message(code: 'openParticipantsAgain.openWithoutMail.button')}
                            </button>
                        </div>

                        <div class="eight wide field" style="text-align: right;">
                            <button name="openOption" type="submit" value="OpenWithMail" class="${Btn.SIMPLE}">
                                ${message(code: 'openParticipantsAgain.openWithMail.button')}
                            </button>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>
    </g:if>

</g:form>


<laser:render template="/info/flyoutWrapper"/>

<laser:script file="${this.getGroovyPageFileName()}">
    <g:if test="${showCheckboxForParticipantsHasAccess || showCheckboxForParticipantsHasNoAccess}">
        $('#orgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        })
    </g:if>
    <g:if test="${showTransferFields}">
        JSPC.app.adjustDropdown = function () {
            var url = '<g:createLink controller="ajaxJson" action="adjustSubscriptionList"/>'

            url = url + '?'

            var status = $("select#status").serialize()
            if (status) {
                url = url + '&' + status
            }
            var selectedSubIds = [];
            <g:if test="${params.surveySubscriptions}">
                <g:each in="${params.list('surveySubscriptions')}" var="subId">
                    selectedSubIds.push(${subId});
                </g:each>
            </g:if>

            var dropdownSelectedObjects = $('#targetSubscriptionId');

            dropdownSelectedObjects.empty();
            dropdownSelectedObjects.append($('<option></option>').attr('value', '').text("${message(code: 'default.select.choose.label')}"));

            $.ajax({
                url: url,
                success: function (data) {
                    $.each(data, function (key, entry) {
                        <g:if test="${params.surveySubscriptions}">
                            if(jQuery.inArray(entry.value, selectedSubIds) >=0 ){
                                dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).attr('selected', 'selected').text(entry.text));
                            }
                            else{
                                dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).text(entry.text));
                            }
                        </g:if>
                        <g:else>
                            dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).text(entry.text));
                        </g:else>
                   });
               }
            });
        }

        JSPC.app.adjustDropdown();

    </g:if>
    JSPC.app.propertiesChanged = function (propertyDefinitionId) {
        $.ajax({
            url: '<g:createLink controller="survey" action="showPropertiesChanged" params="[tab: params.tab, surveyConfigID: surveyConfig.id, id: surveyInfo.id]"/>&propertyDefinitionId='+propertyDefinitionId,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalPropertiesChanged").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal('show');
            }
        });
    }

    $('.normalExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $("#downloadWrapper").hide();
        $.ajax({
            url: $(this).attr('href'),
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response).show();
            $('#globalLoadingIndicator').hide();
        }).fail(function(resp, status){
            $("#downloadWrapper").text('Es ist zu einem Fehler beim Abruf gekommen').show();
            $('#globalLoadingIndicator').hide();
        });
    });

    $('.surveyTitlesCount').each(function(){
        let wrapper = $(this);
        $.ajax({
            url: '<g:createLink controller="ajaxHtml" action="getSurveyTitlesCount"/>',
            data: {
                surveyConfig: $(this).attr("data-surveyConfig"),
                subParticipant: $(this).attr("data-subParticipant")
            }
        }).done(function(response){
            wrapper.html(response);
        }).fail(function(resp, status){
            console.log(status+' '+resp);
        });
    });

</laser:script>

<g:if test="${editable}">
    <laser:render template="/templates/documents/modal"
                  model="${[newModalId: "modalUploadTitleListDoc", owntp: 'subscription']}"/>


    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.callbacks.modal.onShow.modalUploadTitleListDoc = function(trigger) {
            $('#modalUploadTitleListDoc input[name=ownerid]').attr('value', $(trigger).attr('data-ownerid'))
            $('#modalUploadTitleListDoc input[name=ownerclass]').attr('value', $(trigger).attr('data-ownerclass'))
            $('#modalUploadTitleListDoc input[name=ownertp]').attr('value', $(trigger).attr('data-ownertp'))
            $('#modalUploadTitleListDoc select[name=doctype]').dropdown('set selected', $(trigger).attr('data-doctype'))
        }
        docs.init('.js-la-upload-list');
    </laser:script>

</g:if>

