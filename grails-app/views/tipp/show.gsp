<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title><g:message code="tipp.show.label"
                      args="${[tipp.name, tipp.pkg.name, tipp.platform.name]}"/></title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="package" action="show" id="${tipp.pkg.id}"
                 text="${tipp.pkg.name} [${message(code: 'package.label')}]"/>
    <semui:crumb text="${tipp.name} [${message(code: 'title.label')}]" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon type="${tipp.titleType}"/>
<g:message code="tipp.show.label" args="${[tipp.name, tipp.pkg.name, tipp.platform.name]}"/>
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
            <semui:listIcon type="${tipp.titleType}"/>
            <div class="content">
                ${tipp.titleType}
            </div>
        </div>
        <g:if test="${tipp.titleType.contains('Book') && (tipp.firstAuthor || tipp.firstEditor)}">
            <div class="item">
                <i class="grey icon user circle la-popup-tooltip la-delay" data-content="${message(code: 'author.slash.editor')}"></i>
                <div class="content">
                    ${tipp.getEbookFirstAutorOrFirstEditor()}
                </div>
            </div>
        </g:if>

        <g:if test="${tipp.titleType.contains('Book')}">
            <g:if test="${tipp.volume}">
                <div class="item">
                    <i class="grey icon la-books la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                    <div class="content">
                        ${tipp.volume})
                    </div>
                </div>
            </g:if>
            <g:if test="${tipp.editionStatement}">
                <div class="item">
                    <i class="grey icon copy la-popup-tooltip la-delay" data-content="${message(code: 'title.editionStatement.label')}"></i>
                    <div class="content">
                        ${tipp.editionStatement}
                    </div>
                </div>
            </g:if>
            <g:if test="${tipp.editionNumber}">
                <div class="item">
                    <i class="grey icon copy outline la-popup-tooltip la-delay" data-content="${message(code: 'title.editionNumber.label')}"></i>
                    <div class="content">
                        ${tipp.editionNumber}
                    </div>
                </div>
            </g:if>
            <g:if test="${tipp.summaryOfContent}">
                <div class="item">
                    <i class="grey icon desktop la-popup-tooltip la-delay" data-content="${message(code: 'title.summaryOfContent.label')}"></i>
                    <div class="content">
                        ${tipp.summaryOfContent}
                    </div>
                </div>
            </g:if>
            <g:if test="${tipp.seriesName}">
                <div class="item">
                    <i class="grey icon list la-popup-tooltip la-delay" data-content="${message(code: 'title.seriesName.label')}"></i>
                    <div class="content">
                        ${tipp.seriesName}
                    </div>
                </div>
            </g:if>
            <g:if test="${tipp.subjectReference}">
                <div class="item">
                    <i class="grey icon comment alternate la-popup-tooltip la-delay" data-content="${message(code: 'title.subjectReference.label')}"></i>
                    <div class="content">
                        ${tipp.subjectReference}
                    </div>
                </div>
            </g:if>

            <div class="item">
                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                <div class="content">
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstInPrint}"/>
                </div>
            </div>
            <div class="item">
                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                <div class="content"><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                   date="${tipp.dateFirstOnline}"/>
                </div>
            </div>
        </g:if>

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

        <g:if test="${tipp.coverages}">
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
        </g:if>

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

    </div>
</semui:form>

<h3 class="ui header"><g:message code="title.edit.orglink"/></h3>

<table class="ui celled la-table table ">
    <thead>
        <tr>
            %{--<th><g:message code="title.edit.component_id.label"/></th>--}%
            <th><g:message code="template.orgLinks.name"/></th>
            <th><g:message code="template.orgLinks.role"/></th>
            <th><g:message code="title.edit.orglink.from"/></th>
            <th><g:message code="title.edit.orglink.to"/></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${tipp.orgs}" var="org">
            <tr>
                %{--<td>${org.org.id}</td>--}%
                <td><g:link controller="organisation" action="show" id="${org.org.id}">${org.org.name}</g:link></td>
                <td>${org.roleType.getI10n("value")}</td>
                <td>
                    <semui:xEditable owner="${org}" type="date" field="startDate"/>
                </td>
                <td>
                    <semui:xEditable owner="${org}" type="date" field="endDate"/>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<h3 class="ui header">${message(code: 'title.show.history.label')}</h3>
<table class="ui celled la-table table">
    <thead>
        <tr>
            <th>${message(code: 'default.date.label')}</th>
            <th>${message(code: 'title.show.history.from')}</th>
            <th>${message(code: 'title.show.history.to')}</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${titleHistory}" var="th">
            <tr>
                <td><g:formatDate date="${th.eventDate}" formatName="default.date.format.notime"/></td>
                <td>
                    <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='from'}">
                            <g:link controller="title" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br />
                        </g:if>
                    </g:each>
                </td>
                <td>
                    <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='to'}">
                            <g:link controller="title" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br />
                        </g:if>
                    </g:each>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<br />
</body>
</html>
