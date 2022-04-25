<%@ page import="org.springframework.context.i18n.LocaleContextHolder; de.laser.Identifier; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.ApiSource; de.laser.storage.RDStore; de.laser.IdentifierNamespace; de.laser.Package; de.laser.TitleInstancePackagePlatform; de.laser.IssueEntitlement; de.laser.I10nTranslation; de.laser.Platform; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection />
<!-- template: meta/identifier : editable: ${editable} -->
<%
    Map<String, Object> idData = identifierService.prepareIDsForTable(object), objectIds = idData.objectIds
    int count = idData.count
    boolean objIsOrgAndInst = idData.objIsOrgAndInst
    List<IdentifierNamespace> nsList = idData.nsList
%>

<aside class="ui segment la-metabox accordion">
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
                <g:form controller="ajax" action="addIdentifier" class="ui form">
                    <hr/>
                    <div class="field">
                        <label for="namespace">${message(code:'identifier.namespace.label')}</label>
                        <semui:dropdownWithI18nExplanations name="namespace" id="namespace" class="ui search dropdown"
                                                            from="${nsList}" noSelection=""
                                                            optionKey="${{ IdentifierNamespace.class.name + ':' + it.id }}"
                                                            optionValue="${{ it.getI10n('name') ?: it.ns }}"
                                                            optionExpl="${{ it.getI10n('description') }}"/>
                    </div>
                    <div class="two fields">
                        <input name="owner" type="hidden" value="${object.class.name}:${object.id}" />
                        <div class="field">
                            <label for="value">${message(code:'default.identifier.label')}</label>
                            <input name="value" id="value" type="text" class="ui" />
                        </div>
                        <div class="field">
                            <label for="note">${message(code:'default.note.label')}</label>
                            <input name="note" id="note" type="text" class="ui" />
                        </div>
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

<div class="la-metabox-spacer"></div>
<!-- template: meta/identifier -->