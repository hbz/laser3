<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore; de.laser.UserSetting; de.laser.auth.User; de.laser.auth.Role; de.laser.Org" %>
<laser:serviceInjection />

<g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />

<nav class="ui fixed menu la-contextBar" aria-label="${message(code:'wcag.label.modeNavigation')}">

    <div class="ui container">
        <button class="ui button big la-menue-button la-modern-button" style="display:none"><i class="bars icon"></i></button>

        <div class="ui sub header item la-context-org" style="display: none">
            <div class="item la-cb-context">
                <ui:customerTypeIcon org="${contextOrg}" config="display"/>
            </div>
            &nbsp; / &nbsp;
            <div class="item la-cb-context">
                <ui:userAffiliationIcon user="${contextUser}" config="display"/>
            </div>
            &nbsp; / &nbsp;

            <div id="la-cb-context-display" data-display="${contextOrg?.name}">
                ${contextOrg?.name}
            </div>
        </div>

        <div class="right menu la-advanced-view" style="display: none">

            <div id="la-cb-info-display"></div>

        %{-- isMyObject indicator --}%

            <g:if test="${isMyPlatform}">
                <div class="item la-cb-info">
                    <ui:contextBarInfoIcon config="display" text="${message(code: 'license.relationship.platform')}" icon="star" color="yellow" />
                </div>
            </g:if>
            <g:elseif test="${isMyPkg}">
                <div class="item la-cb-info">
                    <ui:contextBarInfoIcon config="display" text="${message(code: 'license.relationship.pkg')}" icon="star" color="violet" />
                </div>
            </g:elseif>
            <g:elseif test="${isMyOrg}">
                <div class="item la-cb-info">
                    <ui:contextBarInfoIcon config="display" text="${message(code: 'license.relationship.org')}" icon="star" color="teal" />
                </div>
            </g:elseif>

        %{-- child indicator --}%

            <g:if test="${controllerName == 'subscription' && subscription}">
                <g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortia()?.id}">
                    <div class="item la-cb-info">
                        <ui:contextBarInfoIcon config="display" text="Sie sehen eine Kindlizenz" icon="child" color="brown" />
                    </div>
                </g:if>
            </g:if>

            <g:if test="${controllerName == 'license' && license}">
                <g:if test="${license.instanceOf && contextService.getOrg().id == license.getLicensingConsortium()?.id}">
                    <div class="item la-cb-info">
                        <ui:contextBarInfoIcon config="display" text="Sie sehen einen Einrichtungsvertrag" icon="child" color="brown" />
                    </div>
                </g:if>
            </g:if>

        %{-- content indicator --}%

            <g:if test="${flagContentCache}">
                <div class="item la-cb-info">
                    <ui:contextBarInfoIcon config="display" text="${message(code: 'statusbar.flagContentCache.tooltip')}" icon="hourglass" color="orange" />
                </div>
            </g:if>
            <g:if test="${flagContentGokb}">
                <div class="item la-cb-info">
                    <ui:contextBarInfoIcon config="display" text="${message(code: 'statusbar.flagContentGokb.tooltip')}" icon="cloud" color="blue" />
                </div>
            </g:if>
            <g:if test="${flagContentElasticsearch}">
                <div class="item la-cb-info">
                    <ui:contextBarInfoIcon config="display" text="${message(code: 'statusbar.flagContentElasticsearch.tooltip')}" icon="cloud" color="blue" />
                </div>
            </g:if>

        %{-- marker --}%

        <g:if test="${controllerName == 'organisation'}">
            <g:if test="${isProviderOrAgency}">
                <ui:markerSwitch org="${orgInstance}"/>
            </g:if>
        </g:if>
        <g:elseif test="${controllerName == 'package'}">
            <ui:markerSwitch package="${packageInstance}"/>
        </g:elseif>
        <g:elseif test="${controllerName == 'platform'}">
            <ui:markerSwitch platform="${platformInstance}"/>
        </g:elseif>

        %{-- edit mode switcher  --}%

            <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription' || controllerName=='license') && actionName=='show' && (editable || contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))}">
                <div class="item la-cb-action">
                    <g:if test="${contextUser?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)?.value=='Yes'}">
                        <button class="ui icon toggle active la-toggle-advanced blue button la-modern-button la-popup-tooltip la-delay"
                                data-content="${message(code:'statusbar.showButtons.tooltip')}" data-position="bottom center">
                            <i class="pencil alternate icon"></i>
                        </button>
                    </g:if>
                    <g:else>
                        <button class="ui icon toggle inactive la-toggle-advanced blue button la-modern-button la-popup-tooltip la-delay"
                                data-content="${message(code:'statusbar.hideButtons.tooltip')}" data-position="bottom center">
                            <i class="pencil alternate slash icon"></i>
                        </button>
                    </g:else>
                </div>
            </g:if>

        %{-- advanced mode switcher  --}%

            <g:if test="${(params.mode)}">
                <div class="item la-cb-action">
                    <g:if test="${params.mode=='advanced'}">
                        <div class="ui icon toggle active la-toggle-advanced blue button la-modern-button la-popup-tooltip la-delay"
                             data-content="${message(code:'statusbar.showAdvancedView.tooltip')}" data-position="bottom center"
                             data-reload="<g:createLink action="${actionName}" params="${params + ['mode':'basic']}" />">
                            <i class="icon plus square"></i>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="ui icon toggle inactive la-toggle-advanced blue button la-modern-button la-popup-tooltip la-delay"
                             data-content="${message(code:'statusbar.showBasicView.tooltip')}" data-position="bottom center"
                             data-reload="<g:createLink action="${actionName}" params="${params + ['mode':'advanced']}" />">
                            <i class="icon plus square green slash"></i>
                        </div>
                    </g:else>
                </div>
            </g:if>

        %{-- survey stuff  --}%

            <g:if test="${controllerName == 'survey' && (actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia')}">
                <div class="item la-cb-action">
                    <g:if test="${actionName == 'workflowsSurveysConsortia'}">
                        <g:link action="currentSurveysConsortia" controller="survey" class="ui icon blue button la-modern-button la-popup-tooltip la-delay"
                                data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom center">
                            <i class="exchange icon"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link action="workflowsSurveysConsortia" controller="survey" class="ui icon blue button la-modern-button la-popup-tooltip la-delay"
                                data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom center">
                            <i class="exchange icon"></i>
                        </g:link>
                    </g:else>
                </div>
            </g:if>
            <g:if test="${(controllerName=='subscription' && actionName=='show') || (controllerName=='dev' && actionName=='frontend')}">
                <div class="item la-cb-action">
                    <button class="ui icon button blue la-modern-button la-help-panel-button"><i class="info circle icon"></i></button>
                </div>
            </g:if>

        </div>%{-- la-advanced-view --}%

    </div>

</nav>%{-- la-contextBar --}%

<style>
#la-cb-info-display {
    font-size: 0.87em;
    font-weight: bold;
    color: grey;
    margin-right: 1em;
}
.la-cb-info.item {
    margin: 0 1em 0 0 !important;
}
.la-cb-info.item > .label {
    margin: 0 !important;
    border: 1px solid #e3eaf3 !important;
    background-color: #e3eaf3 !important;
}
.la-cb-info.item > .label > .icon {
    margin: 0 !important;
}

.la-cb-action.item {
    margin: 0 0 0 1px !important;
    border-right: none !important;
}
.la-cb-action.item > .button {
    height: 40px !important;
    width: 42px !important;
    border-radius: 0 !important;
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

.la-cb-info.item + .la-cb-action.item,
.la-cb-info.item + .la-cb-action-ext.item {
    margin-left: 2em !important;
}

.la-cb-action-ext.item {
    margin: 0 0 0 1px !important;
    border-right: none !important;
}
.la-cb-action-ext.item > .button {
    height: 40px !important;
    width: 42px !important;
    border-radius: 0 !important;
    /*background-color: rgba(0,0,0, 0.1) !important;*/
    background-color: #004678  !important;
}
.la-cb-action-ext.item > .button:hover {
    /*background-color: rgba(0,0,0, 0.2) !important;*/
    background-color: #003668 !important;
}
.la-cb-action-ext.item > .button > .icon {
    /*color: #004678 !important;*/
    color: #fff !important;
}
/* -- overrides -- */

.la-contextBar .ui.button.toggle.active,
.la-contextBar .ui.buttons .button.toggle.active,
.la-contextBar .ui.toggle.buttons .active.button {
    background-color: #98b500 !important;
}
.la-contextBar .ui.button.toggle.inactive,
.la-contextBar .ui.buttons .button.toggle.inactive,
.la-contextBar .ui.toggle.buttons .inactive.button {
    background-color: #D95F3D !important;
}

.la-contextBar .ui.button.toggle.active > .icon,
.la-contextBar .ui.buttons .button.toggle.active > .icon,
.la-contextBar .ui.toggle.buttons .active.button > .icon {
    color: #fff !important;
}
.la-contextBar .ui.button.toggle.inactive > .icon,
.la-contextBar .ui.buttons .button.toggle.inactive > .icon,
.la-contextBar .ui.toggle.buttons .inactive.button > .icon {
    color: #fff !important;
}

.la-contextBar.ui.menu {
    box-shadow: none !important;
}
.la-contextBar.ui.menu .la-context-org {
    flex: 0 0 500px;
}
.la-contextBar.ui.menu .item::before {
    width: 0 !important;
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
        $('.la-context-org, .la-advanced-view').fadeIn(50);
    }, 100);
</laser:script>
