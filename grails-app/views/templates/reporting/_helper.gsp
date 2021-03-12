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
                    green: 'rgb(58,111,196)',
                    blue:  'rgb(144,202,117)',
                    redInactive:   'rgba(238,102,102, 0.3)',
                    greenInactive: 'rgba(58,111,196, 0.3)',
                    blueInactive:  'rgba(144,202,117, 0.3)',
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
            }
        }
    }
}

</laser:script>