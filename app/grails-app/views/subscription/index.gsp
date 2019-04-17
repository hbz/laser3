<%@ page import="com.k_int.kbplus.Subscription" %>
<r:require module="annotations" />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.label', default:'Subscription Details')}</title>
</head>
<body>

<g:render template="breadcrumb" model="${[ params:params ]}"/>
<semui:controlButtons>
    <g:render template="actions" />
</semui:controlButtons>

<semui:modeSwitch controller="subscription" action="index" params="${params}" />

<semui:messages data="${flash}" />

<g:if test="${params.asAt}"><h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'subscription.details.snapshot', args:[params.asAt])}</h1></g:if>

<h1 class="ui icon header"><semui:headerIcon />
    <semui:xEditable owner="${subscriptionInstance}" field="name" />
</h1>
<semui:anualRings object="${subscriptionInstance}" controller="subscription" action="index" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="nav" />

<g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>

<g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
    <g:render template="message" />
</g:if>

<div class="ui grid">

    <div class="row">
        <div class="column">

                <g:if test="${entitlements?.size() > 0}">
                    ${message(code:'subscription.entitlement.plural')} ${message(code:'default.paginate.offset', args:[(offset+1),(offset+(entitlements?.size())),num_sub_rows])}. (
                    <g:if test="${params.mode=='advanced'}">
                        ${message(code:'subscription.details.advanced.note', default:'Includes Expected or Expired entitlements, switch to')}
                        <g:link controller="subscription" action="index" params="${params+['mode':'basic']}">${message(code:'default.basic', default:'Basic')}</g:link>
                        ${message(code:'subscription.details.advanced.note.end', default:'view to hide them')}
                    </g:if>
                    <g:else>
                        ${message(code:'subscription.details.basic.note', default:'Expected or Expired entitlements are filtered, use')}
                        <g:link controller="subscription" action="index" params="${params+['mode':'advanced']}" button type="button" >${message(code:'default.advanced', default:'Advanced')}</g:link>
                        ${message(code:'subscription.details.basic.note.end', default:'view to see them')}
                    </g:else>
                    )
                </g:if>
                <g:else>
                    ${message(code:'subscription.details.no_ents', default:'No entitlements yet')}
                </g:else>

        </div>
    </div><!--.row-->

    <div class="row">
        <div class="column">

            <semui:filter>
                <g:form action="index" params="${params}" method="get" class="ui form">
                    <input type="hidden" name="sort" value="${params.sort}">
                    <input type="hidden" name="order" value="${params.order}">

                    <div class="three fields">
                        <div class="field">
                            <label>${message(code:'default.filter.label', default:'Filter')}</label>
                            <input  name="filter" value="${params.filter}"/>
                        </div>
                        <div class="field">
                            <label>${message(code:'subscription.details.from_pkg', default:'From Package')}</label>
                            <select class="ui dropdown" name="pkgfilter">
                                <option value="">${message(code:'subscription.details.from_pkg.all', default:'All')}</option>
                                <g:each in="${subscriptionInstance.packages}" var="sp">
                                    <option value="${sp.pkg.id}" ${sp.pkg.id.toString()==params.pkgfilter?'selected=true':''}>${sp.pkg.name}</option>
                                </g:each>
                            </select>
                        </div>
                        <g:if test="${params.mode!='advanced'}">
                            <div class="field">
                                <semui:datepicker label="subscription.details.asAt" id="asAt" name="asAt" value="${params.asAt}" />
                            </div>
                        </g:if>
                        <div class="field la-field-right-aligned">
                            <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                            <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filtern')}" />
                        </div>
                    </div>
                </g:form>
            </semui:filter>

        </div>
    </div><!--.row-->


    <div class="row">
        <div class="column">

            <g:form action="subscriptionBatchUpdate" params="${[id:subscriptionInstance?.id]}" class="ui form">
                <g:set var="counter" value="${offset+1}" />
                <g:hiddenField name="sort" value="${params.sort}"/>
                <g:hiddenField name="order" value="${params.order}"/>
                <g:hiddenField name="offset" value="${params.offset}"/>
                <g:hiddenField name="max" value="${params.max}"/>

                <table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
                    <thead>
                    <tr>
                        <th ></th>
                        <th >${message(code:'sidewide.number')}</th>
                        <g:sortableColumn class="ten wide" params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                        <th class="one wide">${message(code:'subscription.details.print-electronic')}</th>
                        <th class="two wide">${message(code:'subscription.details.coverage_dates')}</th>
                        <th class="two wide">${message(code:'subscription.details.access_dates', default:'Access')}</th>
                        <th class="one wide"></th>
                    </tr>
                    <tr>
                        <th rowspan="2" colspan="4"></th>
                        <g:sortableColumn  class="la-smaller-table-head" params="${params}" property="startDate" title="${message(code:'default.from', default:'Earliest date')}" />
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="accessStartDate" title="${message(code:'default.from', default:'Earliest date')}" />

                        <th rowspan="2"></th>
                    </tr>
                    <tr>
                        <g:sortableColumn class="la-smaller-table-head" property="endDate" title="${message(code:'default.to', default:'Latest Date')}" />
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="accessEndDate" title="${message(code:'default.to', default:'Latest Date')}" />
                    </tr>
                    <tr>
                        <g:if test="${editable}">
                            <th>
                                <input id="select-all" type="checkbox" name="chkall" onClick="javascript:selectAll();"/>
                            </th>
                            <th colspan="2">
                                <g:set var="selected_label" value="${message(code:'default.selected.label')}" />
                                <div class="ui selection fluid dropdown la-clearable">
                                    <input type="hidden" id="bulkOperationSelect" name="bulkOperation">
                                    <i class="dropdown icon"></i>
                                    <div class="default text">Bitte auswählen</div>
                                    <div class="menu">
                                        <div class="item" data-value="edit">${message(code:'default.edit.label', args:[selected_label], default:'Edit Selected')}</div>
                                        <div class="item" data-value="remove">${message(code:'default.remove.label', args:[selected_label], default:'Remove Selected')}</div>
                                    </div>
                                </div>
                                <!--
                                <select id="bulkOperationSelect" name="bulkOperation" class="ui wide dropdown">
                                    <option value="edit">${message(code:'default.edit.label', args:[selected_label], default:'Edit Selected')}</option>
                                    <option value="remove">${message(code:'default.remove.label', args:[selected_label], default:'Remove Selected')}</option>
                                </select>
                                -->
                            </th>
                            <th>
                                <g:simpleHiddenRefdata id="bulk_medium" name="bulk_medium" refdataCategory="IEMedium"/>
                            </th>
                            <th>
                                <semui:datepicker hideLabel="true" placeholder="${message(code:'default.from', default:'Earliest date')}" inputCssClass="la-input-small"  id="bulk_start_date" name="bulk_start_date"/>


                                <semui:datepicker hideLabel="true" placeholder="${message(code:'default.to', default:'Latest Date')}" inputCssClass="la-input-small"  id="bulk_end_date" name="bulk_end_date"/>
                            </th>
                            <th>
                                <semui:datepicker hideLabel="true" placeholder="${message(code:'default.from', default:'Earliest date')}" inputCssClass="la-input-small"  id="bulk_access_start_date" name="bulk_access_start_date"/>


                                <semui:datepicker hideLabel="true" placeholder="${message(code:'default.to', default:'Latest Date')}" inputCssClass="la-input-small"  id="bulk_access_end_date" name="bulk_access_end_date"/>
                            </th>
                            <th>

                                <button data-position="top right"  data-content="${message(code:'default.button.apply_batch.label', default:'Apply Batch Changes')}" type="submit" onClick="return confirmSubmit()" class="ui icon button la-popup-tooltip la-delay"><i class="check icon"></i></button>

                            </th>
                        </g:if>
                        <g:else>
                            <th colspan="9">  </th>
                        </g:else>
                    </tr>
                    </thead>
                    <tbody>

                    <g:if test="${entitlements}">
                        <g:each in="${entitlements}" var="ie">
                            <tr>
                                <td><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${ie.id}" class="bulkcheck"/></g:if></td>
                                <td>${counter++}</td>
                                <td>
                                    <semui:listIcon type="${ie.tipp?.title?.type?.value}"/>
                                    <g:link controller="issueEntitlement" id="${ie.id}" action="show"><strong>${ie.tipp.title.title}</strong></g:link>

                                    <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance && ie?.tipp?.title?.volume}">
                                        (${message(code: 'title.volume.label')} ${ie?.tipp?.title?.volume})
                                    </g:if>

                                    <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance && (ie?.tipp?.title?.firstAuthor || ie?.tipp?.title?.firstEditor)}">
                                        <br><b>${ie?.tipp?.title?.getEbookFirstAutorOrFirstEditor()} ${message(code: 'title.firstAuthor.firstEditor.label')}</b>
                                    </g:if>

                                    <br>

                                    <g:each in="${ie?.tipp?.title?.ids.sort{it.identifier.ns.ns}}" var="title_id">
                                        <g:if test="${title_id.identifier.ns.ns.toLowerCase() != 'originediturl'}">
                                            <span class="ui small teal image label">
                                                ${title_id.identifier.ns.ns}: <div class="detail">${title_id.identifier.value}</div>
                                            </span>
                                        </g:if>
                                    </g:each>
                                    <br/>
                                    <!--                  ISSN:<strong>${ie?.tipp?.title?.getIdentifierValue('ISSN') ?: ' - '}</strong>,
                  eISSN:<strong>${ie?.tipp?.title?.getIdentifierValue('eISSN') ?: ' - '}</strong><br/>-->
                                    <div class="ui list">
                                        <div class="item"><b>${message(code:'default.access.label', default:'Access')}:</b> ${ie.availabilityStatus?.getI10n('value')}</div>
                                        <div class="item"><b>${message(code:'title.type.label')}:</b> ${ie?.tipp?.title?.type?.getI10n('value')}</div>
                                        <div class="item"><b>${message(code:'tipp.coverageNote', default:'Coverage Note')}:</b> ${ie.coverageNote?:(ie.tipp?.coverageNote ?: '')}</div>

                                       <div class="item"><b>${message(code:'tipp.package', default:'Package')}:</b>
                                            <div class="la-flexbox">
                                                <i class="icon gift scale la-list-icon"></i>
                                                <g:link controller="package" action="show" id="${ie?.tipp?.pkg?.id}">${ie?.tipp?.pkg?.name}</g:link>
                                            </div>
                                        </div>
                                        <div class="item"><b>${message(code:'tipp.platform', default:'Platform')}:</b>
                                            <g:if test="${ie.tipp?.platform.name}">
                                                ${ie.tipp?.platform.name}
                                            </g:if>
                                            <g:else>${message(code:'default.unknown')}</g:else>

                                            <g:if test="${ie.tipp?.platform.name}">
                                                <g:link  class="ui icon mini  button la-js-dont-hide-button la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.changePlattform')}" controller="platform" action="show" id="${ie.tipp?.platform.id}"><i class="pencil alternate icon"></i></g:link>
                                            </g:if>
                                            <g:if test="${ie.tipp?.hostPlatformURL}">
                                                <a class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.callUrl')}" href="${ie.tipp?.hostPlatformURL.contains('http') ? ie.tipp?.hostPlatformURL :'http://'+ie.tipp?.hostPlatformURL}" target="_blank"><i class="share square icon"></i></a>
                                            </g:if>

                                            <g:if test="${ie.availabilityStatus?.value=='Expected'}">
                                                ${message(code:'default.on', default:'on')} <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessStartDate}"/>
                                            </g:if>
                                            <g:if test="${ie.availabilityStatus?.value=='Expired'}">
                                                ${message(code:'default.on', default:'on')} <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessEndDate}"/>
                                            </g:if>
                                        </div>
                                    <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance}">
                                        <div class="item"><b>${message(code: 'title.editionStatement.label')}:</b> ${ie?.tipp?.title?.editionStatement}
                                        </div>
                                    </g:if>
                                    </div>
                                </td>

                                <td>
                                    <semui:xEditableRefData owner="${ie}" field="medium" config='IEMedium'/>
                                </td>
                                <td>
                                    <!-- von --->
                                    <semui:xEditable owner="${ie}" type="date" field="startDate" /><br>
                                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.volume')}"></i>
                                    <semui:xEditable owner="${ie}" field="startVolume"/><br>

                                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.issue')}"></i>
                                    <semui:xEditable owner="${ie}" field="startIssue"/>
                                    <semui:dateDevider/>
                                   <!-- bis -->
                                    <semui:xEditable owner="${ie}" type="date" field="endDate" /><br>
                                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.volume')}"></i>
                                    <semui:xEditable owner="${ie}" field="endVolume"/><br>

                                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.issue')}"></i>
                                    <semui:xEditable owner="${ie}" field="endIssue"/>
                                </td>
                                <td>
                                    <!-- von --->
                                    <g:if test="${editable}">
                                        <semui:xEditable owner="${ie}" type="date" field="accessStartDate" />
                                        <i class="grey question circle icon la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.access_start.note', default:'Leave empty to default to sub start date')}"></i>
                                    </g:if>
                                    <g:else>
                                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessStartDate}"/>
                                    </g:else>
                                    <semui:dateDevider/>
                                    <!-- bis -->
                                    <g:if test="${editable}">
                                        <semui:xEditable owner="${ie}" type="date" field="accessEndDate" />
                                        <i class="grey question circle icon la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.access_end.note', default:'Leave empty to default to sub end date')}"></i>
                                    </g:if>
                                    <g:else>
                                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessEndDate}"/>
                                    </g:else>
                                </td>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:link action="removeEntitlement" class="ui icon negative button" params="${[ieid:ie.id, sub:subscriptionInstance.id]}" onClick="return confirm(${message(code:'subscription.details.removeEntitlement.confirm', default:'Are you sure you wish to delete this entitlement?')});">
                                            <i class="trash alternate icon"></i>
                                        </g:link>
                                    </g:if>

                                <!-- Review for use in LAS:eR
                  <g:if test="${institutional_usage_identifier}">
                                    <g:if test="${ie?.tipp?.title?.getIdentifierValue('ISSN')}">
                                        | <a href="https://www.jusp.mimas.ac.uk/secure/v2/ijsu/?id=${institutional_usage_identifier.value}&issn=${ie?.tipp?.title?.getIdentifierValue('ISSN')}">ISSN Usage</a>
                                    </g:if>
                                    <g:if test="${ie?.tipp?.title?.getIdentifierValue('eISSN')}">
                                        | <a href="https://www.jusp.mimas.ac.uk/secure/v2/ijsu/?id=${institutional_usage_identifier.value}&issn=${ie?.tipp?.title?.getIdentifierValue('eISSN')}">eISSN Usage</a>
                                    </g:if>
                                </g:if>
                                -->
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

<g:if test="${entitlements}" >
    <semui:paginate  action="index" controller="subscription" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_sub_rows}" />
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
        var agree=confirm('${message(code:'default.continue.confirm', default:'Are you sure you wish to continue?')}');
          if (agree)
            return true ;
          else
            return false ;
        }
      }
</g:if>

    <g:if test="${params.asAt && params.asAt.length() > 0}"> $(function() {
        document.body.style.background = "#fcf8e3";
      });</g:if>

    $('.la-books.icon')
      .popup({
        delay: {
            show: 150,
            hide: 0
        }
      });
    ;
    $('.la-notebook.icon')
      .popup({
        delay: {
            show: 150,
            hide: 0
        }
      });
    ;
</r:script>
</body>
</html>
