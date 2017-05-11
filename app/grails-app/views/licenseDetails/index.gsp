<!doctype html>
<r:require module="annotations" />
<%@ page import="com.k_int.custprops.PropertyDefinition" %>

<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
     <g:javascript src="custom_properties.js"/>
    <title>KB+ <g:message code="licence" default="Licence"/></title>
  </head>

  <body>

    <div class="container">
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

    <div class="container">
      <g:set var="message_str" value="refdata.LicenseType.${license.type?.value}" />
      <g:set var="local_type" value="${message(code:message_str)}" />
      <h1>${license.licensee?.name} ${message(code:'licence.details.type', args:[local_type], default:'Licence')} : <g:xEditable owner="${license}" field="reference" id="reference"/></h1>
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
              <h6>${message(code:'licence.properties')}</h6>
              <div id="custom_props_div" class="span12">
                  <g:render template="/templates/custom_props" model="${[ ownobj:license,prop_desc:PropertyDefinition.LIC_PROP ]}"/>
              </div>
            <br/>
              <div class="span8">
  
                <h6>${message(code:'licence.details.information', default:'Information')}</h6>

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
                      <dt><label class="control-label" for="subscriptions">${message(code:'licence.details.linked_subs', default:'Linked Subscriptions')}</label></dt>
                      <dd>
                        <g:if test="${license.subscriptions && ( license.subscriptions.size() > 0 )}">
                          <g:each in="${license.subscriptions}" var="sub">
                            <g:link controller="subscriptionDetails" action="index" id="${sub.id}">${sub.id} (${sub.name})</g:link><br/>
                          </g:each>
                        </g:if>
                        <g:else>${message(code:'licence.details.linked_subs.none', default:'No currently linked subscriptions.')}</g:else>
                      </dd>
                  </dl>
                
                  <dl>
                      <dt><label class="control-label" for="${license.pkgs}">${message(code:'licence.details.linked_pkg', default:'Linked Packages')}</label></dt>
                      <dd>
                        <g:if test="${license.pkgs && ( license.pkgs.size() > 0 )}">
                          <g:each in="${license.pkgs}" var="pkg">
                            <g:link controller="packageDetails" action="show" id="${pkg.id}">${pkg.id} (${pkg.name})</g:link><br/>
                          </g:each>
                        </g:if>
                        <g:else>${message(code:'licence.details.linked_pkg.none', default:'No currently linked packages.')}</g:else>
                      </dd>
                  </dl>
                
      
                  <dl>
                      <dt><label class="control-label" for="reference">${message(code:'licence.details.reference', default:'Reference')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="reference" id="reference"/>
                      </dd>
                  </dl>
                  <dl>
                      <dt><label class="control-label" for="contact">${message(code:'licence.details.linked_contact',default:'Licence Contact')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="contact" id="contact"/>
                      </dd>
                  </dl>
                  <dl>
                      <dt><label class="control-label" for="reference">${message(code:'licence.details.status',default:'Status')}</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="status" config='License Status'/>
                      </dd>
                  </dl>
      
                  <sec:ifAnyGranted roles="ROLE_ADMIN,KBPLUS_EDITOR">
                    <dl>
                        <dt><label class="control-label">${message(code:'onixplLicence.licence.label', default:'ONIX-PL Licence')}</label></dt>
                        <dd>
                            <g:if test="${license.onixplLicense}">
                                <g:link controller="onixplLicenseDetails" action="index" id="${license.onixplLicense?.id}">${license.onixplLicense.title}</g:link>
                                <g:if test="${editable}">
                                    <g:link class="btn btn-warning" controller="licenseDetails" action="unlinkLicense" params="[license_id: license.id, opl_id: onixplLicense.id]">Unlink</g:link>
                                </g:if>
                            </g:if>
                            <g:else>
                                <g:link class="btn btn-warning" controller='licenseImport' action='doImport' params='[license_id: license.id]'>${message(code:'onix.import.licence', default:'Import an ONIX-PL licence')}</g:link>
                            </g:else>
                        </dd>
                    </dl>
                  </sec:ifAnyGranted>
      
                  <dl>
                      <dt><label class="control-label" for="licenseUrl"><g:message code="licence.details.licenceUrl" default="Licence Url"/></label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licenseUrl" id="licenseUrl"/>
                        <g:if test="${license.licenseUrl}"><a href="${license.licenseUrl}">${message(code:'licence.details.licenceLink', default:'License Link')}</a></g:if>
                      </dd>
                  </dl>
      
                  <dl>
                      <dt><label class="control-label" for="licensorRef">${message(code:'licence.details.licensorRef', default:'Licensor Ref')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licensorRef" id="licensorRef"/>
                      </dd>
                  </dl>
      
                  <dl>
                      <dt><label class="control-label" for="licenseeRef">${message(code:'licence.details.licenseeRef', default:'Licensee Ref')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licenseeRef" id="licenseeRef"/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">${message(code:'licence.is_public', default:'Public?')}</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="isPublic" config='YN'/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">${message(code:'default.startDate.label', default:'Start Date')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" type="date" field="startDate" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">${message(code:'default.endDate.label', default:'End Date')}</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" type="date" field="endDate" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseCategory">${message(code:'licence.details.licenceCategory', default:'Licence Category')}</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="licenseCategory" config='LicenseCategory'/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseeRef">${message(code:'licence.details.orkLinks', default:'Org Links')}</label></dt>
                      <dd>
                        <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:license?.orgLinks,editmode:editable]}" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseeRef">${message(code:'licence.details.incoming', default:'Incoming Licence Links')}</label></dt>
                      <dd>
                        <ul>
                          <g:each in="${license?.incomingLinks}" var="il">
                            <li><g:link controller="licenseDetails" action="index" id="${il.fromLic.id}">${il.fromLic.reference} (${il.type?.value})</g:link> - 
                            ${message(code:'licence.details.incoming.child', default:'Child')}: <g:xEditableRefData owner="${il}" field="isSlaved" config='YN'/>

                            </li>
                          </g:each>
              
                        </ul>
                      </dd>
                  </dl>

                  <div class="clearfix"></div
>              </div>
              </div>
              <div class="span4">
                <div class="well">
                <label>  <h5>${message(code:'licence.details.actions', default:'Licence Actions')}</h5> </label>
            <g:if test="${canCopyOrgs}">
                 
                  <label for="orgShortcode">${message(code:'licence.details.copy', default:'Copy licence for')}:</label>
                  <g:select from="${canCopyOrgs}" optionValue="name" optionKey="shortcode" id="orgShortcode" name="orgShortcode"/>
                              
                   <g:link name="copyLicenceBtn" controller="myInstitutions" action="actionLicenses" params="${[shortcode:'replaceme',baselicense:license.id,'copy-licence':'Y']}" onclick="return changeLink(this,'Are you sure you want to copy this licence?')" class="btn btn-success">${message(code:'default.button.copy.label', default:'Copy')}</g:link>

               <label for="linkSubscription">${message(code:'licence.details.sublink', default:'Link to Subscription')}:</label>

               <g:form id="linkSubscription" name="linkSubscription" action="linkToSubscription">
                <input type="hidden" name="licence" value="${license.id}"/>
                <g:select optionKey="id" optionValue="name" from="${availableSubs}" name="subscription"/>
                <input type="submit" class="btn btn-success" value="${message(code:'default.button.link.label', default:'Link')}"/>
              </g:form>
%{--            
          leave this out for now.. it is a bit confusing.
          <g:link name="deletLicenceBtn" controller="myInstitutions" action="actionLicenses" onclick="return changeLink(this,'Are you sure you want to delete ${license.reference?:'** No licence reference ** '}?')" params="${[baselicense:license.id,'delete-licence':'Y',shortcode:'replaceme']}" class="btn btn-danger">${message(code:'default.button.delete.label', default:'Delete')}</g:link> --}%
          </g:if>
                  <g:else>
                    ${message(code:'licence.details.not_allowed', default:'Actions available to editors only')}
                  </g:else>
                 </div>
                <g:render template="/templates/documents" model="${[ ownobj:license, owntp:'license']}" />
                <g:render template="/templates/notes"  model="${[ ownobj:license, owntp:'license']}" />
              </div>
            </div>
    </div>
    <g:render template="orgLinksModal" 
              contextPath="../templates" 
              model="${[linkType:license?.class?.name,roleLinks:license?.orgLinks,parent:license.class.name+':'+license.id,property:'orgLinks',recip_prop:'lic']}" />

    <r:script language="JavaScript">
      function changeLink(elem,msg){
        var selectedOrg = $('#orgShortcode').val();
        var edited_link =  $("a[name="+elem.name+"]").attr("href",function(i,val){
          return val.replace("replaceme",selectedOrg)
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
     window.onload = function() {
     runCustomPropsJS("<g:createLink controller='ajax' action='lookup'/>");
    };
    </r:script>

  </body>
</html>
