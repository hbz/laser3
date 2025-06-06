<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue" %>
<laser:htmlStart message="menu.institutions.costConfiguration" />

<ui:breadcrumbs>
    <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
    <ui:crumb message="menu.institutions.costConfiguration" class="active" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.institutions.costConfiguration" type="finance" />

<g:if test="${editable}">
    <ui:controlButtons>
        <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="costConfiguration" action="createNewConfiguration" class="ui trigger-modal" message="costItemElementConfiguration.create_new.label"/>
        </ui:actionsDropdown>
    </ui:controlButtons>
</g:if>

        <ui:messages data="${flash}"/>

        <ui:msg class="warning" showIcon="true" message="costConfiguration.preset" hideClose="true" />

        <div class="ui styled fluid">
            <table class="ui celled la-js-responsive-table la-table compact table">
                <thead>
                    <tr>
                        <th><g:message code="financials.costItemElement"/></th>
                        <th><g:message code="financials.costItemConfiguration"/></th>
                        <th><g:message code="costConfiguration.useForCostItems"/></th>
                        <g:if test="${editable}">
                            <th><g:message code="financials.setAll"/></th>
                            <th class="center aligned">
                                <ui:optionsIcon />
                            </th>
                        </g:if>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${costItemElementConfigurations}" var="ciec">
                        <tr>
                            <td>${ciec.costItemElement.getI10n('value')}</td>
                            <td>
                                <ui:xEditableRefData owner="${ciec}" field="elementSign" emptytext="${message(code:'financials.costItemConfiguration.notSet')}" config="${de.laser.storage.RDConstants.COST_CONFIGURATION}"/>
                            </td>
                            <td><ui:xEditableBoolean owner="${ciec}" field="useForCostPerUse"/></td>
                            <g:if test="${editable}">
                                <td>
                                    <g:link class="${Btn.SIMPLE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code:'confirmation.content.bulkCostConfiguration')}"
                                            data-confirm-term-how="ok"
                                            action="setAllCostItems" params="${[cie:ciec.costItemElement.id]}">
                                        ${message(code:'costConfiguration.configureAllCostItems')}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.costItemElementConfiguration", args: [ciec.costItemElement.getI10n("value")])}"
                                            data-confirm-term-how="delete"
                                            controller="costConfiguration" action="deleteCostConfiguration"
                                            params="${[ciec: ciec.id]}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
        <laser:script file="${this.getGroovyPageFileName()}">
            $('.trigger-modal').on('click', function(e) {
                e.preventDefault();

                $.ajax({
                    url: $(this).attr('href')
                }).done( function (data) {
                    $('.ui.dimmer.modals > #ciecModal').remove();
                    $('#dynamicModalContainer').empty().html(data);
                    $('#dynamicModalContainer .ui.modal').modal({
                        onVisible: function () {
                            r2d2.initDynamicUiStuff('#ciecModal');
                            r2d2.initDynamicXEditableStuff('#ciecModal');
                        },
                        detachable: true,
                        autofocus: false,
                        transition: 'scale',
                        onApprove : function() {
                            $(this).find('.ui.form').submit();
                            return false;
                        },
                    }).modal('show');
                })
            })
        </laser:script>

<laser:htmlEnd />