<%@page import="grails.converters.JSON; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants;" %>
<laser:serviceInjection/>

<div class="ui top attached segment" id="chartContainer${entry.id}">
    <h2 class="ui header">${entry.dropdownNamingConvention(institution)}</h2>
    <a class="closeSegment" data-entry="${genericOIDService.getOID(entry)}">
        <i class="ui icon times"></i>
    </a>
</div>

<g:applyCodec encodeAs="none">
    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.goSubGraph = function() {
            let result, labels, seriesObj, series
                <g:if test="${costItemDevelopment}">
                    result = ${costItemDevelopment};
                    labels = [];
                    seriesObj = {};
                    series = [];
                    $("#chartContainer${entry.id}").append('<div id="chart${entry.id}"></div>');
                    for(let k in result) {
                        if(result.hasOwnProperty(k)) {
                            let v = result[k];
                            labels.push(k);
                            for(let subscriber in v) {
                                if(v.hasOwnProperty(subscriber)) {
                                    if(!seriesObj[subscriber])
                                        seriesObj[subscriber] = []
                                    seriesObj[subscriber].push(v[subscriber]);
                                }
                            }
                        }
                    }
                    for(let k in seriesObj) {
                        if(seriesObj.hasOwnProperty(k)) {
                            series.push({name:k,data:seriesObj[k]})
                        }
                    }
                    new Chartist.Bar('#chart${entry.id}',{labels:labels,series:series},{
                        stackBars: true,
                        plugins: [
                            Chartist.plugins.legend()
                        ],
                        height: '500px'
                    }).on('draw', function(data) {
                        if(data.type === 'bar') {
                            data.element.attr({
                                style: 'stroke-width: 30px'
                            });
                        }
                    });
                </g:if>
                <g:if test="${costItemDivision}">
                    result = ${costItemDivision};
                    labels = [];
                    seriesObj = {};
                    series = [];
                    let yr = 0;
                    for(let yearRing in result) {
                        if(result.hasOwnProperty(yearRing)) {
                            $("#chartContainer${entry.id}").append('<div id="chartWrapper${entry.id}'+yr+'"></div>');
                            let elementsInYear = result[yearRing];
                            let el = 0;
                            for(let element in elementsInYear) {
                                if(elementsInYear.hasOwnProperty(element)) {
                                    for(let inst in elementsInYear[element]) {
                                        if(elementsInYear[element].hasOwnProperty(inst)){
                                            labels.push(inst)
                                            //for each element: list the name and the data
                                            series.push(elementsInYear[element][inst]);
                                        }
                                    }
                                    $("#chartWrapper${entry.id}"+yr).append('<h3>'+element+'</h3><div id="chart${entry.id}'+el+'"></div>');
                                    new Chartist.Pie('#chart${entry.id}'+el,{labels:labels,series:series},{
                                        startAngle: 270,
                                        showLabel: true,
                                        height: '500px'
                                    });
                                    labels = [];
                                    series = [];
                                    el++;
                                }
                            }
                            yr++;
                        }
                    }
                    for(let k in seriesObj) {
                        if(seriesObj.hasOwnProperty(k)) {
                            for(let v in seriesObj[k]) {
                                if(seriesObj[k].hasOwnProperty(v)) {
                                    //console.log(seriesObj[k][v]);
                                    series.push({name:v,data:seriesObj[k][v]});
                                }
                            }
                        }
                    }
                </g:if>
            $("#chartContainer${entry.id}").on('click','.closeSegment',function() {
                $('div[data-entry="'+$(this).attr('data-entry')+'"]').remove();
                $('.pickSubscription[data-entry="'+$(this).attr('data-entry')+'"]').removeClass('blue');
            });
        };

        JSPC.app.goSubGraph();
    </laser:script>
</g:applyCodec>