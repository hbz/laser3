<%@ page import="de.laser.auth.Role" %>
<laser:serviceInjection/>
<laser:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form controller="${controllerName}" action="${actionName}" params="${params.id ? [id:params.id] : []}" method="get" class="ui form">

        <div class="four fields">
            <div class="field">
                <label for="name"><g:message code="default.search.text"/></label>
                <input type="text" id="name" name="name" value="${params.name}"/>
            </div>

            <div class="field">
                <label for="role"><g:message code="user.role"/></label>
                <g:select from="${filterableRoles}"
                          noSelection="${['' : message(code:'accessMethod.all')]}"
                          class="ui dropdown"
                          value="${params.role}" optionKey="${{genericOIDService.getOID(it)}}" optionValue="${{message(code:'cv.roles.'+it.authority)}}" id="role" name="role" />
            </div>

            <g:if test="${orgField}">
                <div class="field">
                    <label for="org"><g:message code="user.org"/></label>
                    <g:select from="${availableComboOrgs}" noSelection="${['': message(code:'accessMethod.all')]}" class="ui search dropdown"
                              value="${params.org}" optionKey="${{genericOIDService.getOID(it)}}" optionValue="${{it.getDesignation()}}" id="org" name="org" />
                </div>
            </g:if>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" value="${message(code:'default.button.search.label')}" class="ui secondary button"/>
            </div>
        </div>
    </g:form>
</semui:filter>