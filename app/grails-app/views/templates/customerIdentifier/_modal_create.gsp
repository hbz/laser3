<%@ page import="com.k_int.kbplus.Platform; com.k_int.kbplus.CustomerIdentifier; com.k_int.kbplus.IdentifierNamespace" %>
<semui:modal id="modalCreateCustomerIdentifier"
             text="${customeridentifier? message(code:'org.customerIdentifier.create.edit') : message(code:'org.customerIdentifier.create.new')}"
             isEditModal="true"
             msgSave="${customeridentifier ? message(code:'default.button.save.label') : message(code:'default.button.create.label')}">

    <g:form id="customeridentifier" class="ui form"  url="[controller:'organisation', action:customeridentifier? 'processEditCustomerIdentifier' : 'processCreateCustomerIdentifier', id:customeridentifier?.id]" method="post">

        <g:hiddenField name="orgid" value="${orgInstance?.id}"/>
        <g:if test="${customeridentifier}">
            <g:hiddenField name="customeridentifier" value="${customeridentifier?.id}"/>
        </g:if>

        <div class="field fieldcontain">
            <label for="addCIPlatform">${message(code:'default.provider.label')} : ${message(code:'platform.label')}</label>
            <g:if test="${customeridentifier}">
                <% Platform p = customeridentifier?.platform%>
                <input type="text" id="addCIPlatform" name="addCIPlatform" value="${ p.org.name + (p.org.sortname ? " (${p.org.sortname})" : '') + ' : ' + p.name}" disabled/>
            </g:if>
            <g:else>
                <g:select id="addCIPlatform" name="addCIPlatform"
                          from="${allPlatforms}"
                          required=""
                          class="ui search dropdown"
                          value="${customeridentifier?.id}"
                          optionKey="id"
                          optionValue="${{ it.org.name + (it.org.sortname ? " (${it.org.sortname})" : '') + ' : ' + it.name}}" />
            </g:else>
        </div>
                      %{--optionKey="${{'com.k_int.kbplus.Platform:' + it.id}}"--}%


        <div class="field fieldcontain">
            <label for="value">${message(code: 'org.customerIdentifier')}:</label>

            <input type="text" id="value" name="value" value="${customeridentifier?.value}" required/>
        </div>

        <div class="field fieldcontain">
            <label for="note">${message(code: 'default.notes.label')}:</label>

            <input type="text" id="note" name="note" value="${customeridentifier?.note}"/>
        </div>

    </g:form>
</semui:modal>