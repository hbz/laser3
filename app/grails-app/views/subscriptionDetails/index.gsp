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

  <semui:modeSwitch controller="subscriptionDetails" action="index" params="${params}" />

  <semui:messages data="${flash}" />

  <g:if test="${params.asAt}"><h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'subscription.details.snapshot', args:[params.asAt])}</h1></g:if>

  <h1 class="ui left aligned icon header"><semui:headerIcon />
    <semui:xEditable owner="${subscriptionInstance}" field="name" />
    <semui:anualRings object="${subscriptionInstance}" controller="subscriptionDetails" action="index" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
  </h1>
    <g:render template="nav" />

    <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>

      <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
          <div class="ui negative message">
              <div class="header"><g:message code="myinst.message.attention" /></div>
              <p>
                  <g:message code="myinst.subscriptionDetails.message.ChildView" />
                  <span class="ui label">${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.name}.join(',')}</span>.
              <g:message code="myinst.subscriptionDetails.message.ConsortialView" />
              <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}"><g:message code="myinst.subscriptionDetails.message.here" /></g:link>.
              </p>
          </div>
      </g:if>

    <div class="ui grid">

        <div class="row">
            <div class="column">

                <g:annotatedLabel owner="${subscriptionInstance}" property="entitlements">
                    <g:if test="${entitlements?.size() > 0}">
                        ${message(code:'subscription.entitlement.plural')} ${message(code:'default.paginate.offset', args:[(offset+1),(offset+(entitlements?.size())),num_sub_rows])}. (
                        <g:if test="${params.mode=='advanced'}">
                          ${message(code:'subscription.details.advanced.note', default:'Includes Expected or Expired entitlements, switch to')}
                          <g:link controller="subscriptionDetails" action="index" params="${params+['mode':'basic']}">${message(code:'default.basic', default:'Basic')}</g:link>
                          ${message(code:'subscription.details.advanced.note.end', default:'view to hide them')}
                        </g:if>
                        <g:else>
                          ${message(code:'subscription.details.basic.note', default:'Expected or Expired entitlements are filtered, use')}
                          <g:link controller="subscriptionDetails" action="index" params="${params+['mode':'advanced']}" button type="button" >${message(code:'default.advanced', default:'Advanced')}</g:link>
                          ${message(code:'subscription.details.basic.note.end', default:'view to see them')}
                        </g:else>
                      )
                    </g:if>
                    <g:else>
                      ${message(code:'subscription.details.no_ents', default:'No entitlements yet')}
                    </g:else>
                </g:annotatedLabel>

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
                                <label>
                                    <g:annotatedLabel owner="${subscriptionInstance}" property="qryFilter"> ${message(code:'default.filter.label', default:'Filter')} </g:annotatedLabel>
                                </label>
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
                                    <semui:datepicker label="subscription.details.asAt" name="asAt" value="${params.asAt}" />
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
                  <th rowspan="2"></th>
                  <th rowspan="2">${message(code:'sidewide.number')}</th>
                  <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                  <th rowspan="2">${message(code:'subscription.details.print-electronic')}</th>
                  <g:sortableColumn params="${params}" property="startDate" title="${message(code:'subscription.details.startDate', default:'Earliest date')}" />
              <% /*
                  <g:sortableColumn params="${params}" property="core_status" title="${message(code:'subscription.details.core_status', default:'Core Status')}" />
              */ %>
                  <th rowspan="2">${message(code:'default.actions.label', default:'Actions')}</th>
                </tr>

                <tr>

                  <th>${message(code:'subscription.details.access_dates', default:'Access Dates')}</th>
                  <g:sortableColumn params="${params}" property="endDate" title="${message(code:'subscription.details.endDate', default:'Latest Date')}" />
                  <% /*<th> ${message(code:'subscription.details.core_medium', default:'Core Medium')} </th>*/ %>
                </tr>

                <tr>
                    <g:if test="${editable}">

                      <th style="vertical-align:middle;">
                        <input id="select-all" type="checkbox" name="chkall" onClick="javascript:selectAll();"/>
                      </th>

                      <th colspan="2">
                        <g:set var="selected_label" value="${message(code:'default.selected.label')}" />
<%--
                            <div class="ui radio checkbox">
                                <input name="bulkOperation" value="edit" tabindex="0" class="hidden" type="radio">
                                <label>${message(code:'default.edit.label', args:[selected_label], default:'Edit Selected')}</label>
                            </div>
                            <div class="ui radio checkbox">
                                <input name="bulkOperation" value="remove" tabindex="0" class="hidden" type="radio">
                                <label>${message(code:'default.remove.label', args:[selected_label], default:'Remove Selected')}</label>
                            </div>
--%>
                          <select id="bulkOperationSelect" name="bulkOperation" style="width:50%; float:left">
                            <option value="edit">${message(code:'default.edit.label', args:[selected_label], default:'Edit Selected')}</option>
                            <option value="remove">${message(code:'default.remove.label', args:[selected_label], default:'Remove Selected')}</option>
                          </select>

                          <input type="Submit" value="${message(code:'default.button.apply_batch.label', default:'Apply Batch Changes')}" onClick="return confirmSubmit()" class="ui button"/>
                      </th>

                      <th>
                          <g:simpleHiddenRefdata id="bulk_medium" name="bulk_medium" refdataCategory="IEMedium"/>
                      </th>

                      <th> <semui:simpleHiddenValue id="bulk_start_date" name="bulk_start_date" type="date"/>  <br/>
                           <semui:simpleHiddenValue id="bulk_end_date" name="bulk_end_date" type="date"/>
                      </th>
                        <% /*
                      <th>
                        <g:simpleHiddenRefdata id="bulk_coreStatus" name="bulk_coreStatus" refdataCategory="CoreStatus"/> <br/>
                      </th>
                      */ %>
                    </g:if>
                    <g:else>
                        <th colspan="7">  </th>
                    </g:else>
                    <th></th>
                </tr>
            </thead>
         <tbody>

          <g:if test="${entitlements}">
            <g:each in="${entitlements}" var="ie">
              <tr>
                <td><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${ie.id}" class="bulkcheck"/></g:if></td>
                <td>${counter++}</td>
                <td>
                    <semui:listIcon type="${ie.tipp?.title.type.getI10n('value')}"/>
                    <g:link controller="issueEntitlement" id="${ie.id}" action="show"><strong>${ie.tipp.title.title}</strong></g:link><br>
                    <g:if test="${ie.tipp?.hostPlatformURL}">

                        <a href="${ie.tipp?.hostPlatformURL}" TITLE="${ie.tipp?.hostPlatformURL}" target="_blank">${message(code:'tipp.hostPlatformURL', default:'Host Link')}  <i class="ui icon share square"></i></a>

                    </g:if> <br/>
                  <g:each in="${ie?.tipp?.title?.ids.sort{it.identifier.ns.ns}}" var="title_id">
                    <g:if test="${title_id.identifier.ns.ns.toLowerCase() != 'originediturl'}">
                      ${title_id.identifier.ns.ns}: <strong>${title_id.identifier.value}</strong>
                    </g:if>
                  </g:each>
                  <br/>
<!--                  ISSN:<strong>${ie?.tipp?.title?.getIdentifierValue('ISSN') ?: ' - '}</strong>,
                  eISSN:<strong>${ie?.tipp?.title?.getIdentifierValue('eISSN') ?: ' - '}</strong><br/>-->
                   ${message(code:'default.access.label', default:'Access')}: ${ie.availabilityStatus?.getI10n('value')}<br/>
                   ${message(code:'tipp.coverageNote', default:'Coverage Note')}: ${ie.coverageNote?:(ie.tipp?.coverageNote ?: '')}<br/>
                    ${message(code:'tipp.platform', default:'Platform')}:
                    <g:if test="${ie.tipp?.platform.name}">
                        <g:link controller="platform" action="show" id="${ie.tipp?.platform.id}">${ie.tipp?.platform.name}</g:link>
                    </g:if>
                    <g:else>${message(code:'default.unknown')}</g:else>
                   <g:if test="${ie.availabilityStatus?.value=='Expected'}">
                     ${message(code:'default.on', default:'on')} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessStartDate}"/>
                   </g:if>
                   <g:if test="${ie.availabilityStatus?.value=='Expired'}">
                     ${message(code:'default.on', default:'on')} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessEndDate}"/>
                   </g:if>
                   <g:if test="${params.mode=='advanced' && editable}">
                     <br/> ${message(code:'subscription.details.access_start', default:'Access Start')}: <semui:xEditable owner="${ie}" type="date" field="accessStartDate" /> (${message(code:'subscription.details.access_start.note', default:'Leave empty to default to sub start date')})
                     <br/> ${message(code:'subscription.details.access_end', default:'Access End')}: <semui:xEditable owner="${ie}" type="date" field="accessEndDate" /> (${message(code:'subscription.details.access_end.note', default:'Leave empty to default to sub end date')})
                   </g:if>

                </td>
              
                <td>
                  <semui:xEditableRefData owner="${ie}" field="medium" config='IEMedium'/>
                </td>
                <td>
                    <semui:xEditable owner="${ie}" type="date" field="startDate" /><br/>
                    <semui:xEditable owner="${ie}" type="date" field="endDate" />
                </td>
                <% /*
                <td>
                <g:render template="/templates/coreStatus" model="${['issueEntitlement': ie, 'date': params.asAt]}"/>
               <br/>

               <semui:xEditableRefData owner="${ie}" field="coreStatus" config='CoreStatus'/>
                </td>
                */ %>
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
      <semui:paginate  action="index" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_sub_rows}" />
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
      
    </r:script>
  </body>
</html>
