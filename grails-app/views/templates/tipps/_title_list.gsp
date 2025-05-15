<%@page import="de.laser.wekb.TitleInstancePackagePlatform; de.laser.wekb.Package; de.laser.wekb.Platform; de.laser.wekb.Provider; de.laser.wekb.Vendor; de.laser.IssueEntitlement; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.survey.SurveyPackageResult; de.laser.finance.CostItem; de.laser.storage.RDStore; de.laser.convenience.Marker; de.laser.utils.DateUtils; de.laser.storage.RDConstants; de.laser.Org; de.laser.RefdataValue" %>
<laser:serviceInjection/>
<table class="ui sortable celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
                <g:if test="${tmplConfigItem == 'lineNumber'}">
                    <th class="la-th-wrap">${message(code: 'sidewide.number')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'name'}">
                    <g:sortableColumn property="sortname" title="${message(code: 'default.name.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'status'}">
                    <th>${message(code: 'tipp.status.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'package'}">
                    <g:sortableColumn class="la-th-wrap" property="tipp.pkg.name" title="${message(code: 'package.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'provider'}">
                    <g:sortableColumn class="la-th-wrap" property="tipp.pkg.provider.name" title="${message(code: 'provider.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'platform'}">
                    <g:sortableColumn class="la-th-wrap" property="tipp.pkg.nominalPlatform.name" title="${message(code: 'platform.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'lastUpdatedDisplay'}">
                    <g:sortableColumn class="la-th-wrap" property="lastUpdated" title="${message(code: 'tipp.lastUpdated')}" params="${params}" defaultOrder="desc"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'linkTitle'}">
                    <th class="la-th-wrap x center aligned"></th>
                </g:if>
            </g:each>
        </tr>
    </thead>
    <tbody>
        <g:each in="${tipps}" var="tipp" status="jj">
            <tr>
                <g:each in="${tmplConfigShow}" var="tmplConfigItem">
                    <g:if test="${tmplConfigItem == 'lineNumber'}">
                        <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'name'}">
                        <td>
                            <g:link controller="tipp" action="show" id="${tipp.id}">${tipp.name}</g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'status'}">
                        <td>
                            ${tipp.status.getI10n("value")}
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'package'}">
                        <td>
                            <ui:wekbIconLink type="package" gokbId="${tipp.pkg.gokbId}" />
                            <g:link controller="package" action="show" id="${tipp.pkg.id}">${tipp.pkg.name}</g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'provider'}">
                        <td>
                            <ui:wekbIconLink type="provider" gokbId="${tipp.pkg.provider.gokbId}" />
                            <g:link controller="provider" action="show" id="${tipp.pkg.provider.id}">${tipp.pkg.provider.name}</g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'platform'}">
                        <td>
                            <ui:wekbIconLink type="platform" gokbId="${tipp.pkg.nominalPlatform.gokbId}" />
                            <g:link controller="platform" action="show" id="${tipp.pkg.nominalPlatform.id}">${tipp.pkg.nominalPlatform.name}</g:link>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'lastUpdatedDisplay'}">
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${tipp.lastUpdated}"/>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'linkTitle'}">
                        <td class="right aligned">
                            <g:if test="${editable && (!fixedSubscription || (fixedSubscription && IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.tipp.id = :tippID and ie.subscription = :fixedSubscription and ie.status != :removed', [tippID: tipp.id, fixedSubscription: fixedSubscription, removed: RDStore.TIPP_STATUS_REMOVED])[0] == 0))}">
                                <g:if test="${fixedSubscription}">
                                    <div class="two wide column">
                                        <a id="linkTitleToSubscription_${tipp.gokbId}" href="${createLink(action: 'linkTitleModal', controller: 'ajaxHtml', params: [tippID: tipp.id, fixedSubscription: fixedSubscription.id, headerToken: "subscription.details.linkTitle.heading.subscription"])}" class="ui icon button"><g:message code="subscription.details.linkTitle.label.subscription"/></a>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="two wide column">
                                        <a id="linkTitleToSubscription_${tipp.gokbId}" href="${createLink(action: 'linkTitleModal', controller: 'ajaxHtml', params: [tippID: tipp.id, headerToken: "subscription.details.linkTitle.heading.title"])}" class="ui icon button"><g:message code="subscription.details.linkTitle.label.title"/></a>
                                    </div>
                                </g:else>

                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('#linkTitleToSubscription_${tipp.gokbId}').on('click', function(e) {
                                        e.preventDefault();

                                        $.ajax({
                                            url: $(this).attr('href')
                                        }).done( function (data) {
                                            $('.ui.dimmer.modals > #linkTitleModal').remove();
                                            $('#dynamicModalContainer').empty().html(data);

                                            $('#dynamicModalContainer .ui.modal').modal({
                                               onShow: function () {
                                                    r2d2.initDynamicUiStuff('#linkTitleModal');
                                                    r2d2.initDynamicXEditableStuff('#linkTitleModal');
                                                    $("html").css("cursor", "auto");
                                                },
                                                detachable: true,
                                                autofocus: false,
                                                closable: false,
                                                transition: 'scale',
                                                onApprove : function() {
                                                    $(this).find('.ui.form').submit();
                                                    return false;
                                                }
                                            }).modal('show');
                                        })
                                    });
                                </laser:script>
                            </g:if>
                        </td>
                    </g:if>
                </g:each>
            </tr>
        </g:each>
    </tbody>
</table>