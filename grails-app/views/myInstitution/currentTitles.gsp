<%@ page import="de.laser.titles.BookInstance; de.laser.Platform" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.currentTitles.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="myinst.currentTitles.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [format: 'csv']}">
                    <g:message code="default.button.exports.csv"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles" params="${params + [format: 'csv']}">CSV Export</g:link>
            </g:else>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [exportXLSX: true]}">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles" params="${params+[exportXLSX: true]}">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </g:else>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [exportKBart: true]}">
                    KBART Export
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles"
                        params="${params + [exportKBart: true]}">KBART Export</g:link>
            </g:else>
        </semui:exportDropdownItem>
    <%--<semui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'json']}">JSON Export</g:link>
    </semui:exportDropdownItem>
    <semui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'xml']}">XML Export</g:link>
    </semui:exportDropdownItem>--%>
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${message(code: 'myinst.currentTitles.label')}
<semui:totalNumber total="${num_ti_rows}"/>
</h1>

<semui:messages data="${flash}"/>

<g:render template="/templates/filter/javascript" />

<semui:filter showFilterButton="true">
    <g:form id="filtering-form" action="currentTitles" controller="myInstitution" method="get" class="ui form">

        <g:set var="filterSub" value="${params.filterSub ? params.list('filterSub') : "all"}"/>
        <g:set var="filterPvd" value="${params.filterPvd ? params.list('filterPvd') : "all"}"/>
        <g:set var="filterHostPlat" value="${params.filterHostPlat ? params.list('filterHostPlat') : "all"}"/>
        <g:set var="filterOtherPlat" value="${params.filterOtherPlat ? params.list('filterOtherPlat') : "all"}"/>


        <div class="two fields">
            <div class="field">
                <label>${message(code: 'default.search.text')}</label>
                <input type="hidden" name="sort" value="${params.sort}">
                <input type="hidden" name="order" value="${params.order}">
                <input type="text" name="filter" value="${params.filter}" style="padding-left:5px;"
                       placeholder="${message(code: 'default.search.ph')}"/>
            </div>

            <semui:datepicker label="myinst.currentTitles.subs_valid_on" id="validOn" name="validOn"
                              value="${validOn}"/>

        </div>

        <div class="two fields">
            <div class="field">
                <label for="filterSub">${message(code: 'subscription.plural')}</label>
                <select id="filterSub" name="filterSub" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterSub.contains("all")) ? ' selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_subs')}</option>
                    <g:each in="${subscriptions}" var="s">
                        <option <%=(filterSub.contains(s.id.toString())) ? 'selected="selected"' : ''%> value="${s.id}"
                                                                                                        title="${s.dropdownNamingConvention(institution)}">
                            ${s.dropdownNamingConvention(institution)}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="filterPvd">${message(code: 'default.agency.provider.plural.label')}</label>
                <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterPvd.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_providers')}</option>
                    <g:each in="${providers}" var="p">
                        <%
                            def pvdId = p[0].id.toString()
                            def pvdName = p[0].name
                        %>
                        <option <%=(filterPvd.contains(pvdId)) ? 'selected' : ''%> value="${pvdId}" title="${pvdName}">
                            ${pvdName}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="filterPvd">${message(code: 'default.host.platforms.label')}</label>
                <select name="filterHostPlat" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterHostPlat.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_host_platforms')}</option>
                    <g:each in="${hostplatforms}" var="hp">
                        <%
                            def hostId = hp[0].id.toString()
                            def hostName = hp[0].name
                        %>
                        <option <%=(filterHostPlat.contains(hostId)) ? 'selected' : ''%> value="${hostId}"
                                                                                         title="${hostName}">
                            ${hostName}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="hidden" name="filterSet" value="true"/>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}"/>
            </div>

            <%--<div class="field">
                <label for="filterPvd">${message(code: 'default.all_other.platforms.label')}</label>
                <select name="filterOtherPlat" multiple="" class="ui search selection fluid dropdown">
                    <option <%= (filterOtherPlat.contains("all")) ? 'selected' : '' %>
                            value="">${message(code: 'myinst.currentTitles.all_other_platforms')}</option>
                    <g:each in="${otherplatforms}" var="op">

                        <%
                            def platId = op[0].id.toString()
                            def platName = op[0].name
                        %>
                        <option <%=(filterOtherPlat.contains(platId)) ? 'selected' : ''%> value="${platId}"
                                                                                          title="${platName}">
                            ${platName}
                        </option>
                    </g:each>
                </select>
            </div>--%>
        </div>

        <%--<div class="two fields">

        <%-- class="field">
            <label for="filterMultiIE">${message(code: 'myinst.currentTitles.dupes')}</label>

            <div class="ui checkbox">
                <input type="checkbox" class="hidden" name="filterMultiIE" id="filterMultiIE"
                       value="${true}" <%=(params.filterMultiIE) ? ' checked="true"' : ''%>/>
            </div>
        </div>
        </div>--%>

    </g:form>
</semui:filter>

<div class="la-clear-before">
    <div>
        %{--<span>${message(code: 'title.plural')} ( ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (titles.size())), num_ti_rows])} )</span>--}%

        <div>
            <g:if test="${titles}">
            <g:form action="subscriptionBatchUpdate" params="${[id: subscription.id]}" class="ui form">
                <g:set var="counter" value="${offset + 1}"/>
                <table class="ui sortable celled la-table table ">
                    <thead>
                        <tr>
                            <th>${message(code: 'sidewide.number')}</th>
                            <g:sortableColumn params="${params}" property="tipp.title.sortTitle"
                                              title="${message(code: 'title.label')}"/>
                            <th>${message(code: 'subscription.details.startDate')}</th>
                            <th>${message(code: 'subscription.details.endDate')}</th>
                            <th style="width: 30%">
                                <div class="ui three column grid">
                                    <div class="sixteen wide column">
                                        ${message(code: 'myinst.currentTitles.sub_content')}
                                    </div>
                                    <div class="eight wide column">
                                        ${message(code: 'subscription.details.coverage_dates')}
                                        <br />
                                        ${message(code: 'default.from')}
                                        <br />
                                        ${message(code: 'default.to')}
                                    </div>
                                    <div class="eight wide column">
                                        ${message(code: 'subscription.details.access_dates')}
                                        <br />
                                        ${message(code: 'default.from')}
                                        <br />
                                        ${message(code: 'default.to')}
                                    </div>
                                    <div class="sixteen wide column">
                                        <g:message code="subscription.details.prices"/>
                                    </div>
                                </div>
                            </th>
                        </tr>
                    </thead>
                    <g:each in="${titles}" var="ti" status="jj">
                        <tr>
                            <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                            <td>
                                <semui:listIcon type="${ti.printTitleType()}"/>
                                <strong><g:link controller="title" action="show"
                                                id="${ti?.id}">${ti?.title}</g:link></strong>

                                <g:if test="${ti instanceof BookInstance && ti.volume}">
                                    (${message(code: 'title.volume.label')} ${ti.volume})
                                </g:if>

                                <g:if test="${ti instanceof BookInstance && (ti.firstAuthor || ti.firstEditor)}">
                                    <br /><strong>${ti?.getEbookFirstAutorOrFirstEditor()}</strong>
                                </g:if>

                                <g:if test="${ti instanceof BookInstance && ti.editionStatement}">
                                    <div class="item"><strong>${message(code: 'title.editionStatement.label')}:</strong> ${ti.editionStatement}
                                    </div>
                                    <br />
                                </g:if>


                                <g:each in="${ti?.tipps?.unique { a, b -> a?.platform?.id <=> b?.platform?.id }.sort { it?.platform?.name }}" var="tipp">

                                        <g:if test="${tipp?.hostPlatformURL}">
                                            <a role="button" class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.tooltip.callUrl')}"
                                               href="${tipp?.hostPlatformURL.contains('http') ? tipp?.hostPlatformURL : 'http://' + tipp?.hostPlatformURL}"
                                               target="_blank"><i class="share square icon"></i></a>
                                        </g:if>
                                </g:each>

                                <g:each in="${ti?.ids?.sort { it.ns.ns }}" var="id">
                                    <span class="ui small blue image label">
                                        ${id.ns.ns}: <div class="detail">${id.value}</div>
                                    </span>
                                </g:each>
                                <br />

                                <div class="ui list">

                                    <g:set var="platforms" value="${ti?.tipps?.sort { it?.platform?.name }}"/>
                                    <g:each in="${platforms.groupBy{it.platform?.id}}" var="platformID">

                                        <g:set var="platform" value="${Platform.get(platformID.key)}"/>

                                        <div class="item"><strong>${message(code: 'tipp.platform')}:</strong>
                                            <g:if test="${platform?.name}">
                                                ${platform?.name}
                                            </g:if>
                                            <g:else>${message(code: 'default.unknown')}</g:else>

                                            <g:if test="${platform?.name}">
                                                <g:link class="ui icon mini  button la-js-dont-hide-button la-popup-tooltip la-delay"
                                                        data-content="${message(code: 'tipp.tooltip.changePlattform')}"
                                                        controller="platform" action="show"
                                                        id="${platform?.id}"><i
                                                        class="pencil alternate icon"></i></g:link>
                                            </g:if>
                                            <g:if test="${platform?.primaryUrl}">
                                                <a role="button" class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'tipp.tooltip.callUrl')}"
                                                   href="${platform?.primaryUrl?.contains('http') ? platform?.primaryUrl : 'http://' + platform?.primaryUrl}"
                                                   target="_blank"><i class="share square icon"></i></a>
                                            </g:if>

                                        </div>
                                </g:each>
                                </div>

                            </td>

                            <g:set var="title_coverage_info"
                                   value="${ti.getInstitutionalCoverageSummary(institution, message(code: 'default.date.format.notime'), date_restriction)}"/>

                            <td style="white-space:nowrap">${title_coverage_info.earliest}</td>
                            <td style="white-space:nowrap">${title_coverage_info.latest ?: message(code: 'myinst.currentTitles.to_current')}</td>
                            <td >

                                <div class="ui three column grid">
                                    <g:each in="${title_coverage_info.ies}" var="ie">
                                        <div class="sixteen wide column">
                                            <i class="icon clipboard outline outline la-list-icon"></i>
                                            <g:link controller="subscription" action="index"
                                                    id="${ie.subscription.id}">${ie.subscription.name}</g:link>
                                            &nbsp;
                                            <br />
                                            <g:link controller="issueEntitlement" action="show"
                                                    id="${ie.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                            <br />
                                        </div>

                                        <div class="eight wide centered column">
                                            <g:if test="${ie.tipp.title instanceof BookInstance}">

                                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.tipp.title.dateFirstInPrint}"/>
                                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.tipp.title.dateFirstOnline}"/>


                                            </g:if>
                                            <g:else>

                                                <!-- von --->
                                                <g:each in="${ie.coverages}" var="coverage" status="i">

                                                    <semui:xEditable owner="${coverage}" type="date" field="startDate"/><br />

                                                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'tipp.volume')}"></i>
                                                    <semui:xEditable owner="${coverage}" field="startVolume"/><br />

                                                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'tipp.issue')}"></i>
                                                    <semui:xEditable owner="${coverage}" field="startIssue"/>

                                                    <semui:dateDevider/>
                                                    <!-- bis -->
                                                    <semui:xEditable owner="${coverage}" type="date" field="endDate"/><br />
                                                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'tipp.volume')}"></i>
                                                    <semui:xEditable owner="${coverage}" field="endVolume"/><br />

                                                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'tipp.issue')}"></i>
                                                    <semui:xEditable owner="${coverage}" field="endIssue"/>

                                                </g:each>
                                            </g:else>

                                        </div>
                                        <div class="eight wide centered column">

                                        <!-- von --->
                                            <g:if test="${editable}">
                                                <semui:xEditable owner="${ie}" type="date" field="accessStartDate"/>
                                                <i class="grey question circle icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                                            </g:if>
                                            <g:else>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.accessStartDate}"/>
                                            </g:else>
                                            <semui:dateDevider/>
                                        <!-- bis -->
                                            <g:if test="${editable}">
                                                <semui:xEditable owner="${ie}" type="date" field="accessEndDate"/>
                                                <i class="grey question circle icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                                            </g:if>
                                            <g:else>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.accessEndDate}"/>
                                            </g:else>
                                        </div>
                                        <div class="sixteen wide column">
                                            <g:if test="${ie.priceItem}">
                                                <g:message code="tipp.listPrice"/>: <g:formatNumber number="${ie.priceItem.listPrice}" type="currency" currencyCode="${ie.priceItem.listCurrency?.value}" currencySymbol="${ie.priceItem.listCurrency?.value}"/><br />
                                                <g:message code="tipp.localPrice"/>: <g:formatNumber number="${ie.priceItem.localPrice}" type="currency" currencyCode="${ie.priceItem.localCurrency?.value}" currencySymbol="${ie.priceItem.localCurrency?.value}"/>
                                                (<g:message code="tipp.priceDate"/> <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.priceItem.priceDate}"/>)
                                            </g:if>
                                        </div>
                                    </g:each>
                                </div>
                            </td>
                        </tr>
                    </g:each>

                </table>
            </g:form>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"title.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object" args="${[message(code:"title.plural")]}"/></strong>
                </g:else>
            </g:else>
        </div>
    </div>


    <g:if test="${titles}">
        <semui:paginate action="currentTitles" controller="myInstitution" params="${params}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}" max="${max}"
                        total="${num_ti_rows}"/>
    </g:if>

    <g:if env="development">
        <!-- For Test Only -->
        <div class="accordion" id="accordions">
            <div class="accordion-group">
                <div class="accordion-heading">
                    <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordions"
                       href="#collapse-full-table">
                        For Test Only: Full Table (show/hide)
                    </a>
                </div>

                <div id="collapse-full-table" class="accordion-body collapse out">
                    <div class="accordion-inner">
                        <table class="ui sortable celled la-table table">
                            <tr>
                                <g:sortableColumn params="${params}" property="tipp.title.sortTitle"
                                                  title="${message(code: 'title.label')}"/>
                                <th>ISSN</th>
                                <th>eISSN</th>
                                <th>${message(code: 'subscription.details.startDate')}</th>
                                <th>${message(code: 'subscription.details.endDate')}</th>
                                <th>${message(code: 'default.subscription.label')}</th>
                                <th>${message(code: 'package.content_provider')}</th>
                                <th>${message(code: 'tipp.host_platform')}</th>
                                <th>${message(code: 'tipp.additionalPlatforms')}</th>
                            </tr>
                            <g:each in="${entitlements}" var="ie">
                                <tr>
                                    <td>${ie.tipp.title.title}</td>
                                    <td>${ie.tipp.title.getIdentifierValue('ISSN')}</td>
                                    <td>${ie.tipp.title.getIdentifierValue('eISSN')}</td>
                                    <td><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ie.startDate}"/></td>
                                    <td><g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                      date="${ie.endDate}"/></td>
                                    <td>${ie.subscription.name}</td>
                                    <td>
                                        <g:each in="${ie.tipp.pkg.orgs}" var="role">
                                            <g:if test="${role.roleType?.value?.equals('Content Provider')}">${role.org.name}</g:if>
                                        </g:each>
                                    </td>
                                    <td><div><i class="icon-globe"></i><span>${ie.tipp.platform.name}</span></div></td>
                                    <td>
                                        <g:each in="${ie.tipp.additionalPlatforms}" var="p">
                                            <div><i class="icon-globe"></i><span>${p.platform.name}</span></div>
                                        </g:each>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <!-- End - For Test Only -->
    </g:if>
</div>

<semui:debugInfo>
    <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</semui:debugInfo>

</body>
</html>
