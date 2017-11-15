<%@ page import="com.k_int.kbplus.Subscription" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>
  <body>

    <div>
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <g:if test="${params.shortcode}">
          <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}"> ${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="addEntitlements" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'subscription.details.addEntitlements.label', default:'Add Entitlements')}</g:link> </li>
        <g:if test="${editable}">
          <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
        </g:if>
      </ul>
    </div>

    <div>
      <h1><g:inPlaceEdit domain="Subscription" pk="${subscriptionInstance.id}" field="name" id="name" class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit></h1>
      <g:render template="nav" contextPath="." />
    </div>

    <g:set var="counter" value="${offset+1}" />

    <div>

      <dl>
        <dt>${message(code:'subscription.details.availableTitles', default:'Available Titles')} ( ${message(code:'default.paginate.offset', args:[(offset+1),(offset+(tipps?.size())),num_tipp_rows])} )
          <g:form action="addEntitlements" params="${params}" method="get" class="form-inline">
            <input type="hidden" name="sort" value="${params.sort}">
            <input type="hidden" name="order" value="${params.order}">
            <label>${message(code:'subscription.compare.filter.title', default:'Filters - Title')}:</label> <input name="filter" value="${params.filter}"/> &nbsp;
            <label>${message(code:'subscription.details.from_pkg', default:'From Package')}:</label> <select name="pkgfilter">
                               <option value="">${message(code:'subscription.details.from_pkg.all', default:'All')}</option>
                               <g:each in="${subscriptionInstance.packages}" var="sp">
                                 <option value="${sp.pkg.id}" ${sp.pkg.id.toString()==params.pkgfilter?'selected=true':''}>${sp.pkg.name}</option>
                               </g:each>
                            </select> &nbsp;
            &nbsp; <label>${message(code:'default.startsBefore.label', default:'Starts Before')}: </label>
            <g:simpleHiddenValue id="startsBefore" name="startsBefore" type="date" value="${params.startsBefore}"/>
            &nbsp; <label>${message(code:'default.endsAfter.label', default:'Ends After')}: </label>
            <g:simpleHiddenValue id="endsAfter" name="endsAfter" type="date" value="${params.endsAfter}"/>

            <input type="submit" style="margin-left:10px;" class="ui primary button" value="${message(code:'default.button.submit.label', default:'Submit')}">
          </g:form>
        </dt>
        <dd>
          <g:form action="processAddEntitlements">
            <input type="hidden" name="siid" value="${subscriptionInstance.id}"/>
            <table  class="table table-striped table-bordered columns10">
              <thead>
                <tr>
                  <th style="vertical-align:middle;">
                    <g:if test="${editable}"><input type="checkbox" name="chkall" onClick="javascript:selectAll();"/></g:if>
                  </th>
                  <th>#</th>
                  <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                  <th>ISSN</th>
                  <th>eISSN</th>
                  <g:sortableColumn params="${params}" property="startDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
                  <g:sortableColumn params="${params}" property="endDate" title="${message(code:'default.endDate.label', default:'End Date')}" />
                  <th>${message(code:'tipp.embargo', default:'Embargo')}</th>
                  <th>${message(code:'tipp.coverageDepth', default:'Coverage Depth')}</th>
                  <th>${message(code:'tipp.coverageNote', default:'Coverage Note')}</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${tipps}" var="tipp">
                  <tr>
                    <td><input type="checkbox" name="_bulkflag.${tipp.id}" class="bulkcheck"/></td>
                    <td>${counter++}</td>
                    <td>
                      <g:link controller="tipp" id="${tipp.id}" action="show">${tipp.title.title}</g:link>
                      <br/>
                      <span class="pull-right">
                        <g:if test="${tipp?.hostPlatformURL}"><a href="${tipp?.hostPlatformURL}" TITLE="${tipp?.hostPlatformURL}">${message(code:'tipp.hostPlatformURL', default:'Host Link')}</a>
                            <a href="${tipp?.hostPlatformURL}" TITLE="${tipp?.hostPlatformURL} (In new window)" target="_blank"><i class="icon-share-alt"></i></a> &nbsp;| &nbsp;</g:if>
                            <g:link action="processAddEntitlements" 
                                    params="${[siid:subscriptionInstance.id,('_bulkflag.'+tipp.id):'Y']}"
                                    class="pull-right">${message(code:'subscription.details.addEntitlements.add_now', default:'Add now')}</g:link>
                      </span>
                    </td>
                    <td style="white-space: nowrap;">${tipp?.title?.getIdentifierValue('ISSN')}</td>
                    <td style="white-space: nowrap;">${tipp?.title?.getIdentifierValue('eISSN')}</td>
                    <td style="white-space: nowrap;"><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${tipp.startDate}"/></td>
                    <td style="white-space: nowrap;"><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${tipp.endDate}"/></td>
                    <td>${tipp.embargo}</td>
                    <td>${tipp.coverageDepth}</td>
                    <td>${tipp.coverageNote}</td>
                  </tr>
                </g:each>
              </tbody>
            </table>

            <div class="paginateButtons" style="text-align:center">
              <input type="submit" value="${message(code:'subscription.details.addEntitlements.add_selected', default:'Add Selected Entitlements')}" class="ui primary button"/>
            </div>


            <div class="pagination" style="text-align:center">
              <g:if test="${tipps}" >
                <bootstrap:paginate controller="subscriptionDetails" 
                                  action="addEntitlements" 
                                  params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}"
                                  max="${max}" 
                                  total="${num_tipp_rows}" />
              </g:if>
            </div>
          </g:form>
        </dd>
      </dl>
    </div>

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
        $('.bulkcheck').attr('checked', true);
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
