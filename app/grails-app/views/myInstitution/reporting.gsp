<%@page import="de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants; de.laser.properties.PropertyDefinition" %>
<laser:serviceInjection/>
<!doctype html>
<r:require module="chartist"/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'myinst.reporting')}</title>
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
            <div class="four wide column" id="clickMe">
                <div class="ui grid">
                    <div class="row ui styled accordion">
                        <div class="title">
                            <i class="dropdown icon"></i>
                            Lizenz
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
                <div class="ui grid" id="firstContent">

                </div>
            </div>
        </div>
    </body>
    <r:script>
        $(document).ready(function() {
            let qParams = {status: [], propDef: "", propVal: [], form: [], resource: [], kind: []};
            $("#clickMe").on('change','.subscriptionParam',function(){
                let elem = $("[data-triggeredBy='"+$(this).attr("id")+"']");
                elem.toggleClass("hidden");
            });
            $("#clickMe").on('change','.subLoadingParam',function(){
                console.log($(this).parents("div.content")[0].getAttribute("data-propKey"));
                if($(this).attr("data-propKey") !== $(this).parents("div.content")[0].getAttribute("data-propKey"))
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
                if(qParams.status.length === 0)
                    qParams.status.push(${RDStore.SUBSCRIPTION_CURRENT.id});
                updateSubscriptions();
            });
            $("#clickMe").on('click','.propertyDefinition',function(e){
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
                    url: '<g:createLink controller="ajax" action="lookupSubscriptions"/>',
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
                    for(let k = 0;k < data.results.length;k++) {
                        let v = data.results[k]
                        subscriptionRows.push('<div class="row" data-subscription="'+v.value+'">'+v.name+'</div>');
                    }
                    $("#firstContent").html(subscriptionRows.join(""));
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
