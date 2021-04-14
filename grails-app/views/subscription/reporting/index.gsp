<%@ page import="de.laser.reporting.myInstitution.SubscriptionConfig" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : <g:message code="myinst.reporting"/></title>
        <laser:javascript src="echarts.js"/>%{-- dont move --}%
    </head>

    <body>
        <g:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            <semui:xEditable owner="${subscription}" field="name" />
        </h1>

        <semui:anualRings object="${subscription}" controller="subscription" action="reporting" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <g:render template="nav" />

        <g:render template="/templates/reporting/helper" />

        <g:render template="/subscription/reporting/query" />

        <div id="chart-wrapper"></div>
        <div id="chart-details"></div>

        <style>
            #chart-wrapper { height: 360px; width: 98%; margin: 2em auto 1em; }
        </style>

        <laser:script file="${this.getGroovyPageFileName()}">
            $('*[id^=query-chooser').on( 'change', function(e) {
                var value = $(e.target).dropdown('get value');
                if (value) {
                    $('*[id^=query-chooser').not($('#' + e.target.id)).dropdown('clear');
                    JSPC.app.reporting.current.request = {
                        id: ${subscription.id},
                        query: value,
                        context: '${SubscriptionConfig.KEY}',
                        chart: 'default'
                    }
                    JSPC.app.reporting.requestChartJsonData();
                }
            })

            JSPC.app.reporting.requestChartJsonData = function() {
                if ( JSPC.app.reporting.current.request.query && JSPC.app.reporting.current.request.chart ) {
                    JSPC.app.reporting.current.chart = {};

                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="chart" />",
                        dataType: 'script',
                        method: 'post',
                        data: JSPC.app.reporting.current.request,
                        beforeSend: function (xhr) {
                            //$('#chart-export').attr('disabled', 'disabled');
                        }
                    })
                    .done( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );

                        var echart = echarts.init($('#chart-wrapper')[0]);
                        echart.setOption( JSPC.app.reporting.current.chart.option );

                        echart.on( 'click', function (params) {
                            var valid = false;
                            $.each( JSPC.app.reporting.current.chart.details, function(i, v) {
                                if (params.data[0] == v.id) {
                                    valid = true;
                                    var clone = Object.assign({}, v); // ---- TODO !!!! ----
                                    if (JSPC.helper.contains(clone.id, ':')) { // workaround XYZ
                                        clone.id = clone.id.split(':')[0]
                                    }
                                    clone.context = '${SubscriptionConfig.KEY}';
                                    JSPC.app.reporting.requestChartHtmlDetails(clone);
                                }
                            })
                            if (! valid) {
                                $("#reporting-modal-nodetails").modal('show');
                            }
                        });
                        echart.on( 'legendselectchanged', function (params) { /* console.log(params); */ });

                        JSPC.app.reporting.current.chart.echart = echart;
                        //$('#chart-export').removeAttr('disabled');
                    })
                    .fail( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );
                    })
                }
            }
        </laser:script>

        <semui:modal id="reporting-modal-error" text="REPORTING" hideSubmitButton="true">
            <p>${message(code:'reporting.modal.error')}</p>
        </semui:modal>
        <semui:modal id="reporting-modal-nodata" text="REPORTING" hideSubmitButton="true">
            <p>${message(code:'reporting.modal.nodata')}</p>
        </semui:modal>
        <semui:modal id="reporting-modal-nodetails" text="REPORTING" hideSubmitButton="true">
            <p>${message(code:'reporting.modal.nodetails')}</p>
        </semui:modal>

</body>
</html>
