<%@ page import="com.k_int.kbplus.ApiSource;com.k_int.kbplus.Subscription; com.k_int.kbplus.SubscriptionPackage; com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>

<table class="ui three column table">
  <tr>
    <th scope="row" rowspan="0"
        class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.packages.label')}</th></tr>
  <g:each in="${subscriptionInstance.packages}" var="sp">
    <%
      Map<String,Object> packageMetadata = [:]
      String link
      ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true).each { api ->
        packageMetadata = GOKbService.geElasticsearchFindings(api.baseUrl+api.fixToken, "&uuid=${sp.pkg.gokbId}", "Package", null, 1)
        link = api.editUrl+"/resource/show/"
        if(packageMetadata.warning)
          packageMetadata = packageMetadata.warning
        else if(packageMetadata.info)
          packageMetadata = packageMetadata.info
      }
    %>
    <g:set var="cssId" value="oapLinksModal-${sp.id}"/>
    <tr>
      <td>
        <g:link controller="package" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link>

        <g:if test="${sp.pkg.contentProvider}">
          (${sp.pkg.contentProvider.name})
        </g:if>
        <g:if test="${packageMetadata.records.size() > 0 && link}">
          <p>
            <em><g:message code="subscription.packages.curatoryGroups"/>
              <ul>
                <g:each in="${packageMetadata.records.get(0).curatoryGroups}" var="curatoryGroup">
                  <li><a href="${link}">${curatoryGroup}</a></li>
                </g:each>
              </ul>
            </em>
          </p>
        </g:if>
      </td>
      <td class="right aligned">
        <g:if test="${editmode}">

          <g:link controller="subscription"
                    action="unlinkPackage"
                    extaContentFlag="false"
                    params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y']}"
                    data-confirm-messageUrl="${createLink(controller:'subscription', action:'unlinkPackage', params:[subscription: sp.subscription.id, package: sp.pkg.id])}"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name])}"
                    data-confirm-term-how="delete"
                    class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
                    role="button">
              <i aria-hidden="true" class="trash alternate icon"></i>
            </g:link>
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
                    <semui:linkIcon href="${platform.primaryUrl?.startsWith('http') ? platform.primaryUrl : 'http://' + platform.primaryUrl}"/>
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



