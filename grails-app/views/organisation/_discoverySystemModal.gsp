<%@ page import="de.laser.ui.Btn; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<g:if test="${editable}">
    <g:if test="${config == 'discoverySystemFrontend'}">
        <ui:modal id="frontend" message="org.discoverySystems.frontend.add">
            <g:form class="ui form" url="[controller: 'organisation', action: 'addDiscoverySystem',id:org.id]" method="post">
                <div class="field">
                    <label for="frontendSelection"><g:message code="org.discoverySystems.frontend.label"/>:</label>

                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.DISCOVERY_SYSTEM_FRONTEND)}"
                              class="ui dropdown clearable  fluid"
                              id="frontendSelection"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="frontend"
                              value=""/>
                </div>
            </g:form>
        </ui:modal>
    </g:if>
    <g:elseif test="${config == 'discoverySystemIndex'}">
        <ui:modal id="index" message="org.discoverySystems.index.add">
            <g:form class="ui form" url="[controller: 'organisation', action: 'addDiscoverySystem', id:org.id]" method="post">
                <div class="field">
                    <label for="indexSelection"><g:message code="org.discoverySystems.index.label"/>:</label>

                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.DISCOVERY_SYSTEM_INDEX)}"
                              class="ui dropdown clearable  fluid"
                              id="indexSelection"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="index"
                              value=""/>
                </div>
            </g:form>
        </ui:modal>
    </g:elseif>
</g:if>