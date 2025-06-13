<!-- template: meta/subscriptionTransferInfo -->
<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Subscription; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.Org; de.laser.CustomerTypeService; de.laser.Doc; de.laser.survey.SurveyOrg;" %>

<laser:serviceInjection />

<div class="ui yellow segment " id="subscriptionTransfer-content" style="width: inherit;">
    <div class="ui content grid">
        <div class="sixteen wide column">
            <table class="ui compact monitor stackable celled sortable table la-table la-js-responsive-table">
                <thead>
                <tr>
                    <th class="one wide" cope="col" rowspan="3">${message(code: 'subscription.referenceYear.short.label')}</th>
                    <th class="one wide" scope="col" rowspan="3">${message(code: 'provider.label')}</th>
                    <th class="one wide" scope="col" rowspan="3">${message(code: 'subscription')}</th>
                    <th scope="col" rowspan="2" class="one wide la-smaller-table-head">${message(code: 'subscription.start.label')}</th>
                    <th class="one wide" scope="col" rowspan="3">${message(code: 'subscription.manualCancellation.label')}</th>
                    <th colspan="3" class="one wide center aligned">${message(code: 'subscription.offer.table.th')}</th>
                    <th scope="col" rowspan="3" class="one wide center aligned">${message(code: 'subscription.priceIncreaseInfo.labelBreak')}</th>

                    <th scope="col" rowspan="3" class="one wide center aligned">
                        <span class="la-popup-tooltip" data-content="${message(code: 'survey.label')}" data-position="top center">
                            <i class="${Icon.SURVEY} large"></i>
                        </span>
                    </th>
                    <th scope="col" rowspan="3" class="one wide center aligned">
                        <span class="la-popup-tooltip" data-content="${message(code: 'subscription.survey.evaluation.label')}" data-position="top center">
                            <i class="${Icon.ATTR.SURVEY_EVALUTAION} large"></i>
                        </span>
                    </th>
                    <th scope="col" rowspan="3" class="one wide center aligned">
                        <span class="la-popup-tooltip" data-content="${message(code: 'subscription.survey.cancellation.label')}" data-position="top center">
                            <i class="${Icon.ATTR.SURVEY_CANCELLATION} large"></i>
                        </span>
                    </th>
                    <th scope="col" rowspan="3" class="one wide center aligned">
                        <span class="la-popup-tooltip" data-content="${message(code: 'subscription.discountScale.label')}" data-position="top center">
                            <i class="${Icon.ATTR.SUBSCRIPTION_DISCOUNT_SCALE} large"></i>
                        </span>
                    </th>

                    <th class="la-smaller-table-head center aligned">Reminder</th>
                    <th colspan="3" class="la-smaller-table-head center aligned">Renewal</th>

                    <th scope="col" rowspan="3" class="one wide center aligned">
                        <span class="la-popup-tooltip"
                                data-content="${message(code: 'subscription.participantTransferWithSurvey.label')}"
                                data-position="top center">
                            <i class="large icons">
                                <i class="${Icon.SURVEY}"></i>
                                <i class="top right corner redo icon"></i>
                            </i>
                        </span>
                    </th>
                    <th scope="col" rowspan="3" class="one wide center aligned">
                        <span class="la-popup-tooltip"
                           data-content="${message(code: 'renewalEvaluation.exportRenewal')}"
                           data-position="top center">
                            <i class="${Icon.CMD.DOWNLOAD} large"></i>
                        </span>
                    </th>
                    <th scope="col" rowspan="3" class="center aligned"><i class="${Icon.SYM.OPTIONS} large"></i></th>
                </tr>
                <tr>
                    <th scope="col" class="one wide la-smaller-table-head">${message(code: 'subscription.offerRequested.table.th')}</th>
                    <th scope="col" rowspan="2" class="one wide center aligned one wide">
                        <a href="#" class="la-popup-tooltip" data-content="${message(code: 'subscriptionsManagement.documents')}" data-position="top center">
                            <i class="${Icon.DOCUMENT} large"></i>
                        </a>
                    </th>
                    <th class="one wide" scope="col" rowspan="2">${message(code: 'subscription.offerAccepted.table.th')}</th>
                    <th class="one wide" scope="col" class="la-smaller-table-head">${message(code: 'subscription.reminderSent.table.th')}</th>
                    <th scope="col" class="one wide la-smaller-table-head">${message(code: 'subscription.renewalSent.table.th')}</th>
                    <th scope="col" rowspan="2" class="one wide center aligned one wide">
                        <a href="#" class="la-popup-tooltip" data-content="${message(code: 'subscriptionsManagement.documents')}" data-position="top center">
                            <i class="${Icon.DOCUMENT} large"></i>
                        </a>
                    </th>
                    <th class="one wide" scope="col" rowspan="2" class="center aligned">
                        <i class="la-popup-tooltip exchange alternate icon large" data-content="<g:message code="default.change.label"/>"></i>
                    </th>
                </tr>
                <tr>
                    <th scope="col" rowspan="1" class="one wide la-smaller-table-head">${message(code: 'default.end.label')}</th>
                    <th scope="col" class="one wide la-smaller-table-head">${message(code: 'subscription.offerRequestedDate.table.th')}</th>
                    <th scope="col" class="one wide la-smaller-table-head">${message(code: 'subscription.reminderSentDate.table.th')}</th>
                    <th scope="col" class="one wide la-smaller-table-head">${message(code: 'subscription.renewalSentDate.table.th')}</th>
                </tr>
                </thead>
            <tbody>
                <g:each in="${calculatedSubList}" var="s">
                    <g:set var="surConfig"
                           value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s, true)}"/>
                    <g:set var="surveyClass" value=""/>
                    <g:if test="${surConfig}">
                        <g:set var="surveyClass"
                               value="${surConfig.surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED ? 'positive' : (surConfig.surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY] ? 'warning' : '')}"/>
                    </g:if>

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
    %{--                    <td>
                            <g:each in="${s.vendors}" var="vendor">
                                <g:link controller="vendor" action="show" id="${vendor.id}" target="_blank">
                                    ${fieldValue(bean: vendor, field: "name")}
                                    <g:if test="${vendor.sortname}">
                                        <br/> (${fieldValue(bean: vendor, field: "sortname")})
                                    </g:if>
                                </g:link>
                                <br/>
                            </g:each>
                        </td>--}%
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
                                                %{--                                                <g:set var="supportedMimeType"
                                                       value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <g:if test="${supportedMimeType}">
                                                    <a href="#documentPreview"
                                                       data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>--}%
                                                %{-- <g:if test="${docctx.getDocType()}">
                                                     (${docctx.getDocType().getI10n("value")})
                                                 </g:if>--}%
                                            </div>

                                            <div class="right aligned eight wide column la-column-left-lessPadding la-border-left">
                                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                        class="${Btn.MODERN.SIMPLE} tiny"
                                                        target="_blank">
                                                    <i class="${Icon.CMD.DOWNLOAD} small"></i>
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
                        <td class="${surveyClass}">
                            <g:if test="${surConfig}">
                                <g:link controller="survey" action="show" id="${surConfig.surveyInfo.id}"
                                        target="_blank">
                                    <g:formatDate formatName="default.date.format.notime"
                                                  date="${surConfig.surveyInfo.startDate}"/>
                                    <br/>
                                    <span class="la-secondHeaderRow"
                                          data-label="${message(code: 'default.endDate.label')}:">
                                        <g:formatDate formatName="default.date.format.notime"
                                                      date="${surConfig.surveyInfo.endDate}"/>
                                    </span>
                                </g:link>
                            </g:if>
                        </td>
                        <g:if test="${surConfig}">
                            <g:set var="finish"
                                   value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surConfig).size()}"/>
                            <g:set var="total"
                                   value="${SurveyOrg.findAllBySurveyConfig(surConfig).size()}"/>

                            <g:set var="finishProcess"
                                   value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
                            <td class="${finish == total ? 'positive' : ''}">
                                <g:if test="${finishProcess >= 0}">
                                    <g:link controller="survey" action="surveyEvaluation"
                                            id="${surConfig.surveyInfo.id}">
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
                        <g:if test="${surConfig && surConfig.surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED, RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]}">
                            <g:set var="countOrgsWithTermination" value="${surConfig.countOrgsWithTermination()}"/>
                        </g:if>

                        <td class="${countOrgsWithTermination > 0 && countOrgsWithTermination <= 10 ? 'warning' : (countOrgsWithTermination > 10 ? 'negative' : '')}">
                            <g:if test="${surConfig && countOrgsWithTermination >= 0}">
                                <g:link controller="survey" action="renewalEvaluation" id="${surConfig.surveyInfo.id}" target="_blank">
                                    ${countOrgsWithTermination}
                                </g:link>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${s.discountScales.size() > 0}">
                                <g:if test="${s.discountScale}">
                                    ${s.discountScale.name} : ${s.discountScale.discount}
                                    <g:if test="${s.discountScale.note}">
                                        <span data-position="top left" class="la-popup-tooltip" data-content="${s.discountScale.note}">
                                            <i class="${Icon.TOOLTIP.INFO} blue"></i>
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

                            <g:if test="${editable && surConfig && surConfig.surveyInfo.id.toString() == params.id}">
                                <button type="button" class="${Btn.MODERN.SIMPLE} tiny"
                                        data-ownerid="${s.id}"
                                        data-ownerclass="${s.class.name}"
                                        data-doctype="${RDStore.DOC_TYPE_RENEWAL.value}"
                                        data-ui="modal"
                                        data-href="#modalCreateDocumentSubTransferInfo">
                                    <i aria-hidden="true" class="${Icon.CMD.ADD} small"></i>
                                </button>
                            </g:if>
                            <%
                                Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: s, docType: RDStore.DOC_TYPE_RENEWAL, owner: contextService.getOrg()])
                                documentSet2 = documentSet2.sort { it.owner?.title }
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
                                                %{--                                                <g:set var="supportedMimeType"
                                                       value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <g:if test="${supportedMimeType}">
                                                    <a href="#documentPreview"
                                                       data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>--}%
                                                %{-- <g:if test="${docctx.getDocType()}">
                                                     (${docctx.getDocType().getI10n("value")})
                                                 </g:if>--}%
                                            </div>

                                            <div class="right aligned eight wide column la-column-left-lessPadding la-border-left">
                                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                        class="${Btn.MODERN.SIMPLE} tiny"
                                                        target="_blank">
                                                    <i class="${Icon.CMD.DOWNLOAD} small"></i>
                                                </g:link>
                                            </div>
                                        </div>
                                    </div>
                                </g:if>
                            </g:each>
                        </td>

                        <g:set var="countModificationToContactInformationAfterRenewalDoc" value="${surConfig ? surveyService.countModificationToContactInformationAfterRenewalDoc(s) : 0}"/>

                        <td class="${surConfig ? countModificationToContactInformationAfterRenewalDoc == 0 ? 'positive' : 'negative' : ''}">
                            <g:if test="${countModificationToContactInformationAfterRenewalDoc > 0}">
                                <g:link class="ui label triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                        params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION, id: surConfig.surveyInfo.id, surveyConfigID: surConfig.id, modalText: message(code: 'renewalEvaluation.exportRenewal')]">
                                    <i class="${Icon.CMD.DOWNLOAD}"></i> ${countModificationToContactInformationAfterRenewalDoc}
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:if test="${surConfig}">
                                    ${countModificationToContactInformationAfterRenewalDoc}
                                </g:if>
                            </g:else>
                        </td>

                        <td class="${s.participantTransferWithSurvey ? 'positive' : 'negative'}">
                            ${s.participantTransferWithSurvey ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        </td>
                        <td>
                            <g:if test="${surConfig && surConfig.surveyInfo.status in [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]}">
                                <g:link class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                    params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION,
                                             id: surConfig.surveyInfo.id, surveyConfigID: surConfig.id,
                                             modalText: 'Export: ('+ g.message(code: 'renewalEvaluation.exportRenewal')+'('+ g.message(code: 'subscription.referenceYear.label')+': '+ surConfig.subscription.referenceYear+')']">
                                    <i class="${Icon.CMD.DOWNLOAD}"></i>
                                </g:link>
                            </g:if>
                        </td>
                        <td>
                            <g:link controller="subscription" action="subTransfer" id="${s.id}"
                                    class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </g:link>
                        </td>
                    </tr>
                    </tbody>
                </g:each>
            </table>
        </div>
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

<g:if test="${editable}">
    <laser:render template="/templates/documents/modal"
                  model="${[newModalId: "modalCreateDocumentSubTransferInfo", owntp: 'subscription']}"/>


    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.callbacks.modal.onShow.modalCreateDocumentSubTransferInfo = function(trigger) {
            $('#modalCreateDocumentSubTransferInfo input[name=ownerid]').attr('value', $(trigger).attr('data-ownerid'))
            $('#modalCreateDocumentSubTransferInfo input[name=ownerclass]').attr('value', $(trigger).attr('data-ownerclass'))
            $('#modalCreateDocumentSubTransferInfo input[name=ownertp]').attr('value', $(trigger).attr('data-ownertp'))
            $('#modalCreateDocumentSubTransferInfo select[name=doctype]').dropdown('set selected', $(trigger).attr('data-doctype'))
        }
    </laser:script>

</g:if>
<!-- template: meta/subscriptionTransferInfo -->