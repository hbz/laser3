<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>


        <h1 class="ui header"><semui:headerIcon /><g:message code="globalDataSync.newTracker" args="[item.name,item.identifier,item.source.name]" /></h1>
        <semui:messages data="${flash}" />

    <g:form action="createTracker" controller="globalDataSync" id="${params.id}">

      <input type="hidden" name="localPkg" value="${params.localPkg}"/>
      <input type="hidden" name="synctype" value="${type}"/>

      <div class="ui segment">
        <h1 class="ui header"><semui:headerIcon /><g:message code="globalDataSync.reviewTracker"/></h1>
        <g:if test="${type=='new'}">
          <p><g:message code="globalDataSync.reviewTrackerinfo" args="[item.name,item.source.name]" /></p>
          <dl>
            <dt><g:message code="globalDataSync.newPackageName" /></dt>
            <dd><input type="text" name="newPackageName" value="${item.name}" class="input-xxlarge"/></dd>
          </dl>
        </g:if>
        <g:else>
          <g:message code="globalDataSync.notnewTracker" args="[item.name,item.source.name,localPkg.name]"/>
        </g:else>

        <dl>
          <dt><g:message code="globalDataSync.acceptChanges" /></dt>
          <dd>
          <table class="ui table">
            <tr>
              <td><input type="Checkbox" name="autoAcceptTippAddition" disabled/> TIPP Addition</td>
              <td><input type="Checkbox" name="autoAcceptTippUpdate" disabled/> TIPP Update</td>
              <td><input type="Checkbox" name="autoAcceptTippDelete" disabled/> TIPP Delete</td>
              <td><input type="Checkbox" name="autoAcceptPackageChange" disabled/> Package Changes</td>
            </tr>
          </table>
          </dd>
        </dl>
          <input type="submit" class="ui button" onclick="toggleAlert()"/>
      </div>
</g:form>

    <div class="ui icon message" id="durationAlert" style="display: none">
        <i class="notched circle loading icon"></i>
        <div class="content">
            <div class="header">
              <g:message code="globalDataSync.requestProcessing" />
            </div>
              <g:message code="globalDataSync.requestProcessingInfo" />

        </div>
    </div>

    <script>
        function toggleAlert() {
            $('#durationAlert').toggle();
        }
    </script>

    <div class="ui segment">
      <h1 class="ui header"><semui:headerIcon /><g:message code="globalDataSync.packageSyncImpact" /></h1>
      <table class="ui celled la-table table">
        <tr>
            <g:if test="${type=='new'}">
            </g:if>
            <g:else>
              <th><g:message code="globalDataSync.localPackage" args="[localPkg.name]"/></th>
            </g:else>
          <th><g:message code="globalDataSync.actions.label" /></th>
          <th>
            <g:if test="${type=='new'}">
              <g:message code="globalDataSync.newPackageafterProc"/>
            </g:if>
            <g:else>
              <g:message code="globalDataSync.localPackageafterSync" args="[localPkg.name]"/>
            </g:else>
          </th>
        </tr>
        <g:each in="${impact}" var="i">
          <tr>
              <g:if test="${i.action=='i'}">
              </g:if>
              <g:else>
                <td width="47%">
                <g:if test="${i.action=='-'}">
                  <strong><em>${i.tipp?.title?.name}</em></strong> <br>(<g:each in="${i.tipp?.title?.identifiers}" var="id"><strong>${id.namespace}</strong>: ${id.value} </g:each>) <br/>
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
                  <strong><em>${i.oldtipp?.title?.name}</em></strong> <br>(<g:each in="${i.oldtipp?.title?.identifiers}" var="id"><strong>${id.namespace}</strong>: ${id.value} </g:each>) <br/>
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
                </td>
              </g:else>

            <td width="6%"><g:if test="${i.action=='i'}">
                          <div class="ui green message"><g:message code="globalDataSync.insert"/></div>
                            </g:if>
                            <g:elseif test="${i.action=='d'}">
                              <div class="ui red message"><g:message code="globalDataSync.remove"/></div>
                            </g:elseif>
                            <g:elseif test="${i.action=='u'}">
                              <div class="ui blue message"><g:message code="globalDataSync.update"/></div>
                            </g:elseif>
                            <g:elseif test="${i.action=='-'}">
                              <div class="ui yellow message"><g:message code="globalDataSync.unchange"/></div>
                            </g:elseif>
                            <g:else>${i.action}</g:else>
            </td>
            <td width="47%">
              <g:if test="${i.action=='d'}">
                <strong><em><g:message code="globalDataSync.remove"/></em></strong>
              </g:if>
              <g:else>
                <strong><em>${i.tipp?.title?.name}</em></strong> <br>(<g:each in="${i.tipp.title.identifiers}" var="id"><strong>${id.namespace}</strong>: ${id.value} </g:each>) <br/>
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
