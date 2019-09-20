<%@ page import="com.k_int.kbplus.TitleInstance" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title><g:message code="tipp.show.label" args="${[titleInstanceInstance?.title,tipp.pkg.name,tipp.platform.name]}"/></title>
  </head>
  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="package" action="show" id="${tipp.pkg.id}" text="${tipp.pkg.name} [${message(code:'package.label', default:'package')}]" />
      <semui:crumb text="${tipp.title.title} [${message(code:'title.label')}]" class="active" />
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
      <g:message code="tipp.show.label" args="${[titleInstanceInstance?.title,tipp.pkg.name,tipp.platform.name]}"/>
    </h1>

    <g:render template="/templates/meta/identifier" model="${[object: tipp, editable: editable]}" />

    <semui:messages data="${flash}" />

    <div class="inline-lists">

        <dl>
          <dt><g:message code="tipp.show.avStatus" /></dt>
          <dd><span title="${tipp.availabilityStatusExplanation}">${tipp.availabilityStatus?.getI10n("value")}</span></dd>

          <dt><g:message code="tipp.show.accessStart"/></dt>
          <dd>${tipp.accessStartDate}</dd>

          <dt><g:message code="tipp.show.accessEnd"/></dt>
          <dd>${tipp.accessEndDate}</dd>

          <dt><g:message code="tipp.coverage"/></dt>
          <dd>
            <dl>
              <g:each in="${tipp.coverages}" var="covStmt">
                <dt><g:message code="tipp.show.tippStartDate"/></dt>
                <dd><g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.startDate}"/></dd>

                <dt><g:message code="tipp.show.tippStartVol"/></dt>
                <dd>${covStmt.startVolume}</dd>

                <dt><g:message code="tipp.show.tippStartIss"/></dt>
                <dd>${covStmt.startIssue}</dd>

                <dt><g:message code="tipp.show.tippEndDate"/></dt>
                <dd><g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.endDate}"/></dd>

                <dt><g:message code="tipp.show.tippEndVol"/></dt>
                <dd>${covStmt.endVolume}</dd>

                <dt><g:message code="tipp.show.tippEndIss"/></dt>
                <dd>${covStmt.endIssue}</dd>

                <dt><g:message code="tipp.coverageDepth"/></dt>
                <dd>${covStmt.coverageDepth}</dd>

                <dt><g:message code="tipp.coverageNote"/></dt>
                <dd>${covStmt.coverageNote}</dd>

                <dt><g:message code="tipp.embargo"/></dt>
                <dd>${covStmt.embargo}</dd>
              </g:each>
            </dl>
          </dd>

          <dt><g:message code="tipp.hostPlatformURL"/></dt>
          <dd>${tipp.hostPlatformURL}</dd>

          <dt><g:message code="default.status.label"/></dt>
          <dd>${tipp.status.getI10n("value")}</dd>

          <dt><g:message code="tipp.show.statusReason"/></dt>
          <dd>${tipp.statusReason?.getI10n("value")}</dd>

          <dt><g:message code="tipp.delayedOA"/></dt>
          <dd>${tipp.delayedOA?.getI10n("value")}"</dd>

          <dt><g:message code="tipp.hybridOA"/></dt>
          <dd>${tipp.hybridOA?.getI10n("value")}"</dd>

          <dt><g:message code="tipp.paymentType"/></dt>
          <dd>${tipp.payment?.getI10n("value")}"</dd>

          <dt><g:message code="tipp.host_platform"/></dt>
          <dd>${tipp.platform.name}</dd>
        </dl>

        <dl>
          <dt style="margin-top:10px"><g:message code="tipp.additionalPlatforms"/></dt>
          <dd>
            <table class="ui celled la-table table">
              <thead>
                <tr>
                  <th><g:message code="default.relation.label"/></th>
                  <th><g:message code="tipp.show.platformName"/></th>
                  <th><g:message code="platform.primaryURL"/></th></tr>
              </thead>
              <tbody>
                <g:each in="${tipp.additionalPlatforms}" var="ap">
                  <tr>
                    <td>${ap.rel}</td>
                    <td>${ap.platform.name}</td>
                    <td>${ap.platform.primaryUrl}</td>
                  </tr>
                </g:each>
              </tbody>
            </table>
          </dd>
        </dl>

          <g:if test="${titleInstanceInstance?.tipps}">
            <dl>
            <dt><g:message code="titleInstance.tipps.label" default="${message(code:'titleInstance.tipps.label', default:'Occurences of this title against Packages / Platforms')}" /></dt>
            <dd>

                <semui:filter>
                   <g:form action="show" params="${params}" method="get" class="ui form">
                       <input type="hidden" name="sort" value="${params.sort}">
                       <input type="hidden" name="order" value="${params.order}">
                       <div class="fields">
                           <div class="field">
                               <label for="filter">${message(code:'tipp.show.filter_pkg', default:'Filters - Package Name')}</label>
                               <input id="filter" name="filter" value="${params.filter}"/>
                           </div>
                           <div class="field">
                                <semui:datepicker label="default.startsBefore.label" id="startsBefore" name="startsBefore" value="${params.startsBefore}" />
                           </div>
                           <div class="field">
                               <semui:datepicker label="default.endsAfter.label" id="endsAfter" name="endsAfter" value="${params.endsAfter}" />
                           </div>
                            <div class="field">
                                <label>&nbsp;</label>
                                <input type="submit" class="ui secondary button" value="${message(code:'default.button.submit.label', default:'Submit')}">
                            </div>
                       </div>
                    </g:form>
                </semui:filter>

            <table class="ui celled la-table table">
              <thead>
              <tr>
                <th><g:message code="tipp.coverageStatements"/></th>
                <th><g:message code="platform.label"/></th>
                <th><g:message code="package.label"/></th>
              </tr>
              </thead>
              <tbody>
              <g:each in="${tippList}" var="t">
                <tr>
                  <td>
                    <g:each in="${t.coverages}" var="covStmt">
                      <p>
                        <div>
                          <span><g:message code="default.date.label"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.startDate}"/></span>
                        </div>
                        <div>
                          <span><g:message code="tipp.volume"/>: ${covStmt.startVolume}</span>
                        </div>
                        <div>
                          <span><g:message code="tipp.issue"/>: ${covStmt.startIssue}</span>
                        </div>
                        <div>
                          <span><g:message code="default.date.label"/>: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.endDate}"/></span>
                        </div>
                        <div>
                          <span><g:message code="tipp.volume"/>: ${covStmt.endVolume}</span>
                        </div>
                        <div>
                          <span><g:message code="tipp.issue"/>: ${covStmt.endIssue}</span>
                        </div>
                        <div>
                          <span><g:message code="tipp.coverageDepth"/>: ${covStmt.coverageDepth}</span>
                        </div>
                        <div>
                          <span><g:message code="tipp.coverageNote"/>: ${covStmt.coverageNote}</span>
                        </div>
                        <div>
                          <span><g:message code="tipp.embargo"/>: ${covStmt.embargo}</span>
                        </div>
                      </p>
                    </g:each>
                  </td>
                  <td><g:link controller="platform" action="show" id="${t.platform.id}">${t.platform.name}</g:link></td>
                  <td><g:link controller="package" action="show" id="${t.pkg.id}">${t.pkg.name} (${t.pkg.contentProvider?.name})</g:link></td>
                </tr>
              </g:each>
              </tbody>
            </table>
            </dd>
            </dl>
          </g:if>
    </div>
  </body>
</html>
