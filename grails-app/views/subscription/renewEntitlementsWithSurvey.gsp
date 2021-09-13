<%@ page import="de.laser.helper.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.titles.BookInstance; de.laser.SurveyOrg" %>
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
    <semui:crumb controller="myInstitution" action="surveyInfos" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]" message="issueEntitlementsSurvey.label"/>
    <semui:crumb controller="subscription" action="index" id="${newSub.id}" class="active" text="${newSub.name}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab: 'previousIEs']}">KBART Export "${message(code: 'renewEntitlementsWithSurvey.previousSelectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab: 'previousIEs']}">${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.previousSelectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab: 'allIEs']}">KBART Export "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab: 'allIEs']}">${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportKBart   : true,
                               tab: 'selectedIEs']}">KBART Export "${message(code: 'renewEntitlementsWithSurvey.currentEntitlements')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig.id,
                               exportXLS     : true,
                               tab: 'selectedIEs']}">${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.currentEntitlements')}"</g:link>
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

<g:if test="${params.tab == 'previousIEsStats' || params.tab == 'allIEsStats'}">

    <g:render template="/templates/filter/javascript"/>
    <semui:filter showFilterButton="true">
        <g:form action="renewEntitlementsWithSurvey" class="ui form" method="get">
            <g:hiddenField name="id" value="${newSub.id}"/>
            <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
            <g:hiddenField name="tab" value="${params.tab}"/>
            <g:hiddenField name="sort" value="${params.sort}"/>
            <g:hiddenField name="order" value="${params.order}"/>
            <div class="four fields">
                <div class="field">
                    <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>

                    <select name="series_names" id="series_names" multiple=""
                            class="ui search selection dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${controlledListService.getAllPossibleSeriesBySub(subscription)}" var="seriesName">
                            <option <%=(params.list('series_names')?.contains(seriesName)) ? 'selected="selected"' : ''%>
                                    value="${seriesName}">
                                ${seriesName}
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field">
                    <label for="subject_reference">${message(code: 'titleInstance.subjectReference.label')}</label>

                    <select name="subject_references" id="subject_reference" multiple=""
                            class="ui search selection dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${controlledListService.getAllPossibleSubjectsBySub(subscription)}" var="subject">
                            <option <%=(params.list('subject_references')?.contains(subject)) ? 'selected="selected"' : ''%>
                                    value="${subject}">
                                ${subject}
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field">
                    <label for="ddc">${message(code: 'titleInstance.ddc.label')}</label>

                    <select name="ddcs" id="ddc" multiple=""
                            class="ui search selection dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${controlledListService.getAllPossibleDdcsBySub(subscription)}" var="ddc">
                            <option <%=(params.list('ddcs')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                    value="${ddc.id}">
                                ${ddc.value} - ${ddc.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field">
                    <label for="language">${message(code: 'titleInstance.language.label')}</label>

                    <select name="languages" id="language" multiple="multiple"
                            class="ui search selection dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${controlledListService.getAllPossibleLanguagesBySub(subscription)}" var="language">
                            <option <%=(params.list('languages')?.contains(language.id.toString())) ? 'selected="selected"' : ''%>
                                    value="${language.id}">
                                ${language.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>
            </div>
            <div class="three fields">
                <div class="field">
                    <label for="metricType"><g:message code="default.usage.metricType"/></label>
                    <select name="metricType" id="metricType" multiple="multiple" class="ui selection dropdown">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${metricTypes}" var="metricType">
                            <option <%=(params.list('metricType')?.contains(metricType)) ? 'selected="selected"' : ''%>
                                    value="${metricType}">
                                ${metricType}
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field">
                    <label for="reportType"><g:message code="default.usage.reportType"/></label>
                    <select name="reportType" id="reportType" multiple="multiple" class="ui selection dropdown">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${reportTypes}" var="reportType">
                            <option <%=(params.list('reportType')?.contains(reportType)) ? 'selected="selected"' : ''%>
                                    value="${reportType}">
                                <g:message code="default.usage.${reportType}"/>
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field la-field-right-aligned">
                    <g:link ontroller="subscription" action="renewEntitlementsWithSurvey"
                            id="${newSub.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: params.tab]" class="ui reset primary button">${message(code: 'default.button.reset.label')}</g:link>
                    <input type="submit" class="ui secondary button"
                           value="${message(code: 'default.button.filter.label')}"/>
                </div>
            </div>
        </g:form>
    </semui:filter>
</g:if><g:else>

    <g:render template="/templates/filter/tipp_ieFilter"/>

</g:else>

</br>
<div class="ui pointing five item massive menu">

    <g:link class="item ${params.tab == 'previousIEs' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'previousIEs']">
        <g:message code="renewEntitlementsWithSurvey.previousSelectableTitles"/>
        <div class="ui circular label">${countPreviousIEs}</div>
    </g:link>

    <g:link class="item ${params.tab == 'allIEs' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'allIEs']">
        <g:message code="renewEntitlementsWithSurvey.selectableTitles"/>
        <div class="ui circular label">${countSelectedIEs}/${countAllIEs}</div>
    </g:link>

    <g:link class="item ${params.tab == 'selectedIEs' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'selectedIEs']">
        <g:message code="renewEntitlementsWithSurvey.currentEntitlements"/>
        <div class="ui circular label">${countSelectedIEs}</div></g:link>

    <g:link class="item ${params.tab == 'previousIEsStats' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'previousIEsStats']">
        <g:message code="renewEntitlementsWithSurvey.previousIEsStats"/>
    </g:link>

    <g:link class="item ${params.tab == 'allIEsStats' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'allIEsStats']">
        <g:message code="renewEntitlementsWithSurvey.allIEsStatsStats"/>
    </g:link>

</div>

<g:if test="${params.tab == 'previousIEsStats' || params.tab == 'allIEsStats'}">
    <semui:tabs>
        <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey" params="${params + [tabStat: 'total']}" text="${message(code: 'default.usage.allUsageGrid.header')}" tab="total" subTab="tabStat"/>
        <g:each in="${monthsInRing}" var="month">
            <semui:tabsItem controller="subscription" action="renewEntitlementsWithSurvey" params="${params + [tabStat: month.format("yyyy-MM")]}" text="${month.format("yyyy-MM")}" tab="${month.format("yyyy-MM")}" subTab="tabStat"/>
        </g:each>
    </semui:tabs>
    <div class="ui bottom attached tab active segment">
        <g:if test="${sums}">
            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th><g:message code="default.usage.date"/></th>
                    <th><g:message code="default.usage.metricType"/></th>
                    <th><g:message code="default.usage.reportCount"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${sums}" var="row">
                    <tr>
                        <td><g:formatDate date="${row.reportMonth}" format="yyyy-MM"/></td>
                        <td>${row.metricType}</td>
                        <td>${row.reportCount}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
        <g:elseif test="${usages}">
            <table class="ui sortable celled la-table table">
                <thead>
                <tr>
                    <g:if test="${usages[0].title}">
                        <g:sortableColumn title="${message(code:"default.title.label")}" property="title.name" params="${params}"/>
                    </g:if>
                    <g:sortableColumn title="${message(code:"default.usage.metricType")}" property="r.metricType" params="${params}"/>
                    <g:sortableColumn title="${message(code:"default.usage.reportCount")}" property="r.reportCount" params="${params}"/>
                </tr>
                </thead>
                <tbody>
                <g:each in="${usages}" var="row">
                    <tr>
                        <g:if test="${row.title}">
                            <td>${row.title.name}</td>
                        </g:if>
                        <td>${row.metricType}</td>
                        <td>${row.reportCount}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
            <semui:paginate total="${total}" params="${params}" max="${max}" offset="${offset}"/>
        </g:elseif>
        <g:elseif test="${!sums && !usages}">
            <g:message code="renewEntitlementsWithSurvey.noPreviousIEsStats"/>
        </g:elseif>

    </div>
</g:if><g:else>
<g:form name="renewEntitlements" id="${newSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
    <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
    <g:hiddenField name="tab" value="${params.tab}"/>

    <div class="ui segment">

        <div class="ui grid">

            <div class="row">

                <div class="sixteen wide column">
                    <g:if test="${targetInfoMessage}">
                        <h3 class="ui header center aligned"><g:message code="${targetInfoMessage}"/></h3>
                    </g:if>

                    <g:if test="${sourceInfoMessage}">
                        <h3 class="ui header center aligned"><g:message code="${sourceInfoMessage}"/></h3>
                    </g:if>

                    <semui:form>
                        <g:message code="subscription"/>: <strong><g:link action="show"
                                                                     id="${newSub.id}">${newSub.name}</g:link></strong>
                        <br />
                        <br />
                        <g:message code="package"/>:

                        <div class="ui list">
                            <g:each in="${newSub.packages.sort { it.pkg.name }}" var="subPkg">
                                <div class="item">
                                    <strong>${subPkg.pkg.name}</strong> (<g:message
                                        code="title.plural"/>: ${raw(subPkg.getIEandPackageSize())})
                                </div>
                            </g:each>
                        </div>
                    </semui:form>
                </div>

            </div>

            <g:render template="/templates/survey/entitlementTableSurvey"
                      model="${[ies: [sourceIEs: sourceIEs, targetIEs: targetIEs], showPackage: true, showPlattform: true]}"/>

        </div>

    </div>

    <div class="sixteen wide column">
        <div class="two fields">

            <div class="eight wide field" style="text-align: left;">
                <g:if test="${editable && params.tab == 'allIEs'}">
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


    <g:if test="${sourceIEs}">
        <semui:paginate action="renewEntitlementsWithSurvey" controller="subscription" params="${params}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}" max="${max}"
                        total="${num_ies_rows}"/>
    </g:if>

</g:form>
</g:else>
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
                        <g:if test="${editable && params.tab == 'allIEs'}">
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
