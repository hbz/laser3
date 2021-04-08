<laser:script file="${this.getGroovyPageFileName()}">

if (! JSPC.app.reporting) {
    JSPC.app.reporting = {
        current: {
            chart: {}
        },
        helper: {
            series: {
                color: {
                    red:   'rgb(238,102,102)',
                    green: 'rgb(144,202,117)',
                    blue:  'rgb(58,111,196)',
                    redInactive:   'rgba(238,102,102, 0.3)',
                    greenInactive: 'rgba(144,202,117, 0.3)',
                    blueInactive: 'rgba(58,111,196, 0.3)',
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
            }
        },
        requestChartHtmlDetails: function(request, data) {
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="chartDetails" />",
                method: 'post',
                data: data,
                beforeSend: function(xhr) {
                   $('#chartDetailsExportModal').remove()
                   $('#chartDetailsCopyEmailModal').remove()
                }
            })
            .done( function (data) {
                $('#chart-details').empty().html(data)
                r2d2.initDynamicSemuiStuff('#chart-details')
            })
            .fail( function (data) {
                $("#reporting-modal-error").modal('show')
            })
        }
    }
}

</laser:script>