<%@ page import="de.laser.IssueEntitlement" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Admin::IE Transfer</title>
  </head>
  <body>

      <semui:breadcrumbs>
          <semui:crumb message="menu.admin" controller="admin" action="index" />
          <semui:crumb text="IE Transfer" class="active"/>
      </semui:breadcrumbs>

      <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />IE Transfer</h1>

      <semui:messages data="${flash}" />

        <semui:form>
        <g:form action="ieTransfer" method="get" class="ui form">
          <p>Add the appropriate ID's below. All IssueEntitlements of source will be removed and transfered to target. Detailed information and confirmation will be presented before proceeding</p>

            <div class="control-group">
                <div class="field">
                    <label>Database ID of IE source TIPP</label>
                    <input type="text" name="sourceTIPP" value="${params.sourceTIPP}" />
                </div>
            </div>

            <div class="control-group">
                <div class="field">
                    <label>Database ID of target TIPP</label>
                    <input type="text" name="targetTIPP" value="${params.targetTIPP}"/>
                </div>
            </div>

 			<g:if test="${sourceTIPPObj && targetTIPPObj}">

				  <table class="ui celled la-js-responsive-table la-table table">
			      <thead>
                  <tr>
			        <th></th>
			        <th>(${params.sourceTIPP}) ${sourceTIPPObj.title.title}</th>
			        <th>(${params.targetTIPP}) ${targetTIPPObj.title.title}</th>
                  </tr>
			      </thead>
			      <tbody>
			      <tr>
			      	<td><strong>Package</strong></td>
			      	<td>${sourceTIPPObj.pkg.name}</td>
			      	<td>${targetTIPPObj.pkg.name}</td>
			      </tr>
			      <tr>
			      	<td><strong>Start Date</strong> <br /><strong> End Date</strong></td>
			      	<td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sourceTIPPObj.startDate}"/><br />
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sourceTIPPObj.endDate}"/>
                    </td>
  			      	<td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${targetTIPPObj.startDate}"/><br />
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${targetTIPPObj.endDate}"/>
                    </td>
			      </tr>
			      <tr>
			      	<td><strong> Number of IEs</strong></td>
			      	<td>${IssueEntitlement.countByTipp(sourceTIPPObj)}</td>
			      	<td>${IssueEntitlement.countByTipp(targetTIPPObj)}</td>
			      </tr>
			      </tbody>
			      </table>

                <div class="field">
                    <button onclick="return confirm('All source IEs will be moved to target. Continue?')" class="ui positive button" name="transfer" type="submit" value="Go">Transfer</button>
                </div>
  			</g:if>

            <button class="ui button" type="submit" value="Go">Look Up TIPP Info...</button>
          </dl>
        </g:form>
        </semui:form>
  </body>
</html>