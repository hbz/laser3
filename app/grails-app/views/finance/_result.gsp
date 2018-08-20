<!-- _result.gsp -->
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${queryMode == FinanceController.MODE_CONS_SUBSCR}">

    <div id="financeFilterData" class="ui top attached tabular menu">
        <div class="item active" data-tab="OWNER">Eigene Kosten</div>
        <div class="item" data-tab="CONS_SUBSCR">Teilnehmerkosten</div>
    </div>

    <r:script>
        $('#financeFilterData .item').tab()
    </r:script>

</g:if><%-- FinanceController.MODE_CONS_SUBSCR --%>

<%
    // WORKAROUND; grouping costitems by subscription
    def costItemsOwner = ["clean":[]]
    (ciListOwner?.collect{it.sub}).each{ item ->
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

<%
    def costItemsCS = ciListConsSubsc
%>

<g:if test="${queryMode == FinanceController.MODE_CONS_SUBSCR}">
    <div class="ui bottom attached tab active" data-tab="OWNER">
</g:if><%-- FinanceController.MODE_CONS_SUBSCR --%>

    <g:if test="${! costItemsOwner}">
        <g:render template="result_tab_owner" model="[editable: editable, cost_items: [], i: 'empty']"></g:render>
    </g:if>

    <g:if test="${costItemsOwner.size() > 1}">
        <div class="ui fluid accordion">
            <br />
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

            <g:render template="result_tab_owner" model="[editable: editable, cost_items: cost_items, i: i]"></g:render>

            <g:if test="${costItemsOwner.size() > 1}">
                </div><!-- .content -->
            </g:if>
        </g:each>

    <g:if test="${costItemsOwner.size() > 1}">
        </div><!-- .accordion -->
    </g:if>


<g:if test="${queryMode == FinanceController.MODE_CONS_SUBSCR}">

    </div><!-- OWNER -->
    <div class="ui bottom attached tab" data-tab="CONS_SUBSCR">

        <g:render template="result_tab_sc" model="[editable: editable, cost_items: costItemsCS, i: 'fake']"></g:render>

    </div><!-- CONS_SUBSCR -->

</g:if><%-- FinanceController.MODE_CONS_SUBSCR --%>

<!-- _result.gsp -->