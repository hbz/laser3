<%@ page import="de.laser.Subscription;de.laser.License;de.laser.DocContext;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.properties.PropertyDefinition;de.laser.interfaces.CalculatedType" %>
<!doctype html>
<%-- r:require module="annotations" / --%>
<laser:serviceInjection />
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'license.details.label')}</title>
  </head>

    <body>

        <semui:debugInfo>
            <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
            %{--<g:render template="/templates/debug/orgRoles"  model="[debug: license.orgRelations]" />--}%
            %{--<g:render template="/templates/debug/prsRoles"  model="[debug: license.prsLinks]" />--}%
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

        <%--<g:if test="${institution.id == license.getLicensingConsortium()?.id || (! license.getLicensingConsortium() && institution.id == license.getLicensee()?.id)}">
            <g:render template="/templates/pendingChanges" model="${['pendingChanges':pendingChanges, 'flash':flash, 'model':license]}"/>
        </g:if>--%>

        <div class="ui stackable grid">

            <div class="ten wide column">
                <%--semui:errors bean="${titleInstanceInstance}" /--%>

                <!--<h4 class="ui header">${message(code:'license.details.information')}</h4>-->

                <div class="la-inline-lists">
                    <div class="ui two doubling stackable cards">
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <dt class="control-label">${message(code: 'license.startDate.label')}</dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="startDate" validation="datesCheck" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'startDate']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.endDate.label')}</dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="endDate" validation="datesCheck" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'endDate']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.openEnded.label')}</dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="openEnded" config="${RDConstants.Y_N_U}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'openEnded']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                            </div>
                        </div>
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.status.label')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="status" config="${RDConstants.LICENSE_STATUS}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'status']" auditConfigs="${auditConfigs}"/></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.licenseCategory.label')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="licenseCategory" config="${RDConstants.LICENSE_CATEGORY}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'licenseCategory']" auditConfigs="${auditConfigs}"/></dd>
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
                                        <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'isPublicForApi']" auditConfigs="${auditConfigs}"/></dd>
                                    </g:if>
                                </dl>

                            </div>
                        </div>
                    </div>

                <div id="new-dynamic-properties-block">
                    <g:render template="properties" model="${[ license: license ]}" />
                </div><!-- #new-dynamic-properties-block -->

                </div>

                <div class="clearfix"></div>

            </div><!-- .twelve -->

            <aside class="six wide column la-sidekick">
                <div class="ui one cards">

                    <div id="container-provider">
                        <div class="ui card ">
                            <div class="content">
                                <h2 class="ui header">${message(code: 'license.details.tmplEntity')}</h2>
                                <g:render template="/templates/links/orgLinksAsList"
                                          model="${[roleLinks: visibleOrgRelations,
                                                    roleObject: license,
                                                    roleRespValue: 'Specific license editor',
                                                    editmode: editable,
                                                    showPersons: true
                                          ]}" />

                                <g:render template="/templates/links/orgLinksSimpleModal"
                                          model="${[linkType: license.class.name,
                                                    parent: license.class.name + ':' + license.id,
                                                    property: 'orgRelations',
                                                    recip_prop: 'lic',
                                                    tmplRole: RDStore.OR_LICENSOR,
                                                    tmplEntity: message(code:'license.details.tmplEntity'),
                                                    tmplText: message(code:'license.details.tmplText'),
                                                    tmplButtonText: message(code:'license.details.tmplButtonText'),
                                                    tmplModalID:'osel_add_modal_lizenzgeber',
                                                    tmplType: RDStore.OT_LICENSOR,
                                                    editmode: editable
                                          ]}" />
                            </div>
                        </div>
                    </div>
                    <div id="container-links">
                        <div class="ui card" id="links"></div>
                    </div>
                    <g:render template="/templates/aside1" model="${[ownobj:license, owntp:'license']}" />
                </div>
            </aside><!-- .four -->

        </div><!-- .grid -->
    <laser:script file="${this.getGroovyPageFileName()}">
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="getLinks" />",
                data: {
                    entry:"${genericOIDService.getOID(license)}"
                }
            }).done(function(response){
                $("#links").html(response);
                r2d2.initDynamicSemuiStuff('#links');
            })
    </laser:script>
  </body>
</html>
