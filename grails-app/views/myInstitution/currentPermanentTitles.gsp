<%@ page import="de.laser.storage.RDStore; de.laser.IssueEntitlement; de.laser.PermanentTitle" %>
<laser:htmlStart message="myinst.currentPermanentTitles.label"/>

<ui:breadcrumbs>
    <ui:crumb message="myinst.currentPermanentTitles.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="myinst.currentPermanentTitles.label" total="${num_tipp_rows}" floated="true"/>

<ui:messages data="${flash}"/>

<ui:tabs actionName="${actionName}">
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'currentIEs']"
                 text="${message(code: "package.show.nav.current")}" tab="currentIEs"
                 counts="${currentTippCounts}"/>
    <ui:tabsItem class="disabled" controller="${controllerName}" action="${actionName}"
                 params="[tab: 'plannedIEs']"
                 text="${message(code: "package.show.nav.planned")}" tab="plannedIEs"
                 counts="${plannedTippCounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'expiredIEs']"
                 text="${message(code: "package.show.nav.expired")}" tab="expiredIEs"
                 counts="${expiredTippCounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'deletedIEs']"
                 text="${message(code: "package.show.nav.deleted")}" tab="deletedIEs"
                 counts="${deletedTippCounts}"/>
    <ui:tabsItem controller="${controllerName}" action="${actionName}"
                 params="[tab: 'allIEs']"
                 text="${message(code: "menu.public.all_titles")}" tab="allIEs"
                 counts="${allTippCounts}"/>
</ui:tabs>

<% params.remove('tab') %>

<div class="ui grid">
    <div class="row">
        <div class="column">
            <laser:render template="/templates/filter/tipp_ieFilter"/>
        </div>
    </div>

    <div class="row">
        <div class="eight wide column">
            <h3 class="ui icon header la-clear-before la-noMargin-top"><span
                    class="ui circular  label">${num_tipp_rows}</span> <g:message code="title.filter.result"/></h3>
        </div>

    </div>
</div>
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
    <div class="ui form">
        <div class="three wide fields">
            <div class="field">
                <ui:sortingDropdown noSelection="${message(code: 'default.select.choose.label')}" from="${sortFieldMap}"
                                    sort="${params.sort}" order="${params.order}"/>
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
                                <div class="ui accordion la-accordion-showMore">
                                    <g:each in="${titles}" var="tipp">
                                        <div class="ui raised segments la-accordion-segments">
                                            <%
                                                String instanceFilter = ''
                                                if (institution.isCustomerType_Consortium())
                                                    instanceFilter += ' and sub.instanceOf = null'
                                                Set<IssueEntitlement> ie_infos = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.org = :context and ie.tipp = :tipp and ie.status != :ieStatus' + instanceFilter, [ieStatus: RDStore.TIPP_STATUS_REMOVED, context: institution, tipp: tipp])
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
                                                                        <i class="icon clipboard outline la-list-icon"></i>
                                                                        <div class="content">
                                                                            <div class="header">
                                                                                <g:link controller="subscription"
                                                                                        action="index"
                                                                                        id="${ie.subscription.id}">${ie.subscription.dropdownNamingConvention(institution)}</g:link>
                                                                            </div>
                                                                            <div class="description">
                                                                                <g:link controller="issueEntitlement"
                                                                                        action="show"
                                                                                        class="ui tiny button la-margin-top-05em"
                                                                                        id="${ie.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                                                            </div>
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
        <ui:paginate action="currentPermanentTitles" controller="myInstitution" params="${params}"
                     max="${max}" total="${num_tipp_rows}"/>
    </g:if>

</div>

<laser:htmlEnd/>
