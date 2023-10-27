<%@ page import="de.laser.Identifier; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.remote.ApiSource; de.laser.storage.RDStore; de.laser.IdentifierNamespace; de.laser.Package; de.laser.TitleInstancePackagePlatform; de.laser.IssueEntitlement; de.laser.I10nTranslation; de.laser.Platform; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection />
<!-- template: meta/identifier : editable: ${editable} -->
<%
    Map<String, Object> idData = identifierService.prepareIDsForTable(object), objectIds = idData.objectIds
    int count = idData.count
    boolean objIsOrgAndInst = idData.objIsOrgAndInst
    List<IdentifierNamespace> nsList = idData.nsList
    Map<String, Object> namespacesWithValidations = [:]
    nsList.each { IdentifierNamespace idns ->
        if(idns.validationRegex)
            namespacesWithValidations.put(genericOIDService.getOID(idns), [pattern: idns.validationRegex, prompt: message(code: "validation.${idns.ns.replaceAll(' ','_')}Match"), placeholder: message(code: "identifier.${idns.ns.replaceAll(' ','_')}.info")])
    }
%>

<aside class="ui segment la-metabox accordion" id="identifierAccordion">
    <div class="title">
        <div class="ui blue ribbon label">${count}</div>
        <g:message code="default.identifiers.show"/><i class="dropdown icon la-dropdown-accordion"></i>
    </div>

    <div class="content">
        <div id="objIdentifierPanel">
            <laser:render template="/templates/meta/identifierList" model="[object: object, editable: editable, objectIds: objectIds, count: count]"/>
        </div>
        <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
            <g:if test="${editable && nsList}">
                <g:form name="addIdentifier" controller="ajax" action="addIdentifier" class="ui form">
                    <hr/>
                    <div class="field">
                        <label for="namespace">${message(code:'identifier.namespace.label')}</label>
                        <ui:dropdownWithI18nExplanations name="namespace" id="namespace" class="ui search dropdown"
                                                            from="${nsList}" noSelection=""
                                                            optionKey="${{ genericOIDService.getOID(it) }}"
                                                            optionValue="${{ it.getI10n('name') ?: it.ns }}"
                                                            optionExpl="${{ it.getI10n('description') }}"/>
                    </div>
                    <div class="two fields">
                        <input name="owner" type="hidden" value="${genericOIDService.getOID(object)}" />
                        <div class="field">
                            <label for="value">${message(code:'default.identifier.label')} <i id="idExpl" class="ui icon question circle la-delay la-popup-tooltip" data-content=""></i></label>
                            <input name="value" id="value" type="text" class="ui" />
                        </div>
                        <div class="field">
                            <label for="note">${message(code:'default.note.label')}</label>
                            <input name="note" id="note" type="text" class="ui" />
                        </div>
                        <g:if test="${institution.isCustomerType_Consortium()}">
                            <input name="auditNewIdentifier" id="auditNewIdentifier" type="hidden" value="false"/>
                            <button id="auditNewIdentifierToggle" data-content="${message(code: 'property.audit.off.tooltip')}" class="ui icon blue button la-modern-button la-audit-button la-popup-tooltip la-delay">
                                <i aria-hidden="true" class="icon la-js-editmode-icon la-thumbtack slash"></i>
                            </button>
                        </g:if>
                        <div class="right aligned field">
                            <label>&nbsp;</label>
                            <button type="submit" class="ui button">${message(code:'default.button.add.label')}</button>
                        </div>
                    </div>
                </g:form>
            </g:if>
        </g:if><%-- hidden if org[type=institution] --%>
    </div>
</aside>

<laser:script file="${this.getGroovyPageFileName()}">
    //hidden does not work
    $("#idExpl").hide();
    let dictionary = {};
    let pattern;
    <g:each in="${namespacesWithValidations}" var="entry">
        <g:set var="key" value="${entry.getKey()}"/>
        <g:set var="ns" value="${entry.getValue()}"/>
        pattern = '${ns.pattern}';
        dictionary['${key}'] = {pattern: pattern.replaceAll('&#92;','\\'), prompt: '${ns.prompt}', placeholder: '${ns.placeholder}'.replaceAll('&quot;','"')};
    </g:each>

    $.fn.form.settings.rules.identifierRegex = function() {
        let namespace = $("#namespace").dropdown('get value');
        if(dictionary.hasOwnProperty(namespace)) {
            return $('#value').val().match(dictionary[namespace].pattern);
        }
        else return true;
        %{--
        if(namespace === '${genericOIDService.getOID(ezbSubIdNs)}') {
            return $('#value').val().match('${ezbSubIdNs.validationRegex}'.replace('&#92;', '\\'));
        }
        else if(namespace === '${genericOIDService.getOID(ezbCollIdNs)}') {
            return $('#value').val().match('${ezbCollIdNs.validationRegex}'.replace('&#92;', '\\'));
        }
        --}%
    };

    <g:if test="${flash.message?.contains(message(code:'identifier.label'))}">
        $('#identifierAccordion').accordion('open', 0);
    </g:if>

    $("#auditNewIdentifierToggle").click(function(e) {
        e.preventDefault();
        let inputVal = $("#auditNewIdentifier").val();
        let button = $(this);
        let icon = $(this).find('i');
        button.toggleClass('blue').toggleClass('green');
        if(inputVal === 'true') {
            $("#auditNewIdentifier").val('false');
            icon.addClass('la-thumbtack slash').removeClass('thumbtack');
            button.attr('data-content', "${message(code: 'property.audit.off.tooltip')}");
        }
        else {
            $("#auditNewIdentifier").val('true');
            icon.removeClass('la-thumbtack slash').addClass('thumbtack');
            button.attr('data-content', "${message(code: 'property.audit.on.tooltip')}");
        }
    });

    $('#namespace').change(function() {
        let namespace = $(this).dropdown('get value');
        if(dictionary.hasOwnProperty(namespace)) {
            let dictEntry = dictionary[namespace];
            $("#idExpl").attr("data-content", dictEntry.prompt);
            $("#idExpl").show();
        }
        else {
            $("#idExpl").hide();
        }
    });

    $('#addIdentifier').form({
        on: 'blur',
        inline: true,
        fields: {
            value: {
                identifier: 'value',
                rules: [
                    {
                        type: 'identifierRegex',
                        prompt: '<g:message code="validation.generic"/>'
                    }
                ]
            }
        }
    });
</laser:script>

<div class="la-metabox-spacer"></div>
<!-- template: meta/identifier -->