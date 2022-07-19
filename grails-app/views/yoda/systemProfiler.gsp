<%@ page import="de.laser.Org" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.systemProfiler')}</title>
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.profiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top">${message(code:'menu.yoda.profiler')}</h1>

    <nav class="ui secondary menu">
        <g:link controller="yoda" action="systemProfiler" class="item active">Ladezeiten</g:link>
        <g:link controller="yoda" action="activityProfiler" class="item">Nutzerzahlen</g:link>
        <g:link controller="yoda" action="timelineProfiler" class="item">Seitenaufrufe</g:link>

        <div style="position:absolute; right:0">
            <g:select name="archive" id="archive" class="ui dropdown"
                      from="${allArchives}" optionKey="${{it.toString()}}" optionValue="${{it.toString()}}" value="${archive}"/>
            <laser:script file="${this.getGroovyPageFileName()}">
                $('#archive').on('change', function() {
                    var selection = $(this).val()
                    var link = "${g.createLink(absolute: true, controller: 'yoda', action: 'systemProfiler')}?archive=" + selection
                    window.location.href = link
                })
            </laser:script>
        </div>
    </nav>

    <div class="ui secondary stackable pointing tabular la-tab-with-js menu">
        <a data-tab="first" class="item active">Heat</a>
        <a data-tab="second" class="item">Alle</a>
        <a data-tab="third" class="item">URL/Kontext</a>
    </div>

    <div data-tab="first" class="ui bottom attached tab segment active" style="border-top: 1px solid #d4d4d5;">

        <table class="ui celled la-js-responsive-table la-table compact table" id="heatTable">
            <thead>
            <tr>
                <th>Url</th>
                <th>Aufrufe</th>
                <g:each in="${globalMatrixSteps}" var="step" status="i">
                    <g:if test="${i>0}">
                        <th>
                            &lt; ${((int) step / 1000)} s
                        </th>
                    </g:if>
                </g:each>
                <g:if test="${globalMatrixSteps.size()>1}">
                    <th> &gt; ${((int) globalMatrixSteps.last() / 1000)} s</th>
                </g:if>
                <th>avg</th>
                <th><i class="icon fire"></i></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${globalHeatMap}" var="uri, stat">
                <tr data-uri="${uri}">
                    <td data-uri="${uri}">${uri}</td>
                    <td>${stat[3]}</td>
                    <g:each in="${globalMatrix[uri]}" var="border,hits" status="i">
                        <td>
                            <g:if test="${hits > 0}">
                                <g:if test="${i==0}">
                                    <strong class="ui green circular label">${hits}</strong>
                                </g:if>
                                <g:elseif test="${i==1}">
                                    <strong class="ui yellow circular label">${hits}</strong>
                                </g:elseif>
                                <g:elseif test="${i==2}">
                                    <strong class="ui orange circular label">${hits}</strong>
                                </g:elseif>
                                <g:else>
                                    <strong class="ui red circular label">${hits}</strong>
                                </g:else>
                            </g:if>
                            <g:else>
                                ${hits}
                            </g:else>
                        </td>
                    </g:each>
                    <td>
                        <g:set var="avg" value="${((double) stat[2] / 1000).round(2)}" />
                        <g:if test="${avg >= 8}">
                            <span style="color:red"> ${avg} </span>
                        </g:if>
                        <g:elseif test="${avg >= 4}">
                            <span style="color:orange"> ${avg} </span>
                        </g:elseif>
                        <g:else>
                            <span>${avg}</span>
                        </g:else>
                    </td>
                    <td>
                        <g:set var="heat" value="${((double) stat[0]).round(2)}" />
                        <g:if test="${heat >= 3}">
                            <span class="ui circular red label"> ${heat} </span>
                        </g:if>
                        <g:elseif test="${heat >= 2}">
                            <span class="ui circular orange label"> ${heat} </span>
                        </g:elseif>
                        <g:elseif test="${heat >= 1}">
                            <span class="ui circular yellow label"> ${heat} </span>
                        </g:elseif>
                        <g:else>
                            <span class="ui circular label"> ${heat} </span>
                        </g:else>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </div>

    <div data-tab="second" class="ui bottom attached tab segment" style="border-top: 1px solid #d4d4d5;">

        <table class="ui celled la-js-responsive-table la-table compact table" id="globalTable">
            <thead>
                <tr>
                    <th>Url</th>
                    <th>Aufrufe</th>
                    <g:each in="${globalMatrixSteps}" var="step" status="i">
                        <g:if test="${i>0}">
                            <th>
                                &lt; ${((int) step / 1000)} s
                            </th>
                        </g:if>
                    </g:each>
                    <g:if test="${globalMatrixSteps.size()>1}">
                        <th> &gt; ${((int) globalMatrixSteps.last() / 1000)} s</th>
                    </g:if>
                    <th>avg</th>
                    <th>max</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${globalStats}" var="stat">
                    <tr data-uri="${stat[0]}">
                        <td data-uri="${stat[0]}">${stat[0]}</td>
                        <td>${stat[3]}</td>
                        <g:each in="${globalMatrix[stat[0]]}" var="border,hits" status="i">
                            <td>
                                <g:if test="${hits > 0}">
                                    <g:if test="${i==0}">
                                        <strong class="ui green circular label">${hits}</strong>
                                    </g:if>
                                    <g:elseif test="${i==1}">
                                        <strong class="ui yellow circular label">${hits}</strong>
                                    </g:elseif>
                                    <g:elseif test="${i==2}">
                                        <strong class="ui orange circular label">${hits}</strong>
                                    </g:elseif>
                                    <g:else>
                                        <strong class="ui red circular label">${hits}</strong>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    ${hits}
                                </g:else>
                            </td>
                        </g:each>
                        <td>
                            <g:set var="avg" value="${((double) stat[2] / 1000).round(2)}" />
                            <g:if test="${avg >= 8}">
                                <strong style="color:red"> ${avg} </strong>
                            </g:if>
                            <g:elseif test="${avg >= 4}">
                                <strong style="color:orange"> ${avg} </strong>
                            </g:elseif>
                            <g:else>
                                <span>${avg}</span>
                            </g:else>
                        </td>
                        <td>${((double) stat[1] / 1000).round(2)}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div>

    <div data-tab="third" class="ui bottom attached tab segment" style="border-top: 1px solid #d4d4d5;">

        <div class="ui form">
            <div class="three fields">
                <div class="field">
                    <g:select id="filterTableUri" name="filterTableUri" class="ui dropdown search selection"
                              from="${contextStats.collect{it[0]}.unique().sort()}"
                              optionKey="${{it}}" optionValue="${{it}}"
                              noSelection="['':'Alle anzeigen']"
                    />
                </div>
                <div class="field">
                    <g:select id="filterTableCtx" name="filterTableCtx" class="ui dropdown search selection"
                              from="${contextStats.collect{Org.get(it[3])}.unique()}"
                              optionKey="id" optionValue="${{it.sortname + ' (' + it.shortname + ')'}}"
                              noSelection="['':'Alle anzeigen']"
                    />
                </div>
            </div>
        </div>
        <table class="ui celled la-js-responsive-table la-table compact table" id="contextTable">
            <thead>
                <tr>
                    <th>Url</th>
                    <th>Kontext</th>
                    <th>Aufrufe</th>
                    <th>avg</th>
                    <th>max</th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${contextStats}" var="bench">
                <tr data-uri="${bench[0]}" data-context="${bench[3]}">
                    <td data-uri="${bench[0]}">${bench[0]}</td>
                    <td data-context="${bench[3]}">${Org.get(bench[3]).getDesignation()}</td>
                    <td>${bench[4]}</td>
                    <td>
                        <g:set var="avg" value="${((double) bench[2] / 1000).round(2)}" />
                        <g:if test="${avg >= 8}">
                            <strong style="color:red"> ${avg} </strong>
                        </g:if>
                        <g:elseif test="${avg >= 4}">
                            <strong style="color:orange"> ${avg} </strong>
                        </g:elseif>
                        <g:else>
                            <span>${avg}</span>
                        </g:else>
                    </td>
                    <td>${((double) bench[1] / 1000).round(2)}</td>
                </tr>
                </g:each>
            </tbody>
        </table>
    </div>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#filterTableUri').dropdown().dropdown({
        clearable: true,
        onChange: function(value, text, $selectedItem){
            if (text) {
                $('#filterTableCtx').dropdown('clear')
                $('#contextTable > tbody > tr').addClass('hidden')
                $('#contextTable > tbody > tr[data-uri="' + value + '"]').removeClass('hidden')
            }
            else {
                $('#filterTableUri').dropdown('clear')
                $('#contextTable > tbody > tr').removeClass('hidden')
            }
        }
    })
    $('#filterTableCtx').dropdown().dropdown({
        clearable: true,
        onChange: function(value, text, $selectedItem){
            if (text) {
                $('#filterTableUri').dropdown('clear')
                $('#contextTable > tbody > tr').addClass('hidden')
                $('#contextTable > tbody > tr[data-context="' + value + '"]').removeClass('hidden')
           }
           else {
               $('#filterTableCtx').dropdown('clear')
               $('#contextTable > tbody > tr').removeClass('hidden')
           }
        }
    })

    $('.table tr').hover(
        function(){ $(this).addClass('trHover') },
        function(){ $(this).removeClass('trHover') }
    )
</laser:script>

<style>
    table tr.trHover td {
        background-color:#E1F2B6 !important;
    }
    table tr.trHover td[data-uri]:hover,
    table tr.trHover td[data-context]:hover {
        cursor: pointer;
    }
</style>

</body>
</html>