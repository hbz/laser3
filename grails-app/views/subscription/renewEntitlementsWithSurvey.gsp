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
            <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
                    id="${surveyConfig?.id}"
                    class="ui button">
                <g:message code="surveyInfo.backToSurvey"/>
            </g:link>
        </div>
    </div>
</div>

<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber)?.finishDate != null}">
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

            <div class="two fields">
                <semui:datepicker label="title.dateFirstOnline.from" name="dateFirstOnlineFrom" placeholder="default.date.label" value="${params.dateFirstOnlineFrom}"/>

                <semui:datepicker label="title.dateFirstOnline.to"  name="dateFirstOnlineTo" placeholder="default.date.label" value="${params.dateFirstOnlineTo}"/>
            </div>

        </div>
        <div class="three fields">

            <div class="field">
                <label for="ebookFirstAutorOrFirstEditor">${message(code: 'renewEntitlementsWithSurvey.filter.ebookFirstAutorOrFirstEditor')}</label>
                <input name="ebookFirstAutorOrFirstEditor" id="ebookFirstAutorOrFirstEditor" value="${params.ebookFirstAutorOrFirstEditor}"/>
            </div>

            <div class="field">
                <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>

                <select name="series_names" id="series_names" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${seriesNames}" var="seriesName">
                        <option <%=(params.list('series_names').contains(seriesName)) ? 'selected="selected"' : ''%>
                                value="${seriesName}">
                            ${seriesName}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="subject_reference">${message(code: 'renewEntitlementsWithSurvey.filter.subject_reference')}</label>
               %{-- <input name="summaryOfContent" id="summaryOfContent" value="${params.summaryOfContent}"/>--}%
              %{-- <g:select class="ui dropdown" name="summaryOfContent" title="${g.message(code: 'renewEntitlementsWithSurvey.filter.summaryOfContent')}"
                         from="${subjects}" noSelection="${['':'']}" />--}%

                <select name="subject_references" id="subject_reference" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${subjects}" var="subject">
                        <option <%=(params.list('subject_references').contains(subject)) ? 'selected="selected"' : ''%>
                        value="${subject}">
                        ${subject}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>
        <div class="two fields">

            <div class="field">

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
        <div class="ui circular label">${countAllSourceIEs}/${countAllIEs}</div>
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
                        <g:message code="subscription"/>: <strong><g:link action="show"
                                                                     id="${newSub.id}">${newSub.name}</g:link></strong>
                        <br />
                        <br />
                        <g:message code="package"/>:

                        <div class="ui list">
                            <g:each in="${newSub.packages.sort { it?.pkg?.name }}" var="subPkg">
                                <div class="item">
                                    <strong>${subPkg?.pkg?.name}</strong> (<g:message
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
<laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.iesToAdd = [];

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
                JSPC.app.iesToAdd.push($(this).parents("tr").attr("data-ieId"));
            } else {
                $("tr[data-index='" + index + "'").removeClass("positive");
                var delIdx = JSPC.app.iesToAdd.indexOf($(this).parents("tr").attr("data-ieId"));
                if (~delIdx) JSPC.app.iesToAdd.splice(delIdx, 1);
            }
        });

        $("#renewEntitlements").submit(function () {
            $("#iesToAdd").val(JSPC.app.iesToAdd.join(','));
        });
</laser:script>
</html>
