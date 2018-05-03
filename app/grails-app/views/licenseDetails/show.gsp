<!doctype html>
<r:require module="annotations" />
<%@ page import="com.k_int.properties.PropertyDefinition" %>

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
     <g:javascript src="properties.js"/>
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="license" default="License"/></title>
  </head>

    <body>

        <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

        <h1 class="ui header"><semui:headerIcon />

            ${license.licensee?.name}
            ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
            <semui:xEditable owner="${license}" field="reference" id="reference"/>
        </h1>

        <g:render template="nav" />

        <semui:meta>
            <div class="inline-lists">

                <dl>
                    <dt><g:message code="license.globalUID.label" default="Global UID" /></dt>
                    <dd>
                        <g:fieldValue bean="${license}" field="globalUID"/>
                    </dd>

                    <dt>
                        <g:message code="org.ids.label" default="Ids" />
                        <g:annotatedLabel owner="${license}" property="ids">${message(code:'license.identifiers.label')}</g:annotatedLabel>
                    </dt>
                    <dd>
                        <table class="ui celled la-table la-table-small table ignore-floatThead">
                            <thead>
                            <tr>
                                <th>${message(code:'default.authority.label', default:'Authority')}</th>
                                <th>${message(code:'default.identifier.label', default:'Identifier')}</th>
                                <th>${message(code:'default.actions.label', default:'Actions')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:set var="id_label" value="${message(code:'identifier.label', default:'Identifier')}"/>
                            <g:each in="${license.ids}" var="io">
                                <tr>
                                    <td>${io.identifier.ns.ns}</td>
                                    <td>${io.identifier.value}</td>
                                    <td><g:if test="${editable}">
                                        <g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${license.class.name}:${license.id}",contextProperty:"ids",targetOid:"${io.class.name}:${io.id}"]}'>
                                            ${message(code:'default.delete.label', args:["${message(code:'identifier.label')}"])}</g:link>
                                    </g:if></td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                        <g:if test="${editable}">

                            <semui:formAddIdentifier owner="${license}" buttonText="${message(code:'license.edit.identifier.select.add')}"
                                                     uniqueCheck="yes" uniqueWarningText="${message(code:'license.edit.duplicate.warn.list')}">
                                ${message(code:'identifier.select.text', args:['gasco-lic:0815'])}
                            </semui:formAddIdentifier>

                        </g:if>
                    </dd>
                </dl>
            </div>
        </semui:meta>

        <semui:messages data="${flash}" />

        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':license]}"/>

        <div class="ui grid">

            <div class="twelve wide column">
                <semui:errors bean="${titleInstanceInstance}" />

                <!--<h4 class="ui header">${message(code:'license.details.information', default:'Information')}</h4>-->

                <div class="la-inline-lists">
                    <div class="ui two cards">
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <dt><label class="control-label" for="startDate">${message(code:'license.startDate', default:'Start Date')}</label></dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="startDate" />
                                    </dd>
                                </dl>
                                <dl>
                                    <dt><label class="control-label" for="endDate">${message(code:'license.endDate', default:'End Date')}</label></dt>
                                    <dd>
                                        <semui:xEditable owner="${license}" type="date" field="endDate" />
                                    </dd>
                                </dl>
                            </div>
                        </div>
                        <div class="ui card ">
                            <div class="content">
                                <dl>
                                    <dt><label class="control-label" for="reference">${message(code:'license.status',default:'Status')}</label></dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${license}" field="status" config='License Status'/>
                                    </dd>
                                </dl>
                                <!--
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
                                -->
                            </div>
                        </div>
                    </div>
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt><label class="control-label" for="subscriptions">${message(code:'license.linkedSubscriptions', default:'Linked Subscriptions')}</label></dt>
                                <dd>
                                    <g:if test="${license.subscriptions && ( license.subscriptions.size() > 0 )}">
                                        <g:each in="${license.subscriptions}" var="sub">
                                            <g:link controller="subscriptionDetails" action="show" id="${sub.id}">${sub.name}</g:link>

                                            <g:if test="${editable}">
                                                <div class="ui mini icon buttons">
                                                    <g:link class="ui disabled button" controller="todo" action="todo" id="todo" onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})" >
                                                        <i class="times icon red"></i>${message(code:'default.button.unlink.label')}
                                                    </g:link>
                                                </div>
                                            </g:if>

                                            <br/>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        ${message(code:'license.noLinkedSubscriptions', default:'No currently linked subscriptions.')}
                                    </g:else>

                                    <br />

                                    <g:if test="${editable}">
                                        <g:form id="linkSubscription" class="ui form" name="linkSubscription" action="linkToSubscription">
                                            <br />
                                            <input type="hidden" name="license" value="${license.id}"/>
                                            <div class="two fields">
                                                <div class="field">
                                                    <g:select optionKey="id" optionValue="name" from="${availableSubs}" name="subscription" class="ui fluid dropdown"/>
                                                </div>
                                                <div class="field">
                                                    <input type="submit" class="ui button" value="${message(code:'default.button.link.label', default:'Link')}"/>
                                                </div>
                                            </div>
                                        </g:form>
                                    </g:if>
                                </dd>
                            </dl>
                            <!--
                            <dl>

                                <dt><label class="control-label" for="${license.pkgs}">${message(code:'license.linkedPackages', default:'Linked Packages')}</label></dt>
                                <dd>
                                    <g:if test="${license.pkgs && ( license.pkgs.size() > 0 )}">
                                        <g:each in="${license.pkgs}" var="pkg">
                                            <g:link controller="packageDetails" action="show" id="${pkg.id}">${pkg.name}</g:link><br/>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        ${message(code:'license.noLinkedPackages', default:'No currently linked packages.')}
                                    </g:else>
                                </dd>
                            </dl>
                            -->
                            <dl>
                                <sec:ifAnyGranted roles="ROLE_ADMIN">

                                    <dt><label class="control-label">${message(code:'license.ONIX-PL-License', default:'ONIX-PL License')}</label></dt>
                                    <dd>
                                        <g:if test="${license.onixplLicense}">
                                            <g:link controller="onixplLicenseDetails" action="index" id="${license.onixplLicense?.id}">${license.onixplLicense.title}</g:link>
                                            <g:if test="${editable}">
                                                (
                                                <div class="ui mini icon buttons">
                                                    <g:link class="ui button" controller="licenseDetails" action="unlinkLicense" params="[license_id: license.id, opl_id: onixplLicense.id]">
                                                        <i class="times icon red"></i>${message(code:'default.button.unlink.label')}
                                                    </g:link>
                                                </div>
                                                )
                                            </g:if>
                                        </g:if>
                                        <g:else>
                                            <g:link class="ui negative button" controller='licenseImport' action='doImport' params='[license_id: license.id]'>${message(code:'license.importONIX-PLlicense', default:'Import an ONIX-PL license')}</g:link>
                                        </g:else>
                                    </dd>

                                </sec:ifAnyGranted>
                            </dl>
                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">

                        <%--
                        <dl>
                            <dt><label class="control-label" for="licenseUrl"><g:message code="license" default="License"/> ${message(code:'license.Url', default:'URL')}</label></dt>
                            <dd>
                                <semui:xEditable owner="${license}" field="licenseUrl" id="licenseUrl"/>
                                <g:if test="${license.licenseUrl}"><a href="${license.licenseUrl}">${message(code:'license.details.licenseLink', default:'License Link')}</a></g:if>
                            </dd>
                        </dl>
                        --%>

                        <%--
                            <dl>
                                <dt><label class="control-label" for="licenseeRef">${message(code:'license.incomingLicenseLinks', default:'Incoming License Links')}</label></dt>
                                <dd>
                                    <ul>
                                        <g:each in="${license?.incomingLinks}" var="il">
                                            <li><g:link controller="licenseDetails" action="show" id="${il.fromLic.id}">${il.fromLic.reference} (${il.type?.value})</g:link> -
                                            ${message(code:'license.details.incoming.child', default:'Child')}:
                                            <semui:xEditableRefData owner="${il}" field="isSlaved" config='YN'/>
                                            </li>
                                        </g:each>

                                    </ul>
                                </dd>
                            </dl>
                           --%>
                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks: visibleOrgLinks,
                                            roleObject: license,
                                            roleRespValue: 'Specific license editor',
                                            editmode: editable,
                                            tmplButtonText: 'Lizenzgeber hinzufügen'
                                  ]}" />

                        <g:render template="/templates/links/orgLinksModal"
                                  model="${[linkType: license?.class?.name,
                                            parent: license.class.name+':'+license.id,
                                            property: 'orgLinks',
                                            recip_prop: 'lic',
                                            tmplRole: com.k_int.kbplus.RefdataValue.getByValueAndCategory('Licensor', 'Organisational Role'),
                                            tmplText:'Lizenzgeber hinzufügen',
                                            tmplID:'ContentProvider'
                                  ]}" />
<%--
                        <g:render template="/templates/links/orgLinksAsListAddPrsModal"
                                  model="[roleLinks: visibleOrgLinks,
                                          'license': license,
                                          parent: license.class.name + ':' + license.id,
                                          role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id
                                  ]"/>
                                  --%>
                        </div>
                    </div>

                    <g:render template="/templates/debug/orgRoles" model="[debug: license.orgLinks]" />

                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <h5 class="ui header">${message(code:'license.properties')}</h5>

                            <div id="custom_props_div_props">
                                <g:render template="/templates/properties/custom" model="${[
                                    prop_desc: PropertyDefinition.LIC_PROP,
                                    ownobj: license,
                                    custom_props_div: "custom_props_div_props" ]}"/>
                            </div>
                        </div>
                    </div>
                    <div class="ui card la-dl-no-table">
                        <div class="content">

                            <h5 class="ui header">${message(code:'license.openaccess.properties')}</h5>

                            <div id="custom_props_div_oa">
                                <g:render template="/templates/properties/custom" model="${[
                                        prop_desc: PropertyDefinition.LIC_OA_PROP,
                                        ownobj: license,
                                        custom_props_div: "custom_props_div_oa" ]}"/>
                            </div>
                        </div>
                    </div>
                    <div class="ui card la-dl-no-table">
                        <div class="content">

                            <h5 class="ui header">${message(code:'license.archive.properties')}</h5>

                            <div id="custom_props_div_archive">
                                <g:render template="/templates/properties/custom" model="${[
                                        prop_desc: PropertyDefinition.LIC_ARC_PROP,
                                        ownobj: license,
                                        custom_props_div: "custom_props_div_archive" ]}"/>
                            </div>
                        </div>
                    </div>
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:each in="${authorizedOrgs}" var="authOrg">
                            <g:if test="${authOrg.name == contextOrg?.name}">
                                <h5 class="ui header">${message(code:'license.properties.private')} ${authOrg.name}</h5>

                                <div id="custom_props_div_${authOrg.shortcode}">
                                    <g:render template="/templates/properties/private" model="${[
                                            prop_desc: PropertyDefinition.LIC_PROP,
                                            ownobj: license,
                                            custom_props_div: "custom_props_div_${authOrg.shortcode}",
                                            tenant: authOrg]}"/>

                                    <r:script language="JavaScript">
                                            $(document).ready(function(){
                                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.shortcode}", ${authOrg.id});
                                            });
                                    </r:script>
                                </div>
                            </g:if>
                        </g:each>
                            <r:script language="JavaScript">
                            $(document).ready(function(){
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_oa");
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_archive");
                            });
                        </r:script>
                        </div>
                    </div>
                </div>

                <div class="clearfix"></div>

            </div><!-- .twelve -->

            <aside class="four wide column">
            <%--
                <semui:card message="license.actions">
                    <div class="ui form content">

                        <g:if test="${canCopyOrgs}">

                            <div class="field">
                                <label for="orgShortcode">${message(code:'license.copyLicensefor', default:'Copy license for')}:</label>

                                <g:select from="${canCopyOrgs}" optionValue="name" optionKey="shortcode" name="orgShortcode" id="orgShortcode" class="ui fluid dropdown"/>
                            </div>

                            <g:link name="copyLicenseBtn" controller="myInstitution" action="actionLicenses" params="${[shortcode:'replaceme', baselicense:license.id, 'copy-license':'Y']}" onclick="return changeLink(this, '${message(code:'license.details.copy.confirm')}')" class="ui button" style="margin-bottom:10px">${message(code:'default.button.copy.label', default:'Copy')}</g:link>

                            <br />

                            <label for="linkSubscription">${message(code:'license.linktoSubscription', default:'Link to Subscription')}:</label>

                            <g:form id="linkSubscription" class="ui form" name="linkSubscription" action="linkToSubscription">
                                <div class="field">
                                    <input type="hidden" name="license" value="${license.id}"/>
                                    <g:select optionKey="id" optionValue="name" from="${availableSubs}" name="subscription" class="ui fluid dropdown"/>
                                </div>
                                <input type="submit" class="ui button" value="${message(code:'default.button.link.label', default:'Link')}"/>

                            </g:form>

                            %{--
                                leave this out for now.. it is a bit confusing.
                                <g:link name="deletLicenseBtn" controller="myInstitution" action="actionLicenses" onclick="return changeLink(this,${message(code:'license.details.delete.confirm', args[(license.reference?:'** No license reference ** ')]?)" params="${[baselicense:license.id,'delete-license':'Y',shortcode:'replaceme']}" class="ui negative button">${message(code:'default.button.delete.label', default:'Delete')}</g:link> --}%
                        </g:if>
                        <g:else>
                            ${message(code:'license.details.not_allowed', default:'Actions available to editors only')}
                        </g:else>
                    </div>
                </semui:card>
                --%>

                <g:render template="/templates/tasks/card" model="${[ownobj:license, owntp:'license']}" />
                <g:render template="/templates/documents/card" model="${[ownobj:license, owntp:'license']}" />
                <g:render template="/templates/notes/card"  model="${[ownobj:license, owntp:'license']}" />

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

        <g:if test="${editable}">
        </g:if>
        <g:else>
            $(document).ready(function() {
                $(".announce").click(function(){
                    var id = $(this).data('id');
                    $('#modalComments').load('<g:createLink controller="alert" action="commentsFragment" />/'+id);
                    $('#modalComments').modal('show');
                });
            });
        </g:else>
    </r:script>

  </body>
</html>
