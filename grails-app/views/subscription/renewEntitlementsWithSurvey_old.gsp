<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Org" %>
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
    <ui:crumb controller="subscription" action="index" id="${subscriberSub.id}" class="active" text="${subscriberSub.name}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>

        <div class="header">KBART Exports</div>

        <ui:exportDropdownItem>
            <g:link class="item kbartExport  js-no-wait-wheel" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab           : 'allTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}</g:link>
        </ui:exportDropdownItem>

        <ui:exportDropdownItem>
            <g:link class="item kbartExport  js-no-wait-wheel" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab           : 'selectedIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}</g:link>
        </ui:exportDropdownItem>

        <g:if test="${countCurrentPermanentTitles > 0}">

            <ui:exportDropdownItem>
                <g:link class="item kbartExport  js-no-wait-wheel" action="renewEntitlementsWithSurvey"
                        id="${subscriberSub.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportKBart   : true,
                                   tab           : 'currentPerpetualAccessIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitles')}</g:link>
            </ui:exportDropdownItem>
        </g:if>

        <div class="divider"></div>

        <div class="header">${message(code: 'default.button.exports.xls')}s</div>

        <ui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab           : 'allTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}</g:link>
        </ui:exportDropdownItem>

        <ui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS   : true,
                               tab           : 'selectedIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}</g:link>
        </ui:exportDropdownItem>

        <g:if test="${countCurrentPermanentTitles > 0}">
            <ui:exportDropdownItem>
                <g:link class="item" action="renewEntitlementsWithSurvey"
                        id="${subscriberSub.id}"
                        params="${[surveyConfigID : surveyConfig.id,
                                   exportXLS     : true,
                                   tab           : 'currentPerpetualAccessIEs']}">
                    ${message(code: 'renewEntitlementsWithSurvey.currentTitles')}
                </g:link>
            </ui:exportDropdownItem>
        </g:if>


        <ui:exportDropdownItem>
            <a class="item" data-ui="modal" href="#individuallyExportModal">
                ${message(code: 'renewEntitlementsWithSurvey.selectableTitles')} + ${message(code: 'default.stats.label')}
            </a>
            %{--
            <g:link class="item statsExport action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID : surveyConfig.id,
                               exportForImport: true,
                               tab            : 'allTipps',
                               revision: revision]}">
                ${message(code: 'renewEntitlementsWithSurvey.selectableTitles')} + ${message(code: 'default.stats.label')}
            </g:link>--}%
        </ui:exportDropdownItem>

    </ui:exportDropdown>
    <ui:actionsDropdown>
        <ui:actionsDropdownItem data-ui="modal" id="selectEntitlementsWithKBART" href="#KBARTUploadForm" message="subscription.details.addEntitlements.menu"/>
    </ui:actionsDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${message(code: 'issueEntitlementsSurvey.label')} - ${surveyConfig.surveyInfo.name}">
    <uiSurvey:status object="${surveyConfig.surveyInfo}"/>
</ui:h1HeaderWithIcon>

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

<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber).finishDate != null}">
    <ui:msg class="success" showIcon="true" hideClose="true" message="renewEntitlementsWithSurvey.finish.info" />
</g:if>

<g:if test="${participant}">

    <ui:greySegment>
        <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
        <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

        <table class="ui table la-js-responsive-table la-table compact">
            <tbody>
            <tr>
                <td>
                    <p><strong><g:link controller="organisation" action="show" id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${choosenOrgCPAs}">
                        <g:set var="oldEditable" value="${editable}"/>
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <laser:render template="/addressbook/person_details"
                                      model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                        </g:each>
                        <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>

    </ui:greySegment>
</g:if>

<g:if test="${selectProcess}">
    <ui:msg class="success" header="${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.label')}">
            <g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.selectProcess"
                       args="[selectProcess.processCount, selectProcess.processRows, selectProcess.countSelectTipps, selectProcess.countNotSelectTipps, g.createLink(controller: 'subscription', action: 'renewEntitlementsWithSurvey', params: [id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'selectedIEs'])]"/>
    </ui:msg>
</g:if>

<laser:render template="KBARTSelectionUploadFormModal"/>

<%--
<g:if test="${(params.tab == 'allTipps') && editable}">

    <ui:greySegment>
        <g:form class="ui form" controller="subscription" action="renewEntitlementsWithSurvey"
                params="${[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab]}"
                method="post" enctype="multipart/form-data">

            <h4 class="ui dividing header"><g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.label"/>
                <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${message(code: 'renewEntitlementsWithSurvey.issueEntitlementSelect.info')}">
                <i class="${Icon.TOOLTIP.HELP}"></i>
            </span></h4>

            <div class="two fields">
                %{--<div class="field">
                    ${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.uploadFile.info')}
                    <g:link class="item" action="renewEntitlementsWithSurvey"
                            id="${subscriberSub.id}"
                            params="${[surveyConfigID: surveyConfig.id,
                                       exportForImport   : true,
                                       tab           : 'allTipps']}">
                        ${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.uploadFile.info2')}
                    </g:link> ${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.uploadFile.info3')}
                </div>--}%

                <div class="field">
                    <div class="ui fluid action input">
                        <input type="text" readonly="readonly"
                               placeholder="${message(code: 'template.addDocument.selectFile')}">
                        <input type="file" id="kbartPreselect" name="kbartPreselect" accept="text/tab-separated-values"
                               style="display: none;">

                        <div class="${Btn.BASIC_ICON}">
                            <i class="${Icon.CMD.ATTACHMENT}"></i>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <input type="submit"
                           value="${message(code: 'renewEntitlementsWithSurvey.issueEntitlementSelect.uploadButton')}"
                           class="${Btn.SIMPLE} fluid"/>
                </div>
            </div>
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            $('.action .icon.button').click(function () {
                $(this).parent('.action').find('input:file').click();
            });

            $('input:file', '.ui.action.input').on('change', function (e) {
                var name = e.target.files[0].name;
                $('input:text', $(e.target).parent()).val(name);
            });
        </laser:script>
    </ui:greySegment>
</g:if>
--%>

        <laser:render template="/templates/filter/tipp_ieFilter" model="[notShow: params.tab == 'allTipps', fillDropdownsWithPackage: params.tab == 'allTipps']"/>

<h3 class="ui icon header la-clear-before la-noMargin-top">
    <ui:bubble count="${num_rows}" grey="true"/> <g:message code="title.filter.result"/>
</h3>

<div id="downloadWrapper"></div>

<br />

    <ui:tabs actionName="${actionName}">
    <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                    params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'allTipps']"
                    text="${message(code: "renewEntitlementsWithSurvey.selectableTitles")}" tab="allTipps"
                    counts="${countAllTipps}"/>
    <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                    params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'selectedIEs']"
                    text="${message(code: "renewEntitlementsWithSurvey.currentTitlesSelect")}" tab="selectedIEs"
                    counts="${countSelectedIEs}"/>
    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
            class="item ${'currentPerpetualAccessIEs' == params.tab ? 'active' : ''}"
            params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'currentPerpetualAccessIEs']">
        <g:message code="renewEntitlementsWithSurvey.currentTitles"/>
            <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                  data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitles.mouseover')}">
                <i class="${Icon.TOOLTIP.HELP}"></i>
            </span>
        <div class="ui circular label">${countCurrentPermanentTitles}</div>
    </g:link>
</ui:tabs>

    <div class="ui bottom attached tab active segment">
    <g:if test="${titlesList || sourceIEs}">
    <g:if test="${(params.tab == 'selectedIEs' && titleGroup)}">
        <ui:tabs actionName="${actionName}">
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'currentIEs']"
                         text="${message(code: "package.show.nav.current")}" tab="currentIEs" subTab="currentIEs"
                         counts="${currentIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'plannedIEs']"
                         text="${message(code: "package.show.nav.planned")}" tab="plannedIEs" subTab="plannedIEs"
                         counts="${plannedIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'expiredIEs']"
                         text="${message(code: "package.show.nav.expired")}" tab="expiredIEs" subTab="expiredIEs"
                         counts="${expiredIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'deletedIEs']"
                         text="${message(code: "package.show.nav.deleted")}" tab="deletedIEs" subTab="deletedIEs"
                         counts="${deletedIECounts}"/>
            <ui:tabsItem controller="subscription" action="${actionName}"
                         params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab, subTab: 'allIEs']"
                         text="${message(code: "menu.public.all_titles")}" tab="allIEs" subTab="allIEs"
                         counts="${allIECounts}"/>
        </ui:tabs>
        <br>
    </g:if>


    <g:form name="renewEntitlements" id="${subscriberSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
        <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
        <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
        <g:hiddenField name="tab" value="${params.tab}"/>
        <g:hiddenField name="subTab" value="${params.subTab}"/>
        <g:if test="${params.tab == 'allTipps' || params.tab == 'selectedIEs' || params.tab == 'currentPerpetualAccessIEs'}">

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

        <g:if test="${params.tab == 'allTipps'}">
            <laser:render template="/templates/survey/tippTableSurvey" model="${[titlesList: titlesList, showPackage: true, showPlattform: true]}"/>
        </g:if>
        <g:else>
            <laser:render template="/templates/survey/entitlementTableSurvey" model="${[ies: [sourceIEs: sourceIEs], showPackage: true, showPlattform: true]}"/>
        </g:else>


        <div class="sixteen wide column">
            <div class="two fields">
            <g:if test="${params.tab != 'stats'}">
                <div class="eight wide field" style="text-align: left;">
                    <g:if test="${editable && params.tab == 'allTipps'}">
                        <button type="submit" name="process" id="processButton" value="preliminary" class="${Btn.POSITIVE}">
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

        JSPC.app.updateSelectionCache = function (index,checked) {
            let filterParams = {
                filter: "${params.filter}",
                pkgFilter: "${params.pkgfilter}",
                coverageDepth: "${params.coverageDepth}",
                series_names: ${params.list("series_names")},
                subject_references: ${params.list("subject_references")},
                ddcs: ${params.list("ddcs")},
                languages: ${params.list("languages")},
                yearsFirstOnline: ${params.list("yearsFirstOnline")},
                identifier: "${params.identifier}",
                medium: ${params.list("medium")},
                title_types: ${params.list("title_types")},
                publishers: ${params.list("pulishers")},
                hasPerpetualAccess: "${params.hasPerpetualAccess}",
                titleGroup: "${params.titleGroup}",
                status: ${params.list("status")},
            };
            $.ajax({
                url: "<g:createLink controller="ajax" action="updateChecked" />",
                data: {
                    sub: "${subscriberSub.id}?${params.tab}",
                    index: index,
                    filterParams: JSON.stringify(filterParams),
                    referer: "${actionName}",
                    checked: checked,
                    tab: "${params.tab}",
                    subTab: "${params.subTab}",
                    baseSubID: "${parentSubscription.id}",
                    newSubID: "${subscriberSub.id}",
                    surveyConfigID: "${surveyConfig.id}"

                },
                success: function (data) {
                        <g:if test="${editable && params.tab == 'allTipps'}">
                            $("#processButton").html(data.checkedCount + " ${g.message(code: 'renewEntitlementsWithSurvey.preliminary')}");
                        </g:if>

                        <g:if test="${editable && params.tab == 'selectedIEs'}">
                            $("#processButton").html(data.checkedCount + " ${g.message(code: 'renewEntitlementsWithSurvey.remove')}");
                        </g:if>
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
        var index = $(this).parents(".la-js-checkItem").attr("data-index");

            if (this.checked) {
                $("div[data-index='" + index + "']").addClass("positive");
            } else {
                $("div[data-index='" + index + "']").removeClass("positive");
            }

    <g:if test="${editable && params.tab == 'allTipps'}">
        JSPC.app.updateSelectionCache($(this).parents(".la-js-checkItem").attr("data-tippId"), $(this).prop('checked'));
    </g:if>

    <g:if test="${editable && params.tab == 'selectedIEs'}">
        JSPC.app.updateSelectionCache($(this).parents(".la-js-checkItem").attr("data-ieId"), $(this).prop('checked'));
    </g:if>

    });

    $(".statsExport").on('click', function(e) {
        e.preventDefault();
        /*
        kept for reasons of debug*/
        console.log($("#reportType").dropdown('get value'));
        console.log($("#metricType").dropdown('get value'));
        console.log($("#accessType").dropdown('get value'));
        console.log($("#accessMethod").dropdown('get value'));

        let url = $(this).attr('href')+'&reportType='+$("#reportType").dropdown('get value');
        if($("#metricType").dropdown('get value').length > 0)
            url+='&metricType='+$("#metricType").dropdown('get value');
        if($("#accessType").dropdown('get value').length > 0)
            url+='&accessType='+$("#accessType").dropdown('get value');
        if($("#accessMethod").dropdown('get value').length > 0)
            url+='&accessMethod='+$("#accessMethod").dropdown('get value');
        if($("#platform").dropdown('get value').length > 0) {
            $.each($("#platform").dropdown('get value'), function(i, val) {
                url+='&platform='+val;
            });
        }
        else {
            url+='&platform='+$("#platform").val();
        }
        //do not forget to communicate that to the users!
        if($("#reportType").dropdown('get value') !== '' && $("#reportType").dropdown('get value').length > 0)
            window.location.href = url;
    });

    $('.kbartExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $.ajax({
            url: $(this).attr('href'),
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response);
            $('#globalLoadingIndicator').hide();
        }).fail(function(resp, status){
            $("#downloadWrapper").text('Es ist zu einem Fehler beim Abruf gekommen');
            $('#globalLoadingIndicator').hide();
        });
    });
</laser:script>
<laser:htmlEnd />
