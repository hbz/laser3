<%@ page import="de.laser.RefdataValue;de.laser.storage.RDConstants" %>
<laser:htmlStart message="menu.public.all_titles" />

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

%{--<ui:tabs actionName="${actionName}">--}%
%{--    <ui:tabsItem controller="${controllerName}" action="${actionName}"--}%
%{--                 params="[tab: 'currentTipps']"--}%
%{--                 text="${message(code: "package.show.nav.current")}" tab="currentTipps"--}%
%{--                 counts="${currentTippsCounts}"/>--}%
%{--    <ui:tabsItem controller="${controllerName}" action="${actionName}"--}%
%{--                 params="[tab: 'plannedTipps']"--}%
%{--                 text="${message(code: "package.show.nav.planned")}" tab="plannedTipps"--}%
%{--                 counts="${plannedTippsCounts}"/>--}%
%{--    <ui:tabsItem controller="${controllerName}" action="${actionName}"--}%
%{--                 params="[tab: 'expiredTipps']"--}%
%{--                 text="${message(code: "package.show.nav.expired")}" tab="expiredTipps"--}%
%{--                 counts="${expiredTippsCounts}"/>--}%
%{--    <ui:tabsItem controller="${controllerName}" action="${actionName}"--}%
%{--                 params="[tab: 'deletedTipps']"--}%
%{--                 text="${message(code: "package.show.nav.deleted")}" tab="deletedTipps"--}%
%{--                 counts="${deletedTippsCounts}"/>--}%
%{--    <ui:tabsItem controller="${controllerName}" action="${actionName}"--}%
%{--                 params="[tab: 'allTipps']"--}%
%{--                 text="${message(code: "menu.public.all_titles")}" tab="allTipps"--}%
%{--                 counts="${allTippsCounts}"/>--}%
%{--</ui:tabs>--}%

<% params.remove('tab')%>
    <div class="ui bottom attached tab active segment">

        <laser:render template="/templates/filter/tipp_ieFilter"/>

        <h3 class="ui icon header la-clear-before la-noMargin-top">
            <span class="ui circular label">${num_tipp_rows}</span> <g:message code="title.filter.result"/>
        </h3>
<%
    Map<String, String>
    sortFieldMap = ['sortname': message(code: 'title.label')]
    if (journalsOnly) {
        sortFieldMap['startDate'] = message(code: 'default.from')
        sortFieldMap['endDate'] = message(code: 'default.to')
    } else {
        sortFieldMap['dateFirstInPrint'] = message(code: 'tipp.dateFirstInPrint')
        sortFieldMap['dateFirstOnline'] = message(code: 'tipp.dateFirstOnline')
    }
%>
<div class="ui form">
    <div class="three wide fields">
        <div class="field">
            <ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
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
