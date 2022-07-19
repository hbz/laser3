<%@ page import="de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.Subscription; de.laser.Package; de.laser.RefdataCategory; de.laser.helper.RDConstants" %>
<laser:serviceInjection/>
<%-- r:require module="annotations" --%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.current_ent')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>
<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<semui:modeSwitch controller="subscription" action="index" params="${params}"/>

<semui:messages data="${flash}"/>

<g:if test="${params.asAt}"><h1
        class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${message(code: 'subscription.details.snapshot', args: [params.asAt])}</h1></g:if>

<h1 class="ui icon header la-noMargin-top"><semui:headerIcon/>
<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="iconSubscriptionIsChild"/>
</g:if>
<semui:xEditable owner="${subscription}" field="name"/>
</h1>
<semui:anualRings object="${subscription}" controller="subscription" action="index"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="nav"/>

<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="message"/>
</g:if>

<g:if test="${enrichmentProcess}">
    <div class="ui positive message">
        <i class="close icon"></i>

        <div class="header"><g:message code="subscription.details.issueEntitlementEnrichment.label"/></div>

        <p>
            <g:message code="subscription.details.issueEntitlementEnrichment.enrichmentProcess"
                       args="[enrichmentProcess.issueEntitlements, enrichmentProcess.processCount, enrichmentProcess.processCountChangesCoverageDates, enrichmentProcess.processCountChangesPrice]"/>
        </p>
    </div>
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
                <li><g:message code="subscription.details.frozenHoldings.header" args="${[sp.name]}"/> ${message(code: "subscription.details.frozenHoldings.entry")}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<div class="ui grid">

    <div class="row">
        <div class="column">

            <g:if test="${entitlements && entitlements.size() > 0}">

                <g:if test="${subscription.packages.size() > 1}">
                    <a class="ui right floated button" data-href="#showPackagesModal" data-semui="modal"><g:message
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

                    <semui:msg class="warning" header="${message(code: "message.attention")}"
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
        </div>
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
                        ${titleGroup.items.size()}
                    </span>
                </g:link>
            </g:each>

        </div>

        <div class="ui bottom attached tab active segment">
    </g:if>

    <div class="row">
        <div class="column">

            <g:render template="/templates/filter/tipp_ieFilter"/>

        </div>
    </div><!--.row-->

    <div class="row">
        <div class="column">

            <div class="ui blue large label"><g:message code="title.plural"/>: <div class="detail">${num_ies_rows}</div>
            </div>

            <g:form action="subscriptionBatchUpdate" params="${[id: subscription.id]}" class="ui form">
                <g:set var="counter" value="${offset + 1}"/>
                <g:hiddenField name="sub" value="${subscription.id}"/>
                <g:each in="${considerInBatch}" var="key">
                    <g:hiddenField name="${key}" value="${params[key]}"/>
                </g:each>

                <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header">
                    <thead>
                    <tr>
                        <th></th>
                        <th>${message(code: 'sidewide.number')}</th>
                        <g:sortableColumn class="eight wide" params="${params}" property="tipp.sortname"
                                          title="${message(code: 'title.label')}"/>
                        <%-- legacy ??? <th class="one wide">${message(code: 'subscription.details.print-electronic')}</th>--%>
                        <th class="four wide">${message(code: 'subscription.details.date_header')}</th>
                        <th class="two wide">${message(code: 'subscription.details.access_dates')}</th>
                        <th class="two wide"><g:message code="subscription.details.prices"/></th>
                        <g:if test="${subscription.ieGroups.size() > 0}">
                            <th class="two wide"><g:message code="subscription.details.ieGroups"/></th>
                        </g:if>
                        <th class="one wide"></th>
                    </tr>
                    <tr>
                        <th rowspan="2" colspan="3"></th>
                        <g:if test="${journalsOnly}">
                            <g:sortableColumn class="la-smaller-table-head" params="${params}" property="startDate"
                                              title="${message(code: 'default.from')}"/>
                        </g:if>
                        <g:else>
                            <g:sortableColumn class="la-smaller-table-head" params="${params}" property="tipp.dateFirstInPrint"
                                              title="${message(code: 'tipp.dateFirstInPrint')}"/>
                        </g:else>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="tipp.accessStartDate"
                                          title="${message(code: 'default.from')}"/>
                        <th rowspan="2" colspan="2"></th>
                    </tr>
                    <tr>
                        <g:if test="${journalsOnly}">
                            <g:sortableColumn class="la-smaller-table-head" params="${params}" property="endDate"
                                              title="${message(code: 'default.to')}"/>
                        </g:if>
                        <g:else>
                            <g:sortableColumn class="la-smaller-table-head" params="${params}" property="tipp.dateFirstOnline"
                                              title="${message(code: 'tipp.dateFirstOnline')}"/>
                        </g:else>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="tipp.accessEndDate"
                                          title="${message(code: 'default.to')}"/>
                    </tr>
                    <tr>
                        <g:if test="${editable}">
                            <th>
                                <input id="select-all" type="checkbox" name="chkall" onClick="JSPC.app.selectAll()"/>
                            </th>
                            <th colspan="2">
                                <g:set var="selected_label" value="${message(code: 'default.selected.label')}"/>
                                <div class="ui selection fluid dropdown la-clearable">
                                    <input type="hidden" id="bulkOperationSelect" name="bulkOperation">
                                    <i class="dropdown icon"></i>

                                    <div class="default text">${message(code: 'default.select.choose.label')}</div>

                                    <div class="menu">
                                        <div class="item" data-value="edit">${message(code: 'default.edit.label', args: [selected_label])}</div>
                                        <div class="item" data-value="remove">${message(code: 'default.remove.label', args: [selected_label])}</div>
                                        <g:if test="${institution.getCustomerType() == 'ORG_CONSORTIUM'}">
                                            <div class="item" data-value="removeWithChildren">${message(code: 'subscription.details.remove.withChildren.label')}</div>
                                        </g:if>
                                    </div>
                                </div>
                                <!--
                                <select id="bulkOperationSelect" name="bulkOperation" class="ui wide dropdown">
                                    <option value="edit">${message(code: 'default.edit.label', args: [selected_label])}</option>
                                    <option value="remove">${message(code: 'default.remove.label', args: [selected_label])}</option>
                                </select>
                                -->
                            </th>
                            <%-- legacy??
                            <th>
                                <semui:simpleHiddenValue id="bulk_medium2" name="bulk_medium2" type="refdata"
                                                         category="${RDConstants.IE_MEDIUM}"/>
                            </th>--%>
                            <th>
                                <%--<semui:datepicker hideLabel="true"
                                                  placeholder="${message(code: 'default.from')}"
                                                  inputCssClass="la-input-small" id="bulk_start_date"
                                                  name="bulk_start_date"/>


                                <semui:datepicker hideLabel="true"
                                                  placeholder="${message(code: 'default.to')}"
                                                  inputCssClass="la-input-small" id="bulk_end_date"
                                                  name="bulk_end_date"/>--%>
                            </th>
                            <th>
                                <semui:datepicker hideLabel="true"
                                                  placeholder="${message(code: 'default.from')}"
                                                  inputCssClass="la-input-small" id="bulk_access_start_date"
                                                  name="bulk_access_start_date"/>


                                <semui:datepicker hideLabel="true"
                                                  placeholder="${message(code: 'default.to')}"
                                                  inputCssClass="la-input-small" id="bulk_access_end_date"
                                                  name="bulk_access_end_date"/>
                            </th>
                            <th>

                            </th>
                            <g:if test="${subscription.ieGroups.size() > 0}">
                                <th class="two wide">
                                    <select class="ui dropdown" name="titleGroupInsert" id="titleGroupInsert">
                                        <option value="">${message(code: 'default.select.choose.label')}</option>
                                        <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                                            <option value="${titleGroup.id}">${titleGroup.name}</option>
                                        </g:each>
                                    </select>
                                </th>
                            </g:if>
                            <th>

                                <button data-position="top right"
                                        data-content="${message(code: 'default.button.apply_batch.label')}"
                                        type="submit" onClick="return JSPC.app.confirmSubmit()"
                                        class="ui icon button la-popup-tooltip la-delay"><i class="check icon"></i>
                                </button>

                            </th>
                        </g:if>
                        <g:else>
                            <g:if test="${subscription.ieGroups.size() > 0}">
                                <th colspan="10"></th>
                            </g:if>
                            <g:else>
                                <th colspan="9"></th>
                            </g:else>
                        </g:else>
                    </tr>
                    </thead>
                    <tbody>

                    <g:if test="${entitlements}">

                        <g:each in="${entitlements}" var="ie">
                            <tr>
                                <td><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${ie.id}"
                                                                    class="bulkcheck"/></g:if></td>
                                <td>${counter++}</td>
                                <td>
                                    <!-- START TEMPLATE -->
                                    <g:render template="/templates/title_short"
                                              model="${[ie: ie, tipp: ie.tipp,
                                                        showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false]}"/>
                                    <!-- END TEMPLATE -->
                                </td>
                                <%-- legacy???
                                <td>
                                    ${ie.tipp.medium}
                                </td>
                                --%>
                                <td class="coverageStatements la-tableCard" data-entitlement="${ie.id}">

                                    <g:render template="/templates/tipps/coverages" model="${[ie: ie, tipp: ie.tipp]}"/>

                                </td>
                                <td>
                                <!-- von --->
                                    <g:if test="${editable}">
                                        <semui:xEditable owner="${ie}" type="date" field="accessStartDate"/>
                                        <i class="grey question circle icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                                    </g:if>
                                    <g:else>
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ie.accessStartDate}"/>
                                    </g:else>
                                    <semui:dateDevider/>
                                <!-- bis -->
                                    <g:if test="${editable}">
                                        <semui:xEditable owner="${ie}" type="date" field="accessEndDate"/>
                                        <i class="grey question circle icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                                    </g:if>
                                    <g:else>
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ie.accessEndDate}"/>
                                    </g:else>
                                </td>
                                <td>
                                    <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                        <g:message code="tipp.price.listPrice"/>: <semui:xEditable field="listPrice"
                                                                                             owner="${priceItem}"
                                                                                             format=""/> <semui:xEditableRefData
                                            field="listCurrency" owner="${priceItem}"
                                            config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                        <g:message code="tipp.price.localPrice"/>: <semui:xEditable field="localPrice"
                                                                                              owner="${priceItem}"/> <semui:xEditableRefData
                                            field="localCurrency" owner="${priceItem}"
                                            config="Currency"/> <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                                    <%--<semui:xEditable field="startDate" type="date"
                                                     owner="${priceItem}"/><semui:dateDevider/><semui:xEditable
                                        field="endDate" type="date"
                                        owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>

                                        <g:if test="${editable}">
                                            <span class="right floated" >
                                                <g:link controller="subscription" action="removePriceItem" params="${[priceItem: priceItem.id, id: subscription.id]}" class="ui compact icon button negative tiny"><i class="ui icon minus" data-content="Preis entfernen"></i></g:link>
                                            </span>
                                        </g:if>
                                        <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                    </g:each>
                                    <g:if test="${editable && ie.priceItems.size() < 1 }">
                                        <g:link action="addEmptyPriceItem" class="ui icon blue button la-modern-button"
                                                params="${[ieid: ie.id, id: subscription.id]}">
                                            <i class="money icon la-popup-tooltip la-delay"
                                               data-content="${message(code: 'subscription.details.addEmptyPriceItem.info')}"></i>
                                        </g:link>
                                    </g:if>
                                </td>
                                <g:if test="${subscription.ieGroups.size() > 0}">
                                    <td>
                                        <div class="la-icon-list">
                                            <g:each in="${ie.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                                                <div class="item">
                                                    <i class="grey icon object group la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'issueEntitlementGroup.label')}"></i>

                                                    <div class="content">
                                                        <g:link controller="subscription" action="index"
                                                                id="${subscription.id}"
                                                                params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                                                    </div>
                                                </div>
                                            </g:each>
                                        </div>
                                        <g:if test="${editable}">
                                            <div class="ui grid">
                                                <div class="right aligned wide column">
                                                    <g:link action="editEntitlementGroupItem"
                                                            params="${[cmd: 'edit', ie: ie.id, id: subscription.id]}"
                                                            class="ui icon button trigger-modal la-popup-tooltip la-delay"
                                                            data-content="${message(code: 'subscription.details.ieGroups.edit')}">
                                                        <i class="object group icon"></i>
                                                    </g:link>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                </g:if>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:if test="${subscription.ieGroups.size() > 0}">
                                            <g:link action="removeEntitlementWithIEGroups" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                    params="${[ieid: ie.id, sub: subscription.id]}"
                                                    role="button"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.entitlementWithIEGroups", args: [ie.name])}"
                                                    data-confirm-term-how="delete"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <g:link action="removeEntitlement" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                    params="${[ieid: ie.id, sub: subscription.id]}"
                                                    role="button"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.entitlement", args: [ie.name])}"
                                                    data-confirm-term-how="delete"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
                                            </g:link>
                                        </g:else>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    </tbody>
                </table>
            </g:form>

        </div>
    </div><!--.row-->
    <g:if test="${subscription.ieGroups.size() > 0}">
        </div>
    </g:if>

</div>

<g:if test="${entitlements}">
    <semui:paginate action="index" controller="subscription" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${num_ies_rows}"/>
</g:if>


<div id="magicArea">
</div>

<g:render template="export/individuallyExportIEsModal" model="[modalID: 'individuallyExportIEsModal']" />

<semui:modal id="showPackagesModal" message="subscription.packages.label" hideSubmitButton="true">
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

</semui:modal>


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
                        r2d2.initDynamicSemuiStuff('#editEntitlementGroupItemModal');
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
</laser:script>
</body>
</html>
