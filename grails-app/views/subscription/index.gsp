<%@ page import="de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.remote.ApiSource; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Package; de.laser.RefdataCategory; de.laser.storage.RDConstants" %>
<laser:htmlStart message="subscription.details.current_ent" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>
<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:modeSwitch controller="subscription" action="index" params="${params}"/>

<ui:messages data="${flash}"/>

<g:if test="${params.asAt}"><h1
        class="ui left floated aligned icon header la-clear-before"><ui:headerIcon/>${message(code: 'subscription.details.snapshot', args: [params.asAt])}</h1></g:if>

<ui:h1HeaderWithIcon>
    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <laser:render template="iconSubscriptionIsChild"/>
    </g:if>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>
<ui:anualRings object="${subscription}" controller="subscription" action="index"
               navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<laser:render template="nav"/>

<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <laser:render template="message"/>
</g:if>

<g:if test="${enrichmentProcess}">
    <ui:msg class="positive" header="${message(code: 'subscription.details.issueEntitlementEnrichment.label')}">
        <g:message code="subscription.details.issueEntitlementEnrichment.enrichmentProcess"
                   args="[enrichmentProcess.issueEntitlements, enrichmentProcess.processCount, enrichmentProcess.processCountChangesCoverageDates, enrichmentProcess.processCountChangesPrice]"/>
    </ui:msg>
</g:if>

<g:if test="${deletedSPs}">
    <div class="ui exclamation icon negative message">
        <i class="exclamation icon"></i>
        <ul class="list">
            <g:each in="${deletedSPs}" var="sp">
                <li><g:message code="subscription.details.packagesDeleted.header"
                               args="${[sp.name]}"/> ${message(code: "subscription.details.packagesDeleted.entry", args: [raw(link(url: sp.link) { 'we:kb' })])}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<g:if test="${frozenHoldings}">
    <div class="ui exclamation icon negative message">
        <i class="exclamation icon"></i>
        <ul class="list">
            <g:each in="${frozenHoldings}" var="sp">
                <li><g:message code="subscription.details.frozenHoldings.header"
                               args="${[sp.name]}"/> ${message(code: "subscription.details.frozenHoldings.entry")}</li>
            </g:each>
        </ul>
    </div>
</g:if>
<div class="ui grid">

    <div class="row">
        <div class="column">

            <g:if test="${entitlements && entitlements.size() > 0}">

                <g:if test="${subscription.packages.size() > 1}">
                    <a class="ui right floated button" data-href="#showPackagesModal" data-ui="modal"><g:message
                            code="subscription.details.details.package.label"/></a>
                </g:if>

                <g:if test="${subscription.packages.size() == 1}">
                    <g:link class="ui right floated button" controller="package" action="show"
                            id="${subscription.packages[0].pkg.id}"><g:message
                            code="subscription.details.details.package.label"/></g:link>
                </g:if>
            </g:if>
            <g:else>
                ${message(code: 'subscription.details.no_ents')}
            </g:else>

        </div>
    </div><!--.row-->

    <g:if test="${issueEntitlementEnrichment}">
        <div class="row">
            <div class="column">
                <div class="ui la-filter segment">
                    <h4 class="ui header"><g:message code="subscription.details.issueEntitlementEnrichment.label"/></h4>

                    <ui:msg class="warning" header="${message(code: "message.attention")}"
                            message="subscription.details.addEntitlements.warning"/>
                    <g:form class="ui form" controller="subscription" action="index"
                            params="${[sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscription.id]}"
                            method="post" enctype="multipart/form-data">
                        <div class="three fields">
                            <div class="field">
                                <div class="ui fluid action input">
                                    <input type="text" readonly="readonly"
                                           placeholder="${message(code: 'template.addDocument.selectFile')}">
                                    <input type="file" id="kbartPreselect" name="kbartPreselect"
                                           accept="text/tab-separated-values, text/plain"
                                           style="display: none;">

                                    <div class="ui icon button">
                                        <i class="attach icon"></i>
                                    </div>
                                </div>
                            </div>

                            <div class="field">
                                <div class="ui checkbox toggle">
                                    <g:checkBox name="uploadCoverageDates" value="${uploadCoverageDates}"/>
                                    <label><g:message
                                            code="subscription.details.issueEntitlementEnrichment.uploadCoverageDates.label"/></label>
                                </div>

                                <div class="ui checkbox toggle">
                                    <g:checkBox name="uploadPriceInfo" value="${uploadPriceInfo}"/>
                                    <label><g:message
                                            code="subscription.details.issueEntitlementEnrichment.uploadPriceInfo.label"/></label>
                                </div>
                            </div>

                            <div class="field">
                                <input type="submit"
                                       value="${message(code: 'subscription.details.addEntitlements.preselect')}"
                                       class="fluid ui button"/>
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
            </div>
        </div><!--.row-->
    </g:if>

    <g:if test="${subscription.ieGroups.size() > 0}">
        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:link controller="subscription" action="index" id="${subscription.id}"
                    class="item ${params.titleGroup ? '' : 'active'}">
                Alle
                <span class="ui blue circular label">
                    ${num_ies_rows}
                </span>
            </g:link>

            <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                <g:link controller="subscription" action="index" id="${subscription.id}"
                        params="[titleGroup: titleGroup.id]"
                        class="item ${(params.titleGroup == titleGroup.id.toString()) ? 'active' : ''}">
                    ${titleGroup.name}
                    <span class="ui blue circular label">
                        ${titleGroup.countCurrentTitles()}
                    </span>
                </g:link>
            </g:each>

        </div>

        <div class="ui bottom attached tab active segment">
    </g:if>

    <div class="row">
        <div class="column">

            <laser:render template="/templates/filter/tipp_ieFilter"/>

        </div>
    </div><!--.row-->
<%
    Map<String, String>
    sortFieldMap = ['tipp.sortname': message(code: 'title.label')]
    if (journalsOnly) {
        sortFieldMap['startDate'] = message(code: 'default.from')
        sortFieldMap['endDate'] = message(code: 'default.to')
    } else {
        sortFieldMap['tipp.dateFirstInPrint'] = message(code: 'tipp.dateFirstInPrint')
        sortFieldMap['tipp.dateFirstOnline'] = message(code: 'tipp.dateFirstOnline')
    }
    sortFieldMap['tipp.accessStartDate'] = "${message(code: 'subscription.details.access_dates')} ${message(code: 'default.from')}"
    sortFieldMap['tipp.accessEndDate'] = "${message(code: 'subscription.details.access_dates')} ${message(code: 'default.to')}"
%>
    <div class="row">
        <div class="eight wide column">
            <h3 class="ui icon header la-clear-before la-noMargin-top"><span
                    class="ui circular  label">${num_ies_rows}</span> <g:message code="title.filter.result"/></h3>
        </div>


        <div class="eight wide column">
            <div class="field la-field-right-aligned ">
                <div class="ui right floated button la-js-editButton la-la-clearfix>"><g:message code="default.button.edit.label"/></div>
            </div>
        </div>
    </div><!--.row-->
</div><!--.grid-->


<div class="ui form">
    <div class="three wide fields">
        <div class="field">
            <ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
        </div>
    </div>
</div>


<div class="ui grid">
    <div class="row">
        <span class="column">
        <g:form action="subscriptionBatchUpdate" params="${[id: subscription.id]}" class="ui form">
            <g:set var="counter" value="${offset + 1}"/>
            <g:hiddenField name="sub" value="${subscription.id}"/>
            <g:each in="${considerInBatch}" var="key">
                <g:hiddenField name="${key}" value="${params[key]}"/>
            </g:each>

            <laser:script file="${this.getGroovyPageFileName()}">
                $('.la-js-editButton').on('click', function(){
                  $( ".la-js-show-hide").toggle( "fast" );
                });
            </laser:script>
            <g:if test="${editable}">
                <g:set var="selected_label" value="${message(code: 'default.selected.label')}"/>

                <div class="ui segment la-filter la-js-show-hide" style="display: none">
                    <div class="six fields  left floated">

                        <div class="field la-field-noLabel">
                            <input id="select-all" type="checkbox" name="chkall" onClick="JSPC.app.selectAll()"/>
                        </div>


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
                                    <g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM'}">
                                        <div class="item"
                                             data-value="removeWithChildren">${message(code: 'subscription.details.remove.withChildren.label')}</div>
                                    </g:if>
                                </div>
                            </div>
                        </div>



                        <div class="field">
                            <label><g:message code="subscription.details.access_dates"/></label>
                            <ui:datepicker hideLabel="true"
                                           placeholder="${message(code: 'default.from')}"
                                           inputCssClass="la-input-small" id="bulk_access_start_date"
                                           name="bulk_access_start_date"/>
                        </div>

                        <div class="field la-field-noLabel">
                            <ui:datepicker hideLabel="true"
                                           placeholder="${message(code: 'default.to')}"
                                           inputCssClass="la-input-small" id="bulk_access_end_date"
                                           name="bulk_access_end_date"/>

                        </div>
                        <g:if test="${subscription.ieGroups.size() > 0}">
                            <div class="field">
                                <label><g:message code="subscription.details.ieGroups"/></label>
                                 <select class="ui dropdown" name="titleGroupInsert" id="titleGroupInsert">
                                     <option value="">${message(code: 'default.select.choose.label')}</option>
                                    <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                                        <option value="${titleGroup.id}">${titleGroup.name}</option>
                                    </g:each>
                                </select>
                            </div>
                        </g:if>

                        <div class="field">
                            <label><g:message code="default.button.apply_batch.label"/></label>
                            <button data-position="top right"
                                    data-content="${message(code: 'default.button.apply_batch.label')}"
                                    type="submit" onClick="return JSPC.app.confirmSubmit()"
                                    class="ui icon button la-popup-tooltip la-delay"><i class="check icon"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${entitlements}">
                <div class="ui fluid card">
                    <div class="content">
                        <div class="ui accordion la-accordion-showMore">
                            <g:each in="${entitlements}" var="ie">
                                <div class="ui raised segments la-accordion-segments">
                                    <div class="ui fluid segment title" data-ajaxTippId="${ie.tipp.id}" data-ajaxIeId="${ie ? ie.id : null}">
                                        <div class="ui stackable equal width grid">
                                            <div class="one wide column la-js-show-hide" style="display: none">
                                                <g:if test="${editable}"><input type="checkbox"
                                                                                name="_bulkflag.${ie.id}"
                                                                                class="bulkcheck"/></g:if>
                                            </div>

                                            <div class="one wide column">
                                                ${counter++}
                                            </div>

                                            <div class="column">
                                                <div class="ui list">

                                                    <!-- START TEMPLATE -->
                                                    <laser:render
                                                            template="/templates/title_short_accordion"
                                                            model="${[ie         : ie, tipp: ie.tipp,
                                                                      showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false]}"/>
                                                    <!-- END TEMPLATE -->

                                                </div>
                                            </div>

                                            <div class="column">
                                                <laser:render template="/templates/tipps/coverages_accordion"
                                                              model="${[ie: null, tipp: ie.tipp, overwriteEditable: false]}"/>
                                            </div>

                                            <div class="four wide column">

                                                <!-- START TEMPLATE -->
                                                <laser:render template="/templates/identifier"
                                                              model="${[ie: ie, tipp: ie.tipp]}"/>
                                                <!-- END TEMPLATE -->
                                            </div>

                                            <div class="two wide column">
                                                <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                                    <g:if test="${priceItem.listCurrency}">
                                                        <div class="ui list">
                                                            <div class="item">
                                                                <div class="contet">
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

                                                    <div class="ui icon blue button la-modern-button "><i
                                                            class="ui angle double down icon"></i>
                                                    </div>
                                                    <g:if test="${editable}">
                                                        <g:if test="${subscription.ieGroups.size() > 0}">
                                                            <g:link action="removeEntitlementWithIEGroups"
                                                                    class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                                    params="${[ieid: ie.id, sub: subscription.id]}"
                                                                    role="button"
                                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.entitlementWithIEGroups", args: [ie.name])}"
                                                                    data-confirm-term-how="delete"
                                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                <i class="trash alternate outline icon"></i>
                                                            </g:link>
                                                        </g:if>
                                                        <g:else>
                                                            <g:link action="removeEntitlement"
                                                                    class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                                    params="${[ieid: ie.id, sub: subscription.id]}"
                                                                    role="button"
                                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.entitlement", args: [ie.name])}"
                                                                    data-confirm-term-how="delete"
                                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                                <i class="trash alternate outline icon"></i>
                                                            </g:link>
                                                        </g:else>
                                                    </g:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                                        <div class="ui stackable grid" data-ajaxTarget="true">


%{--
                                            <laser:render template="/templates/title_long_accordion"
                                                          model="${[ie         : ie, tipp: ie.tipp,
                                                                    showPackage: showPackage, showPlattform: showPlattform, showCompact: showCompact, showEmptyFields: showEmptyFields]}"/>

--}%


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
                                                                <i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'default.note.label')}"></i>
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
                                                                <i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>
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
                                                                <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>
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
                                                <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'tipp.tooltip.myArea')}"></i>

                                                <div class="ui la-segment-with-icon">

                                                    <laser:render template="/templates/tipps/coverages_accordion"
                                                                  model="${[ie: ie, tipp: ie.tipp]}"/>

                                                    <div class="ui list">
                                                        <g:if test="${ie}">
                                                            <div class="item">
                                                                <i class="grey save icon la-popup-tooltip la-delay"
                                                                   data-content="${message(code: 'issueEntitlement.perpetualAccessBySub.label')}"></i>

                                                                <div class="content">
                                                                    <div class="header">
                                                                        ${showCompact ? '' : message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}
                                                                    </div>

                                                                    <div class="description">
                                                                        ${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div class="item">
                                                                <i class="grey icon edit la-popup-tooltip la-delay"
                                                                   data-content="${message(code: 'issueEntitlement.myNotes')}"></i>
                                                                <div class="content">
                                                                    <div class="header"><g:message code="issueEntitlement.myNotes"/></div>
                                                                    <div class="description">
                                                                        <ui:xEditable owner="${ie}" type="text"
                                                                                      field="notes"/>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </g:if>

                                                        <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                                            <div class="item">
                                                                <i class="money grey icon la-popup-tooltip la-delay"></i>

                                                                <div class="content">
                                                                    <div class="header"><g:message
                                                                            code="tipp.price.localPrice"/>:</div>

                                                                    <div class="description">
                                                                        <ui:xEditable field="localPrice"
                                                                                      owner="${priceItem}"/> <ui:xEditableRefData
                                                                            field="localCurrency"
                                                                            owner="${priceItem}"
                                                                            config="Currency"/>
                                                                        <g:if test="${editable}">
                                                                            <span class="right floated">
                                                                                <g:link controller="subscription"
                                                                                        action="removePriceItem"
                                                                                        params="${[priceItem: priceItem.id, id: subscription.id]}"
                                                                                        class="ui compact icon button tiny"><i
                                                                                        class="ui icon minus"
                                                                                        data-content="Preis entfernen"></i></g:link>
                                                                            </span>
                                                                        </g:if>
                                                                    </div>

                                                                </div>
                                                            </div>

                                                            <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                                        </g:each>
                                                        <g:if test="${editable && ie.priceItems.size() < 1}">
                                                            <g:link action="addEmptyPriceItem"
                                                                    class="ui tiny button"
                                                                    params="${[ieid: ie.id, id: subscription.id]}">
                                                                <i class="money icon"></i>${message(code: 'subscription.details.addEmptyPriceItem.info')}
                                                            </g:link>
                                                        </g:if>

                                                        <%-- GROUPS START--%>
                                                        <g:if test="${subscription.ieGroups.size() > 0}">
                                                            <g:each in="${ie.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                                                                <div class="item">
                                                                    <i class="grey icon object group la-popup-tooltip la-delay"
                                                                       data-content="${message(code: 'issueEntitlementGroup.label')}"></i>
                                                                    <div class="content">
                                                                        <div class="header"><g:message code="subscription.details.ieGroups"/></div>
                                                                        <div class="description"><g:link controller="subscription" action="index"
                                                                                                         id="${subscription.id}"
                                                                                                         params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                                <g:if test="${editable}">
                                                                    <g:link action="editEntitlementGroupItem"
                                                                            params="${[cmd: 'edit', ie: ie.id, id: subscription.id]}"
                                                                            class="ui tiny button">
                                                                        <i class="object group icon"></i>${message(code: 'subscription.details.ieGroups.edit')}
                                                                    </g:link>
                                                                </g:if>
                                                            </g:each>
                                                        </g:if>


                                                        <%-- GROUPS END--%>
                                                    </div>
                                                </div>
                                            </div>
                                            <%-- My Area END ---%>
                                        </div>
                                    </div>
                                </div>
                            </g:each>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:form>

</div>
</div><!--.row-->
<g:if test="${subscription.ieGroups.size() > 0}">
    </div>
</g:if>

</div>

<g:if test="${entitlements}">
    <ui:paginate action="index" controller="subscription" params="${params}"
                 max="${max}" total="${num_ies_rows}"/>
</g:if>


<div id="magicArea">
</div>

<laser:render template="export/individuallyExportIEsModal" model="[modalID: 'individuallyExportIEsModal']"/>

<ui:modal id="showPackagesModal" message="subscription.packages.label" hideSubmitButton="true">
    <div class="ui ordered list">
        <g:each in="${subscription.packages.sort { it.pkg.name.toLowerCase() }}" var="subPkg">
            <div class="item">
                ${subPkg.pkg.name}
                <g:if test="${subPkg.pkg.contentProvider}">
                    (${subPkg.pkg.contentProvider.name})
                </g:if>:
                <g:link controller="package" action="show" id="${subPkg.pkg.id}"><g:message
                        code="subscription.details.details.package.label"/></g:link>
            </div>
        </g:each>
    </div>

</ui:modal>


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

    $('.la-books.icon').popup({
        delay: {
            show: 150,
            hide: 0
        }
      });
    $('.la-notebook.icon').popup({
        delay: {
            show: 150,
            hide: 0
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
                        JSPC.callbacks.dynPostFunc()
                    },
                    detachable: true,
                    autofocus: false,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        })

    <g:if test="${params.asAt && params.asAt.length() > 0}">$(function() { document.body.style.background = "#fcf8e3"; });</g:if>

    $("[data-ajaxTippId]").accordion().on('click', function(e) {
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
</laser:script>
<laser:htmlEnd/>
