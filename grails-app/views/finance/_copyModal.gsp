<!-- _copyModal.gsp -->
<%@ page import="de.laser.Subscription; de.laser.finance.CostItem; de.laser.finance.CostItemGroup; de.laser.finance.BudgetCode; de.laser.OrgRole; de.laser.RefdataValue" %>
<laser:serviceInjection />

<ui:modal id="costItem_ajaxModal" text="${message(code:'financials.costItem.copy.tooltip')}">
    <g:form class="ui small form" id="copyCost" url="${formUrl}">

        <g:hiddenField name="shortcode" value="${contextService.getOrg().shortcode}" />
        <g:hiddenField name="process" value="process" />

        <div class="field">

            <g:if test="${sub}">

                <g:if test="${costItem?.sub?.id == sub.id}">
                    <label for="newLicenseeTargets">Für folgende Lizenz kopieren</label>
                    <input type="text" id="newLicenseeTargets" readonly="readonly" value="${sub.name}" />
                    <input type="hidden" name="newLicenseeTargets" value="${Subscription.class.name + ':' + sub.id}" />
                </g:if>

                <g:else>
                    <%
                        def validSubChilds = Subscription.findAllByInstanceOf( sub )
                    %>
                    <g:if test="${validSubChilds}">
                        <label for="newLicenseeTargets">Für folgende Einrichtungen kopieren</label>
                        <g:select name="newLicenseeTargets" id="newLicenseeTargets" class="ui search dropdown" multiple="multiple"
                                  from="${validSubChilds}"
                                  optionValue="${{it?.name ? (it.getSubscriber() ? it.getSubscriber() : '') : it.label}}"
                                  optionKey="${{Subscription.class.name + ':' + it?.id}}"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                                  value="${Subscription.class.name + ':' + it?.id}" />

                    </g:if>
                </g:else>

            </g:if>
            <g:else>
                NOT IMPLEMENTED
            </g:else>

        </div><!-- .field -->

    </g:form>
</ui:modal>
<!-- _copyModal.gsp -->
