<!doctype html>
<r:require module="annotations" />
<%@ page import="com.k_int.properties.PropertyDefinition" %>

<html>
  <head>
    <meta name="layout" content="pubbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="public.license" default=" PublicLicense"/></title>
  </head>

  <body>
  <g:render template="public_navbar" contextPath="/templates" model="['active': 'publicExport']"/>

    <div>
      <ul class="breadcrumb">
        <li> <g:link controller="publicLicense" action="index">All Licenses</g:link> <span class="divider">/</span> </li>

        <li> <g:link controller="publicLicense" action="show" id="${params.id}"><g:message code="license.details" default="License Details"/></g:link> </li>
    
        <li class="dropdown pull-right">
          <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">Exports<b class="caret"></b></a>&nbsp;
          <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
            <li>
              <g:link action="index" params="${params+[format:'json']}">Json Export</g:link>
            </li>
            <li>
              <g:link action="index" params="${params+[format:'xml']}">XML Export</g:link>
            </li>
          </ul>
        </li>

      </ul>
    </div>

    <div>
      <h1 class="ui header">${license.licensee?.name} ${license.type?.value} License : <g:xEditable owner="${license}" field="reference" id="reference"/></h1>
    </div>


    <div>
            <div class="row">
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

        <div class="row">
              <div class="span8">
  
                <h6>Information</h6>

                <div class="inline-lists">

                    <semui:messages data="${flash}" />
  
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
                      <dt><label class="control-label" for="subscriptions">Linked Subscriptions</label></dt>
                      <dd>
                        <g:if test="${license.subscriptions && ( license.subscriptions.size() > 0 )}">
                          <g:each in="${license.subscriptions}" var="sub">
                            ${sub.id} (${sub.name})
                          </g:each>
                        </g:if>
                        <g:else>No currently linked subscriptions.</g:else>
                      </dd>
                  </dl>
                
                  <dl>
                      <dt><label class="control-label" for="${license.pkgs}">Linked Packages</label></dt>
                      <dd>
                        <g:if test="${license.pkgs && ( license.pkgs.size() > 0 )}">
                          <g:each in="${license.pkgs}" var="pkg">
                           ${pkg.id} (${pkg.name})<br/>
                          </g:each>
                        </g:if>
                        <g:else>No currently linked packages.</g:else>
                      </dd>
                  </dl>
                
      
                  <dl>
                      <dt><label class="control-label" for="reference">Reference</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="reference" id="reference"/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="reference">Status</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="status" config='License Status'/>
                      </dd>
                  </dl>
      
      
                  <dl>
                      <dt><label class="control-label" for="noticePeriod">Notice Period</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="noticePeriod" id="noticePeriod"/>
                     </dd>
                  </dl>

                  <sec:ifAnyGranted roles="ROLE_ADMIN,KBPLUS_EDITOR">
                    <dl>
                        <dt><label class="control-label">ONIX-PL License</label></dt>
                        <dd>
                            <g:if test="${license.onixplLicense}">
                               ${license.onixplLicense.title}
                            </g:if>

                        </dd>
                    </dl>
                  </sec:ifAnyGranted>
      
                  <dl>
                      <dt><label class="control-label" for="licenseUrl"><g:message code="license" default="License"/> Url</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licenseUrl" id="licenseUrl"/>
                      </dd>
                  </dl>
      
                  <dl>
                      <dt><label class="control-label" for="licensorRef">Licensor Ref</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licensorRef" id="licensorRef"/>
                      </dd>
                  </dl>
      
                  <dl>
                      <dt><label class="control-label" for="licenseeRef">Licensee Ref</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" field="licenseeRef" id="licenseeRef"/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">Public?</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="isPublic" config='YN'/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">Start Date</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" type="date" field="startDate" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="isPublic">End Date</label></dt>
                      <dd>
                        <g:xEditable owner="${license}" type="date" field="endDate" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseCategory">License Category</label></dt>
                      <dd>
                        <g:xEditableRefData owner="${license}" field="licenseCategory" config='LicenseCategory'/>
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseeRef">Org Links</label></dt>
                      <dd>
                        <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:license?.orgLinks,editmode:editable]}" />
                      </dd>
                  </dl>

                  <dl>
                      <dt><label class="control-label" for="licenseeRef">Incoming License Links</label></dt>
                      <dd>
                        <ul>
                          <g:each in="${license?.incomingLinks}" var="il">
                            <li>${il.fromLic.reference} (${il.type?.value}) - 
                            Child: <g:xEditableRefData owner="${il}" field="isSlaved" config='YN'/>

                            </li>
                          </g:each>
              
                        </ul>
                      </dd>
                  </dl>

                  <div class="clearfix"></div>
              </div>
              </div>
              <div class="span4">
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

    
        $(document).ready(function() {
          $(".announce").click(function(){
            var id = $(this).data('id');
            $('#modalComments').load('<g:createLink controller="alert" action="commentsFragment" />/'+id);
            $('#modalComments').modal('show');
          });
        });

    </r:script>

  </body>
</html>
