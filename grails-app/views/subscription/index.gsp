<%@ page import="de.laser.AuditConfig; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.IssueEntitlementCoverage; de.laser.storage.RDStore; de.laser.Subscription; de.laser.wekb.Package; de.laser.RefdataCategory; de.laser.storage.RDConstants" %>

<laser:htmlStart message="subscription.details.current_ent" />

<laser:render template="breadcrumb" model="${[params: params]}"/>
<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:messages data="${flash}"/>

<g:if test="${params.asAt}">
    <h1 class="ui header" style="display: inline">
        ${message(code: 'subscription.details.snapshot', args: [params.asAt])}
    </h1>
</g:if>

<ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
    <laser:render template="iconSubscriptionIsChild"/>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>
<ui:anualRings object="${subscription}" controller="subscription" action="index" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<laser:render template="nav"/>

<div id="downloadWrapper"></div>

<g:if test="${permanentTitlesProcessRunning}">
    <ui:msg class="warning" showIcon="true" hideClose="true" header="Info" message="subscription.details.permanentTitlesProcessRunning.info" />
</g:if>

<g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortium()?.id}">
    <laser:render template="message"/>
</g:if>

<g:if test="${issueEntitlementEnrichment && (success || error)}">
    <g:if test="${success}">
        <ui:msg class="success" header="${message(code: 'subscription.details.issueEntitlementEnrichment.label')}">
            <g:message code="subscription.details.issueEntitlementEnrichment.enrichmentProcess"
                       args="[processCount, toAddCount, processCountChangesCoverageDates, processCountChangesPrice]"/>
        </ui:msg>
    </g:if>
    <g:if test="${Boolean.valueOf(error)}">
        <g:if test="${pickWithNoPick}">
            <ui:msg class="error" showIcon="true" message="subscription.details.issueEntitlementEnrichment.pickWithNoPick"/>
        </g:if>
        <g:else>
            <input id="errorMailto" type="hidden" value="${mailTo.content.join(';')}" />
            <input id="errorMailcc" type="hidden" value="${contextService.getUser().email}" />
            <textarea id="errorMailBody" class="hidden">${mailBody}</textarea>
            <ui:msg class="error" showIcon="true" message="subscription.details.issueEntitlementEnrichment.matchingError"
                    args="[notAddedCount, notSubscribedCount, notInPackageCount, g.createLink(controller: 'package', action:'downloadLargeFile', params:[token: token, fileformat: 'kbart'])]"/>
            <laser:script file="${this.getGroovyPageFileName()}">
                JSPC.notifyProvider = function () {
                    let mailto = $('#errorMailto').val();
                    let mailcc = $('#errorMailcc').val();
                    let subject = 'Fehlerhafte KBART';
                    let body = $('#errorMailBody').html();
                    let href = 'mailto:' + mailto + '?subject=' + subject + '&cc=' + mailcc + '&body=' + body;

                    window.location.href = encodeURI(href);
                }
            </laser:script>
        </g:else>
    </g:if>
</g:if>

<g:if test="${deletedSPs}">
    <div class="ui icon error message">
        <i class="${Icon.UI.ERROR}"></i>
        <ul class="list">
            <g:each in="${deletedSPs}" var="sp">
                <li>
                    <g:message code="subscription.details.packagesDeleted.header" args="${[sp.name]}"/>
                    <g:message code="subscription.details.packagesDeleted.entry"/> <ui:wekbIconLink type="package" gokbId="${sp.uuid}"/>
                </li>
            </g:each>
        </ul>
    </div>
</g:if>

<g:if test="${frozenHoldings}">
    <div class="ui icon error message">
        <i class="${Icon.UI.ERROR}"></i>
        <ul class="list">
            <g:each in="${frozenHoldings}" var="sp">
                <li><g:message code="subscription.details.frozenHoldings.header"
                               args="${[sp.name]}"/> ${message(code: "subscription.details.frozenHoldings.entry")}</li>
            </g:each>
        </ul>
    </div>
</g:if>
<div class="ui grid">
    <g:if test="${subscription.packages}">
        <div class="sixteen wide column">
            <div class="la-inline-lists">
                <div id="packages" class="la-padding-top-1em"></div>
            </div>
        </div>
    </g:if>


    <g:if test="${entitlements && entitlements.size() == 0}">
        <div class="row">
            <div class="column">
                ${message(code: 'subscription.details.no_ents')}
            </div>
        </div><!--.row-->
    </g:if>

</div><!--.grid-->
    <g:if test="${issueEntitlementEnrichment}">
        <div class="ui grid">
            <div class="row">
                <div class="column">
                    <g:if test="${!AuditConfig.getConfig(subscription, 'holdingSelection')}">
                        <div class="ui la-filter segment">
                            <h4 class="ui header"><g:message code="subscription.details.issueEntitlementEnrichment.label"/></h4>

                            <ui:msg class="warning" header="${message(code: "message.attention")}"
                                    message="subscription.details.addEntitlements.warning"/>

                            <g:form class="ui form" controller="subscription" action="processIssueEntitlementEnrichment"
                                    params="${[sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscription.id]}"
                                    method="post" enctype="multipart/form-data">
                                <div class="three fields">
                                    <div class="field">
                                        <div class="ui fluid action input">
                                            <input type="text" readonly="readonly"
                                                   placeholder="${message(code: 'template.addDocument.selectFile')}">
                                            <input type="file" id="kbartPreselect" name="kbartPreselect"
                                                   accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                                                   style="display: none;">

                                            <div class="${Btn.ICON.SIMPLE}">
                                                <i class="${Icon.CMD.ATTACHMENT}"></i>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="field">
                                        <g:if test="${issueEntitlementService.existsSerialInHolding(subscription, params.list('status'))}">
                                            <div class="ui checkbox toggle">
                                                <g:checkBox name="uploadCoverageDates" value="${uploadCoverageDates}"/>
                                                <label><g:message
                                                        code="subscription.details.issueEntitlementEnrichment.uploadCoverageDates.label"/></label>
                                            </div>
                                        </g:if>

                                        <div class="ui checkbox toggle">
                                            <g:checkBox name="uploadPriceInfo" value="${uploadPriceInfo}"/>
                                            <label><g:message
                                                    code="subscription.details.issueEntitlementEnrichment.uploadPriceInfo.label"/></label>
                                        </div>
                                    </div>

                                    <div class="field">
                                        <div class="ui checkbox toggle">
                                            <g:checkBox name="withPick"/>
                                            <label><g:message code="subscription.details.issueEntitlementEnrichment.withPick.label"/></label>
                                        </div>
                                    </div>

                                    <div class="field">
                                        <input type="submit"
                                               value="${message(code: 'subscription.details.addEntitlements.preselect')}"
                                               class="${Btn.SIMPLE} fluid"/>
                                    </div>
                                </div>
                            </g:form>
                            <laser:script file="${this.getGroovyPageFileName()}">
                                $('.action .icon.button').click(function () {
                                    $(this).parent('.action').find('input:file').click();
                                });

                                $('input:file', '.ui.action.input').on('change', function (e) {
                                    var name = e.target.files[0].name;
                                    $('input:text', $(e.target).parent()).val(name);
                                });
                            </laser:script>
                        </div>
                    </g:if>
                    <g:else>
                        <ui:msg class="error" showIcon="error" header="${message(code:"default.error")}" message="subscription.details.addEntitlements.holdingEntire" />
                    </g:else>
                </div>
            </div><!--.row-->
        </div><!--.grid-->
    </g:if>

%{--    <g:if test="${subscription.ieGroups.size() > 0}">
        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:link controller="subscription" action="index" id="${subscription.id}"
                    class="item ${params.titleGroup ? '' : 'active'}">
                Alle
                <ui:bubble count="${num_ies_rows}" />
            </g:link>

            <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                <g:link controller="subscription" action="index" id="${subscription.id}"
                        params="[titleGroup: titleGroup.id]"
                        class="item ${(params.titleGroup == titleGroup.id.toString()) ? 'active' : ''}">
                    ${titleGroup.name}
                    <ui:bubble count="${titleGroup.countCurrentTitles()}" />
                </g:link>
            </g:each>
        </div>
        <div class="ui bottom attached tab active segment">
    </g:if>--}%

<laser:render template="/templates/titles/top_attached_title_tabs"
              model="${[
                      tt_controller: 'subscription',
                      tt_action:     actionName,
                      tt_tabs:       ['currentIEs', 'plannedIEs', 'expiredIEs', 'deletedIEs', 'allIEs'],
                      tt_counts:     [currentIECounts, plannedIECounts, expiredIECounts, deletedIECounts, allIECounts],
                      tt_params:     [id: subscription.id]
              ]}" />

<% String tab = params.remove('tab')%>

<div class="ui bottom attached tab active segment">

    <laser:render template="/templates/filter/tipp_ieFilter" model="[forTitles: tab]"/>
    <br>

    <div class="ui grid">
        <div class="row">
            <div class="eight wide column">
                <h3 class="ui icon header la-clear-before la-noMargin-top">
                    <ui:bubble count="${num_ies_rows}" grey="true"/> <g:message code="title.found.result"/>
                </h3>
            </div>

            <div class="eight wide column">
                <g:if test="${entitlements && editable}">
                    <div class="field la-field-right-aligned">
                        <div class="${Btn.SIMPLE} right floated la-js-editButton la-la-clearfix>"><g:message code="default.button.edit.label"/></div>
                    </div>
                </g:if>
            </div>
        </div><!--.row-->
    </div><!--.grid-->
    <g:if test="${entitlements}">
        <div class="ui form">
            <div class="two wide fields">
                <div class="field">
                    <laser:render template="/templates/titles/sorting_dropdown" model="${[sd_type: 1, sd_journalsOnly: journalsOnly, sd_sort: params.sort, sd_order: params.order]}" />
                </div>
                <div class="field la-field-noLabel">
                    <ui:showMoreCloseButton />
                </div>
            </div>
        </div>
    </g:if>
<div class="ui grid">
    <div class="row">
        <div class="column">
        <g:form action="subscriptionBatchUpdate" params="${[id: subscription.id]}" class="ui form">
            <g:set var="counter" value="${offset + 1}"/>
            <g:hiddenField name="sub" value="${subscription.id}"/>
            <g:each in="${considerInBatch}" var="key">
                <g:if test="${params[key] instanceof String[] || params[key] instanceof List}">
                    <g:each in="${params[key]}" var="paramVal">
                        <g:hiddenField name="${key}" value="${paramVal}"/>
                    </g:each>
                </g:if>
                <g:else>
                    <g:hiddenField name="${key}" value="${params[key]}"/>
                </g:else>
            </g:each>

                <laser:script file="${this.getGroovyPageFileName()}">
                    $('.la-js-editButton').on('click', function(){
                      $( ".la-js-show-hide").toggle( "fast" );
                    });
                </laser:script>
                <g:if test="${editable}">
                    <g:set var="selected_label" value="${message(code: 'default.selected.label')}"/>

                <div class="ui segment grid la-filter la-js-show-hide" style="display: none">
                    <g:if test="${IssueEntitlementCoverage.executeQuery('select count(ic) from IssueEntitlementCoverage ic join ic.issueEntitlement ie where ie.subscription = :sub and ie.status != :removed', [sub: subscription, removed: RDStore.TIPP_STATUS_REMOVED])[0] > 0}">
                        <div class="row">
                            <div class="three wide column">
                                <div class="field">
                                    <label><g:message code="tipp.startDate.tooltip"/></label>
                                    <ui:datepicker hideLabel="true"
                                                   placeholder="${message(code: 'default.from')}"
                                                   inputCssClass="la-input-small" id="bulk_start_date"
                                                   name="bulk_start_date"/>
                                </div>
                            </div>
                            <div class="three wide column">
                                <div class="field">
                                    <label><g:message code="tipp.startVolume.tooltip"/></label>
                                    <input class="ui input" type="text" name="bulk_start_volume"/>
                                </div>
                            </div>
                            <div class="three wide column">
                                <div class="field">
                                    <label><g:message code="tipp.startIssue.tooltip"/></label>
                                    <input class="ui input" type="text" name="bulk_start_issue"/>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="three wide column">
                                <div class="field">
                                    <label><g:message code="tipp.endDate.tooltip"/></label>
                                    <ui:datepicker hideLabel="true"
                                                   placeholder="${message(code: 'default.to')}"
                                                   inputCssClass="la-input-small" id="bulk_end_date"
                                                   name="bulk_end_date"/>
                                </div>
                            </div>
                            <div class="three wide column">
                                <div class="field">
                                    <label><g:message code="tipp.endVolume.tooltip"/></label>
                                    <input class="ui input" type="text" name="bulk_end_volume"/>
                                </div>
                            </div>
                            <div class="three wide column">
                                <div class="field">
                                    <label><g:message code="tipp.endIssue.tooltip"/></label>
                                    <input class="ui input" type="text" name="bulk_end_issue"/>
                                </div>
                            </div>
                            <div class="three wide column">
                                <div class="field">
                                    <label>Embargo</label>
                                    <input class="ui input" type="text" name="bulk_embargo"/>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <div class="row">
                        <div class="four wide column">
                            <div class="field">
                                <label><g:message code="subscription.details.access_dates"/></label>
                                <ui:datepicker hideLabel="true"
                                               placeholder="${message(code: 'default.from')}"
                                               inputCssClass="la-input-small" id="bulk_access_start_date"
                                               name="bulk_access_start_date"/>
                            </div>
                        </div>
                        <div class="four wide column">
                            <div class="field la-field-noLabel">
                                <ui:datepicker hideLabel="true"
                                               placeholder="${message(code: 'default.to')}"
                                               inputCssClass="la-input-small" id="bulk_access_end_date"
                                               name="bulk_access_end_date"/>

                            </div>
                        </div>
                        <div class="four wide column">
                            <div class="field">
                                <label><g:message code="tipp.price.localPrice"/></label>
                                <input class="ui input" type="text" name="bulk_local_price"/>
                            </div>
                        </div>
                        <div class="four wide column">
                            <div class="field la-field-noLabel">
                                <ui:select class="ui dropdown clearable" name="bulk_local_currency" title="${message(code: 'financials.addNew.currencyType')}"
                                    optionKey="id" optionValue="value" from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.CURRENCY)}"/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="four wide column">
                            <div class="field">
                                <label><g:message code="issueEntitlement.myNotes"/></label>
                                <input class="ui input" type="text" name="bulk_notes"/>
                            </div>
                        </div>
                        <div class="four wide column">
                            <g:if test="${subscription.ieGroups.size() > 0}">
                                <div class="field">
                                    <label><g:message code="subscription.details.ieGroups"/></label>
                                    <select class="ui dropdown clearable" name="titleGroupInsert" id="titleGroupInsert">
                                        <option value="">${message(code: 'default.select.choose.label')}</option>
                                        <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                                            <option value="${titleGroup.id}">${titleGroup.name}</option>
                                        </g:each>
                                    </select>
                                </div>
                            </g:if>
                        </div>
                        <%--<div class="four wide column">
                            <div class="field la-field-noLabel">
                                <div class="ui selection dropdown la-clearable">
                                    <input type="hidden" id="bulkOperationSelect" name="bulkOperation">
                                    <i class="dropdown icon"></i>

                                    <div class="default text">${message(code: 'default.select.choose.label')}</div>

                                    <div class="menu">
                                        <div class="item"
                                             data-value="edit">${message(code: 'default.edit.label', args: [selected_label])}</div>

                                        <div class="item"
                                             data-value="remove">${message(code: 'default.remove.label', args: [selected_label])}</div>
                                        <g:if test="${institution.isCustomerType_Consortium()}">
                                            <div class="item"
                                                 data-value="removeWithChildren">${message(code: 'subscription.details.remove.withChildren.label')}</div>
                                        </g:if>
                                    </div>
                                </div>
                            </div>
                        </div>--%>
                    </div>
                    <div class="row">
                        <div class="one wide column">
                            <div class="field la-field-noLabel">
                                <input id="select-all" type="checkbox" name="chkall" onClick="JSPC.app.selectAll()"/>
                            </div>
                        </div>
                        <div class="twelve wide column"></div>
                        <div class="three wide column">
                            <div class="field right align">
                                <button data-position="top right"
                                        data-content="${message(code: 'default.button.apply_batch.label')}"
                                        type="submit" onClick="return JSPC.app.confirmSubmit()"
                                        class="${Btn.SIMPLE_TOOLTIP}"><g:message code="default.button.apply_batch.label"/>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
                <g:if test="${entitlements}">
                    <div class="ui fluid card">
                        <div class="content">
                            <div class="ui accordion la-accordion-showMore la-js-showMoreCloseArea">
                                <g:each in="${entitlements}" var="ie">
                                    <div class="ui raised segments la-accordion-segments">
                                        <div class="ui fluid segment title" data-ajaxTippId="${ie.tipp.id}" data-ajaxIeId="${ie ? ie.id : null}">
                                            <div class="ui stackable equal width grid">
                                                <g:set var="participantPerpetualAccessToTitle"
                                                       value="${surveyService.listParticipantPerpetualAccessToTitle(subscription.getSubscriberRespConsortia(), ie.tipp)}"/>
                                                <g:if test="${participantPerpetualAccessToTitle.size() > 0}">
                                                    <g:if test="${ie.perpetualAccessBySub && !(ie.perpetualAccessBySub.id in [subscription.id, subscription.instanceOf?.id])}">
                                                        <g:link controller="subscription" action="index" id="${ie.perpetualAccessBySub.id}">
                                                            <span class="ui mini left corner label la-perpetualAccess la-js-notOpenAccordion la-popup-tooltip"
                                                                  data-content="${message(code: 'subscription.start.with')} ${ie.perpetualAccessBySub.dropdownNamingConvention()}"
                                                                  data-position="left center" data-variation="tiny">
                                                                <i class="star blue icon"></i>
                                                            </span>
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <span class="ui mini left corner label la-perpetualAccess la-js-notOpenAccordion la-popup-tooltip"
                                                              data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')} ${participantPerpetualAccessToTitle.collect{it.getPermanentTitleInfo()}.join(',')}"
                                                              data-position="left center" data-variation="tiny">
                                                            <i class="star icon"></i>
                                                        </span>
                                                    </g:else>
                                                </g:if>
                                                <div class="one wide column la-js-show-hide" style="display: none">
                                                    <g:if test="${editable}"><input type="checkbox"
                                                                                    name="_bulkflag.${ie.id}"
                                                                                    class="bulkcheck la-vertical-centered la-js-notOpenAccordion"/></g:if>
                                                </div>

                                                <div class="one wide column">
                                                    <span class="la-vertical-centered">${counter++}</span>
                                                </div>

                                                <div class="column">
                                                    <div class="ui list">

                                                        <!-- START TEMPLATE -->
                                                        <laser:render
                                                                template="/templates/titles/title_short_accordion"
                                                                model="${[ie         : ie, tipp: ie.tipp,
                                                                          showPackage: true, showPlattform: true, showEmptyFields: false, sub: subscription.id]}"/>
                                                        <!-- END TEMPLATE -->

                                                    </div>
                                                </div>

                                                <div class="column">
                                                    <laser:render template="/templates/tipps/coverages_accordion"
                                                                  model="${[ie: null, tipp: ie.tipp, overwriteEditable: false]}"/>
                                                </div>

                                                <div class="four wide column">

                                                    <!-- START TEMPLATE -->
                                                    <laser:render template="/templates/identifier" model="${[tipp: ie.tipp]}"/>
                                                    <!-- END TEMPLATE -->
                                                </div>

                                                <div class="two wide column">
                                                    <g:each in="${ie.tipp.priceItems}" var="priceItem" status="i">
                                                        <g:if test="${priceItem.listCurrency}">
                                                            <div class="ui list">
                                                                <div class="item">
                                                                    <div class="content">
                                                                        <div class="header"><g:message code="tipp.price.listPrice"/></div>
                                                                        <div class="content"><g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}"
                                                                                                                                                              currencySymbol="${priceItem.listCurrency.value}"/>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </g:if>
                                                    </g:each>
                                                </div>

                                                <div class="one wide column">
                                                    <div class="ui right floated buttons">
                                                        <div class="right aligned wide column">
                                                        </div>
                                                        <div class="${Btn.MODERN.SIMPLE}">
                                                            <i class="${Icon.CMD.SHOW_MORE}"></i>
                                                        </div>
                                                        <g:if test="${editable}">
                                                            <g:if test="${subscription.ieGroups.size() > 0}">
                                                                <g:link action="removeEntitlementWithIEGroups"
                                                                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                        params="${[ieid: ie.id, sub: subscription.id, tab: tab]}"
                                                                        role="button"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.entitlementWithIEGroups", args: [ie.tipp.name])}"
                                                                        data-confirm-term-how="delete"
                                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </g:link>
                                                            </g:if>
                                                            <g:else>
                                                                <g:link action="removeEntitlement"
                                                                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                        params="${[ieid: ie.id, sub: subscription.id, tab: tab]}"
                                                                        role="button"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.entitlement", args: [ie.tipp.name])}"
                                                                        data-confirm-term-how="delete"
                                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </g:link>
                                                            </g:else>
                                                        </g:if>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                                            <div class="ui stackable grid" data-ajaxTarget="true">

                                                <div class="three wide column">
                                                    <div class="ui list la-label-list">
                                                        <g:if test="${ie.tipp.accessStartDate}">
                                                            <div class="ui label la-label-accordion">${message(code: 'tipp.access')}</div>
                                                            <div class="item">
                                                                <div class="content">
                                                                        <g:formatDate
                                                                                format="${message(code: 'default.date.format.notime')}"
                                                                                date="${ie.tipp.accessStartDate}"/>
                                                                </div>
                                                            </div>

                                                        </g:if>
                                                        <g:if test="${ie.tipp.accessEndDate}">
                                                            <!-- bis -->
                                                            <!-- DEVIDER  -->
                                                            <ui:dateDevider/>
                                                            <div class="item">
                                                                <div class="content">
                                                                    <g:formatDate
                                                                            format="${message(code: 'default.date.format.notime')}"
                                                                            date="${ie.tipp.accessEndDate}"/>
                                                                </div>
                                                            </div>
                                                        </g:if>

                                                        <%-- Coverage Details START --%>
                                                        <g:each in="${ie.tipp.coverages}" var="covStmt" status="counterCoverage">
                                                            <g:if test="${covStmt.coverageNote || covStmt.coverageDepth || covStmt.embargo}">
                                                                <div class="ui label la-label-accordion">${message(code: 'tipp.coverageDetails')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
                                                            </g:if>
                                                            <g:if test="${covStmt.coverageNote}">
                                                                <div class="item">
                                                                    <i class="${Icon.ATTR.TIPP_COVERAGE_NOTE} la-popup-tooltip" data-content="${message(code: 'default.note.label')}"></i>
                                                                    <div class="content">
                                                                        <div class="header">
                                                                            ${message(code: 'default.note.label')}
                                                                        </div>
                                                                        <div class="description">
                                                                            ${covStmt.coverageNote}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </g:if>
                                                            <g:if test="${covStmt.coverageDepth}">
                                                                <div class="item">
                                                                    <i class="${Icon.ATTR.TIPP_COVERAGE_DEPTH} la-popup-tooltip" data-content="${message(code: 'tipp.coverageDepth')}"></i>
                                                                    <div class="content">
                                                                        <div class="header">
                                                                            ${message(code: 'tipp.coverageDepth')}
                                                                        </div>
                                                                        <div class="description">
                                                                            ${covStmt.coverageDepth}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </g:if>
                                                            <g:if test="${covStmt.embargo}">
                                                                <div class="item">
                                                                    <i class="${Icon.ATTR.TIPP_EMBARGO} la-popup-tooltip" data-content="${message(code: 'tipp.embargo')}"></i>
                                                                    <div class="content">
                                                                        <div class="header">
                                                                            ${message(code: 'tipp.embargo')}
                                                                        </div>
                                                                        <div class="description">
                                                                            ${covStmt.embargo}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </g:if>
                                                        </g:each>
                                                        <%-- Coverage Details END --%>
                                                    </div>
                                                </div>
                                                <%-- My Area START--%>
                                                <div class="seven wide column">
                                                    <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip"
                                                       data-content="${message(code: 'tipp.tooltip.myArea')}"></i>

                                                    <div class="ui la-segment-with-icon">
                                                        <laser:render template="/templates/tipps/coverages_accordion"
                                                                      model="${[ie: ie, tipp: ie.tipp]}"/>

                                                        <div class="ui list">
                                                            <g:if test="${ie}">
                                                                <div class="item">
                                                                    <i class="grey ${Icon.CMD.EDIT} la-popup-tooltip"
                                                                       data-content="${message(code: 'issueEntitlement.myNotes')}"></i>
                                                                    <div class="content">
                                                                        <div class="header"><g:message code="issueEntitlement.myNotes"/></div>
                                                                        <div class="description">
                                                                            <ui:xEditable owner="${ie}" type="text" field="notes"/>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </g:if>

                                                            <div id="priceWrapper_${ie.id}">
                                                                <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                                                    <g:render template="/templates/tipps/priceItem" model="[priceItem: priceItem, editable: editable]"/>
                                                                </g:each>
                                                            </div>
                                                            <hr>
                                                            <g:if test="${editable && subscription.holdingSelection != RDStore.SUBSCRIPTION_HOLDING_ENTIRE && !auditService.getAuditConfig(subscription.instanceOf, 'holdingSelection')}">
                                                                <button class="${Btn.SIMPLE} tiny addObject" data-wrapper="priceWrapper" data-objType="priceItem" data-ie="${ie.id}">
                                                                    <i class="${Icon.FNC.COST_CONFIG}"></i>${message(code: 'subscription.details.addEmptyPriceItem.info')}
                                                                </button>
                                                                <g:if test="${ie.tipp.titleType == 'serial'}">
                                                                    <button class="${Btn.SIMPLE} tiny addObject" data-wrapper="coverageWrapper" data-objType="coverage" data-ie="${ie.id}">
                                                                        <%-- TODO David new icon for coverage statement --%>
                                                                        <i class="file icon"></i>${message(code: 'subscription.details.addCoverage')}
                                                                    </button>
                                                                </g:if>
                                                            </g:if>

                                                            <%-- GROUPS START--%>
                                                            <g:if test="${subscription.ieGroups.size() > 0}">
                                                                <g:each in="${ie.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                                                                    <div class="item">
                                                                        <i class="${Icon.IE_GROUP} grey la-popup-tooltip"
                                                                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i>
                                                                        <div class="content">
                                                                            <div class="header"><g:message code="subscription.details.ieGroups"/></div>
                                                                            <div class="description"><g:link controller="subscription" action="index"
                                                                                                             id="${subscription.id}"
                                                                                                             params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </g:each>
                                                                <g:if test="${editable}">
                                                                    <g:link action="editEntitlementGroupItem"
                                                                            params="${[cmd: 'edit', ie: ie.id, id: subscription.id]}"
                                                                            class="${Btn.SIMPLE} tiny trigger-modal">
                                                                        <i class="${Icon.IE_GROUP}"></i>${message(code: 'subscription.details.ieGroups.edit')}
                                                                    </g:link>
                                                                </g:if>
                                                            </g:if>


                                                            <%-- GROUPS END--%>
                                                        </div>
                                                    </div>

                                                    <laser:render template="/templates/reportTitleToProvider/multiple_infoBox" model="${[tipp: ie.tipp]}"/>
                                                </div><%-- My Area END --%>
                                            </div><%-- .grid --%>
                                        </div><%-- .segment --%>
                                    </div><%--.segments --%>
                                </g:each>
                            </div><%-- .accordions --%>
                        </div><%-- .content --%>
                    </div><%-- .card --%>
                </g:if>
            </g:form>
        </div><%-- .column --%>
    </div><%--.row --%>
</div><%--.grid --%>
<g:if test="${entitlements}">
    <div class="ui clearing segment la-segmentNotVisable">
        <ui:showMoreCloseButton />
    </div>
</g:if>
</div>

%{--<g:if test="${subscription.ieGroups.size() > 0}">
    </div><%-- .ui.bottom.attached.tab.active.segment --%>
</g:if>--}%

<g:if test="${entitlements}">
    <ui:paginate action="index" controller="subscription" params="${params}" max="${max}" total="${num_ies_rows}"/>
</g:if>

<div id="magicArea">
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.hideModal = function () {
      $("[name='coreAssertionEdit']").modal('hide');
    }
    JSPC.app.showCoreAssertionModal = function () {
      $("[name='coreAssertionEdit']").modal('show');
    }

    <g:if test="${editable}">

        JSPC.app.selectAll = function () {
          $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
        }

        JSPC.app.confirmSubmit = function () {
          if ( $.inArray($('#bulkOperationSelect').val(), ['remove', 'removeWithChildren']) > -1 ) {
            var agree=confirm('${message(code: 'default.continue.confirm')}');
          if (agree)
            return true ;
          else
            return false ;
        }
      }
    </g:if>

    $('.la-books.icon, .la-notebook.icon').popup({
        delay: {
            show: 150, hide: 0
        }
      });
    $('.trigger-modal').on('click', function(e) {
            e.preventDefault();

            $.ajax({
                url: $(this).attr('href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #editEntitlementGroupItemModal').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                    onVisible: function () {
                        r2d2.initDynamicUiStuff('#editEntitlementGroupItemModal');
                        r2d2.initDynamicXEditableStuff('#editEntitlementGroupItemModal');
                        $("html").css("cursor", "auto");
                    },
                    detachable: true,
                    autofocus: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        });

    $('.kbartExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $.ajax({
            url: "<g:createLink action="exportHolding" params="${params + [exportKBart: true]}"/>",
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response);
            $('#globalLoadingIndicator').hide();
        });
    });


    $("[data-ajaxTippId]").on('click', function(e) {
            var tippID = $(this).attr('data-ajaxTippId');
            var ieID = $(this).attr('data-ajaxIeId');

            var dataAjaxTarget = $(this)
                .next($('[data-ajaxTargetWrap]'))
                .children($('[data-ajaxTarget]'));

            var dataAjaxTopic = $(this)
                .next($('[data-ajaxTargetWrap]'))
                .find($('[data-ajaxTopic]'));


            $.ajax({
                url: '<g:createLink controller="ajaxHtml" action="showAllTitleInfosAccordion" params="[showPackage: true, showPlattform: true, showCompact: showCompact, showEmptyFields: showEmptyFields]"/>&tippID=' + tippID + '&ieID=' + ieID,
                    success: function(result) {
                        dataAjaxTopic.remove();
                        dataAjaxTarget.prepend(result);
                    }
                });
    });

    $(".addObject").on('click', function(e) {
        e.preventDefault();
        let objType = $(this).attr('data-objType');
        let ie = $(this).attr('data-ie');
        let wrapper = "#"+$(this).attr('data-wrapper')+"_"+ie;
        $.ajax({
            url: '<g:createLink controller="ajaxHtml" action="addObject"/>',
            data: {
                object: objType,
                ieid: ie
            }
        }).done(function(result) {
            $(wrapper).append(result);
            r2d2.initDynamicUiStuff(wrapper);
            r2d2.initDynamicXEditableStuff(wrapper);
        });
    });

    $(".removeObject").on('click', function(e) {
        e.preventDefault();
        let objType = $(this).attr('data-objType');
        let objId = $(this).attr('data-objId');
        let trigger = $(this).attr('data-trigger');
        $.ajax({
            url: '<g:createLink controller="ajaxJson" action="removeObject"/>',
            data: {
                object: objType,
                objId: objId
            }
        }).done(function(result) {
            if(result.success === true) {
                $('[data-object="'+trigger+'"]').remove();
            }
        });
    });

    <%--
    JSPC.app.loadFilter = function() {
        $.ajax({
            url: "<g:createLink action="getTippIeFilter"/>",
            data: {
                formAction: "${actionName}",
                <g:each in="${params.keySet()}" var="key">${key}: <g:if test="${params[key] instanceof String[] || params[key] instanceof List}">[${params[key].join(',')}]</g:if><g:else>"${params[key]}"</g:else>,</g:each>
            }
        }).done(function(response){
            $("#filterWrapper").html(response);
            r2d2.initDynamicUiStuff("#filterWrapper");
        });
    }

    JSPC.app.loadFilter();
    --%>

    JSPC.app.loadPackages = function () {
    $.ajax({
        url: "<g:createLink controller="ajaxHtml" action="getPackageData"/>",
                  data: {
                      subscription: "${subscription.id}"
                  }
              }).done(function(response){
                  $("#packages").html(response);
                  r2d2.initDynamicUiStuff("#packages");
              })
          }

    JSPC.app.loadPackages();
</laser:script>

<g:render template="/clickMe/export/js"/>

<g:render template="/templates/reportTitleToProvider/multiple_flyoutAndTippTask"/>

<laser:htmlEnd/>
