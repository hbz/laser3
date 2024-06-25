<%@ page import="de.laser.reporting.export.LocalExportHelper;" %>
<br />

<div style="text-align: right">
    <button id="details-export-button" class="ui icon button" data-href="#detailsExportModal" data-ui="modal">
        <i class="ui icon download"></i>
    </button>
</div>

<div class="ui tiny steps">
    <g:each in="${labels}" var="lbl" status="i">
        <g:if test="${i+1 == labels.size()}">
            <div class="step">
        </g:if>
        <g:else>
            <div class="step">
        </g:else>
        <div class="content">
            <div class="title">
                <g:if test="${query in ['timeline-annualMember-subscription', 'timeline-referenceYearMember-subscription']}"> %{-- TODO --}%
                    <g:if test="${lbl == -1}">
                        ${message(code: 'reporting.chart.result.noEndDate.label')}
                    </g:if>
                    <g:elseif test="${lbl == -2}">
                        ${message(code: 'reporting.chart.result.noStartDate.label')}
                    </g:elseif>
                    <g:elseif test="${lbl}">
                        ${lbl}
                    </g:elseif>
                    <g:else> %{-- TODO --}%
                        ${message(code: 'reporting.chart.result.noData.label')}
                    </g:else>
                </g:if>
                <g:else>
                    ${lbl}
                </g:else>
            </div>
        </div>
        </div>
    </g:each>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $(function(){ responsiveTables.go(); })
</laser:script>

