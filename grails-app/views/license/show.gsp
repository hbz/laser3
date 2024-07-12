<%@ page import="de.laser.helper.Icons;de.laser.Subscription;de.laser.License;de.laser.OrgRole;de.laser.DocContext;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.properties.PropertyDefinition;de.laser.interfaces.CalculatedType;de.laser.AuditConfig;de.laser.FormService" %>
<laser:htmlStart message="license.details.label" serviceInjection="true"/>

        <ui:debugInfo>
            <div style="padding: 1em 0;">
                <p>lic.dateCreated: ${license.dateCreated}</p>
                <p>lic.lastUpdated: ${license.lastUpdated}</p>
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

        <ui:h1HeaderWithIcon visibleProviders="${visibleProviders}">
            <ui:xEditable owner="${license}" field="reference" id="reference"/>
        </ui:h1HeaderWithIcon>
        <g:if test="${editable}">
            <ui:auditButton class="la-auditButton-header" auditable="[license, 'reference']" auditConfigs="${auditConfigs}" withoutOptions="true"/>
        </g:if>

        <ui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

        <laser:render template="${customerTypeService.getNavTemplatePath()}" />

        <%--<ui:objectStatus object="${license}" status="${license.status}" />--%>

        <g:if test="${license.instanceOf && (institution.id == license.getLicensingConsortium()?.id)}">
                <ui:msg class="error" header="${message(code:'myinst.message.attention')}" hideClose="true">
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
                                    <dt class="control-label"><g:message code="org.altname.label" /></dt>
                                    <dd>
                                        <div id="altnames" class="ui divided middle aligned selection list la-flex-list accordion la-accordion-showMore">
                                            <g:if test="${license.altnames}">
                                                <div class="item title" id="altname_title">
                                                    <div class="item" data-objId="${genericOIDService.getOID(license.altnames[0])}">
                                                        <div class="content la-space-right">
                                                            <g:if test="${!license.altnames[0].instanceOf}">
                                                                <ui:xEditable owner="${license.altnames[0]}" field="name"/>
                                                            </g:if>
                                                            <g:else>
                                                                ${license.altnames[0].name}
                                                            </g:else>
                                                        </div>
                                                        <g:if test="${editable}">
                                                            <g:if test="${showConsortiaFunctions}">
                                                                <g:if test="${!license.altnames[0].instanceOf}">
                                                                    <g:if test="${! AuditConfig.getConfig(license.altnames[0])}">
                                                                        <ui:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                                 controller="ajax"
                                                                                 action="toggleAlternativeNameAuditConfig"
                                                                                 params='[ownerId: "${license.id}",
                                                                                          ownerClass: "${license.class}",
                                                                                          showConsortiaFunctions: true,
                                                                                          (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                 ]'
                                                                                 data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [license.altnames[0].name])}"
                                                                                 data-confirm-term-how="inherit"
                                                                                 id="${license.altnames[0].id}"
                                                                                 data-content="${message(code:'property.audit.off.tooltip')}"
                                                                                 role="button"
                                                                        >
                                                                            <i class="icon la-thumbtack slash"></i>
                                                                        </ui:link>
                                                                        <div class="ui buttons">
                                                                            <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: license.altnames[0].id]"
                                                                                     data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [license.altnames[0].name])}"
                                                                                     data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                                <i class="${Icons.CMD.DELETE}"></i>
                                                                            </ui:remoteLink>
                                                                        </div>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <ui:link class="ui icon green button la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                                 controller="ajax" action="toggleAlternativeNameAuditConfig"
                                                                                 params='[ownerId: "${license.altnames[0].id}",
                                                                                          ownerClass: "${license.altnames[0].class}",
                                                                                          showConsortiaFunctions: true,
                                                                                          (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                 ]'
                                                                                 id="${license.altnames[0].id}"
                                                                                 data-content="${message(code:'property.audit.on.tooltip')}"
                                                                                 data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.identifier", args: [license.altnames[0].name])}"
                                                                                 data-confirm-term-how="inherit"
                                                                                 role="button"
                                                                        >
                                                                            <i class="thumbtack icon"></i>
                                                                        </ui:link>
                                                                    </g:else>
                                                                </g:if>
                                                                <g:else>
                                                                    <div class="ui buttons">
                                                                        <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: license.altnames[0].id]"
                                                                                 data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [license.altnames[0].name])}"
                                                                                 data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(license.altnames[0])}')">
                                                                            <i class="${Icons.CMD.DELETE}"></i>
                                                                        </ui:remoteLink>
                                                                    </div>
                                                                </g:else>
                                                            </g:if>
                                                            <g:elseif test="${license.altnames[0].instanceOf}">
                                                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                                            </g:elseif>
                                                            <g:else>
                                                                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: license.altnames[0].id]"
                                                                         data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [license.altnames[0].name])}"
                                                                         data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(license.altnames[0])}')">
                                                                    <i class="${Icons.CMD.DELETE}"></i>
                                                                </ui:remoteLink>
                                                            </g:else>
                                                        </g:if>
                                                    </div>
                                                    <div class="ui icon blue button la-show-button la-modern-button la-popup-tooltip la-delay"
                                                         data-content="${message(code: 'org.altname.show')}">
                                                        <i class="${Icons.CMD.SHOW_MORE}"></i>
                                                    </div>
                                                </div>
                                                <div class="content">
                                                    <g:each in="${license.altnames.drop(1)}" var="altname">
                                                        <div class="ui item" data-objId="${genericOIDService.getOID(altname)}">
                                                            <div class="content la-space-right">
                                                                <g:if test="${!altname.instanceOf}">
                                                                    <ui:xEditable owner="${altname}" field="name"/>
                                                                </g:if>
                                                                <g:else>
                                                                    ${altname.name}
                                                                </g:else>
                                                            </div>
                                                            <g:if test="${editable}">
                                                                <g:if test="${showConsortiaFunctions}">
                                                                    <g:if test="${!altname.instanceOf}">
                                                                        <g:if test="${! AuditConfig.getConfig(altname)}">
                                                                            <ui:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                                     controller="ajax"
                                                                                     action="toggleAlternativeNameAuditConfig"
                                                                                     params='[ownerId: "${license.id}",
                                                                                              ownerClass: "${license.class}",
                                                                                              showConsortiaFunctions: true,
                                                                                              (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                     ]'
                                                                                     data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                                     data-confirm-term-how="inherit"
                                                                                     id="${altname.id}"
                                                                                     data-content="${message(code:'property.audit.off.tooltip')}"
                                                                                     role="button"
                                                                            >
                                                                                <i class="icon la-thumbtack slash"></i>
                                                                            </ui:link>
                                                                            <div class="ui buttons">
                                                                                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                                         data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                         data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                                    <i class="${Icons.CMD.DELETE}"></i>
                                                                                </ui:remoteLink>
                                                                            </div>
                                                                        </g:if>
                                                                        <g:else>
                                                                            <ui:link class="ui icon green button la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                                                     controller="ajax" action="toggleAlternativeNameAuditConfig"
                                                                                     params='[ownerId: "${altname.id}",
                                                                                              ownerClass: "${altname.class}",
                                                                                              showConsortiaFunctions: true,
                                                                                              (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                     ]'
                                                                                     id="${altname.id}"
                                                                                     data-content="${message(code:'property.audit.on.tooltip')}"
                                                                                     data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                                     data-confirm-term-how="inherit"
                                                                                     role="button"
                                                                            >
                                                                                <i class="thumbtack icon"></i>
                                                                            </ui:link>
                                                                        </g:else>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <div class="ui buttons">
                                                                            <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                                     data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                     data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                                <i class="${Icons.CMD.DELETE}"></i>
                                                                            </ui:remoteLink>
                                                                        </div>
                                                                    </g:else>
                                                                </g:if>
                                                                <g:elseif test="${altname.instanceOf}">
                                                                    <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                                                </g:elseif>
                                                                <g:else>
                                                                    <div class="ui buttons">
                                                                        <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                                 data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                 data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                            <i class="${Icons.CMD.DELETE}"></i>
                                                                        </ui:remoteLink>
                                                                    </div>
                                                                </g:else>
                                                            </g:if>
                                                            <g:elseif test="${altname.instanceOf}">
                                                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                                            </g:elseif>
                                                        </div>
                                                    </g:each>
                                                </div>
                                            </g:if>
                                        </div>
                                    </dd>
                                </dl>
                                <g:if test="${editable}">
                                    <dl>
                                        <dt></dt>
                                        <dd><input name="addAltname" id="addAltname" type="button" class="ui button addListValue" data-objtype="altname" value="${message(code: 'org.altname.add')}"></dd>
                                    </dl>
                                </g:if>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.startDate.label')}</dt>
                                    <dd>
                                        <ui:xEditable owner="${license}" type="date" field="startDate" validation="datesCheck" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd><ui:auditButton auditable="[license, 'startDate']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.endDate.label')}</dt>
                                    <dd>
                                        <ui:xEditable owner="${license}" type="date" field="endDate" validation="datesCheck" />
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd><ui:auditButton auditable="[license, 'endDate']" auditConfigs="${auditConfigs}" /></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.openEnded.label')}</dt>
                                    <dd>
                                        <ui:xEditableRefData owner="${license}" field="openEnded" config="${RDConstants.Y_N_U}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd><ui:auditButton auditable="[license, 'openEnded']" auditConfigs="${auditConfigs}" /></dd>
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
                                        <dd><ui:auditButton auditable="[license, 'status']" auditConfigs="${auditConfigs}"/></dd>
                                    </g:if>
                                </dl>
                                <dl>
                                    <dt><label class="control-label">${message(code:'license.licenseCategory.label')}</label></dt>
                                    <dd>
                                        <ui:xEditableRefData owner="${license}" field="licenseCategory" config="${RDConstants.LICENSE_CATEGORY}"/>
                                    </dd>
                                    <g:if test="${editable}">
                                        <dd><ui:auditButton auditable="[license, 'licenseCategory']" auditConfigs="${auditConfigs}"/></dd>
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
                                            <dd><ui:auditButton auditable="[license, 'isPublicForApi']" auditConfigs="${auditConfigs}"/></dd>
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
                                    <h2 class="ui header">${message(code: 'provider.label')}</h2>
                                    <laser:render template="/templates/links/providerLinksAsList"
                                              model="${[providerRoles: visibleProviders,
                                                        roleObject: license,
                                                        roleRespValue: 'Specific license editor',
                                                        editmode: editable,
                                                        showPersons: true
                                              ]}" />

                                    <div class="ui la-vertical buttons">
                                        <laser:render template="/templates/links/providerLinksSimpleModal"
                                                  model="${[linkType: license.class.name,
                                                            parent: license.class.name + ':' + license.id,
                                                            recip_prop: 'license',
                                                            tmplEntity: message(code:'license.details.tmplEntity'),
                                                            tmplText: message(code:'license.details.tmplText'),
                                                            tmplButtonText: message(code:'license.details.tmplLinkProviderText'),
                                                            tmplModalID:'osel_add_modal_lizenzgeber',
                                                            editmode: editable
                                                  ]}" />

                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="container-vendor">
                            <div class="ui card">
                                <div class="content">
                                    <h2 class="ui header">${message(code: 'vendor.label')}</h2>
                                    <laser:render template="/templates/links/vendorLinksAsList"
                                                  model="${[vendorRoles: visibleVendors,
                                                            roleObject: license,
                                                            roleRespValue: 'Specific license editor',
                                                            editmode: editable,
                                                            showPersons: true
                                                  ]}" />
                                    <div class="ui la-vertical buttons">
                                        <laser:render template="/templates/links/vendorLinksSimpleModal"
                                                      model="${[linkType: license.class.name,
                                                                parent: license.class.name + ':' + license.id,
                                                                recip_prop: 'license',
                                                                tmplEntity: message(code:'license.details.linkAgency.tmplEntity'),
                                                                tmplText: message(code:'license.details.linkAgency.tmplText'),
                                                                tmplButtonText: message(code:'license.details.tmplLinkAgencyText'),
                                                                tmplModalID:'osel_add_modal_agency',
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
        $('.addListValue').click(function() {
            let url;
            let returnSelector;
            switch($(this).attr('data-objtype')) {
                case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: genericOIDService.getOID(license)]"/>';
                    returnSelector = '#altnames';
                    break;
            }

            $.ajax({
                url: url,
                success: function(result) {
                    $(returnSelector).append(result);
                    r2d2.initDynamicUiStuff(returnSelector);
                    r2d2.initDynamicXEditableStuff(returnSelector);
                }
            });
        });
        JSPC.app.removeListValue = function(objId) {
            $("div[data-objId='"+objId+"']").remove();
        }
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
