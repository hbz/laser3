<%@ page import="de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.IssueEntitlement;de.laser.Platform; de.laser.remote.ApiSource; de.laser.PermanentTitle; de.laser.Subscription" %>
<laser:htmlStart message="myinst.currentTitles.label"/>

<ui:breadcrumbs>
    <ui:crumb message="myinst.currentTitles.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
    <%--
    <ui:exportDropdownItem>
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
    </ui:exportDropdownItem>
    <ui:exportDropdownItem>
        <g:if test="${filterSet}">
            <g:link class="item js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                    data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                    params="${params + [exportXLSX: true]}">
                <g:message code="default.button.exports.xls"/>
            </g:link>
        </g:if>
        <g:else>
            <g:link class="item" action="currentTitles" params="${params + [exportXLSX: true]}">
                <g:message code="default.button.exports.xls"/>
            </g:link>
        </g:else>
    </ui:exportDropdownItem>
    --%>
        <g:if test="${num_ti_rows < 1000000}">
            <ui:exportDropdownItem>
                <a class="item" data-ui="modal" href="#individuallyExportTippsModal">Export</a>
            </ui:exportDropdownItem>
        </g:if>
        <g:else>
            <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
        </g:else>
        <ui:exportDropdownItem>
            <%--<g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [exportKBart: true]}">
                    KBART Export
                </g:link>
            </g:if>
            <g:else>--%>
                <g:link class="item kbartExport" params="${params + [exportKBart: true]}">KBART Export</g:link>
            <%--</g:else>--%>
        </ui:exportDropdownItem>
    <%--<ui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'json']}">JSON Export</g:link>
    </ui:exportDropdownItem>
    <ui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'xml']}">XML Export</g:link>
    </ui:exportDropdownItem>--%>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="myinst.currentTitles.label" total="${num_ti_rows}" floated="true"/>

<ui:messages data="${flash}"/>

<g:set var="availableStatus"
       value="${RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS) - RDStore.TIPP_STATUS_REMOVED}"/>

<ui:filter>
    <g:form id="filtering-form" action="currentTitles" controller="myInstitution" method="get" class="ui form">


        <div class="two fields">
            <div class="field">
                <label>${message(code: 'default.search.text')}</label>
                <input type="hidden" name="sort" value="${params.sort}">
                <input type="hidden" name="order" value="${params.order}">
                <input type="text" name="filter" value="${params.filter}" style="padding-left:5px;"
                       placeholder="${message(code: 'default.search.ph')}"/>
            </div>

            <%--
            Filter i.m.h.o. unnecessary because controlled with subscription status
            <ui:datepicker label="myinst.currentTitles.subs_valid_on" id="validOn" name="validOn"
                           value="${validOn}"/>
            --%>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="filterSub">${message(code: 'subscription.plural')}</label>
                <div class="ui search selection fluid multiple dropdown" id="filterSub">
                    <input type="hidden" name="filterSub"/>
                    <div class="default text"><g:message code="default.select.all.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                %{--
                <select id="filterSub" name="filterSub" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterSub.contains("all")) ? ' selected' : '' %>--%>
                            value="">${message(code: 'default.select.all.label')}</option>
                    <g:each in="${subscriptions}" var="s">
                        <option <%=(filterSub.contains(s.id.toString())) ? 'selected="selected"' : ''%> value="${s.id}"
                                                                                                        title="${s.dropdownNamingConvention(institution)}">
                            ${s.dropdownNamingConvention(institution)}
                        </option>
                    </g:each>
                </select>
                --}%

            </div>

            <div class="field">
                <label for="filterPvd">${message(code: 'default.agency.provider.plural.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="filterPvd">
                    <input type="hidden" name="filterPvd"/>
                    <div class="default text"><g:message code="default.select.all.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                %{--
                <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterPvd.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'default.select.all.label')}</option>
                    <g:each in="${providers}" var="p">
                        <%
                            def pvdId = p[0].toString()
                            def pvdName = p[1]
                        %>
                        <option <%=(filterPvd.contains(pvdId)) ? 'selected' : ''%> value="${pvdId}" title="${pvdName}">
                            ${pvdName}
                        </option>
                    </g:each>
                </select>
                --}%
            </div>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="filterHostPlat">${message(code: 'default.host.platforms.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="filterHostPlat">
                    <input type="hidden" name="filterHostPlat"/>
                    <div class="default text"><g:message code="default.select.all.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                %{--
                <select name="filterHostPlat" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterHostPlat.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'default.select.all.label')}</option>
                    <g:each in="${hostplatforms}" var="hp">
                        <%
                            def hostId = hp[0].toString()
                            def hostName = hp[1]
                        %>
                        <option <%=(filterHostPlat.contains(hostId)) ? 'selected' : ''%> value="${hostId}"
                                                                                         title="${hostName}">
                            ${hostName}
                        </option>
                    </g:each>
                </select>
                --}%
            </div>

            <div class="field">
                <label for="status">
                    ${message(code: 'default.status.label')}
                </label>
                <select name="status" id="status" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${availableStatus}" var="status">
                        <option <%=(params.list('status')?.contains(status.id.toString())) ? 'selected="selected"' : ''%>
                                value="${status.id}">
                            ${status.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>

    <%--<div class="two fields">
      <div class="field">
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
       </div>
        </div>--%>

    <%--<div class="two fields">

    <%-- class="field">
        <label for="filterMultiIE">${message(code: 'myinst.currentTitles.dupes')}</label>

        <div class="ui checkbox">
            <input type="checkbox" class="hidden" name="filterMultiIE" id="filterMultiIE"
                   value="${true}" <%=(params.filterMultiIE) ? ' checked="true"' : ''%>/>
        </div>
    </div>
    </div>--%>

        <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}"
               class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
            <input type="hidden" name="filterSet" value="true"/>
            <input type="submit" class="ui primary button"
                   value="${message(code: 'default.button.filter.label')}"/>
        </div>

    </g:form>
</ui:filter>

<div id="downloadWrapper"></div>

<ui:tabs actionName="${actionName}">
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'currentIEs']"
                 text="${message(code: "package.show.nav.current")}" tab="currentIEs"
                 counts="${currentIECounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'plannedIEs']"
                 text="${message(code: "package.show.nav.planned")}" tab="plannedIEs"
                 counts="${plannedIECounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'expiredIEs']"
                 text="${message(code: "package.show.nav.expired")}" tab="expiredIEs"
                 counts="${expiredIECounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'deletedIEs']"
                 text="${message(code: "package.show.nav.deleted")}" tab="deletedIEs"
                 counts="${deletedIECounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'allIEs']"
                 text="${message(code: "menu.public.all_titles")}" tab="allIEs"
                 counts="${allIECounts}"/>
</ui:tabs>

<% params.remove('tab') %>

<%
    Map<String, String>
    sortFieldMap = ['tipp.sortname': message(code: 'title.label')]
    if (journalsOnly) {
        sortFieldMap['startDate'] = message(code: 'default.from')
        sortFieldMap['endDate'] = message(code: 'default.to')
    } else {
        sortFieldMap['tipp.dateFirstInPrint'] = message(code: 'tipp.dateFirstInPrint')
        sortFieldMap['tipp.dateFirstOnline'] = message(code: 'tipp.dateFirstOnline')
    }
    sortFieldMap['tipp.accessStartDate'] = "${message(code: 'subscription.details.access_dates')} ${message(code: 'default.from')}"
    sortFieldMap['tipp.accessEndDate'] = "${message(code: 'subscription.details.access_dates')} ${message(code: 'default.to')}"
%>


<div class="la-clear-before">
    <div class="ui bottom attached tab active segment">

        <div class="ui form">
            <div class="three wide fields">
                <div class="field">
                    <ui:sortingDropdown noSelection="${message(code: 'default.select.choose.label')}"
                                        from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
                </div>
            </div>
        </div>


        <div>
            <div>
                <g:if test="${titles}">
                    <g:set var="counter" value="${offset + 1}"/>


                    <g:if test="${titles}">
                        <div class="ui fluid card">
                            <div class="content">
                                <div class="ui accordion la-accordion-showMore">
                                    <g:each in="${titles}" var="tipp">
                                        <div class="ui raised segments la-accordion-segments">
                                            <%
                                                String instanceFilter = ''
                                                if (institution.isCustomerType_Consortium())
                                                    instanceFilter += ' and pvd.instanceOf = null'
                                                Set<IssueEntitlement> ie_infos = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription pvd join pvd.orgRelations oo where oo.org = :context and ie.tipp = :tipp and pvd.status = :current and ie.status != :ieStatus' + instanceFilter, [ieStatus: RDStore.TIPP_STATUS_REMOVED, context: institution, tipp: tipp, current: RDStore.SUBSCRIPTION_CURRENT])
                                            %>

                                            <g:render template="/templates/title_segment_accordion"
                                                      model="[ie: null, tipp: tipp, permanentTitle: PermanentTitle.findByOwnerAndTipp(institution, tipp)]"/>

                                            <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                                                <div class="ui stackable grid" data-ajaxTarget="true">

                                                    <laser:render template="/templates/title_long_accordion"
                                                                  model="${[ie         : null, tipp: tipp,
                                                                            showPackage: true, showPlattform: true, showEmptyFields: false]}"/>

                                                    <div class="three wide column">
                                                        <div class="ui list la-label-list">
                                                            <g:if test="${tipp.accessStartDate}">
                                                                <div class="ui label la-label-accordion">${message(code: 'tipp.access')}</div>

                                                                <div class="item">
                                                                    <div class="content">
                                                                        <g:formatDate
                                                                                format="${message(code: 'default.date.format.notime')}"
                                                                                date="${tipp.accessStartDate}"/>
                                                                    </div>
                                                                </div>

                                                            </g:if>
                                                            <g:if test="${tipp.accessEndDate}">
                                                                <!-- bis -->
                                                                <!-- DEVIDER  -->
                                                                <ui:dateDevider/>
                                                                <div class="item">
                                                                    <div class="content">
                                                                        <g:formatDate
                                                                                format="${message(code: 'default.date.format.notime')}"
                                                                                date="${tipp.accessEndDate}"/>
                                                                    </div>
                                                                </div>
                                                            </g:if>

                                                        <%-- Coverage Details START --%>
                                                            <g:each in="${tipp.coverages}" var="covStmt"
                                                                    status="counterCoverage">
                                                                <g:if test="${covStmt.coverageNote || covStmt.coverageDepth || covStmt.embargo}">
                                                                    <div class="ui label la-label-accordion">${message(code: 'tipp.coverageDetails')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
                                                                </g:if>
                                                                <g:if test="${covStmt.coverageNote}">
                                                                    <div class="item">
                                                                        <i class="grey icon quote right la-popup-tooltip la-delay"
                                                                           data-content="${message(code: 'default.note.label')}"></i>

                                                                        <div class="content">
                                                                            <div class="header">
                                                                                ${message(code: 'default.note.label')}
                                                                            </div>

                                                                            <div class="description">
                                                                                ${covStmt.coverageNote}
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </g:if>
                                                                <g:if test="${covStmt.coverageDepth}">
                                                                    <div class="item">
                                                                        <i class="grey icon file alternate right la-popup-tooltip la-delay"
                                                                           data-content="${message(code: 'tipp.coverageDepth')}"></i>

                                                                        <div class="content">
                                                                            <div class="header">
                                                                                ${message(code: 'tipp.coverageDepth')}
                                                                            </div>

                                                                            <div class="description">
                                                                                ${covStmt.coverageDepth}
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </g:if>
                                                                <g:if test="${covStmt.embargo}">
                                                                    <div class="item">
                                                                        <i class="grey icon hand paper right la-popup-tooltip la-delay"
                                                                           data-content="${message(code: 'tipp.embargo')}"></i>

                                                                        <div class="content">
                                                                            <div class="header">
                                                                                ${message(code: 'tipp.embargo')}
                                                                            </div>

                                                                            <div class="description">
                                                                                ${covStmt.embargo}
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </g:if>
                                                            </g:each>
                                                        <%-- Coverage Details END --%>
                                                        </div>
                                                    </div>
                                                    <%-- My Area START--%>
                                                    <div class="seven wide column">
                                                        <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip la-delay"
                                                           data-content="${message(code: 'menu.my.subscriptions')}"></i>

                                                        <div class="ui la-segment-with-icon">

                                                            <div class="ui list">
                                                                <g:each in="${ie_infos}" var="ie">
                                                                    <div class="item">
                                                                        <div class="sixteen wide column">
                                                                            <i class="icon clipboard outline la-list-icon"></i>
                                                                            <g:link controller="subscription"
                                                                                    action="index"
                                                                                    id="${ie.subscription.id}">${ie.subscription.dropdownNamingConvention(institution)}</g:link>
                                                                            &nbsp;
                                                                            <br/>
                                                                            <br/>
                                                                            <g:link controller="issueEntitlement"
                                                                                    action="show"
                                                                                    id="${ie.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                                                            <br/>
                                                                        </div>
                                                                    </div>
                                                                </g:each>

                                                            </div>
                                                        </div>
                                                    </div><%-- My Area END --%>
                                                </div><%-- .grid --%>
                                            </div><%-- .segment --%>
                                        </div><%--.segments --%>
                                    </g:each>
                                </div><%-- .accordions --%>
                            </div><%-- .content --%>
                        </div><%-- .card --%>
                    </g:if>
                </g:if>
                <g:else>
                    <g:if test="${filterSet}">
                        <br/><strong><g:message code="filter.result.empty.object"
                                                args="${[message(code: "title.plural")]}"/></strong>
                    </g:if>
                    <g:else>
                        <br/><strong><g:message code="result.empty.object"
                                                args="${[message(code: "title.plural")]}"/></strong>
                    </g:else>
                </g:else>
            </div>
        </div>

    </div>
    <g:if test="${titles}">
        <ui:paginate action="currentTitles" controller="myInstitution" params="${params}"
                     max="${max}" total="${num_ti_rows}"/>
    </g:if>

</div>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>

<laser:render template="/templates/export/individuallyExportTippsModal"
              model="[modalID: 'individuallyExportTippsModal']"/>

<laser:script>
    $('.kbartExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $.ajax({
            url: "<g:createLink action="currentTitles" params="${params + [exportKBart: true]}"/>",
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response);
            $('#globalLoadingIndicator').hide();
        });
    });

    //should be made general
    JSPC.app.ajaxDropdown = function(selector, url, valuesString) {
        let values = [];
        if(valuesString.includes(',')) {
            values = valuesString.split(',');
        }
        else if(valuesString.length > 0) {
            values.push(valuesString);
        }
        selector.dropdown({
            apiSettings: {
                url: url,
                cache: false
            },
            clearable: true,
            minCharacters: 0
        });
        if(values.length > 0) {
            selector.dropdown('queryRemote', '', () => {
                selector.dropdown('set selected', values);
            });
        }
    }

    //continue here: setup dropdown preselection and move then to backend hookup; after done so: move on to addEntitlements() migration
    JSPC.app.ajaxDropdown($('#filterSub'), '<g:createLink controller="ajaxJson" action="lookupSubscriptions"/>?query={query}&restrictLevel=true', '${params.filterSub}');
    JSPC.app.ajaxDropdown($('#filterPvd'), '<g:createLink controller="ajaxJson" action="lookupProviders"/>?query={query}', '${params.filterPvd}');
    JSPC.app.ajaxDropdown($('#filterHostPlat'), '<g:createLink controller="ajaxJson" action="lookupPlatforms"/>?query={query}', '${params.filterHostPlat}');
</laser:script>

<laser:htmlEnd/>
