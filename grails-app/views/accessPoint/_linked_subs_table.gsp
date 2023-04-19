<%@ page import="de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<table id="subPkgPlatformTable" class="ui celled la-js-responsive-table la-table table compact">
  <thead>
  <tr>
    <th>${message(code: "default.subscription.label")}</th>
    <th>${message(code: "accessPoint.package.label")}</th>
    <th>${message(code: "platform.label")}</th>
  </tr>
  </thead>
  <tbody>
  <g:each in="${linkedPlatformSubscriptionPackages}" var="linkedPlatformSubscriptionPackage">
    <g:set var="subscription" value="${linkedPlatformSubscriptionPackage[2]}"/>
    <tr>
      <td><g:link controller="subscription" action="show"
                  id="${subscription.id}">${subscription.name} ${(subscription.status != RDStore.SUBSCRIPTION_CURRENT) ? '('+ subscription.status.getI10n('value') +')': ''}</g:link></td>
      <td><g:link controller="package" action="show"
                  id="${linkedPlatformSubscriptionPackage[1].pkg.id}">${linkedPlatformSubscriptionPackage[1].pkg.name}</g:link></td>
      <td><g:link controller="platform" action="show"
                  id="${linkedPlatformSubscriptionPackage[0].id}">${linkedPlatformSubscriptionPackage[0].name}</g:link></td>
    </tr>
  </g:each>
  </tbody>
</table>