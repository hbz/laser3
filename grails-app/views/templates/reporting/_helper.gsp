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
                    showTitle: false,
                    orient: 'vertical',
                    itemGap: 15,
                    right: 0,
                    feature: {
                        saveAsImage: {
                            title: '${message(code:'reporting.chart.toolbox.saveAsImage')}',
                            icon: 'image://${resource(dir:'images', file:'reporting/download.svg', absolute:true)}'
                        },
                        myZoomIn: {
                            title: '${message(code:'reporting.chart.toolbox.zoomIn')}',
                            icon: 'image://${resource(dir:'images', file:'reporting/zoom-in.svg', absolute:true)}',
                            onclick: function (){
                                JSPC.app.reporting.current.chart.echart.getModel().getSeries().forEach( function(s) {
                                    var r = parseInt( s.option.radius[1].replace('%','') )
                                    if (r < 90) {
                                        s.option.radius[1] = (r + 5) + '%'
                                        JSPC.app.reporting.current.chart.echart.resize()
                                    }
                                })
                            }
                        },
                        myZoomOut: {
                            title: '${message(code:'reporting.chart.toolbox.zoomOut')}',
                            icon: 'image://${resource(dir:'images', file:'reporting/zoom-out.svg', absolute:true)}',
                            onclick: function (){
                                JSPC.app.reporting.current.chart.echart.getModel().getSeries().forEach( function(s) {
                                    var r = parseInt( s.option.radius[1].replace('%','') )
                                    if (r > 30) {
                                        s.option.radius[1] = (r - 5) + '%'
                                        JSPC.app.reporting.current.chart.echart.resize()
                                    }
                                })
                            }
                        },
                        myLegendToggle: {
                            title: '${message(code:'reporting.chart.toolbox.toggleLegend')}',
                            icon: 'image://${resource(dir:'images', file:'reporting/menu.svg', absolute:true)}',
                            onclick: function (){
                                var show = ! JSPC.app.reporting.current.chart.echart.getOption().legend[0].show
                                JSPC.app.reporting.current.chart.echart.setOption({ legend: {show: show} })
                                JSPC.app.reporting.current.chart.echart.getModel().getSeries().forEach( function(s) {
                                    if (show) {
                                        s.option.center = ['50%', '40%']
                                    } else {
                                        s.option.center = ['50%', '50%']
                                    }
                                })
                                JSPC.app.reporting.current.chart.echart.resize()
                            }
                        },
                    }
                },
            },
            _toolbox: {
                tooltip: {
                    show: true,
                    position: 'left',
                    padding: [2, 10, 5, 10],
                    textStyle: { fontSize: 13 },
                    color: '#383838',
                    backgroundColor: '#ffffff',
                    borderColor: 'rgba(0,0,0,0.2)',
                    borderWidth: 1,
                    shadowBlur: 10,
                    shadowOffsetX: 3,
                    shadowOffsetY: 2,
                    shadowColor: 'rgba(0,0,0,0.1)',
                    formatter: function (param) {
                        return '<div>' + param.title + '</div>'
                    }
                }
            },
            series: {
                _color: {
                    palette: [ '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc' ],
                    redInactiveSolid: '#f2cccd',
                    background: '#f4f8f9',

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
                            padding: [10, 12],
                            backgroundColor: '#ffffff',
                            borderColor: 'rgba(0,0,0,0.2)',
                            borderWidth: 1,
                            borderRadius: 3,
                            shadowBlur: 10,
                            shadowOffsetX: 3,
                            shadowOffsetY: 2,
                            shadowColor: 'rgba(0,0,0,0.1)',
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
                showTitle: false,
                orient: 'vertical',
                right: 0,
                feature: {
                    saveAsImage: {
                        title: '${message(code:'reporting.chart.toolbox.saveAsImage')}',
                        icon: 'image://${resource(dir:'images', file:'reporting/download.svg', absolute:true)}'
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

JSPC.app.reporting.helper._pie.toolbox.tooltip = JSPC.app.reporting.helper._toolbox.tooltip
JSPC.app.reporting.helper.toolbox.tooltip = JSPC.app.reporting.helper._toolbox.tooltip

</laser:script>