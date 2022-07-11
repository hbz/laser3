<%@ page import="de.laser.config.ConfigMapper; de.laser.Person; de.laser.PersonRole; de.laser.Subscription; de.laser.Links; java.text.SimpleDateFormat;de.laser.properties.PropertyDefinition; de.laser.OrgRole; de.laser.License;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.interfaces.CalculatedType" %>
<laser:htmlStart message="subscription.details.label" serviceInjection="true"/>

%{-- help sidebar --}%
<laser:render template="/templates/help/help_subscription_show"/>
<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>sub.type: ${subscription.type}</p>

        <p>sub.instanceOf: <g:if test="${subscription.instanceOf}"><g:link action="show"
                                                                           id="${subscription.instanceOf.id}">${subscription.instanceOf.name}</g:link>
            ${subscription.instanceOf.getAllocationTerm()}
        </g:if></p>

        <p>sub.administrative: ${subscription.administrative}</p>

        <p>getCalculatedType(): ${subscription._getCalculatedType()}</p>
    </div>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
%{--<laser:render template="/templates/debug/orgRoles"  model="[debug: subscription.orgRelations]" />--}%
%{--<laser:render template="/templates/debug/prsRoles"  model="[debug: subscription.prsLinks]" />--}%
</ui:debugInfo>
<laser:render template="breadcrumb" model="${[params: params]}"/>
<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>
<ui:h1HeaderWithIcon>
<laser:render template="iconSubscriptionIsChild"/>
<ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>
<g:if test="${editable}">
    <ui:auditButton class="la-auditButton-header" auditable="[subscription, 'name']" auditConfigs="${auditConfigs}" withoutOptions="true"/>
</g:if>
<ui:anualRings object="${subscription}" controller="subscription" action="show" navNext="${navNextSubscription}"
                  navPrev="${navPrevSubscription}"/>

<laser:render template="nav"/>

<ui:objectStatus object="${subscription}" status="${subscription.status}"/>
<laser:render template="message"/>
<laser:render template="/templates/meta/identifier" model="${[object: subscription, editable: editable]}"/>


<ui:messages data="${flash}"/>

<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="ten wide column">
        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="startDate" type="date"
                                                 validation="datesCheck"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'startDate']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="endDate" type="date"
                                                 validation="datesCheck" overwriteEditable="${editable && !subscription.isAutomaticRenewAnnually}"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'endDate']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.manualCancellationDate.label')}</dt>
                            <dd><ui:xEditable owner="${subscription}" field="manualCancellationDate"
                                                 type="date"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'manualCancellationDate']"
                                        auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL &&
                                subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) ||
                                (subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL &&
                                        subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isMultiYear.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="isMultiYear"/></dd>
                            </dl>
                        </g:if>

                        <g:if test="${(subscription.type == RDStore.SUBSCRIPTION_TYPE_LOCAL &&
                                subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL)}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isAutomaticRenewAnnually.label')}</dt>
                                <dd><ui:xEditableBoolean owner="${subscription}" field="isAutomaticRenewAnnually"
                                                            overwriteEditable="${editable && subscription.isAllowToAutomaticRenewAnnually()}"/></dd>
                            </dl>
                        </g:if>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'default.status.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="status"
                                                        config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                        constraint="removeValue_deleted"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'status']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <sec:ifAnyGranted roles="ROLE_YODA">
                            <dl>
                                <dt class="control-label">alter Lizenztyp</dt>
                                <dd>
                                    <ui:xEditableRefData owner="${subscription}" field="type"
                                                            config="${RDConstants.SUBSCRIPTION_TYPE}"
                                                            constraint="removeValue_administrativeSubscription,removeValue_localSubscription"/>
                                </dd>
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'type']" auditConfigs="${auditConfigs}"/></dd>
                            </dl>
                        </sec:ifAnyGranted>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="kind"
                                                        config="${RDConstants.SUBSCRIPTION_KIND}"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'kind']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="form"
                                                        config="${RDConstants.SUBSCRIPTION_FORM}"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'form']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                            <dd><ui:xEditableRefData owner="${subscription}" field="resource"
                                                        config="${RDConstants.SUBSCRIPTION_RESOURCE}"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'resource']" auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>
                        <g:if test="${!params.orgBasicMemberView && subscription.instanceOf && contextOrg.id == subscription.getConsortia().id}">
                            <dl>
                                <dt class="control-label">${message(code: 'subscription.isInstanceOfSub.label')}</dt>
                                <dd>
                                    <g:link controller="subscription" action="show"
                                            id="${subscription.instanceOf.id}">${subscription.instanceOf}</g:link>
                                </dd>
                            </dl>

                            <sec:ifAnyGranted roles="ROLE_ADMIN">
                                <dl>
                                    <dt class="control-label">
                                        ${message(code: 'license.details.linktoLicense.pendingChange')}
                                    </dt>
                                    <dd>
                                        <ui:xEditableBoolean owner="${subscription}" field="isSlaved"/>
                                    </dd>
                                </dl>
                            </sec:ifAnyGranted>
                        </g:if>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.isPublicForApi.label')}</dt>
                            <dd><ui:xEditableBoolean owner="${subscription}" field="isPublicForApi"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'isPublicForApi']"
                                        auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                            <%--<dd><ui:xEditableRefData owner="${subscription}" field="hasPerpetualAccess" config="${RDConstants.Y_N}" /></dd>--%>
                            <dd><ui:xEditableBoolean owner="${subscription}" field="hasPerpetualAccess"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'hasPerpetualAccess']"
                                        auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'subscription.hasPublishComponent.label')}</dt>
                            <dd><ui:xEditableBoolean owner="${subscription}" field="hasPublishComponent"/></dd>
                            <g:if test="${editable}">
                                <dd class="la-js-editmode-container"><ui:auditButton
                                        auditable="[subscription, 'hasPublishComponent']"
                                        auditConfigs="${auditConfigs}"/></dd>
                            </g:if>
                        </dl>

                    </div>
                </div>
            </div>


            <g:if test="${subscription.packages}">
                <div id="packages" class="la-padding-top-1em"></div>
            </g:if>
        <%--
        <div class="ui card la-js-hideable hidden">
            <div class="content">
                <laser:render template="/templates/links/orgLinksAsList"
                          model="${[roleLinks: visibleOrgRelations,
                                    roleObject: subscription,
                                    roleRespValue: 'Specific subscription editor',
                                    editmode: editable,
                                    showPersons: true
                          ]}" />
                <div class="ui la-vertical buttons la-js-hide-this-card">

                    <laser:render template="/templates/links/orgLinksSimpleModal"
                              model="${[linkType: subscription.class.name,
                                        parent: genericOIDService.getOID(subscription),
                                        property: 'orgs',
                                        recip_prop: 'sub',
                                        tmplRole: RDStore.OR_PROVIDER,
                                        tmplType: RDStore.OT_PROVIDER,
                                        tmplEntity:message(code:'subscription.details.linkProvider.tmplEntity'),
                                        tmplText:message(code:'subscription.details.linkProvider.tmplText'),
                                        tmplButtonText:message(code:'subscription.details.linkProvider.tmplButtonText'),
                                        tmplModalID:'modal_add_provider',
                                        editmode: editable
                              ]}" />
                    <laser:render template="/templates/links/orgLinksSimpleModal"
                              model="${[linkType: subscription.class.name,
                                        parent: genericOIDService.getOID(subscription),
                                        property: 'orgs',
                                        recip_prop: 'sub',
                                        tmplRole: RDStore.OR_AGENCY,
                                        tmplType: RDStore.OT_AGENCY,
                                        tmplEntity: message(code:'subscription.details.linkAgency.tmplEntity'),
                                        tmplText: message(code:'subscription.details.linkAgency.tmplText'),
                                        tmplButtonText: message(code:'subscription.details.linkAgency.tmplButtonText'),
                                        tmplModalID:'modal_add_agency',
                                        editmode: editable
                              ]}" />

                </div><!-- la-js-hide-this-card -->

            </div>
        </div>
        --%>
            <div class="ui card" id="licenses"></div>
            <g:if test="${usage}">
                <div class="ui card la-dl-no-table">
                    <div class="content">
                        <g:if test="${totalCostPerUse}">
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
                        <g:if test="${lusage}">
                            <dl>
                                <dt class="control-label">${message(code: 'default.usage.licenseGrid.header')}</dt>
                                <dd>
                                    <table class="ui compact celled la-table-inCard table">
                                        <thead>
                                        <tr>
                                            <th>${message(code: 'default.usage.reportType')}</th>
                                            <g:each in="${l_x_axis_labels}" var="l">
                                                <th>${l}</th>
                                            </g:each>
                                            <th></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:set var="counter" value="${0}"/>
                                        <g:each in="${lusage}" var="v">
                                            <tr>
                                                <g:set var="reportMetric" value="${l_y_axis_labels[counter++]}"/>
                                                <td>${reportMetric}
                                                </td>
                                                <g:each in="${v}" var="v2">
                                                    <td>${v2}</td>
                                                </g:each>
                                                <td>
                                                    <g:set var="missingSubMonths"
                                                           value="${missingSubscriptionMonths[reportMetric.split(':')[0]]}"/>
                                                    <g:if test="${missingSubMonths}">
                                                        <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                              data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missingSubMonths.join(',')}">
                                                            <i class="exclamation triangle icon la-popup small"></i>
                                                        </span>
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                                </dd>
                            </dl>

                            <div class="ui divider"></div>
                        </g:if>
                        <dl>
                            <dt class="control-label la-js-dont-hide-this-card">${message(code: 'default.usage.label')}</dt>
                            <dd>
                                <table class="ui compact celled la-table-inCard la-ignore-fixed table">
                                    <thead>
                                    <tr>
                                        <th>${message(code: 'default.usage.reportType')}
                                        </th>
                                        <g:each in="${x_axis_labels}" var="l">
                                            <th>${l}</th>
                                        </g:each>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:set var="counter" value="${0}"/>
                                    <g:each in="${usage}" var="v">
                                        <tr>
                                            <g:set var="reportMetric" value="${y_axis_labels[counter++]}"/>
                                            <td>${reportMetric}
                                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                      data-html="${message(code: 'default.usage.reportUpToInfo')}: ${lastUsagePeriodForReportType[reportMetric.split(':')[0]]}">
                                                    <i class="info icon small circular la-popup"></i>
                                                </span>
                                            </td>
                                            <g:each in="${v}" status="i" var="v2">
                                                <td>
                                                    <laser:statsLink
                                                            base="${ConfigMapper.getStatsApiUrl()}"
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
                                            <g:set var="missing" value="${missingMonths[reportMetric.split(':')[0]]}"/>
                                            <td>
                                                <g:if test="${missing}">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                          data-html="${message(code: 'default.usage.missingUsageInfo')}: ${missing.join(',')}">
                                                        <i class="exclamation triangle icon la-popup small"></i>
                                                    </span>
                                                </g:if>
                                            </td>
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
                <laser:render template="properties" model="${[subscription: subscription]}"/>
            </div><!-- #new-dynamic-properties-block -->

            <div class="clear-fix"></div>
        </div>
    </div><!-- .twelve -->
    <aside class="six wide column la-sidekick">
        <div class="ui one cards">
            <div id="container-provider">
                <div class="ui card ">
                    <div class="content">
                        <h2 class="ui header">${message(code: 'subscription.organisations.label')}</h2>
                        <laser:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks    : visibleOrgRelations,
                                            roleObject   : subscription,
                                            roleRespValue: 'Specific subscription editor',
                                            editmode     : editable,
                                            showPersons  : true
                                  ]}"/>
                        <div class="ui la-vertical buttons la-js-hide-this-card">

                            <laser:render template="/templates/links/orgLinksSimpleModal"
                                      model="${[linkType      : subscription.class.name,
                                                parent        : genericOIDService.getOID(subscription),
                                                property      : 'orgs',
                                                recip_prop    : 'sub',
                                                tmplRole      : RDStore.OR_PROVIDER,
                                                tmplType      : RDStore.OT_PROVIDER,
                                                tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                tmplButtonText: message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                tmplModalID   : 'modal_add_provider',
                                                editmode      : editable
                                      ]}"/>
                            <laser:render template="/templates/links/orgLinksSimpleModal"
                                      model="${[linkType      : subscription.class.name,
                                                parent        : genericOIDService.getOID(subscription),
                                                property      : 'orgs',
                                                recip_prop    : 'sub',
                                                tmplRole      : RDStore.OR_AGENCY,
                                                tmplType      : RDStore.OT_AGENCY,
                                                tmplEntity    : message(code: 'subscription.details.linkAgency.tmplEntity'),
                                                tmplText      : message(code: 'subscription.details.linkAgency.tmplText'),
                                                tmplButtonText: message(code: 'subscription.details.linkAgency.tmplButtonText'),
                                                tmplModalID   : 'modal_add_agency',
                                                editmode      : editable
                                      ]}"/>

                        </div><!-- la-js-hide-this-card -->

                    </div>
                </div>
            </div>

            <div id="container-billing">
                <g:if test="${costItemSums.ownCosts || costItemSums.consCosts || costItemSums.subscrCosts}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <g:if test="${costItemSums.ownCosts}">
                                <g:if test="${(contextOrg.id != subscription.getConsortia()?.id && subscription.instanceOf) || !subscription.instanceOf}">
                                    <h2 class="ui header">${message(code: 'financials.label')}: ${message(code: 'financials.tab.ownCosts')}</h2>
                                    <laser:render template="financials" model="[data: costItemSums.ownCosts]"/>
                                </g:if>
                            </g:if>
                            <g:if test="${costItemSums.consCosts}">
                                <h2 class="ui header">${message(code: 'financials.label')}: ${message(code: 'financials.tab.consCosts')}</h2>
                                <laser:render template="financials" model="[data: costItemSums.consCosts]"/>
                            </g:if>
                            <g:elseif test="${costItemSums.subscrCosts}">
                                <h2 class="ui header">${message(code: 'financials.label')}: ${message(code: 'financials.tab.subscrCosts')}</h2>
                                <laser:render template="financials" model="[data: costItemSums.subscrCosts]"/>
                            </g:elseif>
                        </div>
                    </div>
                </g:if>
            </div>
            <div id="container-links">
                <div class="ui card"  id="links"></div>
            </div>
            <laser:render template="/templates/aside1" model="${[ownobj: subscription, owntp: 'subscription']}"/>
        </div>
    </aside><!-- .four -->
</div><!-- .grid -->

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.unlinkPackage = function(pkg_id) {
      var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage', params: [subscription: subscription.id])}&package="+pkg_id

            $.ajax({url: req_url,
              success: function(result){
                 //$("#unlinkPackageModal").clear();
                 //$('#magicArea').html(result);
              },
              complete: function(){
                $("#unlinkPackageModal").show();
              }
            });
          }
          JSPC.app.loadLinks = function () {
              $.ajax({
                  url: "<g:createLink controller="ajaxHtml" action="getLinks"/>",
                  data: {
                      entry:"${genericOIDService.getOID(subscription)}"
                  }
              }).done(function(response){
                  $("#links").html(response);
                  r2d2.initDynamicSemuiStuff('#links');
              })
          }
          JSPC.app.loadLicenses = function () {
              $.ajax({
                  url: "<g:createLink controller="ajaxHtml" action="getLinks"/>",
                  data: {
                      entry:"${genericOIDService.getOID(subscription)}",
                      subscriptionLicenseLink: true
                  }
              }).done(function(response){
                  $("#licenses").html(response);
                  r2d2.initDynamicSemuiStuff("#licenses");
              })
          }
          JSPC.app.loadPackages = function () {
              $.ajax({
                  url: "<g:createLink controller="ajaxHtml" action="getPackageData"/>",
                  data: {
                      subscription: "${subscription.id}"
                  }
              }).done(function(response){
                  $("#packages").html(response);
                  r2d2.initDynamicSemuiStuff("#packages");
              })
          }

          JSPC.app.loadLinks();
          JSPC.app.loadLicenses();
          JSPC.app.loadPackages();
</laser:script>

<laser:htmlEnd />
