<%@ page import="de.laser.*;de.laser.storage.RDStore;de.laser.interfaces.CalculatedType;de.laser.storage.RDConstants" %>
<laser:serviceInjection/>
<g:if test="${editable}">

        <a role="button"
           class="ui button la-modern-button"
           data-ui="modal" href="#${tmplModalID}"
           class="la-popup-tooltip">
            <g:if test="${tmplButtonText}">
                ${tmplButtonText}
            </g:if>
        </a>

</g:if>

<%
    String lookupName, instanceType
    switch(linkField) {
        case 'license':
            lookupName = "lookupLicenses"
            instanceType = message(code:"license")
            break
        case 'provider':
            lookupName = "lookupProviders"
            instanceType = message(code:"provider.label")
            break
    }
%>

<ui:modal id="${tmplModalID}" text="${tmplText}" msgSave="${message(code: 'default.button.link.label')}">
    <g:form class="ui form" controller="survey" action="setProviderOrLicenseLink" params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]" method="post">
        <div class="field">
            <div id="sub_role_tab_${tmplModalID}" class="ui grid">
                <div class="row">
                    <div class="four wide column">
                        <g:message code="${instanceType}" />
                    </div>
                    <div class="twelve wide column">
                            <div class="ui search selection dropdown clearable la-full-width" id="${linkField}">
                                <input type="hidden" name="${linkField}"/>
                                <i class="dropdown icon"></i>
                                <input type="text" class="search"/>
                                <div class="default text"></div>
                            </div>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</ui:modal>
<g:if test="${linkField}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $("#${linkField}").dropdown({
            apiSettings: {
                url: "<g:createLink controller="ajaxJson" action="${lookupName}"/>?query={query}",
                cache: false
            },
            clearable: true,
            minCharacters: 1
        });
    </laser:script>
</g:if>