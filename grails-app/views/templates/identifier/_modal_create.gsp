<%@ page import="de.laser.Org; de.laser.IdentifierNamespace" %>
<semui:modal id="modalCreateIdentifier" formID="identifier"
             text="${identifier? message(code:'default.identifier.edit') : message(code:'default.identifier.create')}"
             isEditModal="true"
             msgSave="${identifier ? message(code:'default.button.save.label') : message(code:'default.button.create.label')}">

    <g:form id="identifier" class="ui form"  url="[controller:'organisation', action:identifier? 'processEditIdentifier' : 'processCreateIdentifier', id:identifier?.id]" method="post">

        <g:hiddenField id="org_id_${orgInstance?.id}" name="orgid" value="${orgInstance?.id}"/>
        <g:if test="${identifier}">
            <g:hiddenField id="identifier_id_${identifier.id}" name="identifierId" value="${identifier.id}"/>
        </g:if>

        <div class="field">
            <label for="namespace">${message(code: 'identifier.namespace.label')}:</label>
            <g:if test="${identifier}">
                <input type="text" id="namespace" name="ns.id" value="${identifier.ns.getI10n('name') ?: identifier.ns.ns}" disabled/>
            </g:if>
            <g:else>
                <g:select id="namespace" name="ns.id"
                          from="${nsList}"
                          optionKey="id"
                          required=""
                          optionValue="${{ it.getI10n('name') ?: it.ns }}"
                          class="ui search dropdown"/>
            </g:else>
       </div>

        <div class="field ${identifier && identifier.ns.ns == IdentifierNamespace.LEIT_ID ? 'required' : ''}">
            <label for="value">${message(code: 'default.identifier.label')}:</label>

            <g:if test="${identifier && identifier.ns.ns == IdentifierNamespace.LEIT_ID}">

                <g:set var="leitID" value="${identifier.getLeitID()}"/>
                <div class="ui right labeled input">
                    <input type="text" name="leitID1" value="${leitID.leitID1}" placeholder="${message(code: 'identifier.leitID.leitID1.info')} (${message(code: 'default.mandatory.tooltip')})" minlength="2" maxlength="12" pattern="[0-9]{2,12}" required>
                    <div class="ui basic label">-</div>
                    <input type="text" name="leitID2" value="${leitID.leitID2}" placeholder="${message(code: 'identifier.leitID.leitID2.info')}" minlength="0" maxlength="30" pattern="[a-zA-Z0-9]{0,30}">
                    <div class="ui basic label">-</div>
                    <input type="text" name="leitID3" value="${leitID.leitID3}" placeholder="${message(code: 'identifier.leitID.leitID3.info')} (${message(code: 'default.mandatory.tooltip')})" minlength="2" maxlength="2" pattern="[0-9]{2,2}" required>
                </div>
            </g:if>
            <g:else>
                <input type="text" id="value" name="value" value="${identifier?.value == IdentifierNamespace.UNKNOWN ? '' : identifier?.value}" required/>
            </g:else>
        </div>

        <div class="field">
            <label for="note">${message(code: 'default.notes.label')}:</label>

            <input type="text" id="note" name="note" value="${identifier?.note}"/>
        </div>

    </g:form>
</semui:modal>