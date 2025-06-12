<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Subscription; grails.converters.JSON; de.laser.storage.RDStore; de.laser.wekb.Platform; de.laser.IssueEntitlementGroup" %>

<laser:htmlStart message="subscription.details.addEntitlements.label" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.addEntitlements.label')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" floated="true">
    <laser:render template="iconSubscriptionIsChild"/>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.addEntitlements.label')}</h2>
<%-- <laser:render template="nav"/> --%>

<div id="downloadWrapper"></div>

<g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortium()?.id}">
    <laser:render template="message"/>
</g:if>

<g:set var="counter" value="${offset + 1}"/>

<%--<div id="filterWrapper"></div>--%>
<laser:render template="/templates/filter/tipp_ieFilter" />

    <ui:messages data="${flash}"/>
    <g:if test="${errorFile}">
        <div class="errorKBARTWrapper">
            <g:render template="/templates/bulkItemDownload" model="[errorCount: errorCount, token: errorFile, errorKBART: true, fileformat: 'kbart']"/>
        </div>
    </g:if>

<%-- <laser:render template="KBARTSelectionUploadFormModal"/> --%>

<ui:modal id="linkToIssueEntitlementGroup" message="subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup"
          refreshModal="true"
          msgSave="${g.message(code: 'subscription.details.addEntitlements.add_withGroup.confirm')}">

    <g:form action="processAddEntitlements" class="ui form">
        <input type="hidden" name="id" value="${subscription.id}"/>
        <g:hiddenField name="process" value="withTitleGroup"/>

        <div class="ui two fields">

            <g:if test="${subscription.ieGroups}">
                <div class="field">
                    <label for="issueEntitlementGroup">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.add')}:</label>

                    <select name="issueEntitlementGroupID" id="issueEntitlementGroup"
                            class="ui search dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${subscription.ieGroups.sort { it.name }}" var="titleGroup">
                            <option value="${titleGroup.id}">
                                ${titleGroup.name} (${titleGroup.countCurrentTitles()})
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <div class="field">
                <label for="issueEntitlementGroupNew">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.new')}:</label>
                <input type="text" id="issueEntitlementGroupNew" name="issueEntitlementGroupNew" value="">
            </div>

        </div>
    </g:form>
</ui:modal>

<div class="ui segment">

<g:form action="processAddEntitlements" class="ui form">
    <input type="hidden" name="id" value="${subscription.id}"/>

    <g:if test="${tipps && !allPerpetuallyBought}">
        <div class="field">
            <g:if test="${blockSubmit}">
                <ui:msg header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.thread.running" />
            </g:if>
            <a class="${Btn.SIMPLE} left floated" id="processButton" data-ui="modal" href="#linkToIssueEntitlementGroup" ${blockSubmit ? 'disabled="disabled"' : '' }>
                ${checkedCount} <g:message code="subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup"/></a>

            <button type="submit" name="process" id="processButton2" value="withoutTitleGroup" ${blockSubmit ? 'disabled="disabled"' : '' } class="${Btn.SIMPLE} right floated">
                ${checkedCount} ${message(code: 'subscription.details.addEntitlements.add_selected')}</button>
        </div>

        <div class="field"></div>

        <%-- meaningless since the control of the title distribution via inheritance!
        cases:
        a) title of consortium = title of members: either holding entire or holding partial with inheritance activated
        b) title of consortium != title of members: process title enrichment at _each_ member individually
        <g:if test="${institution.isCustomerType_Consortium() && !auditService.getAuditConfig(subscription, 'holdingSelection')}">
            <div class="field">
                <div class="ui right floated checkbox toggle">
                    <g:checkBox name="withChildren" />
                    <label><g:message code="subscription.details.addEntitlements.withChildren"/></label>
                </div>
            </div>
        </g:if>
        --%>
        <div class="ui blue large label">
            <g:message code="title.plural"/>: <div class="detail">${num_tipp_rows}</div>
        </div>

        <br>
        <br>
    </g:if>


    <g:set var="counter" value="${offset + 1}"/>
    <g:set var="sumlistPrice" value="${0}"/>
    <g:set var="sumlocalPrice" value="${0}"/>

    <%
        String allChecked = ""
        checkedCache.each { e ->
            if (e != "checked")
                allChecked = ""
        }
    %>

    <div class="ui accordion la-accordion-showMore" id="surveyEntitlements">
        <g:if test="${allPerpetuallyBought}">
            <ui:msg message="${message(code: allPerpetuallyBought)}" hideClose="true"/>
        </g:if>
        <g:if test="${tipps}">
            <g:if test="${editable && !allPerpetuallyBought}"><input id="select-all" type="checkbox" name="chkall" ${allChecked}/></g:if>
            <g:each in="${tipps}" var="tipp">

                <g:set var="participantPerpetualAccessToTitle" value="${permanentTitles.containsKey(tipp) ? permanentTitles.get(tipp) : []}"/>

                <div class="ui raised segments la-accordion-segments">

                    <div class="ui fluid segment title">

                        <div class="ui stackable equal width grid la-js-checkItem" data-gokbId="${tipp.gokbId}"
                             data-tippId="${tipp.id}" data-index="${counter}">
                            <g:if test="${participantPerpetualAccessToTitle.size() > 0}">
                                <span class="ui mini left corner label la-perpetualAccess la-popup-tooltip"
                                      data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')} ${participantPerpetualAccessToTitle.collect{it.getPermanentTitleInfo()}.join(',')}"
                                      data-position="left center" data-variation="tiny">
                                    <i class="star icon"></i>
                                </span>
                            </g:if>
                            <div class="one wide column">
                                <g:if test="${editable && participantPerpetualAccessToTitle.size() == 0}">
                                    <input type="checkbox" name="bulkflag"
                                           class="bulkcheck la-js-notOpenAccordion" ${checkedCache ? checkedCache[tipp.gokbId] : ''}>
                                </g:if>
                            </div>


                            <div class="one wide column">
                                <span class="la-vertical-centered">${counter++}</span>
                            </div>

                            <div class="column">
                                <div class="ui list">
                                    <!-- START TEMPLATE -->
                                    <laser:render
                                            template="/templates/titles/title_short_accordion"
                                            model="${[tipp       : tipp,
                                                      showPackage: true, showPlattform: true, showEmptyFields: false, sub: subscription.id]}"/>
                                    <!-- END TEMPLATE -->

                                </div>
                            </div>

                            <div class="column">
                                <laser:render template="/templates/tipps/coverages_accordion" model="${[tipp: tipp, overwriteEditable: false]}"/>
                            </div>

                            <div class="four wide column">

                                <!-- START TEMPLATE -->
                                <laser:render template="/templates/identifier" model="${[tipp: tipp]}"/>
                                <!-- END TEMPLATE -->
                            </div>

                            <div class="two wide column">
                                <g:if test="${tipp.priceItems}">
                                    <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                                        <div class="ui list">
                                            <g:if test="${priceItem.listPrice}">
                                                <div class="item">
                                                    <div class="contet">
                                                        <div class="header">
                                                            <g:message code="tipp.price.listPrice"/>
                                                        </div>

                                                        <div class="content">
                                                            <g:formatNumber number="${priceItem.listPrice}" type="currency"
                                                                            currencyCode="${priceItem.listCurrency?.value}"
                                                                            currencySymbol="${priceItem.listCurrency?.value}"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </g:if>
                                        </div>
                                        <g:if test="${priceItem.listPrice && (i < tipp.priceItems.size() - 1)}">
                                            <hr>
                                        </g:if>
                                        <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                        <g:set var="sumlocalPrice"
                                               value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                                    </g:each>
                                </g:if>
                            </div>

                            <div class="one wide column">
                                <div class="ui right floated buttons">
                                    <div class="right aligned wide column">
                                    </div>

                                    <div class="${Btn.MODERN.SIMPLE}">
                                        <i class="${Icon.CMD.SHOW_MORE}"></i>
                                    </div>
                                    <g:if test="${editable && participantPerpetualAccessToTitle.size() == 0}">
                                        <g:if test="${!blockSubmit}">
                                            <g:link class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                                    action="processAddEntitlements"
                                                    params="${[id: subscription.id, singleTitle: tipp.gokbId, uploadPriceInfo: uploadPriceInfo, preselectCoverageDates: preselectCoverageDates]}"
                                                    data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                                                <i class="${Icon.CMD.ADD}"></i>
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <div class="la-popup-tooltip"
                                                 data-content="${message(code: 'subscription.details.addEntitlements.thread.running')}">
                                                <g:link class="${Btn.ICON.SIMPLE} disabled"
                                                        action="processAddEntitlements"
                                                        params="${[id: subscription.id, singleTitle: tipp.gokbId, uploadPriceInfo: uploadPriceInfo, preselectCoverageDates: preselectCoverageDates]}">
                                                    <i class="${Icon.CMD.ADD}"></i>
                                                </g:link>
                                            </div>
                                        </g:else>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="ui fluid segment content" data-ajaxTargetWrap="true">
                        <div class="ui stackable grid" data-ajaxTarget="true">

                            <laser:render template="/templates/titles/title_long_accordion"
                                          model="${[tipp       : tipp,
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
                                    <g:each in="${tipp.coverages}" var="covStmt" status="counterCoverage">
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

                            <g:if test="${uploadPriceInfo || preselectCoverageDates}">
                                <div class="seven wide column">
                                    <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip"
                                       data-content="${message(code: 'tipp.tooltip.myArea')}"></i>

                                    <div class="ui la-segment-with-icon">
                                        <g:if test="${(tipp.titleType == 'serial')}">
                                            <g:set var="coverageStatements"
                                                   value="${preselectCoverageDates ? issueEntitlementOverwrite[tipp.gokbId]?.coverages : [:]}"/>
                                            <div class="ui stackable grid"></div>
                                            <g:each in="${coverageStatements}" var="covStmt" status="counterCoverage">

                                                <laser:render template="/templates/tipps/coverageStatement_accordion"
                                                              model="${[covStmt: covStmt, showEmbargo: false, objectTypeIsIE: false, counterCoverage: counterCoverage, overwriteEditable: false]}"/>

                                            </g:each>
                                        </g:if>

                                        <div class="ui list">
                                            <g:if test="${uploadPriceInfo}">
                                                <div class="ui list">
                                                    <div class="item">
                                                        <div class="content">
                                                            <div class="header">
                                                                <g:message code="tipp.price.localPrice"/>
                                                            </div>

                                                            <div class="content">
                                                                <g:formatNumber
                                                                        number="${issueEntitlementOverwrite[tipp.gokbId]?.localPrice}"
                                                                        type="currency"
                                                                        currencySymbol="${issueEntitlementOverwrite[tipp.gokbId]?.localCurrency}"
                                                                        currencyCode="${issueEntitlementOverwrite[tipp.gokbId]?.localCurrency}"/>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                        </div><%-- .grid --%>
                    </div><%-- .segment --%>
                </div><%--.segments --%>
            </g:each>
            <br>
            <br>
        </g:if>
        <g:else>
            <g:message code="subscription.details.addEntitlements.noResult"/>
            <g:link controller="subscription" action="index" id="${params.id}" params="${params+[tab: 'currentIEs']}"><g:message code="subscription.details.current_ent"/></g:link>
        </g:else>
    </div><%-- .accordions --%>

    <g:if test="${tipps}">
        <div class="paginateButtons" style="text-align:center">
            <g:if test="${!allPerpetuallyBought}">
                <div class="field">
                    <g:if test="${blockSubmit}">
                        <ui:msg header="${message(code:"message.attention")}" message="subscription.details.addEntitlements.thread.running" />
                    </g:if>
                    <a class="${Btn.SIMPLE} left floated" id="processButton3" data-ui="modal" href="#linkToIssueEntitlementGroup" ${blockSubmit ? 'disabled="disabled"' : '' }>
                        ${checkedCount} <g:message code="subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup"/></a>

                    <button type="submit" name="process" id="processButton4" value="withoutTitleGroup" ${blockSubmit ? 'disabled="disabled"' : '' } class="${Btn.SIMPLE} right floated">
                        ${checkedCount} ${message(code: 'subscription.details.addEntitlements.add_selected')}</button>
                </div>
            </g:if>
        </div>

        <br>
        <br>
        <ui:paginate controller="subscription"
                        action="addEntitlements"
                        params="${params + [pagination: true]}"
                        max="${max}"
                        total="${num_tipp_rows}"/>
    </g:if>

</g:form>

</div>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.selectAll = function () {
        $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
        JSPC.app.updateSelectionCache("all",$('#select-all').prop('checked'));
    }

    JSPC.app.updateSelectionCache = function (index,checked) {
        let filterParams = {
                hasPerpetualAccess: "${params.hasPerpetualAccess}",
                filter: "${params.filter}",
                pkgFilter: "${params.pkgfilter}",
                asAt: "${params.asAt}",
                coverageDepth: "${params.coverageDepth}",
                series_names: "${params.list("series_names")}",
                subject_references: "${params.list("subject_references")}",
                ddcs: "${params.list("ddcs")}",
                languages: "${params.list("languages")}",
                yearsFirstOnline: "${params.list("yearsFirstOnline")}",
                identifier: "${params.identifier}",
                medium: "${params.list("medium")}",
                title_types: "${params.list("title_types")}",
                publishers: "${params.list("publishers")}",
                hasPerpetualAccess: "${params.hasPerpetualAccess}",
                titleGroup: "${params.titleGroup}",
                status: "${params.list("status") ?: ''}",
        };
        $.ajax({
            url: "<g:createLink controller="ajax" action="updateChecked" />",
            data: {
                sub: ${subscription.id},
                index: index,
                filterParams: JSON.stringify(filterParams),
                referer: "${actionName}",
                checked: checked
            },
            success: function (data) {
                $("#processButton").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup')}");
                $("#processButton2").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selected')}");
                 $("#processButton3").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selectedToIssueEntitlementGroup')}");
                  $("#processButton4").html(data.checkedCount + " ${g.message(code: 'subscription.details.addEntitlements.add_selected')}");
            }
        }).done(function(result){

        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    }

    $("#select-all").change(function() {
        JSPC.app.selectAll();
    });

    $(".bulkcheck").change(function() {
        JSPC.app.updateSelectionCache($(this).parents(".la-js-checkItem").attr("data-gokbId"), $(this).prop('checked'));
    });

    $('.kbartExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $.ajax({
            url: "<g:createLink action="exportPossibleEntitlements" params="${params + [exportKBart: true]}"/>",
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response);
            $('#globalLoadingIndicator').hide();
        });
    });

    <%--
    JSPC.app.loadFilter = function() {
        $.ajax({
            url: "<g:createLink action="getTippIeFilter"/>",
            data: {
                formAction: "${actionName}",
                <g:each in="${params.keySet()}" var="key">${key}: <g:if test="${params[key] instanceof String[] || params[key] instanceof List}">[${params[key].join(',')}]</g:if><g:else>"${params[key]}"</g:else>,</g:each>
            }
        }).done(function(response){
            $("#filterWrapper").html(response);
            r2d2.initDynamicUiStuff("#filterWrapper");
        });
    }

    JSPC.app.loadFilter();
    --%>
</laser:script>

<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
