<%@ page import="de.laser.storage.RDStore" %>

<div class="ui ${context == 'consAtInst' ? 'five' : 'four'} statistics">
    <div class="statistic stats-toggle" data-target="stats_subscription">
        <span class="value"> ${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id)?.size() ?: 0} </span>
        <span class="label"> ${message(code: 'subscription.plural.current')} </span>
    </div>
    <div class="statistic stats-toggle" data-target="stats_license">
        <span class="value"> ${licenseMap.get(RDStore.LICENSE_CURRENT.id)?.size() ?: 0} </span>
        <span class="label"> ${message(code: 'license.plural.current')} </span>
    </div>
    <div class="statistic stats-toggle" data-target="stats_provider">
        <span class="value"> ${providerMap?.size() ?: 0} </span>
        <span class="label"> ${message(code:'default.provider.label')} </span>
    </div>
    <div class="statistic stats-toggle" data-target="stats_survey">
        <span class="value"> ${surveyMap.get('open')?.size() ?: 0} </span>
        <span class="label"> Offene Umfragen </span>
    </div>
    <g:if test="${context == 'consAtInst'}">
        <div class="statistic stats-toggle" data-target="stats_cost">
            <span class="value"> ${costs.costItems?.size() ?: 0} </span>
            <span class="label"> Kosten (${message(code: 'subscription.plural.current')}) </span>
        </div>
    </g:if>
</div>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.info = {
        chart_config_helper: {
            tooltip_formatter_notNull: function (params, x, callback) {
                let content = ''
                params.forEach(function (e) {
                    if (e.value > 0) {
                        content = content + '<br/>' + e.marker + ' ' + e.seriesName + '&nbsp;&nbsp;&nbsp;<strong style="float:right">' + e.value + '</strong>'
                    }
                })
                return '<div>' + params[0].name + content + '</div>'
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

    let $statsToggle = $('.stats-toggle')

    $statsToggle.on('click', function() {
        $('.stats-content').hide()
        $statsToggle.removeClass('active')

        $(this).addClass('active')
        $('#' + $(this).attr('data-target')).show()

        if (JSPC.app.info && JSPC.app.info.charts) {
            $.each(JSPC.app.info.charts, function(i, e) { e.resize() })
        }
    })

    $statsToggle.first().trigger('click')

</laser:script>

<style>
    .statistics > .stats-toggle.active {
      background-color: rgba(0,0,0, 0.045);
    }
    .statistics > .stats-toggle.active > span {
      color: #1b1c1d !important;
    }

    .statistics > .statistic:hover {
      cursor: pointer;
      background-color: rgba(0,0,0, 0.1);
    }
    .statistics > .statistic > span {
      color: #015591 !important;
    }
    .statistics > .statistic:hover > span {
      color: #1b1c1d !important;
    }

    .stats-content {
      display: none;
    }

    .stats-content .secondary.menu .year {
      float: right;
      margin-left: auto;
      font-size: 120%;
      font-weight: bold;
      color: #222;
    }
    .stats-content .secondary.menu .item {
      padding: 0.6em 0.9em;
      border: 1px solid transparent;
    }
    .stats-content .secondary.menu .item.active {
      border-color: #dedede;
    }
    .stats-content tr.sub {
      background-color: rgba(0,0,0, 0.03)
    }

    .menu > .item.black {
      color: #1b1b1b;
      font-weight: bold;
    }
    h3.header > i.icon {
      vertical-align: baseline !important;
    }
    .ui.table > tfoot > tr > td {
      background-color: #fff;
    }

    .chartWrapper {
      width: 100%;
      min-height: 350px;
      margin-bottom: 35px;
    }
</style>