<!-- A: templates/properties/_groupBindings -->
<%@ page import="de.laser.License; de.laser.Org; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.Subscription" %>
<laser:serviceInjection />

    <div id="propDefGroupBindingConfig">

        <table class="ui compact la-js-responsive-table la-table-inCard table">
            <thead>
                <tr>
                    <th class="la-js-dont-hide-this-card">Merkmalsgruppe</th>
                    <th></th>
                    <th>Voreinstellung</th>
                    <th>Für dieses Objekt überschreiben</th>
                    <g:if test="${showConsortiaFunctions == true}">
                        <th>Für Teilnehmer anzeigen</th>
                    </g:if>
                    <th class="la-action-info">${message(code:'default.actions.label')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${availPropDefGroups}" var="propDefGroup">
                    <tr>
                        <td>
                            <strong>${propDefGroup.name}</strong>

                            <g:if test="${propDefGroup.description}">
                                <p>${propDefGroup.description}</p>
                            </g:if>
                        </td>
                        <td>
                            ${propDefGroup.tenant ? '' : ' (global)'}
                        </td>
                        <td>
                            ${propDefGroup.isVisible ? message(code:'refdata.Yes') : message(code:'refdata.No')}
                        </td>
                        <td>
                            <%
                                def binding

                                switch (ownobj.class.name) {
                                    case License.class.name:
                                        binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndLic(propDefGroup, ownobj)
                                        break
                                    case Org.class.name:
                                        binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(propDefGroup, ownobj)
                                        break
                                    case Subscription.class.name:
                                        binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndSub(propDefGroup, ownobj)
                                        break
                                }
                            %>
                            <g:if test="${editable && binding}">
                                <ui:xEditableBoolean owner="${binding}" field="isVisible" />
                            </g:if>
                        </td>
                        <g:if test="${showConsortiaFunctions == true}">
                            <td>
                                <g:if test="${editable && binding}">
                                    <ui:xEditableBoolean owner="${binding}" field="isVisibleForConsortiaMembers" />
                                </g:if>
                            </td>
                        </g:if>
                        <td class="center aligned la-js-editmode-container">
                            <g:if test="${editable}">
                                <g:if test="${! binding}">
                                    <g:if test="${propDefGroup.isVisible}">
                                        <ui:remoteLink controller="ajax" action="addCustomPropertyGroupBinding"
                                                      params='[propDefGroup: "${propDefGroup.class.name}:${propDefGroup.id}",
                                                               ownobj:"${ownobj.class.name}:${ownobj.id}",
                                                               isVisible:"No",
                                                               editable:"${editable}",
                                                               showConsortiaFunctions:"${showConsortiaFunctions}"
                                                                ]'
                                                      onComplete="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#propDefGroupBindingConfig')"
                                                      data-update="propDefGroupBindingConfig"
                                                      class="ui icon button">
                                            Nicht anzeigen
                                        </ui:remoteLink>
                                    </g:if>
                                    <g:else>
                                        <ui:remoteLink controller="ajax" action="addCustomPropertyGroupBinding"
                                                      params='[propDefGroup: "${propDefGroup.class.name}:${propDefGroup.id}",
                                                               ownobj:"${ownobj.class.name}:${ownobj.id}",
                                                               isVisible:"Yes",
                                                               editable:"${editable}",
                                                               showConsortiaFunctions:"${showConsortiaFunctions}"
                                                               ]'
                                                      onComplete="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#propDefGroupBindingConfig')"
                                                      data-update="propDefGroupBindingConfig"
                                                      class="ui icon button">
                                            Anzeigen
                                        </ui:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <ui:remoteLink controller="ajax" action="deleteCustomPropertyGroupBinding"
                                                  params='[propDefGroupBinding: "${binding.class.name}:${binding.id}",
                                                           propDefGroup: "${propDefGroup.class.name}:${propDefGroup.id}",
                                                           ownobj:"${ownobj.class.name}:${ownobj.id}",
                                                           editable:"${editable}",
                                                           showConsortiaFunctions:"${showConsortiaFunctions}"
                                                  ]'
                                                  onComplete="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#propDefGroupBindingConfig')"
                                                  data-update="propDefGroupBindingConfig"
                                                  class="ui icon negative button la-modern-button"
                                                      role="button"
                                                      ariaLabel="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="icon times"></i>
                                    </ui:remoteLink>
                                </g:else>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div><!-- #propDefGroupBindingConfig -->

<laser:script file="${this.getGroovyPageFileName()}">
        $('#propDefGroupBindings .actions .button.propDefGroupBindings').on('click', function(e){
            e.preventDefault()
            window.location.reload()
        })
</laser:script>
<!-- O: templates/properties/_groupBindings -->