<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore; de.laser.UserSetting; de.laser.auth.User; de.laser.auth.Role; de.laser.Org" %>
<laser:serviceInjection />

<g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />

<g:if test="${false /*! AppUtils.isPreviewOnly()*/ }">

    <nav class="ui fixed menu la-contextBar" aria-label="${message(code:'wcag.label.modeNavigation')}">

        <div class="ui container">
            <button class="ui button big la-menue-button la-modern-button" style="display:none"><i class="bars icon"></i></button>
            <div class="ui sub header item la-context-org">
                <g:if test="${contextOrg}">
                    ${contextOrg.name}
                    <g:if test="${currentServer == AppUtils.LOCAL}">
                        - ${contextOrg.getCustomerTypeI10n()}
                        - ${contextUser.formalRole?.getI10n('authority')}
                    </g:if>
                </g:if>
            </div>

            <div class="right menu la-advanced-view">

                %{-- content indicator --}%

                <div class="item">
                    <g:if test="${flagContentCache}">
                        <i class="hourglass end icon la-popup-tooltip la-delay" data-content="${message(code:'statusbar.flagContentCache.tooltip')}" data-position="bottom right" data-variation="tiny"></i>
                    </g:if>
                    <g:if test="${flagContentGokb}">
                        <i class="cloud icon la-popup-tooltip la-delay" data-content="${message(code:'statusbar.flagContentGokb.tooltip')}" data-position="bottom right" data-variation="tiny"></i>
                    </g:if>
                    <g:if test="${flagContentElasticsearch}">
                        <i class="cloud icon la-popup-tooltip la-delay" data-content="${message(code:'statusbar.flagContentElasticsearch.tooltip')}" data-position="bottom right" data-variation="tiny"></i>
                    </g:if>
                </div>

            %{-- edit mode switcher  --}%

                <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription' || controllerName=='license') && actionName=='show' && (editable || contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))}">
                    <div class="item">
                        <g:if test="${contextUser?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)?.value=='Yes'}">
                            <button class="ui icon toggle active blue button la-modern-button la-toggle-controls la-popup-tooltip la-delay" data-content="${message(code:'statusbar.showButtons.tooltip')}" data-position="bottom right">
                                <i class="pencil alternate icon"></i>
                            </button>
                        </g:if>
                        <g:else>
                            <button class="ui icon toggle blue button la-modern-button la-toggle-controls la-popup-tooltip la-delay"  data-content="${message(code:'statusbar.hideButtons.tooltip')}" data-position="bottom right">
                                <i class="pencil alternate slash icon"></i>
                            </button>
                        </g:else>
                    </div>
                </g:if>

            %{-- advanced mode switcher  --}%

                <g:if test="${(params.mode)}">
                    <div class="item">
                        <g:if test="${params.mode=='advanced'}">
                            <div class="ui toggle la-toggle-advanced blue button la-modern-button la-popup-tooltip la-delay"
                                 data-content="${message(code:'statusbar.showAdvancedView.tooltip')}" data-position="bottom right">
                                <i class="icon plus square"></i>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="ui toggle la-toggle-advanced blue button la-modern-button la-popup-tooltip la-delay"
                                 data-content="${message(code:'statusbar.showBasicView.tooltip')}" data-position="bottom right">
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
                            <g:link action="currentSurveysConsortia" controller="survey" class="ui blue button la-modern-button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                <i class="exchange icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link action="workflowsSurveysConsortia" controller="survey" class="ui blue button la-modern-button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                <i class="exchange icon"></i>
                            </g:link>
                        </g:else>
                    </div>
                </g:if>
                <g:if test="${(controllerName=='subscription' && actionName=='show') || (controllerName=='dev' && actionName=='frontend')}">
                    <div class="item">
                        <button class="ui button blue la-modern-button la-help-panel-button"><i class="info circle large icon"></i></button>
                    </div>
                </g:if>

            </div>%{-- la-advanced-view --}%

        </div>

    </nav>%{-- la-contextBar --}%

</g:if>
<g:else>%{-- DEMO --}%

    <nav class="la-cb-wrapper" aria-label="${message(code:'wcag.label.modeNavigation')}">
        <div class="ui container three column grid">

            <div class="ten wide column la-cb-context">
                ${contextOrg?.name}
                &nbsp; / &nbsp;
                <ui:customerTypeIcon org="${contextOrg}" label="false"/>
                &nbsp; / &nbsp;
                <ui:userAffiliationIcon user="${contextUser}" label="false"/>
            </div><!-- .la-cb-context -->

            <div class="two wide column la-cb-info">
                <g:set var="infoIconClass" value="circular icon la-popup-tooltip la-delay" />

                <g:if test="${flagContentCache}">
                    <i class="${infoIconClass} orange hourglass end" data-content="${message(code:'statusbar.flagContentCache.tooltip')}" data-position="bottom right" data-variation="tiny"></i>
                </g:if>
                <g:if test="${flagContentGokb}">
                    <i class="${infoIconClass} blue cloud" data-content="${message(code:'statusbar.flagContentGokb.tooltip')}" data-position="bottom right" data-variation="tiny"></i>
                </g:if>
                <g:if test="${flagContentElasticsearch}">
                    <i class="${infoIconClass} teal cloud" data-content="${message(code:'statusbar.flagContentElasticsearch.tooltip')}" data-position="bottom right" data-variation="tiny"></i>
                </g:if>

            </div><!-- .la-cb-info -->

            <div class="two wide column la-cb-options">

                %{-- edit mode switcher  --}%
                <g:if test="${(controllerName=='dev' && actionName=='frontend' ) || (controllerName=='subscription' || controllerName=='license') && actionName=='show' && (editable || contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))}">
                    <div class="item">
                        <g:if test="${contextUser?.getSettingsValue(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)?.value=='Yes'}">
                            <button class="ui icon active button la-cb-option-button toggle la-toggle-controls la-popup-tooltip la-delay" data-content="${message(code:'statusbar.showButtons.tooltip')}" data-position="bottom right">
                                <i class="pencil alternate icon"></i>
                            </button>
                        </g:if>
                        <g:else>
                            <button class="ui icon button la-cb-option-button toggle la-toggle-controls la-popup-tooltip la-delay"  data-content="${message(code:'statusbar.hideButtons.tooltip')}" data-position="bottom right">
                                <i class="pencil alternate slash icon"></i>
                            </button>
                        </g:else>
                    </div>
                </g:if>

                %{-- advanced mode switcher  --}%
                <g:if test="${(params.mode)}">
                    <div class="item">
                        <g:if test="${params.mode=='advanced'}">
                            <div class="ui icon button la-cb-option-button toggle la-toggle-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code:'statusbar.showAdvancedView.tooltip')}" data-position="bottom right">
                                <i class="icon plus square"></i>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="ui icon button la-cb-option-button toggle la-toggle-advanced la-popup-tooltip la-delay"
                                 data-content="${message(code:'statusbar.showBasicView.tooltip')}" data-position="bottom right">
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
                            <g:link action="currentSurveysConsortia" controller="survey" class="ui button la-cb-option-button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.change.currentSurveysConsortiaView.tooltip')}" data-position="bottom right">
                                <i class="exchange icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link action="workflowsSurveysConsortia" controller="survey" class="ui button la-cb-option-button la-popup-tooltip la-delay" data-content="${message(code:'statusbar.change.workflowsSurveysConsortiaView.tooltip')}" data-position="bottom right">
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

            </div><!-- .la-cb-options -->

            <div class="two wide column la-cb-actions"></div><!-- .la-cb-actions -->

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
    }
    .la-cb-wrapper > .container {
        margin: 0;
    }
    .la-cb-wrapper > .container > .column {
        /*padding-top: 0.5rem !important;*/
        /*padding-bottom: 0.5rem !important;*/
    }

    .la-cb-context {
        font-family: "Lato", system-ui, -apple-system, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", Arial, "Noto Sans", "Liberation Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
        font-weight: bold;
        font-size: 90%;
        color: #767676;
        padding-left: 0 !important;
    }
    .la-cb-info {
    }
    .la-cb-options {
    }
    .la-cb-option-button {
        margin-right: 0 !important;
        margin-left: 2px !important;
        background-color: rgba(0,0,0, 0.1) !important;
    }
    .la-cb-option-button:hover {
        cursor: pointer;
        background-color: rgba(0,0,0, 0.2) !important;
    }

    .la-cb-option-button i.icon {
        color: #004678 !important;
    }
    .la-cb-actions {
        padding-right: 0 !important;
    }
    .la-cb-info, .la-cb-options, .la-cb-actions {
        text-align: right;
        padding-top: 5px !important;
        padding-bottom: 5px !important;
    }
    .la-cb-info > .item, .la-cb-options > .item, .la-cb-actions > .item {
        display: inline-block;
    }

    main.ui.container.main {
        margin-top: 115px !important;
    }
    </style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.la-cb-wrapper .la-cb-actions').append($('nav.buttons'));
</laser:script>

</g:else>