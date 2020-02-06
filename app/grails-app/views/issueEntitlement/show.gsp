<%@ page import="com.k_int.kbplus.IssueEntitlement" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'issueEntitlement.label', default: 'IssueEntitlement')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>
    <semui:breadcrumbs>
        <g:if test="${issueEntitlementInstance.subscription.subscriber}">
            <semui:crumb controller="myInstitution" action="currentSubscriptions" params="${[shortcode:issueEntitlementInstance.subscription.subscriber.shortcode]}" text="${issueEntitlementInstance.subscription.subscriber.name} - ${message(code:'subscription.plural', default:'Subscriptions')}"/>
        </g:if>
        <semui:crumb controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}"  text="${issueEntitlementInstance.subscription.name}" />
        <semui:crumb class="active" id="${issueEntitlementInstance?.id}" text="${issueEntitlementInstance.tipp.title.title}" />
    </semui:breadcrumbs>
    <br>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon type="${issueEntitlementInstance.tipp.title.printTitleType()}"/>

        <g:message code="issueEntitlement.for_title.label"/> ${issueEntitlementInstance.tipp.title.title}
    </h1>

    <semui:messages data="${flash}" />

        <div class="inline-lists">

            <dl>
                <g:if test="${issueEntitlementInstance.subscription}">
                    <dt><g:message code="default.subscription.label"/></dt>

                    <dd><g:link controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}">${issueEntitlementInstance.subscription.name}</g:link></dd>

                </g:if>
            <g:if test="${issueEntitlementInstance.subscription.owner}">
                <dt><g:message code="license.label"/></dt>

                <dd><g:link controller="license" action="show" id="${issueEntitlementInstance.subscription.owner.id}">${issueEntitlementInstance.subscription.owner.reference}</g:link></dd>

            </g:if>
            <g:if test="${issueEntitlementInstance?.subscription?.owner?.onixplLicense}">
                <dt><g:message code="onixplLicense.license.label"/></dt>

                <dd><g:link controller="onixplLicense" action="index" id="${issueEntitlementInstance.subscription.owner.onixplLicense.id}">${issueEntitlementInstance.subscription.owner.onixplLicense.title}</g:link></dd>
            </g:if>

            <g:if test="${issueEntitlementInstance.tipp}">
                    <dt><g:message code="title.label" default="Title" /></dt>
                    <dd><g:link controller="title" action="show" id="${issueEntitlementInstance.tipp.title.id}">${issueEntitlementInstance.tipp.title.title}</g:link> (<g:message code="title.type.label"/>: ${issueEntitlementInstance.tipp.title.printTitleType()})</dd>
                    <dt><g:message code="tipp.delayedOA" default="TIPP Delayed OA" /></dt>
                    <dd>${issueEntitlementInstance.tipp.delayedOA?.value}</dd>
                    <dt><g:message code="tipp.hybridOA" default="TIPP Hybrid OA" /></dt>
                    <dd>${issueEntitlementInstance.tipp.hybridOA?.value}</dd>
                    <dt><g:message code="tipp.show.accessStart" default="Date Title Joined Package" /></dt>
                    <dd><g:formatDate format="${message(code:'default.date.format.notime')}" date="${issueEntitlementInstance.tipp.accessStartDate}"/></dd>
            </g:if>

                <g:if test="${issueEntitlementInstance.tipp.title.ids}">
                    <dt><g:message code="title.identifiers.label" /></dt>
                    <dd><ul>
                      <g:each in="${issueEntitlementInstance.tipp.title.ids?.sort{it.ns.ns}}" var="i">
                          <li>
                              ${i.ns.ns}:${i.value}
                              <!--<g:if test="${i.ns.ns.equalsIgnoreCase('issn')}">
                              (<a href="http://suncat.edina.ac.uk/F?func=find-c&ccl_term=022=${i.value}">search on SUNCAT</a>)
                            </g:if>
                            <g:if test="${i.ns.ns.equalsIgnoreCase('eissn')}">
                              (<a href="http://suncat.edina.ac.uk/F?func=find-c&ccl_term=022=${i.value}">search on SUNCAT</a>)
                            </g:if>-->
                          </li>
                      </g:each>
                    </ul></dd>

                </g:if>


                <dt><g:message code="issueEntitlement.globalUID.label" /></dt>
                <dd>
                    <g:fieldValue bean="${issueEntitlementInstance}" field="globalUID"/>
                </dd>


                <g:if test="${issueEntitlementInstance.coreStatus}">
                    <dt><g:message code="subscription.details.core_medium"/></dt>
                    <%-- fully qualified reference because we do not make imports for one occurrence --%>
                    <dd><semui:xEditableRefData owner="${issueEntitlementInstance}" field="coreStatus" config='${de.laser.helper.RDConstants.CORE_STATUS}'/> </dd>
                </g:if>
              <g:set var="iecorestatus" value="${issueEntitlementInstance.getTIP()?.coreStatus(null)}"/>
<%--<dt>${message(code:'subscription.details.core_status', default:'Core Status')}</dt>
<dd>
  <g:render template="/templates/coreStatus" model="${['issueEntitlement': issueEntitlementInstance]}"/>
</dd> --%>

  <g:if test="${issueEntitlementInstance.tipp.hostPlatformURL}">
      <dt><g:message code="tipp.hostPlatformURL"/></dt>
      <dd> <a href="${issueEntitlementInstance.tipp.hostPlatformURL.contains('http') ?:'http://'+issueEntitlementInstance.tipp.hostPlatformURL}" target="_blank" TITLE="${issueEntitlementInstance.tipp.hostPlatformURL}">${issueEntitlementInstance.tipp.platform.name}</a></dd>
  </g:if>
</dl>

<br/>

<h3 class="ui header"><strong><g:message code="issueEntitlement.subscription_access.label"/></strong> : ${issueEntitlementInstance.subscription.name}</h3>

<table class="ui celled la-table table">
  <thead>
      <tr>
          <th><g:message code="tipp.startDate"/></th>
          <th><g:message code="tipp.startVolume"/></th>
          <th><g:message code="tipp.startIssue"/></th>
          <th><g:message code="tipp.startDate"/></th>
          <th><g:message code="tipp.endVolume"/></th>
          <th><g:message code="tipp.endIssue"/></th>
          <th><g:message code="tipp.embargo"/></th>
          <th><g:message code="tipp.coverageDepth"/></th>
          <th><g:message code="tipp.coverageNote" /></th>
      </tr>
  </thead>
  <tbody>
    <g:each in="${issueEntitlementInstance.coverages}" var="ieCoverage">
        <tr>
            <td><semui:xEditable owner="${ieCoverage}" field="startDate" type="date"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="startVolume"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="startIssue"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="endDate" type="date"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="endVolume"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="endIssue"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="embargo"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="coverageDepth"/></td>
            <td><semui:xEditable owner="${ieCoverage}" field="coverageNote"/></td>
        </tr>
    </g:each>
  </tbody>
</table>

<br/>

<h3 class="ui header">
    <strong><g:message code="issueEntitlement.package_defaults.label"/></strong> : ${issueEntitlementInstance.tipp.pkg.name}
</h3>

<table class="ui celled la-table table">
  <thead>
      <tr>
          <th><g:message code="tipp.startDate"/></th>
          <th><g:message code="tipp.startVolume"/></th>
          <th><g:message code="tipp.startIssue"/></th>
          <th><g:message code="tipp.startDate"/></th>
          <th><g:message code="tipp.endVolume"/></th>
          <th><g:message code="tipp.endIssue"/></th>
          <th><g:message code="tipp.embargo"/> (tipp)</th>
          <th><g:message code="tipp.coverageDepth"/></th>
          <th><g:message code="tipp.coverageNote"/></th>
      </tr>
  </thead>
  <tbody>
    <g:each in="${issueEntitlementInstance.tipp.coverages}" var="tippCoverage">
        <tr>
            <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tippCoverage.startDate}"/></td>
            <td>${tippCoverage.startVolume}</td>
            <td>${tippCoverage.startIssue}</td>
            <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tippCoverage.endDate}"/></td>
            <td>${tippCoverage.endVolume}</td>
            <td>${tippCoverage.endIssue}</td>
            <td>${tippCoverage.embargo}</td>
            <td>${tippCoverage.coverageDepth}</td>
            <td>${tippCoverage.coverageNote}</td>
        </tr>
    </g:each>
  </tbody>
</table>

<g:if test="${(institutional_usage_identifier) && ( usage != null ) && ( usage.size() > 0 ) }">
<span class="la-float-right">
    <laser:statsLink class="ui basic negative"
                     base="${grailsApplication.config.statsApiUrl}"
                     module="statistics"
                     controller="default"
                     action="select"
                     target="_blank"
                     params="[mode:usageMode,
                              packages:issueEntitlementInstance.subscription.getCommaSeperatedPackagesIsilList(),
                              vendors     : natStatSupplierId,
                              institutions:statsWibid
                     ]"
                     title="Springe zu Statistik im Nationalen Statistikserver">
        <i class="chart bar outline icon"></i>
    </laser:statsLink>
</span>
<h3 class="ui header">${message(code:'default.usage.header')}</h3>
<h4 class="ui">${message(code: 'default.usage.licenseGrid.header')}</h4>
<table class="ui celled la-table table">
    <thead>
    <tr>
      <th>${message(code: 'default.usage.reportType')}</th>
      <g:each in="${l_x_axis_labels}" var="l">
        <th>${l}</th>
      </g:each>
    </tr>
    </thead>
    <tbody>
    <g:set var="counter" value="${0}"/>
    <g:each in="${lusage}" var="v">
      <tr>
        <td>${l_y_axis_labels[counter++]}</td>
        <g:each in="${v}" var="v2">
          <td>${v2}</td>
        </g:each>
      </tr>
    </g:each>
    </tbody>
  </table>
<h4 class="ui">${message(code: 'default.usage.allUsageGrid.header')}</h4>
<table class="ui celled la-table table">
    <thead>
    <tr>
        <th><g:message code="default.usage.reportType"/></th>
        <g:each in="${x_axis_labels}" var="l">
            <th>${l}</th>
        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:set var="counter" value="${0}"/>
    <g:each in="${usage}" var="v">
        <tr>
          <g:set var="reportMetric" value="${y_axis_labels[counter++]}" />
            <td>${reportMetric}</td>
            <g:each in="${v}" status="i" var="v2">
                <td>
                    <laser:statsLink
                            base="${grailsApplication.config.statsApiUrl}"
                            module="statistics"
                            controller="default"
                            action="select"
                            target="_blank"
                            params="[mode        : usageMode,
                                     packages    : issueEntitlementInstance.subscription.getCommaSeperatedPackagesIsilList(),
                                     vendors     : natStatSupplierId,
                                     institutions: statsWibid,
                                     reports     : reportMetric.split(':')[0],
                                     years: x_axis_labels[i]
                            ]"
                            title="Springe zu Statistik im Nationalen Statistikserver">
                        ${v2}
                    </laser:statsLink>
                </td>
            </g:each>
        </tr>
    </g:each>
    </tbody>
</table>
</g:if>

<g:if test="${issueEntitlementInstance.tipp.title?.tipps}">
  <br/>

  <h3 class="ui header"><strong><g:message code="titleInstance.tipps.label" default="Occurrences of this title against Packages / Platforms" /></strong></h3>


  <semui:filter>
      <g:form action="show" params="${params}" method="get" class="ui form">
        <input type="hidden" name="sort" value="${params.sort}">
        <input type="hidden" name="order" value="${params.order}">

          <div class="fields three">
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
          </div>
          <div class="field">
              <input type="submit" class="ui secondary button" value="${message(code:'default.button.submit.label', default:'Submit')}">
          </div>

      </g:form>
  </semui:filter>

  <table class="ui celled la-table table">
      <thead>
          <tr>
              <th><g:message code="tipp.startDate"/></th>
              <th><g:message code="tipp.startVolume"/></th>
              <th><g:message code="tipp.startIssue"/></th>
              <th><g:message code="tipp.startDate"/></th>
              <th><g:message code="tipp.endVolume"/></th>
              <th><g:message code="tipp.endIssue"/></th>
              <th><g:message code="tipp.coverageDepth"/></th>
              <th><g:message code="platform.label"/></th>
              <th><g:message code="package.label"/></th>
              <th><g:message code="default.actions.label"/></th>
          </tr>
      </thead>
      <tbody>
          <g:each in="${tippList}" var="t">
              <g:each in="${t.coverages}" var="tippCoverage">
                <tr>
                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tippCoverage.startDate}"/></td>
                    <td>${tippCoverage.startVolume}</td>
                    <td>${tippCoverage.startIssue}</td>
                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tippCoverage.endDate}"/></td>
                    <td>${tippCoverage.endVolume}</td>
                    <td>${tippCoverage.endIssue}</td>
                    <td>${tippCoverage.coverageDepth}</td>
                    <td><g:link controller="platform" action="show" id="${t.platform.id}">${t.platform.name}</g:link></td>
                    <td><g:link controller="package" action="show" id="${t.pkg.id}">${t.pkg.name}</g:link></td>
                    <td><g:link controller="tipp" action="show" id="${t.id}">${message(code:'tipp.details', default:'View Details')}</g:link></td>
                </tr>
              </g:each>
          </g:each>
      </tbody>
  </table>
</g:if>
</div>


<div id="magicArea">

    <g:set var="yodaService" bean="yodaService" />

<g:if test="${yodaService.showDebugInfo()}">
<g:render template="coreAssertionsModal" contextPath="../templates" model="${[tipID:-1,coreDates:[]]}"/>
</g:if>
</div>
<r:script language="JavaScript">
function hideModal(){
$("[name='coreAssertionEdit']").modal('hide');
}
function showCoreAssertionModal(){
$("input.datepicker-class").datepicker({
format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
language:"${message(code:'default.locale.label', default:'en')}",
autoclose:true
});
$("[name='coreAssertionEdit']").modal('show');
$('.xEditableValue').editable();
}
</r:script>
</body>
</html>
