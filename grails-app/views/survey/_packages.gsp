<%@ page import="de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection/>

<div class="ui card">
    <div class="content">
        <g:each in="${packages}" var="pkgInfo">

            <div class="ui accordion la-accordion-showMore js-package-info-accordion">
                <div class="item">
                    <div class="title">
                        <button
                                class="ui button icon blue la-modern-button la-popup-tooltip la-delay right floated " data-content="<g:message code="surveyConfigsInfo.packageInfo.show"/>">
                            <i class="ui angle double down large icon"></i>
                        </button>
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('.js-package-info-accordion')
                              .accordion({
                                onOpen: function() {
                                  $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.packageInfo.hide"/> ')
                                    },
                                    onClose: function() {
                                      $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.packageInfo.show"/> ')
                                    }
                                  })
                                ;
                        </laser:script>

                        <i aria-hidden="true" class="circular la-package icon"></i>

                        <h2 class="ui icon header la-clear-before la-noMargin-top">
                            <g:link controller="package" action="show"
                                    id="${pkgInfo.packageInstance.id}">${pkgInfo.packageInstance.name}</g:link>
                        </h2>
                    </div>
                    <div class="content">
                        <div class="ui grid">
                            <div class="sixteen wide column">
                                <div class="la-inline-lists">
                                    <div class="ui two cards">
                                        <div class="ui card la-time-card">
                                            <div class="content">
                                                <dl>
                                                    <dt>${message(code: 'default.status.label')}</dt>
                                                    <dd>${pkgInfo.packageInstance.packageStatus?.getI10n('value')}</dd>
                                                </dl>
                                                <g:if test="${pkgInfo.packageInstanceRecord}">
                                                    <dl>
                                                        <dt>${message(code: 'package.show.altname')}</dt>
                                                        <dd>
                                                            <div class="ui bulleted list">
                                                                <g:each in="${pkgInfo.packageInstanceRecord.altname}"
                                                                        var="altname">
                                                                    <div class="item">${altname}</div>
                                                                </g:each>
                                                            </div>
                                                        </dd>
                                                    </dl>
                                                    <dl>
                                                        <dt>${message(code: 'package.curatoryGroup.label')}</dt>
                                                        <dd>
                                                            <div class="ui bulleted list">
                                                                <g:each in="${pkgInfo.packageInstanceRecord.curatoryGroups}"
                                                                        var="curatoryGroup">
                                                                    <div class="item"><g:link
                                                                            url="${editUrl}resource/show/${curatoryGroup.curatoryGroup}">${curatoryGroup.name} ${curatoryGroup.type ? "(${curatoryGroup.type})" : ""}</g:link></div>
                                                                </g:each>
                                                            </div>
                                                        </dd>
                                                    </dl>
                                                    <dl>
                                                        <dt>${message(code: 'package.lastUpdated.label')}</dt>
                                                        <dd>
                                                            <g:if test="${pkgInfo.packageInstanceRecord.lastUpdatedDisplay}">
                                                                <g:formatDate formatName="default.date.format.notime"
                                                                              date="${DateUtils.parseDateGeneric(pkgInfo.packageInstanceRecord.lastUpdatedDisplay)}"/>
                                                            </g:if>
                                                        </dd>
                                                    </dl>
                                                    <dl>
                                                        <dt>${message(code: 'package.source.label')}</dt>
                                                        <dd>
                                                            <g:if test="${pkgInfo.packageInstanceRecord.source?.automaticUpdates}">
                                                                <g:message code="package.index.result.automaticUpdates"/>
                                                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                                      data-position="right center"
                                                                      data-content="${pkgInfo.packageInstanceRecord.source.frequency}">
                                                                    <i class="question circle icon"></i>
                                                                </span>
                                                            </g:if>
                                                            <g:else>
                                                                <g:message code="package.index.result.noAutomaticUpdates"/>
                                                            </g:else>
                                                        </dd>
                                                    </dl>
                                                </g:if>
                                                <dl>
                                                    <dt>${message(code: 'package.file')}</dt>
                                                    <dd>${pkgInfo.packageInstance.file?.getI10n("value")}</dd>
                                                </dl>
                                            </div>
                                        </div>

                                        <div class="ui card">
                                            <div class="content">
                                                <dl>
                                                    <dt>${message(code: 'package.contentType.label')}</dt>
                                                    <dd>${pkgInfo.packageInstance.contentType?.getI10n("value")}</dd>
                                                </dl>
                                                <g:if test="${pkgInfo.packageInstanceRecord}">
                                                    <dl>
                                                        <dt>${message(code: 'package.breakable')}</dt>
                                                        <dd>${pkgInfo.packageInstanceRecord.breakable ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.breakable, RDConstants.PACKAGE_BREAKABLE).getI10n("value") : message(code: 'default.not.available')}</dd>
                                                    </dl>
                                                <%--<dl>
                                                    <dt>${message(code: 'package.consistent')}</dt>
                                                    <dd>${pkgInfo.packageInstanceRecord.consistent ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.consistent, RDConstants.PACKAGE_CONSISTENT).getI10n("value") : message(code: 'default.not.available')}</dd>
                                                </dl>--%>
                                                    <dl>
                                                        <dt>${message(code: 'package.scope.label')}</dt>
                                                        <dd>
                                                            ${pkgInfo.packageInstanceRecord.scope ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.scope, RDConstants.PACKAGE_SCOPE).getI10n("value") : message(code: 'default.not.available')}
                                                            <g:if test="${pkgInfo.packageInstanceRecord.scope == RDStore.PACKAGE_SCOPE_NATIONAL.value}">
                                                                <dl>
                                                                    <dt>${message(code: 'package.nationalRange.label')}</dt>
                                                                    <g:if test="${pkgInfo.packageInstanceRecord.nationalRanges}">
                                                                        <dd>
                                                                            <div class="ui bulleted list">
                                                                                <g:each in="${pkgInfo.packageInstanceRecord.nationalRanges}"
                                                                                        var="nr">
                                                                                    <div class="item">${RefdataValue.getByValueAndCategory(nr.value, RDConstants.COUNTRY) ? RefdataValue.getByValueAndCategory(nr.value, RDConstants.COUNTRY).getI10n('value') : nr}</div>
                                                                                </g:each>
                                                                            </div>
                                                                        </dd>
                                                                    </g:if>
                                                                </dl>
                                                                <dl>
                                                                    <dt>${message(code: 'package.regionalRange.label')}</dt>
                                                                    <g:if test="${pkgInfo.packageInstanceRecord.regionalRanges}">
                                                                        <dd>
                                                                            <div class="ui bulleted list">
                                                                                <g:each in="${pkgInfo.packageInstanceRecord.regionalRanges}"
                                                                                        var="rr">
                                                                                    <div class="item">${RefdataValue.getByValueAndCategory(rr.value, RDConstants.REGIONS_DE) ? RefdataValue.getByValueAndCategory(rr.value, RDConstants.REGIONS_DE).getI10n('value') : rr}</div>
                                                                                </g:each>
                                                                            </div>
                                                                        </dd>
                                                                    </g:if>
                                                                </dl>
                                                            </g:if>
                                                        </dd>
                                                    </dl>
                                                    <dl>
                                                        <dt>${message(code: 'package.paymentType.label')}</dt>
                                                        <dd>${RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.paymentType, RDConstants.PAYMENT_TYPE) ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.paymentType, RDConstants.PAYMENT_TYPE).getI10n("value") : pkgInfo.packageInstanceRecord.paymentType}</dd>
                                                    </dl>
                                                    <dl>
                                                        <dt>${message(code: 'package.openAccess.label')}</dt>
                                                        <dd>${pkgInfo.packageInstanceRecord.openAccess ? RefdataValue.getByValueAndCategory(pkgInfo.packageInstanceRecord.openAccess, RDConstants.LICENSE_OA_TYPE)?.getI10n("value") : RefdataValue.getByValueAndCategory('Empty', RDConstants.LICENSE_OA_TYPE).getI10n("value")}</dd>
                                                    </dl>
                                                    <dl>
                                                        <dt>${message(code: 'package.ddc.label')}</dt>
                                                        <dd>
                                                            <div class="ui bulleted list">
                                                                <g:each in="${pkgInfo.packageInstanceRecord.ddcs}" var="ddc">
                                                                    <div class="item">${RefdataValue.getByValueAndCategory(ddc.value, RDConstants.DDC) ? RefdataValue.getByValueAndCategory(ddc.value, RDConstants.DDC).getI10n('value') : message(code: 'package.ddc.invalid')}</div>
                                                                </g:each>
                                                            </div>
                                                        </dd>
                                                    </dl>
                                                </g:if>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="ui card">
                                        <div class="content">
                                            <dl>
                                                <dt>${message(code: 'platform.label')}</dt>
                                                <dd>
                                                    <g:if test="${pkgInfo.packageInstance.nominalPlatform}">
                                                        <g:link controller="platform" action="show"
                                                                id="${pkgInfo.packageInstance.nominalPlatform.id}">${pkgInfo.packageInstance.nominalPlatform.name}</g:link>

                                                        <g:if test="${pkgInfo.packageInstance.nominalPlatform.primaryUrl}">
                                                            <ui:linkWithIcon
                                                                    href="${pkgInfo.packageInstance.nominalPlatform.primaryUrl?.startsWith('http') ? pkgInfo.packageInstance.nominalPlatform.primaryUrl : 'http://' + pkgInfo.packageInstance.nominalPlatform.primaryUrl}"/>
                                                        </g:if>
                                                    </g:if>
                                                </dd>
                                            </dl>

                                        </div>
                                    </div>
                                </div>
                            </div><!-- .twelve -->
                        </div><!-- .grid -->
                    </div>
                </div>
            </div>
        </g:each>
    </div><!-- .content -->
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.js-package-info-accordion')
      .accordion({
        onOpen: function() {
          $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.packageInfo.hide"/> ')
                                    },
                                    onClose: function() {
                                      $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.packageInfo.show"/> ')
                                    }
                                  })
                                ;
</laser:script>