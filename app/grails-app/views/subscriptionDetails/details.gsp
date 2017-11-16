<%@ page import="com.k_int.kbplus.Subscription" %>
<%@ page import="java.text.SimpleDateFormat"%>
<%
  def dateFormater = new SimpleDateFormat(session.sessionPreferences?.globalDateFormat)
%>
<r:require module="annotations" />

<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.details.label', default:'Subscription Details')}</title>
  </head>
  <body>
  
    <div>
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <g:if test="${params.shortcode}">
          <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}"> ${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'subscription.details.label', default:'Subscription Details')}</g:link> </li>
        
      

        </li>
        <g:if test="${editable}">
          <li class="pull-right"><span class="badge badge-warning">${message(code:'default.editable', default:'Editable')}</span>&nbsp;</li>
        </g:if>
        <li class="pull-right"><g:annotatedLabel owner="${subscriptionInstance}" property="detailsPageInfo"></g:annotatedLabel>&nbsp;</li>
      </ul>
    </div>

  <semui:messages data="${flash}" />

    <div>
      <g:if test="${params.asAt}"><h1>${message(code:'myinst.subscriptionDetails.snapshot', args:[params.asAt])} </h1></g:if>
       <h1><g:xEditable owner="${subscriptionInstance}" field="name" /></h1>
       <g:render template="nav"  />
    </div>

    <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>


    <div id="collapseableSubDetails" class="ui grid">
        <div class="twelve wide column">
            <br/>
            <h6>${message(code:'subscription.information.label', default:'Subscription Information')}</h6>
            <div class="inline-lists"> 
              <dl><dt>${message(code:'subscription.details.isPublic', default:'Public?')}</dt>
                  <dd>
                      <g:xEditableRefData owner="${subscriptionInstance}" field="isPublic" config='YN' />
                  </dd>
              </dl>
              <dl>
                <dt>${message(code:'license')}</dt>
                <dd>
                  <g:if test="${subscriptionInstance.subscriber || subscriptionInstance.consortia}">
                    <g:xEditableRefData owner="${subscriptionInstance}" field="owner" dataController="subscriptionDetails" dataAction="possibleLicensesForSubscription" />
                    <g:if test="${subscriptionInstance.owner != null}">
                    (
                      <g:link controller="licenseDetails" action="index" id="${subscriptionInstance.owner.id}">${message(code:'default.button.link.label', default:'Link')}</g:link> 
                      <g:link controller="licenseDetails" action="index" target="new" id="${subscriptionInstance.owner.id}"><i class="icon-share-alt"></i></g:link>
                    )
                    </g:if>
                  </g:if>
                  <g:else>N/A (Subscription offered)</g:else>
                  </dd>
              </dl>
              <dl>
                  <dt>${message(code:'package.show.pkg_name', default:'Package Name')}</dt>
                  <dd>
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
                              <a onclick="unlinkPackage(${sp.pkg.id})" style="cursor:pointer;">${message(code:'default.button.unlink.label', default:'Unlink')} <i class="fa fa-times"></i></a>
                            </g:if>
                          </td>
                        </tr>
                      </g:each>
                    </tbody>
                  </table>
                  </dd>
              </dl>

               <dl><dt><g:annotatedLabel owner="${subscriptionInstance}" property="ids">${message(code:'subscription.identifiers.label', default:'Subscription Identifiers')}</g:annotatedLabel></dt>
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

                           <laser:formAddIdentifier owner="${subscriptionInstance}" uniqueCheck="yes" uniqueWarningText="${message(code:'subscription.details.details.duplicate.warn')}">
                              ${message(code:'identifier.select.text', args:['JC:66454'])}
                           </laser:formAddIdentifier>

                        </g:if>
                   </dd>
               </dl>

               <dl><dt>${message(code:'default.startDate.label', default:'Start Date')}</dt><dd><g:xEditable owner="${subscriptionInstance}" field="startDate" type="date"/></dd></dl>

               <dl><dt>${message(code:'default.endDate.label', default:'End Date')}</dt><dd><g:xEditable owner="${subscriptionInstance}" field="endDate" type="date"/></dd></dl>

               <dl><dt>${message(code:'financials.label', default:'Financials')}</dt>
                   <dd>
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
                   </dd>
               </dl>


               <dl><dt>${message(code:'subscription.manualRenewalDate.label', default:'Manual Renewal Date')}</dt><dd><g:xEditable owner="${subscriptionInstance}" field="manualRenewalDate" type="date"/></dd></dl>
               <dL><dt>${message(code:'license.details.incoming.child', default:'Child')} </dt><dd>
                        <g:xEditableRefData owner="${subscriptionInstance}" field="isSlaved" config='YN'/>
               </dd></dL>
               <dl>
                 <dt>
                   <g:annotatedLabel owner="${subscriptionInstance}" property="nominalPlatform">${message(code:'package.nominalPlatform', default:'Nominal Platform')}</g:annotatedLabel>
                 </dt><dd>
                    <g:each in="${subscriptionInstance.packages}" var="sp">
                        ${sp.pkg?.nominalPlatform?.name}<br/>
                    </g:each></dd></dl>

             <dl>
                <dt>${message(code:'financials.cancellationAllowances', default:'Cancellation Allowances')}</dt>
                <dd>
                  <g:xEditable owner="${subscriptionInstance}" field="cancellationAllowances" />
                </dd>
              </dl>


               <dl><dt><label class="control-label" for="licenseeRef">${message(code:'org.links.label', default:'Org Links')}</label></dt><dd>
                       <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:subscriptionInstance?.orgRelations,editmode:editable]}" />
                     </dd>
               </dl>

               <g:if test="${params.mode=='advanced'}">
                 <dl><dt><label class="control-label" for="licenseeRef">${message(code:'default.status.label', default:'Status')}</label></dt><dd>
                      <g:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status'/>
                     </dd>
               </dl>
               </g:if>

               <div class="clear-fix"></div>
            </div>
        </div>

        <div class="four wide column">
          <g:render template="documents" contextPath="../templates" model="${[ ownobj:subscriptionInstance, owntp:'subscription']}" />
          <g:render template="notes" contextPath="../templates" model="${[ ownobj:subscriptionInstance, owntp:'subscription']}" />
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
