<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore; de.laser.UserSetting; de.laser.auth.User; de.laser.auth.Role; de.laser.Org" %>
<laser:serviceInjection />

<g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />

<nav class="ui fixed menu la-contextBar" aria-label="${message(code:'wcag.label.modeNavigation')}">

    <div class="ui container">
        <button class="ui button big la-menue-button la-modern-button" style="display:none"><i class="bars icon"></i></button>

        <div class="ui sub header item la-context-org" style="display: none">
            <ui:cbItemCustomerType org="${contextOrg}" />
            <ui:cbItemUserAffiliation user="${contextUser}" />
            <ui:cbItemUserSysRole user="${contextUser}" />

            <div id="la-cb-context-display" data-display="${contextOrg?.name}">
                ${contextOrg?.name}
            </div>
        </div>

        <div class="right menu la-advanced-view" style="display: none">

            <div id="la-cb-info-display"></div>

            %{-- my object indicator --}%

            <g:if test="${isMyPlatform}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.platform')}" icon="star" color="yellow" />
            </g:if>
            <g:elseif test="${isMyPkg}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.pkg')}" icon="star" color="violet" />
            </g:elseif>
            <g:elseif test="${isMyOrg}">
                <ui:cbItemInfo display="${message(code: 'license.relationship.org')}" icon="star" color="teal" />
            </g:elseif>

            %{-- child indicator --}%

            <g:if test="${controllerName == 'subscription' && subscription}">
                <g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortia()?.id}">
                    <ui:cbItemInfo display="Sie sehen eine Kindlizenz" icon="child" color="orange" />
                </g:if>
            </g:if>

            <g:if test="${controllerName == 'license' && license}">
                <g:if test="${license.instanceOf && contextService.getOrg().id == license.getLicensingConsortium()?.id}">
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

            <g:if test="${(controllerName=='subscription' && actionName=='show') || (controllerName=='dev' && actionName=='frontend')}">
                <div class="item la-cb-action">
                    <button class="ui icon button la-help-panel-button"><i class="question circle icon"></i></button>
                </div>
            </g:if>

            %{-- edit mode switcher  --}%

            <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription' || controllerName=='license') && actionName=='show' && (editable || contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))}">
                <g:if test="${contextUser?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)?.value=='Yes'}">
                    <ui:cbItemToggleAction status="active" icon="pencil alternate" tooltip="${message(code:'statusbar.showButtons.tooltip')}" />
                </g:if>
                <g:else>
                    <ui:cbItemToggleAction status="inactive" icon="pencil alternate slash" tooltip="${message(code:'statusbar.hideButtons.tooltip')}" />
                </g:else>
            </g:if>

            %{-- advanced mode switcher  --}%

            <g:if test="${(params.mode)}">
                <g:if test="${params.mode=='advanced'}">
                    <ui:cbItemToggleAction status="active" icon="plus square" tooltip="${message(code:'statusbar.showAdvancedView.tooltip')}"
                                               reload="${g.createLink(action: actionName, params: params + ['mode':'basic'])}" />
                </g:if>
                <g:else>
                    <ui:cbItemToggleAction status="inactive" icon="plus square slash" tooltip="${message(code:'statusbar.showBasicView.tooltip')}"
                                               reload="${g.createLink(action: actionName, params: params + ['mode':'advanced'])}" />
                </g:else>
            </g:if>

            %{-- survey stuff  --}%

            <g:if test="${controllerName == 'survey' && (actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia')}">
                <div class="item la-cb-action">
                    <g:if test="${actionName == 'workflowsSurveysConsortia'}">
                        <g:link action="currentSurveysConsortia" controller="survey" class="ui icon button la-popup-tooltip la-delay"
                                data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom center">
                            <i class="la-tab icon"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link action="workflowsSurveysConsortia" controller="survey" class="ui icon button la-popup-tooltip la-delay"
                                data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom center">
                            <i class="la-tab slash icon"></i>
                        </g:link>
                    </g:else>
                </div>
            </g:if>

            %{-- marker --}%

            <g:if test="${controllerName == 'organisation'}">
                <g:if test="${isProviderOrAgency}">
                    <ui:cbItemMarkerAction org="${orgInstance}"/>
                </g:if>
            </g:if>
            <g:elseif test="${controllerName == 'package'}">
                <g:if test="${packageInstance}">
                    <ui:cbItemMarkerAction package="${packageInstance}"/>
                </g:if>
            </g:elseif>
            <g:elseif test="${controllerName == 'platform'}">
                <g:if test="${platformInstance}">
                    <ui:cbItemMarkerAction platform="${platformInstance}"/>
                </g:if>
            </g:elseif>

        </div>%{-- la-advanced-view --}%

    </div>%{-- container --}%

</nav>%{-- la-contextBar --}%



%{-- stable --}%
<style>


    #la-cb-info-display {
        font-size: 0.87em;
        font-weight: bold;
        color: grey;
        margin-right: 1em;
    }

    .la-cb-context.item,
    .la-cb-info.item {
        margin: 0 1em 0 0 !important;
    }
    .la-cb-context.item > .label,
    .la-cb-info.item > .label {
        margin: 0 !important;
        background-color: #e3eaf3 !important;
    }
    .la-cb-context.item > .label:hover,
    .la-cb-info.item > .label:hover {
        cursor: help;
    }
    .la-cb-context.item > .label > .icon,
    .la-cb-info.item > .label > .icon {
        margin: 0 !important;
    }

    .la-cb-info.item + .la-cb-action.item,
    .la-cb-info.item + .la-cb-action-ext.item {
        margin-left: 2em !important;
    }

    .la-cb-action.item,
    .la-cb-action-ext.item {
        margin: 0 0 0 1px !important;
        border-right: none !important;
    }

    .la-cb-action.item > .button {
        background-color: rgba(0,0,0, 0.1) !important;
        /*background-color: #004678  !important;*/
    }
    .la-cb-action.item > .button:hover {
        background-color: rgba(0,0,0, 0.2) !important;
        /*background-color: #003668 !important;*/
    }
    .la-cb-action.item > .button > .icon {
        color: #004678 !important;
        /*color: #fff !important;*/
    }
    .la-cb-action.item > .button:hover > .icon {
        color: #000 !important;
    }

    /* -- overrides -- */

    .la-contextBar .la-cb-action.item .button.la-toggle-advanced.active {
        background-color: #98b500 !important;
    }
    .la-contextBar .la-cb-action.item .button.la-toggle-advanced.inactive {
        background-color: #D95F3D !important;
    }
    .la-contextBar .la-cb-action.item .button.toggle .icon {
        color: #fff !important;
    }

</style>

%{-- unstable --}%
<style>


    .la-cb-info.item > .label {
        border: 1px solid #e3eaf3 !important;
    }

    .la-cb-action.item > .button,
    .la-cb-action-ext.item > .button {
        height: 40px !important;
        width: 42px !important;
        border-radius: 0 !important;
    }

/* -- todo -- */

.la-contextBar .la-cb-action.item .button.purple.active {
    background-color: #2185d0 !important;
}
.la-contextBar .la-cb-action.item .button.purple.active .icon {
    color: #fff !important;
}
.la-contextBar .la-cb-action.item .button.purple.inactive {
}
.la-contextBar .la-cb-action.item .button.purple.inactive .icon {
    color: #2185d0 !important;
}



</style>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.initLaToggle = function() {
        let $button = $('.button.la-toggle-advanced');
        let reload = $button.attr('data-reload');

        var handler = {
            activate: function() {
                $icon = $(this).find('.icon');
                if ($(this).hasClass("inactive")) {
                    $(this).removeClass('inactive').addClass('active')
                    $icon.removeClass("slash");
                    if (reload) {
                        window.location.href = reload
                    }
                }
                else {
                    $(this).removeClass('active').addClass('inactive')
                    $icon.addClass("slash");
                    if (reload) {
                        window.location.href = reload
                    }
                }
            }
        };
        $button.on('click', handler.activate);
    };
    JSPC.app.initLaToggle();

    var $cbContextDisplay = $('#la-cb-context-display')
    var $cbInfoDisplay = $('#la-cb-info-display')

    $('.la-cb-context.item > *[data-display]').hover(
        function() {
            $cbContextDisplay.addClass('active').text($(this).attr('data-display'))
        },
        function() {
            $cbContextDisplay.removeClass('active')
            setTimeout( function(){ $('#la-cb-context-display:not(.active)').text($cbContextDisplay.attr('data-display')); }, 750);
        }
    );
        $('.la-cb-info.item > .label[data-display]').hover(
        function() {
            $cbInfoDisplay.addClass('active').text($(this).attr('data-display') + ' (INFO) ')
        },
        function() {
            $cbInfoDisplay.removeClass('active')
            setTimeout( function(){ $('#la-cb-info-display:not(.active)').text(''); }, 750);
        }
    );

    setTimeout( function(){
        $('nav.buttons > .button').each( function() {
            let $item = $('<div class="item la-cb-action-ext"></div>')
            $('.la-advanced-view').append($item)
            $item.append(this)
            $(this).addClass('icon')
        })
        $('.la-context-org, .la-advanced-view').fadeIn(100);
    }, 100);
</laser:script>
