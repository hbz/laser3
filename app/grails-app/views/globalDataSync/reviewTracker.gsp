<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
    <%-- <style>
        .alert {
            padding: 20px;
            background-color: #f44336;
            color: white;
            display: none;
        }

        .closebtn {
            margin-left: 15px;
            color: white;
            font-weight: bold;
            float: right;
            font-size: 22px;
            line-height: 20px;
            cursor: pointer;
            transition: 0.3s;
        }

        .closebtn:hover {
            color: black;
        }
    </style> --%>
  </head>
  <body>


        <h1 class="ui header">Track ${item.name}(${item.identifier}) from ${item.source.name}</h1>
        <semui:messages data="${flash}" />

    <g:form action="createTracker" controller="globalDataSync" id="${params.id}">

      <input type="hidden" name="localPkg" value="${params.localPkg}"/>
      <input type="hidden" name="synctype" value="${type}"/>

      <div class="container well">
        <h1 class="ui header">Review Tracker</h1>
        <g:if test="${type=='new'}">
          <p>This tracker will create a new local package for "${item.name}" from "${item.source.name}". Set the new package name below.</p>
          <dl>
            <dt>New Package Name</dt>
            <dd><input type="text" name="newPackageName" value="${item.name}" class="input-xxlarge"/></dd>
          </dl>
        </g:if>
        <g:else>
          <p>This tracker will synchronize package "<b><em>${item.name}</em></b>" from "<b><em>${item.source.name}</em></b>" with the existing local package <b><em>${localPkg.name}</em></b> </p>
        </g:else>

        <dl>
          <td>Auto accept the following changes</dt>
          <dd>
          <table class="ui table">
            <tr>
              <td><input type="Checkbox" name="autoAcceptTippAddition"/>TIPP Addition</td>
              <td><input type="Checkbox" name="autoAcceptTippUpdate"/>TIPP Update</td>
              <td><input type="Checkbox" name="autoAcceptTippDelete"/>TIPP Delete</td>
              <td><input type="Checkbox" name="autoAcceptPackageChange"/>Package Changes</td>
            </tr>
          </table>
          </dd>
        </dl>
          <%-- <input type="submit"/> --%>
          <input type="submit" onclick="toggleAlert()"/>
    <%-- <button onclick="alertBox()">Daten absenden</button> --%>
    <%-- <button onclick="toggleAlert()">Daten absenden</button> --%>
</div>
</g:form>

<%-- // begin added by frank 04.12.2017
<script>
function alertBox() {
    alert("Bestätigen mit 'OK', um die Bearbeitung zu starten.\nDiese kann, abbhängig von der Größe des Pakets, mehrere Minuten in Anspruch nehmen.")
}

</script>
<%-- // end added by frank 04.12.2017

<%-- //begin added by frank 04.12.2017 --%>
    <%-- <div class="alert" id="durationAlert">
        <span class="closebtn" onclick="this.parentElement.style.display='none';">&times;
        </span>
            Die Anfrage ist in Bearbeitung. Diese kann einige Minuten dauern.
    </div> --%>

    <div class="ui icon message" id="durationAlert" style="display: none">
        <i class="notched circle loading icon"></i>
        <div class="content">
            <div class="header">
                Ihre Anfrage ist in Bearbeitung.
            </div>
            <p>Bitte haben sie einen Moment Geduld</p>
            <p>Die Verarbeitung kann einige Minuten dauern</p>
        </div>
    </div>

    <script>
        function toggleAlert() {
            var alert = document.getElementById('durationAlert');
            var displaySetting = alert.style.display;

            if(displaySetting == 'block') {
                alert.style.display = 'none';
            }
            else {
                alert.style.display = 'block';
            }
        }
    </script>

<%-- // end added by frank 04.12.2017 --%>

    <div class="container well">
      <h1 class="ui header">Package Sync Impact</h1>
      <table class="ui celled striped table">
        <tr>
          <th>
            <g:if test="${type=='new'}">
              No current package
            </g:if>
            <g:else>
              ${localPkg.name} as now
            </g:else>
          </th>
          <th>Action</th>
          <th>
            <g:if test="${type=='new'}">
              New Package After Processing
            </g:if>
            <g:else>
              ${localPkg.name} after sync
            </g:else>
          </th>
        </tr>
        <g:each in="${impact}" var="i">
          <tr>
            <td width="47%">
              <g:if test="${i.action=='i'}">
              </g:if>
              <g:else>
                <g:if test="${i.action=='-'}">
                  <b><em>${i.tipp?.title?.name}</em></b> (<g:each in="${i.tipp?.title?.identifiers}" var="id">${id.namespace}:${id.value} </g:each>) <br/>
                  <table class="ui table">
                    <tr><th></th><th>Volume</th><th>Issue</th><th>Date</th></tr>
                    <g:each in="${i.tipp.coverage}" var="c">
                      <tr><th>Start</th> <td>${c.startVolume}</td><td>${c.startIssue}</td>
                          <td>${c.startDate}</td></tr>
                      <tr><th>End</th> <td>${c.endVolume}</td><td> ${c.endIssue}</td><td>${c.endDate}</td></tr>
                      <tr><td colspan="4"> ${c.coverageDepth} ${c.coverageNote}</td></tr>
                    </g:each>
                  </table>
                </g:if>
                <g:else>
                  <b><em>${i.oldtipp?.title?.name}</em></b> (<g:each in="${i.oldtipp?.title?.identifiers}" var="id">${id.namespace}:${id.value} </g:each>) <br/>
                  <table class="ui table">
                    <tr><th></th><th>Volume</th><th>Issue</th><th>Date</th></tr>
                    <g:each in="${i.oldtipp.coverage}" var="c">
                      <tr><th>Start</th> <td>${c.startVolume}</td><td>${c.startIssue}</td>
                          <td>${c.startDate}</td></tr>
                      <tr><th>End</th> <td>${c.endVolume}</td><td> ${c.endIssue}</td><td>${c.endDate}</td></tr>
                      <tr><td colspan="4"> ${c.coverageDepth} ${c.coverageNote}</td></tr>
                    </g:each>
                  </table>
                </g:else>
              </g:else>
            </td>
            <td width="6%">${i.action}</td>
            <td width="47%">
              <g:if test="${i.action=='d'}">
                <b><em>Removed</em></b>
              </g:if>
              <g:else>
                <b><em>${i.tipp?.title?.name}</em></b> (<g:each in="${i.tipp.title.identifiers}" var="id">${id.namespace}:${id.value} </g:each>) <br/>
                <table class="ui table">
                  <tr><th></th><th>Volume</th><th>Issue</th><th>Date</th></tr>
                  <g:each in="${i.tipp.coverage}" var="c">
                    <tr><th>Start</th> <td>${c.startVolume}</td><td>${c.startIssue}</td><td>${c.startDate}</td></tr>
                    <tr><th>End</th> <td>${c.endVolume}</td><td> ${c.endIssue}</td><td>${c.endDate}</td></tr>
                    <tr><td colspan="4"> ${c.coverageDepth} ${c.coverageNote}</td></tr>
                  </g:each>
                </table>
              </g:else>
            </td>
            </td>
          </tr>
        </g:each>
      
      </table>
    </div>

  </body>
</html>
