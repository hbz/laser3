<!-- template: meta/subscriptionTransferInfo -->
<%@ page import="de.laser.helper.Icons; de.laser.ExportClickMeService; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Subscription; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.Org; de.laser.CustomerTypeService; de.laser.Doc; de.laser.survey.SurveyOrg;" %>

<laser:serviceInjection />

<div class="ui yellow segment" id="subscriptionTransfer-content">
    <div class="content">
        <table class="ui compact monitor stackable celled sortable table la-table la-js-responsive-table">
            <thead>
            <tr>
                <th scope="col" rowspan="3">${message(code: 'subscription.referenceYear.short.label')}</th>
                <th scope="col" rowspan="3">${message(code: 'provider.label')}</th>
                <th scope="col" rowspan="3">${message(code: 'vendor.label')}</th>
                <th scope="col" rowspan="3">${message(code: 'subscription')}</th>
                <th scope="col" rowspan="2" class="la-smaller-table-head">${message(code: 'default.startDate.label.shy')}</th>
                <th scope="col" rowspan="3">${message(code: 'subscription.manualCancellationDate.label.shy')}</th>
                <th colspan="3"
                    class="la-smaller-table-head center aligned">${message(code: 'subscription.offer.table.th')}</th>
                <th scope="col" rowspan="3" class="center aligned">${message(code: 'subscription.priceIncreaseInfo.label')}</th>

                <th scope="col" rowspan="3" class="center aligned">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code: 'survey.label')}" data-position="top center">
                        <i class="${Icons.SURVEY} large icon"></i>
                    </span>
                </th>
                <th scope="col" rowspan="3" class="center aligned">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.survey.evaluation.label')}" data-position="top center">
                        <i class="comments large icon"></i>
                    </span>
                </th>
                <th scope="col" rowspan="3" class="center aligned">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.survey.cancellation.label')}" data-position="top center">
                        <i class="times circle large icon"></i>
                    </span>
                </th>
                <th scope="col" rowspan="3" class="center aligned">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.discountScale.label')}" data-position="top center">
                        <i class="percentage large icon"></i>
                    </span>
                </th>

                <th class="la-smaller-table-head center aligned">Reminder</th>
                <th colspan="3" class="la-smaller-table-head center aligned">Renewal</th>

                <th scope="col" rowspan="3" class="center aligned">
                    <span class="la-popup-tooltip la-delay"
                            data-content="${message(code: 'subscription.participantTransferWithSurvey.label')}"
                            data-position="top center">
                        <i class="large icons">
                            <i class="chart pie icon"></i>
                            <i class="top right corner redo icon"></i>
                        </i>
                    </span>
                </th>
                <th scope="col" rowspan="3" class="center aligned">
                    <span class="la-popup-tooltip la-delay"
                       data-content="${message(code: 'renewalEvaluation.exportRenewal')}"
                       data-position="top center">
                        <i class="download icon"></i>
                    </span>
                </th>
                <th scope="col" rowspan="3" class="center aligned"></th>
            </tr>
            <tr>
                <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.offerRequested.table.th')}</th>
                <th scope="col" rowspan="2" class="center aligned two wide">${message(code: 'subscriptionsManagement.documents')}</th>
                <th scope="col" rowspan="2">${message(code: 'subscription.offerAccepted.table.th')}</th>
                <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.reminderSent.table.th')}</th>
                <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.renewalSent.table.th')}</th>
                <th scope="col" rowspan="2" class="center aligned two wide">${message(code: 'subscriptionsManagement.documents')}</th>
                <th scope="col" rowspan="2" class="center aligned two wide">
                    <g:message code="default.change.label"/>
                </th>
            </tr>
            <tr>
                <th scope="col" rowspan="1" class="la-smaller-table-head">${message(code: 'default.endDate.label')}</th>
                <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.offerRequestedDate.table.th')}</th>
                <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.reminderSentDate.table.th')}</th>
                <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.renewalSentDate.table.th')}</th>
            </tr>
            </thead>
        <tbody>
            <g:each in="${calculatedSubList}" var="s">
                <tr>
                    <td>
                        ${s.referenceYear}
                    </td>
                    <td>
                    <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                        <g:each in="${s.providers}" var="org">
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">
                                ${fieldValue(bean: org, field: "name")}
                                <g:if test="${org.sortname}">
                                    <br/> (${fieldValue(bean: org, field: "sortname")})
                                </g:if>
                            </g:link>
                            <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${s.vendors}" var="vendor">
                            <g:link controller="vendor" action="show" id="${vendor.id}" target="_blank">
                                ${fieldValue(bean: vendor, field: "name")}
                                <g:if test="${vendor.sortname}">
                                    <br/> (${fieldValue(bean: vendor, field: "sortname")})
                                </g:if>
                            </g:link>
                            <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}"
                                target="_blank">
                            <g:if test="${s.name}">
                                ${s.name}
                            </g:if>
                            <g:else>
                                -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                            </g:else>
                        </g:link>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/>
                        <br/>
                        <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label')}:">
                            <g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/>
                        </span>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${s.manualCancellationDate}"/>
                    </td>
                    <td class="${s.offerRequested ? 'positive' : 'negative'}">
                        ${s.offerRequested ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <br/>
                        <g:formatDate formatName="default.date.format.notime" date="${s.offerRequestedDate}"/>
                    </td>
                    <td>
                        <%
                            Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: s, docType: RDStore.DOC_TYPE_OFFER, owner: contextService.getOrg()])
                            documentSet = documentSet.sort { it.owner?.title }
                        %>
                        <g:each in="${documentSet}" var="docctx">
                            <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                <div class="ui small feed content">
                                    <div class="ui grid summary">
                                        <div class="eleven wide column la-column-right-lessPadding">
                                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                            <g:if test="${supportedMimeType}">
                                                <a href="#documentPreview"
                                                   data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                            </g:if>
                                            <g:else>
                                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                            </g:else>
                                        %{-- <g:if test="${docctx.getDocType()}">
                                             (${docctx.getDocType().getI10n("value")})
                                         </g:if>--}%
                                        </div>

                                        <div class="right aligned five wide column la-column-left-lessPadding la-border-left">
                                            <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                    class="ui icon blue tiny button la-modern-button"
                                                    target="_blank">
                                                <i class="download small icon"></i>
                                            </g:link>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </g:each>
                        <div class="ui small feed content">
                            ${s.offerNote}
                        </div>
                    </td>
                    <td class="${s.offerAccepted ? 'positive' : 'negative'}">
                        ${s.offerAccepted ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </td>
                    <td>
                        ${s.priceIncreaseInfo}
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
                            <g:link controller="survey" action="show" id="${surveyConfig.surveyInfo.id}"
                                    target="_blank">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${surveyConfig.surveyInfo.startDate}"/>
                                <br/>
                                <span class="la-secondHeaderRow"
                                      data-label="${message(code: 'default.endDate.label')}:">
                                    <g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyConfig.surveyInfo.endDate}"/>
                                </span>
                            </g:link>
                        </g:if>
                    </td>
                    <g:if test="${surveyConfig}">
                        <g:set var="finish"
                               value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()}"/>
                        <g:set var="total"
                               value="${SurveyOrg.findAllBySurveyConfig(surveyConfig).size()}"/>

                        <g:set var="finishProcess"
                               value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
                        <td class="${finish == total ? 'positive' : ''}">
                            <g:if test="${finishProcess >= 0}">
                                <g:link controller="survey" action="surveyEvaluation"
                                        id="${surveyConfig.surveyInfo.id}">
                                    ${finish}/${total}
                                    (<g:formatNumber number="${finishProcess}"
                                                     type="number"
                                                     maxFractionDigits="2"
                                                     minFractionDigits="2"/>%)
                                </g:link>
                            </g:if>
                        </td>
                    </g:if><g:else>
                    <td></td>
                </g:else>

                    <g:set var="countOrgsWithTermination" value="${0}"/>
                    <g:if test="${surveyConfig && surveyConfig.surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED, RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]}">
                        <g:set var="countOrgsWithTermination" value="${surveyConfig.countOrgsWithTermination()}"/>
                    </g:if>

                    <td class="${countOrgsWithTermination > 0 && countOrgsWithTermination <= 10 ? 'warning' : (countOrgsWithTermination > 10 ? 'negative' : '')}">
                        <g:if test="${surveyConfig && countOrgsWithTermination >= 0}">
                            <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}" target="_blank">
                                ${countOrgsWithTermination}
                            </g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${s.discountScales.size() > 0}">
                            <g:if test="${s.discountScale}">
                                ${s.discountScale.name} : ${s.discountScale.discount}
                                <g:if test="${s.discountScale.note}">
                                    <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${s.discountScale.note}">
                                        <i class="info circle icon blue"></i>
                                    </span>
                                </g:if>
                            </g:if>
                        </g:if>
                    </td>

                    <td class="${s.reminderSent ? 'positive' : 'negative'}">
                        ${s.reminderSent ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <br/>
                        <g:formatDate formatName="default.date.format.notime" date="${s.reminderSentDate}"/>
                    </td>

                    <td class="${s.renewalSent ? 'positive' : 'negative'}">
                        ${s.renewalSent ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <br/>
                        <g:formatDate formatName="default.date.format.notime" date="${s.renewalSentDate}"/>
                    </td>
                    <td>
                        <%
                            Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: s, docType: RDStore.DOC_TYPE_RENEWAL, owner: contextService.getOrg()])
                            documentSet2 = documentSet2.sort { it.owner?.title }
                        %>
                        <g:each in="${documentSet2}" var="docctx">
                            <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                <div class="ui small feed content">
                                    <div class="ui grid summary">
                                        <div class="eleven wide column la-column-right-lessPadding">
                                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                            <g:set var="supportedMimeType"
                                                   value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                            <g:if test="${supportedMimeType}">
                                                <a href="#documentPreview"
                                                   data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                            </g:if>
                                            <g:else>
                                                ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                            </g:else>
                                        %{-- <g:if test="${docctx.getDocType()}">
                                             (${docctx.getDocType().getI10n("value")})
                                         </g:if>--}%
                                        </div>

                                        <div class="right aligned five wide column la-column-left-lessPadding la-border-left">
                                            <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                    class="ui icon blue tiny button la-modern-button"
                                                    target="_blank">
                                                <i class="download small icon"></i>
                                            </g:link>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </g:each>
                    </td>

                    <g:set var="countModificationToContactInformationAfterRenewalDoc" value="${surveyConfig ? surveyService.countModificationToContactInformationAfterRenewalDoc(s) : 0}"/>

                    <td class="${surveyConfig ? countModificationToContactInformationAfterRenewalDoc == 0 ? 'positive' : 'negative' : ''}">
                        <g:if test="${countModificationToContactInformationAfterRenewalDoc > 0}">
                            <g:link class="ui label triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                    params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION, id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id]">
                                <i class="download icon"></i> ${countModificationToContactInformationAfterRenewalDoc}
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:if test="${surveyConfig}">
                                ${countModificationToContactInformationAfterRenewalDoc}
                            </g:if>
                        </g:else>
                    </td>

                    <td class="${s.participantTransferWithSurvey ? 'positive' : 'negative'}">
                        ${s.participantTransferWithSurvey ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </td>
                    <td>
                        <g:if test="${surveyConfig && surveyConfig.surveyInfo.status in [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]}">
                            <g:link class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION,
                                         id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id,
                                         modalText: 'Export: ('+ g.message(code: 'subscription.referenceYear.label')+': '+ surveyConfig.subscription.referenceYear+')']">
                                <i class="download small icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:link controller="subscription" action="subTransfer" id="${s.id}"
                                class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="write icon"></i>
                        </g:link>
                    </td>
                </tr>
                </tbody>
            </g:each>
        </table>

    </div>
</div>


<g:render template="/clickMe/export/js"/>

<laser:script file="${this.getGroovyPageFileName()}">
    docs.init('#subscriptionTransfer-content');

    setTimeout(function() {
    tooltip.init('#subscriptionTransfer-content');
    }, 1000);
</laser:script>
<style>
    #subscriptionTransfer-content {
        position: fixed !important;
        top: 100px;
        z-index: 100;
        display: none;
    }
</style>
<!-- template: meta/subscriptionTransferInfo -->