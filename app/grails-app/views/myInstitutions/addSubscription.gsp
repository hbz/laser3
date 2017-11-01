<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.addSubscription.label', default:'Add Subscripton')}</title>
  </head>
  <body>

  <laser:breadcrumbs>
      <laser:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
      <laser:crumb controller="myInstitutions" action="addSubscription" params="${[shortcode:params.shortcode]}" text="${institution.name}" message="myinst.addSubscription.label" />
  </laser:breadcrumbs>

    <div class="container">
      <h1>${institution?.name} - ${message(code:'myinst.addSubscription.label', default:'Add Subscripton')}</h1>
      <g:render template="subsNav" contextPath="." />
    </div>

        
      <div class="container">
          <div class="pull-right">
              <g:form action="addSubscription" params="${[shortcode:params.shortcode]}" controller="myInstitutions" method="get" class="form-inline">
                  <label>${message(code:'default.search.text', default:'Search text')}</label>: <input type="text" name="q" placeholder="${message(code:'default.search.ph', default:'enter search term...')}"  value="${params.q?.encodeAsHTML()}"  />
                  <label>${message(code:'default.valid_on.label', default:'Valid On')}</label>: <input name="validOn" type="text" value="${validOn}"/>
                  <input type="submit" class="btn btn-primary" value="${message(code:'default.button.search.label', default:'Search')}" />
              </g:form>
          </div>
      </div>

    <div class="container">
        <g:if test="${packages}" >
          <g:form action="processAddSubscription" params="${[shortcode:params.shortcode]}" controller="myInstitutions" method="post">
 
            <div class="pull-left subscription-create">
            <g:if test="${is_inst_admin}">
              <select name="createSubAction"> 
                <option value="copy">${message(code:'myinst.addSubscription.copy_with_ent', default:'Copy With Entitlements')}</option>
                <option value="nocopy">${message(code:'myinst.addSubscription.copy_wo_ent', default:'Copy Without Entitlements')}</option>
                <input type="submit" class="btn disabled" value="${message(code:'myinst.addSubscription.button.create', default:'Create Subscription')}" />
            </g:if>
            <g:else>${message(code:'myinst.addLicense.no_permission')}</g:else>
            </div>
              
              <div class="clearfix"></div>
              
            <table class="table table-striped table-bordered subscriptions-list">
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
                    <td><g:if test="${p.license!=null}"><g:link controller="licenseDetails" action="index" id="${p.license.id}">${p.license.reference}</g:link></g:if></td>
                  </tr>
                  
                </g:each>
             </table>
          </g:form>
        </g:if>
  
        <div class="pagination" style="text-align:center">
          <g:if test="${packages}" >
            <bootstrap:paginate  action="addSubscription" controller="myInstitutions" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${num_pkg_rows}" />
          </g:if>
        </div>
    </div>
    <r:script type="text/javascript">
        $(document).ready(function() {
            var activateButton = function() {
                $('.subscription-create input').removeClass('disabled');
                $('.subscription-create input').addClass('btn-primary');
            }
            
            // Disables radio selection when using back button.
            $('.subscriptions-list input[type=radio]:checked').prop('checked', false);
            
            // Activates the create subscription button when a radio button is selected.
            $('.subscriptions-list input[type=radio]').click(activateButton);
        });
    </r:script>
  </body>
</html>
