<%@ page import="de.laser.finance.CostInformationDefinition; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.properties.PropertyDefinition;de.laser.*"%>
<laser:htmlStart message="menu.institutions.prop_groups" />

        <ui:breadcrumbs>
            <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
            <ui:crumb message="menu.institutions.manage_props" class="active"/>
        </ui:breadcrumbs>

        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:link class="item" action="managePropertyGroups" params="[cmd: 'exportXLS']">${message(code: 'default.button.export.xls')}</g:link>
                </ui:exportDropdownItem>
            </ui:exportDropdown>
            <laser:render template="actions"/>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.institutions.manage_props" type="${contextService.getOrg().getCustomerType()}" />

        <laser:render template="nav" />

        <ui:messages data="${flash}" />

    <div class="ui styled fluid accordion">
        <g:each in="${propDefGroups}" var="typeEntry">
            <div class="title ${params.ownerType == typeEntry.key ? 'active' : ''}">
                <i class="dropdown icon"></i>
                <g:message code="propertyDefinition.${typeEntry.key}.label"/> (${typeEntry.value.size()})
            </div>
            <div class="content ${params.ownerType == typeEntry.key ? 'active' : ''}">
                <table class="ui sortable table la-js-responsive-table la-table compact">
                    <thead>
                        <tr>
                            <th><g:message code="default.order.label"/></th>
                            <th><g:message code="default.name.label"/></th>
                            <th><g:message code="propertyDefinitionGroup.table.header.description"/></th>
                            <th><g:message code="propertyDefinitionGroup.table.header.properties"/></th>
                            <%-- removed as of ERMS-6520 <th><g:message code="propertyDefinitionGroup.table.header.presetShow"/></th>--%>
                            <g:if test="${editable}">
                                <th class="center aligned">
                                    <ui:optionsIcon />
                                </th>
                            </g:if>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${typeEntry.value}" var="pdGroup" status="i">
                        <g:set var="pdgOID" value="${genericOIDService.getOID(pdGroup)}" />
                        <tr>
                            <td>
                                <g:if test="${i == 1 && propDefGroups.size() == 2}">%{-- override layout --}%
                                    <div class="${Btn.ICON.SIMPLE} compact la-hidden"><icon:placeholder /></div>
                                    <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'moveUp', oid:pdgOID, ownerType: typeEntry.key]}" class="${Btn.MODERN.SIMPLE} compact"
                                            role="button">
                                        <i class="${Icon.CMD.MOVE_UP}"></i>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${typeEntry.value.size() > 1}">
                                    <g:if test="${i > 0}">
                                        <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'moveUp', oid:pdgOID, ownerType: typeEntry.key]}" class="${Btn.MODERN.SIMPLE} compact"
                                                role="button">
                                            <i class="${Icon.CMD.MOVE_UP}"></i>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <div class="${Btn.ICON.SIMPLE} compact la-hidden"><icon:placeholder /></div>
                                    </g:else>
                                    <g:if test="${i < typeEntry.value.size()-1}">
                                        <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'moveDown', oid:pdgOID, ownerType: typeEntry.key]}" class="${Btn.MODERN.SIMPLE} compact"
                                                role="button">
                                            <i class="${Icon.CMD.MOVE_DOWN}"></i>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <div class="${Btn.ICON.SIMPLE} compact la-hidden"><icon:placeholder /></div>
                                    </g:else>
                                </g:elseif>
                            </td>
                            <td>
                                <ui:xEditable owner="${pdGroup}" field="name" />
                            </td>
                            <td>
                                <ui:xEditable owner="${pdGroup}" field="description" />
                            </td>
                            <td>
                                ${pdGroup.getPropertyDefinitions().size()}
                            </td>
                            <%-- removed as of ERMS-6520 <td>
                                <ui:xEditableBoolean owner="${pdGroup}" field="isVisible" />
                            </td>--%>
                            <g:if test="${editable}">
                                <td class="x">
                                    <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'edit', oid:pdgOID]}" class="${Btn.MODERN.SIMPLE} trigger-modal"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                        <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                    </g:link>
                                    <g:link controller="myInstitution"
                                            action="managePropertyGroups"
                                            params="${[cmd:'delete', oid:pdgOID]}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.prop_groups", args: [fieldValue(bean: pdGroup, field: "name")])}"
                                            data-confirm-term-how="delete"
                                            class="${Btn.MODERN.NEGATIVE_CONFIRM}"
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
        </g:each>
        <div class="title ${params.ownerType == CostInformationDefinition.COST_INFORMATION ? 'active' : ''}">
            <i class="dropdown icon"></i>
            <g:message code="costInformationDefinition.label"/>
        </div>
        <div class="content ${params.ownerType == CostInformationDefinition.COST_INFORMATION ? 'active' : ''}">
            <table class="ui sortable table la-js-responsive-table la-table compact">
                <thead>
                    <tr>
                        <th><g:message code="propertyDefinitionGroup.table.header.properties"/></th>
                        <g:if test="${editable}">
                            <th class="center aligned">
                                <ui:optionsIcon />
                            </th>
                        </g:if>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>
                            ${costInformationDefinitionsInUse.size()}
                        </td>
                        <g:if test="${editable}">
                            <td class="x">
                                <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'edit', costInformationsInUse: true]}" class="${Btn.MODERN.SIMPLE} trigger-modal"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                </g:link>
                            </td>
                        </g:if>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.trigger-modal').on('click', function(e) {
            e.preventDefault();

            $.ajax({
                url: $(this).attr('href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #propDefGroupModal').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                   onShow: function () {
                        r2d2.initDynamicUiStuff('#propDefGroupModal');
                        r2d2.initDynamicXEditableStuff('#propDefGroupModal');
                        $("html").css("cursor", "auto");
                        var prop_descr_selector_controller = {
                            init: function () {
                                if($('#propDefGroupModal #prop_descr_selector').length > 0) {
                                    $('#propDefGroupModal #prop_descr_selector').on('change', function () {
                                        prop_descr_selector_controller.changeTable($(this).val())
                                    })

                                    $('#propDefGroupModal #prop_descr_selector').trigger('change')
                                }
                                else prop_descr_selector_controller.changeTable('ci');
                            },
                            changeTable: function (target) {
                                $('#propDefGroupModal .table, #propDefGroupModal .propDefFilter').addClass('hidden')
                                $('#propDefGroupModal .table input').attr('disabled', 'disabled')

                                $('#propDefGroupModal .table[data-propDefTable="' + target + '"], #propDefGroupModal .propDefFilter[data-propDefTable="' + target + '"]').removeClass('hidden')
                                $('#propDefGroupModal .table[data-propDefTable="' + target + '"] input').removeAttr('disabled')

                                $("#propDefGroupModal .propDefFilter").on('input', function() {
                                    let table = $(this).attr('data-forTable');
                                    $("#"+table+" td.pdName:containsInsensitive_laser('"+$(this).val()+"')").parent("tr").show();
                                    $("#"+table+" td.pdName:not(:containsInsensitive_laser('"+$(this).val()+"'))").parent("tr").hide();
                                });

                                //own selector for case-insensitive :contains
                                jQuery.expr[':'].containsInsensitive_laser = function(a, i, m) {
                                    return jQuery(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
                                };
                            }
                        }
                        prop_descr_selector_controller.init();
                    },
                    detachable: true,
                    autofocus: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        })
    </laser:script>

<laser:htmlEnd />
