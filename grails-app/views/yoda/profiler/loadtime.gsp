<%@ page import="de.laser.system.SystemProfiler; de.laser.Org" %>
<laser:htmlStart message="menu.yoda.profilerLoadtime">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.profiler" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.profiler" type="yoda" total="${SystemProfiler.executeQuery('select count(*) from SystemProfiler')[0]}" />

    <nav class="ui secondary stackable menu">
        <g:render template="profiler/menu" model="${[hideWrapper: true]}"/>

        <div style="position:absolute; right:0">
            <g:select name="archive" id="archive" class="ui dropdown clearable"
                      from="${allArchives}" optionKey="${{it[0].toString()}}" optionValue="${{it[0].toString() + ' (' + it[1].toString() + ')'}}" value="${archive}"/>
            <laser:script file="${this.getGroovyPageFileName()}">
                $('#archive').on('change', function() {
                    let selection = $(this).val()
                    let link = "${g.createLink(absolute: true, controller: 'yoda', action: 'profilerLoadtime')}?archive=" + selection
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

<div data-tab="first" class="ui bottom attached tab fluid card active">
    <div class="content">
        <table class="ui celled la-js-responsive-table la-table la-hover-table compact table" id="heatTable">
            <thead>
            <tr>
                <th>Url</th>
                <th>Aufrufe</th>
                <g:each in="${globalMatrixSteps}" var="step" status="i">
                    <g:if test="${i>0}">
                        <th> &lt; ${((int) step / 1000)} s </th>
                    </g:if>
                </g:each>
                <g:if test="${globalMatrixSteps.size()>1}">
                    <th> &gt; ${((int) globalMatrixSteps.last() / 1000)} s </th>
                </g:if>
                <th>avg</th>
                <th class="center aligned">score</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${globalHeatMap}" var="uri, stat">
                <g:set var="avg" value="${((double) stat[2] / 1000).round(2)}" />
                <g:set var="heat" value="${((double) stat[0]).round(2)}" />

                <tr data-uri="${uri}" class="${avg >= 8 ? 'error' : avg >= 4 ? 'warning' : ''}">
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
                        <g:if test="${avg >= 8}">
                            <strong class="sc_red"> ${avg} </strong>
                        </g:if>
                        <g:elseif test="${avg >= 4}">
                            <strong class="sc_orange"> ${avg} </strong>
                        </g:elseif>
                        <g:else>
                            <span>${avg}</span>
                        </g:else>
                    </td>
                    <td class="center aligned">
                        <g:if test="${heat >= 3}">
                            <span class="ui circular red mini label"> ${heat} </span>
                        </g:if>
                        <g:elseif test="${heat >= 2}">
                            <span class="ui circular orange mini label"> ${heat} </span>
                        </g:elseif>
                        <g:elseif test="${heat >= 1}">
                            <span class="ui circular yellow mini label"> ${heat} </span>
                        </g:elseif>
                        <g:else>
                            <span class="ui circular mini label"> ${heat} </span>
                        </g:else>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>

<div data-tab="second" class="ui bottom attached tab fluid card">
    <div class="content">
        <table class="ui celled la-js-responsive-table la-table la-hover-table compact table" id="globalTable">
            <thead>
                <tr>
                    <th>Url</th>
                    <th>Aufrufe</th>
                    <g:each in="${globalMatrixSteps}" var="step" status="i">
                        <g:if test="${i>0}">
                            <th> &lt; ${((int) step / 1000)} s </th>
                        </g:if>
                    </g:each>
                    <g:if test="${globalMatrixSteps.size()>1}">
                        <th> &gt; ${((int) globalMatrixSteps.last() / 1000)} s </th>
                    </g:if>
                    <th>avg</th>
                    <th>max</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${globalStats}" var="stat">
                    <g:set var="avg" value="${((double) stat[2] / 1000).round(2)}" />

                    <tr data-uri="${stat[0]}" class="${avg >= 8 ? 'error' : avg >= 4 ? 'warning' : ''}">
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
                            <g:if test="${avg >= 8}">
                                <strong class="sc_red"> ${avg} </strong>
                            </g:if>
                            <g:elseif test="${avg >= 4}">
                                <strong class="sc_orange"> ${avg} </strong>
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
</div>

<div data-tab="third" class="ui bottom attached tab fluid card">
    <div class="content">
        <div class="ui form">
            <div class="three fields">
                <div class="field">
                    <g:select id="filterTableUri" name="filterTableUri" class="ui dropdown clearable search selection"
                              from="${contextStats.collect{it[0]}.unique().sort()}"
                              optionKey="${{it}}" optionValue="${{it}}"
                              noSelection="['':'Alle anzeigen']"
                    />
                </div>
                <div class="field">
                    <g:select id="filterTableCtx" name="filterTableCtx" class="ui dropdown clearable search selection"
                              from="${contextStats.collect{Org.get(it[3])}.unique()}"
                              optionKey="id" optionValue="${{it.sortname}}"
                              noSelection="['':'Alle anzeigen']"
                    />
                </div>
            </div>
        </div>
        <table class="ui celled la-js-responsive-table la-table la-hover-table compact table" id="contextTable">
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
                <g:set var="avg" value="${((double) bench[2] / 1000).round(2)}" />

                <tr data-uri="${bench[0]}" data-context="${bench[3]}" class="${avg >= 8 ? 'error' : avg >= 4 ? 'warning' : ''}">
                    <td data-uri="${bench[0]}">${bench[0]}</td>
                    <td data-context="${bench[3]}">${Org.get(bench[3]).getDesignation()}</td>
                    <td>${bench[4]}</td>
                    <td>
                        <g:if test="${avg >= 8}">
                            <strong class="sc_red"> ${avg} </strong>
                        </g:if>
                        <g:elseif test="${avg >= 4}">
                            <strong class="sc_orange"> ${avg} </strong>
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

<laser:htmlEnd />