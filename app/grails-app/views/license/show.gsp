<%@ page import="com.k_int.kbplus.License;com.k_int.kbplus.Subscription;de.laser.helper.RDStore;de.laser.helper.RDConstants;com.k_int.properties.PropertyDefinition;de.laser.interfaces.CalculatedType;com.k_int.kbplus.GenericOIDService" %>
<!doctype html>
<%-- r:require module="annotations" / --%>
<laser:serviceInjection />

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
     <g:javascript src="properties.js"/>
    <title>${message(code:'laser')} : ${message(code:'license.details.label')}</title>
  </head>

    <body>

        <semui:debugInfo>
            <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
            <g:render template="/templates/debug/orgRoles"  model="[debug: license.orgLinks]" />
            <g:render template="/templates/debug/prsRoles"  model="[debug: license.prsLinks]" />
        </semui:debugInfo>

        <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            <semui:xEditable owner="${license}" field="reference" id="reference"/>
        </h1>

        <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

        <g:render template="nav" />

        <%--<semui:objectStatus object="${license}" status="${license.status}" />--%>

        <g:if test="${license.instanceOf && (institution.id == license.getLicensingConsortium()?.id)}">
            <div class="ui negative message">
                <div class="header"><g:message code="myinst.message.attention" /></div>
                <p>
                    <g:message code="myinst.licenseDetails.message.ChildView" />
                    <g:message code="myinst.licenseDetails.message.ConsortialView" />
                    <g:link controller="license" action="show" id="${license.instanceOf.id}">
                        <g:message code="myinst.subscriptionDetails.message.here" />
                    </g:link>.
                </p>
            </div>
        </g:if>

        <g:render template="/templates/meta/identifier" model="${[object: license, editable: editable]}" />

        <semui:messages data="${flash}" />

        <g:if test="${institution.id == license.getLicensingConsortium()?.id || (! license.getLicensingConsortium() && institution.id == license.getLicensee()?.id)}">
            <g:render template="/templates/pendingChanges" model="${['pendingChanges':pendingChanges, 'flash':flash, 'model':license]}"/>
        </g:if>

        <div class="ui stackable grid">

            <div class="twelve wide column">
                <%--semui:errors bean="${titleInstanceInstance}" /--%>

                <!--<h4 class="ui header">${message(code:'license.details.information')}</h4>-->

                <div class="la-inline-lists">
                    <div class="ui two stackable cards">
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <dt class="control-label">${message(code: 'license.startDate')}</dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="startDate" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'startDate']" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.endDate')}</dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="endDate" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'endDate']" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.openEnded')}</dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="openEnded" config="${RDConstants.Y_N_U}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'openEnded']" /></dd>
                                    </g:if>
                                </dl>
                            </div>
                        </div>
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.status')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="status" config="${RDConstants.LICENSE_STATUS}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'status']"/></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.licenseCategory')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="licenseCategory" config="${RDConstants.LICENSE_CATEGORY}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'licenseCategory']"/></dd>
                                    </g:if>
                                </dl>

                                <g:if test="${license.instanceOf && institution.id == license.getLicensingConsortium().id}">
                                    <dl>
                                        <dt class="control-label">${message(code:'license.linktoLicense')}</dt>

                                        <g:link controller="license" action="show" id="${license.instanceOf.id}">${license.instanceOf}</g:link>
                                    </dl>
                                    <%--<dl>
                                        <dt class="control-label">
                                            ${message(code:'license.details.linktoLicense.pendingChange')}
                                        </dt>
                                        <dd>
                                            <semui:xEditableBoolean owner="${license}" field="isSlaved" />
                                        </dd>
                                    </dl>--%>
                                </g:if>

                                <dl>
                                    <dt class="control-label">${message(code: 'license.isPublicForApi.label')}</dt>
                                    <dd><semui:xEditableBoolean owner="${license}" field="isPublicForApi" /></dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'isPublicForApi']"/></dd>
                                    </g:if>
                                </dl>

                            </div>
                        </div>
                    </div>

                    <div class="ui card">
                        <div class="content">
                            <h5 class="ui header">
                                <g:message code="license.details.linksHeader"/>
                            </h5>
                            <g:if test="${links.entrySet()}">
                                <table class="ui three column table">
                                    <g:each in="${links.entrySet().toSorted()}" var="linkTypes">
                                        <g:if test="${linkTypes.getValue().size() > 0 && linkTypes.getKey() != GenericOIDService.getOID(RDStore.LINKTYPE_LICENSE)}">
                                            <g:each in="${linkTypes.getValue()}" var="link">
                                                <tr>
                                                    <th scope="row" class="control-label la-js-dont-hide-this-card">${linkTypes.getKey()}</th>
                                                    <td>
                                                        <g:set var="pair" value="${link.getOther(license)}"/>
                                                        <g:if test="${pair instanceof License}">
                                                            <g:link controller="license" action="show" id="${pair.id}">
                                                                ${pair.reference}
                                                            </g:link>
                                                        </g:if>
                                                        <g:elseif test="${pair instanceof Subscription}">
                                                            <g:link controller="subscription" action="show" id="${pair.id}">
                                                                ${pair.name}
                                                            </g:link>
                                                        </g:elseif><br>
                                                        <g:formatDate date="${pair.startDate}" format="${message(code:'default.date.format.notime')}"/>-<g:formatDate date="${pair.endDate}" format="${message(code:'default.date.format.notime')}"/><br>
                                                        <g:set var="comment" value="${com.k_int.kbplus.DocContext.findByLink(link)}"/>
                                                        <g:if test="${comment}">
                                                            <em>${comment.owner.content}</em>
                                                        </g:if>
                                                    </td>
                                                    <td class="right aligned">
                                                        <g:render template="/templates/links/subLinksModal"
                                                                  model="${[tmplText               :message(code:'license.details.editLink'),
                                                                            tmplIcon               :'write',
                                                                            tmplCss                : 'icon la-selectable-button',
                                                                            tmplID                 :'editLink',
                                                                            tmplModalID            :"sub_edit_link_${link.id}",
                                                                            editmode               : editable,
                                                                            context                : license,
                                                                            subscriptionLicenseLink: true,
                                                                            link                   : link
                                                                  ]}" />
                                                        <g:if test="${editable}">
                                                            <g:link class="ui negative icon button la-selectable-button js-open-confirm-modal"
                                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.license.license")}"
                                                                    data-confirm-term-how="unlink"
                                                                    controller="ajax" action="delete" params='[cmd: "deleteLink", oid: "${link.class.name}:${link.id}"]'>
                                                                <i class="unlink icon"></i>
                                                            </g:link>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                            </g:each>
                                        </g:if>
                                    </g:each>
                                </table>
                            </g:if>
                            <g:else>
                                <p>
                                    <g:message code="license.details.noLink"/>
                                </p>
                            </g:else>
                            <div class="ui la-vertical buttons">
                                <g:render template="/templates/links/subLinksModal"
                                          model="${[tmplText:message(code:'license.details.addLink'),
                                                    tmplID:'addLink',
                                                    tmplButtonText:message(code:'license.details.addLink'),
                                                    tmplModalID:'sub_add_link',
                                                    editmode: editable,
                                                    subscriptionLicenseLink: true,
                                                    context: license
                                          ]}" />
                            </div>
                        </div>
                    </div>

                    <div class="ui card">
                        <div class="content">

                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks: visibleOrgLinks,
                                            roleObject: license,
                                            roleRespValue: 'Specific license editor',
                                            editmode: editable,
                                            showPersons: true
                                  ]}" />

                        <g:render template="/templates/links/orgLinksSimpleModal"
                                  model="${[linkType: license.class.name,
                                            parent: license.class.name + ':' + license.id,
                                            property: 'orgLinks',
                                            recip_prop: 'lic',
                                            tmplRole: RDStore.OR_LICENSOR,
                                            tmplEntity: message(code:'license.details.tmplEntity'),
                                            tmplText: message(code:'license.details.tmplText'),
                                            tmplButtonText: message(code:'license.details.tmplButtonText'),
                                            tmplModalID:'osel_add_modal_lizenzgeber',
                                            editmode: editable,
                                            orgList: availableLicensorList,
                                            signedIdList: existingLicensorIdList
                                  ]}" />
                        </div>
                    </div>

                    <div id="new-dynamic-properties-block">

                        <g:render template="properties" model="${[
                                license: license,
                                authorizedOrgs: authorizedOrgs
                        ]}" />

                    </div><!-- #new-dynamic-properties-block -->

                </div>

                <div class="clearfix"></div>

            </div><!-- .twelve -->

            <aside class="four wide column la-sidekick">
                <g:render template="/templates/aside1" model="${[ownobj:license, owntp:'license']}" />
            </aside><!-- .four -->


        </div><!-- .grid -->
  </body>
</html>
