<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'myinst.currentTitles.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="myinst.currentTitles.label" class="active"/>
</semui:breadcrumbs>
<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-term-content="${message(code: 'confirmation.content.exportPartial', default: 'Achtung!  Dennoch fortfahren?')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [format: 'csv']}">
                    ${message(code: 'default.button.exports.csv')}
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles" params="${params + [format: 'csv']}">CSV Export</g:link>
            </g:else>
        </semui:exportDropdownItem>
    <%--<semui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'json']}">JSON Export</g:link>
    </semui:exportDropdownItem>
    <semui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'xml']}">XML Export</g:link>
    </semui:exportDropdownItem>--%>
        <semui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-term-content="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [exportKBart: true]}">
                    KBart Export
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles"
                        params="${params + [exportKBart: true]}">KBart Export</g:link>
            </g:else>
        </semui:exportDropdownItem>
        <g:each in="${transforms}" var="transkey,transval">
            <semui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-term-content="${message(code: 'confirmation.content.exportPartial', default: 'Achtung!  Dennoch fortfahren?')}"
                            data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                            params="${params + [format: 'xml', transformId: transkey]}">
                        ${transval.name}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="currentTitles"
                            params="${params + [format: 'xml', transformId: transkey]}">${transval.name}</g:link>
                </g:else>
            </semui:exportDropdownItem>
        </g:each>
    </semui:exportDropdown>
</semui:controlButtons>

<semui:messages data="${flash}"/>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${message(code: 'myinst.currentTitles.label', default: 'Current Titles')}
<semui:totalNumber total="${num_ti_rows}"/>
</h1>

<semui:filter>
    <g:form id="filtering-form" action="currentTitles" controller="myInstitution" method="get" class="ui form">

        <g:set var="filterSub" value="${params.filterSub ? params.list('filterSub') : "all"}"/>
        <g:set var="filterPvd" value="${params.filterPvd ? params.list('filterPvd') : "all"}"/>
        <g:set var="filterHostPlat" value="${params.filterHostPlat ? params.list('filterHostPlat') : "all"}"/>
        <g:set var="filterOtherPlat" value="${params.filterOtherPlat ? params.list('filterOtherPlat') : "all"}"/>


        <div class="two fields">
            <div class="field">
                <label>${message(code: 'default.search.text', default: 'Search text')}</label>
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
                            value="">${message(code: 'myinst.currentTitles.all_subs', default: 'All Subscriptions')}</option>
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
                            value="">${message(code: 'myinst.currentTitles.all_providers', default: 'All Content Providers')}</option>
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
                            value="">${message(code: 'myinst.currentTitles.all_host_platforms', default: 'All Host Platforms')}</option>
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

            <div class="field">
                <label for="filterPvd">${message(code: 'default.all_other.platforms.label')}</label>
                <select name="filterOtherPlat" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterOtherPlat.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_other_platforms', default: 'All Additional Platforms')}</option>
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
            </div>
        </div>

        <div class="two fields">

            <div class="field">
                <label>${message(code: 'myinst.currentTitles.dupes', default: 'Titles we subscribe to through 2 or more packages')}</label>

                <div class="ui checkbox">
                    <input type="checkbox" class="hidden" name="filterMultiIE"
                           value="${true}" <%=(params.filterMultiIE) ? ' checked="true"' : ''%>/>
                </div>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="hidden" name="filterSet" value="true"/>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label', default: 'Filter')}"/>
            </div>
        </div>

    </g:form>
</semui:filter>

<div>
    <div>
        <span>${message(code: 'title.plural', default: 'Titles')} ( ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (titles.size())), num_ti_rows])} )</span>

        <div>
            <g:form action="subscriptionBatchUpdate" params="${[id: subscriptionInstance?.id]}" class="ui form">
                <g:set var="counter" value="${offset + 1}"/>
                <table class="ui sortable celled la-table table ">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <g:sortableColumn params="${params}" property="tipp.title.sortTitle"
                                          title="${message(code: 'title.label', default: 'Title')}"/>
                        <th>${message(code: 'subscription.details.startDate', default: 'Earliest Date')}</th>
                        <th>${message(code: 'subscription.details.endDate', default: 'Latest Date')}</th>
                        <th style="width: 30%"><div class="ui two column grid">
                            <div class="sixteen wide column">
                                ${message(code: 'myinst.currentTitles.sub_content', default: 'Subscribed Content')}
                            </div>
                            <div class="eight wide column">
                                ${message(code: 'subscription.details.coverage_dates')}
                                <br>
                                ${message(code: 'default.from')}
                                <br>
                                ${message(code: 'default.to')}
                            </div>
                            <div class="eight wide column">
                                ${message(code: 'subscription.details.access_dates', default: 'Access')}
                                <br>
                                ${message(code: 'default.from')}
                                <br>
                                ${message(code: 'default.to')}
                            </div>
                        </div>
                        </th>

                    </tr>

                    </thead>
                    <g:each in="${titles}" var="ti" status="jj">
                        <tr>
                            <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                            <td>
                                <semui:listIcon type="${ti?.type?.value}"/>
                                <strong><g:link controller="title" action="show"
                                                id="${ti?.id}">${ti?.title}</g:link></strong>

                                <g:if test="${ti instanceof com.k_int.kbplus.BookInstance && ti?.volume}">
                                    (${message(code: 'title.volume.label')} ${ti?.volume})
                                </g:if>

                                <g:if test="${ti instanceof com.k_int.kbplus.BookInstance && (ti?.firstAuthor || ti?.firstEditor)}">
                                    <br><b>${ti?.getEbookFirstAutorOrFirstEditor()}</b>
                                </g:if>

                                <g:if test="${ti instanceof com.k_int.kbplus.BookInstance}">
                                    <div class="item"><b>${message(code: 'title.editionStatement.label')}:</b> ${ti?.editionStatement}
                                    </div>
                                    <br/>
                                </g:if>


                                <g:each in="${ti?.tipps?.sort { it?.platform?.name }}" var="tipp">

                                        <g:if test="${tipp?.hostPlatformURL}">
                                            <a class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                               data-content="${message(code: 'tipp.tooltip.callUrl')}"
                                               href="${tipp?.hostPlatformURL.contains('http') ? tipp?.hostPlatformURL : 'http://' + tipp?.hostPlatformURL}"
                                               target="_blank"><i class="share square icon"></i></a>
                                        </g:if>
                                </g:each>

                                <g:each in="${ti?.ids?.sort { it?.identifier?.ns?.ns }}" var="id">
                                    <g:if test="${id.identifier.ns.ns == 'originediturl'}">
                                        <span class="ui small teal image label">
                                            ${id.identifier.ns.ns}: <div class="detail"><a
                                                href="${id.identifier.value}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                                        </div>
                                        </span>
                                        <span class="ui small teal image label">
                                            ${id.identifier.ns.ns}: <div class="detail"><a
                                                href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                                        </div>
                                        </span>
                                    </g:if>
                                    <g:else>
                                        <span class="ui small teal image label">
                                            ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                                        </span>
                                    </g:else>
                                </g:each>
                                <br/>

                                <div class="ui list">

                                    <g:set var="platforms" value="${ti?.tipps?.sort { it?.platform?.name }}"/>
                                    <g:each in="${platforms.groupBy{it.platform?.id}}" var="platformID">

                                        <g:set var="platform" value="${com.k_int.kbplus.Platform.get(platformID.key)}"/>

                                        <div class="item"><b>${message(code: 'tipp.platform', default: 'Platform')}:</b>
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
                                                <a class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay"
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
                            <td style="white-space:nowrap">${title_coverage_info.latest ?: message(code: 'myinst.currentTitles.to_current', default: 'To Current')}</td>
                            <td >

                                <div class="ui two column grid">
                                    <g:each in="${title_coverage_info.ies}" var="ie">
                                        <div class="sixteen wide column">
                                            <i class="icon folder open outline la-list-icon"></i>
                                            <g:link controller="subscription" action="index"
                                                    id="${ie.subscription.id}">${ie.subscription.name}</g:link>
                                            &nbsp;
                                            <br/>
                                            <g:link controller="issueEntitlement" action="show"
                                                    id="${ie.id}">${message(code: 'myinst.currentTitles.full_ie', default: 'Full Issue Entitlement Details')}</g:link>
                                            <br/>
                                        </div>

                                        <div class="eight wide centered column">
                                            <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance}">

                                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'title.dateFirstInPrint.label')}"></i>
                                                ${ie?.tipp?.title?.dateFirstInPrint}
                                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'title.dateFirstOnline.label')}"></i>
                                                ${ie?.tipp?.title?.dateFirstOnline}

                                            </g:if>
                                            <g:else>

                                                <!-- von --->
                                                <semui:xEditable owner="${ie}" type="date" field="startDate"/><br>
                                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'tipp.volume')}"></i>
                                                <semui:xEditable owner="${ie}" field="startVolume"/><br>

                                                <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'tipp.issue')}"></i>
                                                <semui:xEditable owner="${ie}" field="startIssue"/>
                                                <semui:dateDevider/>
                                                <!-- bis -->
                                                <semui:xEditable owner="${ie}" type="date" field="endDate"/><br>
                                                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'tipp.volume')}"></i>
                                                <semui:xEditable owner="${ie}" field="endVolume"/><br>

                                                <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'tipp.issue')}"></i>
                                                <semui:xEditable owner="${ie}" field="endIssue"/>
                                            </g:else>

                                        </div>
                                        <div class="eight wide centered column">
                                        <!-- von --->
                                            <g:if test="${editable}">
                                                <semui:xEditable owner="${ie}" type="date" field="accessStartDate"/>
                                                <i class="grey question circle icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'subscription.details.access_start.note', default: 'Leave empty to default to sub start date')}"></i>
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
                                                   data-content="${message(code: 'subscription.details.access_end.note', default: 'Leave empty to default to sub end date')}"></i>
                                            </g:if>
                                            <g:else>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.accessEndDate}"/>
                                            </g:else>
                                        </div>

                                    </g:each>
                                </div>
                            </td>
                        </tr>
                    </g:each>

                </table>
            </g:form>
        </div>
    </div>


    <g:if test="${titles}">
        <semui:paginate action="currentTitles" controller="myInstitution" params="${params}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
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
                                                  title="${message(code: 'title.label', default: 'Title')}"/>
                                <th>ISSN</th>
                                <th>eISSN</th>
                                <th>${message(code: 'subscription.details.startDate', default: 'Earliest Date')}</th>
                                <th>${message(code: 'subscription.details.endDate', default: 'Latest Date')}</th>
                                <th>${message(code: 'subscription.label', default: 'Subscription')}</th>
                                <th>${message(code: 'package.content_provider', default: 'Content Provider')}</th>
                                <th>${message(code: 'tipp.host_platform', default: 'Host Platform')}</th>
                                <th>${message(code: 'tipp.additionalPlatforms', default: 'Additional Platforms')}</th>
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

</body>
</html>
