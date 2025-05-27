<%@ page import="de.laser.RefdataValue;de.laser.storage.RDConstants" %>
<laser:htmlStart message="menu.public.all_titles" />

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
    </ui:debugInfo>

    <ui:breadcrumbs>
      <ui:crumb message="menu.public.all_titles" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.public.all_titles" total="${allTippsCounts}" floated="true" />

<laser:render template="/templates/titles/top_attached_title_tabs"
              model="${[
                      tt_controller:    controllerName,
                      tt_action:        actionName,
                      tt_tabs:          ['currentTipps', 'plannedTipps', 'expiredTipps', 'deletedTipps', 'allTipps'],
                      tt_counts:        [currentTippsCounts, plannedTippsCounts, expiredTippsCounts, deletedTippsCounts, allTippsCounts]
              ]}" />

<% params.remove('tab')%>

    <div class="ui bottom attached tab active segment">

        <laser:render template="/templates/filter/tipp_ieFilter"/>

        <h3 class="ui icon header la-clear-before ">
            <ui:bubble count="${num_tipp_rows}" grey="true"/> <g:message code="title.found.result"/>
        </h3>

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
            <laser:render template="/templates/tipps/table_accordion" model="[tipps: titlesList, showPackage: false, showPlattform: true]"/>
        </div>
    </div>
</div>

</div>

<g:if test="${titlesList}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${num_tipp_rows}"/>
</g:if>
<laser:htmlEnd />
