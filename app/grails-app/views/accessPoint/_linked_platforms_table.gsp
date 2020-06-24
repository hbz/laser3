<%@ page import="de.laser.helper.RDConstants; de.laser.helper.RDStore" %>
<laser:serviceInjection/>
<table id="platformTable" class="ui celled la-table table compact">
  <thead>
  <tr>
    <th>${message(code: "platform.label")}</th>
    <g:if test="${ (accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') && inContextOrg ) || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') )}">
      <th>${message(code: "accessPoint.subscriptions.label")}</th>
      <th>${message(code: 'accessPoint.platformLink.action')}</th>
    </g:if>
  </tr>
  </thead>
  <tbody>
  <g:each in="${linkedPlatforms}" var="linkedPlatform">
    <tr>
      <td><g:link controller="platform" action="show" id="${linkedPlatform.platform.id}">${linkedPlatform.platform.name}</g:link></td>
      <td>
        <g:each in="${linkedPlatform.linkedSubs}" var="linkedSub">
          <g:link controller="Subscription" action="show" id="${linkedSub.id}">${linkedSub.name} ${(linkedSub.status != de.laser.helper.RDStore.SUBSCRIPTION_CURRENT) ? '('+ com.k_int.kbplus.RefdataValue.getByValueAndCategory(linkedSub.status.value, de.laser.helper.RDConstants.SUBSCRIPTION_STATUS).getI10n('value') +')': ''}</g:link><br/>
        </g:each>
      </td>
      <g:if test="${ (accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') && inContextOrg ) || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR'))}">
        <td class="center aligned">
          <g:link class="ui negative icon button button js-open-confirm-modal" id="${linkedPlatform.aplink.id}"
                  controller="accessPoint" action="unlinkPlatform"
                  data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform',
                      args: [accessPoint.name, linkedPlatform.platform.name])}"
                  data-confirm-term-how="unlink" data-confirm-id="${java.util.UUID.randomUUID().toString()}">
            <i class="unlink icon"></i>
          </g:link>
        </td>
      </g:if>
    </tr>
  </g:each>
  </tbody>
</table>