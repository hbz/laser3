<!-- _copyModal.gsp -->
<%@ page import="com.k_int.kbplus.CostItem;com.k_int.kbplus.CostItemGroup;com.k_int.kbplus.BudgetCode;com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataValue"" %>
<laser:serviceInjection />

<g:render template="vars" /><%-- setting vars --%>

<semui:modal id="costItem_ajaxModal" text="${message(code:'financials.costItem.copy.tooltip')}">
    <g:form class="ui small form" id="copyCost" url="${formUrl}">

        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}" />
        <g:hiddenField name="process" value="process" />

        <div class="field">

            <g:if test="${sub}">

                <g:if test="${costItem?.sub?.id == sub.id}">
                    <label for="newLicenseeTargets">Für folgende Lizenz kopieren</label>
                    <input type="text" id="newLicenseeTargets" readonly="readonly" value="${sub.name}" />
                    <input type="hidden" name="newLicenseeTargets" value="${'com.k_int.kbplus.Subscription:' + sub.id}" />
                </g:if>

                <g:else>
                    <%
                        def validSubChilds = com.k_int.kbplus.Subscription.findAllByInstanceOfAndStatusNotEqual(
                                sub,
                                com.k_int.kbplus.RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
                        )
                    %>
                    <g:if test="${validSubChilds}">
                        <label for="newLicenseeTargets">Für folgende Teilnehmer kopieren</label>
                        <%--from="${[[id:'forAllSubscribers', label:'Für alle Teilnehmer']] + validSubChilds}"--%>
                        <g:select name="newLicenseeTargets" id="newLicenseeTargets" class="ui search dropdown" multiple="multiple"
                                  from="${validSubChilds}"
                                  optionValue="${{it?.name ? it.getAllSubscribers().join(', ') : it.label}}"
                                  optionKey="${{"com.k_int.kbplus.Subscription:" + it?.id}}"
                                  noSelection="['':'']"
                                  value="${'com.k_int.kbplus.Subscription:' + it?.id}" />

                    </g:if>
                </g:else>

            </g:if>
            <g:else>
                NOT IMPLEMENTED
            </g:else>

        </div><!-- .field -->

    </g:form>

    <script>
        var ajaxPostFunc = function () {
            console.log("ajaxPostFunc")
        }
    </script>
</semui:modal>
<!-- _copyModal.gsp -->
