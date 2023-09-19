<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore; de.laser.UserSetting; de.laser.auth.User; de.laser.auth.Role; de.laser.Org" %>
<laser:serviceInjection />

<g:set var="visibilityContextOrgMenu" value="la-show-context-orgMenu" />

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
