<!-- _result.gsp -->
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>
<laser:serviceInjection />

<%
    def tabOwnerActive  = (tab?.equalsIgnoreCase('owner')) ? 'active' : ''
    def tabSCActive     = (tab?.equalsIgnoreCase('sc')) ? 'active' : ''
%>


<div id="financeFilterData" class="ui top attached tabular menu" data-current="${queryMode ? queryMode.minus('MODE_') : ''}">
    <g:if test="${queryMode != FinanceController.MODE_CONS_AT_SUBSCR}">
        <div class="item ${tabOwnerActive}" data-tab="OWNER">Eigene Kosten</div>
    </g:if>
    <g:if test="${queryMode == FinanceController.MODE_CONS}">
        <div class="item ${tabSCActive}" data-tab="CONS">Teilnehmerkosten (Konsortialsicht)</div>
    </g:if>
    <g:if test="${queryMode == FinanceController.MODE_CONS_AT_SUBSCR}">
        <div class="item ${tabOwnerActive}" data-tab="CONS_AT_SUBSCR">Teilnehmerkosten (Konsortialsicht)</div>
    </g:if>
    <g:if test="${queryMode == FinanceController.MODE_SUBSCR}">
        <div class="item" data-tab="SUBSCR">Teilnehmerkosten</div>
    </g:if>
</div>

<%
    // WORKAROUND; grouping costitems by subscription
    def costItemsOwner = ["clean":[]]
    (ciList?.collect{it.sub}).each{ item ->
        if (item) {
            costItemsOwner << ["${item.name}": []]
        }
    }
    cost_items.each{ item ->
        if (item.sub) {
            costItemsOwner.get("${item.sub?.name}").add(item)
        }
        else {
            costItemsOwner.get('clean').add(item)
        }
    }
    costItemsOwner = costItemsOwner.findAll{ ! it.value.isEmpty() }

%>

<g:if test="${queryMode != FinanceController.MODE_CONS_AT_SUBSCR}">
    <!-- OWNER -->
    <div class="ui bottom attached tab ${tabOwnerActive}" data-tab="OWNER">

        <g:if test="${! costItemsOwner}">
            <br />
            <g:render template="result_tab_owner" model="[editable: editable, cost_items: [], i: 'empty']"></g:render>
        </g:if>

        <g:if test="${costItemsOwner.size() > 1}">
            <br />
            <div class="ui fluid accordion">
        </g:if>

            <g:each in="${costItemsOwner}" var="subListItem" status="i">

                <g:if test="${costItemsOwner.size() > 1}">
                    <div class="title">
                        <i class="dropdown icon"></i>
                        ${subListItem.key != 'clean' ? subListItem.key : 'Ohne konkrete Zuordnung'}
                        ( ${subListItem.value?.size()} )
                    </div>

                    <div class="content">
                </g:if>

                <g:set var="cost_items" value="${subListItem.value}" />

                <br />
                <g:render template="result_tab_owner" model="[editable: editable, cost_items: cost_items, i: i]"></g:render>

                <g:if test="${costItemsOwner.size() > 1}">
                    </div><!-- .content -->
                </g:if>
            </g:each>

        <g:if test="${costItemsOwner.size() > 1}">
            </div><!-- .accordion -->
        </g:if>

    </div><!-- OWNER -->
</g:if>

<g:if test="${queryMode == FinanceController.MODE_CONS}">

    <!-- CONS -->
    <div class="ui bottom attached tab ${tabSCActive}" data-tab="CONS">
        <br />
        <g:render template="result_tab_cons" model="[editable: editable, cost_items: ciListCons, i: 'CONS']"></g:render>
    </div><!-- CONS -->
</g:if>
<g:if test="${queryMode == FinanceController.MODE_CONS_AT_SUBSCR}">

    <!-- CONS_AT_SUBSCR -->
    <div class="ui bottom attached tab ${tabOwnerActive}" data-tab="CONS_AT_SUBSCR">
    <br />
    <g:render template="result_tab_cons" model="[editable: editable, cost_items: ciListCons, i: 'CONS_AT_SUBSCR']"></g:render>
</div><!-- CONS_AT_SUBSCR -->
</g:if>
<g:if test="${queryMode == FinanceController.MODE_SUBSCR}">

    <!-- SUBSCR -->
    <div class="ui bottom attached tab" data-tab="SUBSCR">
        <br />
        <g:render template="result_tab_subscr" model="[editable: editable, cost_items: ciListSubscr, i: 'SUBSCR']"></g:render>
    </div><!-- SUBSCR -->
</g:if>

<r:script>
    $('#financeFilterData .item').tab({
        onVisible: function(tabPath) {
            $('#financeFilterData').attr('data-current', tabPath)
            console.log(tabPath)
        }
    })
</r:script>

<!-- _result.gsp -->