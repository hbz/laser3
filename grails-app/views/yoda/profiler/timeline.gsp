<%@ page import="de.laser.system.SystemProfiler" %>
<laser:htmlStart message="menu.yoda.profilerTimeline">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.profiler" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.profiler" type="yoda" total="${SystemProfiler.executeQuery('select count(*) from SystemProfiler')[0]}" />

    <nav class="ui secondary menu">
        <g:link controller="yoda" action="profilerLoadtime" class="item">Ladezeiten</g:link>
        <g:link controller="yoda" action="profilerActivity" class="item">Nutzerzahlen</g:link>
        <g:link controller="yoda" action="profilerTimeline" class="item active">Seitenaufrufe</g:link>
        <g:link controller="yoda" action="profilerCurrent" class="item">..</g:link>

        <div style="position:absolute; right:0">
            <div class="ui toggle checkbox">
                <input type="checkbox" id="chartToggle" name="chartToggle">
            </div>

            <laser:script file="${this.getGroovyPageFileName()}">
                JSPC.app.chartToggler = $('#chartToggle').parent()
                JSPC.app.chartToggler.checkbox('check')

                JSPC.app.chartToggler.on('change', function() {
                    if (JSPC.app.chartToggler.checkbox('is checked')) {
                        $('.echarts-wrapper').show()
                    } else {
                        $('.echarts-wrapper').hide()
                    }
                })

                $('table tr[data-cat]').hover(
                    function() {
                        if (JSPC.app.chartToggler.checkbox('is unchecked')) {
                            $('table tr[data-cat=' + $(this).attr('data-cat') + ']').addClass('warning')
                        }
                    },
                    function() {
                        if (JSPC.app.chartToggler.checkbox('is unchecked')) {
                            $('table tr[data-cat]').removeClass('warning')
                        }
                    }
                );
            </laser:script>
        </div>
    </nav>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr class="center aligned">
                <th>Registrierte Seitenaufrufe am Tag</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${globalTimelineOrder}" var="ik,iv" status="index">
                <g:set var="itemValue" value="${globalTimeline[ik]}" />

                <tr data-cat="${ik.split('/')[1]}">
                    <td>
                        <p><strong>${ik}</strong> (${iv})</p>

                        <div id="ct-chart-${index}" class="echarts-wrapper"></div>

                        <laser:script file="${this.getGroovyPageFileName()}">
                            <g:if test="${index == 0}">
                                JSPC.app.chartData_base = {
                                    xAxis: {
                                        type: 'category',
                                        boundaryGap: false,
                                        data: []
                                    },
                                    yAxis: {
                                        type: 'value',
                                        minInterval: 1
                                    },
                                    grid: {
                                        top:20, right:30, bottom:30, left:50
                                    },
                                    tooltip: {
                                        trigger: 'axis'
                                    },
                                    series: [{
                                        data: [],
                                        name: 'Seitenaufrufe',
                                        type: 'line',
                                        smooth: true,
                                        animation: false,
                                        areaStyle: { color: 'rgba(58,111,196, 0.3)' },
                                        lineStyle: { color: 'rgb(58,111,196)', width: 2 },
                                    }]
                                }
                            </g:if>

                            JSPC.app.chartData_${index} = Object.assign({}, JSPC.app.chartData_base);
                            JSPC.app.chartData_${index}.xAxis.data = [<% print '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>];
                            JSPC.app.chartData_${index}.series[0].data = [<% print itemValue.join(', ') %>];

                            echarts.init( $('#ct-chart-${index}')[0] ).setOption( JSPC.app.chartData_${index} );
                        </laser:script>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <style>
    table tbody tr td p {
        margin: 0;
    }
    .echarts-wrapper {
        width: 100%;
        height: 150px;
    }
    </style>

<laser:htmlEnd />