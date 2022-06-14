<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.ApiSource; de.laser.helper.ConfigUtils; de.laser.IssueEntitlement" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'issueEntitlement.label')}"/>
    <title>${message(code: 'laser')} : <g:message code="default.show.label" args="[entityName]"/></title>
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
                 text="${issueEntitlementInstance.name}"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon
        type="${issueEntitlementInstance.tipp.titleType}"/>

<g:message code="issueEntitlement.for_title.label"
           args="[issueEntitlementInstance.name, issueEntitlementInstance.subscription.name]"/>
</h1>

<semui:messages data="${flash}"/>

<g:render template="/templates/meta/identifier" model="${[object: issueEntitlementInstance, editable: false]}"/>

<div class="la-inline-lists">

    <g:if test="${isMySub || SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
        <div class="ui card">
            <div class="content">
                <h3 class="ui header">
                    <g:message code="subscription.label"/>
                </h3>
            </div>

            <div class="content">
                <g:link controller="subscription" action="show" id="${issueEntitlementInstance.subscription.id}">
                    ${issueEntitlementInstance.subscription.dropdownNamingConvention()}
                </g:link>
                <br>
                <br>
                <g:link controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}">
                    <g:message code="subscription.details.current_ent"/>
                </g:link>
            </div>
        </div>
    </g:if>

    <div class="ui card">
        <div class="content">
            <h3 class="ui header">
                <g:message code="issueEntitlement.titel"/>
            </h3>
        </div>

        <div class="content">
            <!-- START TEMPLATE -->
            <g:render template="/templates/title_long"
                      model="${[ie         : issueEntitlementInstance, tipp: issueEntitlementInstance.tipp,
                                showPackage: true, showPlattform: true, showCompact: false, showEmptyFields: true]}"/>
            <!-- END TEMPLATE -->

            <br/>



            <g:if test="${issueEntitlementInstance.tipp.titleType == 'Book'}">
                <div class="la-title">${message(code: 'tipp.print')} & ${message(code: 'tipp.online')}</div>
            </g:if>
            <g:elseif test="${issueEntitlementInstance.tipp.titleType == "Journal"}">
                <div class="la-title">${message(code: 'tipp.coverage')}</div>
            </g:elseif>
            <g:else>
                <div class="la-title">${message(code: 'tipp.online')}</div>
            </g:else>

            <div class="la-icon-list">
                <g:render template="/templates/tipps/coverages"
                          model="${[ie: issueEntitlementInstance, tipp: issueEntitlementInstance.tipp]}"/>
            </div>


            <br/>

            <g:if test="${isMySub || SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                <div class="la-title">${message(code: 'subscription.details.access_dates')}</div>

                <div class="la-icon-list">
                    <div class="item">
                        <i class="grey calendar icon la-popup-tooltip la-delay"
                           data-content="${message(code: 'subscription.details.access_start')}"></i>
                        <g:if test="${editable}">
                            <semui:xEditable owner="${issueEntitlementInstance}" type="date"
                                             field="accessStartDate"/>
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

                <br/>


                <div class="la-title">${message(code: 'subscription.details.prices')}</div>

                <div class="la-icon-list">
                    <g:if test="${issueEntitlementInstance.priceItems}">
                        <div class="ui cards">
                            <g:each in="${issueEntitlementInstance.priceItems}" var="priceItem" status="i">
                                <div class="item">
                                    <div class="ui card">
                                        <div class="content">
                                            <div class="la-card-column">
                                                <g:message code="tipp.price.listPrice"/>:
                                                <semui:xEditable field="listPrice"
                                                                 owner="${priceItem}"/> <semui:xEditableRefData
                                                        field="listCurrency" owner="${priceItem}" config="Currency"/>

                                                <br/>
                                                <g:message code="tipp.price.localPrice"/>: <semui:xEditable
                                                        field="localPrice"
                                                        owner="${priceItem}"/> <semui:xEditableRefData
                                                        field="localCurrency" owner="${priceItem}" config="Currency"/>
                                                <%--<br/>
                                                (<g:message code="tipp.price.startDate"/> <semui:xEditable field="startDate"
                                                                                                          type="date"
                                                                                                          owner="${priceItem}"/>-
                                                <g:message code="tipp.price.endDate"/> <semui:xEditable field="endDate"
                                                                                                       type="date"
                                                                                                       owner="${priceItem}"/>)--%>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                            </g:each>
                        </div>
                    </g:if>
                </div>
                <br/>

                <div class="la-title">${message(code: 'subscription.details.ieGroups')}</div>

                <div class="la-icon-list">
                    <g:if test="${issueEntitlementInstance.ieGroups}">
                        <g:each in="${issueEntitlementInstance.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                            <div class="item">
                                <i class="grey icon object group la-popup-tooltip la-delay"
                                   data-content="${message(code: 'issueEntitlementGroup.label')}"></i>

                                <div class="content">
                                    <g:link controller="subscription" action="index"
                                            id="${issueEntitlementInstance.subscription.id}"
                                            params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                                </div>
                            </div>
                        </g:each>
                    </g:if>
                </div>
            </g:if>
        </div>

    </div>



    <g:if test="${(institutional_usage_identifier) && (usage != null) && (usage.size() > 0)}">
        <br/>

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
                <table class="ui celled la-js-responsive-table la-table table">
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
                <table class="ui celled la-js-responsive-table la-table table">
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



<%-- we should not be supposed to see the same tipp in other circumstances ... do we need the following information at all?
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

            <table class="ui celled la-js-responsive-table la-table table">
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
    --%>
</div>

</body>
</html>
