<%@ page import="de.laser.ui.Btn;de.laser.ui.Icon;de.laser.Subscription;de.laser.License;de.laser.OrgRole;de.laser.DocContext;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.properties.PropertyDefinition;de.laser.interfaces.CalculatedType;de.laser.AuditConfig;de.laser.FormService" %>
<laser:htmlStart message="license.details.label" />

        <ui:debugInfo>
            <div style="padding: 1em 0;">
                <p>lic.dateCreated: ${license.dateCreated}</p>
                <p>lic.lastUpdated: ${license.lastUpdated}</p>
                <p>lic.licenseCategory: ${license.licenseCategory}</p>

                <p>lic.licensingConsortium: ${license.licensingConsortium}</p>
                <p>lic.licensor: ${license.licensor}</p>
                <p>lic.licensee: ${license.licensee}</p>
                <p>lic.getAllLicensee(): ${license.getAllLicensee()}</p>

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

        <%--<ui:objectStatus object="${license}" />--%>

        <g:if test="${license.instanceOf && (contextService.getOrg().id == license.getLicensingConsortium()?.id)}">
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
                                    <dt class="control-label"><g:message code="altname.plural" /></dt>
                                    <dd id="altnames" class="ui accordion la-accordion-showMore la-accordion-altName" style="padding-bottom: 0">
                                        <g:if test="${license.altnames}">
                                            <div  class="ui divided middle aligned selection list la-flex-center">
                                                <div class="item title" id="altname_title" data-objId="altname-${license.altnames[0].id}">
                                                    <div class="content la-space-right">
                                                        <g:if test="${!license.altnames[0].instanceOf}">
                                                            <ui:xEditable owner="${license.altnames[0]}"
                                                                          field="name"/>
                                                        </g:if>
                                                        <g:else>
                                                            ${license.altnames[0].name}
                                                        </g:else>
                                                    </div>
                                                    <g:if test="${editable}">
                                                        <g:if test="${showConsortiaFunctions}">
                                                            <g:if test="${!license.altnames[0].instanceOf}">
                                                                <g:if test="${!AuditConfig.getConfig(license.altnames[0])}">
                                                                    <ui:link
                                                                        class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                                        controller="ajax"
                                                                        action="toggleAlternativeNameAuditConfig"
                                                                        params='[ownerId                         : "${license.id}",
                                                                                 ownerClass                      : "${license.class}",
                                                                                 showConsortiaFunctions          : true,
                                                                                 (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                        ]'
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [license.altnames[0].name])}"
                                                                        data-confirm-term-how="inherit"
                                                                        id="${license.altnames[0].id}"
                                                                        data-content="${message(code: 'property.audit.off.tooltip')}"
                                                                        role="button">
                                                                        <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                                                    </ui:link>

                                                                    <ui:remoteLink role="button"
                                                                                   class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                                   controller="ajaxJson"
                                                                                   action="removeObject"
                                                                                   params="[object: 'altname', objId: license.altnames[0].id]"
                                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [license.altnames[0].name])}"
                                                                                   data-confirm-term-how="delete"
                                                                                   data-done="JSPC.app.removeListValue('altname-${license.altnames[0].id}')">
                                                                        <i class="${Icon.CMD.DELETE}"></i>
                                                                    </ui:remoteLink>

                                                                </g:if>
                                                                <g:else>
                                                                    <ui:link
                                                                        class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                                        controller="ajax"
                                                                        action="toggleAlternativeNameAuditConfig"
                                                                        params='[ownerId                         : "${license.altnames[0].id}",
                                                                                 ownerClass                      : "${license.altnames[0].class}",
                                                                                 showConsortiaFunctions          : true,
                                                                                 (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                        ]'
                                                                        id="${license.altnames[0].id}"
                                                                        data-content="${message(code: 'property.audit.on.tooltip')}"
                                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.identifier", args: [license.altnames[0].name])}"
                                                                        data-confirm-term-how="inherit"
                                                                        role="button">
                                                                        <i class="${Icon.SIG.INHERITANCE}"></i>
                                                                    </ui:link>
                                                                    <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                        <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                    </div>
                                                                </g:else>
                                                            </g:if>
                                                            <g:else>
                                                                <ui:remoteLink role="button"
                                                                               class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                               controller="ajaxJson"
                                                                               action="removeObject"
                                                                               params="[object: 'altname', objId: license.altnames[0].id]"
                                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [license.altnames[0].name])}"
                                                                               data-confirm-term-how="delete"
                                                                               data-done="JSPC.app.removeListValue('altname-${license.altnames[0].id}')">
                                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                                </ui:remoteLink>
                                                            </g:else>
                                                        </g:if>
                                                        <g:elseif test="${license.altnames[0].instanceOf}">
                                                            <ui:auditIcon type="auto"/>
                                                        </g:elseif>
                                                        <g:else>
                                                            <ui:remoteLink role="button"
                                                                           class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                           controller="ajaxJson"
                                                                           action="removeObject"
                                                                           params="[object: 'altname', objId: license.altnames[0].id]"
                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [license.altnames[0].name])}"
                                                                           data-confirm-term-how="delete"
                                                                           data-done="JSPC.app.removeListValue('altname-${license.altnames[0].id}')">
                                                                <i class="${Icon.CMD.DELETE}"></i>
                                                            </ui:remoteLink>
                                                        </g:else>
                                                    </g:if>
                                                    <div class="${Btn.MODERN.SIMPLE_TOOLTIP} la-show-button"
                                                         data-content="${message(code: 'altname.showAll')}">
                                                        <i class="${Icon.CMD.SHOW_MORE}"></i>
                                                    </div>
                                                </div>


                                                <div class="content" style="padding:0">
                                                    <g:each in="${license.altnames.drop(1)}" var="altname">
                                                        <div class="ui item" data-objId="altname-${altname.id}">
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
                                                                        <g:if test="${!AuditConfig.getConfig(altname)}">
                                                                            <ui:link
                                                                                class="test ${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                                                controller="ajax"
                                                                                action="toggleAlternativeNameAuditConfig"
                                                                                params='[ownerId                         : "${license.id}",
                                                                                         ownerClass                      : "${license.class}",
                                                                                         showConsortiaFunctions          : true,
                                                                                         (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                ]'
                                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                                data-confirm-term-how="inherit"
                                                                                id="${altname.id}"
                                                                                data-content="${message(code: 'property.audit.off.tooltip')}"
                                                                                role="button">
                                                                                <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                                                            </ui:link>
                                                                            <ui:remoteLink role="button"
                                                                                           class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                                           controller="ajaxJson"
                                                                                           action="removeObject"
                                                                                           params="[object: 'altname', objId: altname.id]"
                                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                           data-confirm-term-how="delete"
                                                                                           data-done="JSPC.app.removeListValue('altname-${altname.id}')">
                                                                                <i class="${Icon.CMD.DELETE}"></i>
                                                                            </ui:remoteLink>
                                                                            <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                                <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                            </div>
                                                                        </g:if>
                                                                        <g:else>
                                                                            <ui:link
                                                                                class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                                                controller="ajax"
                                                                                action="toggleAlternativeNameAuditConfig"
                                                                                params='[ownerId                         : "${altname.id}",
                                                                                         ownerClass                      : "${altname.class}",
                                                                                         showConsortiaFunctions          : true,
                                                                                         (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                                ]'
                                                                                id="${altname.id}"
                                                                                data-content="${message(code: 'property.audit.on.tooltip')}"
                                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [altname.name])}"
                                                                                data-confirm-term-how="inherit"
                                                                                role="button">
                                                                                <i class="${Icon.SIG.INHERITANCE}"></i>
                                                                            </ui:link>
                                                                            <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                                <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                            </div>                                                                    <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                                            <icon:placeholder/><%-- Hidden Fake Button --%>
                                                                        </div>
                                                                        </g:else>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <div class="ui buttons">
                                                                            <ui:remoteLink role="button"
                                                                                           class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                                           controller="ajaxJson"
                                                                                           action="removeObject"
                                                                                           params="[object: 'altname', objId: altname.id]"
                                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                           data-confirm-term-how="delete"
                                                                                           data-done="JSPC.app.removeListValue('altname-${altname.id}')">
                                                                                <i class="${Icon.CMD.DELETE}"></i>
                                                                            </ui:remoteLink>
                                                                        </div>
                                                                    </g:else>
                                                                </g:if>
                                                                <g:elseif test="${altname.instanceOf}">
                                                                    <ui:auditIcon type="auto"/>
                                                                </g:elseif>
                                                                <g:else>
                                                                    <div class="ui buttons">
                                                                        <ui:remoteLink role="button"
                                                                                       class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                                       controller="ajaxJson"
                                                                                       action="removeObject"
                                                                                       params="[object: 'altname', objId: altname.id]"
                                                                                       data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                                       data-confirm-term-how="delete"
                                                                                       data-done="JSPC.app.removeListValue('altname-${altname.id}')">
                                                                            <i class="${Icon.CMD.DELETE}"></i>
                                                                        </ui:remoteLink>
                                                                    </div>
                                                                </g:else>
                                                            </g:if>
                                                            <g:elseif test="${altname.instanceOf}">
                                                                <ui:auditIcon type="auto"/>
                                                            </g:elseif>
                                                        </div>
                                                    </g:each>

                                                </div>
                                            </div>
                                        </g:if>
                                    </dd>
                                    <dd>
                                        <g:if test="${editable}">
                                            <button  data-content="${message(code: 'altname.add')}" data-objtype="altname" id="addAltname"  class="${Btn.MODERN.POSITIVE} la-js-addItem blue la-popup-tooltip">
                                                <i class="${Icon.CMD.ADD}"></i>
                                            </button>
                                        </g:if>
                                    </dd>
                                </dl>
                                <div></div>%{-- Breaks DL for a reason --}%
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

                                <g:if test="${license.instanceOf && contextService.getOrg().id == license.getLicensingConsortium().id}">
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

                    <g:if test="${license.instanceOf && !contextService.getOrg().isCustomerType_Support()}">
                        <div id="container-consortium">
                            <div class="ui card">
                                <div class="content">
                                    <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                                        <h2>${message(code: 'consortium.label')}</h2>
                                    </div>
                                    <laser:render template="/templates/links/consortiumLinksAsList"
                                                  model="${[consortium   : license.getLicensingConsortium(),
                                                            roleObject   : license,
                                                            roleRespValue: 'Specific subscription editor'
                                                  ]}"/>
                                </div>
                            </div>
                        </div>
                    </g:if>

                    <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                        <div id="container-provider">
                            <div class="ui card">
                                <div class="content">
                                    <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                                        <h2>${message(code: 'provider.label')}</h2>
                                        <laser:render template="/templates/links/providerLinksSimpleModal"
                                                      model="${[linkType: license.class.name,
                                                                parent: license.class.name + ':' + license.id,
                                                                recip_prop: 'license',
                                                                tmplEntity: message(code:'license.details.tmplEntity'),
                                                                tmplText: message(code:'license.details.tmplText'),
                                                                tmplIcon        : 'add',
                                                                tmplTooltip     : message(code:'license.details.tmplLinkProviderText'),
                                                                tmplModalID:'osel_add_modal_lizenzgeber',
                                                                editmode: editable,
                                                                tmplCss       : ''
                                                      ]}" />
                                    </div>
                                    <laser:render template="/templates/links/providerLinksAsList"
                                              model="${[providerRoles: visibleProviders,
                                                        roleObject: license,
                                                        roleRespValue: RDStore.PRS_RESP_SPEC_LIC_EDITOR.value,
                                                        editmode: editable,
                                                        showPersons: true
                                              ]}" />
                                </div>
                            </div>
                        </div>
                        <div id="container-vendor">
                            <div class="ui card">
                                <div class="content">
                                    <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                                        <h2>${message(code: 'vendor.label')}</h2>
                                        <laser:render template="/templates/links/vendorLinksSimpleModal"
                                                      model="${[linkType: license.class.name,
                                                                parent: license.class.name + ':' + license.id,
                                                                recip_prop: 'license',
                                                                tmplEntity: message(code:'license.details.linkAgency.tmplEntity'),
                                                                tmplText: message(code:'license.details.linkAgency.tmplText'),
                                                                tmplIcon        : 'add',
                                                                tmplTooltip     : message(code:'license.details.tmplLinkAgencyText'),
                                                                tmplModalID:'osel_add_modal_agency',
                                                                editmode: editable,
                                                                tmplCss       : ''
                                                      ]}" />
                                    </div>
                                    <laser:render template="/templates/links/vendorLinksAsList"
                                                  model="${[vendorRoles: visibleVendors,
                                                            roleObject: license,
                                                            roleRespValue: RDStore.PRS_RESP_SPEC_LIC_EDITOR.value,
                                                            editmode: editable,
                                                            showPersons: true
                                                  ]}" />

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
        $('.la-js-addItem').click(function() {
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
