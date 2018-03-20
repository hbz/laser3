<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.todo', default:'ToDo List')}</title>
  </head>

  <body>

  <semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <semui:crumb message="default.changeLog.label" class="active" />

    <li class="dropdown pull-right">
      <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">${message(code:'default.exports.label', default:'Exports')}<strong class="caret"></strong></a>
      <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
        <li><g:link controller="myInstitution" action="changeLog" params="${params+[format:'csv']}">CSV Export</g:link></li>
      </ul>
    </li>
  </semui:breadcrumbs>

    <div class="home-page">

      <div style="text-align:center">
        ${message(code:'myinst.changeLog.showing', args:[num_changes])}<br/>
        <semui:paginate  action="changeLog" controller="myInstitution" params="${params}" next="Next" prev="Prev" max="${max}" total="${num_changes}" /> <br/>
        <g:form method="get" action="changeLog" params="${params}">
          ${message(code:'myinst.changeLog.restrictTo', default:'Restrict to')}: <select name="restrict" onchange="this.form.submit()">
            <option value="">${message(code:'myinst.changelog.all', default:'ALL')}</option>
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
