<%@ page import="de.laser.utils.RandomUtils; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<table id="platformTable" class="ui celled la-js-responsive-table la-table table compact">
  <thead>
  <tr>
    <th>${message(code: "platform.label")}</th>
    <g:if test="${contextService.is_INST_EDITOR_with_PERMS_BASIC( inContextOrg )}">
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
          <g:link controller="Subscription" action="show" id="${linkedSub.id}">${linkedSub.name} ${(linkedSub.status != RDStore.SUBSCRIPTION_CURRENT) ? '('+ linkedSub.status.getI10n('value') +')': ''}</g:link><br />
        </g:each>
      </td>
      <g:if test="${contextService.is_INST_EDITOR_with_PERMS_BASIC( inContextOrg )}">
        <td class="center aligned">
          <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}" id="${linkedPlatform.aplink.id}"
                  controller="accessPoint" action="unlinkPlatform"
                  data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform',
                      args: [accessPoint.name, linkedPlatform.platform.name])}"
                  data-confirm-term-how="unlink" data-confirm-id="${RandomUtils.getUUID()}"
                  role="button"
                  aria-label="${message(code: 'ariaLabel.unlink.universal')}">
            <i class="${Icon.CMD.UNLINK}"></i>
          </g:link>
        </td>
      </g:if>
    </tr>
  </g:each>
  </tbody>
</table>