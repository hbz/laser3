<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.finance.BudgetCode; de.laser.finance.CostItem; de.laser.IssueEntitlement; de.laser.IssueEntitlementGroup; de.laser.Subscription; de.laser.SubscriptionPackage; de.laser.UserSetting; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.*; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection />

<ui:modal modalSize="large" id="costItem_ajaxModal" formID="editCost_${idSuffix}" text="${modalText}" msgSave="${submitButtonLabel}">
    <g:if test="${costItem}">
        <g:if test="${showVisibilitySettings && costItem.isVisibleForSubscriber}">
            <div class="ui orange ribbon label">
                <strong>${costItem.sub.getSubscriber()}</strong>
            </div>
        </g:if>
        <g:elseif test="${copyCostsFromConsortia}">
            <div class="ui orange ribbon label">
                <strong><g:message code="financials.transferConsortialCosts"/>: </strong>
            </div>
        </g:elseif>
        <g:elseif test="${subscription}">
            <div class="ui orange ribbon label">
                <strong>${subscription.getSubscriber().name}</strong>
            </div>
        </g:elseif>
        <div class="ui blue right right floated mini button la-js-clickButton" data-position="top center" data-title="${costItem.globalUID}"><g:message code="globalUID.label"/></div>
        <laser:script file="${this.getGroovyPageFileName()}">
            $('.la-js-clickButton')
              .popup({
                on: 'click'
              })
            ;
        </laser:script>
    </g:if>

        <g:form class="ui small form clearing segment la-form" name="editCost_${idSuffix}" url="${formUrl}">
            <laser:render template="costItemInput" />
        </g:form>

</ui:modal>
<!-- _ajaxModal.gsp -->
