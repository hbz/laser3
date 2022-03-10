<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} Admin::TIPP Transfer</title>
  </head>
  <body>

      <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
        <semui:crumb text="TIPP Transfer" class="active"/>
      </semui:breadcrumbs>

      <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />TIPP Transfer</h1>

      <semui:messages data="${flash}" />

        <g:each in="${error}" var="err">
            <semui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${err}"/>
        </g:each>

        <g:if test="${success}">
            <semui:msg class="warning" text="Transfer Sucessful" />
        </g:if>

      <semui:form>
        <g:form action="tippTransfer" method="get" class="ui form">
          <p>Add the appropriate ID's below. All IssueEntitlements of source will be removed and transfered to target. Detailed information and confirmation will be presented before proceeding</p>
            <div class="control-group">
                <div class="field">
                    <label for="databaseIDofTipp">Database ID of TIPP</label>
                    <input type="text" id="databaseIDofTipp" name="sourceTIPP" value="${params.sourceTIPP}" />
              </div>
            </div>

            <div class="control-group">
                <div class="field">
                    <label for="databaseIDofTarget">Database ID of target TitleInstance</label>
                    <input type="text" id="databaseIDofTarget" name="targetTI" value="${params.targetTI}"/>
                </div>
            </div>
            <div class="field">
                  <button onclick="return confirm('Any existing TIs on TIPP will be replaced. Continue?')" class="ui button" type="submit">Transfer</button>
            </div>
        </g:form>
      </semui:form>

  </body>
</html>