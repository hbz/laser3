<%@page import="grails.converters.JSON; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.helper.RDConstants; de.laser.ReportingService" %>
<laser:serviceInjection/>

<g:each in="${growth}" var="row">
    <g:set var="key" value="${row.getKey()}"/>
    <g:set var="graph" value="${row.getValue()}"/>
    <div class="ui top attached segment generalChartContainer" id="${requestObject}${key}">
        <g:if test="${key.contains(ReportingService.CONFIG_ORG_PROPERTY)}">
            <g:set var="groupingText" value="${propDef}"/>
        </g:if>
        <g:else>
            <g:set var="groupingText" value="${message(code:'org.'+key+'.label')}"/>
        </g:else>
        <h2 class="ui header"><g:message code="myinst.reporting.graphHeader" args="${[message(code:'myinst.reporting.graphHeader.'+requestObject),groupingText]}"/></h2>
        <a class="closeSegment">
            <i class="ui icon times"></i>
        </a>
    </div>

    <g:applyCodec encodeAs="none">
        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.genGraph = function() {
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
                new Chartist.Bar('#${requestObject}${key}',{labels:labels,series:series},{
                    plugins: [
                        Chartist.plugins.legend()
                    ],
                    stackBars: true,
                    height: '500px'
                });
                $(".generalChartContainer").on('click','.closeSegment',function() {
                    $(this).parent("div").hide();
                    $('.generalLoadingParam[data-display="${key}"]').removeClass('red');
                });
            };

            JSPC.app.genGraph();
        </laser:script>
    </g:applyCodec>
</g:each>