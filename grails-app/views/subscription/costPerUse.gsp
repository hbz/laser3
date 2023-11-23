<%@ page import="de.laser.Subscription" %>
<laser:htmlStart message="default.subscription.label" />

    <laser:render template="breadcrumb" model="${[ params:params ]}"/>

    <ui:messages data="${flash}" />

    <ui:h1HeaderWithIcon>
      <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>

    <laser:render template="nav"  />

      <g:if test="${costItems && costItems.size() > 0}">
        <table class="ui celled la-js-responsive-table la-table table">
          <thead>
            <tr>
              <th>${message(code:'financials.invoice_number')}</th>
              <th>${message(code:'default.startDate.label.shy')}</th>
              <th>${message(code:'default.endDate.label.shy')}</th>
              <th>${message(code:'financials.invoice_total')}</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${costItems}" var="ci">
              <tr>
                <td>${ci.invoice.invoiceNumber}</td>
                <td><g:formatDate date="${ci.invoice.startDate}" format="${message(code:'default.date.format.notime')}"/></td>
                <td><g:formatDate date="${ci.invoice.endDate}" format="${message(code:'default.date.format.notime')}"/></td>
                <td><span class="la-float-right"><g:formatNumber number="${ci.total}" groupingUsed="true" type="currency" currencyCode="${ci.billingCurrency}"/></span></td>
              </tr>
              <tr>
                <td colspan="4">Total usage for this invoice period: <g:formatNumber number="${ci.total_usage_for_sub}"  groupingUsed="true"/> gives an overall cost per use of
                       <strong><g:formatNumber number="${ci.overall_cost_per_use}" groupingUsed="true"  type="currency" currencyCode="${ci.billingCurrency}"/></strong></td>
              </tr>
              <g:each in="${ci.usage}" var="u">
                <tr>
                  <td colspan="3"><span class="la-float-right">Apportionment for usage period ${u[0]}/${u[1]}</span></td>
                  <td><span class="la-float-right">${u[2]} @ <g:formatNumber number="${ci.overall_cost_per_use}"  groupingUsed="true" type="currency" currencyCode="${ci.billingCurrency}"/>
                       = <g:formatNumber number="${ci.overall_cost_per_use * Integer.parseInt(u[2])}"  groupingUsed="true" type="currency" currencyCode="${ci.billingCurrency}"/></span></td>

                </tr>
              </g:each>
            </g:each>
          </tbody>
        </table>
      </g:if>
      <g:else>
        <p>
        ${message(code:'myinst.subscriptionDetails.noCostPerUse')}
        </p>
      </g:else>

<laser:htmlEnd />
