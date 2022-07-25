<%@ page import="de.laser.storage.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.titles.BookInstance" %>
<laser:htmlStart message="default.subscription.label" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.renewEntitlements.label')}"/>
</ui:breadcrumbs>
<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="subscription.details.renewEntitlements.label" />

<laser:render template="nav"/>

    <ui:messages data="${flash}"/>

<g:form name="renewEntitlements" id="${newSub.id}" action="processRenewEntitlements" class="ui form">
    <g:hiddenField id="tippsToAdd" name="tippsToAdd"/>
    <g:hiddenField id="tippsToDelete" name="tippsToDelete"/>
    <g:hiddenField id="packageId" name="packageId" value="${params.packageId}" />
    <div class="ui grid">
        <div class="row">
            <laser:render template="/templates/tipps/entitlementTable" model="${[subscriptions: [sourceId: subscription.id,targetId: newSub.id], ies: [sourceIEs: sourceIEs, targetIEs: targetIEs], side: "source"]}" />
            <laser:render template="/templates/tipps/entitlementTable" model="${[subscriptions: [sourceId: subscription.id,targetId: newSub.id], ies: [sourceIEs: sourceIEs, targetIEs: targetIEs], side: "target"]}" />
        </div>
        <div class="row">
            <div class="sixteen wide column">
                <button type="submit" name="process" value="preliminary" class="ui green button"><g:message code="subscription.details.renewEntitlements.preliminary"/></button>
                <button type="submit" name="process" value="finalise" class="ui red button"><g:message code="subscription.details.renewEntitlements.submit"/></button>
            </div>
        </div>
    </div>
</g:form>

<laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.tippsToAdd = [];
        JSPC.app.tippsToDelete = [];

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
                    if(JSPC.app.tippsToAdd.indexOf($(this).parents("tr").attr("data-gokbId")) < 0)
                        JSPC.app.tippsToAdd.push($(this).parents("tr").attr("data-gokbId"));
                }
                else if(corresp.find(".bulkcheck:checked")) {
                    var delIdx = JSPC.app.tippsToDelete.indexOf($(this).parents("tr").attr("data-gokbId"));
                    if (~delIdx) JSPC.app.tippsToDelete.splice(delIdx,1);
                    $("tr[data-index='"+index+"'").removeClass("negative").addClass("positive");
                    corresp.find(".bulkcheck:checked").prop("checked", false);
                    JSPC.app.tippsToAdd.push($(this).parents("tr").attr("data-gokbId"));
                }
            }
            else {
                $("tr[data-index='"+index+"'").removeClass("positive");
                var delIdx = JSPC.app.tippsToAdd.indexOf($(this).parents("tr").attr("data-gokbId"));
                if (~delIdx) JSPC.app.tippsToAdd.splice(delIdx,1);
            }
        });

        $("#target .bulkcheck").change(function() {
            var index = $(this).parents("tr").attr("data-index");
            var corresp = $("#source tr[data-index='"+index+"']");
            if(this.checked) {
                var delIdx = JSPC.app.tippsToAdd.indexOf($(this).parents("tr").attr("data-gokbId"));
                if (~delIdx) JSPC.app.tippsToAdd.splice(delIdx,1);
                $("tr[data-index='"+index+"'").removeClass("positive").addClass("negative");
                corresp.find(".bulkcheck:checked").prop("checked", false);
                JSPC.app.tippsToDelete.push($(this).parents("tr").attr("data-gokbId"));
            }
            else {
                $("tr[data-index='"+index+"'").removeClass("negative");
                var delIdx = JSPC.app.tippsToDelete.indexOf($(this).parents("tr").attr("data-gokbId"));
                if (~delIdx) JSPC.app.tippsToDelete.splice(delIdx,1);
            }
        });

        $("#renewEntitlements").submit(function(){
            $("#tippsToAdd").val(JSPC.app.tippsToAdd.join(','));
            $("#tippsToDelete").val(JSPC.app.tippsToDelete.join(','));
        });
</laser:script>
<laser:htmlEnd />
