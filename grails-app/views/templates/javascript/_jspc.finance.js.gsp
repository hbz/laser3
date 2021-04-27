// templates/javascript/_jspc.finance.js.gsp
<%@ page import="de.laser.finance.CostItemElementConfiguration; de.laser.helper.RDStore; de.laser.UserSetting" %>
<laser:serviceInjection/>

JSPC.finance${idSuffix} = {
    userLang: "${contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE,null)}",
    currentForm: $("#editCost_${idSuffix}"),
    newSubscription: $("#newSubscription_${idSuffix}"),
    newPackage: $("#newPackage_${idSuffix}"),
    newIE: $("#newIE_${idSuffix}"),
    newTitleGroup: $("#newTitleGroup_${idSuffix}"),
    toggleLicenseeTarget: $("#toggleLicenseeTarget_${idSuffix}"),
    newLicenseeTarget: $("#newLicenseeTarget_${idSuffix}"),
    newLicenseeDiv: $("#newLicenseeTarget_${idSuffix}").parent('div'),
    costBillingCurrency: $("#newCostInBillingCurrency_${idSuffix}"),
    costBillingCurrencyAfterTax: $("#newCostInBillingCurrencyAfterTax_${idSuffix}"),
    calculateBillingCurrency: $("#calculateBillingCurrency_${idSuffix}"),
    costCurrencyRate: $("#newCostCurrencyRate_${idSuffix}"),
    calculateCurrencyRate: $("#calculateExchangeRate_${idSuffix}"),
    costLocalCurrency: $("#newCostInLocalCurrency_${idSuffix}"),
    costLocalCurrencyAfterTax: $("#newCostInLocalCurrencyAfterTax_${idSuffix}"),
    calculateLocalCurrency: $("#calculateLocalCurrency_${idSuffix}"),
    costCurrency: $("#newCostCurrency_${idSuffix}"),
    costItemElement: $("#newCostItemElement_${idSuffix}"),
    billingSumRounding: $("#newBillingSumRounding_${idSuffix}"),
    finalCostRounding: $("#newFinalCostRounding_${idSuffix}"),
    taxRate: $("#newTaxRate_${idSuffix}"),
    ciec: $("#ciec_${idSuffix}"),
    costElems: $("#newCostInLocalCurrency_${idSuffix}, #newCostCurrencyRate_${idSuffix}, #newCostInBillingCurrency_${idSuffix}"),
    calc: $(".calc"),
    newSubscription: $("#newSubscription_${idSuffix}"),
    selectedMembers: $("[name='newLicenseeTarget']~a"),
    costItemElementConfigurations: {
    <%
        costItemElements.eachWithIndex { CostItemElementConfiguration ciec, int i ->
            String tmp = "${ciec.costItemElement.id}: ${ciec.elementSign.id}"
            if(i < costItemElements.size() - 1)
                tmp += ','
            println tmp
        }
    %>
    },
    selLinks: {
        newSubscription: "${createLink([controller:"ajaxJson", action:"lookupSubscriptions"])}?query={query}",
        <g:if test="${costItem?.sub || subscription}">
            <%
                String contextSub = ""
                if(costItem && costItem.sub)
                    contextSub = genericOIDService.getOID(costItem.sub)
                else if(subscription)
                    contextSub = genericOIDService.getOID(subscription)
            %>
            newPackage_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}&sub=${contextSub}",
            newIE_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub=${contextSub}",
            newTitleGroup_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}&sub=${contextSub}"
        </g:if>
        <g:else>
            newPackage_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}",
            newIE_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}",
            newTitleGroup_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}"
        </g:else>
    },
    eurVal: "${RDStore.CURRENCY_EUR.id}",
    isError: function(elem)  {
        if (elem.val().length <= 0 || elem.val() < 0) {
            $(".la-account-currency").children(".field").removeClass("error");
            elem.parent(".field").addClass("error");
            return true
        }
        return false
    },
    updateTitleDropdowns: function() {
        $(".newCISelect").each(function(k,v){
            console.log(JSPC.finance${idSuffix}.selLinks[$(this).attr("id")]);
            $(this).dropdown({
                apiSettings: {
                    url: JSPC.finance${idSuffix}.selLinks[$(this).attr("id")],
                    cache: false
                },
                clearable: true,
                minCharacters: 0
            });
        });
    <% if(costItem?.issueEntitlement) {
            String ieTitleName = costItem.issueEntitlement.tipp.name
            String ieTitleTypeString = costItem.issueEntitlement.tipp.titleType %>
            JSPC.finance${idSuffix}.newIE.dropdown('set text',"${ieTitleName} (${ieTitleTypeString}) (${costItem.sub.dropdownNamingConvention(contextService.getOrg())})");
    <%  }
    if(costItem?.issueEntitlementGroup) {
            String issueEntitlementGroupName = costItem.issueEntitlementGroup.name %>
            JSPC.finance${idSuffix}.newTitleGroup.dropdown('set text',"${issueEntitlementGroupName} (${costItem.sub.dropdownNamingConvention(contextService.getOrg())})");
    <%  }  %>
    },
    collect: function (fields) {
        let values = [];
        for(let i = 0;i < fields.length;i++) {
            let value = fields[i];
            if(!value.getAttribute("data-value").match(/:null|:for/))
                        values.push(value.getAttribute("data-value"));
        }
        //console.log(values);
        return values;
    },
    preselectMembers: function () {
        <g:if test="${pickedSubscriptions}">
            JSPC.finance${idSuffix}.newLicenseeTarget.dropdown("set selected",[${raw(pickedSubscriptions.join(','))}]);
            <g:if test="${pickedSubscriptions.size() > 9}">
                JSPC.finance${idSuffix}.newLicenseeTarget.parent('div').toggle();
            </g:if>
        </g:if>
    },
    onSubscriptionUpdate: function () {
        let context;
        if(JSPC.finance${idSuffix}.selectedMembers.length === 1){
            let values = JSPC.finance${idSuffix}.collect(JSPC.finance${idSuffix}.selectedMembers);
                if(!values[0].match(/:null|:for/)) {
                context = values[0];
            }
        }
        else if(JSPC.finance${idSuffix}.newLicenseeTarget.length === 0)
            context = JSPC.finance${idSuffix}.val();
        JSPC.finance${idSuffix}.selLinks.newIE_${idSuffix} = "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="+context;
        JSPC.finance${idSuffix}.selLinks.newTitleGroup_${idSuffix} = "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}&sub="+context;
        JSPC.finance${idSuffix}.selLinks.newPackage_${idSuffix} = "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}&ctx="+context;
        JSPC.finance${idSuffix}.newIE.dropdown('clear');
        JSPC.finance${idSuffix}.newTitleGroup.dropdown('clear');
        JSPC.finance${idSuffix}.newPackage.dropdown('clear');
        JSPC.finance${idSuffix}.updateTitleDropdowns();
    },
    checkPackageBelongings: function () {
        var subscription = $("#newSubscription_${idSuffix}, #pickedSubscription_${idSuffix}").val();
        let values = JSPC.finance${idSuffix}.collect(JSPC.finance${idSuffix}.selectedMembers);
        if(values.length === 1) {
            subscription = values[0];
            $.ajax({
                url: "<g:createLink controller="ajaxJson" action="checkCascade"/>?subscription="+subscription+"&package="+JSPC.finance${idSuffix}.newPackage.val()+"&issueEntitlement="+JSPC.finance${idSuffix}.newIE.val(),
            }).done(function (response) {
                //console.log("function ran through w/o errors, please continue implementing! Response from server is: "+JSON.stringify(response))
                if(!response.sub)
                    JSPC.finance${idSuffix}.newSubscription.addClass("error");
                else
                    JSPC.finance${idSuffix}.newSubscription.removeClass("error");
                if(!response.subPkg)
                    JSPC.finance${idSuffix}.newPackage.addClass("error");
                else
                    JSPC.finance${idSuffix}.newPackage.removeClass("error");
                if(!response.ie)
                    JSPC.finance${idSuffix}.newIE.addClass("error");
                else
                    JSPC.finance${idSuffix}.newIE.removeClass("error");
            }).fail(function () {
                console.log("AJAX error! Please check logs!");
            });
        }
    },
    checkValues: function () {
        if ( (JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costBillingCurrency.val()) * JSPC.finance${idSuffix}.costCurrencyRate.val()).toFixed(2) !== JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costLocalCurrency.val()).toFixed(2) ) {
            console.log("inserted values are: "+JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costBillingCurrency.val())+" * "+JSPC.finance${idSuffix}.costCurrencyRate.val()+" = "+JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costLocalCurrency.val()).toFixed(2)+", correct would be: "+(JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costBillingCurrency.val()) * JSPC.finance${idSuffix}.costCurrencyRate.val()).toFixed(2));
            JSPC.finance${idSuffix}.costElems.parent('.field').addClass('error');
            return false;
        }
        else {
            JSPC.finance${idSuffix}.costElems.parent('.field').removeClass('error');
            return true;
        }
    },
    calcTaxResults: function () {
        let roundB = JSPC.finance${idSuffix}.billingSumRounding.prop('checked');
        let roundF = JSPC.finance${idSuffix}.finalCostRounding.prop('checked');
        //console.log(taxRate.val());
        let taxF = 1.0 + (0.01 * JSPC.finance${idSuffix}.taxRate.val().split("§")[1]);
        let parsedBillingCurrency = JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costBillingCurrency.val().trim());
        let parsedLocalCurrency = JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costLocalCurrency.val().trim());
        let billingCurrencyAfterRounding = roundB ? Math.round(parsedBillingCurrency) : JSPC.finance${idSuffix}.convertDouble(parsedBillingCurrency)
        let localCurrencyAfterRounding = roundB ? Math.round(parsedLocalCurrency) : JSPC.finance${idSuffix}.convertDouble(parsedLocalCurrency)
        JSPC.finance${idSuffix}.costBillingCurrency.val(JSPC.finance${idSuffix}.outputValue(billingCurrencyAfterRounding));
        JSPC.finance${idSuffix}.costLocalCurrency.val(JSPC.finance${idSuffix}.outputValue(localCurrencyAfterRounding));
        let billingAfterTax = roundF ? Math.round(billingCurrencyAfterRounding * taxF) : JSPC.finance${idSuffix}.convertDouble(billingCurrencyAfterRounding * taxF)
        let localAfterTax = roundF ? Math.round(localCurrencyAfterRounding * taxF ) : JSPC.finance${idSuffix}.convertDouble(localCurrencyAfterRounding * taxF)
        JSPC.finance${idSuffix}.costBillingCurrencyAfterTax.val(
             JSPC.finance${idSuffix}.outputValue(billingAfterTax)
        );
        JSPC.finance${idSuffix}.costLocalCurrencyAfterTax.val(
             JSPC.finance${idSuffix}.outputValue(localAfterTax)
        );
    },
    convertDouble: function (input) {
        let output;
        //determine locale from server
        if(typeof(input) === 'number') {
            output = input.toFixed(2);
            console.log("input: "+input+", typeof: "+typeof(input));
        }
        else if(typeof(input) === 'string') {
            output = 0.0;
            if(JSPC.finance${idSuffix}userLang === 'en') {
                output = parseFloat(input);
            }
            else {
                if(input.match(/(\d+\.?)*\d+(,\d{2})?/g))
                    output = parseFloat(input.replace(/\./g,"").replace(/,/g,"."));
                else if(input.match(/(\d+,?)*\d+(\.\d{2})?/g))
                    output = parseFloat(input.replace(/,/g, ""));
                else console.log("Please check over regex!");
            }
            //console.log("string input parsed, output is: "+output);
        }
        return output;
    },
    outputValue: function(input) {
        //console.log(userLang);
        let output;
        if(JSPC.finance${idSuffix}.userLang !== 'en')
            output = input.toString().replace(".",",");
        else output = input.toString();
        //console.log("output: "+output+", typeof: "+typeof(output));
        return output;
    },
    init: function(elem) {
        //console.log(this);
        this.newSubscription.change(function(){
            JSPC.finance${idSuffix}.onSubscriptionUpdate();
        });
        this.newLicenseeTarget.change(function(){
            JSPC.finance${idSuffix}.onSubscriptionUpdate();
        });
        this.toggleLicenseeTarget.click( function() {
            JSPC.finance${idSuffix}.newLicenseeTarget.parent('div').toggle();
        });
        this.newPackage.change(function(){
            let context;
            if(JSPC.finance${idSuffix}.selectedMembers.length === 1) {
                let values = JSPC.finance${idSuffix}.collect(JSPC.finance${idSuffix}.selectedMembers);
                if(!values[0].match(/:null|:for/)) {
                    context = values[0];
                }
            }
            else if(JSPC.finance${idSuffix}.selectedMembers.length === 0)
                context = JSPC.finance${idSuffix}.newSubscription.val();
            JSPC.finance${idSuffix}.selLinks.newIE = "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="+context+"&pkg="+JSPC.finance${idSuffix}.newPackage.val();
            JSPC.finance${idSuffix}.newIE.dropdown('clear');
            JSPC.finance${idSuffix}.updateTitleDropdowns();
        });
        this.newIE.change(function(){
            JSPC.finance${idSuffix}.checkPackageBelongings();
        });
        this.newTitleGroup.change(function(){
            JSPC.finance${idSuffix}.updateTitleDropdowns();
        });
        this.calculateBillingCurrency.click( function() {
            if (! JSPC.finance${idSuffix}.isError(JSPC.finance${idSuffix}.costLocalCurrency) && ! JSPC.finance${idSuffix}.isError(JSPC.finance${idSuffix}.costCurrencyRate)) {
                let parsedLocalCurrency = JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costLocalCurrency.val().trim());
                JSPC.finance${idSuffix}.costBillingCurrency.val(JSPC.finance${idSuffix}.outputValue(JSPC.finance${idSuffix}.parsedLocalCurrency / JSPC.finance${idSuffix}.costCurrencyRate.val().trim()));
                $(".la-account-currency").find(".field").removeClass("error");
                JSPC.finance${idSuffix}.calcTaxResults();
            }
        });
        this.calculateCurrencyRate.click( function() {
            if (! JSPC.finance${idSuffix}.isError(JSPC.finance${idSuffix}.costLocalCurrency) && ! JSPC.finance${idSuffix}.isError(JSPC.finance${idSuffix}.costBillingCurrency)) {
                let parsedLocalCurrency = JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costLocalCurrency.val().trim());
                let parsedBillingCurrency = JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costBillingCurrency.val().trim());
                JSPC.finance${idSuffix}.costCurrencyRate.val((parsedLocalCurrency / parsedBillingCurrency));
                $(".la-account-currency").find(".field").removeClass("error");
                JSPC.finance${idSuffix}.calcTaxResults();
            }
        });
        this.calculateLocalCurrency.click( function() {
            if (! JSPC.finance${idSuffix}.isError(JSPC.finance${idSuffix}.costBillingCurrency) && ! JSPC.finance${idSuffix}.isError(JSPC.finance${idSuffix}.costCurrencyRate)) {
                let parsedBillingCurrency = JSPC.finance${idSuffix}.convertDouble(JSPC.finance${idSuffix}.costBillingCurrency.val().trim());
                JSPC.finance${idSuffix}.costLocalCurrency.val(JSPC.finance${idSuffix}.outputValue(parsedBillingCurrency * JSPC.finance${idSuffix}.costCurrencyRate.val().trim()));
                $(".la-account-currency").find(".field").removeClass("error");
                JSPC.finance${idSuffix}.calcTaxResults();
            }
        });
        this.costBillingCurrency.change( function(){
            if(JSPC.finance${idSuffix}.costCurrency.val() == JSPC.finance${idSuffix}.eurVal) {
                JSPC.finance${idSuffix}.calculateLocalCurrency.click();
            }
        });
        this.costItemElement.change(function() {
            console.log(JSPC.finance${idSuffix}.ciec);
            if(typeof(JSPC.finance${idSuffix}.costItemElementConfigurations[JSPC.finance${idSuffix}.costItemElement.val()]) !== 'undefined')
                JSPC.finance${idSuffix}.ciec.dropdown('set selected', JSPC.finance${idSuffix}.costItemElementConfigurations[JSPC.finance${idSuffix}.costItemElement.val()]);
            else
                JSPC.finance${idSuffix}.ciec.dropdown('set selected','null');
        });
        this.calc.change( function() {
            JSPC.finance${idSuffix}.calcTaxResults();
        });
        this.costElems.change(function(){
            JSPC.finance${idSuffix}.checkValues();
            if(JSPC.finance${idSuffix}.costCurrency.val() != 0) {
                JSPC.finance${idSuffix}.costCurrency.parent(".field").removeClass("error");
            }
            else {
                JSPC.finance${idSuffix}.costCurrency.parent(".field").addClass("error");
            }
        });
        this.costCurrency.change(function(){
            //console.log("event listener succeeded, picked value is: "+$(this).val());
            if($(this).val() === JSPC.finance${idSuffix}.eurVal)
                JSPC.finance${idSuffix}.costCurrencyRate.val(1.0);
            else JSPC.finance${idSuffix}.costCurrencyRate.val(0.0);
            JSPC.finance${idSuffix}.calculateLocalCurrency.click();
        });
        this.currentForm.submit(function(e){
            e.preventDefault();
            if(JSPC.finance${idSuffix}.costCurrency.val() != 0) {
                let valuesCorrect = JSPC.finance${idSuffix}.checkValues();
                if(valuesCorrect) {
                    JSPC.finance${idSuffix}.costCurrency.parent(".field").removeClass("error");
                    if(JSPC.finance${idSuffix}.newSubscription.hasClass('error') || JSPC.finance${idSuffix}.newPackage.hasClass('error') || JSPC.finance${idSuffix}.newIE.hasClass('error'))
                        alert("${message(code:'financials.newCosts.entitlementError')}");
                    else {
                        if(JSPC.finance${idSuffix}.newLicenseeTarget.length === 1 && JSPC.finance${idSuffix}.newLicenseeTarget.val().length === 0) {
                            alert("${message(code:'financials.newCosts.noSubscriptionError')}")
                        }
                        else {
                            //console.log(JSPC.finance${idSuffix}.newLicenseeTarget.val());
                            if(JSPC.finance${idSuffix}.newLicenseeTarget.val() && JSPC.finance${idSuffix}.newLicenseeTarget.val().join(";").indexOf('forParent') > -1) {
                                if(confirm("${message(code:'financials.newCosts.confirmForParent')}")) JSPC.finance${idSuffix}.currentForm.unbind('submit').submit();
                            }
                            else JSPC.finance${idSuffix}.currentForm.unbind('submit').submit();
                        }
                    }
                }
                else {
                     alert("${message(code:'financials.newCosts.calculationError')}");
                }
            }
            else {
                alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
                JSPC.finance${idSuffix}.costCurrency.parent(".field").addClass("error");
           }
        });
    }
}
JSPC.finance${idSuffix}.init();