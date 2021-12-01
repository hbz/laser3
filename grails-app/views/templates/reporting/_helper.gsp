<laser:script file="${this.getGroovyPageFileName()}">

if (! JSPC.app.reporting) {
    JSPC.app.reporting = {
        current: {
            chart: {}
        },
        helper: {
            _pie: {
                /* legend: {
                    align:  'left',
                    orient: 'vertical',
                    type:   'scroll',
                    left:   'left'
                } */
                legend: {
                    bottom: 0,
                    left: 'center',
                    z: 1
                },
                toolbox: {
                    showTitle: true,
                    orient: 'vertical',
                    right: 0,
                    feature: {
                        saveAsImage: {
                            title: '${message(code:'reporting.chart.toolbox.saveAsImage')}'
                        },
                        myLegendToggle: {
                            title: '${message(code:'reporting.chart.toolbox.toggleLegend')}',
                            icon: 'path://M28,0C12.561,0,0,12.561,0,28s12.561,28,28,28s28-12.561,28-28S43.439,0,28,0z M40,41H16c-1.104,0-2-0.896-2-2s0.896-2,2-2 h24c1.104,0,2,0.896,2,2S41.104,41,40,41z M40,30H16c-1.104,0-2-0.896-2-2s0.896-2,2-2h24c1.104,0,2,0.896,2,2S41.104,30,40,30z M40,19H16c-1.104,0-2-0.896-2-2s0.896-2,2-2h24c1.104,0,2,0.896,2,2S41.104,19,40,19z',
                            /* icon: 'path://M4,10h24c1.104,0,2-0.896,2-2s-0.896-2-2-2H4C2.896,6,2,6.896,2,8S2.896,10,4,10z M28,14H4c-1.104,0-2,0.896-2,2  s0.896,2,2,2h24c1.104,0,2-0.896,2-2S29.104,14,28,14z M28,22H4c-1.104,0-2,0.896-2,2s0.896,2,2,2h24c1.104,0,2-0.896,2-2  S29.104,22,28,22z', */
                            onclick: function (){
                                JSPC.app.reporting.current.chart.echart.setOption({ legend: {show: ! JSPC.app.reporting.current.chart.echart.getOption().legend[0].show} })
                            }
                        }
                    }
                },
            },
            series: {
                _color: {
                    palette: [ '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc' ],
                    redInactiveSolid: '#f2cccd',

                    red:    'rgb(238,102,102)',
                    green:  'rgb(144,202,117)',
                    blue:   'rgb(58,111,196)',
                    ice:    'rgb(115,192,222)',
                    yellow: 'rgb(250,200,88)',
                    orange: 'rgb(255,165,0)',
                    purple: 'rgb(154,96,180)',
                    redInactive:    'rgba(238,102,102, 0.3)',
                    greenInactive:  'rgba(144,202,117, 0.3)',
                    blueInactive:   'rgba(58,111,196, 0.3)',
                    iceInactive:    'rgba(115,192,222, 0.3)',
                    yellowInactive: 'rgba(250,200,88, 0.3)',
                    orangeInactive: 'rgba(255,165,0, 0.3)',
                    purpleInactive: 'rgba(154,96,180, 0.3)'
                },
                _pie: {
                    emphasis: {
                        label: {
                            padding: [10, 10, 7, 10],
                            backgroundColor: '#f4f8f9',
                            borderColor: 'rgba(0,0,0,0.3)',
                            borderWidth: 1,
                            borderRadius: 3,
                            z: 100
                        },
                        itemStyle: {
                            shadowBlur: 7,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0,0,0,0.4)'
                        }
                    }
                },
                bar: {
                    itemStyle: {
                        color: function(color, active) {
                            if (active) {
                                return JSPC.app.reporting.helper.series._color[color]
                            }
                            return JSPC.app.reporting.helper.series._color[color + 'Inactive']
                        }
                    }
                }
            },
            tooltip: {
                getEntry: function(marker, text, value) {
                    if (!marker) {
                        marker = '<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:#d3dae3;"></span>'
                    }
                    return '<br/>' + marker + ' ' + text + '&nbsp;&nbsp;&nbsp;<strong style="float:right">' + value + '</strong>'
                }
            },
            toolbox: {
                showTitle: true,
                orient: 'vertical',
                right: 0,
                feature: {
                    saveAsImage: {
                        title: 'Exportieren'
                    }
                }
            },
        },
        requestChartHtmlDetails: function(data) {
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="chartDetails" />",
                method: 'post',
                data: data,
                beforeSend: function(xhr) {
                   $('#detailsExportModal').remove()
                   $('#detailsCopyEmailModal').remove()
                   $('#loadingIndicator').show()
                }
            })
            .done( function (data) {
                $('#chart-details').empty().html(data)
                r2d2.initDynamicSemuiStuff('#chart-details')
            })
            .fail( function (data) {
                $("#reporting-modal-error").modal('show')
            })
            .always(function() {
                $('#loadingIndicator').hide()
            });
        }
    }
}

</laser:script>