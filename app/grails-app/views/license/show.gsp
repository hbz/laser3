<%@ page import="de.laser.helper.RDStore;de.laser.helper.RDConstants;com.k_int.properties.PropertyDefinition;de.laser.interfaces.CalculatedType" %>
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

        <g:render template="nav" />

        <semui:objectStatus object="${license}" status="${license.status}" />

        <g:if test="${license.instanceOf && (institution?.id == license.getLicensingConsortium()?.id)}">
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
                                    <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'startDate']" /></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'license.endDate')}</dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="endDate" />
                                    </dd>
                                    <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'endDate']" /></dd>
                                </dl>
                            </div>
                        </div>
                        <div class="ui card ">
                            <div class="content">
                                <dl>

                                    <dt><label class="control-label" for="licenseCategory">${message(code:'license.licenseCategory', default:'License Category')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="licenseCategory" config="${RDConstants.LICENSE_CATEGORY}"/>
                                    </dd>
                                </dl>

                                <g:if test="${license.instanceOf && institution.id == license.getLicensingConsortium().id}">
                                    <dl>
                                        <dt class="control-label">${message(code:'license.linktoLicense')}</dt>

                                        <g:link controller="license" action="show" id="${license.instanceOf.id}">${license.instanceOf}</g:link>
                                    </dl>
                                    <dl>
                                        <dt class="control-label">
                                            ${message(code:'license.details.linktoLicense.pendingChange')}
                                        </dt>
                                        <dd>
                                            <semui:xEditableBoolean owner="${license}" field="isSlaved" />
                                        </dd>
                                    </dl>
                                </g:if>

                                <dl>
                                    <dt class="control-label">${message(code: 'license.isPublicForApi.label')}</dt>
                                    <dd><semui:xEditableBoolean owner="${license}" field="isPublicForApi" /></dd>
                                    <dd class="la-js-editmode-container"><semui:auditButton auditable="[license, 'isPublicForApi']"/></dd>
                                </dl>

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
