<%@ page import="com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue" %>
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
          <button class="ui icon negative button la-selectable-button" onclick="unlinkPackage(${sp.pkg.id})">
            <i class="unlink icon"></i>
          </button>
        </g:if>
      </td>
    </tr>
    <tr>
      <td></td>
      <td colspan="2">
        <div class="ui top aligned divided relaxed list">
          <div class="item">
            <div class="right floated content">
              <b>Zugangskonfiguration</b>
            </div>
            <div class="content">
              <b>Platform</b>
            </div>
          </div>
          <g:if test="${sp.pkg.tipps}">
            <g:each in="${sp.pkg.tipps.platform.unique()}" var="platform">
              <div class="item">
                <div class="right floated content">
                  <g:each in="${sp.getAccessPointListForOrgAndPlatform(contextOrg, platform)?.collect()}" var="orgap">
                    <g:link controller="accessPoint" action="edit_${orgap.oap.accessMethod}"
                            id="${orgap.oap.id}">${orgap.oap.name} (${orgap.oap.accessMethod.getI10n('value')})</g:link>
                    <g:if test="${!platform.usesPlatformAccessPoints(contextOrg, sp)}">
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
                                        editmode           : editable,
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
                            data-content="Zugangskonfigurationen werden von Platform geerbt. Für Lizenzpaket spezifische Verknüpfungen hier klicken">
                        <g:render template="/templates/links/accessPointInheritModal"
                                  model="${[tmplText           : 'Vererbung von Platform aufheben',
                                            tmplID             : 'removeDerivation',
                                            tmplIcon           : 'thumbtack blue',
                                            tmplCss            : 'icon small la-selectable-button',
                                            tmplModalID        : "removeDerivationModal-${sp.id}",
                                            editmode           : editable,
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
                            data-content="Zugangskonfigurationen werden auf Lizenzpaket Ebene verknüpft und nicht von der Platform geerbt. Klicken, um von der Platform zu erben">
                        <g:render template="/templates/links/accessPointInheritModal"
                                  model="${[tmplText           : 'Von Platform erben',
                                            tmplID             : 'addDerivation',
                                            tmplIcon           : 'la-thumbtack slash blue',
                                            tmplCss            : 'icon small la-selectable-button',
                                            tmplModalID        : "derivationModal-${sp.id}",
                                            editmode           : editable,
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



