<%@ page import="de.laser.titles.TitleInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title><g:message code="tipp.show.label"
                      args="${[titleInstanceInstance?.title, tipp.pkg.name, tipp.platform.name]}"/></title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="package" action="show" id="${tipp.pkg.id}"
                 text="${tipp.pkg.name} [${message(code: 'package.label')}]"/>
    <semui:crumb text="${tipp.title.title} [${message(code: 'title.label')}]" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
<g:message code="tipp.show.label" args="${[titleInstanceInstance?.title, tipp.pkg.name, tipp.platform.name]}"/>
</h1>

<g:render template="/templates/meta/identifier" model="${[object: tipp, editable: editable]}"/>

<semui:messages data="${flash}"/>

<semui:form>

    <div class="la-icon-list">

        %{--<div class="item">
            <i class="grey key icon la-popup-tooltip la-delay"
               data-content="${message(code: 'default.access.label')}"></i>

            <div class="content">
                ${tipp.availabilityStatus?.getI10n('value')}
            </div>
        </div>--}%

        <div class="item">
            <i class="grey clipboard check clip icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.show.accessStart')}"></i>

            <div class="content">
                <g:formatDate date="${tipp.accessStartDate}" format="${message(code:'default.date.format.notime')}"/>
            </div>
        </div>

        <div class="item">
            <i class="grey clipboard check clip icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.show.accessEnd')}"></i>

            <div class="content">
                <g:formatDate date="${tipp.accessEndDate}" format="${message(code:'default.date.format.notime')}"/>
            </div>
        </div>

        <dt><g:message code="tipp.coverage"/></dt>
        <dd>
            <div class="ui cards">
                <g:each in="${tipp.coverages}" var="coverage">
                    <div class="ui card">
                        <div class="content">
                            <div class="la-card-column">
                                <!-- von -->
                                <g:formatDate date="${coverage.startDate}"
                                              format="${message(code: 'default.date.format.notime')}"/><br />
                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.volume')}"></i>
                                ${coverage.startVolume}<br />
                                <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.issue')}"></i>
                                ${coverage.startIssue}
                                <semui:dateDevider/>
                                <!-- bis -->
                                <g:formatDate date="${coverage.endDate}"
                                              format="${message(code: 'default.date.format.notime')}"/><br />
                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.volume')}"></i>
                                ${coverage.endVolume}<br />
                                <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                   data-content="${message(code: 'tipp.issue')}"></i>
                                ${coverage.endIssue}
                            </div>

                            <div class="la-card-column-with-row">
                                <div class="la-card-row">
                                    <i class="grey icon file alternate right la-popup-tooltip la-delay"
                                       data-content="${message(code: 'tipp.coverageDepth')}"></i>${coverage.coverageDepth}<br />
                                    <i class="grey icon quote right la-popup-tooltip la-delay"
                                       data-content="${message(code: 'tipp.coverageNote')}"></i>${coverage.coverageNote}<br />
                                    <i class="grey icon hand paper right la-popup-tooltip la-delay"
                                       data-content="${message(code: 'tipp.embargo')}"></i>${coverage.embargo}<br />
                                </div>
                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
        </dd>

        <div class="item">
            <i class="grey key icon la-popup-tooltip la-delay"
               data-content="${message(code: 'default.status.label')}"></i>

            <div class="content">
                ${tipp.status.getI10n("value")}
            </div>
        </div>

        <div class="item">
            <i class="grey edit icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.show.statusReason')}"></i>

            <div class="content">
                ${tipp.statusReason?.getI10n("value")}
            </div>
        </div>

        <div class="item">
            <i class="grey lock open icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.delayedOA')}"></i>

            <div class="content">
                ${tipp.delayedOA?.getI10n("value")}"
            </div>
        </div>

        <div class="item">
            <i class="grey lock open alternate icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.hybridOA')}"></i>

            <div class="content">
                ${tipp.hybridOA?.getI10n("value")}
            </div>
        </div>


        <div class="item">
            <i class="grey money icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.paymentType')}"></i>

            <div class="content">
                ${tipp.payment?.getI10n("value")}
            </div>
        </div>


        <div class="item">
            <i class="grey icon cloud la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.tooltip.changePlattform')}"></i>

            <div class="content">
                <g:if test="${tipp?.platform.name}">
                    <g:link controller="platform" action="show" id="${tipp?.platform.id}">
                        ${tipp?.platform.name}
                    </g:link>
                </g:if>
                <g:else>
                    ${message(code: 'default.unknown')}
                </g:else>
            </div>
        </div>
        <br />
        <g:if test="${tipp.hostPlatformURL}">
            <semui:linkIcon href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
        </g:if>

        <%--
        <br />
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
        </dl>--%>
    </div>
</semui:form>

<br />
<semui:form>

    <h4 class="ui header"><g:message code="titleInstance.tipps.label"
                   default="${message(code: 'titleInstance.tipps.label')}"/>
    </h4>
    <g:if test="${titleInstanceInstance?.tipps}">

        <semui:filter>
            <g:form action="show" params="${params}" method="get" class="ui form">
                <input type="hidden" name="sort" value="${params.sort}">
                <input type="hidden" name="order" value="${params.order}">

                <div class="fields">
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

                    <div class="field la-field-right-aligned">
                        <a href="${request.forwardURI}"
                           class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button"
                               value="${message(code: 'default.button.filter.label')}">
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
                                <span><g:message code="default.date.label"/>: <g:formatDate
                                        format="${message(code: 'default.date.format.notime')}"
                                        date="${covStmt.startDate}"/></span>
                            </div>

                            <div>
                                <span><g:message code="tipp.volume"/>: ${covStmt.startVolume}</span>
                            </div>

                            <div>
                                <span><g:message code="tipp.issue"/>: ${covStmt.startIssue}</span>
                            </div>

                            <div>
                                <span><g:message code="default.date.label"/>: <g:formatDate
                                        format="${message(code: 'default.date.format.notime')}"
                                        date="${covStmt.endDate}"/></span>
                            </div>

                            <div>
                                <span><g:message code="tipp.volume"/>: ${covStmt.endVolume}</span>
                            </div>

                            <div>
                                <span><g:message code="tipp.issue"/>: ${covStmt.endIssue}</span>
                            </div>

                            <div>
                                <span><g:message
                                        code="tipp.coverageDepth"/>: ${covStmt.coverageDepth}</span>
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
                    <td><g:link controller="platform" action="show"
                                id="${t.platform.id}">${t.platform.name}</g:link></td>
                    <td><g:link controller="package" action="show"
                                id="${t.pkg.id}">${t.pkg.name} (${t.pkg.contentProvider?.name})</g:link></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>

</semui:form>
</body>
</html>
