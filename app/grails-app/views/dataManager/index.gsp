<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>KB+ ${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</title>
  </head>

  <body>

    <div class="container">
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="dataManager" action="index">${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</g:link> </li>
      </ul>
    </div>

    <g:if test="${flash.message}">
      <div class="container">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </div>
    </g:if>

    <g:if test="${flash.error}">
      <div class="container">
        <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
      </div>
    </g:if>

    <div class="container">
      <h2>${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</h2>
    </div>

    <g:if test="${pendingChanges?.size() > 0}">
      <div class="container alert-warn">
        <h6>${message(code:'datamanager.pending.label')}</h6>
        <table class="table table-bordered">
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
                  <g:link controller="pendingChange" action="accept" id="${pc.id}" class="btn btn-success"><i class="icon-white icon-ok"></i>${message(code:'datamanager.accept.label')}</g:link>
                  <g:link controller="pendingChange" action="reject" id="${pc.id}" class="btn btn-danger"><i class="icon-white icon-remove"></i>${message(code:'datamanager.reject.label')}</g:link>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </g:if>
    <g:else>
      <div class="container alert-warn">
        <h6>${message(code:'datamanager.none_pending.label', default:'No pending package changes')}</h6>
      </div>
    </g:else>

  </body>
</html>
