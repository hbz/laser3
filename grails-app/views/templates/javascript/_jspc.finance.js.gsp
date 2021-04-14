<%@ page import="de.laser.finance.CostItemElementConfiguration; com.k_int.kbplus.GenericOIDService; de.laser.helper.RDStore" %>
// templates/javascript/_jspc.finance.js.gsp

JSPC.finance = {
    currentForm: $("#editForm_${idSuffix}"),
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
    finalCostRounding: $("#newFinalCostRounding_${idSuffix}"),
    taxRate: $("newTaxRate_${idSuffix}"),
    ciec: $("#ciec_${idSuffix}"),
    costElems: $("#newCostInLocalCurrency_${idSuffix}, #newCostCurrencyRate_${idSuffix}, #newCostInBillingCurrency_${idSuffix}"),
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
                    contextSub = GenericOIDService.getOID(costItem.sub)
                else if(subscription)
                    contextSub = GenericOIDService.getOID(subscription)
            %>
            newPackage: "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}&sub="${contextSub}",
            newIE: "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="${contextSub}",
            newTitleGroup: "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}&sub="${contextSub}"
        </g:if>
        <g:else>
            newPackage: "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}",
            newIE: "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}",
            newTitleGroup: "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}"
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
    calcTaxResults: function () {
        let roundF = finalCostRounding.prop('checked');
        //console.log(taxRate.val());
        let taxF = 1.0 + (0.01 * taxRate.val().split("§")[1]);
        let parsedBillingCurrency = convertDouble(costBillingCurrency.val().trim());
        let parsedLocalCurrency = convertDouble(costLocalCurrency.val().trim());
        costBillingCurrencyAfterTax.val(
            roundF ? Math.round(parsedBillingCurrency * taxF) : convertDouble(parsedBillingCurrency * taxF)
        );
        costLocalCurrencyAfterTax.val(
            roundF ? Math.round(parsedLocalCurrency * taxF ) : convertDouble(parsedLocalCurrency * taxF )
        );
    },
    init: function(elem) {
        if(typeof(this.toggleLicenseeTarget) !== 'undefined') {
            this.toggleLicenseeTarget.click( function() {
                this.newLicenseeTarget.toggle();
            });
        }
        this.calculateBillingCurrency.click( function() {
            if (! isError(this.costLocalCurrency) && ! isError(this.costCurrencyRate)) {
                let parsedLocalCurrency = this.convertDouble(this.costLocalCurrency.val().trim());
                this.costBillingCurrency.val(this.convertDouble(this.parsedLocalCurrency / this.costCurrencyRate.val().trim()));
                $(".la-account-currency").find(".field").removeClass("error");
                this.calcTaxResults();
            }
        });
        this.calculateCurrencyRate.click( function() {
            if (! isError(costLocalCurrency) && ! isError(costBillingCurrency)) {
                let parsedLocalCurrency = this.convertDouble(this.costLocalCurrency.val().trim());
                let parsedBillingCurrency = this.convertDouble(this.costBillingCurrency.val().trim());
                this.costCurrencyRate.val((parsedLocalCurrency / parsedBillingCurrency));
                $(".la-account-currency").find(".field").removeClass("error");
                this.calcTaxResults();
            }
        });
        this.calculateLocalCurrency.click( function() {
            if (! isError(this.costBillingCurrency) && ! isError(this.costCurrencyRate)) {
                let parsedBillingCurrency = this.convertDouble(this.costBillingCurrency.val().trim());
                this.costLocalCurrency.val(this.convertDouble(parsedBillingCurrency * this.costCurrencyRate.val().trim()));
                $(".la-account-currency").find(".field").removeClass("error");
                this.calcTaxResults();
            }
        });
        this.costBillingCurrency.change( function(){
            if(this.costCurrency.val() == eurVal) {
                this.calculateLocalCurrency.click();
            }
        });
        $('.calc').change( function() {
            this.calcTaxResults();
        });
        this.costItemElement.change(function() {
            //console.log(this.costItemElementConfigurations[this.costItemElement.val()]);
            if(typeof(this.costItemElementConfigurations[this.costItemElement.val()]) !== 'undefined')
                this.ciec.dropdown('set selected', this.costItemElementConfigurations[this.costItemElement.val()]);
            else
                this.ciec.dropdown('set selected','null');
        });
        this.costElems.change(function(){
            this.checkValues();
            if(this.costCurrency.val() != 0) {
                this.custCurrency.parent(".field").removeClass("error");
            }
            else {
                this.costCurrency.parent(".field").addClass("error");
            }
        });
        this.currentForm.submit(function(e){
            //console.log("eee");
            e.preventDefault();
            if(this.costCurrency.val() != 0) {
                let valuesCorrect = this.checkValues();
                if(valuesCorrect) {
                    costCurrency.parent(".field").removeClass("error");
                    if(this.newSubscription.hasClass('error') || this.newPackage.hasClass('error') || this.newIE.hasClass('error'))
                        alert("${message(code:'financials.newCosts.entitlementError')}");
                    else {
                        if(this.newLicenseeTarget.length === 1 && this.newLicenseeTarget.val()[0] === '') {
                            alert("${message(code:'financials.newCosts.noSubscriptionError')}")
                        }
                        else {
                            if(this.newLicenseeTarget.val() && this.newLicenseeTarget.val().join(";").indexOf('forParent') > -1) {
                                if(confirm("${message(code:'financials.newCosts.confirmForParent')}")) this.currentForm.unbind('submit').submit();
                            }
                            else this.currentForm.unbind('submit').submit();
                        }
                    }
                }
                else {
                     alert("${message(code:'financials.newCosts.calculationError')}");
                }
            }
            else {
                alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
                this.costCurrency.parent(".field").addClass("error");
           }
        });
    }
}
JSPC.finance.init();
<%-- <laser:script file="${this.getGroovyPageFileName()}">


            $("#newCostCurrency").change(function(){
                //console.log("event listener succeeded, picked value is: "+$(this).val());
                if($(this).val() === JSPC.app.eurVal)
                    currentForm.find("[name='newCostCurrencyRate']").val(1.0);
                else currentForm.find("[name='newCostCurrencyRate']").val(0.0);
                currentForm.find('calculateLocalCurrency').click();
            });

        <g:if test="${idSuffix != 'bulk'}">
            $("[name='newSubscription']").change(function(){
                JSPC.app.onSubscriptionUpdate();
                JSPC.callbacks.dynPostFunc();
            });

            JSPC.app.onSubscriptionUpdate = function () {
                let context;
                //console.log($("[name='newLicenseeTarget']~a").length);
                if($("[name='newLicenseeTarget']~a").length === 1){
                    let values = JSPC.app.collect($("[name='newLicenseeTarget']~a"));
                    if(!values[0].match(/:null|:for/)) {
                        context = values[0];
                    }
                }
                else if($("[name='newLicenseeTarget']").length === 0)
                    context = $("[name='newSubscription']").val();
                JSPC.app.selLinks.newIE = "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="+context;
                JSPC.app.selLinks.newTitleGroup = "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}&sub="+context;
                JSPC.app.selLinks.newPackage = "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}&ctx="+context;
                $("#newIE").dropdown('clear');
                $("#newTitleGroup").dropdown('clear');
                $("#newPackage").dropdown('clear');
                JSPC.callbacks.dynPostFunc();
            }

            $("#newPackage").change(function(){
                let context;
                if($("[name='newLicenseeTarget']~a").length === 1) {
                    let values = JSPC.app.collect($("[name='newLicenseeTarget']~a"));
                    if(!values[0].match(/:null|:for/)) {
                        context = values[0];
                    }
                }
                else if($("[name='newLicenseeTarget']").length === 0)
                    context = $("[name='newSubscription']").val();
                JSPC.app.selLinks.newIE = "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="+context+"&pkg="+$("[name='newPackage']").val();
                $("#newIE").dropdown('clear');
                JSPC.callbacks.dynPostFunc();
            });

            $("#newIE").change(function(){
                JSPC.app.checkPackageBelongings();
            });

            $("[name='newTitleGroup']").change(function(){
                JSPC.callbacks.dynPostFunc();
            });

            JSPC.callbacks.dynPostFunc = function() {
                console.log('dynPostFunc @ finance/_ajaxModal.gsp')

                $(".newCISelect").each(function(k,v){
                    $(this).dropdown({
                        apiSettings: {
                            url: JSPC.app.selLinks[$(this).attr("id")],
                            cache: false
                        },
                        clearable: true,
                        minCharacters: 0
                    });
                });
                <%
                    if(costItem?.issueEntitlement) {
                        String ieTitleName = costItem.issueEntitlement.tipp.name
                        String ieTitleTypeString = costItem.issueEntitlement.tipp.titleType
                        %>
                    $("#newIE").dropdown('set text',"${ieTitleName} (${ieTitleTypeString}) (${costItem.sub.dropdownNamingConvention(contextService.getOrg())})");
                <%  }  %>
                <%
                    if(costItem?.issueEntitlementGroup) {
                        String issueEntitlementGroupName = costItem.issueEntitlementGroup.name
                        %>
                $("#newTitleGroup").dropdown('set text',"${issueEntitlementGroupName} (${costItem.sub.dropdownNamingConvention(contextService.getOrg())})");
                <%  }  %>
            }

            JSPC.app.checkPackageBelongings = function () {
                var subscription = $("[name='newSubscription'], #pickedSubscription").val();
                let values = JSPC.app.collect($("[name='newLicenseeTarget']~a"))
                if(values.length === 1) {
                    subscription = values[0];
                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="checkCascade"/>?subscription="+subscription+"&package="+$("[name='newPackage']").val()+"&issueEntitlement="+$("[name='newIE']").val(),
                        }).done(function (response) {
                            //console.log("function ran through w/o errors, please continue implementing! Response from server is: "+JSON.stringify(response))
                            if(!response.sub) $("#newSubscription").addClass("error");
                            else $("#newSubscription").removeClass("error");
                            if(!response.subPkg) $("#newPackage").addClass("error");
                            else $("#newPackage").removeClass("error");
                            if(!response.ie) $("#newIE").addClass("error");
                            else $("#newIE").removeClass("error");
                        }).fail(function () {
                            console.log("AJAX error! Please check logs!");
                        });
                    }
                }
        </g:if>


            JSPC.app.setupCalendar = function () {
                $("[name='newFinancialYear']").parents(".datepicker").calendar({
                    type: 'year',
                    minDate: new Date('1582-10-15'),
                    maxDate: new Date('2099-12-31')
                });
            }

            JSPC.app.checkValues = function () {
                if ( (JSPC.app.convertDouble(currentForm.find("[name='newCostInBillingCurrency']").val()) * currentForm.find("[name='newCostCurrencyRate']").val()).toFixed(2) !== JSPC.app.convertDouble(currentForm.find("[name='newCostInLocalCurrency']").val()).toFixed(2) ) {
                    console.log("inserted values are: "+JSPC.app.convertDouble(currentForm.find("[name='newCostInBillingCurrency']").val())+" * "+currentForm.find("[name='newCostCurrencyRate']").val()+" = "+JSPC.app.convertDouble(currentForm.find("[name='newCostInLocalCurrency']").val()).toFixed(2)+", correct would be: "+(JSPC.app.convertDouble(currentForm.find("[name='newCostInBillingCurrency']").val()) * currentForm.find("[name='newCostCurrencyRate']").val()).toFixed(2));
                    JSPC.app.costElems.parent('.field').addClass('error');
                    return false;
                }
                else {
                    JSPC.app.costElems.parent('.field').removeClass('error');
                    return true;
                }
            }


            JSPC.app.collect = function (fields) {
                let values = [];
                for(let i = 0;i < fields.length;i++) {
                    let value = fields[i];
                    if(!value.getAttribute("data-value").match(/:null|:for/))
                        values.push(value.getAttribute("data-value"));
                }
                //console.log(values);
                return values;
            }

            JSPC.app.convertDouble = function (input) {
                //console.log("input: "+input+", typeof: "+typeof(input));
                var output;
                //determine locale from server
                var userLang = "${contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE,null)}";
                //console.log(userLang);
                if(typeof(input) === 'number') {
                    output = input.toFixed(2);
                    if(userLang !== 'en')
                        output = output.replace(".",",");
                }
                else if(typeof(input) === 'string') {
                    output = 0.0;
                    if(userLang === 'en') {
                        output = parseFloat(input);
                    }
                    else {
                        if(input.match(/(\d{1-3}\.?)*\d+(,\d{2})?/g))
                            output = parseFloat(input.replace(/\./g,"").replace(/,/g,"."));
                        else if(input.match(/(\d{1-3},?)*\d+(\.\d{2})?/g)) {
                            output = parseFloat(input.replace(/,/g, ""));
                        }
                        else console.log("Please check over regex!");
                    }
                    //console.log("string input parsed, output is: "+output);
                }
                return output;
            }

            JSPC.app.preselectMembers = function () {
                <g:if test="${pickedSubscriptions}">
                    $("#newLicenseeTarget").dropdown("set selected",[${raw(pickedSubscriptions.join(','))}]);
                    <g:if test="${pickedSubscriptions.size() > 9}">
                        $("#newLicenseeTarget").parent('div').toggle();
                    </g:if>
                </g:if>

            }

    </laser:script> --%>