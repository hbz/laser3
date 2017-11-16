<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Data import explorer</title>
  </head>

  <body>

    <div>
        <ul class="breadcrumb">
        <li> <g:link controller="home">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller='admin' action='index'>Admin</g:link> <span class="divider">/</span> </li>
        <li class="active">Manage Affiliation Requests</li>
      </ul>
    </div>




    <div>

      <semui:messages data="${flash}" />

      <div class="well">
        <h1 class="ui header">Data Reconciliation</h1>
        <g:if test="${recon_status.active}">
          Data reconciliation currently in stage : ${recon_status.stage} (nn%)
        </g:if>
        <g:else>
          Data reconciliation not currently active. <g:link action="startReconciliation">Start</g:link>
        </g:else>

        <g:if test="${stats}">
           <table class="ui celled table">
            <g:each in="${stats}" var="s">
              <tr><td>${s.key}</td><td>${s.value}</td></tr>
            </g:each>
          </table>
        </g:if>
      </div>


    </div>




  </body>
</html>
