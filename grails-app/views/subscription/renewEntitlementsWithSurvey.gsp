<%@ page import="de.laser.helper.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.titles.BookInstance; de.laser.SurveyOrg; de.laser.ApiSource" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb controller="myInstitution" action="surveyInfos"
                 params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]" message="issueEntitlementsSurvey.label"/>
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

    <g:if test="${surveyService.showStatisticByParticipant(subscription, subscriber)}">
        <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey"
                        params="[id: newSub.id, surveyConfigID: surveyConfig.id, tab: 'allIEsStats']"
                        text="${message(code: "renewEntitlementsWithSurvey.allIEsStats")}" tab="allIEsStats"/>
    </g:if>

</semui:tabs>


<g:if test="${params.tab == 'allIEsStats'}">
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

        <g:if test="${params.tab == 'allIEsStats'}">
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
                        ${checkedCount} <g:message code="renewEntitlementsWithSurvey.preliminary"/></button>
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
                        <g:if test="${editable && (params.tab == 'allIEs' || params.tab == 'allIEsStats')}">
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
