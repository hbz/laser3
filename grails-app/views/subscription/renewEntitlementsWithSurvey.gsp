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
    <semui:crumb controller="myInstitution" action="surveyInfosIssueEntitlements"
                 id="${surveyConfig?.id}" message="issueEntitlementsSurvey.label"/>
    <semui:crumb controller="subscription" action="index" id="${newSub.id}" class="active" text="${newSub.name}"/>
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
            <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">

                <g:link action="renewEntitlements"
                        id="${surveyConfig.id}" params="[participant: participant.id]"
                        class="ui button">
                    <g:message code="renewEntitlementsWithSurvey.renewEntitlements"/>
                </g:link>

            </g:if>
            <g:else>
                <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
                        id="${surveyConfig.id}"
                        class="ui button">
                    <g:message code="surveyInfo.backToSurvey"/>
                </g:link>
            </g:else>
        </div>
    </div>
</div>
</br>

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

<g:render template="/templates/filter/tipp_ieFilter"/>

</br>
<div class="ui pointing three item massive menu">

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

</div>

<g:form name="renewEntitlements" id="${newSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}"/>
    <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}"/>
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

                    <g:link action="renewEntitlements"
                            id="${surveyConfig.id}" params="[participant: participant.id]"
                            class="ui button">
                        <g:message code="renewEntitlementsWithSurvey.renewEntitlements"/>
                    </g:link>

                </g:if>
                <g:else>
                    <g:link controller="myInstitution" action="surveyInfosIssueEntitlements"
                            id="${surveyConfig.id}"
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
