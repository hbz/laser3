<%@ page import="de.laser.helper.Icons; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.Platform; de.laser.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="subscription.details.linkPackage.heading" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.linkPackage.heading')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${subscription.name}" />
<br/>
<br/>

<h2 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'subscription.details.linkPackage.heading')}</h2>
<br/>
<br/>

<h3 class="ui left floated aligned icon header la-clear-before">${message(code: 'package.plural')}
<ui:totalNumber total="${recordsCount}"/>
</h3>


<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter" model="[tmplConfigShow: [
            ['q', 'pkgStatus'],
            ['provider', 'vendor', 'ddc', 'curatoryGroup'],
            ['curatoryGroupType', 'automaticUpdates']
    ]]"/>
</g:if>

<ui:messages data="${flash}"/>

<div class="ui icon message" id="durationAlert" style="display: none">
    <i class="notched circle loading icon"></i>

    <div class="content">
        <div class="header">
            <g:message code="globalDataSync.requestProcessing"/>
        </div>
        <g:message code="globalDataSync.requestProcessingInfo"/>

    </div>
</div>



<g:if test="${records}">
    <laser:render template="/templates/filter/packageGokbFilterTable"
                  model="[
                          tmplConfigShow: ['lineNumber', 'name', 'status', 'titleCount', 'provider', 'vendor', 'platform', 'curatoryGroup', 'automaticUpdates', 'lastUpdatedDisplay', 'linkPackage'],
                          pkgs: pkgs,
                          bulkProcessRunning: bulkProcessRunning
                  ]"
    />
    <ui:paginate action="linkPackage" controller="subscription" params="${params}"
                    max="${max}" total="${recordsCount}"/>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:else>
</g:else>




<div id="magicArea"></div>

<ui:modal id="linkPackageModal" message="myinst.currentSubscriptions.link_pkg"
             msgSave="${message(code: 'default.button.link.label')}">

    <g:form class="ui form" id="linkPackageForm" url="[controller: 'subscription', action: 'processLinkPackage', id: params.id]">
        <input type="hidden" name="addUUID" value=""/>
        <div class="field">
            <label for="pkgName">${message(code: 'package.label')}</label>
            <input type="text" id="pkgName" name="pkgName" value="" readonly/>
        </div>
        <div class="field">
            <label for="holdingSelection">${message(code: 'subscription.holdingSelection.label')} <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code: "subscription.holdingSelection.explanation")}"><i class="${Icons.HELP_TOOLTIP} icon"></i></span></label>
        </div>
        <div class="four fields">
            <g:if test="${subscription.instanceOf && auditService.getAuditConfig(subscription.instanceOf, 'holdingSelection')}">
                <div class="field">
                    ${subscription.holdingSelection.getI10n('value')}
                </div>
            </g:if>
            <g:else>
                <div class="field">
                    <ui:select class="ui dropdown search selection" id="holdingSelection" name="holdingSelection" from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}" optionKey="id" optionValue="value"/>
                </div>
                <g:if test="${institution.isCustomerType_Consortium() && !subscription.instanceOf}">
                    <div class="field">
                        <g:if test="${auditService.getAuditConfig(subscription, 'holdingSelection')}">
                            <button id="inheritHoldingSelection" data-content="${message(code: 'subscription.holdingSelection.inherited')}" class="ui icon green button la-modern-button la-audit-button la-popup-tooltip la-delay" data-inherited="true">
                                <i aria-hidden="true" class="icon thumbtack"></i>
                            </button>
                        </g:if>
                        <g:else>
                            <button id="inheritHoldingSelection" data-content="${message(code: 'subscription.holdingSelection.inherit')}" class="ui icon blue button la-modern-button la-audit-button la-popup-tooltip la-delay" data-inherited="false">
                                <i aria-hidden="true" class="icon la-thumbtack slash"></i>
                            </button>
                        </g:else>
                    </div>
                </g:if>
            </g:else>
            <div class="field">
                <div class="ui checkbox toggle">
                    <g:checkBox name="createEntitlements"/>
                    <label><g:message code="subscription.details.link.with_ents"/></label>
                </div>
            </div>
            <g:if test="${institution.isCustomerType_Consortium()}">
                <div class="field">
                    <div class="ui linkToChildren checkbox toggle">
                        <g:checkBox name="linkToChildren"/>
                        <label><i data-content="${message(code:'consortium.member.plural')}" data-position="top center" class="users icon la-popup-tooltip la-delay"></i> <g:message code="subscription.details.linkPackage.label"/></label>
                    </div>
                </div>
                <div class="field">
                    <div class="ui createEntitlementsForChildren checkbox toggle">
                        <g:checkBox name="createEntitlementsForChildren"/>
                        <label><i data-content="${message(code:'consortium.member.plural')}" data-position="top center" class="users icon la-popup-tooltip la-delay"></i> <g:message code="subscription.details.link.with_ents"/></label>
                    </div>
                </div>
            </g:if>
        </div>
    </g:form>

        <%--
        <div class="ui divided grid">
            <g:set var="colCount" value="${institution.isCustomerType_Consortium() ? 'eight' : 'sixteen'}"/>
            <div class="${colCount} wide column">
                <div class="grouped required fields">
                    <label for="With">${message(code: 'subscription.details.linkPackage.label')}</label>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input type="radio" name="addType" id="With" value="With" tabindex="0" class="hidden">
                            <label for="With">${message(code: 'subscription.details.link.with_ents')}</label>
                        </div>
                    </div>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input type="radio" name="addType" id="Without" value="Without" tabindex="0" class="hidden">
                            <label for="Without">${message(code: 'subscription.details.link.no_ents')}</label>
                        </div>
                    </div>
                </div>

                <br/>
                <br/>
            </div>
        </div>
        --%>

                <%--
                <div class="field">
                    <h5 class="ui dividing header">
                        <g:message code="subscription.packages.config.label" args="${[""]}"/>
                    </h5>

                    <table class="ui table compact la-table-height53px">
                        <tr>
                            <th class="control-label"><g:message code="subscription.packages.changeType.label"/></th>
                            <th class="control-label">
                                <g:message code="subscription.packages.setting.label"/>
                            </th>
                            <th class="control-label la-popup-tooltip la-delay"
                                data-content="${message(code: "subscription.packages.notification.label")}">
                                <i class="ui large icon bullhorn"></i>
                            </th>
                        </tr>
                        <g:set var="excludes"
                               value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                         PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                        <g:each in="${PendingChangeConfiguration.SETTING_KEYS-PendingChangeConfiguration.TITLE_REMOVED}" var="settingKey">
                            <tr>
                                <td class="control-label">
                                    <g:message code="subscription.packages.${settingKey}"/>
                                </td>
                                <td>
                                    <g:if test="${!(settingKey in excludes)}">
                                        <g:if test="${editable}">
                                            <ui:select class="ui dropdown"
                                                          name="${settingKey}!§!setting"
                                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)}"
                                                          optionKey="id" optionValue="value"
                                                          value="${RDStore.PENDING_CHANGE_CONFIG_PROMPT.id}"/>
                                        </g:if>
                                        <g:else>
                                            ${RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n("value")}
                                        </g:else>
                                    </g:if>
                                </td>
                                <td>
                                    <g:if test="${editable}">
                                        <g:checkBox class="ui checkbox" name="${settingKey}!§!notification"
                                                    checked="${false}"/>
                                    </g:if>
                                    <g:else>
                                        ${RDStore.YN_NO.getI10n("value")}
                                    </g:else>
                                </td>
                            </tr>
                        </g:each>
                    </table>
                </div>
                <div class="inline field">
                    <label for="freezeHolding"><g:message code="subscription.packages.freezeHolding"/> <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.packages.freezeHolding.expl')}"><i class="${Icons.HELP_TOOLTIP} icon"></i></span></label>
                    <g:checkBox class="ui checkbox" name="freezeHolding" checked="${false}"/>
                </div>
            </div>
            <g:if test="${institution.isCustomerType_Consortium()}">
                <div class="${colCount} wide column">
                    <div class="grouped fields">
                        <label for="WithForChildren">${message(code: 'subscription.details.linkPackage.children.label')}</label>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input type="radio" name="addTypeChildren" id="WithForChildren" value="WithForChildren" tabindex="0" class="hidden">
                                <label>${message(code: 'subscription.details.link.with_ents')}</label>
                            </div>
                        </div>

                        <div class="field">
                            <div class="ui radio checkbox">
                                <input type="radio" name="addTypeChildren" id="WithoutForChildren" value="WithoutForChildren" tabindex="0" class="hidden">
                                <label>${message(code: 'subscription.details.link.no_ents')}</label>
                            </div>
                        </div>
                    </div>

                    <br/>
                    <br/>
                    <div class="field">
                        <h5 class="ui dividing header">
                            <g:message code="subscription.packages.config.children.label" args="${[""]}"/>
                        </h5>

                        <table class="ui table compact la-table-height53px">
                            <tr>
                                <th class="control-label la-popup-tooltip la-delay" data-contet="${message(code: "subscription.packages.auditable")}">
                                    <i class="ui large icon thumbtack"></i>
                                </th>
                                <th class="control-label la-popup-tooltip la-delay" data-content="${message(code: "subscription.packages.notification.auditable")}">
                                    <i class="ui large icon bullhorn"></i>
                                </th>
                            </tr>
                            <g:set var="excludes"
                                   value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                             PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                            <g:each in="${PendingChangeConfiguration.SETTING_KEYS-PendingChangeConfiguration.TITLE_REMOVED}" var="settingKey">
                                <tr>
                                    <td>
                                        <g:if test="${!(settingKey in excludes)}">
                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!auditable"
                                                        checked="${false}"/>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${editable}">
                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!notificationAudit"
                                                        checked="${false}"/>
                                        </g:if>
                                        <g:else>
                                            ${RDStore.YN_NO.getI10n("value")}
                                        </g:else>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </div>
                    <div class="inline field">
                        <label for="freezeHoldingAudit"><g:message code="subscription.packages.freezeHolding"/> <span class="la-popup-tooltip la-delay" data-content="${message(code: 'subscription.packages.freezeHolding.expl')}"><i class="${Icons.HELP_TOOLTIP} icon"></i></span></label>
                        <g:checkBox class="ui checkbox" name="freezeHoldingAudit" checked="${false}"/>
                    </div>
                </div>
            </g:if>
        </div>

    </g:form>
    --%>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.callbacks.modal.onShow.linkPackageModal = function(trigger) {
            tooltip.init("#linkPackageModal");
            $('#linkPackageModal #pkgName').attr('value', $(trigger).attr('data-packageName'))
            $('#linkPackageModal input[name=addUUID]').attr('value', $(trigger).attr('data-addUUID'))
        }
    </laser:script>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
%{--    JSPC.app.unlinkPackage = function (pkg_id){
      var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage', params: [subscription: subscription.id])}&package="+pkg_id

        $.ajax({url: req_url,
          success: function(result){
             $("#unlinkPackageModal").remove();
             $('#magicArea').html(result);
          },
          complete: function(){
            $("#unlinkPackageModal").modal("show");
          }
        });
      }--}%
    JSPC.app.toggleAlert = function() {
      $('#durationAlert').toggle();
    }
    JSPC.app.disableChildEnt = function() {
        $(".checkbox.createEntitlementsForChildren").checkbox('uncheck').checkbox('set disabled');
    }

    $("#inheritHoldingSelection").click(function(e) {
        e.preventDefault();
        let isInherited = $(this).attr('data-inherited') === 'true';
        let button = $(this);
        let icon = $(this).find('i');
        $.ajax({
            url: '<g:createLink controller="ajax" action="toggleAudit" params="[owner: genericOIDService.getOID(subscription), property: 'holdingSelection', returnSuccessAsJSON: true]"/>'
        }).done(function(response) {
            button.toggleClass('blue').toggleClass('green');
            if(isInherited) {
                icon.addClass('la-thumbtack slash').removeClass('thumbtack');
                button.attr('data-inherited', 'false');
            }
            else {
                icon.removeClass('la-thumbtack slash').addClass('thumbtack');
                button.attr('data-inherited', 'true');
            }
        }).fail(function () {
            console.log("AJAX error! Please check logs!");
        });
    });

    $(".checkbox.linkToChildren").checkbox({
        onChecked: function() {
            $(".checkbox.createEntitlementsForChildren").checkbox('set enabled');
        },
        onUnchecked: function() {
            JSPC.app.disableChildEnt();
        }
    });

    JSPC.app.disableChildEnt();

      $(".packageLink").click(function(evt) {
          evt.preventDefault();

          var check = confirm('${message(code: 'subscription.details.link.with_ents.confirm')}');
            console.log(check)
            if (check == true) {
                JSPC.app.toggleAlert();
                window.open($(this).attr('href'), "_self");
            }
        });

        $(".packageLinkWithoutIE").click(function(evt) {
            evt.preventDefault();

            var check = confirm('${message(code: 'subscription.details.link.no_ents.confirm')}');
            console.log(check)
            if (check == true) {
                JSPC.app.toggleAlert();
                window.open($(this).attr('href'), "_self");
            }
        });
</laser:script>

<laser:htmlEnd />
