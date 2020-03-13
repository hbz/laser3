<%@ page import="com.k_int.kbplus.IdentifierNamespace" %>
<semui:modal id="modalCreateIdentifier"
             text="${identifier? message(code:'default.identifier.edit') : message(code:'default.identifier.create')}"
             isEditModal="true"
             msgSave="${identifier ? message(code:'default.button.save.label') : message(code:'default.button.create.label')}">

    <g:form id="identifier" class="ui form"  url="[controller:'organisation', action:identifier? 'processEditIdentifier' : 'processCreateIdentifier', id:identifier?.id]" method="post">

        <g:hiddenField name="orgid" value="${orgInstance?.id}"/>
        <g:if test="${identifier}">
            <g:hiddenField name="identifierId" value="${identifier?.id}"/>
        </g:if>

        <div class="field fieldcontain">
            <label for="namespace">${message(code: 'identifier.namespace.label')}:</label>

            <g:select id="namespace" name="ns.id"
                      from="${com.k_int.kbplus.IdentifierNamespace.list(sort: 'ns')}"
                      optionKey="id"
                      required=""
                      optionValue="ns"
                      value="${identifier?.ns?.id}"
                      class="ui search dropdown"/>
        </div>

        <div class="field fieldcontain">
            <label for="value">${message(code: 'default.identifier.label')}:</label>

            <input type="text" id="value" name="value" value="${identifier?.value}"/>
        </div>

        <div class="field fieldcontain">
            <label for="note">${message(code: 'default.notes.label')}:</label>

            <input type="text" id="note" name="note" value="${identifier?.note}"/>
        </div>

    </g:form>
</semui:modal>