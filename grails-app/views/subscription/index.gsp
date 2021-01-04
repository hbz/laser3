<%@ page import="de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.Subscription; de.laser.Package; de.laser.RefdataCategory; de.laser.helper.RDConstants" %>

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
    <div class="header"><g:message code="subscription.details.issueEntitlementEnrichment.label"/> </div>
    <p>
        <g:message code="subscription.details.issueEntitlementEnrichment.enrichmentProcess" args="[enrichmentProcess.issueEntitlements, enrichmentProcess.processCount, enrichmentProcess.processCountChangesCoverageDates, enrichmentProcess.processCountChangesPrice]"/>
    </p>
    </div>
</g:if>

<g:if test="${deletedSPs}">
    <div class="ui exclamation icon negative message">
        <i class="exclamation icon"></i>
        <ul class="list">
            <g:each in="${deletedSPs}" var="sp">
                <li><g:message code="subscription.details.packagesDeleted.header" args="${[sp.name]}"/> ${message(code:"subscription.details.packagesDeleted.entry",args:[raw(link(url:sp.link){'GOKb'})])}</li>
            </g:each>
        </ul>
    </div>
</g:if>

<div class="ui grid">

    <div class="row">
        <div class="column">

            <g:if test="${entitlements && entitlements.size() > 0}">
                ${message(code: 'subscription.entitlement.plural')} ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (entitlements.size())), num_sub_rows])}
                (<g:if test="${params.mode == 'advanced'}">
                    ${message(code: 'subscription.details.advanced.note')}
                    <g:link controller="subscription" action="index"
                            params="${params + ['mode': 'basic']}">${message(code: 'default.basic')}</g:link>
                    ${message(code: 'subscription.details.advanced.note.end')}
                </g:if>
                <g:else>
                    ${message(code: 'subscription.details.basic.note')}
                    <g:link controller="subscription" action="index" params="${params + ['mode': 'advanced']}"
                            type="button">${message(code: 'default.advanced')}</g:link>
                    ${message(code: 'subscription.details.basic.note.end')}
                </g:else>
                )
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

                <semui:msg class="warning" header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.warning" />
                <g:form class="ui form" controller="subscription" action="index"
                        params="${[sort: params.sort, order: params.order, filter: params.filter, pkgFilter: params.pkgfilter, startsBefore: params.startsBefore, endsAfter: params.endAfter, id: subscription.id]}"
                        method="post" enctype="multipart/form-data">
                    <div class="three fields">
                        <div class="field">
                            <div class="ui fluid action input">
                                <input type="text" readonly="readonly"
                                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                                <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"
                                       style="display: none;">

                                <div class="ui icon button">
                                    <i class="attach icon"></i>
                                </div>
                            </div>
                        </div>

                        <div class="field">
                            <div class="ui checkbox toggle">
                                <g:checkBox name="uploadCoverageDates" value="${uploadCoverageDates}"/>
                                <label><g:message code="subscription.details.issueEntitlementEnrichment.uploadCoverageDates.label"/></label>
                            </div>
                            <div class="ui checkbox toggle">
                                <g:checkBox name="uploadPriceInfo" value="${uploadPriceInfo}"/>
                                <label><g:message code="subscription.details.issueEntitlementEnrichment.uploadPriceInfo.label"/></label>
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
            <div class="ui top attached tabular menu">
                <g:link controller="subscription" action="index" id="${subscription.id}" class="item ${params.titleGroup ? '': 'active' }">
                    Alle
                    <span class="ui circular label">
                    ${num_ies}
                    </span>
                </g:link>

                <g:each in="${subscription.ieGroups.sort{it.name}}" var="titleGroup">
                    <g:link controller="subscription" action="index" id="${subscription.id}" params="[titleGroup: titleGroup.id]" class="item ${(params.titleGroup == titleGroup.id.toString()) ? 'active': '' }">
                        ${titleGroup.name}
                        <span class="ui circular label">
                            ${titleGroup.items.size()}
                        </span>
                    </g:link>
                </g:each>

            </div>

        <div class="ui bottom attached tab active segment">
    </g:if>

    <div class="row">
        <div class="column">

            <g:render template="/templates/filter/javascript" />
            <semui:filter showFilterButton="true">
                <g:form action="index" params="${params}" method="get" class="ui form">
                    <input type="hidden" name="sort" value="${params.sort}">
                    <input type="hidden" name="order" value="${params.order}">

                    <div class="three fields">
                        <div class="field">
                            <label for="filter">${message(code: 'default.search.text')}</label>
                            <input name="filter" id="filter" value="${params.filter}"/>
                        </div>

                        <div class="field">
                            <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                            <select class="ui dropdown" name="pkgfilter" id="pkgfilter">
                                <option value="">${message(code: 'subscription.details.from_pkg.all')}</option>
                                <g:each in="${subscription.packages}" var="sp">
                                    <option value="${sp.pkg.id}" ${sp.pkg.id.toString() == params.pkgfilter ? 'selected=true' : ''}>${sp.pkg.name}</option>
                                </g:each>
                            </select>
                        </div>
                        <g:if test="${params.mode != 'advanced'}">
                            <div class="field">
                                <semui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                                  value="${params.asAt}" placeholder="subscription.details.asAt.placeholder"/>
                            </div>
                        </g:if>
                    </div>
                    <div class="three fields">
                        <div class="field">
                            <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>

                            <select name="series_names" id="series_names" multiple="" class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${seriesNames}" var="seriesName">
                                    <option <%=(params.list('series_names').contains(seriesName)) ? 'selected="selected"' : ''%>
                                            value="${seriesName}">
                                        ${seriesName}
                                    </option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field">
                            <label for="subject_reference">${message(code: 'titleInstance.subjectReference.label')}</label>

                            <select name="subject_references" id="subject_reference" multiple="" class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${subjects}" var="subject">
                                    <option <%=(params.list('subject_references').contains(subject)) ? 'selected="selected"' : ''%>
                                            value="${subject}">
                                        ${subject}
                                    </option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field la-field-right-aligned">
                            <a href="${request.forwardURI}"
                               class="ui reset primary button">${message(code: 'default.button.filterreset.label')}</a>
                            <input type="submit" class="ui secondary button"
                                   value="${message(code: 'default.button.filter.label')}"/>
                        </div>
                    </div>
                </g:form>
            </semui:filter>

        </div>
    </div><!--.row-->


    <div class="row">
        <div class="column">

            <g:form action="subscriptionBatchUpdate" params="${[id: subscription.id]}" class="ui form">
                <g:set var="counter" value="${offset + 1}"/>
                <g:hiddenField name="sort" value="${params.sort}"/>
                <g:hiddenField name="order" value="${params.order}"/>
                <g:hiddenField name="offset" value="${params.offset}"/>
                <g:hiddenField name="max" value="${params.max}"/>

                <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header">
                    <thead>
                    <tr>
                        <th></th>
                        <th>${message(code: 'sidewide.number')}</th>
                        <g:sortableColumn class="eight wide" params="${params}" property="tipp.title.sortTitle"
                                          title="${message(code: 'title.label')}"/>
                        <th class="one wide">${message(code: 'subscription.details.print-electronic')}</th>
                        <th class="four wide">${message(code: 'subscription.details.coverage_dates')}</th>
                        <th class="two wide">${message(code: 'subscription.details.access_dates')}</th>
                        <th class="two wide"><g:message code="subscription.details.prices" /></th>
                        <g:if test="${subscription.ieGroups.size() > 0}">
                            <th class="two wide"><g:message code="subscription.details.ieGroups" /></th>
                        </g:if>
                        <th class="one wide"></th>
                    </tr>
                    <tr>
                        <th rowspan="2" colspan="4"></th>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="startDate"
                                          title="${message(code: 'default.from')}"/>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="accessStartDate"
                                          title="${message(code: 'default.from')}"/>

                        <th rowspan="2" colspan="2"></th>
                    </tr>
                    <tr>
                        <g:sortableColumn class="la-smaller-table-head" property="endDate"
                                          title="${message(code: 'default.to')}"/>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="accessEndDate"
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

                                    <div class="default text">${message(code:'default.select.choose.label')}</div>

                                    <div class="menu">
                                        <div class="item"
                                             data-value="edit">${message(code: 'default.edit.label', args: [selected_label])}</div>

                                        <div class="item"
                                             data-value="remove">${message(code: 'default.remove.label', args: [selected_label])}</div>
                                    </div>
                                </div>
                                <!--
                                <select id="bulkOperationSelect" name="bulkOperation" class="ui wide dropdown">
                                    <option value="edit">${message(code: 'default.edit.label', args: [selected_label])}</option>
                                    <option value="remove">${message(code: 'default.remove.label', args: [selected_label])}</option>
                                </select>
                                -->
                            </th>
                            <th>
                                <semui:simpleHiddenValue id="bulk_medium2" name="bulk_medium2" type="refdata" category="${RDConstants.IE_MEDIUM}"/>
                            </th>
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
                                    <select class="ui dropdown" name="titleGroup" id="titleGroup">
                                        <option value="">${message(code: 'default.select.choose.label')}</option>
                                        <g:each in="${subscription.ieGroups.sort{it.name}}" var="titleGroup">
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
                                    <semui:listIcon type="${ie.tipp.title.class.name}"/>
                                    <g:link controller="issueEntitlement" id="${ie.id}"
                                            action="show"><strong>${ie.tipp.title.title}</strong>
                                    </g:link>
                                    <g:if test="${ie.tipp.hostPlatformURL}">
                                        <semui:linkIcon href="${ie.tipp.hostPlatformURL.startsWith('http') ? ie.tipp.hostPlatformURL : 'http://' + ie.tipp.hostPlatformURL}"/>
                                    </g:if>
                                    <br />
                                    <!-- START TEMPLATE -->

                                    <g:render template="/templates/title" model="${[item: ie, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]}"/>
                                    <!-- END TEMPLATE -->
                                </td>

                                <td>
                                    ${ie.medium} <!-- may be subject of sync if issue entitlement medium and TIPP medium may differ -->
                                </td>
                                <td class="coverageStatements la-tableCard" data-entitlement="${ie.id}">
                                    <g:if test="${ie.tipp.title instanceof BookInstance}">

                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ie.tipp.title.dateFirstInPrint}"/>
                                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                           data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ie.tipp.title.dateFirstOnline}"/>

                                    </g:if>
                                    <g:elseif test="${ie.tipp.title instanceof JournalInstance}">
                                        <%
                                            Map<String, Object> paramData = [issueEntitlement: ie.id]
                                            if(params.sort && params.order) {
                                                paramData.sort = params.sort
                                                paramData.order = params.order
                                            }
                                            if(params.max && params.offset) {
                                                paramData.max = params.max
                                                paramData.offset = params.offset
                                            }
                                        %>
                                        <div class="ui cards">
                                        <g:each in="${ie.coverages}" var="covStmt">
                                            <div class="ui card">
                                                <g:render template="/templates/tipps/coverageStatement" model="${[covStmt: covStmt]}"/>
                                            </div>
                                        </g:each>
                                        </div>
                                        <g:if test="${editable}">
                                            <br />
                                            <g:link action="addCoverage" params="${paramData}" class="ui compact icon button positive tiny"><i class="ui icon plus" data-content="${message(code:'subscription.details.addCoverage')}"></i></g:link>
                                        </g:if>
                                    </g:elseif>


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
                                    <g:if test="${ie.priceItem}">
                                        <g:message code="tipp.listPrice"/>: <semui:xEditable field="listPrice" owner="${ie.priceItem}" format=""/> <semui:xEditableRefData field="listCurrency" owner="${ie.priceItem}" config="Currency"/> <%--<g:formatNumber number="${ie.priceItem.listPrice}" type="currency" currencyCode="${ie.priceItem.listCurrency.value}" currencySymbol="${ie.priceItem.listCurrency.value}"/>--%><br />
                                        <g:message code="tipp.localPrice"/>: <semui:xEditable field="localPrice" owner="${ie.priceItem}"/> <semui:xEditableRefData field="localCurrency" owner="${ie.priceItem}" config="Currency"/> <%--<g:formatNumber number="${ie.priceItem.localPrice}" type="currency" currencyCode="${ie.priceItem.localCurrency.value}" currencySymbol="${ie.priceItem.listCurrency.value}"/>--%>
                                        (<g:message code="tipp.priceDate"/> <semui:xEditable field="priceDate" type="date" owner="${ie.priceItem}"/> <%--<g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.priceItem.priceDate}"/>--%>)
                                    </g:if>
                                    <g:elseif test="${editable}">
                                        <g:link action="addEmptyPriceItem" class="ui icon positive button"
                                                params="${[ieid: ie.id, id: subscription.id]}">
                                            <i class="money icon la-popup-tooltip la-delay"
                                               data-content="${message(code: 'subscription.details.addEmptyPriceItem.info')}"></i>
                                        </g:link>
                                    </g:elseif>
                                </td>
                                <g:if test="${subscription.ieGroups.size() > 0}">
                                    <td>
                                        <div class="la-icon-list">
                                        <g:each in="${ie.ieGroups.sort{it.ieGroup.name}}" var="titleGroup">
                                            <div class="item">
                                                <i class="grey icon object group la-popup-tooltip la-delay" data-content="${message(code: 'issueEntitlementGroup.label')}"></i>
                                                <div class="content">
                                                <g:link controller="subscription" action="index" id="${subscription.id}" params="[titleGroup: titleGroup.ieGroup.id]" >${titleGroup.ieGroup.name}</g:link>
                                                </div>
                                            </div>
                                        </g:each>
                                        </div>
                                        <g:if test="${editable}">
                                            <div class="ui grid">
                                                <div class="right aligned wide column">
                                                    <g:link action="editEntitlementGroupItem" params="${[cmd:'edit', ie:ie.id, id: subscription.id]}" class="ui icon button trigger-modal"
                                                            data-tooltip="${message(code:'subscription.details.ieGroups.edit')}">
                                                        <i class="object group icon"></i>
                                                    </g:link>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                </g:if>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:link action="removeEntitlement" class="ui icon negative button"
                                                params="${[ieid: ie.id, sub: subscription.id]}"
                                                onClick="return confirm('${message(code: 'subscription.details.removeEntitlement.confirm')}');">
                                            <i class="trash alternate icon"></i>
                                        </g:link>
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
                    total="${num_sub_rows}"/>
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
      if ( $('#bulkOperationSelect').val() === 'remove' ) {
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
