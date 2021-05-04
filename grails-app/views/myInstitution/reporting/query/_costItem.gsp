<%@page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.reporting.myInstitution.CostItemConfig;" %>
<laser:serviceInjection/>

<g:if test="${filterResult}">
    <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterResult.labels]}" />

    <g:if test="${filterResult.data.costItemIdList}">

        <div class="ui message success">
            <p>
                Mit diesen Filtereinstellungen wurden
                <strong>${filterResult.data.costItemIdList.size()} Kosten</strong> gefunden.
            </p>
        </div>

        <g:render template="/myInstitution/reporting/query/base.part1" />

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.reporting.current.request = {
                context: '${BaseConfig.KEY}',
                filter: '${BaseConfig.KEY_COSTITEM}',
                token: '${token}'
            }
        </laser:script>

    </g:if>
    <g:else>
        <div class="ui message negative">
            <p><g:message code="reporting.filter.no.matches" /></p>
        </div>
    </g:else>
</g:if>