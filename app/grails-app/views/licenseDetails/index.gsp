<!doctype html>
<r:require module="annotations" />
<%@ page import="com.k_int.properties.PropertyDefinition" %>

<html>
  <head>
    <meta name="layout" content="semanticUI"/>
     <g:javascript src="properties.js"/>
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="license" default="License"/></title>
  </head>

  <body>

    <div class="container">
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

    <div class="container">
      <h1>
          ${license.licensee?.name}
          ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
          <g:xEditable owner="${license}" field="reference" id="reference"/>
      </h1>
      <g:render template="nav" />
    </div>
    <div class="container">

    <g:if test="${flash.message}">
      <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
    </g:if>
    </div>

    <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':license]}"/>

    <div class="container">
            <div class="row">
                <div class="span12">
                    <h6>${message(code:'license.properties')}</h6>

                    <div id="custom_props_div_props">
                        <g:render template="/templates/properties/custom" model="${[
                                prop_desc: PropertyDefinition.LIC_PROP,
                                ownobj: license,
                                custom_props_div: "custom_props_div_props" ]}"/>
                    </div>

                    <h6>${message(code:'license.openaccess.properties')}</h6>

                    <div id="custom_props_div_oa">
                        <g:render template="/templates/properties/custom" model="${[
                                prop_desc: PropertyDefinition.LIC_OA_PROP,
                                ownobj: license,
                                custom_props_div: "custom_props_div_oa" ]}"/>
                    </div>

                    <h6>${message(code:'license.archive.properties')}</h6>

                    <div id="custom_props_div_archive">
                        <g:render template="/templates/properties/custom" model="${[
                                prop_desc: PropertyDefinition.LIC_ARC_PROP,
                                ownobj: license,
                                custom_props_div: "custom_props_div_archive" ]}"/>
                    </div>

                    <r:script language="JavaScript">
                        $(document).ready(function(){
                            initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
                            initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_oa");
                            initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_archive");
                        });
                    </r:script>
                </div>
                <br/>

                <div class="span8">
  
                <h6>${message(code:'license.details.information', default:'Information')}</h6>

                <div class="inline-lists">

                <g:hasErrors bean="${titleInstanceInstance}">
                  <bootstrap:alert class="alert-error">
                  <ul>
                    <g:eachError bean="${titleInstanceInstance}" var="error">
                      <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                    </g:eachError>
                  </ul>
                  </bootstrap:alert>
                </g:hasErrors>

                    <dl>
                        <dt><g:annotatedLabel owner="${license}" property="ids">${message(code:'license.identifiers.label')}</g:annotatedLabel></dt>
                        <dd>
                            <table class="table table-bordered">
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

                                <laser:formAddIdentifier owner="${license}" buttonText="${message(code:'license.edit.identifier.select.add')}"
                                                         uniqueCheck="yes" uniqueWarningText="${message(code:'license.edit.duplicate.warn.list')}">
                                    ${message(code:'identifier.select.text', args:['gasco-lic:0815'])}
                                </laser:formAddIdentifier>

                            </g:if>
                        </dd>
                    </dl>

                  <dl>
                      <dt><label class="control-label" for="subscriptions">${message(code:'license.linkedSubscriptions', default:'Linked Subscriptions')}</label></dt>
                      <dd>
                        <g:if test="${license.subscriptions && ( license.subscriptions.size() > 0 )}">
                          <g:each in="${license.subscriptions}" var="sub">
                            <g:link controller="subscriptionDetails" action="index" id="${sub.id}">${sub.id} (${sub.name})</g:link><br/>
                          </g:each>
                        </g:if>
                        <g:else>${message(code:'license.noLinkedSubscriptions', default:'No currently linked subscriptions.')}</g:else>
                      </dd>
                  </dl>
                
                  <dl>
                      <dt><label class="control-label" for="${license.pkgs}">${message(code:'license.linkedPackages', default:'Linked Packages')}</label></dt>
                      <dd>
                        <g:if test="${license.pkgs && ( license.pkgs.size() > 0 )}">
                          <g:each in="${license.pkgs}" var="pkg">
                            <g:link controller="packageDetails" action="show" id="${pkg.id}">${pkg.id} (${pkg.name})</g:link><br/>
                          </g:each>
                        </g:if>
                        <g:else>${message(code:'license.noLinkedPackages', default:'No currently linked packages.')}</g:else>
                      </dd>
                  </dl>
                
      
                  <dl>
                      <dt><label class="control-label" for="reference">${message(code:'license.reference', default:'Reference')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="reference" id="reference"/>
                      </dd>
                  </dl>
                  <dl>
                      <dt><label class="control-label" for="contact">${message(code:'license.licenseContact', default:'License Contact')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="contact" id="contact"/>
                      </dd>
                  </dl>
                  <dl>
                      <dt><label class="control-label" for="reference">${message(code:'license.status',default:'Status')}</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="status" config='License Status'/>
                      </dd>
                  </dl>
      
                  <sec:ifAnyGranted roles="ROLE_ADMIN,KBPLUS_EDITOR">
                    <dl>
                        <dt><label class="control-label">${message(code:'license.ONIX-PL-License', default:'ONIX-PL License')}</label></dt>
                        <dd>
                            <g:if test="${license.onixplLicense}">
                                <g:link controller="onixplLicenseDetails" action="index" id="${license.onixplLicense?.id}">${license.onixplLicense.title}</g:link>
                                <g:if test="${editable}">
                                    <g:link class="btn btn-warning" controller="licenseDetails" action="unlinkLicense" params="[license_id: license.id, opl_id: onixplLicense.id]">${message(code:'default.button.unlink.label', default:'Unlink')}</g:link>
                                </g:if>
                            </g:if>
                            <g:else>
                                <g:link class="btn btn-warning" controller='licenseImport' action='doImport' params='[license_id: license.id]'>${message(code:'license.importONIX-PLlicense', default:'Import an ONIX-PL license')}</g:link>
                            </g:else>
                        </dd>
                    </dl>
                  </sec:ifAnyGranted>
      
                  <dl>
                      <dt><label class="control-label" for="licenseUrl"><g:message code="license" default="License"/> ${message(code:'license.Url', default:'URL')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licenseUrl" id="licenseUrl"/>
                        <g:if test="${license.licenseUrl}"><a href="${license.licenseUrl}">${message(code:'license.details.licenseLink', default:'License Link')}</a></g:if>
                      </dd>
                  </dl>
      
                  <dl>
                      <dt><label class="control-label" for="licensorRef">${message(code:'license.licensorRef', default:'Licensor Ref')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licensorRef" id="licensorRef"/>
                      </dd>
                  </dl>
      
                  <dl>
                      <dt><label class="control-label" for="licenseeRef">${message(code:'license.licenseeRef', default:'Licensee Ref')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licenseeRef" id="licenseeRef"/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">${message(code:'license.isPublic', default:'Public?')}</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="isPublic" config='YN'/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">${message(code:'license.startDate', default:'Start Date')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" type="date" field="startDate" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">${message(code:'license.endDate', default:'End Date')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" type="date" field="endDate" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseCategory">${message(code:'license.licenseCategory', default:'License Category')}</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="licenseCategory" config='LicenseCategory'/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseeRef">${message(code:'license.orgLinks', default:'Org Links')}</label></dt>
                      <dd>
                        <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:license?.orgLinks,editmode:editable]}" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseeRef">${message(code:'license.incomingLicenseLinks', default:'Incoming License Links')}</label></dt>
                 <%-- <dt><label class="control-label" for="licenseeRef">Incoming License Links</label></dt> --%>
                      <dd>
                        <ul>
                          <g:each in="${license?.incomingLinks}" var="il">
                            <li><g:link controller="licenseDetails" action="index" id="${il.fromLic.id}">${il.fromLic.reference} (${il.type?.value})</g:link> - 
                            ${message(code:'license.details.incoming.child', default:'Child')}: <g:xEditableRefData owner="${il}" field="isSlaved" config='YN'/>

                            </li>
                          </g:each>
              
                        </ul>
                      </dd>
                  </dl>

                  <div class="clearfix"></div>
                </div>
              </div>
              <div class="span4">
                <laser:card title="license.actions" class="card-grey">

            <g:if test="${canCopyOrgs}">
                 
                  <label for="orgShortcode">${message(code:'license.copyLicensefor', default:'Copy license for')}:</label>
             <%-- <label for="orgShortcode">Copy license for:</label> --%>
                  <g:select from="${canCopyOrgs}" optionValue="name" optionKey="shortcode" id="orgShortcode" name="orgShortcode"/>
                              
                   <g:link name="copyLicenseBtn" controller="myInstitutions" action="actionLicenses" params="${[shortcode:'replaceme',baselicense:license.id,'copy-license':'Y']}" onclick="return changeLink(this, '${message(code:'license.details.copy.confirm')}')" class="btn btn-success" style="margin-bottom:10px">${message(code:'default.button.copy.label', default:'Copy')}</g:link>

               <label for="linkSubscription">${message(code:'license.linktoSubscription', default:'Link to Subscription')}:</label>
          <%-- <label for="linkSubscription">Link to Subscription:</label> --%>

               <g:form id="linkSubscription" name="linkSubscription" action="linkToSubscription">
                <input type="hidden" name="license" value="${license.id}"/>
                <g:select optionKey="id" optionValue="name" from="${availableSubs}" name="subscription"/>
                <input type="submit" class="btn btn-success" style="margin-bottom:10px" value="${message(code:'default.button.link.label', default:'Link')}"/>
              </g:form>
%{--            
          leave this out for now.. it is a bit confusing.
          <g:link name="deletLicenseBtn" controller="myInstitutions" action="actionLicenses" onclick="return changeLink(this,${message(code:'license.details.delete.confirm', args[(license.reference?:'** No license reference ** ')]?)" params="${[baselicense:license.id,'delete-license':'Y',shortcode:'replaceme']}" class="btn btn-danger">${message(code:'default.button.delete.label', default:'Delete')}</g:link> --}%
          </g:if>
                  <g:else>
                    ${message(code:'license.details.not_allowed', default:'Actions available to editors only')}
                  </g:else>
                 </laser:card>
                <g:render template="/templates/documents" model="${[ ownobj:license, owntp:'license']}" />
                <g:render template="/templates/notes"  model="${[ ownobj:license, owntp:'license']}" />
              </div>
            </div>
    </div>
    <g:render template="orgLinksModal" 
              contextPath="../templates" 
              model="${[linkType:license?.class?.name,roleLinks:license?.orgLinks,parent:license.class.name+':'+license.id,property:'orgLinks',recip_prop:'lic']}" />

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
