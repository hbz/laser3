<%@ page import="de.laser.ApiSource; de.laser.helper.RDConstants; de.laser.Platform" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'platform.label')}"/>
    <title>${message(code: 'laser')} : <g:message code="platform.details"/></title>
</head>

<body>

<semui:modeSwitch controller="platform" action="show" params="${params}"/>

<semui:breadcrumbs>
    <semui:crumb controller="platform" action="index" message="platform.show.all"/>
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>

    <g:if test="${editable}"><span id="platformNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax"
                                                           action="editableSetValue"/>'>${platformInstance.name}</span>
    </g:if>
    <g:else>${platformInstance.name}</g:else>
</h1>

<semui:messages data="${flash}"/>

<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="sixteen wide column">
        <div class="la-inline-lists">
          <div class="ui two stackable cards">
            <div class="ui card la-time-card">
              <div class="content">
                <dl>
                  <dt>${message(code: 'platform.name')}</dt>
                  <dd><semui:xEditable owner="${platformInstance}" field="name"/></dd>
                </dl>
                <dl>
                  <dt>GOKb ID</dt>
                  <dd>
                    ${platformInstance?.gokbId}
                    <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                            var="gokbAPI">
                      <g:if test="${platformInstance?.gokbId}">
                        <a target="_blank"
                           href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/resource/show/' + platformInstance?.gokbId : '#'}"><i
                            title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                      </g:if>
                    </g:each>
                  </dd>
                </dl>
                <dl>
                  <dt>${message(code: 'platform.org')}</dt>
                  <dd>
                    <g:if test="${platformInstance.org}">
                      <g:link controller="organisation" action="show"
                              id="${platformInstance.org.id}">${platformInstance.org.name}</g:link>
                    </g:if>
                  </dd>
                </dl>
              </div>
            </div>
            <div class="ui card">
              <div class="content">
                <dl>
                  <dt>${message(code: 'platform.primaryUrl', default: 'Primary URL')}</dt>
                  <dd>
                    <semui:xEditable owner="${platformInstance}" field="primaryUrl"/>
                    <g:if test="${platformInstance?.primaryUrl}">
                      <a role="button" class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                         data-content="${message(code: 'tipp.tooltip.callUrl')}"
                         href="${platformInstance?.primaryUrl?.contains('http') ? platformInstance?.primaryUrl : 'http://' + platformInstance?.primaryUrl}"
                         target="_blank"><i class="share square icon"></i></a>
                    </g:if>
                  </dd>
                </dl>
                <dl>
                  <dt>${message(code: 'platform.serviceProvider')}</dt>
                  <dd><semui:xEditableRefData owner="${platformInstance}" field="serviceProvider" config="${RDConstants.Y_N}"/></dd>
                </dl>
                <dl>
                  <dt>${message(code: 'platform.softwareProvider')}</dt>
                  <dd><semui:xEditableRefData owner="${platformInstance}" field="softwareProvider" config="${RDConstants.Y_N}"/></dd>
                </dl>
                <g:if test="${params.mode == 'advanced'}">
                  <dl>
                    <dt>${message(code: 'default.type.label')}</dt>
                    <dd><semui:xEditableRefData owner="${platformInstance}" field="type" config="${RDConstants.Y_N_O}"/></dd>
                  </dl>
                  <dl>
                    <dt>${message(code: 'default.status.label')}</dt>
                    <dd><semui:xEditableRefData owner="${platformInstance}" field="status"
                                                config="${RDConstants.USAGE_STATUS}"/></dd>
                  </dl>
                  <dl>
                    <dt><g:message code="platform.globalUID.label" default="Global UID"/></dt>
                    <dd><g:fieldValue bean="${platformInstance}" field="globalUID"/></dd>
                  </dl>
                </g:if>
              </div>
            </div>
          </div>
            <div id="new-dynamic-properties-block">
                <g:render template="properties" model="${[ platform: platformInstance ]}"/>
            </div><!-- #new-dynamic-properties-block -->

            <div class="ui card">
                <div class="content">
                    <table class="ui three column table">
                        <g:each in="${orgAccessPointList}" var="orgAccessPoint">
                            <tr>
                                <th scope="row" class="control-label la-js-dont-hide-this-card">${message(code: 'platform.accessPoint')}</th>
                                <td>
                                    <g:link controller="accessPoint" action="edit_${orgAccessPoint.oap.accessMethod}"  id="${orgAccessPoint.oap.id}">
                                        ${orgAccessPoint.oap.name}  (${orgAccessPoint.oap.accessMethod.getI10n('value')})
                                    </g:link>
                                </td>
                                <td class="right aligned">
                                <g:if test="${editable}">
                                    <g:link class="ui negative icon button button js-open-confirm-modal" controller="accessPoint" action="unlinkPlatform" id="${orgAccessPoint.id}"
                                            data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform', args: [orgAccessPoint.oap.name, platformInstance.name])}"
                                            data-confirm-term-how="unlink"
                                    >
                                        <i class="unlink icon"></i>
                                    </g:link>
                                </g:if>

                                </td>
                            </tr>
                        </g:each>
                    </table>


                    <div class="ui la-vertical buttons">
                        <g:render template="/templates/links/accessPointLinksModal"
                                  model="${[tmplText:message(code:'platform.link.accessPoint.button.label'),
                                            tmplID:'addLink',
                                            tmplButtonText:message(code:'platform.link.accessPoint.button.label'),
                                            tmplModalID:'platf_link_ap',
                                            editmode: editable,
                                            accessPointList: accessPointList,
                                            institution:institution,
                                            selectedInstitution:selectedInstitution
                                  ]}" />
                    </div>
                </div>
            </div>

            <div class="clearfix"></div>
        </div>
    </div>
</div>

</body>
</html>
