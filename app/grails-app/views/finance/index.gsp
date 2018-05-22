<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Financial Information</title>

</head>
<body>

    <g:render template="vars" /><%-- setting vars --%>

<%--
<g:set var="filterMode" value="ON" />
--%>

<semui:breadcrumbs>
    <g:if test="${inSubMode}">
        <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}" />
        <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />
        <semui:crumb class="active"  message="${fixedSubscription?.name}" />
    </g:if>
    <g:else>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution.name}" />
        <semui:crumb class="active" text="${message(code:'menu.institutions.finance')}" />
    </g:else>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <a class="item" data-mode="all" class="export" style="cursor: pointer">CSV Cost Items</a>
        </semui:exportDropdownItem>
        <%--
        <semui:exportDropdownItem>
            <a data-mode="sub" class="disabled export" style="cursor: pointer">CSV Costs by Subscription</a>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <a data-mode="code" class="disabled export" style="cursor: pointer">CSV Costs by Code</a>
        </semui:exportDropdownItem>
        --%>
    </semui:exportDropdown>

    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="myInstitution" action="financeImport" message="financials.action.financeImport" />
        </semui:actionsDropdown>
    </g:if>
</semui:controlButtons>

<g:if test="${fixedSubscription}">
    <h1 class="ui header"><semui:headerIcon />${fixedSubscription}</h1>
    <g:render template="../subscriptionDetails/nav" model="${[subscriptionInstance:fixedSubscription, params:(params << [id:fixedSubscription.id])]}"/> <%-- mapping="subfinance" params="${[sub:params.id]} --%>
</g:if>
<g:else>
    <h1 class="ui header"><semui:headerIcon />${institution.name}</h1>
</g:else>

<div class="ui grid">
    <div class="column">
        <button class="ui button" type="submit" data-semui="modal" href="#recentlyAdded_modal" id="showHideRecent">${message(code:'financials.recentCosts')}</button>

        <g:if test="${editable}">
            <%--<button class="ui button pull-right" type="submit" id="BatchSelectedBtn" title="${g.message(code: 'financials.filtersearch.deleteAll')}" value="remove">Remove Selected</button>--%>

            <button class="ui button pull-right" id="addNew">${message(code:'financials.addNewCost')}</button>
            <script>
$('#addNew').on('click', function(e) {
   $.ajax({
       url: "<g:createLink controller='finance' action='editCostItem'/>"
   }).done( function(data) {
       $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
       $('#dynamicModalContainer').empty().html(data);

       $('#dynamicModalContainer .ui.modal').modal({
           onVisible: function () {
               r2d2.initDynamicSemuiStuff('#costItem_createModal');
               r2d2.initDynamicXEditableStuff('#costItem_createModal');

               ajaxPostFunc()
           },
           detachable: true,
           closable: true,
           transition: 'fade up',
           onApprove : function() {
               $(this).find('.ui.form').submit();
               return false;
           }
       }).modal('show');
   })
})
            </script>

        </g:if>
    </div>
</div>

    <g:render template="recentlyAddedModal" />

    <div class="ui grid">
        <div class="sixteen wide column">

            <div id="userError" hidden="">
                <table class="ui celled la-table table">
                    <thead>
                    <tr><th>Problem/Update</th>
                        <th>Info</th></tr>
                    </thead>
                    <tbody><tr></tr></tbody>
                </table>
            </div>

            <div id="filterTemplateWrapper" class="wrapper">
                <div id="filterTemplate">
                    <g:render template="filter" />
                </div>
            </div>

            <button class="ui button pull-right" data-offset="#jumpMark_top" title="Select this button to go back to the top of the page" id="top">${message(code:'financials.backToTop')}</button>

        </div><!-- .sixteen -->
    </div><!-- .grid -->

<r:script>
    var financeRecentConstroller = {

        pullJob : null,

        go : function() {
            console.log("go")
            financeRecentConstroller.recentCostItems( null ); // pulls latest cost items
            financeRecentConstroller.pullJob = setInterval( financeRecentConstroller.costItemsPresent, 60 * 1000 ); // Recently updated code block
        },

        recentCostItems : function(to) {
            console.log("recentCostItems: ${from} - " + to + " @ ${contextService.getOrg()?.shortcode}")
            $.ajax({
                method: 'POST',
                url: "<g:createLink controller='finance' action='getRecentCostItems' />",
                data: {
                    from: "${from}",
                    to: to,
                    shortcode: "${contextService.getOrg()?.shortcode}"
                },
                global: false
            })
                .done(function(data) {
                    $('#recentlyAdded_modal > .content').replaceWith($(data).find('.content'));
                })
                .fail(function(jqXHR, textStatus, errorThrown ) {
                    console.log('Unable to perform recent cost update ... ', errorThrown);
                    clearTimeout(financeRecentConstroller.pullJob);
                });
        },

        costItemsPresent : function() {
            console.log("costItemsPresent: ${from} @ ${contextService.getOrg()?.shortcode}")
            var renderedDateTo = $('#recentUpdatesTable').data('resultsto');
            //if (renderedDateTo != null) // TODO: remove as comment
            {
                $.ajax({
                    method: "POST",
                    url: "<g:createLink controller='finance' action='newCostItemsPresent' />",
                    data: {
                        shortcode: "${contextService.getOrg()?.shortcode}",
                        to:renderedDateTo,
                        from: "${from}",
                        format:'json'
                    },
                    global: false
                })
                    .fail(function( jqXHR, textStatus, errorThrown ) {
                        errorHandling(textStatus, 'Recent Cost Updates', errorThrown);
                        clearTimeout(financeRecentConstroller.pullJob);
                        $('#recentModalWrapper', '#showHideRecent').remove();
                    })
                    .done(function(data) {
                        if(data.count > 0) {
                            financeRecentConstroller.recentCostItems(renderedDateTo);
                        }
                    });
            }
        }

    }

    $(document).ready(function() {
        financeRecentConstroller.go()
    })
</r:script>

</body>
</html>
