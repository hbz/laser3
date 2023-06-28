<%@ page import="de.laser.storage.RDStore; de.laser.Subscription; de.laser.Subscription; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.Org; de.laser.CustomerTypeService; de.laser.Doc; de.laser.survey.SurveyOrg;" %>

<laser:htmlStart message="menu.my.currentSubscriptionsTransfer" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.currentSubscriptionsTransfer" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="menu.my.currentSubscriptionsTransfer" total="${num_sub_rows}" floated="true"/>

<g:if test="${params.referenceYears}">
    <div class="ui large label la-annual-rings"><g:link action="currentSubscriptionsTransfer"
                                                        params="${[referenceYears: (Integer.parseInt(params.referenceYears).value - 1).toString()]}"
                                                        class="item"><i class="arrow left icon"
                                                                        aria-hidden="true"></i></g:link><span
            class="la-annual-rings-text">${params.referenceYears}</span><g:link action="currentSubscriptionsTransfer"
                                                                                params="${[referenceYears: (Integer.parseInt(params.referenceYears).value + 1).toString()]}"
                                                                                class="item"><i class="arrow right icon"
                                                                                                aria-hidden="true"></i></g:link>
    </div>
</g:if>

<ui:messages data="${flash}"/>

<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
        <table class="ui celled sortable table la-table la-js-responsive-table">
            <thead>
            <tr>
                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn scope="col" params="${params}" property="providerAgency"
                                  title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}"
                                  rowspan="2"/>

                <g:sortableColumn params="${params}" property="s.name" title="${message(code: 'subscription')}"
                                  rowspan="2"
                                  scope="col"/>

                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate"
                                  title="${message(code: 'default.startDate.label')}"/>

                <g:sortableColumn params="${params}" property="s.manualCancellationDate"
                                  title="${message(code: 'subscription.manualCancellationDate.label')}" rowspan="2"
                                  scope="col"/>

                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="s.offerRequested"
                                  title="${message(code: 'subscription.offerRequested.label')}"/>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.offerNote.label')}
                </th>

                <g:sortableColumn params="${params}" property="s.offerAccepted"
                                  title="${message(code: 'subscription.offerAccepted.label')}" rowspan="2" scope="col"/>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.priceIncreaseInfo.label')}
                </th>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'survey.label')}
                </th>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.survey.evaluation.label')}
                </th>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.survey.cancellation.label')}
                </th>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.discountScale.label')}
                </th>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.renewalFile.label')}
                </th>

                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.renewalSent"
                                  title="${message(code: 'subscription.renewalSent.label')}"/>

                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'subscription.participantTransferWithSurvey.label')}
                </th>

            </tr>
            <tr>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate"
                                  title="${message(code: 'default.endDate.label')}"/>

                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="s.offerRequestedDate"
                                  title="${message(code: 'subscription.offerRequestedDate.label')}"/>

                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="s.renewalSentDate"
                                  title="${message(code: 'subscription.renewalSentDate.label')}"/>
            </tr>
            </thead>
            <tbody>
            <g:each in="${subscriptions}" var="s" status="i">
                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td>
                    <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                        <g:each in="${s.providers}" var="org">
                            <g:link controller="organisation" action="show"
                                    id="${org.id}">${fieldValue(bean: org, field: "name")}
                                <g:if test="${org.sortname}">
                                    <br/>
                                    (${fieldValue(bean: org, field: "sortname")})
                                </g:if>
                            </g:link><br/>
                        </g:each>
                        <g:each in="${s.agencies}" var="org">
                            <g:link controller="organisation" action="show" id="${org.id}">
                                ${fieldValue(bean: org, field: "name")}
                                <g:if test="${org.sortname}">
                                    <br/>
                                    (${fieldValue(bean: org, field: "sortname")})
                                </g:if> (${message(code: 'default.agency.label')})
                            </g:link><br/>
                        </g:each>
                    </td>
                    <th scope="row" class="la-th-column">
                        <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                            <g:if test="${s.name}">
                                ${s.name}
                            </g:if>
                            <g:else>
                                -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                            </g:else>
                        </g:link>
                    </th>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                        <span class="la-secondHeaderRow"
                              data-label="${message(code: 'default.endDate.label')}:"><g:formatDate
                                formatName="default.date.format.notime" date="${s.endDate}"/></span>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${s.manualCancellationDate}"/>
                    </td>
                    <td>
                        <ui:xEditableBoolean owner="${s}" field="offerRequested"/>
                        <br/>
                        <ui:xEditable owner="${s}" field="offerRequestedDate" type="date"
                                      validation="datesCheck"/>
                    </td>
                    <td>
                        <div class="right aligned">
                            <button type="button" class="ui icon button blue la-modern-button" data-ui="modal"
                                    data-href="${"#modalCreateDocumentOffer" + s.id}"><i aria-hidden="true"
                                                                                         class="plus icon"></i></button>
                        </div>
                        <laser:render template="/templates/documents/modal"
                                      model="${[newModalId: "modalCreateDocumentOffer" + s.id, ownobj: s, owntp: 'subscription']}"/>

                        <%
                            Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType', [subscription: s, docType: RDStore.DOC_TYPE_OFFER])
                            documentSet = documentSet.sort { it.owner?.title }
                        %>
                        <g:each in="${documentSet}" var="docctx">
                            <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                <div class="ui small feed content la-js-dont-hide-this-card">
                                    <div class="ui grid summary">
                                        <div class="eight wide column la-column-right-lessPadding">

                                            <g:set var="supportedMimeType"
                                                   value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                            <g:if test="${supportedMimeType}">
                                                <a href="#documentPreview"
                                                   data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                            </g:if>
                                            <g:else>
                                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                            </g:else>
                                            <g:if test="${docctx.getDocType()}">
                                                (${docctx.getDocType().getI10n("value")})
                                            </g:if>

                                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                        </div>

                                        <div class="right aligned eight wide column la-column-left-lessPadding">

                                            <g:if test="${!(editable)}">
                                            <%-- 1 --%>
                                                <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                        class="ui icon blue button la-modern-button la-js-dont-hide-button"
                                                        target="_blank"><i class="download icon"></i></g:link>
                                            </g:if>
                                            <g:else>
                                                <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                                <%-- 1 --%>
                                                    <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                            class="ui icon blue button la-modern-button la-js-dont-hide-button"
                                                            target="_blank"><i class="download icon"></i></g:link>

                                                <%-- 2 --%>
                                                    <laser:render template="/templates/documents/modal"
                                                                  model="[s: s, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                    <button type="button" class="ui icon blue button la-modern-button"
                                                            data-ui="modal"
                                                            data-href="#modalEditDocument_${docctx.id}"
                                                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                        <i class="pencil icon"></i>
                                                    </button>
                                                </g:if>

                                            <%-- 4 --%>
                                                <g:if test="${docctx.owner.owner?.id == contextOrg.id && !docctx.isShared}">
                                                    <g:link controller="${ajaxCallController ?: controllerName}"
                                                            action="deleteDocuments"
                                                            class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                            data-confirm-term-how="delete"
                                                            params='[instanceId: "${s.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                        <i class="trash alternate outline icon"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <div class="ui icon button la-hidden">
                                                        <i class="fake icon"></i><%-- Hidden Fake Button --%>
                                                    </div>
                                                </g:else>
                                            </g:else>%{-- (editable || editable2) --}%
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </g:each>

                        <ui:xEditable owner="${s}" field="offerNote"/>
                    </td>
                    <td>
                        <ui:xEditableBoolean owner="${s}" field="offerAccepted"/>
                    </td>
                    <td>
                        <ui:xEditable owner="${s}" field="priceIncreaseInfo" type="textarea"/>
                    </td>

                    <g:set var="surveyConfig"
                           value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s, true)}"/>
                    <g:set var="surveyClass" value=""/>
                    <g:if test="${surveyConfig}">
                        <g:set var="surveyClass"
                               value="${surveyConfig.surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED ? 'positive' : (surveyConfig.surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY] ? 'warning' : '')}"/>
                    </g:if>
                    <td class="${surveyClass}">
                        <g:if test="${surveyConfig}">
                            <g:link controller="survey" action="show" id="${surveyConfig.surveyInfo.id}">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${surveyConfig.surveyInfo.startDate}"/><br/>
                                <span class="la-secondHeaderRow"
                                      data-label="${message(code: 'default.endDate.label')}:"><g:formatDate
                                        formatName="default.date.format.notime"
                                        date="${surveyConfig.surveyInfo.endDate}"/></span>
                            </g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${surveyConfig}">
                            <g:set var="finish"
                                   value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()}"/>
                            <g:set var="total"
                                   value="${SurveyOrg.findAllBySurveyConfig(surveyConfig).size()}"/>

                            <g:set var="finishProcess"
                                   value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
                            <g:if test="${finishProcess > 0}">
                                <g:link controller="survey" action="surveyEvaluation"
                                        id="${surveyConfig.surveyInfo.id}">
                                    <g:formatNumber number="${finishProcess}"
                                                    type="number"
                                                    maxFractionDigits="2"
                                                    minFractionDigits="2"/>%
                                </g:link>
                            </g:if>
                        </g:if>
                    </td>

                    <g:set var="countOrgsWithTermination" value="${0}"/>
                    <g:if test="${surveyConfig}">
                        <g:set var="countOrgsWithTermination" value="${surveyConfig.countOrgsWithTermination()}"/>
                    </g:if>

                    <td class="${countOrgsWithTermination > 0 && countOrgsWithTermination <= 10 ? 'warning' : (countOrgsWithTermination > 10 ? 'negative' : '')}">
                        <g:if test="${surveyConfig && countOrgsWithTermination >= 0}">
                            <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}">
                                ${countOrgsWithTermination}
                            </g:link>
                        </g:if>
                    </td>
                    <td>

                    </td>

                    <td>

                        <div class="right aligned">
                            <button type="button" class="ui icon button blue la-modern-button" data-ui="modal"
                                    data-href="${"#modalCreateDocumentRenewal" + s.id}"><i aria-hidden="true"
                                                                                           class="plus icon"></i>
                            </button>
                        </div>
                        <laser:render template="/templates/documents/modal"
                                      model="${[newModalId: "modalCreateDocumentRenewal" + s.id, ownobj: s, owntp: 'subscription']}"/>

                        <%
                            Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType', [subscription: s, docType: RDStore.DOC_TYPE_RENEWAL])
                            documentSet2 = documentSet2.sort { it.owner?.title }
                        %>
                        <g:each in="${documentSet2}" var="docctx">
                            <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                <div class="ui small feed content la-js-dont-hide-this-card">
                                    <div class="ui grid summary">
                                        <div class="eight wide column la-column-right-lessPadding">

                                            <g:set var="supportedMimeType"
                                                   value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                            <g:if test="${supportedMimeType}">
                                                <a href="#documentPreview"
                                                   data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                            </g:if>
                                            <g:else>
                                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                            </g:else>
                                            <g:if test="${docctx.getDocType()}">
                                                (${docctx.getDocType().getI10n("value")})
                                            </g:if>

                                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                        </div>

                                        <div class="right aligned eight wide column la-column-left-lessPadding">

                                            <g:if test="${!(editable)}">
                                            <%-- 1 --%>
                                                <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                        class="ui icon blue button la-modern-button la-js-dont-hide-button"
                                                        target="_blank"><i class="download icon"></i></g:link>
                                            </g:if>
                                            <g:else>
                                                <g:if test="${docctx.owner.owner?.id == contextOrg.id}">
                                                <%-- 1 --%>
                                                    <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                            class="ui icon blue button la-modern-button la-js-dont-hide-button"
                                                            target="_blank"><i class="download icon"></i></g:link>

                                                <%-- 2 --%>
                                                    <laser:render template="/templates/documents/modal"
                                                                  model="[s: s, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                    <button type="button" class="ui icon blue button la-modern-button"
                                                            data-ui="modal"
                                                            data-href="#modalEditDocument_${docctx.id}"
                                                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                        <i class="pencil icon"></i>
                                                    </button>
                                                </g:if>

                                            <%-- 4 --%>
                                                <g:if test="${docctx.owner.owner?.id == contextOrg.id && !docctx.isShared}">
                                                    <g:link controller="${ajaxCallController ?: controllerName}"
                                                            action="deleteDocuments"
                                                            class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                            data-confirm-term-how="delete"
                                                            params='[instanceId: "${s.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                        <i class="trash alternate outline icon"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <div class="ui icon button la-hidden">
                                                        <i class="fake icon"></i><%-- Hidden Fake Button --%>
                                                    </div>
                                                </g:else>
                                            </g:else>%{-- (editable || editable2) --}%
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </g:each>
                    </td>

                    <td>
                        <ui:xEditableBoolean owner="${s}" field="renewalSent"/>
                        <br/>
                        <ui:xEditable owner="${s}" field="renewalSentDate" type="date"
                                      validation="datesCheck"/>
                    </td>

                    <td>
                        <ui:xEditableBoolean owner="${s}" field="participantTransferWithSurvey"/>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
    %{-- <g:if test="${filterSet}">
         <br/><strong><g:message code="filter.result.empty.object"
                                 args="${[message(code: "subscription.plural")]}"/></strong>
     </g:if>
     <g:else>--}%
        <br/><strong><g:message code="result.empty.object"
                                args="${[message(code: "subscription.plural")]}"/></strong>
    %{--</g:else>--}%
    </g:else>

</div>

<g:if test="${subscriptions}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                 max="${max}" total="${num_sub_rows}"/>
</g:if>


<laser:render template="export/individuallyExportModalSubsTransfer" model="[modalID: 'individuallyExportModal']" />

<laser:htmlEnd/>
