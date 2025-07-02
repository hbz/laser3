<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.helper.Params; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.IssueEntitlement; de.laser.wekb.Platform; de.laser.PermanentTitle; de.laser.Subscription" %>
<laser:htmlStart message="myinst.currentTitles.label" />

<ui:breadcrumbs>
    <ui:crumb message="myinst.currentTitles.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <g:if test="${num_ti_rows < 1000000}">
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.TIPPS]"/>
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
                <a class="item export" href="#kbart" data-fileformat="kbart">KBART Export</a>
            <%--</g:else>--%>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<div id="downloadWrapper"></div>

<ui:h1HeaderWithIcon message="myinst.currentTitles.label" total="${num_ti_rows}" floated="true"/>

<ui:messages data="${flash}"/>

<g:set var="availableStatus"
       value="${RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS) - RDStore.TIPP_STATUS_REMOVED}"/>

<ui:filter>
    <g:form action="currentTitles" controller="myInstitution" class="ui form">
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
                                                                                                        title="${s.dropdownNamingConvention()}">
                            ${s.dropdownNamingConvention()}
                        </option>
                    </g:each>
                </select>
                --}%

            </div>
        </div>

        <div class="two fields">

            <div class="field">
                <label for="filterPvd">${message(code: 'provider.label')}</label>
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

            <div class="field">
                <label for="filterVen">${message(code: 'vendor.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="filterVen">
                    <input type="hidden" name="filterVen"/>
                    <div class="default text"><g:message code="default.select.all.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
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
                        <option <%=Params.getLongList(params, 'status').contains(status.id) ? 'selected="selected"' : ''%>
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
            <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</a>
            <input type="hidden" name="filterSet" value="true"/>
            <input type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}"/>
        </div>

    </g:form>
</ui:filter>

<laser:render template="/templates/titles/top_attached_title_tabs"
              model="${[
                      tt_controller:    controllerName,
                      tt_action:        actionName,
                      tt_tabs:          ['currentIEs', 'plannedIEs', 'expiredIEs', 'deletedIEs', 'allIEs'],
                      tt_counts:        [currentIECounts, plannedIECounts, expiredIECounts, deletedIECounts, allIECounts]
              ]}" />

<% params.remove('tab') %>

<div class="ui bottom attached tab active segment">
    <g:if test="${titles}">
        <div class="ui form">
            <div class="two wide fields">
                <div class="field">
                    <laser:render template="/templates/titles/sorting_dropdown" model="${[sd_type: 1, sd_journalsOnly: journalsOnly, sd_sort: params.sort, sd_order: params.order]}" />
                </div>
                <div class="field la-field-noLabel">
                    <ui:showMoreCloseButton />
                </div>
            </div>
        </div>
    </g:if>


    <g:if test="${titles}">
                <g:set var="counter" value="${offset + 1}"/>

                    <g:if test="${titles}">
                        <div class="ui fluid card">
                            <div class="content">
                                <div class="ui accordion la-accordion-showMore la-js-showMoreCloseArea">
                                    <g:each in="${titles}" var="tipp">
                                        <div class="ui raised segments la-accordion-segments">
                                            <%
                                                String instanceFilter = ''
                                                if (contextService.getOrg().isCustomerType_Consortium())
                                                    instanceFilter += ' and sub.instanceOf = null'
                                                Set<IssueEntitlement> ie_infos = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.org = :context and ie.tipp = :tipp and (sub.status = :current or sub.hasPerpetualAccess = true) and ie.status != :ieStatus' + instanceFilter, [ieStatus: RDStore.TIPP_STATUS_REMOVED, context: contextService.getOrg(), tipp: tipp, current: RDStore.SUBSCRIPTION_CURRENT])
                                            %>

                                        <g:render template="/templates/titles/title_segment_accordion"
                                                  model="${[ie: null, tipp: tipp, permanentTitle: PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and (pt.owner = :owner or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :owner and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))", [owner: contextService.getOrg(), tipp: tipp, subscriberCons: RDStore.OR_SUBSCRIBER_CONS])[0]]}"/>

                                        <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                                            <div class="ui stackable grid" data-ajaxTarget="true">

                                                <laser:render template="/templates/titles/title_long_accordion"
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
                                                                    <i class="${Icon.ATTR.TIPP_COVERAGE_NOTE} la-popup-tooltip" data-content="${message(code: 'default.note.label')}"></i>

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
                                                                    <i class="${Icon.ATTR.TIPP_COVERAGE_DEPTH} la-popup-tooltip"
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
                                                                    <i class="${Icon.ATTR.TIPP_EMBARGO} la-popup-tooltip"
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
                                                    <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip"
                                                       data-content="${message(code: 'menu.my.subscriptions')}"></i>

                                                    <div class="ui la-segment-with-icon">
                                                        <div class="ui list">
                                                            <g:each in="${ie_infos}" var="ie">
                                                                <div class="item">
                                                                    <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                                    <div class="content">
                                                                        <div class="header">
                                                                            <g:link controller="subscription"
                                                                                    action="index"
                                                                                    id="${ie.subscription.id}">${ie.subscription.dropdownNamingConvention()}</g:link>
                                                                        </div>
                                                                        <div class="description">
                                                                            <g:link controller="issueEntitlement"
                                                                                action="show"
                                                                                class="${Btn.SIMPLE} tiny la-margin-top-05em"
                                                                                id="${ie.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </g:each>
                                                        </div>
                                                    </div>

                                                    <laser:render template="/templates/reportTitleToProvider/multiple_infoBox" model="${[tipp: tipp]}"/>
                                                </div><%-- My Area END --%>
                                            </div><%-- .grid --%>
                                        </div><%-- .segment --%>
                                    </div><%--.segments --%>
                                </g:each>
                            </div><%-- .accordions --%>
                        </div><%-- .content --%>
                    </div><%-- .card --%>
                    <div class="ui clearing segment la-segmentNotVisable">
                            <ui:showMoreCloseButton />
                    </div>
                </g:if>
    </g:if>
    <g:else>
        <g:if test="${filterSet}">
            <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "title.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br/><strong><g:message code="result.empty.object" args="${[message(code: "title.plural")]}"/></strong>
        </g:else>
    </g:else>

</div>

<g:if test="${titles}">
    <ui:paginate action="currentTitles" controller="myInstitution" params="${params}" max="${max}" total="${num_ti_rows}"/>
</g:if>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.export').click(function(e) {
        e.preventDefault();
        $('#exportClickMeModal').modal('hide');
        $('#globalLoadingIndicator').show();
        //the shorthand ?: is not supported???
        let fileformat = $(this).attr('data-fileformat') ? $(this).attr('data-fileformat') : $('#fileformat-query').val();
        let fd;
        if(fileformat === 'kbart')
            fd = { fileformat: fileformat };
        else {
            let nativeForm = new FormData($('#exportClickMeModal').find('form')[0]);
            nativeForm.forEach((value, key) => fd[key] = value);
        }
        <g:each in="${params.keySet()}" var="param">
            <g:if test="${params[param] instanceof ArrayList}">
                fd.${param} = ['${params[param].join("','")}']
            </g:if>
            <g:else>
                fd.${param} = '${params[param]}';
            </g:else>
        </g:each>
        $.ajax({
            url: "<g:createLink action="currentTitles"/>",
            data: fd
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

    JSPC.app.ajaxDropdown($('#filterSub'), '<g:createLink controller="ajaxJson" action="lookupSubscriptions"/>?query={query}&restrictLevel=true', '${params.filterSub}');
    JSPC.app.ajaxDropdown($('#filterPvd'), '<g:createLink controller="ajaxJson" action="lookupProviders"/>?query={query}', '${params.filterPvd}');
    JSPC.app.ajaxDropdown($('#filterVen'), '<g:createLink controller="ajaxJson" action="lookupVendors"/>?query={query}', '${params.filterPvd}');
    JSPC.app.ajaxDropdown($('#filterHostPlat'), '<g:createLink controller="ajaxJson" action="lookupPlatforms"/>?query={query}', '${params.filterHostPlat}');
</laser:script>

<g:render template="/clickMe/export/js"/>

<g:render template="/templates/reportTitleToProvider/multiple_flyoutAndTippTask"/>

<laser:htmlEnd/>
