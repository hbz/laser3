<%@ page import="de.laser.remote.ApiSource; de.laser.titles.BookInstance; de.laser.storage.RDStore; de.laser.titles.TitleHistoryEventParticipant" %>

<laser:htmlStart text="${message(code:"tipp.show.label", args:[tipp.name, tipp.pkg.name, tipp.platform.name])}" />

<ui:breadcrumbs>
    <ui:crumb controller="package" action="show" id="${tipp.pkg.id}"
                 text="${tipp.pkg.name} [${message(code: 'package.label')}]"/>
    <ui:crumb text="${tipp.name} [${message(code: 'title.label')}]" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="tipp.show.label" args="${[tipp.name, tipp.pkg.name, tipp.platform.name]}" type="${tipp.titleType}" />

<laser:render template="/templates/meta/identifier" model="${[object: tipp, editable: editable]}"/>

<ui:messages data="${flash}"/>


<div class="la-inline-lists">
    <g:if test="${participantPerpetualAccessToTitle}">
        <div class="ui card">
            <div class="content">
                <div class="header"><g:message code="myinst.currentPermanentTitles.label"/> in: </div>
            </div>

            <div class="content">
                <div class="ui list">
                    <g:each in="${participantPerpetualAccessToTitle}" var="pt">
                        <div class="item">
                            <div class="sixteen wide column">
                                <i class="icon clipboard outline la-list-icon"></i>
                                <g:link controller="subscription"
                                        action="index"
                                        id="${pt.subscription.id}">${pt.subscription.dropdownNamingConvention(contextOrg)}</g:link>
                                &nbsp;
                                <br/>
                                <g:link controller="issueEntitlement"
                                        action="show"
                                        id="${pt.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                <br/>
                            </div>
                        </div>
                    </g:each>

                </div>
            </div>
        </div>
    </g:if>


    <div class="ui card">
        <div class="content">
            <div class="header"><g:message code="title.label"/></div>
        </div>

        <div class="content">

            <!-- START TEMPLATE -->
                <laser:render template="/templates/title_long"
                          model="${[ie: null, tipp: tipp,
                                    showPackage: false, showPlattform: false, showCompact: false, showEmptyFields: true]}"/>
            <!-- END TEMPLATE -->
            <br/>
            <br/>

            <g:if test="${(tipp.titleType == 'Book')}">
                <div class="la-title">${message(code: 'tipp.print')} & ${message(code: 'tipp.online')}</div>
            </g:if>
            <g:elseif test="${tipp.titleType == "Journal"}">
                <div class="la-title">${message(code: 'tipp.coverage')}</div>
            </g:elseif>
            <g:else>
                <div class="la-title">${message(code: 'tipp.online')}</div>
            </g:else>

            <div class="la-icon-list">
                <laser:render template="/templates/tipps/coverages" model="${[ie: null, tipp: tipp]}"/>
            </div>
            <br/>

            <div class="la-title">${message(code: 'tipp.access_dates')}</div>

            <div class="la-icon-list">
                <div class="item">
                    <i class="grey clipboard check clip icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.accessStartDate.tooltip')}"></i>

                    <div class="content">
                        ${message(code: 'tipp.accessStartDate')}: <g:formatDate date="${tipp.accessStartDate}"
                                                                                 format="${message(code: 'default.date.format.notime')}"/>
                    </div>
                </div>

                <div class="item">
                    <i class="grey clipboard check clip icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.accessEndDate.tooltip')}"></i>

                    <div class="content">
                        ${message(code: 'tipp.accessEndDate')}: <g:formatDate date="${tipp.accessEndDate}"
                                                                               format="${message(code: 'default.date.format.notime')}"/>
                    </div>
                </div>
            </div>

            <br/>

            <div class="la-title"><g:message code="tipp.price.plural"/></div>

            <div class="la-icon-list">
                <div class="ui cards">
                    <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                        <div class="item">
                            <div class="ui card">
                                <div class="content">
                                    <div class="la-card-column">
                                        <g:message code="tipp.price.listPrice"/>: <ui:xEditable field="listPrice"
                                                                                             owner="${priceItem}"
                                                                                             overwriteEditable="false"/> <ui:xEditableRefData
                                                field="listCurrency" owner="${priceItem}"
                                                config="Currency"
                                                overwriteEditable="false"/>
                                        <br />
                                        <%--(<g:message code="tipp.price.startDate"/> <ui:xEditable field="startDate"
                                                                                                  type="date"
                                                                                                  owner="${priceItem}"
                                                                                                  overwriteEditable="false"/>-
                                        <g:message code="tipp.price.endDate"/> <ui:xEditable field="endDate"
                                                                                               type="date"
                                                                                               owner="${priceItem}"
                                                                                               overwriteEditable="false"/>)--%>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </g:each>
                </div>
            </div>

            <br/>
        </div>
    </div>

    <div class="ui card">
        <div class="content">
            <div class="header"><g:message code="package.label"/></div>
        </div>

        <div class="content">
            <div class="item">
                <i class="grey icon gift scale la-popup-tooltip la-delay"
                   data-content="${message(code: 'package.label')}"></i>
                <g:link controller="package" action="show"
                        id="${tipp.pkg?.id}">${tipp.pkg?.name}</g:link>

                <br/>
                <br/>
                <g:link controller="package" action="current"
                        id="${tipp.pkg?.id}">
                    <g:message code="package.show.nav.current"/>: <g:message code="package.compare.overview.tipps"/> ${currentTippsCounts}</g:link>
                <br/>
                <g:link controller="package" action="planned"
                        id="${tipp.pkg?.id}">
                    <g:message code="package.show.nav.planned"/>: <g:message code="package.compare.overview.tipps"/> ${plannedTippsCounts}</g:link>
                <br/>
                <g:link controller="package" action="expired"
                        id="${tipp.pkg?.id}">
                    <g:message code="package.show.nav.expired"/>: <g:message code="package.compare.overview.tipps"/> ${expiredTippsCounts}</g:link>

                <br/>
                <g:link controller="package" action="deleted"
                        id="${tipp.pkg?.id}">
                    <g:message code="package.show.nav.deleted"/>: <g:message code="package.compare.overview.tipps"/> ${deletedTippsCounts}</g:link>

                <br/>
                <br/>
                <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                        var="gokbAPI">
                    <g:if test="${tipp.pkg.gokbId}">
                        <a role="button"
                           class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                           data-content="${message(code: 'wekb')}"
                           href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/public/packageContent/?id=' + tipp.pkg.gokbId : '#'}"
                           target="_blank"><i class="la-gokb  icon"></i>
                        </a>
                    </g:if>
                </g:each>

            </div>
        </div>
    </div>

    <div class="ui card">
        <div class="content">
            <div class="header"><g:message code="platform.label"/></div>
        </div>

        <div class="content">
            <div class="item">
                <i class="grey icon cloud la-popup-tooltip la-delay"
                   data-content="${message(code: 'platform.label')}"></i>
                <g:if test="${tipp.platform.name}">
                    <g:link controller="platform" action="show" id="${tipp.platform.id}">
                        ${tipp.platform.name}
                    </g:link>
                </g:if>
                <g:else>
                    ${message(code: 'default.unknown')}
                </g:else>

                <g:if test="${tipp.hostPlatformURL}">
                    <br/>
                    <ui:linkWithIcon
                            href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
                </g:if>
            </div>

            <div class="item">
                ${message(code: 'platform.provider')}:  <g:if test="${tipp.platform.org}">
                    <g:link controller="organisation" action="show"
                            id="${tipp.platform.org.id}">${tipp.platform.org.name}</g:link>
                </g:if>

            </div>

            <div class="item">${message(code: 'platform.primaryURL')}:               ${tipp.platform.primaryUrl}
                <g:if test="${tipp.platform.primaryUrl}">
                    <a role="button" class="ui icon mini blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.tooltip.callUrl')}"
                       href="${tipp.platform.primaryUrl?.contains('http') ? tipp.platform.primaryUrl : 'http://' + tipp.platform.primaryUrl}"
                       target="_blank"><i class="external alternate icon"></i></a>
                </g:if>
            </div>
            <br/>
            <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                    var="gokbAPI">
                <g:if test="${tipp.platform.gokbId}">
                    <a role="button"
                       class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                       data-content="${message(code: 'wekb')}"
                       href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/public/platformContent/?id=' + tipp.platform.gokbId : '#'}"
                       target="_blank"><i class="la-gokb  icon"></i>
                    </a>
                </g:if>
            </g:each>

        </div>
    </div>


    <div class="ui card">
        <div class="content">
            <div class="header"><g:message code="title.edit.orglink"/></div>
        </div>

        <div class="content">

            <table class="ui celled la-js-responsive-table la-table table ">
                <thead>
                <tr>
                    %{--<th><g:message code="title.edit.component_id.label"/></th>--}%
                    <th><g:message code="template.orgLinks.name"/></th>
                    <th><g:message code="default.role.label"/></th>
                    <th><g:message code="default.from"/></th>
                    <th><g:message code="default.to"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${tipp.orgs}" var="org">
                    <tr>
                        %{--<td>${org.org.id}</td>--}%
                        <td><g:link controller="organisation" action="show"
                                    id="${org.org.id}">${org.org.name}</g:link></td>
                        <td>${org.roleType.getI10n("value")}</td>
                        <td>
                            <ui:xEditable owner="${org}" type="date" field="startDate"/>
                        </td>
                        <td>
                            <ui:xEditable owner="${org}" type="date" field="endDate"/>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>

  <div class="ui card">
                <div class="content">
                    <div class="header">${message(code: 'title.show.history.label')}</div>
                </div>
                <div class="content">
                    <table class="ui celled la-js-responsive-table la-table table">
                        <thead>
                            <tr>
                                <th>${message(code: 'default.date.label')}</th>
                                <th>${message(code: 'default.from')}</th>
                                <th>${message(code: 'default.to')}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${titleHistory}" var="tiH">
                                <tr>
                                    <td><g:formatDate date="${tiH.eventDate}" formatName="default.date.format.notime"/></td>
                                    <td>
                                        <g:if test="${tiH.from}">
                                            ${tiH.from}
                                        </g:if>
                                        <g:else>
                                            <g:each in="${TitleHistoryEventParticipant.findAllByParticipantNotEqualAndEvent(tipp,tiH)}" var="p">
                                                <g:if test="${p.participantRole=='to'}">
                                                    <g:link controller="tipp" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == tiH.id}">font-weight:bold</g:if>">${p.participant.name} (${p.participant.pkg.name} / ${p.participant.platform.name})</span></g:link><br/>
                                                </g:if>
                                            </g:each>
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${tiH.to}">
                                            ${tiH.to}
                                        </g:if>
                                        <g:else>
                                            <g:each in="${TitleHistoryEventParticipant.findAllByParticipantNotEqualAndEvent(tipp,tiH)}" var="p">
                                                <g:if test="${p.participantRole=='from'}">
                                                    <g:link controller="tipp" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == tiH.id}">font-weight:bold</g:if>">${p.participant.name} (${p.participant.pkg.name} / ${p.participant.platform.name})</span></g:link><br/>
                                                </g:if>
                                            </g:each>
                                        </g:else>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </div>
            </div>
</div>

<laser:htmlEnd />
