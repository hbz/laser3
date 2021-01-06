<%@page import="de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants; de.laser.properties.PropertyDefinition; de.laser.ReportingService" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title><g:message code="laser"/> : <g:message code="myinst.reporting"/></title>
        <asset:stylesheet src="chartist.css"/><laser:javascript src="chartist.js"/>%{-- dont move --}%
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}"/>
            <semui:crumb text="${message(code:'myinst.reporting')}" class="active" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="reporting" params="${exportParams}">${message(code: 'default.button.export.xls')}</g:link>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
        </semui:controlButtons>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="myinst.reporting"/></h1>

        <div class="ui form grid">
            <div class="row" id="controlling">
                <button class="ui button primary" id="reset"><g:message code="myinst.reporting.reset"/></button>
                <button class="ui button" id="collapse"><i class="ui icon angle double left"></i></button>
            </div>
            <div class="four wide column" id="clickMe">
                <div class="grid">
                    <div class="row ui styled accordion">
                        <div class="title">
                            <i class="dropdown icon"></i>
                            <g:message code="myinst.reporting.general"/>
                        </div>
                        <div class="content">
                            <g:if test="${institution.getCustomerType() == "ORG_CONSORTIUM"}">
                                <div class="ui toggle checkbox">
                                    <input type="radio" name="general" id="members">
                                    <label for="members"><g:message code="myinst.reporting.members"/></label>
                                </div>
                            </g:if>
                            <div class="ui toggle checkbox">
                                <input type="radio" name="general" id="costs">
                                <label for="costs"><g:message code="myinst.reporting.costs"/></label>
                            </div>
                            <div class="ui toggle checkbox">
                                <input type="radio" name="general" id="subscriptions">
                                <label for="subscriptions"><g:message code="myinst.reporting.subscriptions"/></label>
                            </div>
                        </div>
                        <div class="title">
                            <i class="dropdown icon"></i>
                            <g:message code="subscription"/>
                        </div>
                        <div class="content">
                            <div class="accordion">
                                <div class="title">
                                    <i class="dropdown icon"></i>
                                    <g:message code="subscription.status.label"/>
                                </div>
                                <div class="content">
                                    <g:each in="${subStatus}" var="status">
                                        <div class="ui checkbox">
                                            <input type="checkbox" class="subLoadingParam" data-toArray="status" id="sub${status.value}" value="${status.id}"><label for="sub${status.value}">${status.getI10n("value")}</label>
                                        </div>
                                    </g:each>
                                </div>
                                <div class="title">
                                    <i class="dropdown icon"></i>
                                    <g:message code="myinst.reporting.subProp"/>
                                </div>
                                <div class="content">
                                    <div class="accordion">
                                        <g:each in="${subProp}" var="propDef">
                                            <div class="title subPropertyDefinition" id="sub${propDef.name}" <g:if test="${propDef.refdataCategory}">data-rdc="${propDef.refdataCategory}"</g:if> data-value="${genericOIDService.getOID(propDef)}" data-objecttype="${PropertyDefinition.SUB_PROP}">
                                                <i class="dropdown icon"></i>
                                                ${propDef.getI10n("name")}
                                            </div>
                                            <div class="content" data-triggeredBy="sub${propDef.name}" data-propKey="${genericOIDService.getOID(propDef)}"></div>
                                        </g:each>
                                    </div>
                                </div>
                                <div class="title">
                                    <i class="dropdown icon"></i>
                                    <g:message code="subscription.form.label"/>
                                </div>
                                <div class="content">
                                    <g:each in="${subForm}" var="form">
                                        <div class="ui checkbox">
                                            <input type="checkbox" class="subLoadingParam" data-toArray="form" id="sub${form.value}" value="${form.id}"><label for="sub${form.value}">${form.getI10n("value")}</label>
                                        </div>
                                    </g:each>
                                </div>
                                <div class="title">
                                    <i class="dropdown icon"></i>
                                    <g:message code="subscription.resource.label"/>
                                </div>
                                <div class="content">
                                    <g:each in="${subResourceType}" var="resource">
                                        <div class="ui checkbox">
                                            <input type="checkbox" class="subLoadingParam" data-toArray="resource" id="sub${resource.value}" value="${resource.id}"><label for="sub${resource.value}">${resource.getI10n("value")}</label>
                                        </div>
                                    </g:each>
                                </div>
                                <div class="title">
                                    <i class="dropdown icon"></i>
                                    <g:message code="subscription.kind.label"/>
                                </div>
                                <div class="content">
                                    <g:each in="${subKind}" var="kind">
                                        <div class="ui checkbox">
                                            <input type="checkbox" class="subLoadingParam" data-toArray="kind" id="sub${kind.value}" value="${kind.id}"><label for="sub${kind.value}">${kind.getI10n("value")}</label>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="twelve wide column">
                <div class="ui grid" id="selectionPanel">
                    <div class="ui row" id="displayConfigurations"></div>
                    <div class="ui row" id="selection">
                        <div id="result"></div>
                    </div>
                </div>
            </div>
        </div>
    </body>
    <asset:script type="text/javascript">
        $(document).ready(function() {
            $(".ui.checkbox").checkbox('uncheck');
            let expanded = true;
            let subFilter = {status: [], propDef: "", propVal: [], form: [], resource: [], kind: []};
            let subGrouping = [];
            let selPropDef;
            const controlling = $("#controlling");
            const selectionPanel = $("#selectionPanel");
            const clickMe = $("#clickMe");
            <g:if test="${institution.getCustomerType() == "ORG_CONSORTIUM"}">
                subGrouping.push("subscriber");
            </g:if>
            <g:elseif test="${institution.getCustomerType() == "ORG_CONSORTIUM"}">
                subGrouping.push("consortia");
            </g:elseif>
            let dConfs = [];
            controlling.on('click','#collapse',function() {
                expanded = !expanded;
                $("#clickMe").toggle();
                if(expanded) {
                    $(this).find("i").removeClass("right").addClass("left");
                }
                else {
                    $(this).find("i").removeClass("left").addClass("right");
                }
            });
            controlling.on('click','#reset',function(){
                $('.ui.checkbox').checkbox('uncheck');
                $('#displayConfigurations').empty();
                $('#selection').empty();
                $('.result').remove();
                selPropDef = null;
            });
            selectionPanel.on('click','.pickSubscription',function(){
                $(this).toggleClass('blue');
                let subscription = $(this).attr("data-entry");
                let subId = subscription.split(":")[1];
                let subscriptionContainer = $('#'+subscription);
                if(subscriptionContainer.length === 0 || (subscriptionContainer.find("#chart"+subId).is(":empty") && !subscriptionContainer.is(":visible"))) {
                    let requestOptions = JSON.stringify({ group: subGrouping, displayConfiguration: dConfs })
                    $.ajax({
                        url: '<g:createLink controller="ajaxHtml" action="getGraphsForSubscription"/>',
                        data: {
                            costItem: "true", //temp
                            subscription: subscription,
                            requestOptions: requestOptions
                        },
                        method: 'POST'
                    }).done(function(response){
                        if(subscriptionContainer.length === 0)
                            $("#result").append('<div data-entry="'+subscription+'">'+response+'</div>');
                        else if(subscriptionContainer.find("#chart"+subId).is(":empty"))
                            subscriptionContainer.html(response).show();
                    }).fail(function(xhr,status,message){
                        console.log(message);
                    });
                }
                else {
                    subscriptionContainer.hide();
                }
            });
            selectionPanel.on('click','.display',function(){
                $(this).toggleClass("red");
                let index = dConfs.indexOf($(this).attr("data-display"));
                if(index < 0) {
                    dConfs.push($(this).attr("data-display"));
                }
                else dConfs.splice(index,1);
            });
            selectionPanel.on('click','.generalLoadingParam',function(){
                if(!$(this).hasClass("red")) {
                    $(this).addClass("red");
                    let genGrouping = collectGenGrouping();
                    if(genGrouping.length > 0) {
                        let requestParams = {groupOptions: genGrouping.join(","),requestParam: $(this).attr("data-requestParam")};
                        if(genGrouping.indexOf('${ReportingService.CONFIG_ORG_PROPERTY}') > -1)
                            requestParams.propDef = selPropDef;
                        updateGeneral(requestParams);
                    }
                }
                else {
                    $(this).removeClass("red");
                    $("#"+$(this).attr("data-display")).hide();
                }
            });
            selectionPanel.on('change','.la-filterPropDef',function() {
                let groupOptions = collectGenGrouping();
                //console.log(groupOptions);
                if(groupOptions.length > 0) {
                    let requestParams = {groupOptions: groupOptions.join(","),requestParam: $(this).attr("data-requestParam"),propDef: selPropDef};
                    updateGeneral(requestParams);
                }
            });
            selectionPanel.on('click','#orgProperty',function(){
                const orgPropertySelection = $('#orgPropertySelection');
                if(!$(this).hasClass("red")) {
                    $(this).addClass("red");
                    if(orgPropertySelection.length === 0) {
                        loadThirdLevel({secondLevel: "orgProperty", queried: $(this).attr("data-requestParam")});
                    }
                    else {
                        orgPropertySelection.show();
                    }
                }
                else {
                    $(this).removeClass("red");
                    orgPropertySelection.hide();
                    selPropDef = null;
                    $("#property").hide();
                }
            });
            clickMe.on('change','[name="general"]',function(){
                loadFilter({entry:"general",queried:$(this).attr("id")});
            });
            clickMe.on('change','.subscriptionParam',function(){
                let elem = $("[data-triggeredBy='"+$(this).attr("id")+"']");
                elem.toggleClass("hidden");
                if($(this).attr("id") === "subProp" && $(this).is(':checked') === false) {
                    $(".subPropertyDefinition").each(function(k){
                        $(this).parent().accordion("close",k);
                    });
                }
            });
            clickMe.on('change','.subLoadingParam',function(){
                //console.log($(".subPropertyDefinition.active").attr("data-value"));
                if(typeof($(".subPropertyDefinition.active").attr("data-value")) === "undefined")
                    subFilter.propDef = "";
                subFilter.status = [];
                subFilter.propVal = [];
                subFilter.form = [];
                subFilter.resource = [];
                subFilter.kind = [];
                $(".subLoadingParam").each(function(k,v) {
                    if(v.checked){
                        subFilter[v.getAttribute("data-toArray")].push(v.value);
                    }
                });
                subFilter.propDef = $(".subPropertyDefinition.active").attr("data-value");
                if(subFilter.status.length === 0)
                    subFilter.status.push(${RDStore.SUBSCRIPTION_CURRENT.id});
                if($(":checked").length > 0 || subFilter.propDef !== ""){
                    loadFilter({entry:"subscription"});
                    updateSubscriptions();
                }
            });
            clickMe.on('click','.subPropertyDefinition',function(){
                let propDefKey = $(this).attr('data-value');
                subFilter.propDef = propDefKey;
                subFilter.propVal = [];
                loadFilter({entry:"subscription"});
                updateSubscriptions();
                let elemKey = $(this).attr("id");
                if($("[data-triggeredBy='"+elemKey+"']").is(':empty')) {
                    let params = {elemKey: elemKey, format: "json"};
                    if(typeof($(this).attr('data-rdc')) !== "undefined")
                        params.cat = $(this).attr('data-rdc');
                    else params.oid = propDefKey;
                    updatePropertyDefinitions(params);
                }
            });

            function loadFilter(config) {
                $.ajax({
                    url: '<g:createLink controller="ajaxHtml" action="loadGeneralFilter"/>',
                    data: config
                }).done(function(response){
                    $("#displayConfigurations").html(response);
                    $("#orgPropertySelection").remove();
                }).fail(function(xhr,status,message){
                    console.log("error occurred, consult logs!");
                });
            }

            function collectGenGrouping() {
                let genGrouping = [];
                let elem = $('.la-filterPropDef');
                $("#displayConfigurations .red").each(function(index){
                    let chart = $("#"+$(this).attr("data-requestParam")+$(this).attr("data-display"));
                    if(($(this).attr("data-display") !== "${ReportingService.CONFIG_ORG_PROPERTY}" && chart.length > 0) || ($(this).attr("data-display") === "${ReportingService.CONFIG_ORG_PROPERTY}" && elem.dropdown("get value") === selPropDef)) {
                        //console.log(elem.dropdown("get value")+" vs. "+selPropDef);
                        chart.show();
                    }
                    else {
                        if($(this).attr("data-display") === "${ReportingService.CONFIG_ORG_PROPERTY}" && elem.dropdown("get value") !== selPropDef) {
                            selPropDef = elem.dropdown("get value");
                        }
                        genGrouping.push($(this).attr("data-display"));
                    }
                });
                return genGrouping;
            }

            function updateGeneral(requestOptions) {
                $.ajax({
                    url: '<g:createLink controller="ajaxHtml" action="getGraphsForGeneral"/>',
                    data: {
                        requestOptions: JSON.stringify(requestOptions)
                    },
                    method: 'POST'
                }).done(function(response){
                    $("#result").append(response);
                }).fail(function(xhr,status,message){
                    console.log("error occurred, consult logs!");
                });
            }

            function loadThirdLevel(requestParams) {
                $.ajax({
                    url: '<g:createLink controller="ajaxHtml" action="loadThirdLevel" />',
                    data: requestParams
                }).done(function(response){
                    selectionPanel.append(response);
                }).fail(function(xhr,status,message){
                    console.log("error occurred, consult logs!");
                });
            }

            function updateSubscriptions() {
                $.ajax({
                    url: '<g:createLink controller="ajaxJson" action="lookupSubscriptions"/>',
                    data: {
                        restrictLevel: "true",
                        status: subFilter.status.join(","),
                        form: subFilter.form.join(","),
                        propDef: subFilter.propDef,
                        propVal: subFilter.propVal.join(","),
                        resource: subFilter.resource.join(","),
                        kind: subFilter.kind.join(",")
                    }
                }).done(function(data){
                    let subscriptionRows = [];
                    dConfs = [];
                    for(let k = 0;k < data.results.length;k++) {
                        let v = data.results[k]
                        let blue = '';
                        if($('div[data-entry="'+v.value+'"]').length > 0)
                            blue = 'blue';
                        subscriptionRows.push('<a class="ui label '+blue+' pickSubscription" data-entry="'+v.value+'">'+v.name+'</a>');
                    }
                    $("#selection").html(subscriptionRows.join(""));
                }).fail(function(xhr,status,message){
                    console.log("error occurred, consult logs!");
                });
            }

            function updatePropertyDefinitions(params) {
                let link;
                if(params.cat)
                    link = '<g:createLink controller="ajaxJson" action="refdataSearchByCategory"/>';
                else link = '<g:createLink controller="ajaxJson" action="getPropValues"/>';
                $.ajax({
                    url: link,
                    data: params
                }).done(function(data){
                    let elemContent = $("[data-triggeredBy='"+params.elemKey+"']");
                    for(let k = 0;k < data.length; k++){
                        let v = data[k];
                        let input = '<div class="ui checkbox"><input type="checkbox" class="subLoadingParam" data-toArray="propVal" data-propKey="'+params.oid+'" id="subPropVal'+k+'" value="'+v.value+'"><label for="subPropVal'+k+'">'+v.text+'</label></div>';
                        elemContent.append(input);
                    }
                }).fail(function(xhr,status,message){
                    console.log("error occurred, consult logs!")
                });
            }
        });
    </asset:script>
</html>
