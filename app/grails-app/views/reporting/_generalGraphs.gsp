<%@page import="grails.converters.JSON; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants;" %>
<laser:serviceInjection/>

<g:each in="${growth}" var="row">
    <g:set var="key" value="${row.getKey()}"/>
    <g:set var="graph" value="${row.getValue()}"/>
    <div class="ui top attached segment generalChartContainer" id="${key}">
        <h2><g:message code="myinst.reporting.graphHeader" args="${[message(code:'myinst.reporting.graphHeader.'+requestObject),message(code:'org.'+key+'.label')]}"/></h2>
        <a class="closeSegment">
            <i class="ui icon times"></i>
        </a>
    </div>

    <g:applyCodec encodeAs="none">
        <script>
            $(document).ready(function(){
                let result = ${graph};
                let labels = [];
                let seriesObj = {};
                let series = [];
                for(let k in result) {
                    if(result.hasOwnProperty(k)) {
                        let v = result[k];
                        labels.push(k);
                        for(let group in v) {
                            if(v.hasOwnProperty(group)) {
                                if(!seriesObj[group])
                                    seriesObj[group] = [];
                                seriesObj[group].push(v[group]);
                            }
                        }
                    }
                }
                for(let k in seriesObj) {
                    if(seriesObj.hasOwnProperty(k)) {
                        series.push({name:k,data:seriesObj[k]});
                    }
                }
                new Chartist.Bar('#${key}',{labels:labels,series:series},{
                    plugins: [
                        Chartist.plugins.legend()
                    ],
                    stackBars: true,
                    height: '500px'
                });
                $(".generalChartContainer").on('click','.closeSegment',function() {
                    $(this).parent("div").remove();
                });
            });
        </script>
    </g:applyCodec>
</g:each>