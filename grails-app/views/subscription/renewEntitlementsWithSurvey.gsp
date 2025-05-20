<%@ page import="de.laser.RefdataValue; de.laser.survey.SurveyPersonResult; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Org; de.laser.ExportService" %>
<laser:htmlStart message="subscription.details.renewEntitlements.label" />

<ui:breadcrumbs>
    <g:if test="${contextService.getOrg().id == surveyConfig.surveyInfo.owner.id}">
        <ui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
        <ui:crumb controller="survey" action="show"
                     params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]" message="issueEntitlementsSurvey.label"/>
    </g:if>
    <g:else>
        <ui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
        <ui:crumb controller="myInstitution" action="surveyInfos"
                     params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]" message="issueEntitlementsSurvey.label"/>
    </g:else>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" class="active" text="${subscription.name}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>

        <div class="header">KBART Exports</div>

        <ui:exportDropdownItem>
            <g:link class="item normalExport" action="exportRenewalEntitlements"
                    id="${subscription.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportConfig   : ExportService.KBART,
                               tab           : 'allTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}</g:link>
        </ui:exportDropdownItem>

        <ui:exportDropdownItem>
            <g:link class="item normalExport" action="exportRenewalEntitlements"
                    id="${subscription.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportConfig   : ExportService.KBART,
                               tab           : 'selectableTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTipps')}</g:link>
        </ui:exportDropdownItem>

        <ui:exportDropdownItem>
            <g:link class="item normalExport" action="exportRenewalEntitlements"
                    id="${subscription.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportConfig   : ExportService.KBART,
                               tab           : 'selectedIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}</g:link>
        </ui:exportDropdownItem>

        <g:if test="${countCurrentPermanentTitles > 0}">

            <ui:exportDropdownItem>
                <g:link class="item normalExport" action="exportRenewalEntitlements"
                        id="${subscription.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportConfig   : ExportService.KBART,
                                   tab           : 'currentPerpetualAccessIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitles')}</g:link>
            </ui:exportDropdownItem>
        </g:if>

        <div class="divider"></div>

        <div class="header">${message(code: 'default.button.exports.xls')}s</div>

        <ui:exportDropdownItem>
            <g:link class="item normalExport" action="exportRenewalEntitlements"
                    id="${subscription.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportConfig     : ExportService.EXCEL,
                               tab           : 'allTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}</g:link>
        </ui:exportDropdownItem>

        <ui:exportDropdownItem>
            <g:link class="item normalExport" action="exportRenewalEntitlements"
                    id="${subscription.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportConfig     : ExportService.EXCEL,
                               tab           : 'selectableTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTipps')}</g:link>
        </ui:exportDropdownItem>

        <ui:exportDropdownItem>
            <g:link class="item normalExport" action="exportRenewalEntitlements"
                    id="${subscription.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportConfig   : ExportService.EXCEL,
                               tab           : 'selectedIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}</g:link>
        </ui:exportDropdownItem>

        <g:if test="${countCurrentPermanentTitles > 0}">
            <ui:exportDropdownItem>
                <g:link class="item normalExport" action="exportRenewalEntitlements"
                        id="${subscription.id}"
                        params="${[surveyConfigID : surveyConfig.id,
                                   exportConfig     : ExportService.EXCEL,
                                   tab           : 'currentPerpetualAccessIEs']}">
                    ${message(code: 'renewEntitlementsWithSurvey.currentTitles')}
                </g:link>
            </ui:exportDropdownItem>
        </g:if>


        <ui:exportDropdownItem>
            <a class="item" data-ui="modal" href="#individuallyExportModal">
                ${message(code: 'renewEntitlementsWithSurvey.selectableTitles')} + ${message(code: 'default.stats.label')}
            </a>
        </ui:exportDropdownItem>

    </ui:exportDropdown>
    <g:if test="${editable}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem id="selectEntitlementsWithIDOnly" href="${createLink(action: 'kbartSelectionUpload', controller: 'ajaxHtml', id: subscription.id, params: [referer: actionName, headerToken: 'subscription.details.addEntitlements.menuID', withIDOnly: true, progressCacheKey: '/survey/renewEntitlementsWithSurvey/', surveyConfigID: surveyConfig.id, tab: params.tab])}" message="subscription.details.addEntitlements.menuID"/>
            <ui:actionsDropdownItem id="selectEntitlementsWithKBART" href="${createLink(action: 'kbartSelectionUpload', controller: 'ajaxHtml', id: subscription.id, params: [referer: actionName, headerToken: 'subscription.details.addEntitlements.menu', progressCacheKey: '/survey/renewEntitlementsWithSurvey/', surveyConfigID: surveyConfig.id, tab: params.tab])}" message="subscription.details.addEntitlements.menu"/>
            <ui:actionsDropdownItem id="selectEntitlementsWithPick" href="${createLink(action: 'kbartSelectionUpload', controller: 'ajaxHtml', id: subscription.id, params: [referer: actionName, headerToken: 'subscription.details.addEntitlements.menuPick', withPick: true, progressCacheKey: '/survey/renewEntitlementsWithSurvey/', surveyConfigID: surveyConfig.id, tab: params.tab])}" message="subscription.details.addEntitlements.menuPick"/>
        </ui:actionsDropdown>
    </g:if>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${message(code: 'issueEntitlementsSurvey.label')} - ${surveyConfig.surveyInfo.name}">
    <uiSurvey:status object="${surveyConfig.surveyInfo}"/>
</ui:h1HeaderWithIcon>

<div id="downloadWrapper"></div>

    <ui:messages data="${flash}"/>

<div class="sixteen wide column">
    <div class="two fields">

        <div class="eight wide field" style="text-align: right;">
            <g:if test="${contextService.getOrg().id == surveyConfig.surveyInfo.owner.id}">
                <g:link controller="survey" action="evaluationParticipant"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: subscriber.id]"
                        class="${Btn.SIMPLE}">
                    <g:message code="surveyInfo.backToSurvey"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="myInstitution" action="surveyInfos"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]"
                        class="${Btn.SIMPLE}">
                    <g:message code="surveyInfo.backToSurvey"/>
                </g:link>
            </g:else>
        </div>
    </div>
</div>
<br />

<g:render template="/survey/participantMessage" model="[participant: subscriber]"/>

<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber).finishDate != null}">
    <ui:msg class="success" showIcon="true" hideClose="true" message="renewEntitlementsWithSurvey.finish.info" />
</g:if>

<g:render template="/survey/participantInfos" model="[participant: subscriber]"/>

<laser:render template="/templates/filter/tipp_ieFilter" model="[notShow: params.tab in ['selectedIEs', 'selectableTipps', 'currentPerpetualAccessIEs'], fillDropdownsWithPackage: params.tab in ['allTipps', 'selectableTipps']]"/>

<h3 class="ui icon header la-clear-before la-noMargin-top">
    <ui:bubble count="${num_rows}" grey="true"/> <g:message code="title.filter.result"/>
</h3>

<br />

    <ui:tabs actionName="${actionName}">
        <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                     params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: 'selectableTipps']"
                     text="${message(code: "renewEntitlementsWithSurvey.selectableTipps")}" tab="selectableTipps"
                     counts="${countAllTipps - countSelectedIEs - countCurrentPermanentTitles}"/>
        <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                         params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: 'selectedIEs']"
                         text="${message(code: "renewEntitlementsWithSurvey.currentTitlesSelect")}" tab="selectedIEs"
                         counts="${countSelectedIEs}"/>
        <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                class="item ${'currentPerpetualAccessIEs' == params.tab ? 'active' : ''}"
                params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: 'currentPerpetualAccessIEs']">
            <g:message code="renewEntitlementsWithSurvey.currentTitles"/>
                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                      data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitles.mouseover')}">
                    <i class="${Icon.TOOLTIP.HELP}"></i>
                </span>
            <div class="ui circular label">${countCurrentPermanentTitles}</div>
        </g:link>
        <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                     params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: 'allTipps']"
                     text="${message(code: "renewEntitlementsWithSurvey.selectableTitles")}" tab="allTipps"
                     counts="${countAllTipps}"/>
</ui:tabs>

    <div class="ui bottom attached tab active segment">
    <g:if test="${titlesList || sourceIEs}">
    <g:if test="${(params.tab == 'selectedIEs' && titleGroup)}">
        <ui:tabs actionName="${actionName}">
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'currentIEs']"
                         text="${message(code: "package.show.nav.current")}" tab="currentIEs" subTab="currentIEs"
                         counts="${currentIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'plannedIEs']"
                         text="${message(code: "package.show.nav.planned")}" tab="plannedIEs" subTab="plannedIEs"
                         counts="${plannedIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'expiredIEs']"
                         text="${message(code: "package.show.nav.expired")}" tab="expiredIEs" subTab="expiredIEs"
                         counts="${expiredIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'deletedIEs']"
                         text="${message(code: "package.show.nav.deleted")}" tab="deletedIEs" subTab="deletedIEs"
                         counts="${deletedIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscription.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'allIEs']"
                         text="${message(code: "menu.public.all_titles")}" tab="allIEs" subTab="allIEs"
                         counts="${allIECounts}"/>
        </ui:tabs>
        <br>
    </g:if>


    <g:form name="renewEntitlements" id="${subscription.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
        <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
        <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
        <g:hiddenField name="tab" value="${params.tab}"/>
        <g:hiddenField name="subTab" value="${params.subTab}"/>
        <g:if test="${params.tab == 'allTipps' || params.tab == 'selectableTipps' || params.tab == 'selectedIEs' || params.tab == 'currentPerpetualAccessIEs'}">

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

        <g:if test="${params.tab == 'allTipps' || params.tab == 'selectableTipps'}">
            <laser:render template="/templates/survey/tippTableSurvey" model="${[titlesList: titlesList, showPackage: true, showPlattform: true, showBulkCheck: RefdataValue.get(params.hasPerpetualAccess) != RDStore.YN_YES]}"/>
        </g:if>
        <g:else>
            <laser:render template="/templates/survey/entitlementTableSurvey" model="${[ies: [sourceIEs: sourceIEs], showPackage: true, showPlattform: true]}"/>
        </g:else>


        <div class="sixteen wide column">
            <div class="two fields">
            <g:if test="${params.tab != 'stats'}">
                <div class="eight wide field" style="text-align: left;">
                    <g:if test="${editable && params.tab == 'allTipps' || params.tab == 'selectableTipps'}">
                        <button type="submit" name="process" id="processButton" value="add" class="${Btn.POSITIVE}">
                            ${checkedCount} <g:message code="renewEntitlementsWithSurvey.preliminary"/></button>
                    </g:if>

                    <g:if test="${editable && params.tab == 'selectedIEs'}">
                        <button type="submit" name="process" id="processButton" value="remove" class="${Btn.NEGATIVE}">
                            ${checkedCount}  <g:message code="renewEntitlementsWithSurvey.remove"/></button>
                    </g:if>
                </div>
            </g:if>

            <div class="eight wide field" style="text-align: right;">
                <ui:showMoreCloseButton />
            </div>
        </div>
    </div>

</g:form>
</g:if>

</div>

    <div class="ui clearing segment la-segmentNotVisable">
        <g:if test="${contextService.getOrg().id == surveyConfig.surveyInfo.owner.id}">
            <g:link controller="survey" action="evaluationParticipant"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: subscriber.id]"
                    class="${Btn.SIMPLE} right floated">
                <g:message code="surveyInfo.backToSurvey"/>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="myInstitution" action="surveyInfos"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]"
                    class="${Btn.SIMPLE} right floated">
                <g:message code="surveyInfo.backToSurvey"/>
            </g:link>
        </g:else>
    </div>

    <ui:paginate action="renewEntitlementsWithSurvey" controller="subscription" params="${params + [pagination: true]}"
                    max="${max}" total="${num_rows}"/>


<laser:render template="export/exportUsageForSurvey" />

<laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.selectAll = function () {
           if ( $('#select-all').is( ":checked") ){
                $( '#surveyEntitlements .bulkcheck' ).each(function( index ) {
                    $(this).prop('checked', true);
                     $(this).parents('.la-js-checkItem').addClass("positive");
                    console.log(  $(this));
                });
           }
           else if ( $('#select-all').not( ":checked") ){
                $( '#surveyEntitlements .bulkcheck' ).each(function( index ) {
                    $(this).prop('checked', false);
                    $(this).parents('.la-js-checkItem').removeClass("positive");
                    console.log( "nein");
                });
           }
           //$('#select-all').is( ":checked") ? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
           //$('#select-all').is( ":checked") ? $("#surveyEntitlements .la-js-checkItem").addClass("positive") : $("#surveyEntitlements .la-js-checkItem").removeClass("positive");
            JSPC.app.updateSelectionCache("all",$('#select-all').prop('checked'));
        }

        JSPC.app.recalculatePrices = function() {
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="updatePricesSelection" />",
                data: {
                    sub: "${subscription.id}?${params.tab}",
                    referer: "${actionName}"
                }
            }).done(function(result){
                $("#dynamicListWrapper").html(result);
            }).fail(function(xhr,status,message){
                console.log("error occurred, consult logs!");
            });
        };

        JSPC.app.updateSelectionCache = function (index,checked) {
            let filterParams = {
                filter: "${params.filter}",
                pkgFilter: "${params.pkgfilter}",
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
                status: "${params.list("status")}",
            };
            $.ajax({
                url: "<g:createLink controller="ajax" action="updateChecked" />",
                data: {
                    id: "${subscription.id}",
                    sub: "${subscription.id}?${params.tab}",
                    index: index,
                    filterParams: JSON.stringify(filterParams),
                    referer: "${actionName}",
                    checked: checked,
                    tab: "${params.tab}",
                    subTab: "${params.subTab}",
                    baseSubID: "${baseSub.id}",
                    newSubID: "${subscription.id}",
                    surveyConfigID: "${surveyConfig.id}"

                },
                success: function (data) {
                    <g:if test="${editable && (params.tab == 'allTipps' || params.tab == 'selectableTipps' )}">
                        $("#processButton").html(data.checkedCount + " ${g.message(code: 'renewEntitlementsWithSurvey.preliminary')}");
                    </g:if>

                    <g:if test="${editable && params.tab == 'selectedIEs'}">
                        $("#processButton").html(data.checkedCount + " ${g.message(code: 'renewEntitlementsWithSurvey.remove')}");
                    </g:if>
                    JSPC.app.recalculatePrices();
                }
            }).done(function(result){

            }).fail(function(xhr,status,message){
                console.log("error occurred, consult logs!");
            });
    }

    //initial load of eventual cache
    JSPC.app.recalculatePrices();

    $("#select-all").change(function() {
        JSPC.app.selectAll();
    });

    $(".bulkcheck").change(function() {
        var index = $(this).parents(".la-js-checkItem").attr("data-index");

            if (this.checked) {
                $("div[data-index='" + index + "']").addClass("positive");
            } else {
                $("div[data-index='" + index + "']").removeClass("positive");
            }

    <g:if test="${editable && params.tab == 'allTipps' || params.tab == 'selectableTipps'}">
        JSPC.app.updateSelectionCache($(this).parents(".la-js-checkItem").attr("data-tippId"), $(this).prop('checked'));
    </g:if>

    <g:if test="${editable && params.tab == 'selectedIEs'}">
        JSPC.app.updateSelectionCache($(this).parents(".la-js-checkItem").attr("data-ieId"), $(this).prop('checked'));
    </g:if>

    });

    $('.normalExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $("#downloadWrapper").hide();
        $.ajax({
            url: $(this).attr('href'),
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response).show();
            $('#globalLoadingIndicator').hide();
        }).fail(function(resp, status){
            $("#downloadWrapper").text('Es ist zu einem Fehler beim Abruf gekommen').show();
            $('#globalLoadingIndicator').hide();
        });
    });

    $('#selectEntitlementsWithKBART, #selectEntitlementsWithPick, #selectEntitlementsWithIDOnly').on('click', function(e) {
            e.preventDefault();

            $.ajax({
                url: $(this).attr('href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #KBARTUploadForm').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                   onShow: function () {
                        r2d2.initDynamicUiStuff('#KBARTUploadForm');
                        r2d2.initDynamicXEditableStuff('#KBARTUploadForm');
                        $("html").css("cursor", "auto");
                    },
                    detachable: true,
                    autofocus: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        });
</laser:script>
<laser:htmlEnd />
