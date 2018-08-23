<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.financials.label')}</title>

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
        %{--<semui:exportDropdownItem>--}%
            %{--<a class="item" data-mode="all" class="export" style="cursor: pointer">CSV Cost Items</a>--}%
    %{--</semui:exportDropdownItem>--}%
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
            <semui:actionsDropdownItem controller="myInstitution" action="financeImport" message="financials.action.financeImport" />
        </semui:actionsDropdown>
    </g:if>
</semui:controlButtons>

<g:if test="${fixedSubscription}">
    <h1 class="ui header"><semui:headerIcon />${message(code:'subscription.details.financials.label')} für ${fixedSubscription}</h1>
    <g:render template="../subscriptionDetails/nav" model="${[subscriptionInstance:fixedSubscription, params:(params << [id:fixedSubscription.id])]}"/> <%-- mapping="subfinance" params="${[sub:params.id]} --%>
</g:if>
<g:else>
    <h1 class="ui header"><semui:headerIcon />${message(code:'subscription.details.financials.label')} für ${institution.name}</h1>
</g:else>

<semui:messages data="${flash}" />

<div class="ui grid">
    <div class="column">
        <%--<button class="ui button" type="submit" data-semui="modal" href="#recentlyAdded_modal" id="showHideRecent">${message(code:'financials.recentCosts')}</button>--%>

        <g:if test="${editable}">
            <%--<button class="ui button pull-right" type="submit" id="BatchSelectedBtn" title="${g.message(code: 'financials.filtersearch.deleteAll')}" value="remove">Remove Selected</button>--%>

            <script>
                var isClicked = false;
                $('#btnAddNewCostItem').on('click', function(event) {
                    // prevent 2 Clicks open 2 Modals
                    if (!isClicked) {
                        isClicked = true;
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty()
                        $.ajax({
                            url: "<g:createLink controller='finance' action='editCostItem'/>",
                            data: {
                                sub: "${fixedSubscription?.id}"
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

                    <g:render template="filter" model="['ciListOwner': cost_items, 'ciListConsSubsc': cost_items_CS]"/>

                    <g:render template="result" model="['ciListOwner': cost_items, 'ciListConsSubsc': cost_items_CS]"/>
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
            console.log("go")
            financeRecentController.recentCostItems( null ); // pulls latest cost items
            financeRecentController.pullJob = setInterval( financeRecentController.costItemsPresent, 60 * 1000 ); // Recently updated code block
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

        calcSumOfCosts : function () {

            $('table[id^=costTable]').each( function() {

                var costs = {}
                var currencies = $.unique($(this).find('.costData').map(function(){
                    return $(this).attr('data-billingCurrency')
                }))
                currencies.each(function() {
                    costs[this] = {local: 0.0, localAfterTax: 0.0, billing: 0.0, billingAfterTax: 0.0}
                })

                $(this).find('tbody tr span.costData').each( function() {

                    var ci = costs[$(this).attr('data-billingCurrency')]
                    ci.local            += parseFloat($(this).attr('data-costInLocalCurrency'))
                    ci.localAfterTax    += parseFloat($(this).attr('data-costInLocalCurrencyAfterTax'))
                    ci.billing          += parseFloat($(this).attr('data-costInBillingCurrency'))
                    ci.billingAfterTax  += parseFloat($(this).attr('data-costInBillingCurrencyAfterTax'))
                })
                var socClass = $(this).find('span[class^=sumOfCosts]').attr('class')

                var finalLocal = 0.0
                var finalLocalAfterTax = 0.0

                for (ci in costs) {
                    finalLocal += costs[ci].local
                    finalLocalAfterTax += costs[ci].localAfterTax
                }

                var info = "Wert: "
                    info += Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(finalLocal)
                    info += "<br />"
                    info += "Endpreis nach Steuern: "
                    info += Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(finalLocalAfterTax)

                for (ci in costs) {
                    info += "<br /><br /><strong>" + ci + "</strong><br />"
                    info += "Rechnungssumme: "
                    info += Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billing)
                    info += "<br />"
                    info += "Endpreis nach Steuern: "
                    info += Intl.NumberFormat('de-DE', {style: 'currency', currency: ci}).format(costs[ci].billingAfterTax)
                }
                $('.' + socClass).html( info )
            })
        }
    }

    $(document).ready(function() {
        financeRecentController.go()
        financeHelper.calcSumOfCosts()
    })

    $('table[id^=costTable] .x .button:not(.negative)').on('click', function(e) {
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
