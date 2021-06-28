<laser:script file="${this.getGroovyPageFileName()}">

if (! JSPC.app.reporting) {
    JSPC.app.reporting = {
        current: {
            chart: {}
        },
        helper: {
            series: {
                color: {
                    red:    'rgb(238,102,102)',
                    green:  'rgb(144,202,117)',
                    blue:   'rgb(58,111,196)',
                    ice:    'rgb(115,192,222)',
                    redInactive:    'rgba(238,102,102, 0.3)',
                    greenInactive:  'rgba(144,202,117, 0.3)',
                    blueInactive:   'rgba(58,111,196, 0.3)',
                    iceInactive:    'rgba(115,192,222, 0.3)',
                },
                pie: {
                    emphasis: {
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
                                return JSPC.app.reporting.helper.series.color[color]
                            }
                            return JSPC.app.reporting.helper.series.color[color + 'Inactive']
                        }
                    }
                }
            },
            tooltip: {
                getEntry: function(marker, text, value) {
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