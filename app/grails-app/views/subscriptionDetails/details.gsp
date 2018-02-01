<%@ page import="com.k_int.kbplus.Subscription" %>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.k_int.properties.PropertyDefinition" %>
<%@ page import="com.k_int.kbplus.RefdataCategory" %>
<%
  def dateFormater = new SimpleDateFormat(session.sessionPreferences?.globalDateFormat)
%>

<r:require module="annotations" />

<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.details.label', default:'Subscription Details')}</title>
      <g:javascript src="properties.js"/>
    </head>
    <body>
        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <g:render template="actions" />

      <g:if test="${params.asAt}">
          <h1 class="ui header">${message(code:'myinst.subscriptionDetails.snapshot', args:[params.asAt])} </h1>
      </g:if>

       <h1 class="ui header">
           <semui:editableLabel editable="${editable}" />
           <semui:xEditable owner="${subscriptionInstance}" field="name" />
       </h1>

        <g:render template="nav" />

        <semui:meta>
            <div class="inline-lists">

                <g:if test="${subscriptionInstance.globalUID}">
                    <dl>
                        <dt><g:message code="subscription.globalUID.label" default="Global UID" /></dt>
                        <dd>
                            <g:fieldValue bean="${subscriptionInstance}" field="globalUID"/>
                        </dd>
                    </dl>
                </g:if>

                <dl>
                    <dt><g:annotatedLabel owner="${subscriptionInstance}" property="ids">${message(code:'subscription.identifiers.label', default:'Subscription Identifiers')}</g:annotatedLabel></dt>
                    <dd>
                        <table class="ui celled table">
                            <thead>
                            <tr>
                                <th>${message(code:'default.authority.label', default:'Authority')}</th>
                                <th>${message(code:'default.identifier.label', default:'Identifier')}</th>
                                <th>${message(code:'default.actions.label', default:'Actions')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:set var="id_label" value="${message(code:'identifier.label', default:'Identifier')}"/>
                            <g:each in="${subscriptionInstance.ids}" var="io">
                                <tr>
                                    <td>${io.identifier.ns.ns}</td>
                                    <td>${io.identifier.value}</td>
                                    <td><g:if test="${editable}"><g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${subscriptionInstance.class.name}:${subscriptionInstance.id}",contextProperty:"ids",targetOid:"${io.class.name}:${io.id}"]}'>${message(code:'default.delete.label', args:["${message(code:'identifier.label')}"])}</g:link></g:if></td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                        <g:if test="${editable}">

                            <semui:formAddIdentifier owner="${subscriptionInstance}" uniqueCheck="yes" uniqueWarningText="${message(code:'subscription.details.details.duplicate.warn')}">
                                ${message(code:'identifier.select.text', args:['JC:66454'])}
                            </semui:formAddIdentifier>

                        </g:if>
                    </dd>
                </dl>

            </div>
        </semui:meta>

        <semui:messages data="${flash}" />

        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>


    <div id="collapseableSubDetails" class="ui grid">
        <div class="twelve wide column">

            <!--<h4 class="ui header">${message(code:'subscription.information.label', default:'Subscription Information')}</h4>-->

            <div class="inline-lists">

                <dl>
                    <dt>${message(code:'subscription.startDate.label', default:'Start Date')}</dt>
                    <dd><semui:xEditable owner="${subscriptionInstance}" field="startDate" type="date"/></dd>
                </dl>

                <dl>
                    <dt>${message(code:'subscription.endDate.label', default:'End Date')}</dt>
                    <dd><semui:xEditable owner="${subscriptionInstance}" field="endDate" type="date"/></dd>
                </dl>

                <dl>
                    <dt>${message(code:'subscription.packages.label')}</dt>
                    <dd>
                            <g:each in="${subscriptionInstance.packages}" var="sp">

                                <g:link controller="packageDetails" action="show" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>

                                <g:if test="${sp.pkg?.contentProvider}">
                                    ,
                                    ${sp.pkg?.contentProvider?.name}
                                </g:if>

                                <g:if test="${editable}">
                                    <br />
                                    <a href="" onclick="unlinkPackage(${sp.pkg.id})">
                                        ( <i class="unlinkify icon red"></i> ${message(code:'default.button.unlink.label')} )
                                    </a>
                                </g:if>

                            </g:each>

                        <% /*
                        <table class="ui celled table">
                            <thead>
                            <th>${message(code:'package.name.label', default:'Name')}</th>
                            <th>${message(code:'package.content_provider', default:'Content Provider')}</th>
                            <th>${message(code:'default.actions.label', default:'Actions')}</th>
                            </thead>
                            <tbody>
                            <g:each in="${subscriptionInstance.packages}" var="sp">
                                <tr>
                                    <td>
                                        <g:link controller="packageDetails" action="show" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>
                                    </td>
                                    <td>
                                        ${sp.pkg?.contentProvider?.name}
                                    </td>
                                    <td>
                                        <g:if test="${editable}">
                                            <a href="" onclick="unlinkPackage(${sp.pkg.id})">
                                                <i class="unlinkify icon red"></i>
                                                ${message(code:'default.button.unlink.label')}
                                            </a>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                        */ %>

                    </dd>
                </dl>

                <dl>
                    <dt>${message(code:'license')}</dt>
                    <dd>
                        <g:if test="${subscriptionInstance.subscriber || subscriptionInstance.consortia}">
                            <semui:xEditableRefData owner="${subscriptionInstance}" field="owner" dataController="subscriptionDetails" dataAction="possibleLicensesForSubscription" />
                            <g:if test="${subscriptionInstance.owner != null}">
                                (
                                <g:link controller="licenseDetails" action="index" id="${subscriptionInstance.owner.id}">${message(code:'default.button.show.label', default:'Show')}</g:link>
                                <g:link controller="licenseDetails" action="index" target="new" id="${subscriptionInstance.owner.id}"><i class="icon-share-alt"></i></g:link>
                                )
                            </g:if>
                        </g:if>
                        <g:else>N/A (Subscription offered)</g:else>
                    </dd>
                </dl>

                <% /*
                <dl>
                    <dt>${message(code:'subscription.manualRenewalDate.label', default:'Manual Renewal Date')}</dt>
                    <dd><semui:xEditable owner="${subscriptionInstance}" field="manualRenewalDate" type="date"/></dd>
                </dl>

                <dl>
                    <dt>${message(code:'subscription.manualCancellationlDate.label', default:'Manual Cancellation Date')}</dt>
                    <dd><semui:xEditable owner="${subscriptionInstance}" field="manualCancellationDate" type="date"/></dd>
                </dl>
                */ %>

                <dl>
                    <dt>${message(code:'subscription.details.type', default:'Type')}</dt>
                    <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="type" config='Subscription Type' /></dd>
                </dl>

                <g:if test="${subscriptionInstance.instanceOf}">
                    <dl>
                        <dt>${message(code:'subscription.isInstanceOfSub.label')}</dt>
                        <dd>
                            <g:link controller="subscriptionDetails" action="details" id="${subscriptionInstance.instanceOf.id}">${subscriptionInstance.instanceOf}</g:link>
                        </dd>
                    </dl>
                </g:if>

                <dl>
                    <dt>${message(code:'subscription.details.status', default:'Status')}</dt>
                    <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status' /></dd>
                </dl>

                <% /*
                <dl>
                    <dt>${message(code:'subscription.details.isPublic', default:'Public?')}</dt>
                    <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="isPublic" config='YN' /></dd>
                </dl>
                */ %>
                <% /* <dl>
                   <dt>${message(code:'license.details.incoming.child', default:'Child')} </dt>
                   <dd>${subscriptionInstance.getIsSlavedAsString()}</dd>
               </dl> */ %>

                <% /*
                <dl>
                    <dt>
                        <g:annotatedLabel owner="${subscriptionInstance}" property="nominalPlatform">${message(code:'package.nominalPlatform', default:'Nominal Platform')}</g:annotatedLabel>
                    </dt>
                    <dd>
                        <g:each in="${subscriptionInstance.packages}" var="sp">
                            ${sp.pkg?.nominalPlatform?.name}<br/>
                        </g:each>
                    </dd>
                </dl>
                */ %>
                <% /* <dl>
                    <dt>${message(code:'financials.cancellationAllowances', default:'Cancellation Allowances')}</dt>
                    <dd> <semui:xEditable owner="${subscriptionInstance}" field="cancellationAllowances" /></dd>
                </dl> */ %>


                <g:render template="/templates/links/orgLinksAsList" model="${[roleLinks:subscriptionInstance?.orgRelations, editmode:editable]}" />

                <% /*
               <dl>
                    <dt><label class="control-label" for="licenseeRef">${message(code:'org.links.label', default:'Org Links')}</label></dt><dd>
                        <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:subscriptionInstance?.orgRelations,editmode:editable]}" />
                    </dd>
               </dl>
               */ %>

               <% /*g:if test="${params.mode=='advanced'}">
                 <dl><dt><label class="control-label" for="licenseeRef">${message(code:'default.status.label', default:'Status')}</label></dt><dd>
                      <semui:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status'/>
                     </dd>
               </dl>
               </g:if */ %>

                <g:render template="/templates/links/prsLinksAsList" model="[tmplShowFunction:false]"/>

                <g:render template="/templates/links/prsLinksModal"
                          model="['subscription': subscriptionInstance, parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id, role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id]"/>


                <% /*
                <dl>
                    <dt><g:message code="license.responsibilites" default="Responsibilites" /></dt>
                    <dd>
                        <g:render template="/templates/links/prsLinks" model="[tmplShowFunction:false]"/>

                        <g:render template="/templates/links/prsLinksModal"
                                  model="['subscription': subscriptionInstance, parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id, role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id]"/>
                    </dd>
                </dl>
            */ %>

                <dl>
                    <dt>${message(code:'financials.label', default:'Financials')}</dt>
                    <dd>
                        <table class="ui extra table">
                            <thead>
                            <tr>
                                <th>${message(code:'financials.costItemCategory')}</th>
                                <th>${message(code:'financials.costItemElement')}</th>
                                <th>${message(code:'financials.costInLocalCurrency')}</th>
                                <th>${message(code:'financials.costItemStatus', default:'Status')}</th>
                            </tr>
                            </thead>
                            <tbody>
                                <g:each in="${subscriptionInstance.costItems}" var="ci">
                                    <tr>
                                        <td>${ci.costItemCategory?.getI10n('value')}</td>
                                        <td>${ci.costItemElement?.getI10n('value')}</td>
                                        <td>${ci.costInLocalCurrency} ${RefdataCategory.lookupOrCreate('Currency','EUR - Euro Member Countries').getI10n('value')}</td>
                                        <td>${ci.costItemStatus?.getI10n('value')}</td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                        <% /*
                        <table class="ui celled striped table">
                            <thead>
                            <tr>
                                <th>${message(code:'financials.costItem', default:'CI')}</th>
                                <th>${message(code:'financials.order', default:'Order')}</th>
                                <th>${message(code:'financials.datePaid', default:'Date Paid')}</th>
                                <th>${message(code:'default.startDate.label', default:'Start Date')}</th>
                                <th>${message(code:'default.endDate.label', default:'End Date')}</th>
                                <th>${message(code:'financials.amount', default:'Amount')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${subscriptionInstance.costItems}" var="ci">
                                <tr>
                                    <td>${ci.id}</td>
                                    <td>${ci.order?.orderNumber}</td>
                                    <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ci.datePaid}"/></td>
                                    <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ci.startDate}"/></td>
                                    <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ci.endDate}"/></td>
                                    <td>${ci.costInLocalCurrency} / ${ci.costInBillingCurrency}</td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                        */ %>
                    </dd>
                </dl>


                <h5 class="ui header">${message(code:'subscription.properties')}</h5>

                <div id="custom_props_div_props">
                    <g:render template="/templates/properties/custom" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: subscriptionInstance,
                            custom_props_div: "custom_props_div_props" ]}"/>
                </div>

                <r:script language="JavaScript">
                    $(document).ready(function(){
                        initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
                    });
                </r:script>

                <g:each in="${authorizedOrgs}" var="authOrg">
                    <g:if test="${authOrg.name == contextOrg?.name}">
                        <h5 class="ui header">${message(code:'subscription.properties.private')} ${authOrg.name}</h5>

                        <div id="custom_props_div_${authOrg.shortcode}">
                            <g:render template="/templates/properties/private" model="${[
                                    prop_desc: PropertyDefinition.SUB_PROP,
                                    ownobj: subscriptionInstance,
                                    custom_props_div: "custom_props_div_${authOrg.shortcode}",
                                    tenant: authOrg]}"/>

                            <r:script language="JavaScript">
                                $(document).ready(function(){
                                    initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.shortcode}", ${authOrg.id});
                                });
                            </r:script>
                        </div>
                    </g:if>
                </g:each>

               <div class="clear-fix"></div>
            </div>
        </div>

        <div class="four wide column" style="margin-top: 50px">
            <g:render template="card" contextPath="../templates/tasks" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
            <g:render template="card" contextPath="../templates/documents" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
            <g:render template="card" contextPath="../templates/notes" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
        </div><!-- .four -->
    </div><!-- .grid -->


    <div id="magicArea"></div>
    <g:render template="orgLinksModal" 
              contextPath="../templates" 
              model="${[linkType:subscriptionInstance?.class?.name,roleLinks:subscriptionInstance?.orgRelations,parent:subscriptionInstance.class.name+':'+subscriptionInstance.id,property:'orgs',recip_prop:'sub']}" />
    <r:script language="JavaScript">

      function unlinkPackage(pkg_id){
        var req_url = "${createLink(controller:'subscriptionDetails', action:'unlinkPackage',params:[subscription:subscriptionInstance.id])}&package="+pkg_id

        $.ajax({url: req_url, 
          success: function(result){
             $('#magicArea').html(result);
          },
          complete: function(){
            $("#unlinkPackageModal").modal("show");
          }
        });
      }
      
      function hideModal(){
        $("[name='coreAssertionEdit']").modal('hide');
      }

      function showCoreAssertionModal(){

        $("[name='coreAssertionEdit']").modal('show');
       
      }
      
      <g:if test="${editable}">

      $(document).ready(function() {
           
        $(".announce").click(function(){
           var id = $(this).data('id');
           $('#modalComments').load('<g:createLink controller="alert" action="commentsFragment" />/'+id);
           $('#modalComments').modal('show');
         });

         $('#collapseableSubDetails').on('show', function() {
            $('.hidden-license-details i').removeClass('icon-plus').addClass('icon-minus');
        });

        // Reverse it for hide:
        $('#collapseableSubDetails').on('hide', function() {
            $('.hidden-license-details i').removeClass('icon-minus').addClass('icon-plus');
        });
      });

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

      <g:if test="${params.asAt && params.asAt.length() > 0}"> $(function() {
        document.body.style.background = "#fcf8e3";
      });</g:if>
      
    </r:script>
  </body>
</html>
