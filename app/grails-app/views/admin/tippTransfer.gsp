<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Admin::TIPP Transfer</title>
  </head>
  <body>

      <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
        <semui:crumb text="TIPP Transfer" class="active"/>
      </semui:breadcrumbs>

      <h1 class="ui header">TIPP Transfer</h1>

      <semui:messages data="${flash}" />

        <g:each in="${error}" var="err">
          <bootstrap:alert class="alert-danger">${err}</bootstrap:alert>
        </g:each>

        <g:if test="${success}">
          <bootstrap:alert class="alert-info">Transfer Sucessful</bootstrap:alert>
        </g:if>

      <semui:form>
        <g:form action="tippTransfer" method="get" class="ui form">
          <p>Add the appropriate ID's below. All IssueEntitlements of source will be removed and transfered to target. Detailed information and confirmation will be presented before proceeding</p>
            <div class="control-group">
                <div class="field">
                    <label>Database ID of TIPP</label>
                    <input type="text" name="sourceTIPP" value="${params.sourceTIPP}" />
              </div>
            </div>

            <div class="control-group">
                <div class="field">
                    <label>Database ID of target TitleInstance</label>
                    <input type="text" name="targetTI" value="${params.targetTI}"/>
                </div>
            </div>
            <div class="field">
                  <button onclick="return confirm('Any existing TIs on TIPP will be replaced. Continue?')" class="ui button" type="submit">Transfer</button>
            </div>
        </g:form>
      </semui:form>

  </body>
</html>