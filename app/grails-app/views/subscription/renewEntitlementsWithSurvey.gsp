<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Subscription; com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform; com.k_int.kbplus.BookInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="issueEntitlementsSurvey.label"/>
    <semui:crumb controller="subscription" action="index" id="${newSub.id}" class="active"
                 text="${newSub.name}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig?.id,
                               exportKBart   : true]}">KBART Export "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}"</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewEntitlementsWithSurvey"
                    id="${newSub.id}"
                    params="${[surveyConfigID: surveyConfig?.id,
                               exportXLS     : true]}">${message(code: 'default.button.exports.xls')} "${message(code: 'renewEntitlementsWithSurvey.selectableTitles')}"</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>

<br>


<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
${message(code: 'issueEntitlementsSurvey.label')} - ${surveyConfig.surveyInfo.name}
<semui:surveyStatus object="${surveyConfig.surveyInfo}"/>
</h1>

<br>

<g:if test="${flash}">
    <semui:messages data="${flash}"/>
</g:if>

<div class="sixteen wide column">
    <div class="two fields">

        <div class="eight wide field" style="text-align: right;">
            <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
                    id="${surveyConfig?.id}"
                    class="ui button">
                <g:message code="surveyInfo.backToSurvey"/>
            </g:link>
        </div>
    </div>
</div>

<g:if test="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber)?.finishDate != null}">
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

<semui:filter>
    <g:form action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig?.id, tab: params.tab]" method="post" class="ui form">

        <div class="three fields">
            <div class="field">
                <label for="filter">${message(code: 'title.label')}</label>
                <input name="filter" id="filter" value="${params.filter}"/>
            </div>

            <div class="field">
                <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                <select class="ui dropdown" name="pkgfilter" id="pkgfilter">
                    <option value="">${message(code: 'subscription.details.from_pkg.all')}</option>
                    <g:each in="${subscription.packages}" var="sp">
                        <option value="${sp.pkg.id}" ${sp.pkg.id.toString() == params.pkgfilter ? 'selected=true' : ''}>${sp.pkg.name}</option>
                    </g:each>
                </select>
            </div>
            %{--<g:if test="${params.mode != 'advanced'}">
                <div class="field">
                    <semui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                      value="${params.asAt}"/>
                </div>
            </g:if>--}%
        </div>
        <div class="three fields">

            <div class="field">
                <label for="ebookFirstAutorOrFirstEditor">${message(code: 'renewEntitlementsWithSurvey.filter.ebookFirstAutorOrFirstEditor')}</label>
                <input name="ebookFirstAutorOrFirstEditor" id="ebookFirstAutorOrFirstEditor" value="${params.ebookFirstAutorOrFirstEditor}"/>
            </div>

            <div class="field">
                <label for="summaryOfContent">${message(code: 'renewEntitlementsWithSurvey.filter.summaryOfContent')}</label>
                <input name="summaryOfContent" id="summaryOfContent" value="${params.summaryOfContent}"/>
            </div>
            <div class="field la-field-right-aligned">
                <g:link action="renewEntitlementsWithSurvey"
                        id="${newSub.id}"
                        params="${[surveyConfigID: surveyConfig?.id, tab: params.tab]}"
                        class="ui reset primary button">${message(code: 'default.button.filterreset.label')}</g:link>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}"/>
            </div>
        </div>
    </g:form>
</semui:filter>

<div class="ui pointing two item massive menu">

    <g:link class="item ${params.tab == 'allIEs' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'allIEs']">
        <g:message code="renewEntitlementsWithSurvey.selectableTitles"/>
        <div class="ui circular label">${countAllIEs}</div>
    </g:link>

    <g:link class="item ${params.tab == 'selectedIEs' ? 'active' : ''}"
            controller="subscription" action="renewEntitlementsWithSurvey"
            id="${newSub.id}"
            params="[surveyConfigID: surveyConfig.id, tab: 'selectedIEs']">
        <g:message code="renewEntitlementsWithSurvey.currentEntitlements"/>
        <div class="ui circular label">${countSelectedIEs}</div></g:link>

</div>

<g:form name="renewEntitlements" id="${newSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
    <g:hiddenField id="iesToAdd" name="iesToAdd"/>

    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
    <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}"/>

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
                        <g:message code="subscription"/>: <b><g:link action="show"
                                                                     id="${newSub.id}">${newSub.name}</g:link></b>
                        <br>
                        <br>
                        <g:message code="package"/>:

                        <div class="ui list">
                            <g:each in="${newSub.packages.sort { it?.pkg?.name }}" var="subPkg">
                                <div class="item">
                                    <b>${subPkg?.pkg?.name}</b> (<g:message
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
                    <button type="submit" name="process" value="preliminary" class="ui green button"><g:message
                            code="renewEntitlementsWithSurvey.preliminary"/></button>
                </g:if>
            </div>


            <div class="eight wide field" style="text-align: right;">
                <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
                        id="${surveyConfig?.id}"
                        class="ui button">
                    <g:message code="surveyInfo.backToSurvey"/>
                </g:link>
            </div>
        </div>
    </div>


    <g:if test="${sourceIEs}">
        <semui:paginate action="renewEntitlementsWithSurvey" controller="subscription" params="${params}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}" max="${params.max}"
                        total="${num_ies_rows}"/>
    </g:if>

</g:form>

</body>
<r:script>
    $(document).ready(function () {
        var iesToAdd = [];

        $(".select-all").click(function () {
            var id = $(this).parents("table").attr("id");
            if (this.checked) {
                $("#" + id).find('.bulkcheck').prop('checked', true);
                console.log($(this).parents('div.column').siblings('div'));
                $(this).parents('div.column').siblings('div').find('.select-all').prop('checked', false);
            } else {
                $("#" + id).find('.bulkcheck').prop('checked', false);
            }
            $("#" + id + " .bulkcheck").trigger("change");
        });

        $("#surveyEntitlements .bulkcheck").change(function () {
            var index = $(this).parents("tr").attr("data-index");
            if (this.checked) {
                $("tr[data-index='" + index + "'").addClass("positive");
                iesToAdd.push($(this).parents("tr").attr("data-ieId"));
            } else {
                $("tr[data-index='" + index + "'").removeClass("positive");
                var delIdx = iesToAdd.indexOf($(this).parents("tr").attr("data-ieId"));
                if (~delIdx) iesToAdd.splice(delIdx, 1);
            }
        });

        $("#renewEntitlements").submit(function () {
            $("#iesToAdd").val(iesToAdd.join(','));
        });

    });
</r:script>
</html>
