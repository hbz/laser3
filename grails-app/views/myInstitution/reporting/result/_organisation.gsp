<%@page import="de.laser.ReportingService;de.laser.Org" %>
<laser:serviceInjection/>

<g:if test="${result}">
    <g:if test="${result.orgIdList}">

        <div class="ui segment">
            <g:select name="result-chooser"
                      from="${resultList}"
                      optionKey="key"
                      optionValue="value"
                      class="ui selection dropdown"
                      value="opt1"
                      noSelection="${['': message(code: 'default.select.choose.label')]}" />

            <g:select name="echart-chooser"
                      from="${cfgChartsList}"
                      optionKey="key"
                      optionValue="value"
                      class="ui selection dropdown"
                      noSelection="${['': message(code: 'default.select.choose.label')]}" />
        </div>

        <div id="echarts-bar" class="echarts-wrapper"></div>

        <div id="echarts-pie" class="echarts-wrapper"></div>

        <style>
            .echarts-wrapper {
                width: 100%;
                height: 400px;
            }
        </style>

        <laser:script file="${this.getGroovyPageFileName()}">

            // PROTOTYPE ONLY

            $('#result-chooser').on('change', function(e){
                var choice = $('#result-chooser').dropdown('get value')
                alert (' TODO: ' + choice )
           })

            $('#echart-chooser').on('change', function(e){
                var choice = $('#echart-chooser').dropdown('get value')

                $('.echarts-wrapper').addClass('hidden')

                if (choice == 'pie') {
                    $('#echarts-pie').removeClass('hidden')
                    demo_pie()
                }
                if (choice == 'bar') {
                    $('#echarts-bar').removeClass('hidden')
                    demo_bar()
                }
           })


           function demo_pie() {

var myChart = echarts.init($('#echarts-pie')[0]);
var option;

option = {
   title: {
       text: 'Bibliothekstyp aller Einrichtungen',
       left: 'center'
   },
   tooltip: {
       trigger: 'item'
   },
   legend: {
       orient: 'vertical',
       left: 'left',
   },
   series: [
       {
           type: 'pie',
           radius: '60%',
           data: [
            <%
                List dummy = Org.executeQuery(
                        'select c.value_de, count(*) from Org o join o.libraryType c where o.id in :orgIdList group by c.value_de',
                        [orgIdList: result.orgIdList]
                )
                dummy.each{ it ->
                    println "{value: ${it[1]}, name:'${it[0]}'},"
                }
            %>
            ],
            emphasis: {
                itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }
    ]
};

option && myChart.setOption(option);

}


            function demo_bar() {

var myChart = echarts.init($('#echarts-bar')[0]);
var option;

option = {
    dataset: {
        source: [
            ['count', 'libraryType'],
            <%
                List dummy2 = Org.executeQuery(
                        'select c.value_de, count(*) from Org o join o.libraryType c where o.id in :orgIdList group by c.value_de',
                        [orgIdList: result.orgIdList]
                )
                dummy2.each{ it ->
                    println "[${it[1]}, '${it[0]}'],"
                }
            %>
        ]
    },
    grid:  {containLabel: true},
    xAxis: {name: 'Anzahl'},
    yAxis: {type: 'category'},
    series: [
        {
            type: 'bar',
            encode: {
                x: 'count',
                y: 'libraryType'
            },
            label: {
                show: true,
                position: 'right'
            }
        }
    ]
};

option && myChart.setOption(option);
}

        </laser:script>

    </g:if>
</g:if>