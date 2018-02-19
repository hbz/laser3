<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} - ${message(code:'license.copy')}</title>
  </head>
  <body>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
        <semui:crumb controller="myInstitution" action="currentLicenses" message="license.current" />
        <semui:crumb message="license.copy" class="active" />
    </semui:breadcrumbs>

    <g:render template="actions" />

    <h1 class="ui header">${institution?.name} - ${message(code:'license.copy')}</h1>

  <!--
    <semui:subNav actionName="${actionName}">
      <semui:subNavItem controller="myInstitution" action="currentLicenses" message="license.current" />
      <semui:subNavItem controller="myInstitution" action="addLicense" message="license.copy" />
      <g:if test="${is_inst_admin}">
        <semui:subNavItem controller="myInstitution" action="cleanLicense" message="license.add.blank" />
      </g:if>
    </semui:subNav>-->

    <div class="license-searches">
        <div>
            <div class="span6">&nbsp;
                <!--
                <input type="text" name="keyword-search" placeholder="enter search term..." />
                <input type="submit" class="ui button" value="Search" />
                -->
            </div>
            <div class="span6">
            </div>
        </div>
    </div>

      <semui:filter>
        <g:form action="addLicense" params="${params}" method="get" class="ui form">
          <input type="hidden" name="sort" value="${params.sort}">
          <input type="hidden" name="order" value="${params.order}">
          <label>${message(code:'default.filter.plural', default:'Filters')} - ${message(code:'license.name')}:</label> <input name="filter" value="${params.filter}"/> &nbsp;
          <input type="submit" class="ui button" value="${message(code:'default.button.submit.label')}" />
        </g:form>
      </semui:filter>

  <!--
      <div>
          <div class="well license-options">
            <g:if test="${is_inst_admin}">
              <input type="submit" name="copy-license" value="${message(code:'default.button.copySelected.label', default:'Copy Selected')}" class="ui negative button" />
            </g:if>
            <g:else>${message(code:'myinst.addLicense.no_permission', default:'Sorry, you must have editor role to be able to add licenses')}</g:else>
          </div>
      </div>
-->
      <semui:messages data="${flash}" />

      <g:if test="${licenses?.size() > 0}">
        <div class="license-results">
          <table class="ui sortable celled la-table table">
            <thead>
              <tr>
                <g:sortableColumn params="${params}" property="reference" title="${message(code:'license.name')}" />
                <th>${message(code:'license.licensor.label', default:'Licensor')}</th>
                <g:sortableColumn params="${params}" property="startDate" title="${message(code:'default.startDate.label', default:'Start Date')}" />
                <g:sortableColumn params="${params}" property="endDate" title="${message(code:'default.endDate.label', default:'End Date')}" />
                <th>${message(code:'default.actions.label', default:'Action')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${licenses}" var="l">
                <tr>
                  <td>
                    <g:link action="index"
                              controller="licenseDetails" 
                              id="${l.id}">
                              <g:if test="${l.reference}">${l.reference}</g:if>
                              <g:else>${message(code:'myinst.addLicense.no_ref', args:[l.id])}</g:else>
                    </g:link>
                    <g:if test="${l.pkgs && ( l.pkgs.size() > 0 )}">
                      <ul>
                        <g:each in="${l.pkgs}" var="pkg">
                          <li><g:link controller="packageDetails" action="show" id="${pkg.id}">${pkg.id} (${pkg.name})</g:link><br/></li>
                        </g:each>
                      </ul>
                    </g:if>
                    <g:else>
                      <br/>${message(code:'myinst.addLicense.no_results', default:'No linked packages.')}
                    </g:else>
                  </td>
                  <td>${l.licensor?.name}</td>
                  <td><g:formatDate formatName="default.date.format.notime" date="${l.startDate}"/></td>
                  <td><g:formatDate formatName="default.date.format.notime" date="${l.endDate}"/></td>
                  <td><g:link controller="myInstitution" action="actionLicenses" params="${[baselicense:l.id, 'copy-license':'Y']}" class="ui positive button">${message(code:'default.button.copy.label', default:'Copy')}</g:link></td>
                </tr>
              </g:each>
            </tbody>
          </table>


            <g:if test="${licenses}" >
              <semui:paginate  action="addLicense" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${numLicenses}" />
            </g:if>

        </div>

      </g:if>

    <r:script type="text/javascript">
        $('.license-results input[type="radio"]').click(function () {
            $('.license-options').slideDown('fast');
        });

        $('.license-options .delete-license').click(function () {
            $('.license-results input:checked').each(function () {
                $(this).parent().parent().fadeOut('slow');
                $('.license-options').slideUp('fast');
            })
        })
    </r:script>

  </body>
</html>
