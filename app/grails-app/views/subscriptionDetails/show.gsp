<%@ page import="java.math.MathContext; com.k_int.kbplus.Subscription; com.k_int.kbplus.Links" %>
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
            <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'myinst.subscriptionDetails.snapshot', args:[params.asAt])}</h1>
        </g:if>

        <h1 class="ui left aligned icon header"><semui:headerIcon />
            <%
                // TODO: custom tag, if more usages

                if (subscriptionInstance.instanceOf && ! subscriptionInstance.instanceOf.isTemplate()) {
                    if (subscriptionInstance.instanceOf?.getAuditConfig('name')) {
                        if (subscriptionInstance.isSlaved?.value?.equalsIgnoreCase('yes')) {
                            out << '&nbsp; <span data-tooltip="Wert wird automatisch geerbt." data-position="top right"><i class="icon h1-icon-overwrite thumbtack blue"></i></span>'
                        }
                        else {
                            out << '&nbsp; <span data-tooltip="Wert wird geerbt." data-position="top right"><i class="icon h1-icon-overwrite thumbtack grey"></i></span>'
                        }
                    }
                }
                else {
                    if (subscriptionInstance.getAuditConfig('name')) {
                        out << '&nbsp; <span data-tooltip="Wert wird vererbt." data-position="top right"><i class="icon h1-icon-overwrite thumbtack blue"></i></span>'
                    }
                }
            %>
            <semui:xEditable owner="${subscriptionInstance}" field="name" />
            <semui:anualRings object="${subscriptionInstance}" controller="subscriptionDetails" action="show" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
        </h1>

    <g:render template="nav" />

        <semui:objectStatus object="${subscriptionInstance}" status="${subscriptionInstance.status}" />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
        <g:render template="message" />
    </g:if>


    <g:render template="/templates/meta/identifier" model="${[object: subscriptionInstance, editable: editable]}" />

        <semui:messages data="${flash}" />

        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>


    <div id="collapseableSubDetails" class="ui grid">
        <div class="twelve wide column">

            <div class="la-inline-lists">
                <div class="ui two cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <semui:dtAuditCheck message="subscription.startDate.label" auditable="[subscriptionInstance, 'startDate']"/>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="startDate" type="date"/></dd>
                            </dl>
                            <dl>
                                <semui:dtAuditCheck message="subscription.endDate.label" auditable="[subscriptionInstance, 'endDate']"/>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="endDate" type="date"/></dd>
                            </dl>
                            <% /*
                            <dl>
                                <dt>${message(code:'subscription.manualRenewalDate.label', default:'Manual Renewal Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualRenewalDate" type="date"/></dd>
                            </dl>
                            */ %>
                            <dl>
                                <semui:dtAuditCheck message="subscription.manualCancellationlDate.label" auditable="[subscriptionInstance, 'manualCancellationDate']" />
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualCancellationDate" type="date"/></dd>
                            </dl>

                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <semui:dtAuditCheck message="subscription.details.status" auditable="[subscriptionInstance, 'status']"/>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status' /></dd>
                            </dl>
                            <dl>
                                <semui:dtAuditCheck message="subscription.details.type" auditable="[subscriptionInstance, 'type']"/>
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
                            <dl>
                                <semui:dtAuditCheck message="subscription.form.label" auditable="[subscriptionInstance, 'form']"/>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="form" config='Subscription Form'/></dd>
                            </dl>
                            <dl>
                                <semui:dtAuditCheck message="subscription.resource.label" auditable="[subscriptionInstance, 'resource']"/>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="resource" config='Subscription Resource'/></dd>
                            </dl>
                            <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
                                <dl>
                                    <dt class="control-label">${message(code:'subscription.isInstanceOfSub.label')}</dt>
                                    <dd>
                                        <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}">${subscriptionInstance.instanceOf}</g:link>
                                    </dd>
                                </dl>

                                <dl>
                                    <dt class="control-label">
                                        ${message(code:'license.details.linktoLicense.pendingChange', default:'Automatically Accept Changes?')}
                                    </dt>
                                    <dd>
                                        <semui:xEditableRefData owner="${subscriptionInstance}" field="isSlaved" config='YN'/>
                                    </dd>
                                </dl>
                            </g:if>
                        </div>
                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl class="control-label">
                            ${message(code:'subscription.details.linksHeader')}
                        </dl>
                        <g:each in="${links.entrySet()}" var="linkTypes">
                            <g:if test="${linkTypes.getValue().size() > 0}">
                                <dl>
                                    <dt>
                                        ${linkTypes.getKey()}
                                    </dt>
                                </dl>
                                <dl>
                                    <g:each in="${linkTypes.getValue()}" var="link">
                                        <dd>
                                            <g:set var="pair" value="${link.getOther(subscriptionInstance)}"/>
                                            <g:set var="sdf" value="${new SimpleDateFormat('dd.MM.yyyy')}"/>
                                            <g:link controller="subscriptionDetails" action="show" id="${pair.id}">#${pair.id}</g:link>: ${pair.name} (${sdf.format(pair.startDate)} - ${pair.endDate ? sdf.format(pair.endDate) : ""})
                                        </dd>
                                        <dd>
                                            <g:render template="/templates/links/subLinksModal"
                                                      model="${[tmplText:message(code:'subscription.details.editLink'),
                                                                tmplID:'editLink',
                                                                tmplButtonText:message(code:'subscription.details.editLink'),
                                                                tmplModalID:"sub_edit_link_${link.id}",
                                                                editmode: editable,
                                                                context: "${subscriptionInstance.class.name}:${subscriptionInstance.id}",
                                                                link: link
                                                      ]}" />
                                            <g:link class="ui icon negative button js-open-confirm-modal"
                                                    data-confirm-term-content="${message(code:'subscription.details.confirmDeleteLink')}"
                                                    data-confirm-term-how="delete"
                                                    controller="ajax" action="delete" params='[cmd: "deleteLink", oid: "${link.class.name}:${link.id}"]'>
                                                <i class="trash alternate icon"></i>
                                            </g:link>
                                        </dd>
                                    </g:each>
                                </dl>
                            </g:if>
                        </g:each>
                        <div class="ui la-vertical buttons">
                            <g:render template="/templates/links/subLinksModal"
                                      model="${[tmplText:message(code:'subscription.details.addLink'),
                                                tmplID:'addLink',
                                                tmplButtonText:message(code:'subscription.details.addLink'),
                                                tmplModalID:'sub_add_link',
                                                editmode: editable,
                                                context: "${subscriptionInstance.class.name}:${subscriptionInstance.id}"
                                      ]}" />
                        </div>
                    </div>
                </div>

                <div class="ui card la-js-hideable hidden">
                        <div class="content">

                            <table class="ui la-selectable table">
                                <colgroup>
                                    <col width="130" />
                                    <col width="300" />
                                    <col width="430"/>
                                </colgroup>
                                <g:each in="${subscriptionInstance.packages.sort{it.pkg.name}}" var="sp">
                                    <tr>
                                    <th scope="row" class="control-label la-js-dont-hide-this-card">${message(code:'subscription.packages.label')}</th>
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
                                    <th scope="row" class="control-label la-js-dont-hide-this-card">${message(code:'license')}</th>
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


                <div class="ui card la-js-hideable hidden">
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
                    <div class="ui la-vertical buttons la-js-hide-this-card">
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
                    </div>

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

                    <div class="ui card la-dl-no-table la-js-hideable hidden">
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
                    <div class="ui card la-dl-no-table la-js-hideable hidden">
                        <div class="content">
                            <g:if test="${subscriptionInstance.costItems}">
                                <dl>
                                    <dt class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.details.costPerUse.header')}</dt>
                                    <dd><g:formatNumber number="${totalCostPerUse}" type="currency"
                                                        currencyCode="${currencyCode}" maxFractionDigits="2"
                                                        minFractionDigits="2" roundingMode="HALF_UP"/>
                                        (${message(code: 'subscription.details.costPerUse.usedMetric')}: ${costPerUseMetric})
                                    </dd>
                                </dl>
                                <div class="ui divider"></div>
                            </g:if>
                            <dl>
                                <dt class="control-label la-js-dont-hide-this-card">${message(code: 'default.usage.label')}</dt>
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
                                                <g:set var="reportMetric" value="${y_axis_labels[counter++]}" />
                                                <td>${reportMetric}</td>
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
                                                                     vendors     : natStatSupplierId,
                                                                     institutions: statsWibid,
                                                                     reports     : reportMetric.split(':')[0],
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

                <div id="new-dynamic-properties-block">

                    <g:render template="properties" model="${[
                            subscriptionInstance: subscriptionInstance,
                            authorizedOrgs: authorizedOrgs
                    ]}" />

                </div><!-- #new-dynamic-properties-block -->

               <div class="clear-fix"></div>
            </div>
        </div>

        <aside class="four wide column la-sidekick">
            <g:render template="/templates/tasks/card" model="${[ownobj:subscriptionInstance, owntp:'subscription', css_class:'hidden']}"  />
            <g:render template="/templates/documents/card" model="${[ownobj:subscriptionInstance, owntp:'subscription', css_class:'hidden']}" />
            <g:render template="/templates/notes/card" model="${[ownobj:subscriptionInstance, owntp:'subscription', css_class:'hidden']}" />
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
