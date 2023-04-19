<%@ page import="de.laser.utils.DateUtils; de.laser.system.SystemEvent;" %>

<laser:htmlStart message="menu.admin.systemEvents">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.systemEvents" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.systemEvents" total="${events.size()}" type="admin"/>

<br />
<br />

<div class="ui message info">Die letzten ${limit} Tage ..</div>

<br />

<div class="chartWrapper" id="chartWrapper" style="width:100%; min-height:500px"></div>

<br />
<br />

<table class="ui celled la-js-responsive-table la-table table very compact">
    <thead>
        <tr>
            <th class="one wide">Datum</th>
            <th class="one wide">Uhrzeit</th>
            <th class="two wide">Host</th>
            <th class="five wide">URL</th>
            <th class="two wide">Remote</th>
            <th class="five wide">User-Agent</th>
        </tr>
    </thead>
    <tbody>
    <%
        int gc = 0
        String lastRemote = ''
    %>
    <g:each in="${data}" var="d">
        <tr data-group="${lastRemote != d.remote ? ++gc : gc}" class="${gc%2 == 0 ? 'custom-grey' : 'custom-white'}">
            <g:set var="lastRemote" value="${d.remote}" />
            <td data-date="${d.x_date}">
                <span  class="la-popup-tooltip la-delay" data-content="${d.id}" data-position="top right">
                    ${d.x_date}
                </span>
            </td>
            <td>${d.x_time}</td>
            <td>
                <g:if test="${d.host.startsWith('host: ')}">
                    <i class="server icon green"></i> ${d.host.replaceFirst('host: ', '')}
                </g:if>
                <g:elseif test="${d.host.startsWith('cookie: ')}">
                    <span  class="la-popup-tooltip la-delay" data-content="${d.host.replaceFirst('cookie: ', '')}" data-position="top right">
                        <i class="cookie bite icon orange"></i>
                    </span>
                </g:elseif>
                <g:else>
                    ${d.host}
                </g:else>
            </td>
            <td>${d.url}</td>
            <td data-remote="${d.remote}">
                <i class="laptop icon"></i> ${d.remote}
            </td>
            <td>
                <g:if test="${d.useragent.startsWith('user-agent: ')}">
                    <strong>user-agent:</strong> ${d.useragent.replaceFirst('user-agent: ', '')}
                </g:if>
                <g:elseif test="${d.useragent.startsWith('referer: ')}">
                    <span class="ui blue text"><strong>referer:</strong></span> ${d.useragent.replaceFirst('referer: ', '')}
                </g:elseif>
                <g:elseif test="${d.useragent.startsWith('host: ')}">
                    <span class="ui orange text"><strong>host:</strong></span> ${d.useragent.replaceFirst('host: ', '')}
                </g:elseif>
                <g:else>
                    ${d.useragent}
                </g:else>
            </td>
        </tr>
        <% lastRemote = d.remote %>
    </g:each>
    </tbody>
</table>

<style>
    table tbody tr.custom-white   { background: rgba(255,255,255, .03); }
    table tbody tr.custom-grey    { background: rgba(0,0,0, .03);}
</style>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.selw = {
        config: {
            tooltip: {
                trigger: 'item'
            },
            series: [
                {
                    type: 'pie',
                    radius: '60%',
                    center: ['25%', '50%'],
                    data: [ ]
                },
                {
                    type: 'bar',
                    color: '#5470c6',
                    data: [ ]
                },
            ],
            xAxis: {
                type: 'category',
                axisLabel: {
                    formatter: function(id, idx) {
                        return JSPC.app.selw.config.series[1].data[idx].name
                    }
                },
            },
            yAxis: { gridIndex: 0 },
            grid:  { left: '50%', right: '5%', top: '5%', bottom: '10%' },
        }
    };

let calc_remote = $('td[data-remote]').map (function () { return $(this).attr('data-remote') }).toArray();
let calc_remote_counts = calc_remote.reduce (function (all, curr) {
  const currCount = all[curr] ?? 0;
  return {
    ...all,
    [curr]: currCount + 1,
  };
}, {});
for (let key in calc_remote_counts) {
    JSPC.app.selw.config.series[0].data.push ({ name: key, value: calc_remote_counts[key] })
}

let calc_date = $('td[data-date]').map (function () { return $(this).attr('data-date') }).toArray().reverse();
let calc_date_counts = calc_date.reduce (function (all, curr) {
  const currCount = all[curr] ?? 0;
  return {
    ...all,
    [curr]: currCount + 1,
  };
}, {});
for (let key in calc_date_counts) {
    JSPC.app.selw.config.series[1].data.push ({ name: key, value: calc_date_counts[key] })
}

let echart = echarts.init ($('#chartWrapper')[0]);
echart.setOption (JSPC.app.selw.config);
</laser:script>

<laser:htmlEnd />
