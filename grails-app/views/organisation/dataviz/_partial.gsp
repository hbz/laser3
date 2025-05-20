<%@ page import="de.laser.ui.Icon; de.laser.storage.RDStore" %>

%{--<h2 class="ui small header" style="display:inline-block;background-color:red;color:white;padding:0.5em 1em">DEMO</h2>--}%

<div class="ui vertical secondary fluid menu" style="margin-bottom:2em">
    <a class="ui item stats-toggle" data-target="stats_subscription">
        <span class=""> ${message(code: 'subscription.plural.current')} </span>
        <span class="ui ${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id)?.size() ? 'primary' : ''} label"> ${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id)?.size() ?: 0} </span>
    </a>
    <a class="ui item stats-toggle" data-target="stats_provider">
        <span class="">
            ${message(code:'provider.label')}
            <i class="${Icon.TOOLTIP.HELP} la-popup-tooltip" data-content="${message(code: 'subscription.plural')}, ${message(code: 'package.plural')}, ${message(code: 'platform.plural')}"></i>
        </span>
        <span class="ui ${providerMap?.size() ? 'primary' : ''} label"> ${providerMap?.size() ?: 0} </span>
    </a>
    <a class="ui item stats-toggle" data-target="stats_license">
        <span class=""> ${message(code: 'license.plural.current')} </span>
        <span class="ui ${licenseMap.get(RDStore.LICENSE_CURRENT.id)?.size() ? 'primary' : ''} label"> ${licenseMap.get(RDStore.LICENSE_CURRENT.id)?.size() ?: 0} </span>
    </a>

    <g:if test="${context == 'consAtInst'}">
        <a class="ui item stats-toggle" data-target="stats_survey">
            <span class=""> Offene Umfragen </span>
            <span class="ui ${surveyMap.get('open')?.size() ? 'primary' : ''} label"> ${surveyMap.get('open')?.size() ?: 0} </span>
        </a>
        <a class="ui item stats-toggle" data-target="stats_cost">
            <span class=""> Kosten (${message(code: 'subscription.plural.current')}) </span>
            <span class="ui ${costs.costItems?.size() ? 'primary' : ''} label"> ${costs.costItems?.size() ?: 0} </span>
        </a>
    </g:if>
</div>

%{--    <div class="ui ${context == 'consAtInst' ? 'five' : 'four'} statistics">--}%
%{--    <div class="ui horizontal statistics">--}%
%{--        <div class="statistic stats-toggle" data-target="stats_subscription">--}%
%{--            <span class="value"> ${subscriptionMap.get(RDStore.SUBSCRIPTION_CURRENT.id)?.size() ?: 0} </span>--}%
%{--            <span class="label"> ${message(code: 'subscription.plural.current')} </span>--}%
%{--        </div>--}%
%{--        <div class="statistic stats-toggle" data-target="stats_license">--}%
%{--            <span class="value"> ${licenseMap.get(RDStore.LICENSE_CURRENT.id)?.size() ?: 0} </span>--}%
%{--            <span class="label"> ${message(code: 'license.plural.current')} </span>--}%
%{--        </div>--}%
%{--        <div class="statistic stats-toggle" data-target="stats_provider">--}%
%{--            <span class="value"> ${providerMap?.size() ?: 0} </span>--}%
%{--            <span class="label"> ${message(code:'provider.label')} </span>--}%
%{--        </div>--}%
%{--        <div class="statistic stats-toggle" data-target="stats_survey">--}%
%{--            <span class="value"> ${surveyMap.get('open')?.size() ?: 0} </span>--}%
%{--            <span class="label"> Offene Umfragen </span>--}%
%{--        </div>--}%
%{--        <g:if test="${context == 'consAtInst'}">--}%
%{--            <div class="statistic stats-toggle" data-target="stats_cost">--}%
%{--                <span class="value"> ${costs.costItems?.size() ?: 0} </span>--}%
%{--                <span class="label"> Kosten (${message(code: 'subscription.plural.current')}) </span>--}%
%{--            </div>--}%
%{--        </g:if>--}%
%{--    </div>--}%

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
                return '<div>' + params[0].name + content + '</div>'
            },
            grid: {
                left: '0.5%', right: '0.5%', top: '5%', bottom: '20%'
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

    JSPC.app.dataviz.setCounter = function($e, c) {
        if (c < 1) {
            $e.find('.blue.circular.label').addClass('disabled').text( c )
        } else {
            $e.find('.blue.circular.label').removeClass('disabled').text( c )
        }
    }

    let $statsToggle = $('.stats-toggle')

    $statsToggle.on('click', function() {
        $('.stats-content, .stats-menu').hide()
        $statsToggle.removeClass('active')

        $(this).addClass('active')
        $('.' + $(this).attr('data-target')).show()

        if (JSPC.app.dataviz && JSPC.app.dataviz.charts) {
            $.each(JSPC.app.dataviz.charts, function(i, e) { e.resize() })
        }
    })

    $statsToggle.first().trigger('click') // init

</laser:script>

<style>
    /*.statistics > .stats-toggle.active {*/
    /*  background-color: rgba(0,0,0, 0.045);*/
    /*}*/
    /*.statistics > .stats-toggle.active > span {*/
    /*  color: #1b1c1d !important;*/
    /*}*/

    /*.statistics > .statistic {*/
    /*    display: block;*/
    /*    width: 100%;*/
    /*    margin: 0 !important;*/
    /*    padding: 0.5em 1em;*/
    /*}*/
    /*.statistics > .statistic:hover {*/
    /*  cursor: pointer;*/
    /*  background-color: rgba(0,0,0, 0.1);*/
    /*}*/
    /*.statistics > .statistic > span {*/
    /*  color: #015591 !important;*/
    /*}*/
    /*.statistics > .statistic:hover > span {*/
    /*  color: #1b1c1d !important;*/
    /*}*/

    .stats-content, .stats-menu {
      display: none;
    }

    /*.stats-content .form.segment {*/
    /*    box-shadow: none;*/
    /*}*/
    /*.stats-content .secondary.menu .year {*/
    /*  float: right;*/
    /*  margin-left: auto;*/
    /*  font-size: 120%;*/
    /*  font-weight: bold;*/
    /*  color: #222;*/
    /*}*/
    /*.stats-content .secondary.menu .item {*/
    /*  padding: 0.6em 0.9em;*/
    /*  border: 1px solid transparent;*/
    /*}*/
    /*.stats-content .secondary.menu .item.active {*/
    /*  border-color: #dedede;*/
    /*}*/
    .stats-content tr.sub {
      background-color: rgba(0,0,0, 0.01);
    }

    /*.menu > .item.black {*/
    /*  color: #1b1b1b;*/
    /*  font-weight: bold;*/
    /*}*/
    h3.header > i.icon {
      vertical-align: baseline !important;
    }
    .ui.table > tfoot > tr > td {
      background-color: #fff;
    }

    .chartWrapper {
      width: 100%;
      min-height: 350px;
      margin: 3em 0;
    }
</style>