<%@ page import="de.laser.properties.PropertyDefinition;de.laser.*"%>
<laser:serviceInjection />
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'menu.institutions.prop_groups')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.manage_props" class="active"/>
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="managePropertyGroups" params="[cmd: 'exportXLS']">${message(code: 'default.button.export.xls')}</g:link>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
            <g:render template="actions"/>
        </semui:controlButtons>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.institutions.manage_props')}</h1>

        <g:render template="nav" />

        <semui:messages data="${flash}" />

    <div class="ui styled fluid accordion">
        <g:each in="${propDefGroups}" var="typeEntry">
            <div class="title">
                <i class="dropdown icon"></i>
                <g:message code="propertyDefinition.${typeEntry.key}.label"/>
            </div>
            <div class="content">
                <table class="ui celled sortable table la-js-responsive-table la-table compact">
                    <thead>
                    <tr>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="propertyDefinitionGroup.table.header.description"/></th>
                        <th><g:message code="propertyDefinitionGroup.table.header.properties"/></th>
                        <th><g:message code="propertyDefinitionGroup.table.header.presetShow"/></th>
                        <g:if test="${editable}">
                            <th class="la-action-info">${message(code:'default.actions.label')}</th>
                        </g:if>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${typeEntry.value}" var="pdGroup">
                        <tr>
                            <td>
                                <semui:xEditable owner="${pdGroup}" field="name" />
                            </td>
                            <td>
                                <semui:xEditable owner="${pdGroup}" field="description" />
                            </td>
                            <td>
                                ${pdGroup.getPropertyDefinitions().size()}
                            </td>
                            <td>
                                <semui:xEditableBoolean owner="${pdGroup}" field="isVisible" />
                            </td>
                            <g:if test="${editable}">
                                <td class="x">
                                    <g:set var="pdgOID" value="${genericOIDService.getOID(pdGroup)}" />
                                    <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'edit', oid:pdgOID]}" class="ui icon button blue la-modern-button trigger-modal"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                        <i aria-hidden="true" class="write icon"></i>
                                    </g:link>
                                    <g:link controller="myInstitution"
                                            action="managePropertyGroups"
                                            params="${[cmd:'delete', oid:pdgOID]}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.prop_groups", args: [fieldValue(bean: pdGroup, field: "name")])}"
                                            data-confirm-term-how="delete"
                                            class="ui icon negative button la-modern-button js-open-confirm-modal"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="trash alternate outline icon"></i>
                                    </g:link>
                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:each>
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
                        r2d2.initDynamicSemuiStuff('#propDefGroupModal');
                        r2d2.initDynamicXEditableStuff('#propDefGroupModal');
                        $("html").css("cursor", "auto");
                        var prop_descr_selector_controller = {
                            init: function () {
                                $('#propDefGroupModal #prop_descr_selector').on('change', function () {
                                    prop_descr_selector_controller.changeTable($(this).val())
                                })

                                $('#propDefGroupModal #prop_descr_selector').trigger('change')
                            },
                            changeTable: function (target) {
                                $('#propDefGroupModal .table').addClass('hidden')
                                $('#propDefGroupModal .table input').attr('disabled', 'disabled')

                                $('#propDefGroupModal .table[data-propDefTable="' + target + '"]').removeClass('hidden')
                                $('#propDefGroupModal .table[data-propDefTable="' + target + '"] input').removeAttr('disabled')
                            }
                        }
                        prop_descr_selector_controller.init();
                    },
                    detachable: true,
                    autofocus: false,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        })
    </laser:script>

  </body>
</html>
