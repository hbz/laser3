<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.Org; com.k_int.kbplus.Subscription; com.k_int.properties.*" %>
<laser:serviceInjection />

<!-- _propertyGroupBindings -->

    <div id="propDefGroupBindingConfig">

        <table class="ui la-table-small la-table-inCard table">
            <thead>
                <tr>
                    <th>Merkmalsgruppe</th>
                    <th></th>
                    <th>Voreinstellung</th>
                    <th>Anzeigen</th>
                    <th></th>
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
                            ${propDefGroup.visible ? propDefGroup.visible.getI10n('value') : 'Nein'}
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
                                <semui:xEditableRefData owner="${binding}" field="visible" config="YN" />
                            </g:if>
                        </td>
                        <td class="x">
                            <g:if test="${editable}">
                                <g:if test="${! binding}">
                                    <g:if test="${propDefGroup.visible?.value=='Yes'}">
                                        <g:remoteLink controller="ajax" action="addCustomPropertyGroupBinding"
                                                      params='[propDefGroup: "${propDefGroup.class.name}:${propDefGroup.id}",
                                                               ownobj:"${ownobj.class.name}:${ownobj.id}",
                                                               visible:"No",
                                                               editable:"${editable}"]'
                                                      onComplete="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#propDefGroupBindingConfig')"
                                                      update="propDefGroupBindingConfig"
                                                      class="ui icon button">
                                            Nicht anzeigen
                                        </g:remoteLink>
                                    </g:if>
                                    <g:else>
                                        <g:remoteLink controller="ajax" action="addCustomPropertyGroupBinding"
                                                      params='[propDefGroup: "${propDefGroup.class.name}:${propDefGroup.id}",
                                                               ownobj:"${ownobj.class.name}:${ownobj.id}",
                                                               visible:"Yes",
                                                               editable:"${editable}"]'
                                                      onComplete="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#propDefGroupBindingConfig')"
                                                      update="propDefGroupBindingConfig"
                                                      class="ui icon button">
                                            Anzeigen
                                        </g:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:remoteLink controller="ajax" action="deleteCustomPropertyGroupBinding"
                                                  params='[propDefGroupBinding: "${binding.class.name}:${binding.id}",
                                                           propDefGroup: "${propDefGroup.class.name}:${propDefGroup.id}",
                                                           ownobj:"${ownobj.class.name}:${ownobj.id}",
                                                           editable:"${editable}"]'
                                                  onComplete="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#propDefGroupBindingConfig')"
                                                  update="propDefGroupBindingConfig"
                                                  class="ui icon negative button">
                                        <i class="icon times"></i>
                                    </g:remoteLink>
                                </g:else>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div><!-- #propDefGroupBindingConfig -->

<script>
    $(function(){
        $('#propDefGroupBindings .actions .button.propDefGroupBindings').on('click', function(e){
            e.preventDefault()
            window.location.reload()
        })
    })
</script>
<!-- _propertyGroupBindings -->