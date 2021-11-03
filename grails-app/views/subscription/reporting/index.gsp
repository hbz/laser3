<%@ page import="de.laser.reporting.report.ReportingCache; de.laser.reporting.report.myInstitution.base.BaseConfig" %>
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

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
    
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            <semui:xEditable owner="${subscription}" field="name" />
        </h1>

        <semui:anualRings object="${subscription}" controller="subscription" action="reporting" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <g:render template="nav" />

        %{-- <sec:ifAnyGranted roles="ROLE_YODA">
            <g:link controller="yoda" action="cacheInfo" params="${[key: ReportingCache.CTX_SUBSCRIPTION]}" target="_blank" class="ui button small"><i class="icon bug"></i> YODA only CACHE</g:link>
        </sec:ifAnyGranted> --}%

        <g:render template="/templates/reporting/helper" />

        <g:render template="/subscription/reporting/query/query" />

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
                        context: '${BaseConfig.KEY_SUBSCRIPTION}',
                        token: '${token}'
                    }
                    JSPC.app.reporting.requestChartJsonData();
                }
            })

            JSPC.app.reporting.requestChartJsonData = function() {
                if ( JSPC.app.reporting.current.request.query ) {
                    JSPC.app.reporting.current.chart = {};

                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="chart" />",
                        dataType: 'script',
                        method: 'post',
                        data: JSPC.app.reporting.current.request,
                        beforeSend: function (xhr) {
                            $('#query-export-button, #query-help-button').attr('disabled', 'disabled');
                        }
                    })
                    .done( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );

                        if (! JSPC.app.reporting.current.chart.option) {
                            $("#reporting-modal-nodata").modal('show');
                        }
                        else {
                            var dsl = JSPC.app.reporting.current.chart.option.dataset.source.length
                            if (dsl > 11) {
                                $('#chart-wrapper').css('height', 150 + (19 * JSPC.app.reporting.current.chart.option.dataset.source.length) + 'px');
                            } else {
                                $('#chart-wrapper').removeAttr('style');
                            }

                            var echart = echarts.init($('#chart-wrapper')[0]);
                            echart.setOption( JSPC.app.reporting.current.chart.option );

                            echart.on( 'click', function (params) {
                                var clone = Object.assign({}, JSPC.app.reporting.current.request);
                                if (JSPC.helper.contains(params.data[0], ':')) { // workaround XYZ
                                    clone.id  = params.data[0].split(':')[0];
                                    clone.idx = params.data[0];
                                } else {
                                    clone.id = params.data[0];
                                }
                                JSPC.app.reporting.requestChartHtmlDetails(clone);
                            });
                            echart.on( 'legendselectchanged', function (params) { /* console.log(params); */ });

                            JSPC.app.reporting.current.chart.echart = echart;

                            $('#query-export-button').removeAttr('disabled');
                            if (JSPC.app.reporting.current.request.query.indexOf('timeline-') == 0) {
                                $('#query-help-button').removeAttr('disabled');
                            }
                        }
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
