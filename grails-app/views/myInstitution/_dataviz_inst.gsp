<%@ page import="de.laser.InfoService$Helper; de.laser.ui.Icon; de.laser.ui.EChart; java.time.Year; de.laser.RefdataValue; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.Org; de.laser.Subscription" %>

<laser:javascript src="echarts.js"/>%{-- dont move --}%

<%
    def (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([
            org: contextService.getOrg(),
            status: 'FETCH_ALL'
    ])

    // println base_qry; println qry_params

    List<List> subStruct = Subscription.executeQuery('select s.status.id, s.id, s.startDate, s.endDate, s.isMultiYear, s.referenceYear ' + base_qry, qry_params)
//    Map subscriptionMap = InfoService$Helper.reduceMap(InfoService$Helper.listToMap(subStruct))
    Map subscriptionTimelineMap = InfoService$Helper.getTimelineMap(subStruct)
    Map subscriptionMap = InfoService$Helper.getTimelineCatsMap(subscriptionTimelineMap)
%>

%{--${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id)?.size() ?: 0}--}%

<g:if test="${subscriptionMap}">

    <div class="ui fluid card">
%{--        <div class="content">--}%
%{--            <div class="header">Meine Lizenzen</div>--}%
%{--        </div>--}%
        <div class="ui top attached label">
            ${message(code: 'myinst.currentSubscriptions.label')}
        </div>
        <div class="content">
            <div class="ui grid">
                <div class="one wide column"></div>
                <div class="fourteen wide column">
                    <div class="chartWrapper" id="cw-subscription"></div>
                </div>
                <div class="one wide column"></div>
            </div><!-- .grid -->
        </div>
        <div class="extra content">
            <div class="right floated">
                <g:link controller="organisation" action="dataviz" id="${contextService.getOrg().id}">
                    %{--<i class="${Icon.DATA_DASHBOARD} circular"></i>--}% Daten-Dashboard
                </g:link>
            </div>
        </div>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.dataviz = {
            chart_config_helper: {
                tooltip_formatter_notNull: function (params, x, callback) {
                    let content = ''
                    params.forEach(function (e) {
                        if (e.value > 0) {
                            content = content + '<br/>' + e.marker + ' ' + e.seriesName + '&nbsp;&nbsp;&nbsp;<strong style="float:right">' + e.value + '</strong>'
                        }
                    })
                    return '<div><strong>' + params[0].name + '</strong>' + content + '</div>'
                },
                grid: {
                    left: '0.5%', right: '0.5%', top: '0.5%', bottom: '0.5%'
                },
                series_lineStyle:  {
                    type: 'solid',
                    width: 2,
                    shadowColor: 'rgba(0,0,0, 0.3)',
                    shadowBlur: 10,
                    shadowOffsetY: 8
                },
            }
        }

        JSPC.app.dataviz.chart_config_helper.tooltip = {
            trigger:        'axis',
            axisPointer:    { type: 'shadow' },
            formatter:      JSPC.app.dataviz.chart_config_helper.tooltip_formatter_notNull
        }

        JSPC.app.dataviz.chart_config = {
            subscription: {
                tooltip: JSPC.app.dataviz.chart_config_helper.tooltip,
                series: [
                    <g:each in="${subscriptionTimelineMap.values().collect{ it.keySet() }.flatten().unique().sort{ RefdataValue.get(it).getI10n('value') }}" var="status">
                        {
                            name    : '${RefdataValue.get(status).getI10n('value')}',
                            type    : 'bar',
                            stack   : 'total',
                            animation : false,
                            silent  : true,
                            data    : [${subscriptionTimelineMap.values().collect{ it[status] ? it[status].size() : 0 }.join(', ')}],
                            raw     : [${subscriptionTimelineMap.values().collect{ it[status] ?: [] }.join(', ')}],
                            color   : ${EChart.getJspcColorBySubscriptionStatus(RefdataValue.get(status))}
                        },
                    </g:each>
                        {
                            name    : '${message(code: 'subscription.isMultiYear.label')}',
                            type    : 'line',
                            smooth  : true,
                            lineStyle : JSPC.app.dataviz.chart_config_helper.series_lineStyle,
                            animation : false,
                            silent  : true,
                            data    : [<%
                                        List<Long> subsPerYear = subscriptionTimelineMap.values().collect{ it.values().flatten() }
                                        print subsPerYear.collect {
                                            it.collect{ Subscription.get(it).isMultiYear ? 1 : 0 }.sum() ?: 0
                                        }.join(', ')
                                        %>],
                            color   : JSPC.colors.hex.pink
                        },
                        {
                            name    : '${RDStore.SUBSCRIPTION_TYPE_LOCAL.getI10n('value')}',
                            type    : 'line',
                            smooth  : true,
                            lineStyle : JSPC.app.dataviz.chart_config_helper.series_lineStyle,
                            animation : false,
                            silent  : true,
                            data    : [<%
                                print subsPerYear.collect {
                                    it.collect{ Subscription.get(it).type == RDStore.SUBSCRIPTION_TYPE_LOCAL ? 1 : 0 }.sum() ?: 0
                                }.join(', ')
                            %>],
                            color   : JSPC.colors.hex.ice
                        },
                ],
                xAxis: {
                    type: 'category',
                    data: [${subscriptionTimelineMap.keySet().join(', ')}]
                },
                yAxis:  { type: 'value' },
                legend: {
                    orient: 'vertical',
                    top: 'center',
                    left: 'right',
                    selectedMode: false,
                    type: 'scroll'
                },
                grid:   { left: '0.5%', right: '200px', top: '5%', bottom: '20%' }
            },
        };

        JSPC.app.dataviz.charts = {
            subscription : echarts.init ($('#cw-subscription')[0])
        }

        JSPC.app.dataviz.charts.subscription.setOption (JSPC.app.dataviz.chart_config.subscription);

        $(window).resize(function () {
            JSPC.app.dataviz.charts.subscription.resize();
        });
        $(window).trigger('resize');
    </laser:script>

    <style>
        .chartWrapper {
            width: 100%;
            min-height: 200px;
            margin: 1em 0 -1em;
        }
    </style>

</g:if>