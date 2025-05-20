<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.Doc; de.laser.DocContext; de.laser.survey.SurveyConfig; de.laser.Subscription; de.laser.storage.RDStore; de.laser.survey.SurveyOrg" %>
<laser:htmlStart message="subscription.details.subTransfer.label" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
    <laser:render template="iconSubscriptionIsChild"/>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<g:if test="${editable}">
    <ui:auditButton class="la-auditButton-header" auditable="[subscription, 'name']" auditConfigs="${auditConfigs}"
                    withoutOptions="true"/>
</g:if>
<ui:anualRings object="${subscription}" controller="subscription" action="subTransfer" navNext="${navNextSubscription}"
               navPrev="${navPrevSubscription}"/>

<laser:render template="nav"/>

<ui:objectStatus object="${subscription}" />
<laser:render template="message"/>

<ui:messages data="${flash}"/>

<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="eleven wide column">
        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.offerRequested.label')}</dt>
                            <dd><ui:xEditableBoolean owner="${subscription}" field="offerRequested"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.offerRequestedDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="offerRequestedDate" type="date"/></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.offerNote.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="offerNote" type="textarea"
                                              validation="maxlength" maxlength="255"/></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.offerAccepted.label')}</dt>
                            <dd><ui:xEditableBoolean owner="${subscription}" field="offerAccepted"/></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.priceIncreaseInfo.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="priceIncreaseInfo"
                                              type="textarea"/></dd>
                        </dl>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.renewalSent.label')}</dt>
                            <dd><ui:xEditableBoolean owner="${subscription}" field="renewalSent"/></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.renewalSentDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="renewalSentDate" type="date"
                                              validation="datesCheck"/></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">Renewal ${message(code: 'default.change.label')}</dt>
                            <dd>
                                <g:set var="surveyUseForTransfer" value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)}"/>
                                <g:set var="countModificationToContactInformationAfterRenewalDoc" value="${surveyUseForTransfer ? surveyService.countModificationToContactInformationAfterRenewalDoc(subscription) : 0}"/>

                                    <g:if test="${countModificationToContactInformationAfterRenewalDoc > 0}">
                                        <g:link class="ui label triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                                params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION, id: surveyUseForTransfer.surveyInfo.id, surveyConfigID: surveyUseForTransfer.id]">
                                            <i class="${Icon.CMD.DOWNLOAD}"></i> ${countModificationToContactInformationAfterRenewalDoc}
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <g:if test="${surveyUseForTransfer}">
                                            ${countModificationToContactInformationAfterRenewalDoc}
                                        </g:if>
                                    </g:else>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.participantTransferWithSurvey.label')}</dt>
                            <dd><ui:xEditableBoolean owner="${subscription}"
                                                     field="participantTransferWithSurvey"/></dd>
                        </dl>
                    </div>
                </div>
            </div>

            <g:set var="surveyConfig"
                   value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)}"/>

            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'survey.label')}</dt>
                            <dd><g:if test="${surveyConfig}">
                                <g:link controller="survey" action="show" id="${surveyConfig.surveyInfo.id}">
                                    <g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyConfig.surveyInfo.startDate}"/><br/>
                                    <span class="la-secondHeaderRow"
                                          data-label="${message(code: 'default.endDate.label')}:"><g:formatDate
                                            formatName="default.date.format.notime"
                                            date="${surveyConfig.surveyInfo.endDate}"/></span>
                                </g:link>
                            </g:if></dd>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.survey.evaluation.label')}</dt>
                            <dd><g:if test="${surveyConfig}">
                                <g:set var="finish" value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()}"/>
                                <g:set var="total" value="${SurveyOrg.findAllBySurveyConfig(surveyConfig).size()}"/>

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
                            </g:if></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.survey.cancellation.label')}</dt>
                            <dd>
                                <g:set var="countOrgsWithTermination" value="${0}"/>
                                <g:if test="${surveyConfig}">
                                    <g:set var="countOrgsWithTermination" value="${surveyConfig.countOrgsWithTermination()}"/>
                                </g:if>
                                <g:if test="${surveyConfig && countOrgsWithTermination >= 0}">
                                    <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}">
                                        ${countOrgsWithTermination}
                                    </g:link>
                                </g:if>
                            </dd>
                        </dl>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.discountScale.label')}</dt>
                            <dd>

                                <a href="#" id="discountScale" class="xEditableManyToOne editable editable-click" data-onblur="ignore" data-pk="${subscription.class.name}:${subscription.id}" data-confirm-term-how="ok"
                                   data-type="select" data-name="discountScale" data-source="/ajaxJson/getSubscriptionDiscountScaleList?sub=${subscription.id}" data-url="/ajax/editableSetValue" data-emptytext="${message(code:'default.button.edit.label')}">

                                    <g:if test="${subscription.discountScale}">
                                        ${subscription.discountScale.name} : ${subscription.discountScale.discount}
                                        <g:if test="${subscription.discountScale.note}">
                                            <span data-position="top left" class="la-popup-tooltip"
                                                  data-content="${subscription.discountScale.note}">
                                                <i class="${Icon.TOOLTIP.INFO} blue"></i>
                                            </span>
                                        </g:if>
                                    </g:if>
                                </a>
                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('body #discountScale').editable('destroy').editable({
                                        tpl: '<select class="ui dropdown clearable"></select>'
                                        }).on('shown', function() {
                                        r2d2.initDynamicUiStuff('body');

                                        $('.ui.dropdown')
                                            .dropdown({
                                            clearable: true
                                        })
                                        ;
                                        }).on('hidden', function() {
                                        });
                                </laser:script>
                            </dd>
                        </dl>
                    </div>
                </div>
            </div>

        </div>
    </div><!-- .eleven -->
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">
            <div id="container-documents">
                <ui:card message="subscription.offerNote.label" class="documents ${css_class}"
                         href="#modalCreateDocument" editable="${editable || editable2}">
                    <%
                        Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: subscription, docType: RDStore.DOC_TYPE_OFFER, owner: contextService.getOrg()])
                        documentSet = documentSet.sort { it.owner?.title }
                    %>
                    <g:each in="${documentSet}" var="docctx">
                        <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                            <div class="ui small feed content">
                                <div class="ui grid summary">
                                    <div class="eight wide column la-column-right-lessPadding">
                                        <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                        <g:set var="supportedMimeType"
                                               value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                        <g:if test="${supportedMimeType}">
                                            <a href="#documentPreview"
                                               data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                        </g:if>
                                        <g:else>
                                            ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                        </g:else>
                                        <g:if test="${docctx.getDocType()}">
                                            (${docctx.getDocType().getI10n("value")})
                                        </g:if>


                                    </div>

                                    <div class="right aligned eight wide column la-column-left-lessPadding">

                                        <g:if test="${!(editable)}">
                                        <%-- 1 --%>
                                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                    class="${Btn.MODERN.SIMPLE}"
                                                    target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                                        </g:if>
                                        <g:else>
                                            <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id}">
                                            <%-- 1 --%>
                                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                        class="${Btn.MODERN.SIMPLE}"
                                                        target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>

                                            <%-- 2 --%>
                                                <laser:render template="/templates/documents/modal"
                                                              model="[ownobj: subscription, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                <button type="button" class="${Btn.MODERN.SIMPLE}"
                                                        data-ui="modal"
                                                        data-href="#modalEditDocument_${docctx.id}"
                                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                    <i class="${Icon.CMD.EDIT}"></i>
                                                </button>
                                            </g:if>

                                        <%-- 4 --%>
                                            <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id && !docctx.isShared}">
                                                <g:link controller="document"
                                                        action="deleteDocument"
                                                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                        data-confirm-term-how="delete"
                                                        params='[instanceId: "${subscription.id}", deleteId: "${docctx.id}", redirectController:"${ajaxCallController ?: controllerName}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                    <icon:placeholder /><%-- Hidden Fake Button --%>
                                                </div>
                                            </g:else>
                                        </g:else>%{-- (editable || editable2) --}%
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </g:each>
                </ui:card>

                <ui:card message="subscription.renewalFile.label" class="documents ${css_class}"
                         href="#modalCreateDocument" editable="${editable || editable2}">
                    <%
                        Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: subscription, docType: RDStore.DOC_TYPE_RENEWAL, owner: contextService.getOrg()])
                        documentSet2 = documentSet2.sort { it.owner?.title }
                    %>
                    <g:each in="${documentSet2}" var="docctx">
                        <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                            <div class="ui small feed content">
                                <div class="ui grid summary">
                                    <div class="eight wide column la-column-right-lessPadding">
                                        <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                        <g:set var="supportedMimeType"
                                               value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                        <g:if test="${supportedMimeType}">
                                            <a href="#documentPreview"
                                               data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                        </g:if>
                                        <g:else>
                                            ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                        </g:else>
                                        <g:if test="${docctx.getDocType()}">
                                            (${docctx.getDocType().getI10n("value")})
                                        </g:if>
                                    </div>

                                    <div class="right aligned eight wide column la-column-left-lessPadding">

                                        <g:if test="${!(editable)}">
                                        <%-- 1 --%>
                                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                    class="${Btn.MODERN.SIMPLE}"
                                                    target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                                        </g:if>
                                        <g:else>
                                            <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id}">
                                            <%-- 1 --%>
                                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                                        class="${Btn.MODERN.SIMPLE}"
                                                        target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>

                                            <%-- 2 --%>
                                                <laser:render template="/templates/documents/modal"
                                                              model="[ownobj: subscription, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
                                                <button type="button" class="${Btn.MODERN.SIMPLE}"
                                                        data-ui="modal"
                                                        data-href="#modalEditDocument_${docctx.id}"
                                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                                    <i class="${Icon.CMD.EDIT}"></i>
                                                </button>
                                            </g:if>

                                        <%-- 4 --%>
                                            <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id && !docctx.isShared}">
                                                <g:link controller="document"
                                                        action="deleteDocument"
                                                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                                        data-confirm-term-how="delete"
                                                        params='[instanceId: "${subscription.id}", deleteId: "${docctx.id}", redirectController:"${ajaxCallController ?: controllerName}", redirectAction: "${ajaxCallAction ?: actionName}"]'
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                    <icon:placeholder /><%-- Hidden Fake Button --%>
                                                </div>
                                            </g:else>
                                        </g:else>%{-- (editable || editable2) --}%
                                    </div>
                                </div>
                            </div>
                        </g:if>
                    </g:each>
                </ui:card>

                <laser:script file="${this.getGroovyPageFileName()}">
                    docs.init('#container-documents');
                </laser:script>
            </div>
        </div>
    </aside><!-- .four -->
</div><!-- .grid -->

<div id="magicArea"></div>

<g:render template="/clickMe/export/js"/>
<laser:htmlEnd/>
