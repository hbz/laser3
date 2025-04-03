<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.DateUtils; java.text.SimpleDateFormat; java.text.DateFormat; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.SubscriptionPackage; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.Subscription; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.wekb.Platform; de.laser.SubscriptionPackage; de.laser.Org" %>
<laser:serviceInjection/>
<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant)}"/>

<g:if test="${customProperties}">
    <div class="ui card">
        <div class="content">
            <div class="ui accordion la-accordion-showMore js-propertiesCompareInfo-accordion">
                <div class="item">
                    <div class="title">
                        <div class="${Btn.MODERN.SIMPLE_TOOLTIP} right floated"
                                data-content="<g:message code="survey.subscription.propertiesChange.show"/>">
                            <i class="${Icon.CMD.SHOW_MORE}"></i>
                        </div>
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('.js-propertiesCompareInfo-accordion')
                              .accordion({
                                onOpen: function() {
                                  $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                code="survey.subscription.propertiesChange.hide"/> ')
                                                    },
                                                    onClose: function() {
                                                      $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                code="survey.subscription.propertiesChange.show"/> ')
                                                    }
                                                  })
                                                ;
                        </laser:script>
                        <div class="content">
                            <h2 class="ui header">
                                ${message(code: 'survey.subscription.propertiesChange')}
                            </h2>
                        </div>
                    </div>

                    <div class="content" id="propertiesCompareInfo">
                        <div class="ui stackable grid container">
                            <laser:render template="/templates/survey/propertiesCompareInfo"
                                          model="[customProperties: customProperties]"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</g:if>

<div class="ui card">
    <div class="content">
        <g:if test="${!subscription}">
            <div>%{-- needed for css --}%
            <ui:headerTitleIcon type="Subscription"/>
            <g:link class="${Btn.SIMPLE} right floated" controller="public" action="gasco"
                    params="${[q: surveyConfig.subscription.name, consortia: "${surveyInfo.owner.class.name}:${surveyInfo.owner.id}"]}">
                GASCO-Monitor
            </g:link>
                <h2 class="ui icon header">
                    <g:link controller="public" action="gasco"
                            params="${[q: surveyConfig.subscription.name, consortia: "${surveyInfo.owner.class.name}:${surveyInfo.owner.id}"]}">
                        ${surveyConfig.subscription.name}
                    </g:link>
                </h2>
            </div>
        </g:if>
        <g:else>
            <div class="ui accordion la-accordion-showMore js-subscription-info-accordion">
                <div class="item">
                    <div class="title">
                        <div class="${Btn.MODERN.SIMPLE} right floated">
                            <i class="${Icon.CMD.SHOW_MORE}"></i>
                        </div>
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('.js-subscription-info-accordion')
                              .accordion({
                                onOpen: function() {
                                  $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                code="surveyConfigsInfo.subscriptionInfo.hide"/> ')
                                        },
                                        onClose: function() {
                                          $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                code="surveyConfigsInfo.subscriptionInfo.show"/> ')
                                        }
                                      })
                                    ;
                        </laser:script>

                        <g:if test="${subscription}">
                            <ui:headerTitleIcon type="Subscription"/>
                            <h2 class="ui icon header">
                                <g:link controller="subscription" action="show" id="${subscription.id}">
                                    <g:message code="surveyConfigsInfo.subscriptionInfo.show"/>
                                </g:link>
                            </h2>
                            <ui:auditInfo auditable="[subscription, 'name']"/>
                        </g:if>
                    </div>

                    <div class="content" id="subscription-info">
                        <div class="ui stackable grid container">
                            <div class="ten wide column">
                                <dl>
                                    <dt class="control-label">${message(code: 'default.status.label')}</dt>
                                    <dd>${subscription.status.getI10n('value')}</dd>
                                    <dd><ui:auditInfo auditable="[subscription, 'status']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                                    <dd>${subscription.kind?.getI10n('value')}</dd>
                                    <dd><ui:auditInfo auditable="[subscription, 'kind']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                                    <dd>${subscription.form?.getI10n('value')}</dd>
                                    <dd><ui:auditInfo auditable="[subscription, 'form']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                                    <dd>${subscription.resource?.getI10n('value')}</dd>
                                    <dd><ui:auditInfo auditable="[subscription, 'resource']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                    <dd>${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</dd>
                                    <dd><ui:auditInfo auditable="[subscription, 'hasPerpetualAccess']"/></dd>
                                </dl>
                                <dl>
                                    <dt class="control-label">
                                        <g:message code="default.identifiers.label"/>
                                    </dt>
                                    <dd>

                                        <div class="ui list">
                                            <g:each in="${subscription.ids?.sort { it.ns.ns }}"
                                                    var="id">
                                                <div class="item js-copyTriggerParent">
                                                    <span class="ui small basic image label js-copyTrigger la-popup-tooltip"
                                                          data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
                                                        <i class="la-copy grey icon la-js-copyTriggerIcon" aria-hidden="true"></i>
                                                        ${id.ns.ns}: <div class="detail js-copyTopic">${id.value}</div>
                                                    </span>
                                                </div>
                                            </g:each>
                                        </div>
                                    </dd>
                                </dl>
                            </div>


                            <div class="six wide column">
                                %{--<g:if test="${subscription.packages}">
                                    <table class="ui three column la-selectable table">
                                        <g:each in="${subscription.packages.sort { it.pkg.name }}" var="sp">
                                            <tr>
                                                <th scope="row"
                                                    class="control-label">${message(code: 'subscription.packages.label')}</th>
                                                <td>
                                                    <g:link controller="package" action="show"
                                                            id="${sp.pkg.id}">${sp.pkg.name}</g:link>

                                                    <g:if test="${sp.pkg.provider}">
                                                        (${sp.pkg.provider.name})
                                                    </g:if>
                                                </td>
                                                <td class="right aligned">
                                                </td>

                                            </tr>
                                        </g:each>
                                    </table>
                                </g:if>--}%

                                <g:if test="${visibleProviders}">

                                    <laser:render template="/templates/links/providerLinksAsList"
                                                  model="${[roleLinks    : visibleProviders,
                                                            roleObject   : subscription,
                                                            roleRespValue: RDStore.PRS_RESP_SPEC_SUB_EDITOR.value,
                                                            editmode     : false,
                                                            showPersons  : false
                                                  ]}"/>

                                </g:if>

                                <g:if test="${visibleVendors}">

                                    <laser:render template="/templates/links/vendorLinksAsList"
                                                  model="${[roleLinks    : visibleVendors,
                                                            roleObject   : subscription,
                                                            roleRespValue: RDStore.PRS_RESP_SPEC_SUB_EDITOR.value,
                                                            editmode     : false,
                                                            showPersons  : false
                                                  ]}"/>

                                </g:if>
                            </div>
                        </div>


                        <br/>

                        <div class="ui form">

                            <g:set var="oldEditable" value="${editable}"/>
                            <div id="subscription-properties" style="margin: 1em 0">
                                <g:set var="editable" value="${false}" scope="request"/>
                                <g:set var="editable" value="${false}" scope="page"/>
                                <laser:render template="/subscription/properties" model="${[
                                        subscription: subscription, calledFromSurvey: true
                                ]}"/>

                            </div>

                            <g:set var="editable" value="${oldEditable ?: false}" scope="page"/>
                            <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                        </div>

                        <div class="ui card">
                            <div class="content">
                                <h2 class="ui header">
                                    <g:message code="license.plural"/>
                                </h2>
                                <g:if test="${links && links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}">
                                    <table class="ui fixed table">
                                        <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}"
                                                var="link">
                                            <g:set var="pair" value="${link.getOther(subscription)}"/>
                                            <g:if test="${pair}">
                                                <tr>
                                                    <th scope="row"
                                                        class="control-label">${pair?.licenseCategory?.getI10n("value")}</th>
                                                    <td>
                                                        <g:link controller="license" action="show" id="${pair.id}">
                                                            ${pair.reference} (${pair.status.getI10n("value")})
                                                        </g:link>
                                                        <g:formatDate date="${pair.startDate}"
                                                                      format="${message(code: 'default.date.format.notime')}"/>-<g:formatDate
                                                            date="${pair.endDate}"
                                                            format="${message(code: 'default.date.format.notime')}"/><br/>
                                                        <g:set var="comment"
                                                               value="${DocContext.findByLink(link)}"/>
                                                        <g:if test="${comment}">
                                                            <em>${comment.owner.content}</em>
                                                        </g:if>
                                                    </td>
                                                    <td class="right aligned">
                                                        <g:if test="${pair.propertySet}">
                                                            <div id="derived-license-properties-toggle${link.id}"
                                                                 class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                                                 data-content="${message(code: 'subscription.details.viewLicenseProperties')}">
                                                                <i class="${Icon.CMD.SHOW_MORE}"></i>
                                                            </div>
                                                            <laser:script file="${this.getGroovyPageFileName()}">
                                                                $("#derived-license-properties-toggle${link.id}").on('click', function() {
                                                            $("#derived-license-properties${link.id}").transition('slide down');
                                                            //$("#derived-license-properties${link.id}").toggleClass('hidden');

                                                            if ($("#derived-license-properties${link.id}").hasClass('visible')) {
                                                                $(this).html('<i class="angle double down icon"></i>')
                                                            } else {
                                                                $(this).html('<i class="angle double up icon"></i>')
                                                            }
                                                        })
                                                            </laser:script>
                                                        </g:if>
                                                    </td>
                                                </tr>
                                                <g:if test="${pair.propertySet}">
                                                    <tr>
                                                        <td colspan="3"><div id="${link.id}Properties"></div></td>
                                                    </tr>
                                                </g:if>
                                            </g:if>
                                        </g:each>
                                    </table>
                                </g:if>

                            </div>%{-- .content --}%
                        </div>

                    </div>
                </div>
            </div>%{-- Accordion --}%
        </g:else>
    </div>
</div>

<g:if test="${subscription && subscription.packages}">
    <div id="packages" class="la-inline-lists"></div>
</g:if>

<g:if test="${subscription && subscription.packages}">
    <div id="ieInfos" class="la-inline-lists"></div>
</g:if>

%{--<g:if test="${subscription && subscription.packages}">
    <g:if test="${subscriptionService.areStatsAvailable(subscription)}">
        <div class="ui card">
            <div class="content">
                <div id="statsInfos" class="ui accordion la-accordion-showMore js-subscription-info-accordion">
                    <div class="item">
                        <div class="title">
                            <div class="${Btn.MODERN.SIMPLE} right floated">
                                <i class="${Icon.CMD.SHOW_MORE}"></i>
                            </div>

                            <i aria-hidden="true" class="${Icon.STATS} circular green inverted"></i>

                            <h2 class="ui icon header la-clear-before la-noMargin-top">
                                <g:link controller="subscription" action="stats" target="_blank"
                                        id="${subscription.id}"><g:message code="surveyConfigsInfo.stats.show"/></g:link>
                            </h2>
                        </div>

                        <div class="content">
                            <g:link controller="subscription" action="stats" target="_blank"
                                    id="${subscription.id}" class="${Btn.SIMPLE}">
                                <g:message code="renewEntitlementsWithSurvey.stats.button"/>
                            </g:link>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </g:if>
</g:if>--}%


<laser:script file="${this.getGroovyPageFileName()}">

    $('body #participation').editable('destroy').editable({
        tpl: '<select class="ui dropdown clearable"></select>'
        }).on('shown', function() {
            r2d2.initDynamicUiStuff('body');
            $(".table").trigger('reflow');
            $('.ui.dropdown').dropdown({ clearable: true });
        }).on('hidden', function() {
            $(".table").trigger('reflow')
    });
    <g:if test="${links}">
        <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}" var="link">
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="getLicensePropertiesForSubscription"/>",
                      data: {
                           loadFor: "${link.sourceLicense.id}",
                           linkId: ${link.id}
            }
        }).done(function(response) {
            $("#${link.id}Properties").html(response);
            }).fail();
        </g:each>
    </g:if>

    <g:if test="${subscription && subscription.packages}">
        JSPC.app.loadPackages = function () {
                  $.ajax({
                      url: "<g:createLink controller="ajaxHtml" action="getGeneralPackageData"/>",
                      data: {
                          subscription: "${subscription.id}"
                      }
                  }).done(function(response){
                      $("#packages").html(response);
                      r2d2.initDynamicUiStuff("#packages");
                  })
              }


        JSPC.app.loadPackages();
    </g:if>

    <g:if test="${subscription && subscription.packages}">
        JSPC.app.loadIEInfos = function () {
                  $.ajax({
                      url: "<g:createLink controller="ajaxHtml" action="getIeInfos"/>",
                      data: {
                          subscription: "${subscription.id}"
                      }
                  }).done(function(response){
                      $("#ieInfos").html(response);
                      r2d2.initDynamicUiStuff("#ieInfos");
                  })
              }


        JSPC.app.loadIEInfos();
    </g:if>

    $('textarea').each(function () {
        this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    });

</laser:script>
