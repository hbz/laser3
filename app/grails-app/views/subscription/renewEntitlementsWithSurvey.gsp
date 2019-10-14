<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Subscription; com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform; com.k_int.kbplus.BookInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscriptionInstance.id}" text="${subscriptionInstance.name}"/>
    <semui:crumb class="active" text="${message(code: 'subscription.details.renewEntitlements.label')}"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerTitleIcon type="Survey"/>
<g:message code="issueEntitlementsSurvey.label" />: ${surveyConfig?.surveyInfo?.name}
</h1>

<g:render template="nav"/>

%{--<g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id in [subscriptionInstance.getConsortia()?.id, subscriptionInstance.getCollective()?.id])}">
    <g:render template="message"/>
</g:if>--}%

<%--<g:set var="counter" value="${offset + 1}"/>
${message(code: 'subscription.details.availableTitles')} ( ${message(code: 'default.paginate.offset', args: [(offset + 1), (offset + (tipps?.size())), num_tipp_rows])} )--%>

<g:if test="${flash}">
    <semui:messages data="${flash}"/>
</g:if>

<g:if test="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, subscriber)?.finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info" />.
            </p>
        </div>
    </div>
</g:if>

<g:form name="renewEntitlements" id="${newSub.id}" action="processRenewEntitlementsWithSurvey" class="ui form">
    <g:hiddenField id="iesToAdd" name="iesToAdd"/>

    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}" />
    <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}" />

    <semui:form>

    <div class="ui grid">

        <div class="row">
            <g:render template="/templates/tipps/entitlementTable" model="${[subscriptions: [sourceId: subscription.id,targetId: newSub.id], ies: [sourceIEs: sourceIEs, targetIEs: targetIEs], side: "source", surveyFunction: true, showPackage: true, showPlattform: true]}" />
            <g:render template="/templates/tipps/entitlementTable" model="${[subscriptions: [sourceId: subscription.id,targetId: newSub.id], ies: [sourceIEs: sourceIEs, targetIEs: targetIEs], side: "target", surveyFunction: true, showPackage: true, showPlattform: true]}" />
        </div>

        <div class="sixteen wide column">
            <div class="two fields">

                <div class="eight wide field" style="text-align: left;">
                    <g:if test="${editable}">
                        <button type="submit" name="process" value="preliminary" class="ui green button"><g:message code="renewEntitlementsWithSurvey.preliminary"/></button>
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


    </div>

    </semui:form>
</g:form>

</body>
<r:script>
    $(document).ready(function() {
        var iesToAdd = [], tippsToDelete = [];

        $(".select-all").click(function() {
            var id = $(this).parents("table").attr("id");
            if(this.checked) {
                $("#"+id).find('.bulkcheck').prop('checked', true);
                console.log($(this).parents('div.column').siblings('div'));
                $(this).parents('div.column').siblings('div').find('.select-all').prop('checked', false);
            }
            else {
                $("#"+id).find('.bulkcheck').prop('checked', false);
            }
            $("#"+id+" .bulkcheck").trigger("change");
        });

        $("#source .titleCell").each(function(k) {
            var v = $(this).height();
            $("#target .titleCell").eq(k).height(v);
        });

        $("#source .bulkcheck").change(function() {
            var index = $(this).parents("tr").attr("data-index");
            var corresp = $("#target tr[data-index='"+index+"']");
            if(this.checked) {
                if(corresp.attr("data-empty")) {
                    $("tr[data-index='"+index+"'").addClass("positive");
                    if(iesToAdd.indexOf($(this).parents("tr").attr("data-ieId")) < 0)
                        iesToAdd.push($(this).parents("tr").attr("data-ieId"));
                }
                else if(corresp.find(".bulkcheck:checked")) {
                    var delIdx = tippsToDelete.indexOf($(this).parents("tr").attr("data-ieId"));
                    if (~delIdx) tippsToDelete.slice(delIdx,1);
                    $("tr[data-index='"+index+"'").removeClass("negative").addClass("positive");
                    corresp.find(".bulkcheck:checked").prop("checked", false);
                    iesToAdd.push($(this).parents("tr").attr("data-ieId"));
                }
            }
            else {
                $("tr[data-index='"+index+"'").removeClass("positive");
                var delIdx = iesToAdd.indexOf($(this).parents("tr").attr("data-ieId"));
                if (~delIdx) iesToAdd.slice(delIdx,1);
            }
        });

        $("#target .bulkcheck").change(function() {
            var index = $(this).parents("tr").attr("data-index");
            var corresp = $("#source tr[data-index='"+index+"']");
            if(this.checked) {
                var delIdx = iesToAdd.indexOf($(this).parents("tr").attr("data-ieId"));
                if (~delIdx) iesToAdd.slice(delIdx,1);
                $("tr[data-index='"+index+"'").removeClass("positive").addClass("negative");
                corresp.find(".bulkcheck:checked").prop("checked", false);
                tippsToDelete.push($(this).parents("tr").attr("data-ieId"));
            }
            else {
                $("tr[data-index='"+index+"'").removeClass("negative");
                var delIdx = tippsToDelete.indexOf($(this).parents("tr").attr("data-ieId"));
                if (~delIdx) tippsToDelete.slice(delIdx,1);
            }
        });

        $("#renewEntitlements").submit(function(){
            $("#iesToAdd").val(iesToAdd.join(','));
            $("#tippsToDelete").val(tippsToDelete.join(','));
        });

    });
</r:script>
</html>
