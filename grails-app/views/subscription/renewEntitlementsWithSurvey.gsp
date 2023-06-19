<%@ page import="de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.Subscription; de.laser.titles.BookInstance; de.laser.remote.ApiSource; de.laser.Org;" %>
<laser:htmlStart message="subscription.details.renewEntitlements.label" serviceInjection="true"/>

<ui:breadcrumbs>
    <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">
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
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab           : 'allTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}</g:link>
        </ui:exportDropdownItem>

        <g:if test="${countCurrentPermanentTitles > 0}">

            <ui:exportDropdownItem>
                <g:link class="item" action="renewEntitlementsWithSurvey"
                        id="${subscriberSub.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportKBart   : true,
                                   tab           : 'currentPerpetualAccessIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitles')}</g:link>
            </ui:exportDropdownItem>
        </g:if>

%{--        <ui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab           : 'selectedIEs']}">${message(code: 'renewEntitlementsWithSurvey.currentTitlesSelect')}</g:link>
        </ui:exportDropdownItem>--}%

        <div class="divider"></div>

        <div class="header">${message(code: 'default.button.exports.xls')}s</div>

        <ui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${subscriberSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab           : 'allTipps']}">${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}</g:link>
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


%{--        <g:if test="${showStatisticByParticipant && params.tab == 'topUsed'}">
            <ui:exportDropdownItem>
                <g:link class="item statsExport" action="renewEntitlementsWithSurvey"
                        id="${subscriberSub.id}"
                        params="${[surveyConfigID : surveyConfig.id,
                                   exportForImport: true,
                                   tab            : 'allTipps',
                                   revision: revision]}">
                    ${message(code: 'renewEntitlementsWithSurvey.selectableTitles')} + ${message(code: 'default.stats.label')}
                </g:link>
            </ui:exportDropdownItem>
        </g:if>

        <g:if test="${showStatisticByParticipant && params.tab == 'topUsed'}">
            <ui:exportDropdownItem>
                <g:link class="item statsExport" action="renewEntitlementsWithSurvey"
                        id="${subscriberSub.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportXLSStats     : true,
                                   loadFor       : 'allTippsStats',
                                   revision: revision]}">${message(code:'default.usage.exports.filtered')} "${message(code: 'default.stats.label')}"</g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item statsExport" action="renewEntitlementsWithSurvey"
                        id="${subscriberSub.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportXLSStats     : true,
                                   loadFor       : 'holdingIEsStats',
                                   revision: revision]}">${message(code:'default.usage.exports.filtered')} "${message(code: 'default.stats.label')}" ${message(code: 'default.stats.holding')}</g:link>
            </ui:exportDropdownItem>
        </g:if>--}%
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${message(code: 'issueEntitlementsSurvey.label')} - ${surveyConfig.surveyInfo.name}">
    <uiSurvey:status object="${surveyConfig.surveyInfo}"/>
</ui:h1HeaderWithIcon>

    <ui:messages data="${flash}"/>

<div class="sixteen wide column">
    <div class="two fields">

        <div class="eight wide field" style="text-align: right;">
            <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">
                <g:link controller="survey" action="evaluationParticipant"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: subscriber.id]"
                        class="ui button">
                    <g:message code="surveyInfo.backToSurvey"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="myInstitution" action="surveyInfos"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]"
                        class="ui button">
                    <g:message code="surveyInfo.backToSurvey"/>
                </g:link>
            </g:else>
        </div>
    </div>
</div>
<br />

<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber).finishDate != null}">
    <ui:msg class="positive" icon="info" noClose="true">
        <g:message code="renewEntitlementsWithSurvey.finish.info"/>.
    </ui:msg>
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
                            <laser:render template="/templates/cpa/person_details"
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
    <ui:msg class="positive" header="${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.label')}">
            <g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.selectProcess"
                       args="[selectProcess.processCount, countAllTipps, selectProcess.countSelectTipps]"/>
    </ui:msg>
</g:if>


<g:if test="${(params.tab == 'allTipps') && editable}">

    <ui:greySegment>
        <g:form class="ui form" controller="subscription" action="renewEntitlementsWithSurvey"
                params="${[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: params.tab]}"
                method="post" enctype="multipart/form-data">

            <h4 class="ui dividing header"><g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.label"/>
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'renewEntitlementsWithSurvey.issueEntitlementSelect.info')}">
                <i class="question circle icon"></i>
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

                        <div class="ui icon button">
                            <i class="attach icon"></i>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <input type="submit"
                           value="${message(code: 'renewEntitlementsWithSurvey.issueEntitlementSelect.uploadButton')}"
                           class="fluid ui button"/>
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


<div class="row">
    <div class="column">

        <laser:render template="/templates/filter/tipp_ieFilter" model="[showStatsFilter: params.tab in ['topUsed'], notShow: params.tab == 'allTipps', fillDropdownsWithPackage: params.tab == 'allTipps']"/>

    </div>
</div><!--.row-->

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
            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                  data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitles.mouseover')}">
                <i class="question circle icon"></i>
            </span>
        <div class="ui circular label">${countCurrentPermanentTitles}</div>
    </g:link>

%{--    <g:if test="${showStatisticByParticipant}">
        <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                     params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'stats']"
                     text="${message(code: "renewEntitlementsWithSurvey.stats")}" tab="stats"/>
        <ui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                     params="[id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'topUsed']"
                     text="${message(code: "renewEntitlementsWithSurvey.topUsed")}" tab="topUsed"/>
    </g:if>--}%

</ui:tabs>


<div class="ui bottom attached tab active segment">
    <g:form name="renewEntitlements" id="${subscriberSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
    <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
    <g:hiddenField name="tab" value="${params.tab}"/>
        <g:if test="${params.tab == 'allTipps' || params.tab == 'selectedIEs' || params.tab == 'currentPerpetualAccessIEs'}">
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
            <div class="ui grid">
                <div class="row">
                    <div class="eight wide column">
                        <h3 class="ui icon header la-clear-before la-noMargin-top"><span
                                class="ui circular  label">${num_rows}</span> <g:message code="title.filter.result"/>
                        </h3>
                    </div>
                </div><!--.row-->
            </div><!--.grid-->

            <div class="ui form">
                <div class="three wide fields">
                    <div class="field">
                        <ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
                    </div>
                </div>
            </div>
        </g:if>


        <g:if test="${params.tab in ['topUsed']}">
            <g:if test="${usages && usages.size() > 0}">
                <laser:render template="/templates/survey/entitlementTableSurveyWithStats"
                              model="${[stats: usages, sumsByTitle: sumsByTitle, showPackage: true, showPlattform: true]}"/>
            </g:if>
            <g:elseif test="${params.reportType}">
                <g:message code="renewEntitlementsWithSurvey.noIEsStats"/>
            </g:elseif>
            <g:else>
                <g:message code="default.stats.error.noReportSelected"/>
            </g:else>
        </g:if>
        <g:elseif test="${params.tab == 'stats'}">
            <g:link controller="subscription" action="stats"
                    id="${params.id}"
                    class="ui button" target="_blank">
                <g:message code="renewEntitlementsWithSurvey.stats.button"/>
            </g:link>
        </g:elseif>
        <g:elseif test="${params.tab == 'allTipps'}">
            <laser:render template="/templates/survey/tippTableSurvey"
                          model="${[titlesList: titlesList, showPackage: true, showPlattform: true]}"/>
        </g:elseif>
        <g:else>
            <laser:render template="/templates/survey/entitlementTableSurvey"
                      model="${[ies: [sourceIEs: sourceIEs], showPackage: true, showPlattform: true]}"/>
        </g:else>



        <div class="sixteen wide column">
            <div class="two fields">
            <g:if test="${params.tab != 'stats'}">
                <div class="eight wide field" style="text-align: left;">
                    <g:if test="${editable && params.tab == 'allTipps'}">
                        <button type="submit" name="process" id="processButton" value="preliminary" class="ui green button">
                            ${params.tab == 'topUsed' ? '' :checkedCount} <g:message code="renewEntitlementsWithSurvey.preliminary"/></button>
                    </g:if>

                    <g:if test="${editable && params.tab == 'selectedIEs'}">
                        <button type="submit" name="process" id="processButton" value="remove" class="ui red button">
                            ${checkedCount}  <g:message code="renewEntitlementsWithSurvey.remove"/></button>
                    </g:if>
                </div>
            </g:if>

            <div class="eight wide field" style="text-align: right;">
                <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">
                    <g:link controller="survey" action="evaluationParticipant"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: subscriber.id]"
                            class="ui button">
                        <g:message code="surveyInfo.backToSurvey"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="myInstitution" action="surveyInfos"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]"
                            class="ui button">
                        <g:message code="surveyInfo.backToSurvey"/>
                    </g:link>
                </g:else>
            </div>
        </div>
    </div>

</g:form>
</div>
<g:if test="${sourceIEs || titlesList}">
    <ui:paginate action="renewEntitlementsWithSurvey" controller="subscription" params="${params}"
                    max="${max}" total="${num_rows}"/>
</g:if>

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
                titleGroup: "${params.titleGroup}"
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
                    baseSubID: "${subscription.id}",
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
</laser:script>
<laser:htmlEnd />
