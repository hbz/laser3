<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.todo')}</title>
  </head>

  <body>

  <semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <semui:crumb message="default.changeLog.label" class="active" />
  </semui:breadcrumbs>


  <semui:controlButtons>
    <semui:exportDropdown>
      <semui:exportDropdownItem>
        <g:link controller="myInstitution" class="item" action="changeLog" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
      </semui:exportDropdownItem>
    </semui:exportDropdown>
    <g:render template="actions"/>
  </semui:controlButtons>


    <div class="home-page">

      <div>
        ${message(code:'myinst.changeLog.showing', args:[num_changes])}<br/>
        <semui:paginate  action="changeLog" controller="myInstitution" params="${params}" next="Next" prev="Prev" max="${max}" total="${num_changes}" /> <br/>
        <g:form method="get" action="changeLog" params="${params}">
          ${message(code:'myinst.changeLog.restrictTo', default:'Restrict to')}:
          <select class="ui dropdown" name="restrict" onchange="this.form.submit()">
            <option value="${message(code:'myinst.changelog.all', default:'ALL')}">${message(code:'myinst.changelog.all', default:'ALL')}</option>
            <g:each in="${institutional_objects}" var="io">
              <option value="${io[0]}" ${(params.restrict?.equals(io[0]) ? 'selected' : '')}>${io[1]}</option>
            </g:each>
          </select>
        </g:form>
      </div>

      <table class="ui celled la-table table">
        <g:each in="${changes}" var="chg">
          <tr>
            <td><g:formatDate format="yyyy-MM-dd" date="${chg.ts}"/>
             
            </td>
            <td>
              <g:if test="${chg.subscription != null}">${message(code:'subscription.change.to')} <g:link controller="subscriptionDetails" action="index" id="${chg.subscription.id}">${chg.subscription.id} </g:link></g:if>
              <g:if test="${chg.license != null}">${message(code:'license.change.to')} <g:link controller="licenseDetails" action="show" id="${chg.license.id}">${chg.license.id}</g:link></g:if>
              <g:if test="${chg.pkg != null}">${message(code:'package.change.to')} <g:link controller="packageDetails" action="show" id="${chg.package.id}">${chg.package.id}</g:link></g:if>
            </td>
            <td>
              ${chg.desc}
              ${chg.status} on ${chg.actionDate} by ${chg.user?.displayName}
            </td>
          </tr>
        </g:each>
      </table>


        <semui:paginate  action="changeLog" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_changes}" />


    </div>


  </body>
</html>
