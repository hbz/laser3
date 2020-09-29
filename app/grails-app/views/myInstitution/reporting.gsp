<%@page import="de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants; de.laser.properties.PropertyDefinition" %>
<laser:serviceInjection/>
<!doctype html>
<r:require module="chartist"/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title><g:message code="laser"/> : <g:message code="myinst.reporting"/></title>
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

        <p>
            <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="myinst.reporting"/></h1>
        </p>

        <div class="ui grid">
            <div class="row" id="controlling">
                <button class="ui button primary" id="reset"><g:message code="myinst.reporting.reset"/></button>
                <button class="ui button" id="collapse"><i class="ui icon angle double left"></i></button>
            </div>
            <div class="four wide column" id="clickMe">
                <div class="grid">
                    <div class="row ui styled accordion">
                        <div class="title">
                            <i class="dropdown icon"></i>
                            <g:message code="subscription"/>
                        </div>
                        <div class="content">
                            <%-- TODO ask Ingrid for correct display! --%>
                            <div class="ui checkbox">
                                <input type="checkbox" class="subscriptionParam" id="subStatus">
                                <label for="subStatus">
                                    <g:message code="subscription.status.label"/>
                                </label><%-- opens status list --%>
                            </div>
                            <div class="ui checkbox">
                                <input type="checkbox" class="subscriptionParam" id="subProp">
                                <label for="subProp">
                                    <g:message code="myinst.reporting.subProp"/>
                                </label><%-- opens property definition list --%>
                            </div>
                            <div class="ui checkbox">
                                <input type="checkbox" class="subscriptionParam" id="subForm">
                                <label for="subForm">
                                    <g:message code="subscription.form.label"/>
                                </label>
                            </div>
                            <div class="ui checkbox">
                                <input type="checkbox" class="subscriptionParam" id="subResourceType">
                                <label for="subResourceType">
                                    <g:message code="subscription.resource.label"/>
                                </label>
                            </div>
                            <div class="ui checkbox">
                                <input type="checkbox" class="subscriptionParam" id="subKind">
                                <label for="subKind">
                                    <g:message code="subscription.kind.label"/>
                                </label>
                            </div>
                        </div>
                    </div>
                    <div class="row ui styled accordion hidden" data-triggeredBy="subStatus">
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
                    </div>
                    <div class="row ui styled accordion hidden" data-triggeredBy="subProp">
                        <div class="title">
                            <i class="dropdown icon"></i>
                            <g:message code="myinst.reporting.subProp"/>
                        </div>
                        <div class="content">
                            <div class="accordion">
                                <g:each in="${subProp}" var="propDef">
                                    <div class="title propertyDefinition" id="sub${propDef.name}" data-value="${genericOIDService.getOID(propDef)}" data-objecttype="${PropertyDefinition.SUB_PROP}">
                                        <i class="dropdown icon"></i>
                                        ${propDef.getI10n("name")}
                                    </div>
                                    <div class="content" data-triggeredBy="sub${propDef.name}" data-propKey="${genericOIDService.getOID(propDef)}"></div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                    <div class="row ui styled accordion hidden" data-triggeredBy="subForm">
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
                    </div>
                    <div class="row ui styled accordion hidden" data-triggeredBy="subResourceType">
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
                    </div>
                    <div class="row ui styled accordion hidden" data-triggeredBy="subKind">
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
            <div class="twelve wide column">
                <div class="ui grid" id="selectionPanel">
                    <div class="ui row" id="displayConfigurations"></div>
                    <div class="ui row" id="selection"></div>
                </div>
            </div>
        </div>
    </body>
    <r:script>
        $(document).ready(function() {
            $(".ui.checkbox").checkbox('uncheck');
            let expanded = true;
            let qParams = {status: [], propDef: "", propVal: [], form: [], resource: [], kind: []};
            let gParams = [];
            <g:if test="${institution.getCustomerType() == "ORG_CONSORTIUM"}">
                gParams.push("subscriber");
            </g:if>
            <g:elseif test="${institution.getCustomerType() == "ORG_CONSORTIUM"}">
                gParams.push("consortia");
            </g:elseif>
            let dConfs = [];
            $("#controlling").on('click','#collapse',function() {
                expanded = !expanded;
                $("#clickMe").toggle();
                if(expanded) {
                    $(this).find("i").removeClass("right").addClass("left");
                }
                else {
                    $(this).find("i").removeClass("left").addClass("right");
                }
            });
            $("#controlling").on('click','#reset',function(){
                $('.ui.checkbox').checkbox('uncheck');
                $('#displayConfigurations').empty();
                $("#selection").empty();
            });
            $("#selectionPanel").on('click','.pickSubscription',function(){
                $(this).toggleClass('blue');
                let subscription = $(this).attr("data-entry");
                let subId = subscription.split(":")[1];
                let subscriptionContainer = $('div[data-entry="'+subscription+'"]');
                if(subscriptionContainer.length === 0 || (subscriptionContainer.find("#chart"+subId).is(":empty") && !subscriptionContainer.is(":visible"))) {
                    let requestOptions = JSON.stringify({ group: gParams, displayConfiguration: dConfs })
                    $.ajax({
                        url: '<g:createLink controller="ajax" action="getGraphsForSubscription"/>',
                        data: {
                            costItem: "true", //temp
                            subscription: subscription,
                            requestOptions: requestOptions
                        },
                        method: 'POST'
                    }).done(function(response){
                        if(subscriptionContainer.length === 0)
                            $("#selection").after('<div data-entry="'+subscription+'">'+response+'</div>');
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
            $("#selectionPanel").on('click','.display',function(){
                $(this).toggleClass("red");
                let index = dConfs.indexOf($(this).attr("data-display"));
                if(index < 0) {
                    dConfs.push($(this).attr("data-display"));
                }
                else dConfs.splice(index,1);
            });
            $("#clickMe").on('change','.subscriptionParam',function(){
                let elem = $("[data-triggeredBy='"+$(this).attr("id")+"']");
                elem.toggleClass("hidden");
                if($(this).attr("id") === "subProp" && $(this).is(':checked') === false) {
                    $(".propertyDefinition").each(function(k){
                        $(this).parent().accordion("close",k);
                    });
                }
            });
            $("#clickMe").on('change','.subLoadingParam',function(){
                //console.log($(".propertyDefinition.active").attr("data-value"));
                if(typeof($(".propertyDefinition.active").attr("data-value")) === "undefined")
                    qParams.propDef = "";
                qParams.status = [];
                qParams.propVal = [];
                qParams.form = [];
                qParams.resource = [];
                qParams.kind = [];
                $(".subLoadingParam").each(function(k,v) {
                    if(v.checked){
                        qParams[v.getAttribute("data-toArray")].push(v.value);
                    }
                });
                qParams.propDef = $(".propertyDefinition.active").attr("data-value");
                if(qParams.status.length === 0)
                    qParams.status.push(${RDStore.SUBSCRIPTION_CURRENT.id});
                updateSubscriptions();
            });
            $("#clickMe").on('click','.propertyDefinition',function(){
                let propDefKey = $(this).attr('data-value');
                qParams.propDef = propDefKey;
                updateSubscriptions();
                let elemKey = $(this).attr("id");
                if($("[data-triggeredBy='"+elemKey+"']").is(':empty')) {
                    let params = {oid: propDefKey, elemKey: elemKey, format: "json"};
                    updatePropertyDefinitions(params);
                }
            });

            function updateSubscriptions() {
                $.ajax({
                    url: '<g:createLink controller="ajaxJson" action="lookupSubscriptions"/>',
                    data: {
                        restrictLevel: "true",
                        status: qParams.status.join(","),
                        form: qParams.form.join(","),
                        propDef: qParams.propDef,
                        propVal: qParams.propVal.join(","),
                        resource: qParams.resource.join(","),
                        kind: qParams.kind.join(",")
                    }
                }).done(function(data){
                    let subscriptionRows = [];
                    $("#displayConfigurations").empty().append('<a class="ui large label display" data-display="costItemDevelopment">Kostenentwicklung</a><a class="ui large label display" data-display="costItemDivision">Kostenaufteilung</a>');
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
                $.ajax({
                    url: '<g:createLink controller="ajax" action="getPropValues"/>',
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
    </r:script>
</html>
