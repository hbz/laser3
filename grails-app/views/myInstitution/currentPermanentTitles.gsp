<%@ page import="de.laser.ExportClickMeService; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.IssueEntitlement; de.laser.PermanentTitle; de.laser.Subscription" %>
<laser:htmlStart message="myinst.currentPermanentTitles.label" />

<ui:controlButtons>
    <ui:exportDropdown>
        <g:if test="${num_tipp_rows < 1000000}">
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.TIPPS]"/>
            </ui:exportDropdownItem>
        </g:if>
        <g:else>
            <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
        </g:else>
        <ui:exportDropdownItem>
            <g:link class="item kbartExport  js-no-wait-wheel" params="${params + [exportKBart: true]}">KBART Export</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:breadcrumbs>
    <ui:crumb message="myinst.currentPermanentTitles.label" class="active"/>
</ui:breadcrumbs>

<div id="downloadWrapper"></div>

<ui:h1HeaderWithIcon message="myinst.currentPermanentTitles.label" total="${num_tipp_rows}" floated="true"/>

<ui:messages data="${flash}"/>

<laser:render template="/templates/titles/top_attached_title_tabs"
              model="${[
                      tt_controller:    controllerName,
                      tt_action:        actionName,
                      tt_tabs:          ['currentIEs', null, 'expiredIEs', 'deletedIEs', 'allIEs'],
                      tt_counts:        [currentTippCounts, null, expiredTippCounts, deletedTippCounts, allTippCounts]
              ]}" />

<div class="ui bottom attached tab active segment">

<%-- params.remove('tab') --%>

<laser:render template="/templates/filter/tipp_ieFilter" model="[dataAccess='filter']"/>

<h3 class="ui icon header la-clear-before la-noMargin-top">
    <ui:bubble count="${num_tipp_rows}" grey="true"/> <g:message code="title.filter.result"/>
</h3>
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

    <div class="ui grid">
        <div class="row">
            <div class="column">
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
                                                Set<IssueEntitlement> ie_infos = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.org = :context and ie.tipp = :tipp and ie.status != :ieStatus' + instanceFilter, [ieStatus: RDStore.TIPP_STATUS_REMOVED, context: contextService.getOrg(), tipp: tipp])
                                            %>

                                            <g:render template="/templates/titles/title_segment_accordion"
                                                      model="${[ie: null, tipp: tipp, permanentTitle: PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and (pt.owner = :owner or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :owner and oo.roleType = :subscriberCons and exists(select ac from AuditConfig ac where ac.referenceField = 'holdingSelection' and ac.referenceClass = '"+Subscription.class.name+"' and ac.referenceId = s.instanceOf.id)))", [owner: contextService.getOrg(), tipp: tipp, subscriberCons: RDStore.OR_SUBSCRIBER_CONS])[0]]}"/>

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
                                                                        <i class="${Icon.ATTR.TIPP_COVERAGE_DEPTH} la-popup-tooltip" data-content="${message(code: 'tipp.coverageDepth')}"></i>

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
                                                                        <i class="${Icon.ATTR.TIPP_EMBARGO} la-popup-tooltip" data-content="${message(code: 'tipp.embargo')}"></i>

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
                    </g:if>
                    <div class="ui clearing segment la-segmentNotVisable">
                        <ui:showMoreCloseButton />
                    </div>
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
        </div>

    </div>

        <ui:paginate action="currentPermanentTitles" controller="myInstitution" params="${params}" max="${max}" total="${num_tipp_rows}"/>
</g:if>

</div>

<g:render template="/clickMe/export/js"/>

<g:render template="/templates/reportTitleToProvider/multiple_flyoutAndTippTask"/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.kbartExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $.ajax({
            url: "<g:createLink action="exportPermanentTitles" params="${params + [exportKBart: true]}"/>",
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response);
            $('#globalLoadingIndicator').hide();
        });
    });
</laser:script>

<laser:htmlEnd/>
