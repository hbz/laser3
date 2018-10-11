<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Admin::IE Transfer</title>
  </head>
  <body>

      <semui:breadcrumbs>
          <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
          <semui:crumb text="IE Transfer" class="active"/>
      </semui:breadcrumbs>

      <h1 class="ui left aligned icon header"><semui:headerIcon />IE Transfer</h1>

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

				  <table class="ui celled la-table table">
			      <thead>
			        <th></th>
			        <th>(${params.sourceTIPP}) ${sourceTIPPObj.title.title}</th>
			        <th>(${params.targetTIPP}) ${targetTIPPObj.title.title}</th>
			      </thead>
			      <tbody>
			      <tr>
			      	<td><strong>Package</strong></td>
			      	<td>${sourceTIPPObj.pkg.name}</td>
			      	<td>${targetTIPPObj.pkg.name}</td>
			      </tr>
			      <tr>
			      	<td><strong>Start Date</strong> <br/><strong> End Date</strong></td>
			      	<td>
<g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${sourceTIPPObj.startDate}"/>
			      	 <br/>
<g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${sourceTIPPObj.endDate}"/>
 </td>
  			      	<td>
  <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${targetTIPPObj.startDate}"/>
 <br/>
 <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${targetTIPPObj.endDate}"/>
</td>
			      </tr>
			      <tr>
			      	<td><strong> Number of IEs</strong></td>
			      	<td>${com.k_int.kbplus.IssueEntitlement.countByTipp(sourceTIPPObj)}</td>
			      	<td>${com.k_int.kbplus.IssueEntitlement.countByTipp(targetTIPPObj)}</td>
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