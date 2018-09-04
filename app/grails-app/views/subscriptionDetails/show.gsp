<%@ page import="java.math.MathContext; com.k_int.kbplus.Subscription" %>
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
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.label', default:'Subscription Details')}</title>
        <g:javascript src="properties.js"/>
    </head>
    <body>

        <semui:debugInfo>
            <g:render template="/templates/debug/orgRoles" model="[debug: subscriptionInstance.orgRelations]" />
            <g:render template="/templates/debug/prsRoles" model="[debug: subscriptionInstance.prsLinks]" />
        </semui:debugInfo>

        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

        <g:if test="${params.asAt}">
            <h1 class="ui header"><semui:headerIcon />${message(code:'myinst.subscriptionDetails.snapshot', args:[params.asAt])}</h1>
        </g:if>

        <h1 class="ui header"><semui:headerIcon />
            <semui:xEditable owner="${subscriptionInstance}" field="name" />

            <span class="la-forward-back">
                <g:if test="${navPrevSubscription}">
                    <g:link controller="subscriptionDetails" action="show" params="[id:navPrevSubscription.id]"><i class="chevron left icon"></i></g:link>
                </g:if>
                <g:else>
                    <i class="chevron left icon disabled"></i>
                </g:else>
                <g:if test="${navNextSubscription}">
                    <g:link controller="subscriptionDetails" action="show" params="[id:navNextSubscription.id]"><i class="chevron right icon"></i></g:link>
                </g:if>
                <g:else>
                    <i class="chevron right icon disabled"></i>
                </g:else>
           </span>
        </h1>

        <g:render template="nav" />

        <semui:objectStatus object="${subscriptionInstance}" status="${subscriptionInstance.status}" />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg == subscriptionInstance.getConsortia())}">
        <div class="ui negative message">
            <div class="header"><g:message code="myinst.message.attention" /></div>
            <p>
                <g:message code="myinst.subscriptionDetails.message.ChildView" />
                    <span class="ui label">${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.name}.join(',')}</span>.
                <g:message code="myinst.subscriptionDetails.message.ConsortialView" />
                    <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}"><g:message code="myinst.subscriptionDetails.message.here" /></g:link>.
            </p>
        </div>
    </g:if>

        <semui:meta>
            <div class="inline-lists">
                <dl>
                    <g:if test="${subscriptionInstance.globalUID}">
                        <dt class="control-label"><g:message code="subscription.globalUID.label" default="Global UID" /></dt>
                        <dd>
                            <g:fieldValue bean="${subscriptionInstance}" field="globalUID"/>
                        </dd>
                    </g:if>

                    <dt class="control-label">
                        <g:message code="org.ids.label" default="Ids" />
                        (<g:annotatedLabel owner="${subscriptionInstance}" property="ids">${message(code:'subscription.identifiers.label', default:'Subscription Identifiers')}</g:annotatedLabel>)
                    </dt>
                    <dd>
                        <table class="ui single line table ignore-floatThead">
                            <thead>
                            <tr>
                                <th>${message(code:'default.authority.label', default:'Authority')}</th>
                                <th>${message(code:'default.identifier.label', default:'Identifier')}</th>
                                <th>${message(code:'default.actions.label', default:'Actions')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:set var="id_label" value="${message(code:'identifier.label', default:'Identifier')}"/>
                            <g:each in="${subscriptionInstance.ids.sort{it.identifier.ns.ns}}" var="io">
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

            <div class="la-inline-lists">
                <div class="ui two cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code:'subscription.startDate.label', default:'Start Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="startDate" type="date"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code:'subscription.endDate.label', default:'End Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="endDate" type="date"/></dd>
                            </dl>
                            <% /*
                            <dl>
                                <dt>${message(code:'subscription.manualRenewalDate.label', default:'Manual Renewal Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualRenewalDate" type="date"/></dd>
                            </dl>
                            */ %>
                            <dl>
                                <dt class="control-label">${message(code:'subscription.manualCancellationlDate.label', default:'Manual Cancellation Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualCancellationDate" type="date"/></dd>
                            </dl>

                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code:'subscription.details.status', default:'Status')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status' /></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code:'subscription.details.type', default:'Type')}</dt>
                                <dd>
                                    <%-- TODO: subscribers may not edit type, but admins and yoda --%>
                                    <g:if test="${subscriptionInstance.getAllSubscribers().contains(contextOrg)}">
                                        ${subscriptionInstance.type?.getI10n('value')}
                                    </g:if>
                                    <g:else>
                                        <semui:xEditableRefData owner="${subscriptionInstance}" field="type" config='Subscription Type' />
                                    </g:else>
                                </dd>
                            </dl>
                            <g:if test="${subscriptionInstance.instanceOf && (contextOrg == subscriptionInstance.getConsortia())}">
                                <dl>
                                    <dt class="control-label">${message(code:'subscription.isInstanceOfSub.label')}</dt>
                                    <dd>
                                        <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}">${subscriptionInstance.instanceOf}</g:link>
                                    </dd>
                                </dl>
                            </g:if>
                        </div>
                    </div>
                </div>

                <div class="ui card">
                        <div class="content">

                            <table class="ui la-selectable table">
                                <colgroup>
                                    <col width="130" />
                                    <col width="300" />
                                    <col width="430"/>
                                </colgroup>
                                <g:each in="${subscriptionInstance.packages.sort{it.pkg.name}}" var="sp">
                                <tr>
                                <th scope="row" class="control-label">${message(code:'subscription.packages.label')}</th>
                                    <td>
                                        <g:link controller="packageDetails" action="show" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>

                                        <g:if test="${sp.pkg?.contentProvider}">
                                            (${sp.pkg?.contentProvider?.name})
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${editable}">

                                            <div class="ui mini icon buttons">
                                                <button class="ui button la-selectable-button" onclick="unlinkPackage(${sp.pkg.id})">
                                                    <i class="times icon red"></i>${message(code:'default.button.unlink.label')}
                                                </button>
                                            </div>
                                            <br />
                                        </g:if>
                                    </td>
                                </tr>
                                </g:each>
                            </table>

                            <table class="ui la-selectable table">
                                <colgroup>
                                    <col width="130" />
                                    <col width="300" />
                                    <col width="430"/>
                                </colgroup>
                                <tr>
                                    <th scope="row" class="control-label">${message(code:'license')}</th>
                                    <td>
                                        <g:if test="${subscriptionInstance.owner == null}">
                                            <semui:xEditableRefData owner="${subscriptionInstance}" field="owner" dataController="subscriptionDetails" dataAction="possibleLicensesForSubscription" />
                                        </g:if>
                                        <g:else>
                                            <g:link controller="licenseDetails" action="show" id="${subscriptionInstance.owner.id}">
                                                ${subscriptionInstance.owner}
                                            </g:link>
                                        </g:else>
                                        %{-- <g:if test="${subscriptionInstance.owner != null}">
                                             [<g:link controller="licenseDetails" action="show" id="${subscriptionInstance.owner.id}">
                                                 <i class="icon-share-alt"></i> ${message(code:'default.button.show.label', default:'Show')}
                                             </g:link>]
                                         </g:if>--}%
                                    </td>
                                    <td>
                                        <g:if test="${editable && subscriptionInstance.owner}">
                                            <div class="ui mini icon buttons">
                                                <a href="?cmd=unlinkLicense" class="ui button la-selectable-button">
                                                    <i class="times icon red"></i>${message(code:'default.button.unlink.label')}
                                                </a>
                                            </div>
                                            <br />
                                        </g:if>
                                    </td>
                            </table>

                            <g:if test="${editable}">
                                <g:if test="${subscriptionInstance.owner == null}">
                                    <g:link  controller="myInstitution" class="ui button la-new-item" action="emptyLicense" params="[sub: subscriptionInstance.id]">${message(code:'license.add.blank')}
                                    </g:link>
                                </g:if>
                            </g:if>
                        </div>
                    </div>


                <div class="ui card">
                        <div class="content">


                <% /*
                <dl>
                    <dt>${message(code:'subscription.details.isPublic', default:'Public?')}</dt>
                    <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="isPublic" config='YN' /></dd>
                </dl>
                */ %>

                    <g:render template="/templates/links/orgLinksAsList"
                              model="${[roleLinks: visibleOrgRelations,
                                        roleObject: subscriptionInstance,
                                        roleRespValue: 'Specific subscription editor',
                                        editmode: editable
                              ]}" />

                    <g:render template="/templates/links/orgLinksModal"
                              model="${[linkType: subscriptionInstance?.class?.name,
                                        parent: subscriptionInstance.class.name+':'+subscriptionInstance.id,
                                        property: 'orgs',
                                        recip_prop: 'sub',
                                        tmplRole: com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'),
                                        tmplText:'Anbieter hinzufügen',
                                        tmplID:'ContentProvider',
                                        tmplButtonText:'Anbieter hinzufügen',
                                        tmplModalID:'osel_add_modal_anbieter',
                                        editmode: editable
                              ]}" />

                    <g:render template="/templates/links/orgLinksModal"
                                model="${[linkType: subscriptionInstance?.class?.name,
                                    parent: subscriptionInstance.class.name+':'+subscriptionInstance.id,
                                    property: 'orgs',
                                    recip_prop: 'sub',
                                    tmplRole: com.k_int.kbplus.RefdataValue.getByValueAndCategory('Agency', 'Organisational Role'),
                                    tmplText:'Lieferant hinzufügen',
                                    tmplID:'ContentProvider',
                                    tmplButtonText:'Lieferant hinzufügen',
                                    tmplModalID:'osel_add_modal_agentur',
                                    editmode: editable
                                ]}" />

                <% /*
               <dl>
                    <dt><label class="control-label" for="licenseeRef">${message(code:'org.links.label', default:'Org Links')}</label></dt><dd>
                        <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:visibleOrgRelations,editmode:editable]}" />
                    </dd>
               </dl>
               */ %>

               <% /*g:if test="${params.mode=='advanced'}">
                 <dl><dt><label class="control-label" for="licenseeRef">${message(code:'default.status.label', default:'Status')}</label></dt><dd>
                      <semui:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status'/>
                     </dd>
               </dl>
               </g:if */ %>

                <%--
                    <g:render template="/templates/links/prsLinksAsList" model="[tmplShowFunction:false]"/>

                    <g:render template="/templates/links/prsLinksModal"
                          model="['subscription': subscriptionInstance, parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id, role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id]"/>
                --%>

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


                        </div>
                    </div>

                <%-- FINANCE

                <g:if test="${subscriptionInstance.costItems}">

                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code:'financials.label', default:'Financials')}</dt>
                                <dd>
                                    <table class="ui single line  table">
                                        <thead>
                                        <tr>
                                            <th class="la-column-nowrap">${message(code:'financials.costItemCategory')}</th>
                                            <th>${message(code:'financials.costItemElement')}</th>
                                            <th>${message(code:'financials.costInLocalCurrency')}</th>
                                            <th>${message(code:'financials.costItemStatus', default:'Status')}</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <g:each in="${subscriptionInstance.costItems}" var="ci">
                                                <tr>
                                                    <td class="la-column-nowrap">${ci.costItemCategory?.getI10n('value')}</td>
                                                    <td>${ci.costItemElement?.getI10n('value')}</td>
                                                    <td>${ci.costInLocalCurrency} ${RefdataCategory.lookupOrCreate('Currency','EUR').getI10n('value')}</td>
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
                        </div>
                    </div>
                </g:if>

                FINANCE --%>
                <g:if test="${usage}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:if test="${subscriptionInstance.costItems}">
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.details.costPerUse.header')}</dt>
                                    <dd><g:formatNumber number="${totalCostPerUse}" type="currency"
                                                        currencyCode="${currencyCode}" maxFractionDigits="2"
                                                        minFractionDigits="2" roundingMode="HALF_UP"/></dd>
                                </dl>
                            </g:if>
                            <div class="ui divider"></div>
                            <dl>
                                <dt class="control-label">${message(code: 'default.usage.label')}</dt>
                                <dd>
                                    <table class="ui la-table-small celled la-table-inCard table">
                                        <thead>
                                        <tr>
                                            <th>${message(code: 'default.usage.reportType')}</th>
                                            <g:each in="${x_axis_labels}" var="l">
                                                <th>${l}</th>
                                            </g:each>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:set var="counter" value="${0}"/>
                                        <g:each in="${usage}" var="v">
                                            <tr>
                                                <td>${y_axis_labels[counter++]}</td>
                                                <g:each in="${v}" status="i" var="v2">
                                                    <td>
                                                        <laser:statsLink
                                                            base="${grailsApplication.config.statsApiUrl}"
                                                            module="statistics"
                                                            controller="default"
                                                            action="select"
                                                            target="_blank"
                                                            params="[mode        : usageMode,
                                                                     packages    : subscription.getCommaSeperatedPackagesIsilList(),
                                                                     institutions: statsWibid,
                                                                     years       : x_axis_labels[i]
                                                            ]"
                                                            title="Springe zu Statistik im Nationalen Statistikserver">
                                                            ${v2}
                                                        </laser:statsLink>
                                                    </td>
                                                </g:each>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </dd>
                            </dl>
                            <div class="ui divider"></div>
                            <dl>
                                <dt class="control-label">${message(code: 'default.usage.licenseGrid.header')}</dt>
                                <dd>
                                    <table class="ui la-table-small celled la-table-inCard table">
                                        <thead>
                                        <tr>
                                            <th>${message(code: 'default.usage.reportType')}</th>
                                            <g:each in="${l_x_axis_labels}" var="l">
                                                <th>${l}</th>
                                            </g:each>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:set var="counter" value="${0}"/>
                                        <g:each in="${lusage}" var="v">
                                            <tr>
                                                <td>${l_y_axis_labels[counter++]}</td>
                                                <g:each in="${v}" var="v2">
                                                    <td>${v2}</td>
                                                </g:each>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </g:if>
                <div class="ui card la-dl-no-table">
                    <div class="content">
                        <h5 class="ui header">${message(code:'subscription.properties')}</h5>

                        <div id="custom_props_div_props">
                    <g:render template="/templates/properties/custom" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: subscriptionInstance,
                            custom_props_div: "custom_props_div_props" ]}"/>
                        </div>
                    </div>
                </div>

                <r:script language="JavaScript">
                    $(document).ready(function(){
                        c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
                    });
                </r:script>

                <div class="ui card la-dl-no-table">
                    <div class="content">
                        <g:each in="${authorizedOrgs}" var="authOrg">
                            <g:if test="${authOrg.name == contextOrg?.name}">
                                <h5 class="ui header">${message(code:'subscription.properties.private')} ${authOrg.name}</h5>

                                <div id="custom_props_div_${authOrg.id}">
                                    <g:render template="/templates/properties/private" model="${[
                                            prop_desc: PropertyDefinition.SUB_PROP,
                                            ownobj: subscriptionInstance,
                                            custom_props_div: "custom_props_div_${authOrg.id}",
                                            tenant: authOrg]}"/>

                                    <r:script language="JavaScript">
                                        $(document).ready(function(){
                                            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                                        });
                                    </r:script>
                                </div>
                            </g:if>
                        </g:each>
                    </div>
                </div>

               <div class="clear-fix"></div>
            </div>
        </div>

        <aside class="four wide column la-sidekick">
            <g:render template="/templates/tasks/card" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
            <g:render template="/templates/documents/card" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
            <g:render template="/templates/notes/card" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
        </aside><!-- .four -->
    </div><!-- .grid -->


    <div id="magicArea"></div>

    <r:script language="JavaScript">

      function unlinkPackage(pkg_id){
        var req_url = "${createLink(controller:'subscriptionDetails', action:'unlinkPackage', params:[subscription:subscriptionInstance.id])}&package="+pkg_id

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
