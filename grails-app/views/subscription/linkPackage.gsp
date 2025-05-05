<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.wekb.Platform; de.laser.wekb.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="subscription.details.linkPackage.heading" />

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


<g:if test="${error}">
    <ui:msg class="error" noClose="true" text="${error}"/>
</g:if>
<g:else>
    <laser:render template="/templates/filter/packageGokbFilter" model="[
            filterConfig: filterConfig,
            curatoryGroupTypes: curatoryGroupTypes,
            automaticUpdates: automaticUpdates,
    ]"/>
</g:else>

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
                          tmplConfigShow: tmplConfigShow,
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

<ui:modal id="linkPackageModal" message="myinst.currentSubscriptions.link_pkg"
             msgSave="${message(code: 'default.button.link.label')}">

    <g:form class="ui form" id="linkPackageForm" url="[controller: 'subscription', action: 'processLinkPackage', id: params.id]">
        <input type="hidden" name="addUUID" value=""/>
        <div class="field">
            <label for="pkgName">${message(code: 'package.label')}</label>
            <input type="text" id="pkgName" name="pkgName" value="" readonly/>
        </div>
        <div class="field">
            <label for="holdingSelection">${message(code: 'subscription.holdingSelection.label')} <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code: "subscription.holdingSelection.explanation")}"><i class="${Icon.TOOLTIP.HELP}"></i></span></label>
        </div>
        <div class="four fields">
            <g:if test="${subscription.instanceOf && auditService.getAuditConfig(subscription.instanceOf, 'holdingSelection')}">
                <div class="field">
                    ${subscription.holdingSelection.getI10n('value')}
                </div>
            </g:if>
            <g:else>
                <div class="field" id="holdingSelection_${subscription.id}">
                    <ui:select class="ui dropdown clearable search selection" name="holdingSelection" from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}"
                               optionKey="id" optionValue="value" value="${subscription.holdingSelection?.id}" noSelection="${['':message(code:'default.select.choose.label')]}"/>
                </div>
                <%--
                <g:if test="${institution.isCustomerType_Consortium() && !subscription.instanceOf}">
                    <div class="field">
                        <g:if test="${auditService.getAuditConfig(subscription, 'holdingSelection')}">
                            <g:if test="${subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}">
                                <button id="inheritHoldingSelection" data-content="${message(code: 'subscription.holdingSelection.inherited')}" class="${Btn.MODERN.POSITIVE_TOOLTIP} la-audit-button disabled" data-inherited="true">
                                    <i aria-hidden="true" class="${Icon.SIG.INHERITANCE}"></i>
                                </button>
                            </g:if>
                            <g:else>
                                <button id="inheritHoldingSelection" data-content="${message(code: 'subscription.holdingSelection.inherited')}" class="${Btn.MODERN.POSITIVE_TOOLTIP} la-audit-button" data-inherited="true">
                                    <i aria-hidden="true" class="${Icon.SIG.INHERITANCE}"></i>
                                </button>
                            </g:else>
                        </g:if>
                        <g:else>
                            <button id="inheritHoldingSelection" data-content="${message(code: 'subscription.holdingSelection.inherit')}" class="${Btn.MODERN.SIMPLE_TOOLTIP} la-audit-button" data-inherited="false">
                                <i aria-hidden="true" class="${Icon.SIG.INHERITANCE_OFF}"></i>
                            </button>
                        </g:else>
                    </div>
                </g:if>
                --%>
            </g:else>
            <div class="field">
                    <div class="ui createEntitlements checkbox toggle">
                        <g:checkBox name="createEntitlements" checked="${subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}" disabled="${subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}"/>
                        <label><g:message code="subscription.details.link.with_ents"/></label>
                    </div>
            </div>
            <g:if test="${institution.isCustomerType_Consortium()}">
                <div class="field">
                    <div class="ui linkToChildren checkbox toggle">
                        <g:checkBox name="linkToChildren" checked="${subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}" disabled="${subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}"/>
                        <label><i data-content="${message(code:'consortium.member.plural')}" data-position="top center" class="users icon la-popup-tooltip"></i> <g:message code="subscription.details.linkPackage.label"/></label>
                    </div>
                </div>
                <div class="field">
                    <div class="ui createEntitlementsForChildren checkbox toggle">
                        <g:checkBox name="createEntitlementsForChildren" disabled="${subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}"/>
                        <label><i data-content="${message(code:'consortium.member.plural')}" data-position="top center" class="users icon la-popup-tooltip"></i> <g:message code="subscription.details.link.with_ents"/></label>
                    </div>
                </div>
            </g:if>
        </div>
    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.callbacks.modal.onShow.linkPackageModal = function(trigger) {
            tooltip.init("#linkPackageModal");
            $('#linkPackageModal #pkgName').attr('value', $(trigger).attr('data-packageName'));
            $('#linkPackageModal input[name=addUUID]').attr('value', $(trigger).attr('data-addUUID'));
        }
        JSPC.app.disableChildEnt = function() {
            $(".checkbox.createEntitlementsForChildren").checkbox('uncheck').checkbox('set disabled');
        }

        $("#holdingSelection_${subscription.id} .ui.dropdown").dropdown({
            onChange: function(value, text, $selectedItem) {
                let rdvId = Number(value);
                let button = $("#inheritHoldingSelection");
                let icon = button.find('i');
                let holdingEntire = rdvId === ${RDStore.SUBSCRIPTION_HOLDING_ENTIRE.id};
                $.ajax({
                    url: '<g:createLink controller="ajax" action="switchPackageHoldingInheritance" />',
                    data: {
                        id: ${subscription.id},
                        value: rdvId
                    }
                }).done(function(response) {
                    if(holdingEntire) {
                        button.removeClass('blue').addClass('green').addClass('disabled');
                        icon.removeClass('la-thumbtack slash').addClass('thumbtack');
                        button.attr('data-inherited', 'true');
                        $(".checkbox.createEntitlements").checkbox('check').checkbox('set disabled');
                        $(".checkbox.linkToChildren").checkbox('check').checkbox('set disabled');
                        $(".checkbox.createEntitlementsForChildren").checkbox('uncheck').checkbox('set disabled');
                    }
                    else {
                        button.removeClass('green').addClass('blue').removeClass('disabled');
                        icon.addClass('la-thumbtack slash').removeClass('thumbtack');
                        button.attr('data-inherited', 'false');
                        $(".checkbox.createEntitlements").checkbox('uncheck').checkbox('set enabled');
                        $(".checkbox.linkToChildren").checkbox('uncheck').checkbox('set enabled');
                        $(".checkbox.createEntitlementsForChildren").checkbox('set enabled');
                    }
                }).fail(function () {
                    console.log("AJAX error! Please check logs!");
                });
            }
        });

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

        $(".checkbox.withOverwrite").checkbox({
            onChecked: function() {
                let fieldId = $(this).attr("id");
                $("#"+fieldId+"Overwrite").val('on');
                if(fieldId === 'linkToChildren') {
                    console.log("linkToChildren enabled!");
                    $(".checkbox.createEntitlementsForChildren").checkbox('set enabled');
                }
            },
            onUnchecked: function() {
                let fieldId = $(this).attr("id");
                $("#"+fieldId+"Overwrite").val('off');
                if(fieldId === 'linkToChildren') {
                    console.log("linkToChildren disabled!");
                    JSPC.app.disableChildEnt();
                }
            }
        });

        JSPC.app.disableChildEnt();
    </laser:script>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.toggleAlert = function() {
      $('#durationAlert').toggle();
    }
</laser:script>

<laser:htmlEnd />
