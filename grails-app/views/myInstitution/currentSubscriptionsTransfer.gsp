<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.helper.Params; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Subscription; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.Org; de.laser.CustomerTypeService; de.laser.Doc; de.laser.survey.SurveyOrg;" %>

<laser:htmlStart message="menu.my.currentSubscriptionsTransfer" />

<ui:breadcrumbs>
    <ui:crumb message="menu.my.currentSubscriptionsTransfer" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.SUBSCRIPTIONS_TRANSFER]"/>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="menu.my.currentSubscriptionsTransfer" total="${num_sub_rows}" floated="true"/>

<g:if test="${params.referenceYears}">
    <div class="ui large label la-annual-rings"><g:link action="currentSubscriptionsTransfer"
                                                        params="${[referenceYears: (Integer.parseInt(params.referenceYears) - 1).toString()]}"
                                                        class="item"><i class="${Icon.LNK.PREV}"
                                                                        aria-hidden="true"></i></g:link><span
            class="la-annual-rings-text">${params.referenceYears}</span><g:link action="currentSubscriptionsTransfer"
                                                                                params="${[referenceYears: (Integer.parseInt(params.referenceYears) + 1).toString()]}"
                                                                                class="item"><i class="${Icon.LNK.NEXT}"
                                                                                                aria-hidden="true"></i></g:link>
    </div>
</g:if>

<ui:messages data="${flash}"/>

<ui:filter>
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <input type="hidden" name="referenceYears" value="${params.referenceYears}"/>

        <div class="four fields">
            <% /* 1-1 */ %>
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                    <span data-position="right center" class="la-popup-tooltip"
                          data-content="${message(code: 'default.search.tooltip.subscription')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>
            <% /* 1-2 */ %>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" class="la-popup-tooltip"
                          data-content="${message(code: 'default.search.tooltip.subscription.identifier')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="identifier" name="identifier"
                           placeholder="${message(code: 'default.search.identifier.ph')}"
                           value="${params.identifier}"/>
                </div>
            </div>
            <% /* 1-3 */ %>
            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn"
                               placeholder="filter.placeholder" value="${validOn}"/>
            </div>
            <% /* 1-4 */ %>
            <div class="field">
                <label>${message(code: 'menu.my.providers')}</label>
                <g:select class="ui dropdown clearable search" name="provider"
                          from="${providers}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.provider}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">

            <% /* 2-1 and 2-2 */ %>
            <laser:render template="/templates/properties/genericFilter"
                          model="[propList: propList, label: message(code: 'subscription.property.search')]"/>
            <% /* 2-3 */ %>
            <div class="field">
                <label for="form"><g:message code="subscription.form.label"/></label>
                <select id="form" name="form" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}" var="form">
                        <option <%=Params.getLongList(params, 'form').contains(form.id) ? 'selected="selected"' : ''%>
                                value="${form.id}">
                            ${form.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 2-4 */ %>
            <div class="field">
                <label for="resource"><g:message code="subscription.resource.label"/></label>
                <select id="resource" name="resource" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                            var="resource">
                        <option <%=Params.getLongList(params, 'resource').contains(resource.id) ? 'selected="selected"' : ''%>
                                value="${resource.id}">
                            ${resource.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>

        <div class="four fields">
            <% /* 3-1 */ %>
            <div class="field">
                <label for="subKinds">${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" var="subKind">
                        <option <%=Params.getLongList(params, 'subKinds').contains(subKind.id) ? 'selected="selected"' : ''%>
                                value="${subKind.id}">
                            ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>

            </div>
            <% /* 3-2 */ %>
            <div class="field">
                <label>${message(code: 'subscription.isPublicForApi.label')}</label>
                <ui:select class="ui fluid dropdown" name="isPublicForApi"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.isPublicForApi}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <% /* 3-3 */ %>
            <div class="field">
                <label>${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.hasPerpetualAccess}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <% /* 3-4 */ %>
            <div class="field">
                <label>${message(code: 'subscription.hasPublishComponent.label')}</label>
                <ui:select class="ui fluid dropdown" name="hasPublishComponent"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.hasPublishComponent}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">
            <div class="field">
                <label>${message(code: 'subscription.holdingSelection.label')}</label>
                <select id="holdingSelection" name="holdingSelection" multiple=""
                        class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}"
                            var="holdingSelection">
                        <option <%=Params.getLongList(params, 'holdingSelection').contains(holdingSelection.id) ? 'selected="selected"' : ''%>
                                value="${holdingSelection.id}">
                            ${holdingSelection.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                            <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox"
                                   <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox"
                                   <g:if test="${params.subRunTime}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>
            <% /* 4-2 */ %>
        <%-- TODO [ticket=2276] provisoric, name check is in order to prevent id mismatch --%>
            <g:if test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                <div class="field">
                    <fieldset id="subscritionType">
                        <label>${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>

                        <div class="inline fields la-filter-inline">
                            <%
                                List subTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE) - RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE
                            %>
                            <g:each in="${subTypes}" var="subType">
                                <div class="inline field">
                                    <div class="ui checkbox">
                                        <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                        <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox"
                                               value="${subType.id}"
                                            <g:if test="${Params.getLongList(params, 'subTypes').contains(subType.id)}">checked=""</g:if>
                                               tabindex="0">
                                    </div>
                                </div>
                            </g:each>
                        </div>
                    </fieldset>
                </div>
            </g:if>
            <g:else>
                <div class="field"></div>
            </g:else>

            <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
                <div class="field">
                    <fieldset>
                        <legend id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</legend>

                        <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                                  optionKey="${{ Org.class.name + ':' + it.id }}"
                                  optionValue="${{ it.getName() }}"
                                  name="consortia"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"
                                  value="${params.consortia}"/>
                    </fieldset>
                </div>
            </g:if>
            <div class="field la-field-right-aligned">
                <a href="${createLink(controller: controllerName, action: actionName, params: [id: params.id, resetFilter: true, tab: params.tab])}"
                   class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>

    </g:form>
</ui:filter>

<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
    %{--<div class="ui very long scrolling container">
        <table class="ui stuck unstackable celled sortable table">--}%
        <div class="">
            <table class="ui compact monitor stackable celled sortable table la-table la-js-responsive-table">
                <thead>
                <tr>
                   <th scope="col" rowspan="3" class="center aligned">
                        ${message(code: 'sidewide.number')}
                    </th>
                    <g:sortableColumn scope="col" rowspan="3" params="${params}" property="provider"
                                      title="${message(code: 'provider.label')}"/>

                    <g:sortableColumn scope="col" rowspan="3" params="${params}" property="vendor"
                                      title="${message(code: 'vendor.label')}"/>

                    <g:sortableColumn scope="col" rowspan="3" params="${params}" property="name"
                                      title="${message(code: 'subscription')}"/>

                    <g:sortableColumn scope="col" rowspan="2" class="la-smaller-table-head" params="${params}"
                                      property="startDate"
                                      title="${message(code: 'default.startDate.label.shy')}"/>

                    <g:sortableColumn scope="col" rowspan="3" params="${params}" property="manualCancellationDate"
                                      title="${message(code: 'subscription.manualCancellationDate.label.shy')}"/>

                    <th colspan="3" class="la-smaller-table-head center aligned">
                        ${message(code: 'subscription.offer.table.th')}
                    </th>

                    <th scope="col" rowspan="3" class="center aligned">
                        ${message(code: 'subscription.priceIncreaseInfo.label')}
                    </th>

                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip" data-content="${message(code: 'survey.label')}" data-position="top center">
                            <i class="${Icon.SURVEY} large"></i>
                        </a>
                    </th>

                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip" data-content="${message(code: 'subscription.survey.evaluation.label')}" data-position="top center">
                            <i class="${Icon.ATTR.SURVEY_EVALUTAION} large"></i>
                        </a>
                    </th>

                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip" data-content="${message(code: 'subscription.survey.cancellation.label')}" data-position="top center">
                            <i class="${Icon.ATTR.SURVEY_CANCELLATION} large"></i>
                        </a>
                    </th>

                    <th scope="col" rowspan="3" class="center aligned">
                        <a href="#" class="la-popup-tooltip" data-content="${message(code: 'subscription.discountScale.label')}" data-position="top center">
                            <i class="${Icon.ATTR.SUBSCRIPTION_DISCOUNT_SCALE} large"></i>
                        </a>
                    </th> %{--Discount Scale--}%

                    <th class="la-smaller-table-head center aligned">
                        Reminder
                    </th>

                    <th colspan="3" class="la-smaller-table-head center aligned">
                        Renewal
                    </th>

                    <th scope="col" rowspan="3" class="center aligned sortable ${params.sort == 'participantTransferWithSurvey' ? ('sorted '+(params.order == 'asc' ? 'desc' : 'asc')) : ''}">
                        <g:link action="currentSubscriptionsTransfer" params="${params+[sort: 'participantTransferWithSurvey', order: params.order == 'asc' ? 'desc' : 'asc']}" class="la-popup-tooltip" data-content="${message(code: 'subscription.participantTransferWithSurvey.label')}" data-position="top center">
                            <i class="large icons">
                                <i class="${Icon.SURVEY}"></i>
                                <i class="top right corner redo icon"></i>
                            </i>
                        </g:link>
                    </th>
                </tr>
                <tr>
                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                      property="offerRequested"
                                      title="${message(code: 'subscription.offerRequested.table.th')}"/>

                    <th scope="col" rowspan="2" class="center aligned two wide">
                        ${message(code: 'subscriptionsManagement.documents')}
                    </th>

                    <g:sortableColumn scope="col" rowspan="2" params="${params}" property="offerAccepted"
                                      title="${message(code: 'subscription.offerAccepted.table.th')}"/>%{--Accepted--}%

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                      property="reminderSent"
                                      title="${message(code: 'subscription.reminderSent.table.th')}"/>

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                      property="renewalSent"
                                      title="${message(code: 'subscription.renewalSent.table.th')}"/>
                    <th scope="col" rowspan="2" class="center aligned two wide">
                        ${message(code: 'subscriptionsManagement.documents')}
                    </th>%{-- Documents--}%
                    <th scope="col" rowspan="2" class="center aligned two wide">
                      <g:message code="default.change.label"/>
                    </th>
                </tr>
                <tr>
                    <g:sortableColumn scope="col" rowspan="1" class="la-smaller-table-head" params="${params}"
                                      property="endDate"
                                      title="${message(code: 'default.endDate.label.shy')}"/>
                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                      property="offerRequestedDate"
                                      title="${message(code: 'subscription.offerRequestedDate.table.th')}"/>

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                      property="reminderSentDate"
                                      title="${message(code: 'subscription.reminderSentDate.table.th')}"/>

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                      property="renewalSentDate"
                                      title="${message(code: 'subscription.renewalSentDate.table.th')}"/>
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
                            <g:each in="${s.providers}" var="provider">
                                <g:link controller="provider" action="show"
                                        id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                                    <g:if test="${provider.sortname}">
                                        <br/>
                                        (${fieldValue(bean: provider, field: "sortname")})
                                    </g:if>
                                </g:link><br/>
                            </g:each>
                        </td>
                        <td>
                            <g:each in="${s.vendors}" var="vendor">
                                <g:link controller="vendor" action="show" id="${vendor.id}">
                                    ${fieldValue(bean: vendor, field: "name")}
                                    <g:if test="${vendor.sortname}">
                                        <br/>
                                        (${fieldValue(bean: vendor, field: "sortname")})
                                    </g:if> (${message(code: 'vendor.label')})
                                </g:link><br/>
                            </g:each>
                        </td>
                        <td>
                            <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                                <g:if test="${s.name}">
                                    ${s.name}
                                </g:if>
                                <g:else>
                                    -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                                </g:else>
                            </g:link>
                        </td>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                            <span class="la-secondHeaderRow"
                                  data-label="${message(code: 'default.endDate.label.shy')}:"><g:formatDate
                                    formatName="default.date.format.notime" date="${s.endDate}"/></span>
                        </td>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.manualCancellationDate}"/>
                        </td>
                        <td class="${s.offerRequested ? 'positive' : 'negative'}">
                            <ui:xEditableBoolean owner="${s}" field="offerRequested"/>
                            <br/>
                            <ui:xEditable owner="${s}" field="offerRequestedDate" type="date"/>
                        </td>
                        <td>
                            <g:if test="${editable}">
                                <button type="button" class="${Btn.MODERN.SIMPLE} tiny"
                                        data-ownerid="${s.id}"
                                        data-ownerclass="${s.class.name}"
                                        data-doctype="${RDStore.DOC_TYPE_OFFER.value}"
                                        data-ui="modal"
                                        data-href="#modalCreateDocument">
                                    <i aria-hidden="true" class="${Icon.CMD.ADD} small"></i>
                                </button>
                            </g:if>

                            <%
                                Set<DocContext> documentSet = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: s, docType: RDStore.DOC_TYPE_OFFER, owner: contextService.getOrg()])
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
                                                       data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>
                                               %{-- <g:if test="${docctx.getDocType()}">
                                                    (${docctx.getDocType().getI10n("value")})
                                                </g:if>--}%

                                            </div>

                                            <div class="right aligned five wide column la-column-left-lessPadding la-border-left">

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
                                                                      model="[ownobj: s, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
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
                                                                params='[instanceId: "${s.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
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
                            <div class="ui small feed content">
                                <div class="ui grid summary">
                                    <div class="sixteen wide column">
                                        <ui:xEditable owner="${s}" field="offerNote" validation="maxlength" maxlength="255"/>
                                    </div>
                                </div>
                            </div>
                        </td>
                        <td class="${s.offerAccepted ? 'positive' : 'negative'}">
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
                                          data-label="${message(code: 'default.endDate.label.shy')}:"><g:formatDate
                                            formatName="default.date.format.notime"
                                            date="${surveyConfig.surveyInfo.endDate}"/></span>
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
                                <g:link controller="survey" action="renewalEvaluation"
                                        id="${surveyConfig.surveyInfo.id}">
                                    ${countOrgsWithTermination}
                                </g:link>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${s.discountScales.size() > 0}">
                                <a href="#" id="discountScale" class="xEditableManyToOne editable editable-click"
                                   data-onblur="ignore" data-pk="${s.class.name}:${s.id}" data-confirm-term-how="ok"
                                   data-type="select" data-name="discountScale"
                                   data-source="/ajaxJson/getSubscriptionDiscountScaleList?sub=${s.id}"
                                   data-url="/ajax/editableSetValue"
                                   data-emptytext="${message(code: 'default.button.edit.label')}">

                                    <g:if test="${s.discountScale}">
                                        ${s.discountScale.name} : ${s.discountScale.discount}
                                        <g:if test="${s.discountScale.note}">
                                            <span data-position="top left" class="la-popup-tooltip"
                                                  data-content="${s.discountScale.note}">
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
                            </g:if>

                        </td>

                        <td class="${s.reminderSent ? 'positive' : 'negative'}">
                            <ui:xEditableBoolean owner="${s}" field="reminderSent"/>
                            <br/>
                            <ui:xEditable owner="${s}" field="reminderSentDate" type="date"/>
                            <br/>
                            <g:if test="${surveyConfig}">
                                <g:set var="notfinish"
                                       value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNull(surveyConfig).size()}"/>
                                <g:link controller="survey" action="surveyEvaluation"
                                        id="${surveyConfig.surveyInfo.id}" params="[tab: 'participantsViewAllNotFinish']">
                                    ${notfinish}
                                </g:link>
                            </g:if>
                        </td>

                        <td class="${s.renewalSent ? 'positive' : 'negative'}">
                            <ui:xEditableBoolean owner="${s}" field="renewalSent"/>
                            <br/>
                            <ui:xEditable owner="${s}" field="renewalSentDate" type="date"/>
                        </td>
                        <td>
                            <g:if test="${editable}">
                                <button type="button" class="${Btn.MODERN.SIMPLE} tiny"
                                        data-ownerid="${s.id}"
                                        data-ownerclass="${s.class.name}"
                                        data-doctype="${RDStore.DOC_TYPE_RENEWAL.value}"
                                        data-ui="modal"
                                        data-href="#modalCreateDocument">
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
                                        <div class="ui grid summary">
                                            <div class="eleven wide column la-column-right-lessPadding">
                                                <ui:documentIcon doc="${docctx.owner}" showText="false"
                                                                 showTooltip="true"/>
                                                <g:set var="supportedMimeType"
                                                       value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                                                <g:if test="${supportedMimeType}">
                                                    <a href="#documentPreview"
                                                       data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}</a>
                                                </g:if>
                                                <g:else>
                                                    ${docctx.owner.title ?: docctx.owner.filename ?: message(code: 'template.documents.missing')}
                                                </g:else>
                                               %{-- <g:if test="${docctx.getDocType()}">
                                                    (${docctx.getDocType().getI10n("value")})
                                                </g:if>--}%
                                            </div>

                                            <div class="right aligned five wide column la-column-left-lessPadding la-border-left">

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
                                                                      model="[ownobj: s, owntp: 'subscription', docctx: docctx, doc: docctx.owner]"/>
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
                                                                params='[instanceId: "${s.id}", deleteId: "${docctx.id}", redirectAction: "${ajaxCallAction ?: actionName}"]'
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

                        <g:set var="countModificationToContactInformationAfterRenewalDoc" value="${surveyConfig ? surveyService.countModificationToContactInformationAfterRenewalDoc(s) : 0}"/>

                        <td class="${surveyConfig ? countModificationToContactInformationAfterRenewalDoc == 0 ? 'positive' : 'negative' : ''}">
                            <g:if test="${countModificationToContactInformationAfterRenewalDoc > 0}">
                                <g:link class="ui label triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                        params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION, id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id, modalText: message(code: 'renewalEvaluation.exportRenewal')]">
                                    <i class="${Icon.CMD.DOWNLOAD}"></i> ${countModificationToContactInformationAfterRenewalDoc}
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:if test="${surveyConfig}">
                                    ${countModificationToContactInformationAfterRenewalDoc}
                                </g:if>
                            </g:else>
                        </td>

                        <td class="${s.participantTransferWithSurvey ? 'positive' : 'negative'}">
                            <ui:xEditableBoolean owner="${s}" field="participantTransferWithSurvey"/>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
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
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${num_sub_rows}"/>
</g:if>

<g:if test="${editable}">
    <laser:render template="/templates/documents/modal"
                  model="${[newModalId: "modalCreateDocument", owntp: 'subscription']}"/>


<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.callbacks.modal.onShow.modalCreateDocument = function(trigger) {
        $('#modalCreateDocument input[name=ownerid]').attr('value', $(trigger).attr('data-ownerid'))
        $('#modalCreateDocument input[name=ownerclass]').attr('value', $(trigger).attr('data-ownerclass'))
        $('#modalCreateDocument input[name=ownertp]').attr('value', $(trigger).attr('data-ownertp'))
        $('#modalCreateDocument select[name=doctype]').dropdown('set selected', $(trigger).attr('data-doctype'))
    }
</laser:script>

</g:if>

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd/>
