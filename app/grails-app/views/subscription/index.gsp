<%@ page import="com.k_int.kbplus.Subscription; com.k_int.kbplus.BookInstance; com.k_int.kbplus.Package; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.ApiSource;de.laser.helper.RDConstants" %>

<%-- r:require module="annotations" --%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
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
<semui:xEditable owner="${subscriptionInstance}" field="name"/>
</h1>
<semui:anualRings object="${subscriptionInstance}" controller="subscription" action="index"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="nav"/>

<g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id in [subscriptionInstance.getConsortia()?.id,subscriptionInstance.getCollective()?.id])}">
    <g:render template="message"/>
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

            <g:if test="${entitlements?.size() > 0}">
                ${message(code: 'subscription.entitlement.plural')} ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (entitlements?.size())), num_sub_rows])}. (
                <g:if test="${params.mode == 'advanced'}">
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

    <div class="row">
        <div class="column">

            <g:render template="../templates/filter/javascript" />
            <semui:filter showFilterButton="true">
                <g:form action="index" params="${params}" method="get" class="ui form">
                    <input type="hidden" name="sort" value="${params.sort}">
                    <input type="hidden" name="order" value="${params.order}">

                    <div class="three fields">
                        <div class="field">
                            <label for="filter">${message(code: 'default.filter.label')}</label>
                            <input name="filter" id="filter" value="${params.filter}"/>
                        </div>

                        <div class="field">
                            <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                            <select class="ui dropdown" name="pkgfilter" id="pkgfilter">
                                <option value="">${message(code: 'subscription.details.from_pkg.all')}</option>
                                <g:each in="${subscriptionInstance.packages}" var="sp">
                                    <option value="${sp.pkg.id}" ${sp.pkg.id.toString() == params.pkgfilter ? 'selected=true' : ''}>${sp.pkg.name}</option>
                                </g:each>
                            </select>
                        </div>
                        <g:if test="${params.mode != 'advanced'}">
                            <div class="field">
                                <semui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                                  value="${params.asAt}"/>
                            </div>
                        </g:if>
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

            <g:form action="subscriptionBatchUpdate" params="${[id: subscriptionInstance?.id]}" class="ui form">
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
                                <input id="select-all" type="checkbox" name="chkall" onClick="javascript:selectAll();"/>
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
                                <g:simpleHiddenRefdata id="bulk_medium" name="bulk_medium" refdataCategory="${RDConstants.IE_MEDIUM}"/>
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
                            <th>

                                <button data-position="top right"
                                        data-content="${message(code: 'default.button.apply_batch.label')}"
                                        type="submit" onClick="return confirmSubmit()"
                                        class="ui icon button la-popup-tooltip la-delay"><i class="check icon"></i>
                                </button>

                            </th>
                        </g:if>
                        <g:else>
                            <th colspan="9"></th>
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
                                    <br>
                                    <!-- START TEMPLATE -->

                                    <g:render template="../templates/title" model="${[item: ie, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]}"/>
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
                                    <g:elseif test="${ie.tipp.title instanceof com.k_int.kbplus.JournalInstance}">
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
                                            <br>
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
                                        <g:message code="tipp.listPrice"/>: <semui:xEditable field="listPrice" owner="${ie.priceItem}"/> <semui:xEditableRefData field="listCurrency" owner="${ie.priceItem}" config="Currency"/> <%--<g:formatNumber number="${ie.priceItem.listPrice}" type="currency" currencyCode="${ie.priceItem.listCurrency.value}" currencySymbol="${ie.priceItem.listCurrency.value}"/>--%><br>
                                        <g:message code="tipp.localPrice"/>: <semui:xEditable field="localPrice" owner="${ie.priceItem}"/> <semui:xEditableRefData field="localCurrency" owner="${ie.priceItem}" config="Currency"/> <%--<g:formatNumber number="${ie.priceItem.localPrice}" type="currency" currencyCode="${ie.priceItem.localCurrency.value}" currencySymbol="${ie.priceItem.listCurrency.value}"/>--%>
                                        (<g:message code="tipp.priceDate"/> <semui:xEditable field="priceDate" type="date" owner="${ie.priceItem}"/> <%--<g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.priceItem.priceDate}"/>--%>)
                                    </g:if>
                                    <g:elseif test="${editable}">
                                        <g:link action="addEmptyPriceItem" class="ui icon positive button"
                                                params="${[ieid: ie.id, id: subscriptionInstance.id]}">
                                            <i class="money icon"></i>
                                        </g:link>
                                    </g:elseif>
                                </td>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:link action="removeEntitlement" class="ui icon negative button"
                                                params="${[ieid: ie.id, sub: subscriptionInstance.id]}"
                                                onClick="return confirm('${message(code: 'subscription.details.removeEntitlement.confirm', default: 'Are you sure you wish to delete this entitlement?')}');">
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
</div>

<g:if test="${entitlements}">
    <semui:paginate action="index" controller="subscription" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${num_sub_rows}"/>
</g:if>


<div id="magicArea">
</div>


<r:script language="JavaScript">
      function hideModal(){
        $("[name='coreAssertionEdit']").modal('hide');
      }

      function showCoreAssertionModal(){

        $("[name='coreAssertionEdit']").modal('show');

      }

      <g:if test="${editable}">

    function selectAll() {
      $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
    }

    function confirmSubmit() {
      if ( $('#bulkOperationSelect').val() === 'remove' ) {
        var agree=confirm('${message(code: 'default.continue.confirm')}');
          if (agree)
            return true ;
          else
            return false ;
        }
      }
</g:if>

    <g:if test="${params.asAt && params.asAt.length() > 0}">$(function() {
        document.body.style.background = "#fcf8e3";
      });</g:if>

    $('.la-books.icon')
      .popup({
        delay: {
            show: 150,
            hide: 0
        }
      });

    $('.la-notebook.icon')
      .popup({
        delay: {
            show: 150,
            hide: 0
        }
      });
</r:script>
</body>
</html>
