<script>
function getRandomInt(max) {
  return Math.floor(Math.random() * max);
}
function createChart(divId, data) {

var myChart = echarts.init(document.getElementById(divId));
var option;

// prettier-ignore

const dateList = data.map(function (item) {
  return item[0];
});
const valueList = data.map(function (item) {
  return item[1];
});
option = {

  visualMap: [
    {
      show: false,
      type: 'continuous',
      seriesIndex: 0,
      min: 0,
      max: 4
    }
  ],

  tooltip: {
    trigger: 'axis'
  },
  xAxis: [
    {
      show: false,
      data: dateList
    }
  ],
  yAxis: [
    {
      show: false
    }
  ],
  grid: [
    {
      bottom: '7',
      top: '7',
    }
  ],
  series: [
    {
      smooth: true,
      type: 'line',
      showSymbol: false,
      data: valueList
    }
  ]
};
option && myChart.setOption(option);
}
/*createChart('1',  [["2000-06-05", getRandomInt(4)],["2000-06-05", getRandomInt(4)], ["2000-06-06", getRandomInt(4)], ["2000-06-07", getRandomInt(4)]]);
createChart('2',  [["2000-06-05", getRandomInt(4)],["2000-06-05", getRandomInt(4)], ["2000-06-06", getRandomInt(4)], ["2000-06-07", getRandomInt(4)]]);
createChart('3',  [["2000-06-05", getRandomInt(4)],["2000-06-05", getRandomInt(4)], ["2000-06-06", getRandomInt(4)], ["2000-06-07", getRandomInt(4)]]);
createChart('4',  [["2000-06-05", getRandomInt(4)],["2000-06-05", getRandomInt(4)], ["2000-06-06", getRandomInt(4)], ["2000-06-07", getRandomInt(4)]]);
createChart('5',  [["2000-06-05", getRandomInt(4)],["2000-06-05", getRandomInt(4)], ["2000-06-06", getRandomInt(4)], ["2000-06-07", getRandomInt(4)]]);*/


  <g:each in="${globalHeatMap}" var="uri, stat" status="j">
    <g:set var="avg" value="${((double) stat[2] / 1000).round(2)}" />
    <g:set var="heat" value="${((double) stat[0]).round(2)}" />
    createChart('${j+1}',  [
      <g:each in="${allArchives}" var="it" status="x">
                            ["${it[0]}", ${heat}],
      </g:each>
    ]);
  </g:each>




</script>