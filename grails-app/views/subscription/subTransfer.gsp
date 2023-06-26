<%@ page import="de.laser.Doc; de.laser.DocContext; de.laser.survey.SurveyConfig; de.laser.Subscription; de.laser.storage.RDStore; de.laser.survey.SurveyOrg" %>
<laser:htmlStart message="subscription.details.subTransfer.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}">
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

<ui:objectStatus object="${subscription}" status="${subscription.status}"/>
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
                            <dd><ui:xEditable owner="${subscription}" field="offerRequestedDate" type="date"
                                              validation="datesCheck"/></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.offerNote.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="offerNote"
                                              type="textarea"/></dd>
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
                            </g:if></dd>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.survey.cancellation.label')}</dt>
                            <dd>
                                <g:set var="countOrgsWithTermination" value="${0}"/>
                                <g:if test="${surveyConfig}">
                                    <g:set var="countOrgsWithTermination"
                                           value="${surveyConfig.countOrgsWithTermination()}"/>
                                </g:if>
                                <g:if test="${surveyConfig && countOrgsWithTermination >= 0}">
                                    <g:link controller="survey" action="renewalEvaluation"
                                            id="${surveyConfig.surveyInfo.id}">
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
                                            <span data-position="top left" class="la-popup-tooltip la-delay"
                                                  data-content="${subscription.discountScale.note}">
                                                <i class="info circle icon blue"></i>
                                            </span>
                                        </g:if>
                                    </g:if>
                                </a>
                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('body #discountScale').editable('destroy').editable({
                                        tpl: '<select class="ui dropdown"></select>'
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
                <ui:card message="subscription.offerNote.label" class="documents la-js-hideable ${css_class}"
                         href="#modalCreateDocument" editable="${editable || editable2}">
                    <%
                        Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType', [subscription: subscription, docType: RDStore.DOC_TYPE_OFFER])
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
                                                              model="[s: subscription, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
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
                                                        params='[instanceId: "${subscription.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
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
                </ui:card>

                <ui:card message="subscription.renewalFile.label" class="documents la-js-hideable ${css_class}"
                         href="#modalCreateDocument" editable="${editable || editable2}">
                    <%
                        Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType', [subscription: subscription, docType: RDStore.DOC_TYPE_RENEWAL])
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
                                                              model="[s: subscription, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
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
                                                        params='[instanceId: "${subscription.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
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
                </ui:card>

                <laser:script file="${this.getGroovyPageFileName()}">
                    docs.init('#container-documents');
                </laser:script>
            </div>
        </div>
    </aside><!-- .four -->
</div><!-- .grid -->

<div id="magicArea"></div>


<laser:htmlEnd/>
