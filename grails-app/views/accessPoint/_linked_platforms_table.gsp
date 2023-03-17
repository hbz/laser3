<%@ page import="de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<table id="platformTable" class="ui celled la-js-responsive-table la-table table compact">
  <thead>
  <tr>
    <th>${message(code: "platform.label")}</th>
    <g:if test="${accessService.checkInstEditorForCustomerType_Basic( inContextOrg )}">
      <th>${message(code: "accessPoint.subscriptions.label")}</th>
      <th>${message(code: 'default.action.label')}</th>
    </g:if>
  </tr>
  </thead>
  <tbody>
  <g:each in="${linkedPlatforms}" var="linkedPlatform">
    <tr>
      <td><g:link controller="platform" action="show" id="${linkedPlatform.platform.id}">${linkedPlatform.platform.name}</g:link></td>
      <td>
        <g:each in="${linkedPlatform.linkedSubs}" var="linkedSub">
          <g:link controller="Subscription" action="show" id="${linkedSub.id}">${linkedSub.name} ${(linkedSub.status != RDStore.SUBSCRIPTION_CURRENT) ? '('+ RefdataValue.getByValueAndCategory(linkedSub.status.value, RDConstants.SUBSCRIPTION_STATUS).getI10n('value') +')': ''}</g:link><br />
        </g:each>
      </td>
      <g:if test="${accessService.checkInstEditorForCustomerType_Basic( inContextOrg )}">
        <td class="center aligned">
          <g:link class="ui negative icon button la-modern-button js-open-confirm-modal" id="${linkedPlatform.aplink.id}"
                  controller="accessPoint" action="unlinkPlatform"
                  data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform',
                      args: [accessPoint.name, linkedPlatform.platform.name])}"
                  data-confirm-term-how="unlink" data-confirm-id="${java.util.UUID.randomUUID().toString()}"
                  role="button"
                  aria-label="${message(code: 'ariaLabel.unlink.universal')}">
            <i class="unlink icon"></i>
          </g:link>
        </td>
      </g:if>
    </tr>
  </g:each>
  </tbody>
</table>