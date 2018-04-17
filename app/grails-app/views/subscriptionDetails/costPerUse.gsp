<%@ page import="com.k_int.kbplus.Subscription" %>
<r:require module="annotations" />

<!doctype html>
<html>
  <head>
      <meta name="layout" content="semanticUI"/>
      <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
  </head>
  <body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

    <semui:messages data="${flash}" />

    <h1 class="ui header"><semui:headerIcon />

        <semui:xEditable owner="${subscriptionInstance}" field="name" />
    </h1>

    <g:render template="nav"  />

      <g:if test="${costItems && costItems.size() > 0}">
        <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'financials.invoice_number', default:'Invoice Number')}</th>
              <th>${message(code:'default.startDate.label', default:'Start Date')}</th>
              <th>${message(code:'default.endDate.label', default:'End Date')}</th>
              <th>${message(code:'financials.invoice_total', default:'Invoice Total')}</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${costItems}" var="ci">
              <tr>
                <td>${ci.invoice.invoiceNumber}</td>
                <td><g:formatDate date="${ci.invoice.startDate}" format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"/></td>
                <td><g:formatDate date="${ci.invoice.endDate}" format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"/></td>
                <td><span class="pull-right"><g:formatNumber number="${ci.total}" groupingUsed="true" type="currency" currencyCode="${ci.billingCurrency}"/></span></td>
              </tr>
              <tr>
                <td colspan="4">Total usage for this invoice period: <g:formatNumber number="${ci.total_usage_for_sub}"  groupingUsed="true"/> gives an overall cost per use of
                       <strong><g:formatNumber number="${ci.overall_cost_per_use}" groupingUsed="true"  type="currency" currencyCode="${ci.billingCurrency}"/></strong></td>
              </tr>
              <g:each in="${ci.usage}" var="u">
                <tr>
                  <td colspan="3"><span class="pull-right">Apportionment for usage period ${u[0]}/${u[1]}</span></td>
                  <td><span class="pull-right">${u[2]} @ <g:formatNumber number="${ci.overall_cost_per_use}"  groupingUsed="true" type="currency" currencyCode="${ci.billingCurrency}"/>
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

  </body>
</html>
