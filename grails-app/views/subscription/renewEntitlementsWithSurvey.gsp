<%@ page import="de.laser.helper.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.titles.BookInstance; de.laser.SurveyOrg; de.laser.ApiSource; de.laser.Org;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">
        <semui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
        <semui:crumb controller="survey" action="show"
                     params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]" message="issueEntitlementsSurvey.label"/>
    </g:if>
    <g:else>
        <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
        <semui:crumb controller="myInstitution" action="surveyInfos"
                     params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]" message="issueEntitlementsSurvey.label"/>
    </g:else>
    <semui:crumb controller="subscription" action="index" id="${newSub.id}" class="active" text="${newSub.name}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab           : 'allIEs']}">KBART Export "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab           : 'allIEs']}">${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <g:if test="${showStatisticByParticipant}">
            <semui:exportDropdownItem>
                <g:link class="item" action="renewEntitlementsWithSurvey"
                        id="${newSub.id}"
                        params="${[surveyConfigID : surveyConfig.id,
                                   exportForImport: true,
                                   tab            : 'allIEs']}">
                    ${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}" + "${message(code: 'default.stats.label')}"
                </g:link>
            </semui:exportDropdownItem>
        </g:if>
        <g:if test="${countCurrentIEs > 0}">
            <semui:exportDropdownItem>
                <g:link class="item" action="renewEntitlementsWithSurvey"
                        id="${newSub.id}"
                        params="${[surveyConfigID : surveyConfig.id,
                                   exportXLS     : true,
                                   tab           : 'currentIEs']}">
                    ${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.currentTitles')}"
                </g:link>
            </semui:exportDropdownItem>
        </g:if>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab           : 'selectedIEs']}">KBART Export "${message(code: 'renewEntitlementsWithSurvey.currentEntitlements')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab           : 'selectedIEs']}">${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.currentEntitlements')}"</g:link>
        </semui:exportDropdownItem>

        <g:if test="${showStatisticByParticipant}">
            <semui:exportDropdownItem>
                <g:link class="item" action="renewEntitlementsWithSurvey"
                        id="${newSub.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportXLSStats     : true,
                                   data             : 'fetchAll',
                                   tab           : 'allIEsStats',
                                   tabStat: params.tabStat]}">${message(code:'default.usage.exports.all')} "${message(code: 'default.stats.label')}"</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="renewEntitlementsWithSurvey"
                        id="${newSub.id}"
                        params="${[surveyConfigID: surveyConfig.id,
                                   exportXLSStats     : true,
                                   data             : 'fetchAll',
                                   tab           : 'holdingIEsStats',
                                   tabStat: params.tabStat]}">${message(code:'default.usage.exports.all')} "${message(code: 'default.stats.label')}" ${message(code: 'default.stats.holding')}</g:link>
            </semui:exportDropdownItem>
        </g:if>
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
${message(code: 'issueEntitlementsSurvey.label')} - ${surveyConfig.surveyInfo.name}
<semui:surveyStatus object="${surveyConfig.surveyInfo}"/>
</h1>

<g:if test="${flash}">
    <semui:messages data="${flash}"/>
</g:if>

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
</br>

<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber).finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info"/>.
            </p>
        </div>
    </div>
</g:if>

<g:if test="${participant}">

    <semui:form>
        <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
        <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

        <table class="ui table la-js-responsive-table la-table compact">
            <tbody>
            <tr>
                <td>
                    <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${choosenOrgCPAs}">
                        <g:set var="oldEditable" value="${editable}"/>
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <g:render template="/templates/cpa/person_details"
                                      model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                        </g:each>
                        <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>

    </semui:form>
</g:if>

<g:if test="${selectProcess}">
    <div class="ui positive message">
        <i class="close icon"></i>

        <div class="header"><g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.label"/></div>

        <p>
            <g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.selectProcess"
                       args="[selectProcess.processCount, countAllIEs, selectProcess.countSelectIEs]"/>
        </p>
    </div>
</g:if>


<g:if test="${(params.tab == 'allIEs' || params.tab == 'allIEsStats') && editable}">

    <semui:form>
        <g:form class="ui form" controller="subscription" action="renewEntitlementsWithSurvey"
                params="${[id: newSub.id, surveyConfigID: surveyConfig.id, tab: params.tab]}"
                method="post" enctype="multipart/form-data">

            <h4 class="ui dividing header"><g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.label"/>
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'renewEntitlementsWithSurvey.issueEntitlementSelect.info')}">
                <i class="question circle icon"></i>
            </span></h4>

            <div class="two fields">
                %{--<div class="field">
                    ${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.uploadFile.info')}
                    <g:link class="item" action="renewEntitlementsWithSurvey"
                            id="${newSub.id}"
                            params="${[surveyConfigID: surveyConfig.id,
                                       exportForImport   : true,
                                       tab           : 'allIEs']}">
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
    </semui:form>
</g:if>


<div class="row">
    <div class="column">

        <g:render template="/templates/filter/tipp_ieFilter" model="[showStatsFilter: params.tab == 'allIEsStats']"/>

    </div>
</div><!--.row-->

<g:if test="${num_ies_rows}">
    <div class="row">
        <div class="column">

            <div class="ui blue large label"><g:message code="title.plural"/>: <div class="detail">${num_ies_rows}</div>
            </div>
        </div>
    </div>
</g:if>

</br>
<semui:tabs actionName="${actionName}">
    <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                    params="[id: newSub.id, surveyConfigID: surveyConfig.id, tab: 'allIEs']"
                    text="${message(code: "renewEntitlementsWithSurvey.selectableTitles")}" tab="allIEs"
                    counts="${countAllIEs}"/>
    <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                    params="[id: newSub.id, surveyConfigID: surveyConfig.id, tab: 'selectedIEs']"
                    text="${message(code: "renewEntitlementsWithSurvey.currentTitlesSelect")}" tab="selectedIEs"
                    counts="${countSelectedIEs}"/>
    <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                    params="[id: newSub.id, surveyConfigID: surveyConfig.id, tab: 'currentIEs']"
                    text="${message(code: "renewEntitlementsWithSurvey.currentTitles")}" tab="currentIEs"
                    counts="${countCurrentIEs}"/>

    <g:if test="${showStatisticByParticipant}">
        <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                        params="[id: newSub.id, surveyConfigID: surveyConfig.id, tab: 'allIEsStats']"
                        text="${message(code: "renewEntitlementsWithSurvey.allIEsStats")}" tab="allIEsStats"/>
        <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                        params="[id: newSub.id, surveyConfigID: surveyConfig.id, tab: 'holdingIEsStats']"
                        text="${message(code: "renewEntitlementsWithSurvey.holdingIEsStats")}" tab="holdingIEsStats"/>
    </g:if>

</semui:tabs>


<g:if test="${params.tab in ['allIEsStats', 'holdingIEsStats']}">
    <semui:tabs>
        <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                        params="${params + [tabStat: 'total']}"
                        text="${message(code: 'default.usage.allUsageGrid.header')}" tab="total" subTab="tabStat"/>
        <g:each in="${monthsInRing}" var="month">
            <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                            params="${params + [tabStat: month.format("yyyy-MM")]}" text="${month.format("yyyy-MM")}"
                            tab="${month.format("yyyy-MM")}" subTab="tabStat"/>
        </g:each>
    </semui:tabs>
</g:if>

<g:form name="renewEntitlements" id="${newSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
    <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
    <g:hiddenField name="tab" value="${params.tab}"/>

    <div class="ui segment">

        <g:if test="${params.tab in ['allIEsStats', 'holdingIEsStats']}">
            <g:if test="${usages}">
                <g:render template="/templates/survey/entitlementTableSurveyWithStats"
                          model="${[stats: usages, showPackage: true, showPlattform: true]}"/>
            </g:if>
            <g:else>
                <g:message code="renewEntitlementsWithSurvey.noIEsStats"/>
            </g:else>
        </g:if>
        <g:else>
            <g:render template="/templates/survey/entitlementTableSurvey"
                      model="${[ies: [sourceIEs: sourceIEs], showPackage: true, showPlattform: true]}"/>
        </g:else>

    </div>

    <div class="sixteen wide column">
        <div class="two fields">

            <div class="eight wide field" style="text-align: left;">
                <g:if test="${editable && params.tab != 'selectedIEs'}">
                    <button type="submit" name="process" id="processButton" value="preliminary" class="ui green button">
                        ${params.tab == 'allIEsStats' ? '' :checkedCount} <g:message code="renewEntitlementsWithSurvey.preliminary"/></button>
                </g:if>

                <g:if test="${editable && params.tab == 'selectedIEs'}">
                    <button type="submit" name="process" id="processButton" value="remove" class="ui red button">
                        ${checkedCount}  <g:message code="renewEntitlementsWithSurvey.remove"/></button>
                </g:if>
            </div>


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

<g:if test="${usages}">
    <semui:paginate action="renewEntitlementsWithSurvey" controller="subscription" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${total}"/>
</g:if>

<g:if test="${sourceIEs}">
    <semui:paginate action="renewEntitlementsWithSurvey" controller="subscription" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${num_ies_rows}"/>
</g:if>

</body>
<laser:script file="${this.getGroovyPageFileName()}">


        JSPC.app.selectAll = function () {
            $('#select-all').is( ":checked") ? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
            $('#select-all').is( ":checked") ? $("#surveyEntitlements tr").addClass("positive") : $("#surveyEntitlements tr").removeClass("positive");
            JSPC.app.updateSelectionCache("all",$('#select-all').prop('checked'));
        }

        JSPC.app.updateSelectionCache = function (index,checked) {
            $.ajax({
                url: "<g:createLink controller="ajax" action="updateChecked" />",
                data: {
                    sub: "${newSub.id}?${params.tab}",
                    index: index,
                    <g:if test="${params.pkgfilter}">
                        packages: ${params.pkgfilter},
                    </g:if>
                    referer: "${actionName}",
                    checked: checked,
                    tab: "${params.tab}",
                    baseSubID: "${subscription.id}",
                    newSubID: "${newSub.id}"
                },
                success: function (data) {
                        <g:if test="${editable && params.tab != 'selectedIEs'}">
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
        var index = $(this).parents("tr").attr("data-index");
            if (this.checked) {
                $("tr[data-index='" + index + "'").addClass("positive");
            } else {
                $("tr[data-index='" + index + "'").removeClass("positive");
            }
        JSPC.app.updateSelectionCache($(this).parents("tr").attr("data-ieId"), $(this).prop('checked'));
    });

</laser:script>
</html>
