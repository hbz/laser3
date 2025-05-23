<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.config.ConfigMapper; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.IssueEntitlement" %>

<g:set var="entityName" value="${message(code: 'issueEntitlement.label')}"/>
<laser:htmlStart text="${message(code:"default.show.label", args:[entityName])}" />

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>issueEntitlementInstance.dateCreated: ${issueEntitlementInstance.dateCreated}</p>

        <p>issueEntitlementInstance.lastUpdated: ${issueEntitlementInstance.lastUpdated}</p>

        <p>issueEntitlementInstance.status: ${issueEntitlementInstance.status?.value}</p>
    </div>
</ui:debugInfo>

<ui:breadcrumbs>
    <g:if test="${issueEntitlementInstance.subscription.getSubscriberRespConsortia()}">
        <ui:crumb controller="myInstitution" action="currentSubscriptions"
                     params="${[shortcode: issueEntitlementInstance.subscription.getSubscriberRespConsortia().shortcode]}"
                     text="${issueEntitlementInstance.subscription.getSubscriberRespConsortia().name} - ${message(code: 'subscription.plural')}"/>
    </g:if>
    <ui:crumb controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}"
                 text="${issueEntitlementInstance.subscription.name}"/>
    <ui:crumb class="active" id="${issueEntitlementInstance.id}"
                 text="${issueEntitlementInstance.tipp.name}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions" />
</ui:controlButtons>

<ui:h1HeaderWithIcon message="issueEntitlement.for_title.label" args="[issueEntitlementInstance.tipp.name, issueEntitlementInstance.subscription.name]"
                        type="${issueEntitlementInstance.tipp.titleType}" />

<laser:render template="/templates/meta/identifier" model="${[object: issueEntitlementInstance, editable: false]}"/>

<ui:messages data="${flash}"/>

<laser:render template="/templates/reportTitleToProvider/flyoutAndTippTask" model="${[tipp: issueEntitlementInstance.tipp]}"/>

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
                <br/>
                <br/>
                <g:link controller="subscription" action="index" id="${issueEntitlementInstance.subscription.id}">
                    <g:message code="subscription.details.current_ent"/>
                </g:link>
            </div>
        </div>
    </g:if>

    <g:if test="${participantPerpetualAccessToTitle}">
        <div class="ui card">
            <div class="content">
                <div class="header"><g:message code="myinst.currentPermanentTitles.label"/> in: </div>
            </div>

            <div class="content">
                <div class="ui list">
                    <g:each in="${participantPerpetualAccessToTitle}" var="pt">
                        <div class="item">
                                <i class="${Icon.SUBSCRIPTION}"></i>
                                <div class="content">
                                    <div class="header"
                                        <g:link controller="subscription"
                                            action="index"
                                            id="${pt.subscription.id}">${pt.subscription.dropdownNamingConvention()}</g:link>
                                    </div>
                                    <div class="description">
                                        <g:link controller="issueEntitlement"
                                                action="show"
                                                class="${Btn.SIMPLE} tiny la-margin-top-05em"
                                                id="${pt.issueEntitlement.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                    </div>
                                </div>
                        </div>
                    </g:each>
                </div>

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
            <laser:render template="/templates/titles/title_long"
                      model="${[ie         : issueEntitlementInstance, tipp: issueEntitlementInstance.tipp,
                                showPackage: true, showPlattform: true, showCompact: false, showEmptyFields: true, sub: issueEntitlementInstance.subscription.id]}"/>
            <!-- END TEMPLATE -->

            <br/>



            <g:if test="${issueEntitlementInstance.tipp.titleType == 'monograph'}">
                <div class="la-title">${message(code: 'tipp.print')} & ${message(code: 'tipp.online')}</div>
            </g:if>
            <g:elseif test="${issueEntitlementInstance.tipp.titleType == "serial"}">
                <div class="la-title">${message(code: 'tipp.coverage')}</div>
            </g:elseif>
            <g:else>
                <div class="la-title">${message(code: 'tipp.online')}</div>
            </g:else>

            <div class="la-icon-list">
                <laser:render template="/templates/tipps/coverages"
                          model="${[ie: issueEntitlementInstance, tipp: issueEntitlementInstance.tipp]}"/>
            </div>


            <br/>

            <g:if test="${isMySub || SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                <div class="la-title">${message(code: 'subscription.details.access_dates')}</div>

                <div class="la-icon-list">
                    <div class="item">
                        <i class="grey ${Icon.SYM.DATE} la-popup-tooltip"
                           data-content="${message(code: 'subscription.details.access_start')}"></i>
                        <g:if test="${editable}">
                            <ui:xEditable owner="${issueEntitlementInstance}" type="date"
                                             field="accessStartDate"/>
                            <i class="${Icon.TOOLTIP.HELP} la-popup-tooltip"
                               data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                        </g:if>
                        <g:else>
                            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                          date="${issueEntitlementInstance.accessStartDate}"/>
                        </g:else>
                    </div>

                    <div class="item">
                        <i class="grey ${Icon.SYM.DATE} la-popup-tooltip"
                           data-content="${message(code: 'subscription.details.access_end')}"></i>
                        <g:if test="${editable}">
                            <ui:xEditable owner="${issueEntitlementInstance}" type="date" field="accessEndDate"/>
                            <i class="${Icon.TOOLTIP.HELP} la-popup-tooltip"
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
                                                <g:message code="tipp.price.localPrice"/>: <ui:xEditable
                                                        field="localPrice"
                                                        owner="${priceItem}"/> <ui:xEditableRefData
                                                        field="localCurrency" owner="${priceItem}" config="Currency"/>
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
                                <i class="${Icon.IE_GROUP} grey la-popup-tooltip" data-content="${message(code: 'issueEntitlementGroup.label')}"></i>

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
                    <ui:statsLink class="ui basic negative"
                                     base="${ConfigMapper.getStatsApiUrl()}"
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
                        <i class="${Icon.STATS}"></i>
                    </ui:statsLink>
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
                                    <ui:statsLink
                                            base="${ConfigMapper.getStatsApiUrl()}"
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
                                    </ui:statsLink>
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
            <ui:filter simple="true">
                <g:form action="show" params="${params}" method="get" class="ui form">
                    <input type="hidden" name="sort" value="${params.sort}">
                    <input type="hidden" name="order" value="${params.order}">

                    <div class="fields three">
                        <div class="field">
                            <label for="filter">${message(code: 'tipp.show.filter_pkg')}</label>
                            <input id="filter" name="filter" value="${params.filter}"/>
                        </div>

                        <div class="field">
                            <ui:datepicker label="default.startsBefore.label" id="startsBefore"
                                              name="startsBefore"
                                              value="${params.startsBefore}"/>
                        </div>

                        <div class="field">
                            <ui:datepicker label="default.endsAfter.label" id="endsAfter" name="endsAfter"
                                              value="${params.endsAfter}"/>
                        </div>
                    </div>

                    <div class="field">
                        <input type="submit" class="${Btn.PRIMARY}"
                               value="${message(code: 'default.button.submit.label')}">
                    </div>

                </g:form>
            </ui:filter>

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
                                <g:link class="${Btn.SIMPLE}" controller="tipp" action="show"
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

<laser:htmlEnd />
