<%@ page import="de.laser.RefdataValue;de.laser.storage.RDConstants" %>
<laser:htmlStart message="menu.public.all_titles" />

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>

<ui:breadcrumbs>
    <ui:crumb message="menu.public.all_titles" class="active" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.public.all_titles" total="${allTippCounts}" floated="true" />

<ui:messages data="${flash}"/>

<laser:render template="/templates/filter/tipp_ieFilter"/>

<h3 class="ui icon header la-clear-before la-noMargin-top">
    <ui:bubble count="${num_tipp_rows}" grey="true"/> <g:message code="title.filter.result"/>
</h3>

<g:if test="${params.containsKey('filterSet')}">
    <div class="ui form">
        <div class="three wide fields">
            <div class="field">
                <laser:render template="/templates/titles/sorting_dropdown" model="${[sd_type: 2, sd_journalsOnly: journalsOnly, sd_sort: params.sort, sd_order: params.order]}" />
            </div>
        </div>
    </div>
    <div class="ui grid">
        <div class="row">
            <div class="column">
                <laser:render template="/templates/tipps/table_accordion" model="[tipps: titlesList, showPackageLinking: true, disableStatus: true]"/>
            </div>
        </div>
    </div>
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${num_tipp_rows}"/>
</g:if>
<g:else>
    <ui:msg class="info" showIcon="true" message="title.filter.notice"/>
</g:else>
<laser:htmlEnd />
