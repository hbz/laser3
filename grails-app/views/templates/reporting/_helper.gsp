<laser:script file="${this.getGroovyPageFileName()}">

if (! JSPC.app.reporting) {
    JSPC.app.reporting = {
        current: {
            chart: {}
        },
        helper: {
            _pie: {
                legend: {
                    align:  'left',
                    orient: 'vertical',
                    type:   'scroll',
                    left:   'left'
                }
            },
            series: {
                _color: {
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
                            borderColor: 'rgba(0,0,0,0.2)',
                            borderWidth: 1,
                            borderRadius: 3
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
                feature: {
                    saveAsImage: {
                        title: 'Exportieren'
                    },
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