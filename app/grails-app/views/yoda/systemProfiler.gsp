<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.yoda.systemProfiler')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.systemProfiler" class="active"/>
</semui:breadcrumbs>
<br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.systemProfiler')}</h1>

    <h3 class="ui header">Global</h3>

    <table class="ui celled la-table la-table-small table">
        <thead>
            <tr>
                <th>url</th>
                <th>total hits</th>
                <th>count(*) > ${de.laser.domain.SystemProfiler.THRESHOLD_MS} ms</th>
                <th>max(count(*) > ${de.laser.domain.SystemProfiler.THRESHOLD_MS} ms)</th>
                <th>avg(count(*))</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${byUri}" var="bench">
                <tr data-uri="${bench[0]}">
                    <td data-uri="${bench[0]}">${bench[0]}</td>
                    <td>${globalCountByUri.get(bench[0])}</td><%-- //total hits --%>
                    <td>${bench[3]}</td><%-- //count(*) --%>
                    <td>${((double) bench[1] / 1000).round(2)}</td><%-- //max() --%>
                    <td>${((double) bench[2] / 1000).round(2)}</td><%-- //avg() --%>
                </tr>
            </g:each>
        </tbody>
    </table>

    <h3 class="ui header">Kontextbezogen</h3>
    <table class="ui celled la-table la-table-small table">
        <thead>
            <tr>
                <th>url</th>
                <th>context</th>
                <th>count(*) > ${de.laser.domain.SystemProfiler.THRESHOLD_MS} ms</th>
                <th>max(count(*) > ${de.laser.domain.SystemProfiler.THRESHOLD_MS} ms)</th>
                <th>avg(max(count(*) > ${de.laser.domain.SystemProfiler.THRESHOLD_MS} ms))</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${byUriAndContext}" var="bench">
                <tr data-uri="${bench[0]}" data-context="${bench[1]}">
                    <td data-uri="${bench[0]}">${bench[0]}</td>
                    <td data-context="${bench[1]}">${com.k_int.kbplus.Org.get(bench[1]).getDesignation()}</td>
                    <td>${bench[4]}</td><%-- //count(*) --%>
                    <td>${((double) bench[2] / 1000).round(2)}</td><%-- //max() --%>
                    <td>${((double) bench[3] / 1000).round(2)}</td><%-- //avg() --%>
                </tr>
            </g:each>
        </tbody>
    </table>

<r:script>
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
</r:script>

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