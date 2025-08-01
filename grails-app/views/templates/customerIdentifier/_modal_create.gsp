<%@ page import="de.laser.wekb.Platform; de.laser.CustomerIdentifier; de.laser.IdentifierNamespace" %>
<ui:modal id="modalCreateCustomerIdentifier"
             text="${customeridentifier? message(code:'org.customerIdentifier.create.edit') : message(code:'org.customerIdentifier.create.new')}"
             isEditModal="true"
             msgSave="${customeridentifier ? message(code:'default.button.save.label') : message(code:'default.button.create.label')}">

    <g:form id="customeridentifier" class="ui form"  url="[controller:'organisation', action:customeridentifier? 'processEditCustomerIdentifier' : 'processCreateCustomerIdentifier', id:customeridentifier?.id]" method="post">

        <g:hiddenField id="org_id_${orgInstance?.id}" name="orgid" value="${orgInstance?.id}"/>
        <g:if test="${customeridentifier}">
            <g:hiddenField id="customeridentifier_id_${customeridentifier.id}" name="customeridentifier" value="${customeridentifier.id}"/>
        </g:if>

        <div class="field">
            <label for="addCIPlatform">${message(code:'provider.label')} : ${message(code:'platform.label')}</label>
            <g:if test="${customeridentifier}">
                <% Platform p = customeridentifier.platform%>
                <input type="text" id="addCIPlatform" name="addCIPlatform" value="${ p.provider.name + (p.provider.abbreviatedName ? " (${p.provider.abbreviatedName})" : '') + ' : ' + p.name}" disabled/>
            </g:if>
            <g:else>
                <g:select id="addCIPlatform" name="addCIPlatform"
                          from="${allPlatforms}"
                          required=""
                          class="ui search dropdown"
                          optionKey="id"
                          optionValue="${{ it.provider.name + (it.provider.abbreviatedName ? " (${it.provider.abbreviatedName})" : '') + ' : ' + it.name}}"
                />
            </g:else>
        </div>

        <div class="field">
            <label for="value">${message(code: 'org.customerIdentifier')}:</label>

            <input type="text" id="value" name="value" value="${customeridentifier?.value}"/>
        </div>

        <div class="field">
            <label for="requestorKey">${message(code: 'org.requestorKey')}:</label>

            <input type="text" id="requestorKey" name="requestorKey" value="${customeridentifier?.requestorKey}"/>
        </div>

        <div class="field">
            <label for="note">${message(code: 'default.notes.label')}:</label>

            <input type="text" id="note" name="note" value="${customeridentifier?.note}"/>
        </div>

    </g:form>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#customeridentifier').form({
        on: 'blur',
        inline: true,
        fields: {
            value: {
                identifier: 'value',
                rules: [
                    {
                        type: 'empty',
                        prompt: '${message(code: 'validation.needsToBeFilledOut')}'
                    }
                ]
            }
        }
    });
</laser:script>