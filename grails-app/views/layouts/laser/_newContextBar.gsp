<%@ page import="de.laser.HelpService; de.laser.ui.Btn; de.laser.ui.Icon; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.convenience.Marker; de.laser.Subscription; de.laser.GenericOIDService; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.UserSetting; de.laser.auth.User; de.laser.auth.Role; de.laser.Org" %>
<laser:serviceInjection />

<nav id="contextBar" class="ui fixed menu" aria-label="${message(code:'wcag.label.modeNavigation')}">

    <div class="ui container">
        <button class="ui button big la-menue-button la-modern-button" style="display:none"><i class="bars icon"></i></button>

        <div class="ui sub header item la-context-org" style="display: none">
            <ui:cbItemCustomerType org="${contextService.getOrg()}" />
            <ui:cbItemUserAffiliation user="${contextService.getUser()}" />
            <ui:cbItemUserSysRole user="${contextService.getUser()}" />

            <div id="la-js-cb-context-display" data-display="${contextService.getOrg()?.name}">
                ${contextService.getOrg()?.name}
            </div>
        </div>

        <div class="right menu la-advanced-view" style="display: none">

            <div id="la-cb-info-display"></div>

            %{-- my object indicator --}%

            <g:if test="${isMyPlatform}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.platform')}" icon="${Icon.SIG.MY_OBJECT}" color="yellow" />
            </g:if>
            <g:elseif test="${isMyPkg}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.pkg')}" icon="${Icon.SIG.MY_OBJECT}" color="yellow" />
            </g:elseif>
            <g:elseif test="${isMyOrg}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.org')}" icon="${Icon.SIG.MY_OBJECT}" color="yellow" />
            </g:elseif>
            <g:elseif test="${isMyProvider}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.provider')}" icon="${Icon.SIG.MY_OBJECT}" color="yellow" />
            </g:elseif>
            <g:elseif test="${isMyVendor}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.vendor')}" icon="${Icon.SIG.MY_OBJECT}" color="yellow" />
            </g:elseif>

            %{-- child indicator --}%

            <g:if test="${controllerName == 'subscription' && subscription && !surveyConfig}">
                <g:if test="${subscription.instanceOf && ((contextService.getOrg().id == subscription.getConsortium()?.id) || contextService.getUser().isYoda())}">
                    <ui:cbItemInfo display="Sie sehen eine Kindlizenz" icon="child" color="orange" />
                </g:if>
            </g:if>

            <g:if test="${controllerName == 'license' && license}">
                <g:if test="${license.instanceOf && ((contextService.getOrg().id == license.getLicensingConsortium()?.id) || contextService.getUser().isYoda())}">
                    <ui:cbItemInfo display="Sie sehen einen Einrichtungsvertrag" icon="child" color="green" />
                </g:if>
            </g:if>

            %{-- content indicator --}%

            <g:if test="${flagContentCache}">
                <ui:cbItemInfo display="${message(code: 'statusbar.flagContentCache.tooltip')}" icon="hourglass" color="blue" />
            </g:if>
            <g:if test="${flagContentGokb}">
                <ui:cbItemInfo display="${message(code: 'statusbar.flagContentGokb.tooltip')}" icon="cloud" color="blue" />
            </g:if>
            <g:if test="${flagContentElasticsearch}">
                <ui:cbItemInfo display="${message(code: 'statusbar.flagContentElasticsearch.tooltip')}" icon="cloud" color="blue" />
            </g:if>

            %{-- help panel --}%

            <g:set var="helpMatch" value="${helpService.getMatch()}" />

            <g:if test="${helpMatch}">
                <div class="item la-cb-action">
                    <button class="${Btn.ICON.SIMPLE} la-toggle-ui" id="help-toggle"><i class="${Icon.UI.HELP}"></i></button>
                </div>
            </g:if>

            %{-- subscription transfer --}%

            <g:set var="isSubscriptionViewValid" value="${!(actionName.startsWith('copy') || actionName in ['renewEntitlementsWithSurvey', 'renewSubscription', 'emptySubscription'])}" />

            <g:if test="${controllerName in ['finance', 'subscription', 'survey'] && subscription && isSubscriptionViewValid}">
                <g:if test="${editable && contextService.getOrg().isCustomerType_Consortium() && subscription._getCalculatedType() in [Subscription.TYPE_CONSORTIAL]}">
                    <div class="item la-cb-action">
                        <button class="${Btn.ICON.SIMPLE_TOOLTIP} la-toggle-ui" id="subscriptionTransfer-toggle"
                                data-content="${message(code:'statusbar.showSubscriptionTransfer.tooltip')}" data-position="bottom left">
                            <i class="${Icon.SUBSCRIPTION}"></i>
                        </button>
                    </div>
                </g:if>
            </g:if>

            %{-- subscription members --}%

            <g:if test="${controllerName in ['finance', 'subscription'] && subscription && isSubscriptionViewValid}">
                <g:if test="${editable && contextService.getOrg().isCustomerType_Consortium() && subscription.getConsortium()?.id == contextService.getOrg().id}">
                    <div class="item la-cb-action">
%{--                        <button class="${Btn.ICON.SIMPLE_TOOLTIP} la-toggle-ui" id="subscriptionMembers-toggle"--}%
%{--                                data-content="${message(code:'consortium.member.plural')} ${message(code:'default.and')} ${message(code:'subscription.member.plural')}" --}%
%{--                                data-position="bottom left">--}%
%{--                            <i class="${Icon.AUTH.ORG_INST}"></i>--}%
%{--                        </button>--}%
                        <button class="${Btn.ICON.SIMPLE} la-toggle-ui" id="subscriptionMembers-toggle">
                            <i class="${Icon.AUTH.ORG_INST}"></i>
                        </button>
                    </div>
                </g:if>
            </g:if>


            %{-- survey stuff  --}%

            <g:if test="${controllerName == 'survey' && (actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia')}">
                <div class="item la-cb-action">
                    <g:if test="${actionName == 'workflowsSurveysConsortia'}">
                        <g:link action="currentSurveysConsortia" controller="survey" class="${Btn.ICON.SIMPLE_TOOLTIP}"
                                data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom center">
                            <i class="la-tab icon"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link action="workflowsSurveysConsortia" controller="survey" class="${Btn.ICON.SIMPLE_TOOLTIP}"
                                data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom center">
                            <i class="la-tab slash icon"></i>
                        </g:link>
                    </g:else>
                </div>
            </g:if>

            %{-- marker --}%

            <g:if test="${controllerName == 'package'}">
                <g:if test="${packageInstance}">
                    <ui:cbItemMarkerAction package="${packageInstance}" type="${Marker.TYPE.WEKB_CHANGES}"/>
                </g:if>
            </g:if>
            <g:elseif test="${controllerName == 'platform'}">
                <g:if test="${platformInstance}">
                    <ui:cbItemMarkerAction platform="${platformInstance}" type="${Marker.TYPE.WEKB_CHANGES}"/>
                </g:if>
            </g:elseif>
            <g:elseif test="${controllerName == 'provider'}">
                <g:if test="${provider}">
                    <ui:cbItemMarkerAction provider="${provider}" type="${Marker.TYPE.WEKB_CHANGES}"/>
                </g:if>
            </g:elseif>
%{--            <g:elseif test="${controllerName in ['issueEntitlement', 'tipp'] && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">--}%
%{--                <g:if test="${tipp}">--}%
%{--                    <ui:cbItemMarkerAction tipp="${tipp}" type="${Marker.TYPE.TIPP_CHANGES}"/>--}%
%{--                </g:if>--}%
%{--                <g:elseif test="${issueEntitlementInstance?.tipp}">--}%
%{--                    <ui:cbItemMarkerAction tipp="${issueEntitlementInstance.tipp}" type="${Marker.TYPE.TIPP_CHANGES}"/>--}%
%{--                </g:elseif>--}%
%{--            </g:elseif>--}%
            <g:elseif test="${controllerName == 'vendor'}">
                <g:if test="${vendor}">
                    <ui:cbItemMarkerAction vendor="${vendor}" type="${Marker.TYPE.WEKB_CHANGES}"/>
                </g:if>
            </g:elseif>

            %{-- linkify --}%

            <g:if test="${controllerName in ['finance', 'subscription'] && subscription}">
                <g:set var="linkifyMap" value="${linksGenerationService.getSourcesAndDestinations(subscription, contextUser, RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE))}" />

                <%-- null check at getConsortium() because of delete after process; see ERMS-6519 --%>
                <g:if test="${linkifyMap || (subscription.instanceOf && subscription.getConsortium()?.id == contextService.getOrg().id)}">
                    <div class="item la-cb-action-ext">
                        <div class="ui simple dropdown clearable button icon">
                            <i class="${Icon.SYM.LINKED_OBJECTS}"></i>
                            <div class="menu">
                                <g:if test="${subscription.instanceOf}">
                                    <g:link controller="subscription" action="show" id="${subscription.instanceOf.id}" class="item la-flexbox">
                                        <span class="text">
                                            ${subscription}
                                        </span>
                                        <span class="description">
                                            <i class="icon arrow up la-list-icon"></i>
                                            ${message(code:'consortium.superSubscriptionType')}
                                        </span>
                                    </g:link>
                                    <g:if test="${linkifyMap}">
                                        <div class="divider"></div>
                                    </g:if>
                                </g:if>
                                <g:each in="${linkifyMap}" var="linkifyCat">
                                    <g:each in="${linkifyCat.getValue()}" var="link">
                                        <g:set var="linkTarget" value="${link.determineSource() == subscription ? link.determineDestination() : link.determineSource()}" />
                                        <g:set var="linkPrio" value="${link.determineSource() == subscription ? 0 : 1}" />
                                        <g:if test="${linkTarget instanceof de.laser.Subscription}">
                                            <g:set var="linkType" value="${link.linkType.getI10n('value').split("\\|")[linkPrio]}" />
                                            <g:link controller="subscription" action="show" id="${linkTarget.id}" class="item la-flexbox">
                                                <span class="text">
                                                    ${linkTarget}
                                                    (<g:formatDate formatName="default.date.format.notime" date="${linkTarget.startDate}"/> - <g:formatDate formatName="default.date.format.notime" date="${linkTarget.endDate}"/>)
                                                </span>
                                                <span class="description">
                                                    <g:if test="${link.linkType == RDStore.LINKTYPE_FOLLOWS}">
                                                        <i class="${linkPrio == 1 ? Icon.LNK.NEXT : Icon.LNK.PREV} la-list-icon"></i>
                                                    </g:if>
                                                    <g:else>
                                                        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                    </g:else>
                                                    ${linkType}
                                                </span>
                                            </g:link>
                                        </g:if>
                                        <g:elseif test="${linkTarget instanceof de.laser.License}">
                                            <g:set var="linkType" value="${link.linkType.getI10n('value').split("\\|")[Math.abs(linkPrio-1)]}" />
                                            <g:link controller="license" action="show" id="${linkTarget.id}" class="item la-flexbox">
                                                <span class="text">
                                                    ${linkTarget}
                                                    (<g:formatDate formatName="default.date.format.notime" date="${linkTarget.startDate}"/> - <g:formatDate formatName="default.date.format.notime" date="${linkTarget.endDate}"/>)
                                                </span>
                                                <span class="description">
                                                    <i class="${Icon.LICENSE} la-list-icon"></i>
                                                    ${linkType}
                                                </span>
                                            </g:link>
                                        </g:elseif>
                                    </g:each>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </g:if>
            </g:if>
            <g:elseif test="${controllerName == 'license' && license}">
                <g:set var="linkifyMap" value="${linksGenerationService.getSourcesAndDestinations(license, contextUser, RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)-RDStore.LINKTYPE_LICENSE)}" />

                <g:if test="${linkifyMap || license.instanceOf}">
                    <div class="item la-cb-action-ext">
                        <div class="ui simple dropdown button icon">
                            <i class="${Icon.SYM.LINKED_OBJECTS}"></i>
                            <div class="menu">
                                <g:if test="${license.instanceOf}">
                                    <g:link controller="license" action="show" id="${license.instanceOf.id}" class="item la-flexbox">
                                        <span class="text">
                                            ${license}
                                        </span>
                                        <span class="description">
                                            <i class="icon arrow up la-list-icon"></i>
                                            ${message(code:'consortium.superLicenseType')}
                                        </span>
                                    </g:link>
                                    <g:if test="${linkifyMap}">
                                        <div class="divider"></div>
                                    </g:if>
                                </g:if>
                                <g:each in="${linkifyMap}" var="linkifyCat">
                                    <g:each in="${linkifyCat.getValue()}" var="link">
                                        <g:set var="linkTarget" value="${link.determineSource() == license ? link.determineDestination() : link.determineSource()}" />
                                        <g:set var="linkPrio" value="${link.determineSource() == license ? 0 : 1}" />
                                        <g:if test="${linkTarget instanceof de.laser.Subscription}">
                                            <g:set var="linkType" value="${link.linkType.getI10n('value').split("\\|")[Math.abs(linkPrio-1)]}" />
                                            <g:link controller="subscription" action="show" id="${linkTarget.id}" class="item la-flexbox">
                                                <span class="text">
                                                    ${linkTarget}
                                                    (<g:formatDate formatName="default.date.format.notime" date="${linkTarget.startDate}"/> - <g:formatDate formatName="default.date.format.notime" date="${linkTarget.endDate}"/>)
                                                </span>
                                                <span class="description">
                                                    <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                                    ${linkType}
                                                </span>
                                            </g:link>
                                        </g:if>
                                        <g:elseif test="${linkTarget instanceof de.laser.License}">
                                            <g:set var="linkType" value="${link.linkType.getI10n('value').split("\\|")[linkPrio]}" />
                                            <g:link controller="license" action="show" id="${linkTarget.id}" class="item la-flexbox">
                                                <span class="text">
                                                    ${linkTarget}
                                                    (<g:formatDate formatName="default.date.format.notime" date="${linkTarget.startDate}"/> - <g:formatDate formatName="default.date.format.notime" date="${linkTarget.endDate}"/>)
                                                </span>
                                                <span class="description">
                                                    <g:if test="${link.linkType == RDStore.LINKTYPE_FOLLOWS}">
                                                        <i class="${linkPrio == 1 ? Icon.LNK.PREV : Icon.LNK.NEXT} la-list-icon"></i>
                                                    </g:if>
                                                    <g:else>
                                                        <i class="${Icon.LICENSE} la-list-icon"></i>
                                                    </g:else>
                                                    ${linkType}
                                                </span>
                                            </g:link>
                                        </g:elseif>
                                    </g:each>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </g:if>
            </g:elseif>

        </div>%{-- la-advanced-view --}%

    </div>%{-- container --}%

</nav>%{-- contextBar --}%

%{-- help flyout --}%

<g:if test="${helpMatch == HelpService.GSP}">
    <div class="ui very wide flyout" id="help-content">
        <g:render template="${'/help/' + helpService.getMapping()}" />
    </div>
</g:if>
<g:elseif test="${helpMatch in [HelpService.MD, HelpService.BOTH]}">
    <div class="ui very wide la-markdown flyout" id="help-content">
        <div class="scrolling content">
            <ui:renderMarkdown help="${helpService.getMapping()}" />

            <g:if test="${helpMatch == HelpService.BOTH}">
                <br/>
                <g:render template="${'/help/' + helpService.getMapping()}" />
            </g:if>
        </div>
        <div class="basic center aligned actions">
            <a href="mailto:laser@hbz-nrw.de?subject=Supportanfrage">Bei weiteren Fragen erreichen Sie uns per Email (laser@hbz-nrw.de)</a>
        </div>
    </div>
</g:elseif>

<style>
    #contextBar .la-advanced-view .item.la-cb-action-ext .item.la-flexbox {
        display: flex;
    }
    #contextBar .la-advanced-view .item.la-cb-action-ext .item.la-flexbox span.text {
        float: none;
        text-align: left;
    }
    #contextBar .la-advanced-view .item.la-cb-action-ext .item.la-flexbox span.description {
        width: 100%;
        float: none;
        text-align: right;
    }
</style>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.contextBar = {

        $cbContextDisplay:  $('#la-js-cb-context-display'),
        $cbInfoDisplay:     $('#la-cb-info-display'),

        init: function() {
            $('.la-cb-context.item > *[data-display]').hover(
                function() {
                    JSPC.app.contextBar.$cbContextDisplay.addClass('active').text($(this).attr('data-display'));
                },
                function() {
                    JSPC.app.contextBar.$cbContextDisplay.removeClass('active');
                    setTimeout( function(){
                        $('#la-js-cb-context-display:not(.active)').text(JSPC.app.contextBar.$cbContextDisplay.attr('data-display'));
                    }, 750);
                }
            );
            $('.la-cb-info.item > .label[data-display]').hover(
                function() {
                    JSPC.app.contextBar.$cbInfoDisplay.addClass('active').text($(this).attr('data-display'));
                },
                function() {
                    JSPC.app.contextBar.$cbInfoDisplay.removeClass('active');
                    setTimeout( function(){ $('#la-cb-info-display:not(.active)').text(''); }, 750);
                }
            );

            setTimeout( function(){
                $('main > nav.la-js-ctrls > .button').each ( function() {
                    let $item = $('<div class="item la-cb-action-ext"></div>');
                    $('.la-advanced-view').append($item);
                    $item.append(this);
                    $(this).addClass('icon');
                })
                $('main > nav.la-js-ctrls').each ( function() {
                    let $new = $('<div class="la-action-ext-modalWrapper" style="width: inherit;"></div>');
                    $(this).contents().each ( function() {
                        $new.append($(this));
                    })
                    $(this).replaceWith($new);
                })

                $('.la-cb-action-ext > .ui.dropdown').dropdown('destroy').dropdown({
                    selectOnKeydown: false,
                    clearable: true,
                    on: 'hover',
                    displayType: 'block'
                })

                tooltip.initDynamicPopups('.la-cb-action-ext')

                $('.la-context-org, .la-advanced-view').fadeIn(150);
            }, 100);

            $('#advancedMode-toggle').on('click', function() {
                let $button = $(this);
                let reload = $button.attr('data-reload');
                if (reload) {
                    window.location.href = reload
                }
            });

            $('.button.la-toggle-ui').on('click', function() {
                $(this).toggleClass('active');
            });

            $('#help-toggle').on('click', function() {
                $('#help-content').flyout('toggle');
            });

            $('#subscriptionMembers-toggle').on('click', function() {
                $('#subscriptionMembers-content').flyout('toggle');
            });

            $('#subscriptionTransfer-toggle').on('click', function() {
                let $button = $(this);
                let $content = $('#subscriptionTransfer-content')
                if ($button.hasClass('active')) {
                    $content.show();
                    let padding = 45 + $content.height() + $('main.main nav.breadcrumb').height();
                    $('main.main').css('padding-top', padding)
                } else {
                    $content.hide();
                    $('main.main').css('padding-top', 0)
                }
            });
        }
    }

    JSPC.app.contextBar.init();
</laser:script>


