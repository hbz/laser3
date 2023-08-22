<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore; de.laser.UserSetting; de.laser.auth.User; de.laser.auth.Role; de.laser.Org" %>
<laser:serviceInjection />

    <g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />
    <g:set var="cb_isMyX" value="${isMyPlatform || isMyPkg || isMyOrg}" />
    <g:set var="cb_isFlagContent" value="${flagContentCache || flagContentGokb || flagContentElasticsearch}" />

    <nav class="la-cb-wrapper fixed" aria-label="${message(code:'wcag.label.modeNavigation')}">
        <div class="ui container three column grid">

            <div class="nine wide column la-cb-context">
                <ui:customerTypeIcon org="${contextOrg}" />
                &nbsp; / &nbsp;
                <ui:userAffiliationIcon user="${contextUser}" />
                &nbsp; / &nbsp;
                ${contextOrg?.name}

            </div><!-- .la-cb-context -->

            <div class="two wide column la-cb-info">
                <g:if test="${isMyPlatform}">
                    <div class="item">
                        <ui:contextBarInfoIcon tooltip="${message(code: 'license.relationship.platform')}" icon="star" color="yellow" />
                    </div>
                </g:if>
                <g:elseif test="${isMyPkg}">
                    <div class="item">
                        <ui:contextBarInfoIcon tooltip="${message(code: 'license.relationship.pkg')}" icon="star" color="violet" />
                    </div>
                </g:elseif>
                <g:elseif test="${isMyOrg}">
                    <div class="item">
                        <ui:contextBarInfoIcon tooltip="${message(code: 'license.relationship.org')}" icon="star" color="teal" />
                    </div>
                </g:elseif>

                <g:if test="${flagContentCache}">
                    <div class="item">
                        <ui:contextBarInfoIcon tooltip="${message(code: 'statusbar.flagContentCache.tooltip')}" icon="hourglass" color="orange" />
                    </div>
                </g:if>
                <g:if test="${flagContentGokb}">
                    <div class="item">
                        <ui:contextBarInfoIcon tooltip="${message(code: 'statusbar.flagContentGokb.tooltip')}" icon="cloud" color="blue" />
                    </div>
                </g:if>
                <g:if test="${flagContentElasticsearch}">
                    <div class="item">
                        <ui:contextBarInfoIcon tooltip="${message(code: 'statusbar.flagContentElasticsearch.tooltip')}" icon="cloud" color="blue" />
                    </div>
                </g:if>
            </div><!-- .la-cb-info -->

            <div class="five wide column la-cb-options la-cb-actions">

                %{-- edit mode switcher  --}%
                <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription' || controllerName=='license') && actionName=='show' && (editable || contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))}">
                    <div class="item">
                        <g:if test="${contextUser?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)?.value=='Yes'}">
                            <button class="ui icon active button toggle la-cb-option-button la-popup-tooltip la-delay"
                                    data-content="${message(code:'statusbar.showButtons.tooltip')}" data-position="bottom center">
                                <i class="pencil alternate icon"></i>
                            </button>
                        </g:if>
                        <g:else>
                            <button class="ui icon button toggle la-cb-option-button la-popup-tooltip la-delay"
                                    data-content="${message(code:'statusbar.hideButtons.tooltip')}" data-position="bottom center">
                                <i class="pencil alternate slash icon"></i>
                            </button>
                        </g:else>
                    </div>
                </g:if>

                %{-- advanced mode switcher  --}%
                <g:if test="${(params.mode)}">
                    <div class="item">
                        <g:if test="${params.mode=='advanced'}">
                            <div class="ui icon button la-cb-option-button la-toggle-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code:'statusbar.showAdvancedView.tooltip')}" data-position="bottom center">
                                    <i class="icon plus square"></i>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="ui icon button la-cb-option-button la-toggle-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code:'statusbar.showBasicView.tooltip')}" data-position="bottom center">
                                    <i class="icon plus square green slash"></i>
                            </div>
                        </g:else>
                    </div>

                    <laser:script file="${this.getGroovyPageFileName()}">
                        JSPC.app.initLaToggle = function() {
                            var $button = $('.button.la-toggle-advanced');
                            var handler = {
                                activate: function() {
                                    $icon = $(this).find('.icon');
                                    if ($icon.hasClass("slash")) {
                                        $icon.removeClass("slash");
                                        window.location.href = "<g:createLink action="${actionName}" params="${params + ['mode':'advanced']}" />";
                                        }
                                         else {
                                            $icon.addClass("slash");
                                            window.location.href = "<g:createLink action="${actionName}" params="${params + ['mode':'basic']}" />" ;
                                        }
                                    }
                                };
                                $button.on('click', handler.activate);
                            };

                            JSPC.app.initLaToggle();
                    </laser:script>
                </g:if>

                %{-- survey stuff  --}%
                <g:if test="${controllerName == 'survey' && (actionName == 'currentSurveysConsortia' || actionName == 'workflowsSurveysConsortia')}">
                    <div class="item">
                        <g:if test="${actionName == 'workflowsSurveysConsortia'}">
                            <g:link action="currentSurveysConsortia" controller="survey" class="ui button la-cb-option-button la-popup-tooltip la-delay"
                                    data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                <i class="exchange icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link action="workflowsSurveysConsortia" controller="survey" class="ui button la-cb-option-button la-popup-tooltip la-delay"
                                    data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                <i class="exchange icon"></i>
                            </g:link>
                        </g:else>
                    </div>
                </g:if>

                <g:if test="${(controllerName=='subscription' && actionName=='show') || (controllerName=='dev' && actionName=='frontend')}">
                    <div class="item">
                        <button class="ui icon button la-cb-option-button la-help-panel-button"><i class="info circle icon"></i></button>
                    </div>
                </g:if>

%{--                <div class="item"></div><!-- container: la-cb-actions -->--}%

            </div><!-- .la-cb-options / .la-cb-actions -->

%{--            <div class="three wide column la-cb-actions"></div><!-- .la-cb-actions -->--}%

        </div><!-- .container .grid -->
    </nav><!-- .la-cb-wrapper -->

    <style>
    .la-cb-wrapper {
        position: fixed;
        top: 50px;
        z-index: 101;
        width: 100%;
        margin: 0;
        padding: 0 !important;
        background-color: #d3dae3;
        border-bottom: 1px solid #c3cad3;
    }
    .la-cb-wrapper > .container {
        margin: 0;
    }
    .la-cb-wrapper > .container > .column {
        /*padding-top: 0.5rem !important;*/
        /*padding-bottom: 0.5rem !important;*/
        /*border-right: 1px solid #c3cad3;*/
    }

    .la-cb-context {
        font-family: "Lato", system-ui, -apple-system, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", Arial, "Noto Sans", "Liberation Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
        font-weight: bold;
        font-size: 90%;
        color: #767676;
        padding-left: 0 !important;
        /*border-left: 1px solid #c3cad3;*/
    }

    .la-cb-info {
        text-align: right;
        padding-top: 10px !important;
        padding-bottom: 10px !important;
    }
    .la-cb-info > .item > .label {
        background-color: #f4f8f9;
        border: 1px solid #c3cad3;
    }
    .la-cb-info > .item > .label > .icon {
        margin: 0 !important;
    }

    .la-cb-options {
    }
    .la-cb-options > .item > .la-cb-option-button {
        background-color: rgba(0,0,0, 0.1) !important;
    }
    .la-cb-options > .item > .la-cb-option-button:hover {
        cursor: pointer;
        background-color: rgba(0,0,0, 0.2) !important;
    }
    .la-cb-options > .item > .la-cb-option-button i.icon {
        color: #004678 !important;
    }

    .la-cb-actions {
        padding-right: 0 !important;
    }

    .la-cb-options,
    .la-cb-actions {
        text-align: right;
        padding-top: 5px !important;
        padding-bottom: 5px !important;
        border-left: 1px solid #c3cad3;
    }
    .la-cb-info > .item,
    .la-cb-options > .item,
    .la-cb-actions > .item {
        display: inline-block;
        margin-left: 3px;
    }
    .la-cb-info > .item > .button,
    .la-cb-options > .item > .button,
    .la-cb-actions > .item > .button {
        margin: 0 !important;
    }

    main.ui.container.main {
        margin-top: 115px !important;
    }
    </style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('nav.buttons > .button').each( function() {
        let $item = $('<div class="item"></div>')
        $('.la-cb-wrapper .la-cb-actions').append($item)
        $item.append(this)
        $(this).addClass('icon')
    })
%{--    $('.la-cb-wrapper .la-cb-actions .item:last-of-type').append($('nav.buttons'));--}%
</laser:script>
