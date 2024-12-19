<%@ page import="de.laser.ui.Btn" %>
<laser:serviceInjection/>
<g:if test="${editable}">
    <a class="${Btn.SIMPLE}" data-ui="modal" href="#${modalId}">
        <g:message code="${buttonText}"/>
    </a>

    <ui:modal id="${modalId}" message="${buttonText}">
        <g:form class="ui form" url="[controller: controllerName, action: addAction]" method="post">
            <div class="field">
                <g:hiddenField name="id" id="${modalId}_id" value="${ownObj.id}"/>
                <g:hiddenField name="field" id="${modalId}_field" value="${field}"/>
                <label for="attributeSelection_${modalId}"><g:message code="${label}"/>:</label>

                <g:select from="${availableAttributes}"
                          class="ui dropdown fluid"
                          id="attributeSelection_${modalId}"
                          optionKey="${{ genericOIDService.getOID(it) }}"
                          optionValue="${{ it instanceof de.laser.RefdataValue ? it.getI10n('value') : it.name }}"
                          name="${field}"
                          value=""/>
            </div>
        </g:form>
    </ui:modal>
</g:if>
