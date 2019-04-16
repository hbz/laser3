<%@ page import="de.laser.helper.RDStore; java.math.MathContext; com.k_int.kbplus.Subscription; com.k_int.kbplus.Links; java.text.SimpleDateFormat" %>
<%@ page import="com.k_int.properties.PropertyDefinition" %>
<%@ page import="com.k_int.kbplus.RefdataCategory" %>
<laser:serviceInjection />
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
            <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
            <g:render template="/templates/debug/orgRoles"  model="[debug: subscriptionInstance.orgRelations]" />
            <g:render template="/templates/debug/prsRoles"  model="[debug: subscriptionInstance.prsLinks]" />
        </semui:debugInfo>

        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

        <g:if test="${params.asAt}">
            <h1 class="ui icon header"><semui:headerIcon />${message(code:'myinst.subscriptionDetails.snapshot', args:[params.asAt])}</h1>
        </g:if>

        <h1 class="ui icon header"><semui:headerIcon />
            <semui:xEditable owner="${subscriptionInstance}" field="name" />
        </h1>
        <semui:auditButton auditable="[subscriptionInstance, 'name']"/>
        <semui:anualRings object="${subscriptionInstance}" controller="subscription" action="show" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <g:render template="nav" />

        <semui:objectStatus object="${subscriptionInstance}" status="${subscriptionInstance.status}" />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
        <g:render template="message" />
    </g:if>


    <g:render template="/templates/meta/identifier" model="${[object: subscriptionInstance, editable: editable]}" />

        <semui:messages data="${flash}" />

        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':subscriptionInstance]}"/>


    <div id="collapseableSubDetails" class="ui stackable grid">
        <div class="twelve wide column">

            <div class="la-inline-lists">
                <div class="ui two stackable cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="startDate" type="date"/></dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'startDate']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="endDate" type="date"/></dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'endDate']"/></dd>
                            </dl>
                            <% /*
                            <dl>
                                <dt>${message(code:'subscription.manualRenewalDate.label', default:'Manual Renewal Date')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualRenewalDate" type="date"/></dd>
                            </dl>
                            */ %>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.manualCancellationlDate.label')}</dt>
                                <dd><semui:xEditable owner="${subscriptionInstance}" field="manualCancellationDate" type="date"/></dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'manualCancellationDate']" /></dd>
                            </dl>

                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.details.status')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="status" config='Subscription Status' constraint="removeValue_deleted" /></dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'status']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.details.type')}</dt>
                                <dd>
                                    <%-- TODO: subscribers may not edit type, but admins and yoda --%>
                                    <g:if test="${subscriptionInstance.getAllSubscribers().contains(contextOrg)}">
                                        ${subscriptionInstance.type?.getI10n('value')}
                                    </g:if>
                                    <g:else>
                                        <semui:xEditableRefData owner="${subscriptionInstance}" field="type" config='Subscription Type' />
                                    </g:else>
                                </dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'type']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="form" config='Subscription Form'/></dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'form']"/></dd>
                            </dl>
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                                <dd><semui:xEditableRefData owner="${subscriptionInstance}" field="resource" config='Subscription Resource'/></dd>
                                <dd><semui:auditButton auditable="[subscriptionInstance, 'resource']"/></dd>
                            </dl>
                            <g:if test="${subscriptionInstance.instanceOf && (contextOrg?.id == subscriptionInstance.getConsortia()?.id)}">
                                <dl>
                                    <dt class="control-label">${message(code:'subscription.isInstanceOfSub.label')}</dt>
                                    <dd>
                                        <g:link controller="subscription" action="show" id="${subscriptionInstance.instanceOf.id}">${subscriptionInstance.instanceOf}</g:link>
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
                        <h5 class="ui header">
                           Aktuelle Lizenz...

                        </h5>
                        <g:if test="${links.entrySet()}">
                            <table class="ui three column la-selectable table">
                                <g:each in="${links.entrySet().toSorted()}" var="linkTypes">
                                    <g:if test="${linkTypes.getValue().size() > 0}">
                                        <g:each in="${linkTypes.getValue()}" var="link">
                                            <tr>
                                                <th scope="row" class="control-label la-js-dont-hide-this-card">${linkTypes.getKey()}</th>
                                                <td>
                                                    <g:set var="pair" value="${link.getOther(subscriptionInstance)}"/>
                                                    <g:set var="sdf" value="${new SimpleDateFormat('dd.MM.yyyy')}"/>
                                                    <g:link controller="subscription" action="show" id="${pair.id}">
                                                        ${pair.name}
                                                    </g:link><br>
                                                    ${pair.startDate ? sdf.format(pair.startDate) : ""}–${pair.endDate ? sdf.format(pair.endDate) : ""}
                                                </td>
                                                <td class="right aligned">
                                                    <div class="ui icon buttons">
                                                        <g:render template="/templates/links/subLinksModal"
                                                                  model="${[tmplText:message(code:'subscription.details.editLink'),
                                                                            tmplIcon:'write',
                                                                            tmplCss: 'la-selectable-button',
                                                                            tmplID:'editLink',
                                                                            tmplModalID:"sub_edit_link_${link.id}",
                                                                            editmode: editable,
                                                                            context: "${subscriptionInstance.class.name}:${subscriptionInstance.id}",
                                                                            link: link
                                                                  ]}" />
                                                    </div>

                                                    <div class="ui icon negative buttons">
                                                        <g:if test="${editable}">
                                                            <g:link class="ui mini icon button la-selectable-button js-open-confirm-modal"
                                                                    data-confirm-term-what="subscription"
                                                                    data-confirm-term-how="unlink"
                                                                    controller="ajax" action="delete" params='[cmd: "deleteLink", oid: "${link.class.name}:${link.id}"]'>
                                                                <i class="unlink icon"></i>
                                                            </g:link>
                                                        </g:if>
                                                    </div>

                                                </td>
                                            </tr>
                                        </g:each>
                                    </g:if>
                                </g:each>
                            </table>
                        </g:if>
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

                            <table class="ui three column la-selectable table">
                                <g:each in="${subscriptionInstance.packages.sort{it.pkg.name}}" var="sp">
                                    <tr>
                                    <th scope="row" class="control-label la-js-dont-hide-this-card">${message(code:'subscription.packages.label')}</th>
                                        <td>
                                            <g:link controller="package" action="show" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>

                                            <g:if test="${sp.pkg?.contentProvider}">
                                                (${sp.pkg?.contentProvider?.name})
                                            </g:if>
                                        </td>
                                        <td class="right aligned">
                                            <g:if test="${editable}">

                                                <div class="ui icon negative buttons">
                                                    <button class="ui button la-selectable-button" onclick="unlinkPackage(${sp.pkg.id})">
                                                        <i class="unlink icon"></i>
                                                    </button>
                                                </div>
                                                <br />
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:each>
                            </table>

                            <table class="ui three column la-selectable table">
                                <tr>
                                    <th scope="row" class="control-label la-js-dont-hide-this-card">${message(code:'license')}</th>
                                    <td>
                                        <g:if test="${subscriptionInstance.owner == null}">
                                            <semui:xEditableRefData owner="${subscriptionInstance}" field="owner" datacontroller="subscription" dataAction="possibleLicensesForSubscription" />
                                        </g:if>
                                        <g:else>
                                            <g:link controller="license" action="show" id="${subscriptionInstance.owner.id}">
                                                ${subscriptionInstance.owner}
                                            </g:link>
                                        </g:else>
                                        %{-- <g:if test="${subscriptionInstance.owner != null}">
                                             [<g:link controller="license" action="show" id="${subscriptionInstance.owner.id}">
                                                 <i class="icon-share-alt"></i> ${message(code:'default.button.show.label', default:'Show')}
                                             </g:link>]
                                         </g:if>--}%
                                    </td>
                                    <td class="right aligned">
                                        <g:if test="${editable && subscriptionInstance.owner}">
                                            <div class="ui icon negative buttons">
                                                <a href="?cmd=unlinkLicense" class="ui button la-selectable-button">
                                                    <i class="unlink icon"></i>
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

                    <g:render template="/templates/links/orgLinksAsList"
                              model="${[roleLinks: visibleOrgRelations,
                                        roleObject: subscriptionInstance,
                                        roleRespValue: 'Specific subscription editor',
                                        editmode: editable
                              ]}" />

                    <div class="ui la-vertical buttons la-js-hide-this-card">

                        <g:render template="/templates/links/orgLinksSimpleModal"
                                  model="${[linkType: subscriptionInstance?.class?.name,
                                            parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id,
                                            property: 'orgs',
                                            recip_prop: 'sub',
                                            tmplRole: RDStore.OR_PROVIDER,
                                            tmplEntity:'Anbieter',
                                            tmplText:'Anbieter mit dieser Lizenz verknüpfen',
                                            tmplButtonText:'Anbieter verknüpfen',
                                            tmplModalID:'modal_add_provider',
                                            editmode: editable,
                                            orgList: availableProviderList,
                                            signedIdList: existingProviderIdList
                                  ]}" />

                        <g:render template="/templates/links/orgLinksSimpleModal"
                                    model="${[linkType: subscriptionInstance?.class?.name,
                                            parent: subscriptionInstance.class.name + ':' + subscriptionInstance.id,
                                            property: 'orgs',
                                            recip_prop: 'sub',
                                            tmplRole: RDStore.OR_AGENCY,
                                            tmplEntity: 'Lieferanten',
                                            tmplText: 'Lieferanten mit dieser Lizenz verknüpfen',
                                            tmplButtonText: 'Lieferant verknüpfen',
                                            tmplModalID:'modal_add_agency',
                                            editmode: editable,
                                            orgList: availableAgencyList,
                                            signedIdList: existingAgencyIdList
                                    ]}" />

                    </div><!-- la-js-hide-this-card -->

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

                <%-- FINANCE, to be reactivated as of ERMS-943 --%>
                <%-- assemble data on server side --%>
                <div class="ui card la-dl-no-table">
                    <div class="content">
                        <g:if test="${costItemSums.ownCosts && contextOrg.id != subscription.getConsortia()?.id}">
                            <h5 class="ui header">${message(code:'financials.label', default:'Financials')} : ${message(code:'financials.tab.ownCosts')}</h5>
                            <g:render template="financials" model="[data:costItemSums.ownCosts]"/>
                        </g:if>
                        <g:if test="${costItemSums.consCosts}">
                            <h5 class="ui header">${message(code:'financials.label', default:'Financials')} : ${message(code:'financials.tab.consCosts')}</h5>
                            <g:render template="financials" model="[data:costItemSums.consCosts]"/>
                        </g:if>
                        <g:elseif test="${costItemSums.subscrCosts}">
                            <h5 class="ui header">${message(code:'financials.label', default:'Financials')} : ${message(code:'financials.tab.subscrCosts')}</h5>
                            <g:render template="financials" model="[data:costItemSums.subscrCosts]"/>
                        </g:elseif>
                    </div>
                </div>
                <g:if test="${usage}">
                    <div class="ui card la-dl-no-table hidden">
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
                            <div class="ui divider"></div>
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
        </div><!-- .twelve -->

        <aside class="four wide column la-sidekick">
            <g:render template="/templates/aside1" model="${[ownobj:subscriptionInstance, owntp:'subscription']}" />
        </aside><!-- .four -->

    </div><!-- .grid -->


    <div id="magicArea"></div>

    <r:script language="JavaScript">

      function unlinkPackage(pkg_id){
        var req_url = "${createLink(controller:'subscription', action:'unlinkPackage', params:[subscription:subscriptionInstance.id])}&package="+pkg_id

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

         $('#collapseableSubDetails').on('show', function() {
            $('.hidden-license-details i').removeClass('icon-plus').addClass('icon-minus');
        });

        // Reverse it for hide:
        $('#collapseableSubDetails').on('hide', function() {
            $('.hidden-license-details i').removeClass('icon-minus').addClass('icon-plus');
        });
      });

      </g:if>

      <g:if test="${params.asAt && params.asAt.length() > 0}"> $(function() {
        document.body.style.background = "#fcf8e3";
      });</g:if>

    </r:script>
  </body>
</html>
