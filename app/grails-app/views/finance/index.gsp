<%@ page import="de.laser.helper.RDStore" %>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.financials.label')}</title>

</head>
<body>
    <laser:serviceInjection />

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

<g:form name="export" controller="finance" action="financialsExport">
    <g:if test="${fixedSubscription}">
        <input type="hidden" name="sub" value="${fixedSubscription.id}"/>
    </g:if>
</g:form>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <a class="item" onclick="$('#export').submit()">${message(code:'default.button.exports.xls', default:'XLS Export')}</a>
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
            <semui:actionsDropdownItem id="btnAddNewCostItem" message="financials.addNewCost" />
            <semui:actionsDropdownItemDisabled message="financials.action.financeImport" />
            <%--<semui:actionsDropdownItem controller="myInstitution" action="financeImport" message="financials.action.financeImport" />--%>
        </semui:actionsDropdown>
    </g:if>
</semui:controlButtons>
<g:if test="${cost_item_count_CS}">
    <g:if test="${queryMode == 'MODE_CONS'}">
        <g:set var="totalString" value="${cost_item_count} ${message(code:'financials.header.ownCosts')} / ${cost_item_count_CS} ${message(code:'financials.header.consortialCosts')}"/>
    </g:if>
    <g:elseif test="${queryMode == 'MODE_CONS_AT_SUBSCR'}">
        <g:set var="totalString" value="${cost_item_count_CS} ${message(code:'financials.header.consortialCosts')}"/>
    </g:elseif>
</g:if>
<g:elseif test="${cost_item_count_SUBSCR}">
    <g:set var="totalString" value="${cost_item_count} ${message(code:'financials.header.ownCosts')} / ${cost_item_count_SUBSCR} ${message(code:'financials.header.subscriptionCosts')}"/>
</g:elseif>
<g:else>
    <g:set var="totalString" value="${cost_item_count} ${message(code:'financials.header.ownCosts')}"/>
</g:else>

<g:if test="${fixedSubscription}">
    <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'subscription.details.financials.label')} für ${fixedSubscription} <semui:totalNumber total="${totalString}"/>
        <semui:anualRings mapping="subfinance" object="${fixedSubscription}" controller="finance" action="index" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
    </h1>

    <g:render template="../subscriptionDetails/nav" model="${[subscriptionInstance:fixedSubscription, params:(params << [id:fixedSubscription.id])]}"/>
</g:if>
<g:else>
    <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'subscription.details.financials.label')} für ${institution.name} <semui:totalNumber total="${totalString}"/></h1>
</g:else>

<g:if test="${fixedSubscription?.instanceOf && (contextOrg?.id == fixedSubscription?.getConsortia()?.id)}">
    <div class="ui negative message">
        <div class="header">
            <g:message code="myinst.message.attention" />:
            <g:message code="myinst.subscriptionDetails.message.ChildView" />
            <span class="ui label">${fixedSubscription.getAllSubscribers()?.collect{itOrg -> itOrg.getDesignation()}.join(',')}</span>.
        </div>
        <p>
            <g:message code="myinst.subscriptionDetails.message.hereLink" />
            <g:link controller="subscriptionDetails" action="members" id="${fixedSubscription.instanceOf.id}">
                <g:message code="myinst.subscriptionDetails.message.backToMembers" />
            </g:link>
            <g:message code="myinst.subscriptionDetails.message.and" />
            <g:link controller="subscriptionDetails" action="show" id="${fixedSubscription.instanceOf.id}">
                <g:message code="myinst.subscriptionDetails.message.consotialLicence" />
            </g:link>.
        </p>
    </div>
</g:if>

<semui:messages data="${flash}" />

<g:if test="${editable}">
    <button class="ui button" value="" href="#addBudgetCodeModal" data-semui="modal">${message(code:'budgetCode.create_new.label')}</button>

    <semui:modal id="addBudgetCodeModal" message="budgetCode.create_new.label">

        <g:form class="ui form" url="[controller: 'myInstitution', action: 'budgetCodes']" method="POST">
            <input type="hidden" name="cmd" value="newBudgetCode"/>
            <input type="hidden" name="redirect" value="redirect"/>

            <div class="field">
                <label>${message(code:'financials.budgetCode.description')}</label>
                <input type="text" name="bc"/>
            </div>

            <div class="field">
                <label>${message(code:'financials.budgetCode.usage')}</label>
                <textarea name="descr"></textarea>
            </div>

        </g:form>
    </semui:modal>

</g:if>
<%-- --%>

<div class="ui grid">
    <div class="column">
        <%--<button class="ui button" type="submit" data-semui="modal" href="#recentlyAdded_modal" id="showHideRecent">${message(code:'financials.recentCosts')}</button>--%>

        <g:if test="${editable}">
            <%--<button class="ui button pull-right" type="submit" id="BatchSelectedBtn" title="${g.message(code: 'financials.filtersearch.deleteAll')}" value="remove">Remove Selected</button>--%>

            <script>
                var isClicked = false;
                $('#btnAddNewCostItem').on('click', function(event) {
                    event.preventDefault();

                    // prevent 2 Clicks open 2 Modals
                    if (!isClicked) {
                        isClicked = true;
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty()

                        $.ajax({
                            url: "<g:createLink controller='finance' action='editCostItem'/>",
                            data: {
                                fixedSub: "${fixedSubscription?.id}",
                                currSub: "${currentSubscription?.id}",
                                tab: "${params.tab}"
                            }
                        }).done(function (data) {
                            $('#dynamicModalContainer').html(data);

                            $('#dynamicModalContainer .ui.modal').modal({
                                onVisible: function () {
                                    r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                                    r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');

                                    ajaxPostFunc()
                                },
                                detachable: true,
                                closable: false,
                                transition: 'scale',
                                onApprove: function () {
                                    $(this).find('.ui.form').submit();
                                    return false;
                                }
                            }).modal('show');
                        })
                        setTimeout(function () {
                            isClicked = false;
                        }, 800);
                    }
                })
            </script>

        </g:if>
    </div>
</div>

    <g:render template="recentlyAddedModal" />

    <div class="ui grid">
        <div class="sixteen wide column">

            <div id="filterTemplateWrapper" class="wrapper">
                <div id="filterTemplate">

                    <g:render template="filter" model="['ciList':cost_items, 'ciListCons':cost_items_CS, 'ciListSubscr':cost_items_SUBSCR]"/>

                    <g:render template="result" model="['forSingleSubscription':fixedSubscription, 'ciList':cost_items, 'ciCountOwner':cost_item_count, 'ciListCons':cost_items_CS, 'ciCountCons':cost_item_count_CS, 'ciListSubscr':cost_items_SUBSCR, 'ciCountSub':cost_item_count_SUBSCR, 'ownerOffset': ownerOffset, 'subscrOffset': subscrOffset, 'consOffset': consOffset]"/>
                </div>
            </div>

            <br />
            <br />
            <br />

            <%--
            <button class="ui button pull-right" data-offset="#jumpMark_top" title="Select this button to go back to the top of the page" id="top">${message(code:'financials.backToTop')}</button>
            --%>

        </div><!-- .sixteen -->
    </div><!-- .grid -->

<r:script>
    var financeRecentController = {

        pullJob : null,

        go : function() {
            //console.log("go")
            //financeRecentController.recentCostItems( null ); // pulls latest cost items
            //financeRecentController.pullJob = setInterval( financeRecentController.costItemsPresent, 60 * 1000 ); // Recently updated code block
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
                    clearTimeout(financeRecentController.pullJob);
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
                        clearTimeout(financeRecentController.pullJob);
                        $('#recentModalWrapper', '#showHideRecent').remove();
                    })
                    .done(function(data) {
                        if(data.count > 0) {
                            financeRecentController.recentCostItems(renderedDateTo);
                        }
                    });
            }
        }
    }

    var financeHelper = {

        /*
            This function calculates the total sum of the cost items. The number of cost items is the number of elements displayed on a page; it should be considered in the medium-term to
            deploy this onto server side for that "total" really means "total", i.e. is independent of the page the user is currently viewing.

            It is not the original developer annotating this code; so this "documentation" done a posteriori reflects a stranger's understanding of it.
        */
        calcSumOfCosts : function () {

            //take all tabs - we have two at most
            $('table[id^=costTable]').each( function() {

                //this is a collection box for all costs
                var costs = {};
                //get all currencies of cost items
                var allCostItems = $('.costData').filter('[data-elementSign="positive"],[data-elementSign="negative"]');
                /*
                    in the collection box, create for each currency the following sum counters:
                    - local sum (means the actual value and not the amount which is going to be paid)
                    - local sum after taxation (for Germany, VATs of 7 and 19 per cent apply)
                    - billing sum
                    - billing sum after taxation (see above)
                 */
                allCostItems.each(function() {
                    var currency = $(this).attr("data-billingCurrency");
                    if(typeof(costs[currency]) === 'undefined')
                        costs[currency] = {local: 0.0, localAfterTax: 0.0, billing: 0.0, billingAfterTax: 0.0};
                });
                /*
                    the information necessary has been stuffed into a <span> element and are thus defined in the templates
                    _result_tab_{cons|owner_table|subscr}. Again, we take only those which are marked as being considered:
                 */
                $(this).find('tbody tr span.costData').filter('[data-elementSign="positive"],[data-elementSign="negative"]').each( function() {

                    //take the correct currency map to assign
                    var ci = costs[$(this).attr('data-billingCurrency')];

                    /*
                        as of ERMS-804, costs can have several signs: they may be positive, negative or neutral. See the RefdataCategory 'Cost configuration'.
                        For positive and negative costs, we have to assign different operators here
                    */
                    var operators = {
                        'positive': function(a,b) { return a + b },
                        'negative': function(a,b) { return a - b }
                    };

                    /*
                        Here is the actual calculation being done:
                        we have to distinct between non-neutral and neutral cost items. We defined above operators which will be applied here.
                        As of January 4th, 2019, it is unclear what should happen with neutral costs. We shall collect them thus in separate counters for display.
                     */
                    if ($(this).attr('data-costInLocalCurrency')) {
                        ci.local = operators[$(this).attr('data-elementSign')](ci.local,parseFloat($(this).attr('data-costInLocalCurrency')))
                    }
                    if ($(this).attr('data-costInLocalCurrencyAfterTax')) {
                        ci.localAfterTax = operators[$(this).attr('data-elementSign')](ci.localAfterTax,parseFloat($(this).attr('data-costInLocalCurrencyAfterTax')))
                    }
                    if ($(this).attr('data-costInBillingCurrency')) {
                        ci.billing = operators[$(this).attr('data-elementSign')](ci.billing,parseFloat($(this).attr('data-costInBillingCurrency')))
                    }
                    if ($(this).attr('data-costInBillingCurrencyAfterTax')) {
                        ci.billingAfterTax = operators[$(this).attr('data-elementSign')](ci.billingAfterTax,parseFloat($(this).attr('data-costInBillingCurrencyAfterTax')))
                    }

                });

                //this is the final counter for all local costs, independently of their currency
                var finalLocal = 0.0;
                var finalLocalAfterTax = 0.0;

                //add all local costs and those after taxation (if there are costs at all)
                for (ci in costs) {
                    finalLocal += costs[ci].local;
                    finalLocalAfterTax += costs[ci].localAfterTax;
                }

                //get the current tab
                var currentTab = $(this).attr("data-queryMode");
                var colspan1;
                var colspan2;
                var totalHeaderRow;
                switch(currentTab) {
                    case 'OWNER':
                        <g:if test="${inSubMode}">
                            colspan1 = 3;
                            colspan2 = 5;
                        </g:if>
                        <g:else>
                            colspan1 = 4;
                            colspan2 = 5;
                        </g:else>
                        totalHeaderRow = '<tr><th colspan="'+colspan1+'"><strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong></th><th></th><th></th><th colspan="'+colspan2+'"></th></tr>';
                    break;
                    case 'CONS':
                    case 'CONS_AT_SUBSCR':
                        <g:if test="${inSubMode}">
                            colspan1 = 5;
                            colspan2 = 4;
                        </g:if>
                        <g:else>
                            colspan1 = 6;
                            colspan2 = 4;
                        </g:else>
                        totalHeaderRow = '<tr><th colspan="'+colspan1+'"><strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong></th><th></th><th></th><th></th><th colspan="'+colspan2+'"></th></tr>';
                    break;
                    case 'SUBSCR':
                        colspan1 = 2;
                        colspan2 = 4;
                        totalHeaderRow = '<tr><th colspan="'+colspan1+'"><strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong></th><th></th><th></th><th colspan="'+colspan2+'"></th></tr>';
                    break;
                    default: console.log("unhandled tab mode: "+currentTab);
                    break;
                }

                //display the local costs
                $("#localSum_"+currentTab).html('<strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(finalLocal)+'</strong>');
                $("#localSumAfterTax_"+currentTab).html('<strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(finalLocalAfterTax)+'</strong>');

                $("#sumOfCosts_"+currentTab).before(totalHeaderRow);
                var row = "";
                //and display each currency counter
                for (ci in costs) {
                    switch(currentTab) {
                        case 'OWNER':
                            row = '<tr><th colspan="'+colspan1+'"></th><th>${message(code:'financials.sum.billing')} ' + ci + '<br>${message(code:'financials.sum.billingAfterTax')}</th><th colspan="'+colspan2+'"></th></tr>';
                            row += '<tr><td colspan="'+colspan1+'"></td><td class="la-exposed-bg"><strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billing)+'</strong><br><strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billingAfterTax)+'</strong></td><td colspan="'+colspan2+'"></td></tr>';
                        break;
                        case 'CONS':
                        case 'CONS_AT_SUBSCR':
                            row = '<tr><th colspan="'+colspan1+'"></th><th>${message(code:'financials.sum.billing')} ' + ci + '</th><th></th><th>${message(code:'financials.sum.billingAfterTax')}</th><th colspan="'+colspan2+'"></th></tr>';
                            row += '<tr><td colspan="'+colspan1+'"></td><td class="la-exposed-bg"><strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billing)+'</strong></td>';
                            row += '<td></td>';
                            row += '<td class="la-exposed-bg"><strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billingAfterTax)+'</strong></td><td colspan="'+colspan2+'"></td></tr>';
                        break;
                        case 'SUBSCR':
                            row = '<tr><th colspan="'+colspan1+'"></th><th>${message(code:'financials.sum.billing')} ' + ci + '</th><th colspan="'+colspan2+'"></th></tr>';
                            row += '<tr><td colspan="'+colspan1+'"></td><td class="la-exposed-bg"><strong>'+Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billingAfterTax)+'</strong></td><td colspan="'+colspan2+'"></td></tr>';
                        break;
                        default: console.log("unhandled tab mode: "+currentTab);
                        break;
                    }
                    $("#sumOfCosts_"+currentTab).before(row);
                }
                if(typeof(ci) === 'undefined')
                    $("#sumOfCosts_"+currentTab).before('<tr><td colspan="13">${message(code:'financials.noCostsConsidered')}</td></tr>');
            })
        }
    }

    $(document).ready(function() {
        //financeRecentController.go() may make a further improvement; the recent added modal is not implemented anyway - needed to ensure export!
        financeHelper.calcSumOfCosts()
    })

    $('table[id^=costTable] .x .trigger-modal').on('click', function(e) {
        e.preventDefault()

        $.ajax({
            url: $(this).attr('href')
        }).done( function(data) {
            $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({
                onVisible: function () {
                    r2d2.initDynamicSemuiStuff('#costItem_ajaxModal');
                    r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');

                    ajaxPostFunc()
                },
                detachable: true,
                closable: false,
                transition: 'scale',
                onApprove : function() {
                    $(this).find('.ui.form').submit();
                    return false;
                }
            }).modal('show');
        })
    })
</r:script>

</body>
</html>
