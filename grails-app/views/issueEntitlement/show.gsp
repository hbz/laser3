<%@ page import="de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.ApiSource; de.laser.helper.ConfigUtils; de.laser.IssueEntitlement" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'issueEntitlement.label')}"/>
    <title>${message(code:'laser')} : <g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<semui:breadcrumbs>
    <g:if test="${issueEntitlementInstance.subscription.subscriber}">
        <semui:crumb controller="myInstitution" action="currentSubscriptions"
                     params="${[shortcode: issueEntitlementInstance.subscription.subscriber.shortcode]}"
                     text="${issueEntitlementInstance.subscription.subscriber.name} - ${message(code: 'subscription.plural')}"/>
    </g:if>
    <semui:crumb controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}"
                 text="${issueEntitlementInstance.subscription.name}"/>
    <semui:crumb class="active" id="${issueEntitlementInstance.id}"
                 text="${issueEntitlementInstance.tipp.title.title}"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon
        type="${issueEntitlementInstance.tipp.title.printTitleType()}"/>

<g:message code="issueEntitlement.for_title.label"/> ${issueEntitlementInstance.tipp.title.title}
</h1>

<semui:messages data="${flash}"/>

<g:render template="/templates/meta/identifier" model="${[object: issueEntitlementInstance, editable: false]}" />

<div class="la-inline-lists">
    <div class="ui card">
        <div class="content">
            <h3 class="ui header">
                <g:message code="issueEntitlement.titel"/>
            </h3>
        </div>

        <div class="content">
            <semui:listIcon type="${issueEntitlementInstance.tipp.title.class.name}"/>
            <g:link controller="title" action="show"
                    id="${issueEntitlementInstance.tipp.title.id}">${issueEntitlementInstance.tipp.title.title}
            </g:link>
            <g:if test="${issueEntitlementInstance.tipp.hostPlatformURL}">
                <semui:linkIcon
                        href="${issueEntitlementInstance.tipp.hostPlatformURL.startsWith('http') ? issueEntitlementInstance.tipp.hostPlatformURL : 'http://' + issueEntitlementInstance.tipp.hostPlatformURL}"/>
            </g:if>
            <br />

        <!-- START TEMPLATE -->

            <g:render template="/templates/title"
                      model="${[item: issueEntitlementInstance, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]}"/>
        <!-- END TEMPLATE -->

            <g:if test="${issueEntitlementInstance.tipp.title instanceof BookInstance}">
                <br />

                <div class="la-title">${message(code: 'subscription.details.coverage_dates')}</div>

                <div class="la-icon-list">
                    <div class="item">
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                           data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                      date="${issueEntitlementInstance.tipp.title.dateFirstInPrint}"/>
                    </div>

                    <div class="item">
                        <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                           data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                      date="${issueEntitlementInstance.tipp.title.dateFirstOnline}"/>
                    </div>
                </div>

            </g:if>

            <br />

            <div class="la-title">${message(code: 'subscription.details.access_dates')}</div>

            <div class="la-icon-list">
                <div class="item">
                    <i class="grey calendar icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'subscription.details.access_start')}"></i>
                <g:if test="${editable}">
                    <semui:xEditable owner="${issueEntitlementInstance}" type="date" field="accessStartDate"/>
                    <i class="grey question circle icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                </g:if>
                <g:else>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${issueEntitlementInstance.accessStartDate}"/>
                </g:else>
                </div>
                <div class="item">
                    <i class="grey calendar icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'subscription.details.access_end')}"></i>
                <g:if test="${editable}">
                    <semui:xEditable owner="${issueEntitlementInstance}" type="date" field="accessEndDate"/>
                    <i class="grey question circle icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                </g:if>
                <g:else>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${issueEntitlementInstance.accessEndDate}"/>
                </g:else>
                </div>
            </div>

            <g:if test="${issueEntitlementInstance.priceItem}">
                <br />

                <div class="la-title">${message(code: 'subscription.details.prices')}</div>

                <div class="la-icon-list">
                    <div class="item">
                        <g:message code="tipp.listPrice"/>: <semui:xEditable field="listPrice"
                                                                             owner="${issueEntitlementInstance.priceItem}"
                                                                             overwriteEditable="false"/> <semui:xEditableRefData
                                field="listCurrency" owner="${issueEntitlementInstance.priceItem}" config="Currency"
                                overwriteEditable="false"/>
                    </div>

                    <div class="item">
                        <g:message code="tipp.localPrice"/>: <semui:xEditable field="localPrice"
                                                                              owner="${issueEntitlementInstance.priceItem}"
                                                                              overwriteEditable="false"/> <semui:xEditableRefData
                                field="localCurrency" owner="${issueEntitlementInstance.priceItem}" config="Currency"
                                overwriteEditable="false"/>
                        (<g:message code="tipp.priceDate"/> <semui:xEditable field="priceDate" type="date"
                                                                             owner="${issueEntitlementInstance.priceItem}"
                                                                             overwriteEditable="false"/>)
                    </div>
                </div>
            </g:if>

            <g:if test="${issueEntitlementInstance.ieGroups}">
                <br />

                <div class="la-title">${message(code: 'subscription.details.ieGroups')}</div>

                <div class="la-icon-list">
                    <g:each in="${issueEntitlementInstance.ieGroups.sort{it.ieGroup.name}}" var="titleGroup">
                        <div class="item">
                            <i class="grey icon object group la-popup-tooltip la-delay" data-content="${message(code: 'issueEntitlementGroup.label')}"></i>
                            <div class="content">
                                <g:link controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}" params="[titleGroup: titleGroup.ieGroup.id]" >${titleGroup.ieGroup.name}</g:link>
                            </div>
                        </div>
                    </g:each>
                </div>
            </g:if>
        </div>

    </div>

%{--<semui:form>
    <dl>
        <g:if test="${issueEntitlementInstance.subscription}">
            <dt><g:message code="default.subscription.label"/></dt>

            <dd><g:link controller="subscription" action="index"
                        id="${issueEntitlementInstance.subscription.id}">${issueEntitlementInstance.subscription.name}</g:link></dd>

        </g:if>
    --}%%{--<g:if test="${issueEntitlementInstance.subscription.owner}">
        <dt><g:message code="license.label"/></dt>

        <dd><g:link controller="license" action="show"
                    id="${issueEntitlementInstance.subscription.owner.id}">${issueEntitlementInstance.subscription.owner.reference}</g:link></dd>

    </g:if>--}%%{--
    <%--<g:if test="${issueEntitlementInstance.subscription.owner?.onixplLicense}">
        <dt><g:message code="onixplLicense.license.label"/></dt>

        <dd><g:link controller="onixplLicense" action="index"
                    id="${issueEntitlementInstance.subscription.owner.onixplLicense.id}">${issueEntitlementInstance.subscription.owner.onixplLicense.title}</g:link></dd>
    </g:if>--%>

        <g:if test="${issueEntitlementInstance.tipp}">
            <dt><g:message code="title.label" default="Title"/></dt>
            <dd><g:link controller="title" action="show"
                        id="${issueEntitlementInstance.tipp.title.id}">${issueEntitlementInstance.tipp.title.title}</g:link> (<g:message
                    code="title.type.label"/>: ${issueEntitlementInstance.tipp.title.printTitleType()})</dd>
            <dt><g:message code="tipp.delayedOA" default="TIPP Delayed OA"/></dt>
            <dd>${issueEntitlementInstance.tipp.delayedOA?.value}</dd>
            <dt><g:message code="tipp.hybridOA" default="TIPP Hybrid OA"/></dt>
            <dd>${issueEntitlementInstance.tipp.hybridOA?.value}</dd>
            <dt><g:message code="tipp.show.accessStart" default="Date Title Joined Package"/></dt>
            <dd><g:formatDate format="${message(code: 'default.date.format.notime')}"
                              date="${issueEntitlementInstance.tipp.accessStartDate}"/></dd>
        </g:if>


        <dt><g:message code="issueEntitlement.globalUID.label"/></dt>
        <dd>
            <g:fieldValue bean="${issueEntitlementInstance}" field="globalUID"/>
        </dd>


        <g:if test="${issueEntitlementInstance.coreStatus}">
            <dt><g:message code="subscription.details.core_medium"/></dt>
        <%-- fully qualified reference because we do not make imports for one occurrence --%>
            <dd><semui:xEditableRefData owner="${issueEntitlementInstance}" field="coreStatus"
                                        config='${de.laser.helper.RDConstants.CORE_STATUS}'/></dd>
        </g:if>
    <%--              <g:set var="iecorestatus" value="${issueEntitlementInstance.getTIP()?.coreStatus(null)}"/>
<%--<dt>${message(code:'subscription.details.core_status')}</dt>
    <dd>
      <g:render template="/templates/coreStatus" model="${['issueEntitlement': issueEntitlementInstance]}"/>
    </dd> --%>

        <g:if test="${issueEntitlementInstance.tipp.hostPlatformURL}">
            <dt><g:message code="tipp.hostPlatformURL"/></dt>
            <dd><a href="${issueEntitlementInstance.tipp.hostPlatformURL.startsWith('http') ?: 'http://' + issueEntitlementInstance.tipp.hostPlatformURL}"
                   target="_blank"
                   TITLE="${issueEntitlementInstance.tipp.hostPlatformURL}">${issueEntitlementInstance.tipp.platform.name}</a>
            </dd>
        </g:if>
    </dl>
</semui:form>--}%

    <g:if test="${issueEntitlementInstance.tipp.title instanceof JournalInstance}">
        <div class="ui card">
            <div class="content">
                <h3 class="ui header"><strong><g:message
                        code="issueEntitlement.subscription_access.label"/></strong> :
                <g:link controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}">
                    ${issueEntitlementInstance.subscription.name}
                </g:link>
                </h3>
            </div>

            <div class="content">
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
                        <th><g:message code="tipp.coverageNote"/></th>
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
            </div>
        </div>
    </g:if>



    <g:if test="${(institutional_usage_identifier) && (usage != null) && (usage.size() > 0)}">
        <br />

        <div class="ui card">
            <div class="content">
                <h3 class="ui header">${message(code: 'default.usage.header')}</h3>
            </div>

            <div class="content">
                <span class="la-float-right">
                    <laser:statsLink class="ui basic negative"
                                     base="${ConfigUtils.getStatsApiUrl()}"
                                     module="statistics"
                                     controller="default"
                                     action="select"
                                     target="_blank"
                                     params="[mode        : usageMode,
                                              packages    : issueEntitlementInstance.subscription.getCommaSeperatedPackagesIsilList(),
                                              vendors     : natStatSupplierId,
                                              institutions: statsWibid
                                     ]"
                                     title="Springe zu Statistik im Nationalen Statistikserver">
                        <i class="chart bar outline icon"></i>
                    </laser:statsLink>
                </span>


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
                            <g:set var="reportMetric" value="${y_axis_labels[counter++]}"/>
                            <td>${reportMetric}</td>
                            <g:each in="${v}" status="i" var="v2">
                                <td>
                                    <laser:statsLink
                                            base="${ConfigUtils.getStatsApiUrl()}"
                                            module="statistics"
                                            controller="default"
                                            action="select"
                                            target="_blank"
                                            params="[mode        : usageMode,
                                                     packages    : issueEntitlementInstance.subscription.getCommaSeperatedPackagesIsilList(),
                                                     vendors     : natStatSupplierId,
                                                     institutions: statsWibid,
                                                     reports     : reportMetric.split(':')[0],
                                                     years       : x_axis_labels[i]
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
            </div>
        </div>
    </g:if>


    <g:if test="${issueEntitlementInstance.tipp.title instanceof JournalInstance}">
        <br />

        <div class="ui card">
            <div class="content">
                <h3 class="ui header">
                    <strong><g:message
                            code="issueEntitlement.package_defaults.label"/></strong>:
                <g:link controller="package" action="show"
                        id="${issueEntitlementInstance.tipp.pkg.id}">${issueEntitlementInstance.tipp.pkg.name}
                </g:link>

                </h3>
            </div>

            <div class="content">
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th><g:message code="tipp.startDate"/></th>
                        <th><g:message code="tipp.startVolume"/></th>
                        <th><g:message code="tipp.startIssue"/></th>
                        <th><g:message code="tipp.endDate"/></th>
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
                            <td><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${tippCoverage.startDate}"/></td>
                            <td>${tippCoverage.startVolume}</td>
                            <td>${tippCoverage.startIssue}</td>
                            <td><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${tippCoverage.endDate}"/></td>
                            <td>${tippCoverage.endVolume}</td>
                            <td>${tippCoverage.endIssue}</td>
                            <td>${tippCoverage.embargo}</td>
                            <td>${tippCoverage.coverageDepth}</td>
                            <td>${tippCoverage.coverageNote}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </g:if>



    <g:if test="${issueEntitlementInstance.tipp.title.tipps}">
        <br />

        <div class="ui card">
        <div class="content">

        <h3 class="ui header"><strong><g:message code="titleInstance.tipps.label"
                                                 default="Occurrences of this title against Packages / Platforms"/></strong>
        </h3>
        </div>

        <div class="content">
            <semui:filter>
                <g:form action="show" params="${params}" method="get" class="ui form">
                    <input type="hidden" name="sort" value="${params.sort}">
                    <input type="hidden" name="order" value="${params.order}">

                    <div class="fields three">
                        <div class="field">
                            <label for="filter">${message(code: 'tipp.show.filter_pkg')}</label>
                            <input id="filter" name="filter" value="${params.filter}"/>
                        </div>

                        <div class="field">
                            <semui:datepicker label="default.startsBefore.label" id="startsBefore"
                                              name="startsBefore"
                                              value="${params.startsBefore}"/>
                        </div>

                        <div class="field">
                            <semui:datepicker label="default.endsAfter.label" id="endsAfter" name="endsAfter"
                                              value="${params.endsAfter}"/>
                        </div>
                    </div>

                    <div class="field">
                        <input type="submit" class="ui secondary button"
                               value="${message(code: 'default.button.submit.label')}">
                    </div>

                </g:form>
            </semui:filter>

            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th><g:message code="tipp.startDate"/></th>
                    <th><g:message code="tipp.startVolume"/></th>
                    <th><g:message code="tipp.startIssue"/></th>
                    <th><g:message code="tipp.endDate"/></th>
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
                            <td><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${tippCoverage.startDate}"/></td>
                            <td>${tippCoverage.startVolume}</td>
                            <td>${tippCoverage.startIssue}</td>
                            <td><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${tippCoverage.endDate}"/></td>
                            <td>${tippCoverage.endVolume}</td>
                            <td>${tippCoverage.endIssue}</td>
                            <td>${tippCoverage.coverageDepth}</td>
                            <td><g:link controller="platform" action="show"
                                        id="${t.platform.id}">${t.platform.name}</g:link></td>
                            <td><g:link controller="package" action="show"
                                        id="${t.pkg.id}">${t.pkg.name}</g:link></td>
                            <td>
                                <g:link class="ui button" controller="tipp" action="show"
                                        id="${t.id}">${message(code: 'tipp.details')}</g:link></td>
                        </tr>
                    </g:each>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
</div>

</body>
</html>
