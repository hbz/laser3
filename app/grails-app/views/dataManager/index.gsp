<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.datamanager.dash" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <div>
      <h2 class="ui header">${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</h2>
    </div>

    <g:if test="${pendingChanges?.size() > 0}">
      <div class="container alert-warn">
        <h6 class="ui header">${message(code:'datamanager.pending.label')}</h6>
        <table class="ui celled table">
          <thead>
            <tr>
              <td>${message(code:'default.info.label', default:'Info')}</td>
              <td>${message(code:'default.action.label', default:'Action')}</td>
            </tr>
          </thead>
          <tbody>
            <g:each in="${pendingChanges}" var="pc">
              <tr>
                <td><g:link controller="packageDetails" action="show" id="${pc.pkg.id}">${pc.pkg.name}</g:link> <br/>${pc.desc}</td>
                <td>
                  <g:link controller="pendingChange" action="accept" id="${pc.id}" class="ui positive button"><i class="icon-white icon-ok"></i>${message(code:'datamanager.accept.label')}</g:link>
                  <g:link controller="pendingChange" action="reject" id="${pc.id}" class="ui negative button"><i class="icon-white icon-remove"></i>${message(code:'datamanager.reject.label')}</g:link>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </g:if>
    <g:else>
      <div class="container alert-warn">
        <h6 class="ui header">${message(code:'datamanager.none_pending.label', default:'No pending package changes')}</h6>
      </div>
    </g:else>

  </body>
</html>
