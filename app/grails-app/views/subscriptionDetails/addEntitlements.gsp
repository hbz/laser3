<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Subscription" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>
    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
            <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"  text="${subscriptionInstance.name}" />
            <semui:crumb class="active" text="${message(code:'subscription.details.addEntitlements.label', default:'Add Entitlements')}" />
        </semui:breadcrumbs>
        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
        <h1 class="ui left aligned icon header"><semui:headerIcon />

            <g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name" class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
        </h1>

        <g:render template="nav" contextPath="." />

        <g:set var="counter" value="${offset+1}" />

      <semui:filter>
        ${message(code:'subscription.details.availableTitles', default:'Available Titles')} ( ${message(code:'default.paginate.offset', args:[(offset+1),(offset+(tipps?.size())),num_tipp_rows])} )
          <g:form class="ui form" action="addEntitlements" params="${params}" method="get">
            <input type="hidden" name="sort" value="${params.sort}">
            <input type="hidden" name="order" value="${params.order}">

              <div class="fields two">
                  <div class="field">
                      <label>${message(code:'subscription.compare.filter.title', default:'Filters - Title')}</label>
                      <input name="filter" value="${params.filter}"/>
                  </div>
                  <div class="field">
                      <label>${message(code:'subscription.details.from_pkg', default:'From Package')}</label>
                      <select name="pkgfilter">
                          <option value="">${message(code:'subscription.details.from_pkg.all', default:'All')}</option>
                          <g:each in="${subscriptionInstance.packages}" var="sp">
                              <option value="${sp.pkg.id}" ${sp.pkg.id.toString()==params.pkgfilter?'selected=true':''}>${sp.pkg.name}</option>
                          </g:each>
                    </select>
                  </div>
              </div>

              <div class="three fields">
                  <div class="field">
                    <semui:datepicker label="default.startsBefore.label" name="startsBefore" value="${params.startsBefore}" />
                  </div>
                  <div class="field">
                    <semui:datepicker label="default.endsAfter.label" name="endsAfter" value="${params.endsAfter}" />
                  </div>
                  <div class="field la-field-right-aligned">
                      <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                      <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                  </div>
              </div>

          </g:form>
      </semui:filter>
            <%
                List zdbIds = []
                List onlineIds = []
                List printIds = []
                if(identifiers) {
                    zdbIds = identifiers.zdbIds
                    onlineIds = identifiers.onlineIds
                    printIds = identifiers.printIds
                }
            %>
          <g:if test="${flash.error}">
              <semui:messages data="${flash}"/>
          </g:if>
          <g:form controller="subscriptionDetails" action="addEntitlements" params="${[identifiers:identifiers,sort:params.sort,order:params.order,filter:params.filter,pkgFilter:params.pkgfilter,startsBefore:params.startsBefore,endsAfter:params.endAfter,id:subscriptionInstance.id]}" method="post" enctype="multipart/form-data">
              <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"/>
              <input type="submit" value="${message(code:'subscription.details.addEntitlements.preselect', default:'Preselect Entitlements per KBART-File')}" class="ui button"/>
          </g:form>
          <g:form action="processAddEntitlements">
            <input type="hidden" name="siid" value="${subscriptionInstance.id}"/>
              <div class="paginateButtons" style="text-align:center">
                  <input type="submit" value="${message(code:'subscription.details.addEntitlements.add_selected', default:'Add Selected Entitlements')}" class="ui button"/>
              </div>
            <table class="ui celled stripped table">
              <thead>
                <tr>
                  <th rowspan="2" style="vertical-align:middle;">
                    <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" onClick="javascript:selectAll();"/></g:if>
                  </th>
                  <th rowspan="2">${message(code:'sidewide.number')}</th>
                  <g:sortableColumn rowspan="2" params="${params}" property="title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                  <th rowspan="2">ZDB-ID</th>
                  <th rowspan="2">ISSN / ISBN</th>
                  <th rowspan="2">eISSN / eISBN</th>
                  <th colspan="2">${message(code:'tipp.coverage')}</th>
                  <th colspan="2">${message(code:'tipp.access')}</th>
                  <th rowspan="2">${message(code:'tipp.embargo', default:'Embargo')}</th>
                  <th rowspan="2">${message(code:'tipp.coverageDepth', default:'Coverage Depth')}</th>
                  <th rowspan="2">${message(code:'tipp.coverageNote', default:'Coverage Note')}</th>
                  <th rowspan="2">${message(code:'default.actions.label')}</th>
                </tr>
                <tr>
                    <g:sortableColumn params="${params}" property="startDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
                    <g:sortableColumn params="${params}" property="endDate" title="${message(code:'default.endDate.label', default:'End Date')}" />
                    <g:sortableColumn params="${params}" property="accessStartDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
                    <g:sortableColumn params="${params}" property="accessEndDate" title="${message(code:'default.endDate.label', default:'End Date')}" />
                </tr>
              </thead>
              <tbody>
                <g:each in="${tipps}" var="tipp">
                  <%
                      String serial
                      String electronicSerial
                      String checked = ""
                      if(tipp.title.type.equals(RDStore.TITLE_TYPE_EBOOK)) {
                          serial = tipp.title.getIdentifierValue('ISBN')
                          electronicSerial = tipp?.title?.getIdentifierValue('eISBN')
                      }
                      else if(tipp.title.type.equals(RDStore.TITLE_TYPE_JOURNAL)) {
                          serial = tipp?.title?.getIdentifierValue('ISSN')
                          electronicSerial = tipp?.title?.getIdentifierValue('eISSN')
                      }
                      if(identifiers) {
                          if(zdbIds.indexOf(tipp.title.getIdentifierValue('zdb')) > -1) {
                              checked = "checked"
                          }
                          else if(onlineIds.indexOf(electronicSerial) > -1) {
                              checked = "checked"
                          }
                          else if(printIds.indexOf(serial) > -1) {
                              checked = "checked"
                          }
                      }
                  %>
                  <tr>
                    <td><input type="checkbox" name="_bulkflag.${tipp.id}" class="bulkcheck" ${checked}/></td>
                    <td>${counter++}</td>
                    <td>
                      ${tipp.title.type.getI10n("value")} –
                      <g:link controller="tipp" id="${tipp.id}" action="show">${tipp.title.title}</g:link>
                      <br/>
                      <span class="pull-right">
                        <g:if test="${tipp?.hostPlatformURL}"><a href="${tipp?.hostPlatformURL.contains('http') ?:'http://'+tipp?.hostPlatformURL}" TITLE="${tipp?.hostPlatformURL}">${message(code:'tipp.hostPlatformURL', default:'Host Link')}</a>
                            <a href="${tipp?.hostPlatformURL.contains('http') ?:'http://'+tipp?.hostPlatformURL}" TITLE="${tipp?.hostPlatformURL} (In new window)" target="_blank"><i class="icon-share-alt"></i></a></g:if>
                      </span>
                    </td>
                    <td style="white-space: nowrap;">${tipp.title.getIdentifierValue('zdb')}</td>
                    <td style="white-space: nowrap;">${serial}</td>
                    <td style="white-space: nowrap;">${electronicSerial}</td>
                    <td style="white-space: nowrap;"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tipp.startDate}"/></td>
                    <td style="white-space: nowrap;"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tipp.endDate}"/></td>
                    <td style="white-space: nowrap;"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tipp.accessStartDate}"/></td>
                    <td style="white-space: nowrap;"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tipp.accessEndDate}"/></td>
                    <td>${tipp.embargo}</td>
                    <td>${tipp.coverageDepth}</td>
                    <td>${tipp.coverageNote}</td>
                    <td>
                        <g:link class="ui icon positive button" action="processAddEntitlements" params="${[siid:subscriptionInstance.id,('_bulkflag.'+tipp.id):'Y']}" data-tooltip="${message(code:'subscription.details.addEntitlements.add_now', default:'Add now')}">
                            <i class="plus icon"></i>
                        </g:link>
                    </td>
                  </tr>
                </g:each>
              </tbody>
            </table>

            <div class="paginateButtons" style="text-align:center">
              <input type="submit" value="${message(code:'subscription.details.addEntitlements.add_selected', default:'Add Selected Entitlements')}" class="ui button"/>
            </div>


              <g:if test="${tipps}" >
                <%
                    params.remove("kbartPreselect")
                %>
                <semui:paginate controller="subscriptionDetails"
                                  action="addEntitlements" 
                                  params="${params+[identifiers:identifiers,pagination:true]}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}"
                                  max="${max}" 
                                  total="${num_tipp_rows}" />
              </g:if>

          </g:form>

    <r:script language="JavaScript">
      $(document).ready(function() {
        $('span.newipe').editable('<g:createLink controller="ajax" action="genericSetValue" />', {
          type      : 'textarea',
          cancel    : '${message(code:'default.button.cancel.label', default:'Cancel')}',
          submit    : '${message(code:'default.button.ok.label', default:'OK')}',
          id        : 'elementid',
          rows      : 3,
          tooltip   : '${message(code:'default.click_to_edit', default:'Click to edit...')}'
        });
      });

      function selectAll() {
        $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
      }

      $("simpleHiddenRefdata").editable({
          url: function(params) {
            var hidden_field_id = $(this).data('hidden-id');
            $("#"+hidden_field_id).val(params.value);
            // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
          }
        });
    </r:script>

  </body>
</html>
