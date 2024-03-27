<%@ page import="de.laser.Subscription;de.laser.License;de.laser.OrgRole;de.laser.DocContext;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.properties.PropertyDefinition;de.laser.interfaces.CalculatedType" %>
<laser:htmlStart message="license.details.label" serviceInjection="true"/>

        <ui:debugInfo>
            <div style="padding: 1em 0;">
                <p>lic.licenseCategory: ${license.licenseCategory}</p>

                <p>lic.licensingConsortium: ${license.licensingConsortium}</p>
                <p>lic.licensor: ${license.licensor}</p>
                <p>lic.licensee: ${license.licensee}</p>

                <p>lic.instanceOf: <g:if test="${license.instanceOf}">
                    <g:link action="show" id="${license.instanceOf.id}">${license.instanceOf.reference}</g:link>
                </g:if></p>

                <p>getCalculatedType(): ${license._getCalculatedType()}</p>
                <p>orgRole(ctxOrg): ${OrgRole.findAllByLicAndOrg(license, contextService.getOrg()).roleType.join(', ')}</p>
            </div>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
        </ui:debugInfo>

        <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

        <ui:controlButtons>
            <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon visibleOrgRelations="${visibleOrgRelations}">
            <ui:xEditable owner="${license}" field="reference" id="reference"/>
        </ui:h1HeaderWithIcon>
        <g:if test="${editable}">
            <ui:auditButton class="la-auditButton-header" auditable="[license, 'reference']" auditConfigs="${auditConfigs}" withoutOptions="true"/>
        </g:if>

        <ui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

        <laser:render template="${customerTypeService.getNavTemplatePath()}" />

        <%--<ui:objectStatus object="${license}" status="${license.status}" />--%>

        <g:if test="${license.instanceOf && (institution.id == license.getLicensingConsortium()?.id)}">
                <ui:msg class="negative" header="${message(code:'myinst.message.attention')}" noClose="true">
                    <g:message code="myinst.licenseDetails.message.ChildView" />
                    <g:message code="myinst.licenseDetails.message.ConsortialView" />
                    <g:link controller="license" action="show" id="${license.instanceOf.id}">
                        <g:message code="myinst.subscriptionDetails.message.here" />
                    </g:link>.
                </ui:msg>
        </g:if>

        <laser:render template="/templates/meta/identifier" model="${[object: license, editable: editable]}" />

        <ui:messages data="${flash}" />
        <laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" />

        <div class="ui stackable grid">

            <div class="eleven wide column">
                <%--ui:errors bean="${titleInstanceInstance}" /--%>

                <!--<h4 class="ui header">${message(code:'license.details.information')}</h4>-->

                <div class="la-inline-lists">
                    <div class="ui two doubling stackable cards">
                        <div class="ui card">
                            <div class="content">
                                <dl>
                                    <dt class="control-label">${message(code: 'license.startDate.label')}</dt>
                                    <dd>
                                        <ui:xEditable owner="${license}" type="date" field="startDate" validation="datesCheck" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><ui:auditButton auditable="[license, 'startDate']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.endDate.label')}</dt>
                                    <dd>
                                        <ui:xEditable owner="${license}" type="date" field="endDate" validation="datesCheck" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><ui:auditButton auditable="[license, 'endDate']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.openEnded.label')}</dt>
                                    <dd>
                                        <ui:xEditableRefData owner="${license}" field="openEnded" config="${RDConstants.Y_N_U}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><ui:auditButton auditable="[license, 'openEnded']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                            </div>
                        </div>
                        <div class="ui card">
                            <div class="content">
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.status.label')}</label></dt>
                                    <dd>
                                        <ui:xEditableRefData owner="${license}" field="status" config="${RDConstants.LICENSE_STATUS}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><ui:auditButton auditable="[license, 'status']" auditConfigs="${auditConfigs}"/></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.licenseCategory.label')}</label></dt>
                                    <dd>
                                        <ui:xEditableRefData owner="${license}" field="licenseCategory" config="${RDConstants.LICENSE_CATEGORY}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd class="la-js-editmode-container"><ui:auditButton auditable="[license, 'licenseCategory']" auditConfigs="${auditConfigs}"/></dd>
                                    </g:if>
                                </dl>

                                <g:if test="${license.instanceOf && institution.id == license.getLicensingConsortium().id}">
                                    <dl>
                                        <dt class="control-label">${message(code:'license.linktoLicense')}</dt>
                                        <g:link controller="license" action="show" id="${license.instanceOf.id}">${license.instanceOf}</g:link>
                                    </dl>
                                </g:if>

                                <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                                    <dl>
                                        <dt class="control-label">${message(code: 'license.isPublicForApi.label')}</dt>
                                        <dd><ui:xEditableBoolean owner="${license}" field="isPublicForApi" /></dd>
                                        <g:if test="${editable}">
                                            <dd class="la-js-editmode-container"><ui:auditButton auditable="[license, 'isPublicForApi']" auditConfigs="${auditConfigs}"/></dd>
                                        </g:if>
                                    </dl>
                                </g:if>

                            </div>
                        </div>
                    </div>

                <div id="new-dynamic-properties-block">
                    <laser:render template="properties" model="${[ license: license ]}" />
                </div><!-- #new-dynamic-properties-block -->

                </div>

                <div class="clearfix"></div>

            </div><!-- .eleven -->

            <aside class="five wide column la-sidekick">
                <div class="ui one cards">

                    <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                        <div id="container-provider">
                            <div class="ui card">
                                <div class="content">
                                    <h2 class="ui header">${message(code: 'license.details.tmplEntity')}</h2>
                                    <laser:render template="/templates/links/orgLinksAsList"
                                              model="${[roleLinks: visibleOrgRelations,
                                                        roleObject: license,
                                                        roleRespValue: 'Specific license editor',
                                                        editmode: editable,
                                                        showPersons: true
                                              ]}" />

                                    <div class="ui la-vertical buttons la-js-hide-this-card">
                                        <laser:render template="/templates/links/orgLinksSimpleModal"
                                                  model="${[linkType: license.class.name,
                                                            parent: license.class.name + ':' + license.id,
                                                            property: 'orgRelations',
                                                            recip_prop: 'lic',
                                                            tmplRole: RDStore.OR_LICENSOR,
                                                            tmplEntity: message(code:'license.details.tmplEntity'),
                                                            tmplText: message(code:'license.details.tmplText'),
                                                            tmplButtonText: message(code:'license.details.tmplLinkProviderText'),
                                                            tmplModalID:'osel_add_modal_lizenzgeber',
                                                            tmplType: RDStore.OT_LICENSOR,
                                                            editmode: editable
                                                  ]}" />

                                        <laser:render template="/templates/links/orgLinksSimpleModal"
                                                  model="${[linkType: license.class.name,
                                                            parent: license.class.name + ':' + license.id,
                                                            property: 'orgRelations',
                                                            recip_prop: 'lic',
                                                            tmplRole: RDStore.OR_AGENCY,
                                                            tmplEntity: message(code:'license.details.linkAgency.tmplEntity'),
                                                            tmplText: message(code:'license.details.linkAgency.tmplText'),
                                                            tmplButtonText: message(code:'license.details.tmplLinkAgencyText'),
                                                            tmplModalID:'osel_add_modal_agency',
                                                            tmplType: RDStore.OT_AGENCY,
                                                            editmode: editable
                                                  ]}" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </g:if>

                    <div id="container-links">
                        <div class="ui card" id="links"></div>
                    </div>
                    <laser:render template="/templates/sidebar/aside" model="${[ownobj:license, owntp:'license']}" />
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
                r2d2.initDynamicUiStuff('#links');
            })
    </laser:script>
<laser:htmlEnd />
