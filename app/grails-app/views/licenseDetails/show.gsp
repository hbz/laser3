<%@ page import="de.laser.helper.RDStore; com.k_int.properties.PropertyDefinition" %>
<!doctype html>
<r:require module="annotations" />
<laser:serviceInjection />

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
     <g:javascript src="properties.js"/>
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="license" default="License"/></title>
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

        <h1 class="ui left aligned icon header"><semui:headerIcon />
            <g:if test="${license.type?.value == 'Template'}">${message(code:'license.label')} (${license.type.getI10n('value')}):</g:if>
            <semui:xEditable owner="${license}" field="reference" id="reference"/>
        </h1>

        <g:render template="nav" />

        <semui:objectStatus object="${license}" status="${license.status}" />

        <g:if test="${! license.hasTemplate() && license.instanceOf && (contextOrg?.id == license.getLicensingConsortium()?.id)}">
            <div class="ui negative message">
                <div class="header"><g:message code="myinst.message.attention" /></div>
                <p>
                    <g:message code="myinst.licenseDetails.message.ChildView" />
                    <g:each in="${license.getAllLicensee()?.collect{itOrg -> itOrg.getDesignation()}}" var="licensee">
                        <span class="ui label">${licensee}</span>,
                    </g:each>

                    <g:message code="myinst.licenseDetails.message.ConsortialView" />
                    <g:link controller="licenseDetails" action="show" id="${license.instanceOf?.id}">
                        <g:message code="myinst.subscriptionDetails.message.here" />
                    </g:link>.

                </p>
            </div>
        </g:if>

        <g:render template="/templates/meta/identifier" model="${[object: license, editable: editable]}" />

        <semui:messages data="${flash}" />

        <g:if test="${contextOrg.id == license.getLicensingConsortium()?.id || (! license.getLicensingConsortium() && contextOrg.id == license.getLicensee()?.id)}">
            <g:render template="/templates/pendingChanges" model="${['pendingChanges':pendingChanges, 'flash':flash, 'model':license]}"/>
        </g:if>

        <div class="ui grid">

            <div class="twelve wide column">
                <semui:errors bean="${titleInstanceInstance}" />

                <!--<h4 class="ui header">${message(code:'license.details.information', default:'Information')}</h4>-->

                <div class="la-inline-lists">
                    <div class="ui two cards">
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <semui:auditButton message="license.startDate" auditable="[license, 'startDate']" />
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="startDate" />
                                    </dd>
                                </dl>
                                <dl>
                                    <semui:auditButton message="license.endDate" auditable="[license, 'endDate']" />
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="endDate" />
                                    </dd>
                                </dl>
                            </div>
                        </div>
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <semui:auditButton message="license.status" auditable="[license, 'status']" />
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="status" config='License Status'/>
                                    </dd>
                                </dl>
                                <%--
                                <dl>

                                    <dt><label class="control-label" for="licenseCategory">${message(code:'license.licenseCategory', default:'License Category')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="licenseCategory" config='LicenseCategory'/>
                                    </dd>
                                </dl>
                                -->
                                <!--
                                <dl>
                                    <dt><label class="control-label" for="isPublic">${message(code:'license.isPublic', default:'Public?')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="isPublic" config='YN'/>
                                    </dd>
                                </dl>
                                --%>
                                <dl>
                                    <dt class="control-label">${message(code:'license.linktoLicense', default:'License Template')}</dt>

                                    <g:if test="${license.instanceOf}">
                                        <g:link controller="licenseDetails" action="show" id="${license.instanceOf.id}">${license.instanceOf}</g:link>
                                    </g:if>
                                </dl>

                                <g:if test="${license.instanceOf}">
                                    <dl>
                                        <dt class="control-label">
                                            ${message(code:'license.details.linktoLicense.pendingChange', default:'Automatically Accept Changes?')}
                                        </dt>
                                        <dd>
                                            <semui:xEditableRefData owner="${license}" field="isSlaved" config='YN'/>
                                        </dd>
                                    </dl>
                                </g:if>

                            </div>
                        </div>
                    </div>

                    <div class="ui card la-time-card">
                        <div class="content">

                            <g:if test="${license.subscriptions && ( license.subscriptions.size() > 0 )}">
                                <g:each in="${license.subscriptions.sort{it.name}}" var="sub">
                                    <g:if test="${contextOrg?.id in sub.orgRelations?.org?.id || (com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in  contextOrg?.getallOrgTypeIds())}">
                                        <table class="ui three column la-selectable table">
                                            <tr>
                                                <th scope="row">${message(code:'license.linkedSubscription', default:'Linked Subscription')}</th>
                                                <td>
                                                    <g:link controller="subscriptionDetails" action="show" id="${sub.id}">${sub.name }</g:link>
                                                </td>
                                                <td class="right aligned">
                                                    <g:if test="${editable}">
                                                        <div class="ui icon negative buttons">
                                                            <g:link class="ui button la-selectable-button" name="unlinkSubscription"
                                                                    controller="licenseDetails" action="unlinkSubscription"
                                                                    params="['license':license.id, 'subscription':sub.id]"
                                                                    onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})" >
                                                                <i class="unlink icon"></i>
                                                            </g:link>
                                                        </div>
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </table>
                                    </g:if>

                                </g:each>
                            </g:if>
                            <g:else>
                                <dl>
                                    <dt class="control-label">${message(code:'subscription.label')}</dt>
                                    <dd>
                                        ${message(code:'license.noLinkedSubscriptions', default:'No currently linked subscriptions.')}
                                    </dd>
                                </dl>
                            </g:else>

                            <dl>
                                <dt></dt>
                                <dd>
                                    <g:if test="${editable}">
                                        <g:form id="linkSubscription" class="ui form" name="linkSubscription" action="linkToSubscription">
                                            <br />
                                            <input type="hidden" name="license" value="${license.id}"/>
                                            <div class="fields">
                                                <div class="field">
                                                    <g:select class="ui search selectable dropdown"
                                                              optionKey="id" optionValue="${{it.getNameConcatenated()}}"
                                                              from="${availableSubs}" name="subscription"
                                                              noSelection="['':'Wählen Sie hier eine Lizenz zur Verknüpfung ..']"/>
                                                </div>
                                                <div class="field">
                                                    <input type="submit" class="ui button" value="${message(code:'default.button.link.label', default:'Link')}"/>
                                                </div>
                                            </div>
                                        </g:form>
                                    </g:if>
                                </dd>
                            </dl>
                            <%--
                            <dl>

                                <dt><label class="control-label" for="${license.pkgs}">${message(code:'license.linkedPackages', default:'Linked Packages')}</label></dt>
                                <dd>
                                    <g:if test="${license.pkgs && ( license.pkgs.size() > 0 )}">
                                        <g:each in="${license.pkgs.sort{it.name}}" var="pkg">
                                            <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link><br/>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        ${message(code:'license.noLinkedPackages', default:'No currently linked packages.')}
                                    </g:else>
                                </dd>
                            </dl>
                            --%>
                            <dl>
                                <sec:ifAnyGranted roles="ROLE_ADMIN">

                                    <dt class="control-label">${message(code:'license.ONIX-PL-License', default:'ONIX-PL License')}</dt>
                                    <dd>
                                        <g:if test="${license.onixplLicense}">
                                            <g:link controller="onixplLicense" action="index" id="${license.onixplLicense?.id}">${license.onixplLicense.title}</g:link>
                                            <g:if test="${editable}">

                                                <div class="ui mini icon buttons">
                                                    <g:link class="ui button" controller="licenseDetails" action="unlinkLicense" params="[license_id: license.id, opl_id: onixplLicense.id]">
                                                        <i class="unlink icon"> </i>${message(code:'default.button.unlink.label')}
                                                    </g:link>
                                                </div>

                                            </g:if>
                                        </g:if>
                                        <g:else>
                                            <g:link class="ui positive button" controller='licenseImport' action='doImport' params='[license_id: license.id]'>${message(code:'license.importONIX-PLlicense', default:'Import an ONIX-PL license')}</g:link>
                                        </g:else>
                                    </dd>

                                </sec:ifAnyGranted>
                            </dl>
                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">

                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks: visibleOrgLinks,
                                            roleObject: license,
                                            roleRespValue: 'Specific license editor',
                                            editmode: editable
                                  ]}" />

                        <g:render template="/templates/links/orgLinksSimpleModal"
                                  model="${[linkType: license?.class?.name,
                                            parent: license.class.name + ':' + license.id,
                                            property: 'orgLinks',
                                            recip_prop: 'lic',
                                            tmplRole: RDStore.OR_LICENSOR,
                                            tmplEntity: 'Lizenzgeber',
                                            tmplText: 'Lizenzgeber mit diesem Vertrag verknüpfen',
                                            tmplButtonText: 'Lizenzgeber verknüpfen',
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

    <r:script language="JavaScript">
        function changeLink(elem, msg) {
            var selectedOrg = $('#orgShortcode').val();
            var edited_link =  $("a[name=" + elem.name + "]").attr("href", function(i, val){
                return val.replace("replaceme", selectedOrg)
            });

            return confirm(msg);
        }
    </r:script>

  </body>
</html>
