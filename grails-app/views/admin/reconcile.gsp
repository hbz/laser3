<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} Data import explorer</title>
  </head>

  <body>

    <div>
        <ul class="breadcrumb">
        <li> <g:link controller="home">${message(code:'default.home.label')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller='admin' action='index'>Admin</g:link> <span class="divider">/</span> </li>
        <li class="active">Manage Affiliation Requests</li>
      </ul>
    </div>




    <div>

      <semui:messages data="${flash}" />

      <div>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Data Reconciliation</h1>
        <g:if test="${recon_status.active}">
          Data reconciliation currently in stage : ${recon_status.stage} (nn%)
        </g:if>
        <g:else>
          Data reconciliation not currently active. <g:link action="startReconciliation">Start</g:link>
        </g:else>

        <g:if test="${stats}">
           <table class="ui celled la-js-responsive-table la-table table">
            <g:each in="${stats}" var="s">
              <tr><td>${s.key}</td><td>${s.value}</td></tr>
            </g:each>
          </table>
        </g:if>
      </div>


    </div>




  </body>
</html>
