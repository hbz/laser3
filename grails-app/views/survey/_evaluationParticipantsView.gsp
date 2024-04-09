<%@ page import="de.laser.Doc; de.laser.DocContext; de.laser.IssueEntitlementGroup; de.laser.config.ConfigMapper; de.laser.survey.SurveyConfig; de.laser.survey.SurveyResult; de.laser.Org; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;de.laser.RefdataCategory; de.laser.survey.SurveyOrg" %>
<laser:serviceInjection/>

<g:if test="${showOpenParticipantsAgainButtons}">
    <g:set var="mailSubject"
           value="${escapeService.replaceUmlaute(g.message(code: 'email.subject.surveys', args: ["${surveyConfig.surveyInfo.type.getI10n('value')}"]) + " " + surveyConfig.surveyInfo.name + "")}"/>
    <g:set var="mailBody" value="${surveyService.surveyMailHtmlAsString(surveyConfig.surveyInfo)}"/>
    <g:set var="mailString" value=""/>
</g:if>


<g:if test="${surveyConfig}">

    <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

    <g:if test="${surveyConfig.subscription}">

        <g:link class="ui right floated button la-inline-labeled" controller="subscription" action="members" id="${subscription.id}">
            <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

            <div class="ui blue circular label">
                ${countParticipants.subMembers}
            </div>
        </g:link>

        <g:link class="ui right floated button la-inline-labeled" controller="survey" action="surveyParticipants"
                id="${surveyConfig.surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id]">
            <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

            <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
        </g:link>

        <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
            ( ${countParticipants.subMembersWithMultiYear}
            ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
        </g:if>

    </g:if>

    <g:if test="${!surveyConfig.subscription}">
        <g:link class="ui right floated button la-inline-labeled" controller="survey" action="surveyParticipants"
                id="${surveyConfig.surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id]">
            <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
            <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
        </g:link>

    </g:if>
<br/><br/>



<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_RENEWAL.id] }">

    <div class="la-inline-lists">

        <g:if test="${propertiesChanged}">
        <h3 class="ui header">${message(code:'renewalEvaluation.propertiesChanged')}</h3>

            <g:link class="ui right floated button" controller="survey" action="showPropertiesChanged"
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
                            <th>${message(code:'renewalEvaluation.propertiesChanged')}</th>
                            <th>${message(code: 'default.actions.label')}</th>
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
                                    <button class="ui button"  onclick="JSPC.app.propertiesChanged(${property.key});">
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
<g:form action="${actionName}" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
    <laser:render template="/templates/filter/orgFilter"
              model="[
                      tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], surveyConfig.subscription ? ['hasSubscription'] : []],
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

    <div class="ui blue large label">
        <g:message code="surveyEvaluation.participants"/>: <div class="detail">${participants.size()}</div>
    </div>

    <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

    <g:set var="surveyParticipantsHasAccess"
           value="${participants.findAll { it.org.hasInstAdmin() }}"/>


        <g:if test="${surveyParticipantsHasAccess}">
            <laser:render template="/templates/copyEmailaddresses" model="[modalID: 'copyEmailaddresses_participantsWithAccess', orgList: surveyParticipantsHasAccess.org]"/>
            <a data-ui="modal" class="ui icon button right floated"
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

    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <g:if test="${showCheckbox}">
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
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${surveyProperty.getI10n('expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </th>
                    </g:each>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                    <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
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
                    <th>
                        Upload <br>
                        ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                <th>
                    ${message(code: 'surveyInfo.finishedDate')}
                </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                    <th>
                        ${message(code: 'surveyOrg.reminderMailDate')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('downloadTitleList')}">
                    <th>
                        Download <br>
                        ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}
                    </th>
                </g:if>
            </g:each>
            <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${surveyParticipantsHasAccess}" var="surveyOrg" status="i">

            <g:set var="participant"
                   value="${surveyOrg.org}"/>
            <g:set var="surResults" value="[]"/>
            <g:each in="${surveyConfig.getSortedProperties()}" var="surveyProperty">
                <% surResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participant, surveyConfig, surveyProperty) %>
            </g:each>
            <tr>
                <g:if test="${showCheckbox}">
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


                        <g:if test="${surveyConfig.surveyProperties}">
                            <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.processedOrg')}">
                                    <i class="edit green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                    <i class="edit red icon"></i>
                                </span>
                            </g:else>
                        </g:if>

                            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.finishOrg')}">
                                    <i class="check green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                    <i class="x red icon"></i>
                                </span>
                            </g:else>

                            <g:if test="${propertiesChangedByParticipant && participant.id in propertiesChangedByParticipant.id}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                                    <i class="exclamation triangle yellow large icon"></i>
                                </span>
                            </g:if>


                            <g:if test="${!surveyConfig.hasOrgSubscription(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="la-sparkles large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyOrg.orgInsertedItself}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyLinks.newParticipate')}">
                                    <i class="paper plane outline large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${showOpenParticipantsAgainButtons}">
                                <a href="${"mailto:${surveyOrg.org.getMailsOfGeneralContactPersons(false).join(';')}?subject=" + mailSubject +
                                        "&body=" + mailBody}">
                                    <span data-position="right center"
                                          class="la-popup-tooltip la-delay"
                                          data-content="Mail senden an Hauptkontakte">
                                        <i class="ui icon envelope outline la-list-icon"></i>
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

                        <g:set var="sumListPriceSelectedIEsEUR" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_EUR)}"/>
                        <g:set var="sumListPriceSelectedIEsUSD" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_USD)}"/>
                        <g:set var="sumListPriceSelectedIEsGBP" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_GBP)}"/>

                        <td class="center aligned">
                            <g:set var="ieGroup"
                                   value="${IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subParticipant)}"/>
                            <div class="ui circular label">
                            <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                                ${surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subParticipant, surveyConfig)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                            </g:if>
                            <g:else>
                                ${subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subParticipant, ieGroup)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                            </g:else>
                            </div>

                        </td>
                        <td>
                            <g:if test="${sumListPriceSelectedIEsEUR > 0}">
                                <br>
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsEUR}" type="currency" currencyCode="EUR"/>
                                <g:set var="sumListPriceEUR" value="${sumListPriceEUR+sumListPriceSelectedIEsEUR}"/>

                            </g:if>
                            <g:if test="${sumListPriceSelectedIEsUSD > 0}">
                                <br>
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsUSD}" type="currency" currencyCode="USD"/>
                                <g:set var="sumListPriceUSD" value="${sumListPriceUSD+sumListPriceSelectedIEsUSD}"/>

                            </g:if>
                            <g:if test="${sumListPriceSelectedIEsGBP > 0}">
                                <br>
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsGBP}" type="currency" currencyCode="GBP"/>
                                <g:set var="sumListPriceGBP" value="${sumListPriceGBP+sumListPriceSelectedIEsGBP}"/>
                            </g:if>
                        </td>
                        <td>
                            <g:set var="costItemsBudget" value="${de.laser.finance.CostItem.findAllBySubAndCostItemElementAndCostItemStatusNotEqualAndOwner(subParticipant, RDStore.COST_ITEM_ELEMENT_BUDGET_TITLE_PICK, RDStore.COST_ITEM_DELETED, contextOrg)}"/>

                            <g:each in="${costItemsBudget}"
                                    var="costItem">
                                <g:formatNumber number="${costItem.costInBillingCurrency}" type="currency" currencyCode="${costItem.billingCurrency.value}"/>

                                <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_EUR}">
                                    <g:set var="diffEUR" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsEUR}"/>
                                </g:if>

                                <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_USD}">
                                    <g:set var="diffUSD" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsUSD}"/>
                                </g:if>

                                <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_GBP}">
                                    <g:set var="diffGBP" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsGBP}"/>
                                </g:if>

                                <g:set var="sumBudgetEUR" value="${costItem.billingCurrency == RDStore.CURRENCY_EUR ? (sumBudgetEUR+costItem.costInBillingCurrency) : sumBudgetEUR}"/>
                                <g:set var="sumBudgetUSD" value="${costItem.billingCurrency == RDStore.CURRENCY_USD ? (sumBudgetUSD+costItem.costInBillingCurrency) : sumBudgetUSD}"/>
                                <g:set var="sumBudgetGBP" value="${costItem.billingCurrency == RDStore.CURRENCY_GBP ? (sumBudgetGBP+costItem.costInBillingCurrency) : sumBudgetGBP}"/>

                            </g:each>

                        </td>
                        <td>
                            <g:if test="${diffEUR != 0}">
                                <br>
                                <g:formatNumber
                                        number="${diffEUR}" type="currency" currencyCode="EUR"/>
                                <g:set var="sumDiffEUR" value="${sumDiffEUR+diffEUR}"/>

                            </g:if>
                            <g:if test="${diffUSD != 0}">
                                <br>
                                <g:formatNumber
                                        number="${diffUSD}" type="currency" currencyCode="USD"/>
                                <g:set var="sumDiffUSD" value="${sumDiffUSD+diffUSD}"/>

                            </g:if>
                            <g:if test="${diffGBP != 0}">
                                <br>
                                <g:formatNumber
                                        number="${diffGBP}" type="currency" currencyCode="GBP"/>
                                <g:set var="sumDiffGBP" value="${sumDiffGBP+diffGBP}"/>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('uploadTitleListDoc')}">
                        <td>
                            <g:if test="${editable}">
                                <button type="button" class="ui icon tiny button blue la-modern-button"
                                        data-ownerid="${subParticipant.id}"
                                        data-ownerclass="${subParticipant.class.name}"
                                        data-doctype="${RDStore.DOC_TYPE_TITLELIST.value}"
                                        data-ui="modal"
                                        data-href="#modalUploadTitleListDoc"><i aria-hidden="true"
                                                                            class="plus small icon"></i>
                                </button>
                            </g:if>

                            <%
                                Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: subParticipant, docType: RDStore.DOC_TYPE_TITLELIST, owner: contextOrg])
                                documentSet = documentSet.sort { it.owner?.title }
                            %>
                            <g:each in="${documentSet}" var="docctx">
                                <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                    <div class="ui small feed content">
                                        <div class="ui grid summary">
                                            <div class="eleven wide column la-column-right-lessPadding">
                                                <ui:documentIcon doc="${docctx.owner}" showText="false"
                                                                 showTooltip="true"/>
                                                <g:set var="supportedMimeType"
                                                       value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <g:if test="${supportedMimeType}">
                                                    <a href="#documentPreview"
                                                       data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>
                                            </div>

                                            <div class="right aligned five wide column la-column-left-lessPadding la-border-left">

                                                <g:if test="${!(editable)}">
                                                <%-- 1 --%>
                                                    <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                            class="ui icon blue tiny button la-modern-button la-js-dont-hide-button"
                                                            target="_blank"><i class="download small icon"></i></g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                                    <%-- 1 --%>
                                                        <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                                class="ui icon blue tiny button la-modern-button la-js-dont-hide-button"
                                                                target="_blank"><i
                                                                class="download small icon"></i></g:link>

                                                    <%-- 2 --%>
                                                        <laser:render template="/templates/documents/modal"
                                                                      model="[ownobj: subParticipant, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                        <button type="button"
                                                                class="ui icon blue tiny button la-modern-button"
                                                                data-ui="modal"
                                                                data-href="#modalEditDocument_${docctx.id}"
                                                                aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                            <i class="pencil small icon"></i>
                                                        </button>
                                                    </g:if>

                                                <%-- 4 --%>
                                                    <g:if test="${docctx.owner.owner?.id == contextOrg.id && !docctx.isShared}">
                                                        <g:link controller="${ajaxCallController ?: controllerName}"
                                                                action="deleteDocuments"
                                                                class="ui icon negative tiny button la-modern-button js-open-confirm-modal"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                                data-confirm-term-how="delete"
                                                                params='[instanceId: "${subParticipant.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                            <i class="trash alternate outline small icon"></i>
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
                            <g:link controller="subscription" action="renewEntitlementsWithSurvey" id="${subParticipant.id}"
                                    params="${[surveyConfigID: surveyConfig.id,
                                               exportXLS   : true,
                                               tab           : 'selectedIEs']}"
                                    class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                                    data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}" data-position="bottom left"
                                    target="_blank"><i class="download icon"></i></g:link>
                        </td>
                    </g:if>
                </g:each>
                <td>
                    <g:link controller="survey" action="evaluationParticipant"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                            class="ui button blue icon la-modern-button la-popup-tooltip la-delay"
                            data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                        <i class="chart pie icon"></i>
                    </g:link>

                    <g:if test="${surveyConfig.subscription}">
                        <g:set var="participantSub" value="${surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
                            <g:if test="${participantSub}">
                                <br/>
                                <g:link controller="subscription" action="show" id="${participantSub.id}"
                                        class="ui button orange icon"><i class="icon clipboard"></i></g:link>
                            </g:if>
                    </g:if>
                </td>

            </tr>

        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <g:if test="${showCheckbox}">
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
                <g:else>
                    <td></td>
                </g:else>
            </g:each>
        </tr>
        </tfoot>
    </table>

    <g:if test="${showOpenParticipantsAgainButtons}">
        <div class="content">
            <div class="ui form twelve wide column">
                <div class="two fields">
                    <g:if test="${actionName == 'participantsReminder'}">
                       %{-- <div class="eight wide field" style="text-align: left;">
                            <a data-ui="modal" class="ui button"
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
                            <button name="openOption" type="submit" value="ReminderMail" class="ui button">
                                ${message(code: 'openParticipantsAgain.reminder.participantsHasAccess')}
                            </button>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>
    </g:if>
    <br/><br/>
    <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

    <g:set var="surveyParticipantsHasNotAccess"
           value="${participants.findAll { !it.org.hasInstAdmin() }}"/>


    <g:if test="${surveyParticipantsHasNotAccess}">
        <laser:render template="/templates/copyEmailaddresses" model="[modalID: 'copyEmailaddresses_participantsWithoutAccess', orgList: surveyParticipantsHasNotAccess.org]"/>
        <a data-ui="modal" class="ui icon button right floated"
           href="#copyEmailaddresses_participantsWithoutAccess">
            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
        </a>
    </g:if>

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
            <g:if test="${showCheckbox}">
                    <g:if test="${surveyParticipantsHasNotAccess && !(actionName in ['openParticipantsAgain', 'participantsReminder']) && params.tab != 'participantsViewAllNotFinish'}">
                        <th>
                        <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                        </th>
                    </g:if>
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
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${surveyProperty.getI10n('expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </th>
                    </g:each>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                    <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
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
                    <th>
                        Upload <br>
                        ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                    <th>
                        ${message(code: 'surveyInfo.finishedDate')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                    <th>
                        ${message(code: 'surveyOrg.reminderMailDate')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('downloadTitleList')}">
                    <th>
                        Download <br>
                        ${RDStore.DOC_TYPE_TITLELIST.getI10n('value')}
                    </th>
                </g:if>

            </g:each>
            <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
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
                <g:if test="${showCheckbox}">
                    <g:if test="${!(actionName in ['openParticipantsAgain', 'participantsReminder']) && params.tab != 'participantsViewAllNotFinish'}">
                    <td>
                        <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                    </td>
                    </g:if>
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

                        <g:if test="${surveyConfig.surveyProperties}">
                            <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.processedOrg')}">
                                    <i class="edit green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                    <i class="edit red icon"></i>
                                </span>
                            </g:else>
                        </g:if>

                            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.finishOrg')}">
                                    <i class="check green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                    <i class="x red icon"></i>
                                </span>
                            </g:else>

                            <g:if test="${propertiesChangedByParticipant && participant.id in propertiesChangedByParticipant.id}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                                    <i class="exclamation triangle yellow large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${!surveyConfig.hasOrgSubscription(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyOrg.orgInsertedItself}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyLinks.newParticipate')}">
                                    <i class="paper plane outline large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${showOpenParticipantsAgainButtons}">
                                <a href="${"mailto:${surveyOrg.org.getMailsOfGeneralContactPersons(false).join(';')}?subject=" + mailSubject +
                                        "&body=" + mailBody}">
                                    <span data-position="right center"
                                          class="la-popup-tooltip la-delay"
                                          data-content="Mail senden an Hauptkontakte">
                                        <i class="ui icon envelope outline la-list-icon"></i>
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

                        <g:set var="sumListPriceSelectedIEsEUR" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_EUR)}"/>
                        <g:set var="sumListPriceSelectedIEsUSD" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_USD)}"/>
                        <g:set var="sumListPriceSelectedIEsGBP" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subParticipant, surveyConfig, RDStore.CURRENCY_GBP)}"/>

                        <td class="center aligned">
                            <g:set var="ieGroup"
                                   value="${IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subParticipant)}"/>
                            <div class="ui circular label">
                                <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                                    ${surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subParticipant, surveyConfig)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                                </g:if>
                                <g:else>
                                    ${subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subParticipant, ieGroup)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                                </g:else>
                            </div>

                        </td>
                        <td>
                            <g:if test="${sumListPriceSelectedIEsEUR > 0}">
                                <br>
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsEUR}" type="currency" currencyCode="EUR"/>
                                <g:set var="sumListPriceEUR" value="${sumListPriceEUR+sumListPriceSelectedIEsEUR}"/>

                            </g:if>
                            <g:if test="${sumListPriceSelectedIEsUSD > 0}">
                                <br>
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsUSD}" type="currency" currencyCode="USD"/>
                                <g:set var="sumListPriceUSD" value="${sumListPriceUSD+sumListPriceSelectedIEsUSD}"/>

                            </g:if>
                            <g:if test="${sumListPriceSelectedIEsGBP > 0}">
                                <br>
                                <g:formatNumber
                                        number="${sumListPriceSelectedIEsGBP}" type="currency" currencyCode="GBP"/>
                                <g:set var="sumListPriceGBP" value="${sumListPriceGBP+sumListPriceSelectedIEsGBP}"/>
                            </g:if>
                        </td>
                        <td>
                            <g:set var="costItemsBudget" value="${de.laser.finance.CostItem.findAllBySubAndCostItemElementAndCostItemStatusNotEqualAndOwner(subParticipant, RDStore.COST_ITEM_ELEMENT_BUDGET_TITLE_PICK, RDStore.COST_ITEM_DELETED, contextOrg)}"/>

                            <g:each in="${costItemsBudget}"
                                    var="costItem">
                                <g:formatNumber number="${costItem.costInBillingCurrency}" type="currency" currencyCode="${costItem.billingCurrency.value}"/>

                                <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_EUR}">
                                    <g:set var="diffEUR" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsEUR}"/>
                                </g:if>

                                <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_USD}">
                                    <g:set var="diffUSD" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsUSD}"/>
                                </g:if>

                                <g:if test="${costItem.billingCurrency == RDStore.CURRENCY_GBP}">
                                    <g:set var="diffGBP" value="${costItem.costInBillingCurrency - sumListPriceSelectedIEsGBP}"/>
                                </g:if>

                                <g:set var="sumBudgetEUR" value="${costItem.billingCurrency == RDStore.CURRENCY_EUR ? (sumBudgetEUR+costItem.costInBillingCurrency) : sumBudgetEUR}"/>
                                <g:set var="sumBudgetUSD" value="${costItem.billingCurrency == RDStore.CURRENCY_USD ? (sumBudgetUSD+costItem.costInBillingCurrency) : sumBudgetUSD}"/>
                                <g:set var="sumBudgetGBP" value="${costItem.billingCurrency == RDStore.CURRENCY_GBP ? (sumBudgetGBP+costItem.costInBillingCurrency) : sumBudgetGBP}"/>

                            </g:each>

                        </td>
                        <td>
                            <g:if test="${diffEUR != 0}">
                                <br>
                                <g:formatNumber
                                        number="${diffEUR}" type="currency" currencyCode="EUR"/>
                                <g:set var="sumDiffEUR" value="${sumDiffEUR+diffEUR}"/>

                            </g:if>
                            <g:if test="${diffUSD != 0}">
                                <br>
                                <g:formatNumber
                                        number="${diffUSD}" type="currency" currencyCode="USD"/>
                                <g:set var="sumDiffUSD" value="${sumDiffUSD+diffUSD}"/>

                            </g:if>
                            <g:if test="${diffGBP != 0}">
                                <br>
                                <g:formatNumber
                                        number="${diffGBP}" type="currency" currencyCode="GBP"/>
                                <g:set var="sumDiffGBP" value="${sumDiffGBP+diffGBP}"/>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('uploadTitleListDoc')}">
                        <td>
                            <g:if test="${editable}">
                                <button type="button" class="ui icon tiny button blue la-modern-button"
                                        data-ownerid="${subParticipant.id}"
                                        data-ownerclass="${subParticipant.class.name}"
                                        data-doctype="${RDStore.DOC_TYPE_TITLELIST.value}"
                                        data-ui="modal"
                                        data-href="#modalUploadTitleListDoc"><i aria-hidden="true"
                                                                            class="plus small icon"></i>
                                </button>
                            </g:if>

                            <%
                                Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: subParticipant, docType: RDStore.DOC_TYPE_TITLELIST, owner: contextOrg])
                                documentSet = documentSet2.sort { it.owner?.title }
                            %>
                            <g:each in="${documentSet2}" var="docctx">
                                <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                    <div class="ui small feed content">
                                        <div class="ui grid summary">
                                            <div class="eleven wide column la-column-right-lessPadding">
                                                <ui:documentIcon doc="${docctx.owner}" showText="false"
                                                                 showTooltip="true"/>
                                                <g:set var="supportedMimeType"
                                                       value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <g:if test="${supportedMimeType}">
                                                    <a href="#documentPreview"
                                                       data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>
                                            </div>

                                            <div class="right aligned five wide column la-column-left-lessPadding la-border-left">

                                                <g:if test="${!(editable)}">
                                                <%-- 1 --%>
                                                    <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                            class="ui icon blue tiny button la-modern-button la-js-dont-hide-button"
                                                            target="_blank"><i class="download small icon"></i></g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                                    <%-- 1 --%>
                                                        <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                                class="ui icon blue tiny button la-modern-button la-js-dont-hide-button"
                                                                target="_blank"><i
                                                                class="download small icon"></i></g:link>

                                                    <%-- 2 --%>
                                                        <laser:render template="/templates/documents/modal"
                                                                      model="[ownobj: subParticipant, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                        <button type="button"
                                                                class="ui icon blue tiny button la-modern-button"
                                                                data-ui="modal"
                                                                data-href="#modalEditDocument_${docctx.id}"
                                                                aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                            <i class="pencil small icon"></i>
                                                        </button>
                                                    </g:if>

                                                <%-- 4 --%>
                                                    <g:if test="${docctx.owner.owner?.id == contextOrg.id && !docctx.isShared}">
                                                        <g:link controller="${ajaxCallController ?: controllerName}"
                                                                action="deleteDocuments"
                                                                class="ui icon negative tiny button la-modern-button js-open-confirm-modal"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                                data-confirm-term-how="delete"
                                                                params='[instanceId: "${subParticipant.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                            <i class="trash alternate outline small icon"></i>
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
                            <g:link controller="subscription" action="renewEntitlementsWithSurvey" id="${subParticipant.id}"
                                    params="${[surveyConfigID: surveyConfig.id,
                                               exportXLS   : true,
                                               tab           : 'selectedIEs']}"
                                    class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                                    data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}" data-position="bottom left"
                                    target="_blank"><i class="download icon"></i></g:link>
                        </td>
                    </g:if>

                </g:each>
                <td>
                    <g:link controller="survey" action="evaluationParticipant"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                            class="ui button blue icon la-modern-button la-popup-tooltip la-delay"
                            data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                        <i class="chart pie icon"></i>
                    </g:link>

                    <g:if test="${surveyConfig.subscription}">
                        <g:set var="participantSub" value="${surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
                        <g:if test="${participantSub}">
                            <br/>
                            <g:link controller="subscription" action="show" id="${participantSub.id}"
                                    class="ui button orange icon"><i class="icon clipboard"></i></g:link>
                        </g:if>
                    </g:if>
                </td>
            </tr>

        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <g:if test="${showCheckbox}">
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
                <g:else>
                    <td></td>
                </g:else>
            </g:each>
            <td></td>
        </tr>
        </tfoot>
    </table>

    <g:if test="${showTransferFields}">
        <br />
        <br />
        <ui:greySegment>
        <div class="ui form">
        <h3 class="ui header">${message(code: 'surveyTransfer.info.label')}:</h3>
            <div class="two fields">
                <div class="ui field">
                     <div class="field">
                        <label>${message(code: 'filter.status')}</label>
                        <ui:select class="ui dropdown" name="status" id="status"
                                      from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                      optionKey="id"
                                      optionValue="value"
                                      multiple="true"
                                      value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                      onchange="JSPC.app.adjustDropdown()"/>
                    </div>
                    <br />
                    <br id="element-vor-target-dropdown" />
                    <br />

                </div>
                <div class="field">
                    <ui:datepicker label="subscription.startDate.label" id="startDate" name="startDate" value=""/>

                    <ui:datepicker label="subscription.endDate.label" id="endDate" name="endDate" value=""/>
                </div>
            </div>

            <input class="ui button" type="submit" value="${message(code: 'surveyTransfer.button')}">
        </ui:greySegment>
        </div>

    </g:if>

    <g:if test="${showOpenParticipantsAgainButtons}">
        <div class="content">
            <div class="ui form twelve wide column">
                <div class="two fields">
                    <g:if test="${actionName == 'openParticipantsAgain'}">

                        <div class="eight wide field" style="text-align: left;">
                            <button name="openOption" type="submit" value="OpenWithoutMail" class="ui button">
                                ${message(code: 'openParticipantsAgain.openWithoutMail.button')}
                            </button>
                        </div>

                        <div class="eight wide field" style="text-align: right;">
                            <button name="openOption" type="submit" value="OpenWithMail" class="ui button">
                                ${message(code: 'openParticipantsAgain.openWithMail.button')}
                            </button>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>
    </g:if>

</g:form>



<laser:script file="${this.getGroovyPageFileName()}">
<g:if test="${showCheckbox}">
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

    var status = $("select#status").serialize()
    if (status) {
        url = url + '?' + status
    }

    $.ajax({
        url: url,
        success: function (data) {
            var select = '';
            for (var index = 0; index < data.length; index++) {
                var option = data[index];
                var optionText = option.text;
                var optionValue = option.value;
                var count = index + 1
                // console.log(optionValue +'-'+optionText)

                select += '<div class="item" data-value="' + optionValue + '">'+ count + ': ' + optionText + '</div>';
            }

            select = ' <div class="ui fluid search selection dropdown la-filterProp">' +
'   <input type="hidden" id="subscription" name="targetSubscriptionId">' +
'   <i class="dropdown icon"></i>' +
'   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
'   <div class="menu">'
+ select +
'</div>' +
'</div>';

            $('#element-vor-target-dropdown').next().replaceWith(select);

            $('.la-filterProp').dropdown({
                duration: 150,
                transition: 'fade',
                clearable: true,
                forceSelection: false,
                selectOnKeydown: false,
                onChange: function (value, text, $selectedItem) {
                    value.length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
                }
            });
        }, async: false
    });
}

JSPC.app.adjustDropdown()
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
    </laser:script>

</g:if>

