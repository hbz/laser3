<%@page import="de.laser.helper.RDStore" %>
<!doctype html>
<r:require module="chartist"/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'myinst.reporting')}</title>
    </head>

    <p>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}"/>
            <semui:crumb text="${message(code:'myinst.reporting')}" class="active" />
        </semui:breadcrumbs>

        <g:if test="${params.subscription || params.package}">
            <%
                Map<String,Object> exportParams = [format:'xls']
                if(params.subscription)
                    exportParams.subscription = params.subscription
                else if(params.package)
                    exportParams.package = params.package
                else if(params.organisation)
                    exportParams.organisation = params.organisation
            %>
            <semui:controlButtons>
                <semui:exportDropdown>
                    <semui:exportDropdownItem>
                        <g:link class="item" action="reporting" params="${exportParams}">${message(code: 'default.button.export.xls')}</g:link>
                    </semui:exportDropdownItem>
                </semui:exportDropdown>
            </semui:controlButtons>
        </g:if>

        <p>
            <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="myinst.reporting"/></h1>
        </p>

        <semui:filter>
            <g:form name="startingPoint" class="ui form" action="reporting">
                <div class="three fields">
                    <div class="field">
                        <label for="subscription">
                            <g:message code="subscription"/>
                        </label>
                        <g:select name="subscription" from="${subscriptions}" value="${params.subscription}"
                                  optionKey="${{it.id}}" optionValue="${{it.dropdownNamingConvention(institution)}}"
                                  noSelection="['':message(code:'default.select.choose.label')]"
                                  class="ui search selection dropdown"/>
                    </div>
                    <div class="field">
                        <label for="package">
                            <g:message code="package"/>
                        </label>
                        <g:select name="package" from="${packages}" value="${params.package}"
                                  optionKey="${{it.id}}" optionValue="${{it.name}}"
                                  noSelection="['':message(code:'default.select.choose.label')]"
                                  class="ui search selection dropdown"/>
                    </div>
                </div>
                <div class="three fields">
                    <div class="field">
                        <label for="provider">
                            <g:message code="default.provider.label"/>
                        </label>
                        <g:select name="provider" from="${providers}" value="${params.provider}"
                                  optionKey="${{it.id}}" optionValue="${{it.name}}"
                                  noSelection="['':message(code:'default.select.choose.label')]"
                                  class="ui search selection dropdown"/>
                    </div>
                    <div class="field">
                        <label for="subscriber">
                            <g:message code="default.institution"/>
                        </label>
                        <g:select name="subscriber" from="${subscribers}" value="${params.subscriber}"
                                  optionKey="${{it.id}}" optionValue="${{it.sortname ?: it.name}}"
                                  noSelection="['':message(code:'default.select.choose.label')]"
                                  class="ui search selection dropdown"/>
                    </div>
                    <div class="field la-field-right-aligned">
                        <g:hiddenField name="formSubmit" value="true"/>
                        <a href="${createLink(action:'reporting')}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.submit.label')}">
                    </div>
                </div>
            </g:form>
        </semui:filter>

        <g:if test="${formSubmit}">
        <%-- this is just for that we see something. Micha surely has concrete ideas which cause refactoring. --%>
            <g:if test="${costItems}">
                <table class="ui celled la-table table">
                    <thead>
                        <tr>
                            <th colspan="4"><g:message code="myinst.reporting.costItems"/></th>
                        </tr>
                        <tr>
                            <th><g:message code="financials.costItemElement"/></th>
                            <g:each in="${linkedSubscriptionSet}" var="subscription">
                                <th>${subscription.dropdownNamingConvention(institution)}</th>
                            </g:each>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${costItems}" var="row">
                            <tr>
                                <td>${row.getKey().getI10n("value")}</td>
                                <g:each in="${linkedSubscriptionSet}" var="subscription">
                                    <td>
                                        <g:each in="${row.getValue().findAll { subscription.id in [it.sub.id,it.sub.instanceOf?.id] }}" var="ci">
                                            <ul>
                                                <li>${ci.sub.dropdownNamingConvention(institution)}: <g:formatNumber number="${ci.costInBillingCurrency}" type="currency" currencySymbol=""/> ${ci.billingCurrency ?: 'EUR'}</li>
                                            </ul>
                                        </g:each>
                                    </td>
                                </g:each>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:if>
            <p>
                <h2>Meine Subskriptionen</h2><%-- a placeholder title and a gag for that finally, there is really a page like on the landing page screenshot --%>
            </p>

            <div class="ui top attached segment">
                <div id="chartB"></div>
            </div>

            <div class="ui top attached segment">
                <div id="chartA"></div>
            </div>

            <div class="ui top attached segment">
                <div id="chartC"></div>
            </div>

        </g:if>
        <semui:debugInfo>

        </semui:debugInfo>
    </body>
    <r:script>
        <g:if test="${params.formSubmit}">
            $.ajax({
                url: "<g:createLink action="loadChartData" />",
                data: {
                <g:if test="${params.subscription}">
                    subscription: ${params.subscription}
                </g:if>
                <g:elseif test="${params.package}">
                    package: ${params.package}
                </g:elseif>
                <g:elseif test="${params.provider}">
                    provider: ${params.provider}
                </g:elseif>
                <g:elseif test="${params.subscriber}">
                    subscriber: ${params.subscriber}
                </g:elseif>
                }
            }).done(function(data){
                if(data.graphA) {
                    console.log(data.graphA);
                    new Chartist.Line('#chartA',data.graphA,{
                        axisY: {
                            scaleMinSpace: 15
                        },
                        plugins: [
                            Chartist.plugins.legend()
                        ]
                    });
                }
                if(data.graphB) {
                    console.log(data.graphB);
                    new Chartist.Pie('#chartB',data.graphB,{
                        donut:true,
                        donutWidth: 60,
                        donutSolid:true,
                        startAngle: 270,
                        showLabel: false,
                        height: '300px',
                        plugins: [
                            Chartist.plugins.legend()
                        ]
                    });
                }
                if(data.graphC) {
                    console.log(data.graphC);
                    let benchmark = '<div><h5 class="ui red header">BenchMark</h5><table class="ui celled la-table compact table la-ignore-fixed"><thead><tr><th>Step</th><th>Comment</th><th>(Step_x+1 - Step_x) MS</th></tr></thead><tbody>';
                    let sum = 0;
                    for(let i = 0;i < data.graphC.benchmark.length;i++) {
                        let bm = data.graphC.benchmark[i];
                        benchmark += '<tr><td>'+(i+1)+'</td><td>'+bm[0]+'</td><td>';
                        if (i < data.graphC.benchmark.length - 1) {
                            benchmark += (data.graphC.benchmark[i+1][1] - bm[1])
                        }
                        else {
                            benchmark += '--> ' + ( bm[1] - data.graphC.benchmark[0][1] ) + ' <--'
                        }
                        benchmark += '</td></tr>';
                    }
                    benchmark += '</tbody></table></div>';
                    $('#debugInfo div.content').html(benchmark);
                    new Chartist.Bar('#chartC',data.graphC,{
                        stackBars: true,
                        plugins: [
                            Chartist.plugins.legend()
                        ],
                        height: '500px'
                    }).on('draw', function(data) {
                        if(data.type === 'bar') {
                            data.element.attr({
                                style: 'stroke-width: 30px'
                            });
                        }
                    });
                }
            });
        </g:if>
    </r:script>
</html>
