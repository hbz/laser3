<!-- template: meta/subscriptionTransferInfo -->
<%@ page import="de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Subscription; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.Org; de.laser.CustomerTypeService; de.laser.Doc; de.laser.survey.SurveyOrg;" %>

<laser:serviceInjection />

<div class="ui yellow segment" id="subscriptionTransfer-content">
    <g:each in="${calculatedPreviousList}" var="s">
        <g:set var="editable" value="${s.isEditableBy(contextService.getUser())}" />%{-- TODO --}%

        <p class="ui header">
            ${s},
            ${message(code: 'subscription.referenceYear.export.label')}: ${s.referenceYear ?: 'ohne'}
        </p>

        <div class="content">
            <table class="ui compact monitor stackable celled sortable table la-table la-js-responsive-table">
                <thead>
                <tr>
                    <th scope="col" rowspan="3">${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}</th>
                    <th scope="col" rowspan="3">${message(code: 'subscription')}</th>
                    <th scope="col" rowspan="2" class="la-smaller-table-head">${message(code: 'default.startDate.label')}</th>
                    <th scope="col" rowspan="3">${message(code: 'subscription.manualCancellationDate.label')}</th>
                    <th colspan="3" class="la-smaller-table-head center aligned">${message(code: 'subscription.offer.table.th')}</th>
                    <th scope="col" rowspan="3" class="center aligned">${message(code: 'subscription.priceIncreaseInfo.label')}</th>

                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'survey.label')}" data-position="top center">
                            <i class="chart pie large icon"></i>
                        </a>
                    </th>
                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.survey.evaluation.label')}" data-position="top center">
                            <i class="comments large icon"></i>
                        </a>
                    </th>
                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.survey.cancellation.label')}" data-position="top center">
                            <i class="times circle large icon"></i>
                        </a>
                    </th>
                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.discountScale.label')}" data-position="top center">
                            <i class="percentage large icon"></i>
                        </a>
                    </th>

                    <th class="la-smaller-table-head center aligned">Reminder</th>
                    <th colspan="2" class="la-smaller-table-head center aligned">Renewal</th>

                    <th scope="col" rowspan="3" class="center aligned">
                        <g:link action="currentSubscriptionsTransfer" params="${params}" class="la-popup-tooltip la-delay " data-content="${message(code: 'subscription.participantTransferWithSurvey.label')}" data-position="top center">
                            <i class="large icons">
                                <i class="chart pie icon"></i>
                                <i class="top right corner redo icon"></i>
                            </i>
                        </g:link>
                    </th>
                </tr>
                <tr>
                    <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.offerRequested.table.th')}</th>
                    <th scope="col" rowspan="2" class="center aligned two wide">${message(code: 'subscriptionsManagement.documents')}</th>
                    <th scope="col" rowspan="2">${message(code: 'subscription.offerAccepted.table.th')}</th>
                    <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.reminderSent.table.th')}</th>
                    <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.renewalSent.table.th')}</th>
                    <th scope="col" rowspan="2" class="center aligned two wide">${message(code: 'subscriptionsManagement.documents')}</th>
                </tr>
                <tr>
                    <th scope="col" rowspan="1" class="la-smaller-table-head">${message(code: 'default.endDate.label')}</th>
                    <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.offerRequestedDate.table.th')}</th>
                    <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.reminderSentDate.table.th')}</th>
                    <th scope="col" class="la-smaller-table-head">${message(code: 'subscription.renewalSentDate.table.th')}</th>
                </tr>
                </thead>
                <tbody>
                    <tr>
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
                            <g:each in="${s.agencies}" var="org">
                                <g:link controller="organisation" action="show" id="${org.id}" target="_blank">
                                    ${fieldValue(bean: org, field: "name")}
                                    <g:if test="${org.sortname}">
                                        <br/> (${fieldValue(bean: org, field: "sortname")})
                                    </g:if> (${message(code: 'default.agency.label')})
                                </g:link>
                                <br/>
                            </g:each>
                        </td>
                        <td>
                            <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}" target="_blank">
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
                            <g:formatDate format="default.date.format.notime" date="${s.offerRequestedDate}"/>
                        </td>
                        <td>
                            <%
                                Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType', [subscription: s, docType: RDStore.DOC_TYPE_OFFER])
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
                                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue tiny button la-modern-button la-js-dont-hide-button" target="_blank">
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

                        <g:set var="surveyConfig" value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(s, true)}"/>
                        <g:set var="surveyClass" value=""/>
                        <g:if test="${surveyConfig}">
                            <g:set var="surveyClass" value="${surveyConfig.surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED ? 'positive' : (surveyConfig.surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY] ? 'warning' : '')}"/>
                        </g:if>
                        <td class="${surveyClass}">
                            <g:if test="${surveyConfig}">
                                <g:link controller="survey" action="show" id="${surveyConfig.surveyInfo.id}" target="_blank">
                                    <g:formatDate formatName="default.date.format.notime" date="${surveyConfig.surveyInfo.startDate}"/>
                                    <br/>
                                    <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label')}:">
                                        <g:formatDate formatName="default.date.format.notime" date="${surveyConfig.surveyInfo.endDate}"/>
                                    </span>
                                </g:link>
                            </g:if>
                        </td>

                        <g:if test="${surveyConfig}">
                            <g:set var="finish" value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()}"/>
                            <g:set var="total" value="${SurveyOrg.findAllBySurveyConfig(surveyConfig).size()}"/>

                            <g:set var="finishProcess" value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
                            <td class="${finish == total ? 'positive' : ''}">
                                <g:if test="${finishProcess >= 0}">
                                    <g:link controller="survey" action="surveyEvaluation" id="${surveyConfig.surveyInfo.id}" target="_blank">
                                        <g:formatNumber number="${finishProcess}" type="number" maxFractionDigits="2" minFractionDigits="2"/>%
                                    </g:link>
                                </g:if>
                            </td>
                        </g:if>
                        <g:else>
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
                            <g:formatDate format="default.date.format.notime" date="${s.reminderSentDate}"/>
                        </td>

                        <td class="${s.renewalSent ? 'positive' : 'negative'}">
                            ${s.renewalSent ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                            <br/>
                            <g:formatDate format="default.date.format.notime" date="${s.renewalSentDate}"/>
                        </td>
                        <td>
                            <%
                                Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType', [subscription: s, docType: RDStore.DOC_TYPE_RENEWAL])
                                documentSet2 = documentSet2.sort { it.owner?.title }
                            %>
                            <g:each in="${documentSet2}" var="docctx">
                                <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                                    <div class="ui small feed content la-js-dont-hide-this-card">
                                        <div class="ui grid summary">
                                            <div class="eleven wide column la-column-right-lessPadding">
                                                <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                                                <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <g:if test="${supportedMimeType}">
                                                    <a href="#documentPreview" data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>
                                            %{-- <g:if test="${docctx.getDocType()}">
                                                 (${docctx.getDocType().getI10n("value")})
                                             </g:if>--}%
                                            </div>

                                            <div class="right aligned five wide column la-column-left-lessPadding la-border-left">
                                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue tiny button la-modern-button la-js-dont-hide-button" target="_blank">
                                                    <i class="download small icon"></i>
                                                </g:link>
                                            </div>
                                        </div>
                                    </div>
                                </g:if>
                            </g:each>
                        </td>

                        <td class="${s.participantTransferWithSurvey ? 'positive' : 'negative'}">
                            ${s.participantTransferWithSurvey ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        </td>
                    </tr>
                </tbody>
            </table>

        </div>
    </g:each>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    docs.init('#subscriptionTransfer-content');
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