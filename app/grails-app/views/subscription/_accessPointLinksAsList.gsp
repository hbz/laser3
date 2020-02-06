<%@ page import="com.k_int.kbplus.Subscription; com.k_int.kbplus.SubscriptionPackage; com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>

<table class="ui three column table">
  <g:each in="${subscriptionInstance.packages.sort { it.pkg.name }}" var="sp">
    <g:set var="cssId" value="oapLinksModal-${sp.id}"/>
    <tr>
      <th scope="row"
          class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.packages.label')}</th>
      <td>
        <g:link controller="package" action="show" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>

        <g:if test="${sp.pkg?.contentProvider}">
          (${sp.pkg?.contentProvider?.name})
        </g:if>
      </td>
      <td class="right aligned">
        <g:if test="${editmode}">
          EEEEEEE
          <%
              com.k_int.kbplus.Subscription subscription = subscriptionInstance
              com.k_int.kbplus.Package pkg = sp.pkg
            String query = "from IssueEntitlement ie, Package pkg where ie.subscription =:sub and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
            Map queryParams = [sub: subscription, pkg_id: pkg.id]
              String conflictText = ''
//            def numOfPCs = removePackagePendingChanges(pkg.id, subscription.id, params.confirmed)
            def numOfPCs = 456

            def numOfIEs = com.k_int.kbplus.IssueEntitlement.executeQuery("select ie.id ${query}", queryParams).size()
//            def conflict_item_pkg = [name: "${g.message(code: "subscription.details.unlink.linkedPackage")}", details: [['link': createLink(controller: 'package', action: 'show', id: pkg.id), 'text': pkg.name]], action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.linkedPackage.action")}"]]
//            def conflicts_list = [conflict_item_pkg]

            if (numOfIEs > 0) {
              conflictTex += numOfIEs+' Bestand'
//              def conflict_item_ie = [name: "${g.message(code: "subscription.details.unlink.packageIEs")}", details: [['text': "${g.message(code: "subscription.details.unlink.packageIEs.numbers")} " + numOfIEs]], action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.packageIEs.action")}"]]
//              conflicts_list += conflict_item_ie
            }
            if (numOfPCs > 0) {
              conflictTex += numOfPCs+' Pending Changes'
//              def conflict_item_pc = [name: "${g.message(code: "subscription.details.unlink.pendingChanges")}", details: [['text': "${g.message(code: "subscription.details.unlink.pendingChanges.numbers")} " + numOfPCs]], action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.pendingChanges.numbers.action")}"]]
//              conflicts_list += conflict_item_pc
            }

            SubscriptionPackage sp1 = com.k_int.kbplus.SubscriptionPackage.findByPkgAndSubscription(pkg, subscription)
            List accessPointLinks = []
            if (sp1.oapls){
              conflictTex += sp.oapls+' verlinkte Zugangskonfigurationen'
//              Map detailItem = ['text':"${g.message(code: "subscription.details.unlink.accessPoints.numbers")} ${sp1.oapls.size()}"]
//              accessPointLinks.add(detailItem)
            }
            if (accessPointLinks) {
              conflictTex += accessPointLinks+' verknüpfte Zugangskonfigurationen'
//              def conflict_item_oap = [name: "${g.message(code: "subscription.details.unlink.accessPoints")}", details: accessPointLinks, action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.accessPoints.numbers.action")}"]]
//              conflicts_list += conflict_item_oap
            }
          %>
          %{--Anzeige: conflicts_list, each detail_item.link und detail_item.text, von letzterem nur die zahl--}%
          <button class="ui icon negative button la-selectable-button" onclick="unlinkPackage(${sp.pkg.id})">
            <i class="unlink icon"></i>
          </button>
          <g:link action="unlinkPackage"
                  params="[cmd:'delete', deleteIds: sp.pkg.id]"
                  data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [fieldValue(bean: sp.pkg?.contentProvider, field: "name"), conflictText])}"
                  data-confirm-term-how="delete"
                  class="ui icon negative button js-open-confirm-modal"
                  role="button">
                  %{--data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [fieldValue(bean: sp.pkg?.contentProvider, field: "name"), 'Anzahl'])}"--}%
            <i class="trash alternate icon"></i>
          </g:link>
          %{--<laser:remoteLink class="ui icon negative button js-open-confirm-modal"--}%
                            %{--action="unlinkPackage"--}%
                            %{--params="[cmd:'delete', deleteIds: sp.pkg.id]"--}%
                            %{--id="${prop.id}"--}%
                            %{--data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [prop.type.getI10n('name')])}"--}%
                            %{--data-confirm-term-how="delete"--}%
                            %{--data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id})"--}%
                            %{--data-always="c3po.loadJsAfterAjax(); bb8.init('#${custom_props_div}')"--}%
                            %{--data-update="${custom_props_div}"--}%
                            %{--role="button"--}%
          %{-->--}%
            %{--<i class="trash alternate icon"></i>--}%
          %{--</laser:remoteLink>--}%
          FFFFFFF
        </g:if>
      </td>
    </tr>
    <tr>
      <td></td>
      <td colspan="2">
        <div class="ui top aligned divided relaxed list">
          <div class="item">
            <div class="right floated content">
              <b>${message(code: 'subscription.details.linkAccessPoint.accessConfig.label')}</b>
            </div>
            <div class="content">
              <b>${message(code: 'subscription.details.linkAccessPoint.platform.label')}</b>
            </div>
          </div>
          <g:if test="${sp.pkg.tipps}">
            <g:each in="${sp.pkg.tipps.platform.unique()}" var="platform">
              <div class="item">
                <div class="right floated content">
                  <g:each in="${sp.getAccessPointListForOrgAndPlatform(subscriptionInstance.getSubscriber(), platform)?.collect()}" var="orgap">
                    <g:link controller="accessPoint" action="edit_${orgap.oap.accessMethod}"
                            id="${orgap.oap.id}">${orgap.oap.name} (${orgap.oap.accessMethod.getI10n('value')})</g:link>
                    <g:if test="${accessConfigEditable && !platform.usesPlatformAccessPoints(contextOrg, sp)}">
                      <g:link class="ui mini negative icon button js-open-confirm-modal" controller="accessPoint"
                              action="unlinkPlatform" id="${orgap.id}"
                              data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform', args: [orgap.oap.name, orgap.platform.name])}"
                              data-confirm-term-how="unlink">
                        <i class="unlink icon"></i>
                      </g:link>
                    </g:if>
                    <br/>
                  </g:each>
                  <g:if test="${!platform.usesPlatformAccessPoints(contextOrg, sp)}">
                    <g:render template="/templates/links/accessPointLinksModal"
                              model="${[tmplText           : message(code: 'platform.link.accessPoint.button.label'),
                                        tmplID             : 'addLink',
                                        tmplIcon           : 'plus',
                                        tmplCss            : 'icon small la-selectable-button',
                                        tmplModalID        : "platf_link_ap-${sp.id}",
                                        editmode           : accessConfigEditable,
                                        accessPointList    : sp.getNotActiveAccessPoints(contextOrg),
                                        institution        : contextService.getUser().getAuthorizedOrgs(),
                                        selectedInstitution: contextOrg,
                                        platformInstance   : platform,
                                        subscriptionPackage: sp
                              ]}"/>
                  </g:if>
                </div>

                <div class="content">
                  <g:if test="${platform}">
                    <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                    <g:if test="${platform.usesPlatformAccessPoints(contextOrg, sp)}">
                      <span data-position="top right"
                            class="la-popup-tooltip la-delay"
                            data-content="${accessConfigEditable ? message(code:'subscription.details.linkAccessPoint.accessConfig.tooltip.thumbtack.content') : message(code:'subscription.details.linkAccessPoint.accessConfig.tooltip.thumbtack.contentNotEditable')}">
                        <g:render template="/subscription/accessPointInheritModal"
                                  model="${[tmplText           : message(code:'subscription.details.linkAccessPoint.accessConfig.modal.removeDerivation.header'),
                                            tmplID             : 'removeDerivation',
                                            tmplIcon           : 'thumbtack blue',
                                            tmplCss            : accessConfigEditable ? 'icon small la-selectable-button' : 'icon small',
                                            tmplModalID        : "removeDerivationModal-${sp.id}",
                                            editmode           : accessConfigEditable,
                                            accessPointList    : platform.getNotActiveAccessPoints(contextOrg),
                                            institution        : contextService.getUser().getAuthorizedOrgs(),
                                            selectedInstitution: contextOrg,
                                            platformInstance   : platform,
                                            subscriptionPackage: sp
                                  ]}"/>
                      </span>
                    </g:if>
                    <g:else>
                      <span data-position="top right"
                            class="la-popup-tooltip la-delay"
                            data-content="${accessConfigEditable ? message(code:'subscription.details.linkAccessPoint.accessConfig.tooltip.inherit.content') : message(code:'subscription.details.linkAccessPoint.accessConfig.tooltip.inherit.contentNotEditable')}">
                        <g:render template="/subscription/accessPointInheritModal"
                                  model="${[tmplText           : message(code:'subscription.details.linkAccessPoint.accessConfig.modal.addDerivation.header'),
                                            tmplID             : 'addDerivation',
                                            tmplIcon           : 'la-thumbtack slash blue',
                                            tmplCss            : accessConfigEditable ? 'icon small la-selectable-button' : 'icon small',
                                            tmplModalID        : "derivationModal-${sp.id}",
                                            editmode           : accessConfigEditable,
                                            accessPointList    : platform.getNotActiveAccessPoints(contextOrg),
                                            institution        : contextService.getUser().getAuthorizedOrgs(),
                                            selectedInstitution: contextOrg,
                                            platformInstance   : platform,
                                            subscriptionPackage: sp
                                  ]}"/>
                      </span>
                    </g:else>
                  </g:if>
                </div>
              </div>
            </g:each>
          </g:if>
        </div>
      </td>
    </tr>
  </g:each>
</table>



