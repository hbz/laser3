<%@ page import="de.laser.Org" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.systemProfiler')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.systemProfiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.systemProfiler')}</h1>

    <div class="ui la-float-right">
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

    <div class="ui secondary pointing tabular menu">
        <a data-tab="first" class="item active">Global</a>
        <a data-tab="second" class="item">Kontextbezogen</a>
    </div>

    <div data-tab="first" class="ui bottom attached tab segment active" style="border-top: 1px solid #d4d4d5;">

        <table class="ui celled la-table compact table" id="globalTable">
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
                    <th>max</th>
                    <th>avg</th>
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
                        <td>${((double) stat[1] / 1000).round(2)}</td>
                        <td>${((double) stat[2] / 1000).round(2)}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div>
    <div data-tab="second" class="ui bottom attached tab segment" style="border-top: 1px solid #d4d4d5;">

        <g:select id="filterTable" name="filterTable" class="ui dropdown search"
                  from="${contextStats.collect{Org.get(it[3])}.unique()}"
                  optionKey="id" optionValue="${{it.sortname + ' (' + it.shortname + ')'}}"
                  noSelection="['':'Alle anzeigen']"
        />

        <table class="ui celled la-table compact table" id="contextTable">
            <thead>
                <tr>
                    <th>Url</th>
                    <th>Kontext</th>
                    <th>Aufrufe</th>
                    <th>max</th>
                    <th>avg</th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${contextStats}" var="bench">
                <tr data-uri="${bench[0]}" data-context="${bench[3]}">
                    <td data-uri="${bench[0]}">${bench[0]}</td>
                    <td data-context="${bench[3]}">${Org.get(bench[3]).getDesignation()}</td>
                    <td>${bench[4]}</td>
                    <td>${((double) bench[1] / 1000).round(2)}</td>
                    <td>${((double) bench[2] / 1000).round(2)}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>

<laser:script file="${this.getGroovyPageFileName()}">
     $('.secondary.menu > a').tab();

     $('#filterTable').change( function(){
        var ctx = $('#filterTable option:selected').attr('value')

        if(! ctx) {
            $('#contextTable > tbody > tr').removeClass('hidden')
        } else {
            $('#contextTable > tbody > tr').addClass('hidden')
            $('#contextTable > tbody > tr[data-context="' + ctx + '"]').removeClass('hidden')
        }
    })

    $('.table tr td').mouseover( function(){
       var dUri = $(this).attr('data-uri')
       var dCtx = $(this).attr('data-context')

       if (dUri) {
           $('.table tr[data-uri="' + dUri + '"]').addClass('trHover')
       }
       if (dCtx) {
           $('.table tr[data-context="' + dCtx + '"]').addClass('trHover')
       }
    })

    $('.table tr td').mouseout( function(){
       $('.table tr').removeClass('trHover')
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

</body>
</html>