<%@ page import="com.k_int.properties.PropertyDefinition"%>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.manage_prop_groups')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.institutions.manage_prop_groups" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${institution.name}</h1>

        <semui:messages data="${flash}" />

        <g:if test="${editable}">
            <div class="content ui form">
                <div class="fields">
                    <div class="field">
                        <button class="ui button" value="" href="#addPropDefGroupModal" data-semui="modal">${message(code:'propertyDefinitionGroup.create_new.label')}</button>
                    </div>
                </div>
            </div>
        </g:if>

    <table class="ui celled sortable table la-table la-table-small">
        <thead>
            <tr>
                <th>Name</th>
                <th>Beschreibung</th>
                <th>Merkmale</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${propDefGroups}" var="pdGroup">
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
                    <td class="x">
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>


    <semui:modal id="addPropDefGroupModal" message="propertyDefinitionGroup.create_new.label">

        <g:form class="ui form" url="[controller: 'myInstitution', action: 'managePropertyGroups']" method="POST">
            <input type="hidden" name="cmd" value="newPropertyGroup"/>

            <div class="ui two column grid">
                <div class="column">

                    <div class="field">
                        <label>Kategorie</label>
                        <select name="prop_descr" id="prop_descr_selector" class="ui dropdown">
                            <g:each in="${PropertyDefinition.AVAILABLE_GROUPS_DESCR}" var="pdDescr">
                                <option value="${pdDescr}"><g:message code="propertyDefinition.${pdDescr}.label" default="${pdDescr}"/></option>
                            </g:each>
                        </select>
                    </div>

                    <div class="field">
                        <label>Name</label>
                        <input type="text" name="name"/>
                    </div>

                    <div class="field">
                        <label>Beschreibung</label>
                        <textarea name="description"></textarea>
                    </div>
                </div>

                <div class="column">
                    <div class="field">
                        <label>Merkmale</label>

                        <g:each in="${PropertyDefinition.AVAILABLE_GROUPS_DESCR}" var="pdDescr">
                            <table class="ui table la-table-small hidden" data-propDefTable="${pdDescr}">
                                <tbody>
                                    <g:each in="${PropertyDefinition.findAllWhere(tenant:null, descr:pdDescr, [sort: 'name'])}" var="pd">
                                        <tr>
                                            <td>
                                                ${pd.getI10n('name')}
                                            </td>
                                            <td>
                                                <input type="checkbox" name="propertyDefinition" value="${pd.id}" />
                                            </td>
                                        </tr>
                                    </g:each>
                                </tbody>
                            </table>
                        </g:each>
                    </div>
                </div>
            </div>
            <script>
                var prop_descr_selector_controller = {
                    init: function() {
                        $('#prop_descr_selector').on('change', function(){
                            prop_descr_selector_controller.changeTable($(this).val())
                        })

                        $('#prop_descr_selector').trigger('change')
                    },
                    changeTable: function(target) {
                        $('#addPropDefGroupModal .table').addClass('hidden')
                        $('#addPropDefGroupModal .table[data-propDefTable="' + target + '"]').removeClass('hidden')
                    }
                }
                prop_descr_selector_controller.init()
            </script>
        </g:form>
    </semui:modal>

  </body>
</html>
