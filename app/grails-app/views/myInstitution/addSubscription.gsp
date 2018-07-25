<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.addSubscription.label', default:'Add Subscripton')}</title>
  </head>
  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
      <semui:crumb controller="myInstitution" action="addSubscription" text="${institution?.getDesignation()}" message="myinst.addSubscription.label" />
    </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code:'myinst.addSubscription.label', default:'Add Subscripton')}</h1>

    <g:render template="subsNav" contextPath="." />

      <div>
          <div class="pull-right">
              <g:form action="addSubscription" controller="myInstitution" method="get" class="ui form">
                  <label>${message(code:'default.search.text', default:'Search text')}</label>: <input type="text" name="q" placeholder="${message(code:'default.search.ph', default:'enter search term...')}"  value="${params.q?.encodeAsHTML()}"  />
                  <label>${message(code:'default.valid_on.label', default:'Valid On')}</label>: <input name="validOn" type="text" value="${validOn}"/>
                  <input type="submit" class="ui button" value="${message(code:'default.button.filter.label', default:'Filter')}" />
              </g:form>
          </div>
      </div>

    <div>
        <g:if test="${packages}" >
          <g:form action="processAddSubscription" controller="myInstitution" method="post">
 
            <div class="pull-left subscription-create">
            <g:if test="${is_inst_admin}">
              <select name="createSubAction"> 
                <option value="copy">${message(code:'myinst.addSubscription.copy_with_ent', default:'Copy With Entitlements')}</option>
                <option value="nocopy">${message(code:'myinst.addSubscription.copy_wo_ent', default:'Copy Without Entitlements')}</option>
                <input type="submit" class="ui button disabled" value="${message(code:'myinst.addSubscription.button.create', default:'Create Subscription')}" />
            </g:if>
            <g:else>${message(code:'myinst.addLicense.no_permission')}</g:else>
            </div>
              
              <div class="clearfix"></div>
              
            <table class="ui sortable celled la-table table subscriptions-list">
                <tr>
                  <th>${message(code:'default.select.label', default:'Select')}</th>
                  <g:sortableColumn params="${params}" property="p.name" title="${message(code:'default.name.label', default:'Name')}" />
                  <th>${message(code:'consortium.plural', default:'Consortia')}</th>
                  <g:sortableColumn params="${params}" property="p.startDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
                  <g:sortableColumn params="${params}" property="p.endDate" title="${message(code:'default.endDate.label', default:'End Date')}" />
                  <th>${message(code:'tipp.platform', default:'Platform')}</th>
                  <th>${message(code:'license.label', default:'License')}</th>
                </tr>
                <g:each in="${packages}" var="p">
                  <tr>
                    <td><input type="radio" name="packageId" value="${p.id}"/></td>
                    <td>
                      <g:link controller="packageDetails" action="show" id="${p.id}">${p.name}</g:link>
                    </td>
                    <td>${p.getConsortia()?.name}</td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${p.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${p.endDate}"/></td>
                    <td>
                      ${p.nominalPlatform?.name}<br/>
                    </td>
                    <td><g:if test="${p.license!=null}"><g:link controller="licenseDetails" action="show" id="${p.license.id}">${p.license.reference}</g:link></g:if></td>
                  </tr>

                </g:each>
             </table>
          </g:form>
        </g:if>
  

          <g:if test="${packages}" >
            <semui:paginate  action="addSubscription" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${num_pkg_rows}" />
          </g:if>

    </div>
    <r:script type="text/javascript">
        $(document).ready(function() {
            var activateButton = function() {
                $('.subscription-create input').removeClass('disabled');
                $('.subscription-create input').addClass('ui button');
            }
            
            // Disables radio selection when using back button.
            $('.subscriptions-list input[type=radio]:checked').prop('checked', false);
            
            // Activates the create subscription button when a radio button is selected.
            $('.subscriptions-list input[type=radio]').click(activateButton);
        });
    </r:script>
  </body>
</html>
