<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:serviceInjection/>
<g:if test="${editable}">
    <ui:modal id="${modalId}" message="${buttonText}">
        <g:form class="ui form" url="[controller: controllerName, action: addAction]" method="post">
            <div class="field">
                <g:hiddenField name="id" id="${modalId}_id" value="${ownObj.id}"/>
                <g:hiddenField name="field" id="${modalId}_field" value="${field}"/>
                <label for="attributeSelection_${modalId}"><g:message code="${label}"/>:</label>

                <g:select from="${availableAttributes}"
                          class="ui dropdown clearable fluid"
                          id="attributeSelection_${modalId}"
                          optionKey="${{ genericOIDService.getOID(it) }}"
                          optionValue="${{ it instanceof de.laser.RefdataValue ? it.getI10n('value') : it.name }}"
                          name="${field}"
                          value=""/>
            </div>
        </g:form>
    </ui:modal>
</g:if>
