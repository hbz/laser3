<%@ page import="org.springframework.context.i18n.LocaleContextHolder; de.laser.Identifier; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.IdentifierNamespace; de.laser.Package; de.laser.TitleInstancePackagePlatform; de.laser.IssueEntitlement; de.laser.I10nTranslation; de.laser.Platform" %>
<laser:serviceInjection />
<!-- template: meta/identifier : editable: ${editable} -->
<%
    boolean objIsOrgAndInst = object instanceof Org && object.getAllOrgTypeIds().contains(RDStore.OT_INSTITUTION.id)
    String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())
    List<IdentifierNamespace> nsList = IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where (idns.nsType = :objectType or idns.nsType = null) and idns.isFromLaser = true order by idns.name_'+locale+' asc',[objectType:object.class.name])
    Map<String, SortedSet> objectIds = [:]
    ApiSource gokbAPI
    if(!objIsOrgAndInst && object.hasProperty("gokbId") && object.gokbId) {
        SortedSet idSet = new TreeSet()
        idSet << object.gokbId
        objectIds.put(message(code: 'org.wekbId.label'), idSet)
        gokbAPI = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
    }
    if(object.globalUID) {
        SortedSet idSet = new TreeSet()
        idSet << object.globalUID
        objectIds.put(message(code: 'globalUID.label'), idSet)
    }
    if(object.hasProperty("ids")) {
        object.ids.each { Identifier ident ->
            String key = ident.ns.getI10n('name') ?: ident.ns.ns
            SortedSet<Identifier> idsOfNamespace = objectIds.get(key)
            if(!idsOfNamespace)
                idsOfNamespace = new TreeSet<Identifier>()
            idsOfNamespace << ident
            objectIds.put(key, idsOfNamespace)
        }
    }
    int count = 0
    objectIds.values().each { SortedSet idSet ->
        count += idSet.size()
    }
%>

<aside class="ui segment metaboxContent accordion">
    <div class="title">
        <div class="ui blue ribbon label">${count}</div>
        <g:message code="default.identifiers.show"/><i class="dropdown icon la-dropdown-accordion"></i>
    </div>

    <div class="content">
        <div class="ui ${editable ? "three" : "two"} column grid">
            <div class="ui header row">
                <div class="column">${message(code: 'identifier.namespace.label')}</div>
                <div class="column">${message(code: 'default.identifier.label')}</div>
                <g:if test="${editable}">
                    <div class="column">${message(code: 'default.actions.label')}</div>
                </g:if>
            </div>
            <g:each in="${objectIds}" var="row">
                <g:set var="namespace" value="${row.getKey()}"/>
                <g:each in="${row.getValue()}" var="ident">
                    <div class="ui row">
                        <div class="column">
                            ${namespace}
                            <g:if test="${ident instanceof Identifier && ident.ns.getI10n('description')}">
                                <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${ident.ns.getI10n('description')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </div>
                        <div class="column">
                            <g:if test="${ident instanceof Identifier}">
                                ${ident.value}
                            </g:if>
                            <g:else>
                                ${ident}
                                <g:if test="${!objIsOrgAndInst && object.hasProperty("gokbId") && ident == object.gokbId}">
                                    <g:if test="${object instanceof Package}">
                                        <a target="_blank"
                                           href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/packageContent/' + ident : '#'}"><i
                                                title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                                    </g:if>
                                    <g:elseif test="${object instanceof TitleInstancePackagePlatform}">
                                        <a target="_blank"
                                           href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/tippContent/' + ident : '#'}"><i
                                                title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                                    </g:elseif>
                                    <g:elseif test="${object instanceof Platform}">
                                        <a target="_blank"
                                           href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/platformContent/' + ident : '#'}"><i
                                                title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                                    </g:elseif>
                                </g:if>
                            </g:else>
                        </div>
                        <g:if test="${editable && ident instanceof Identifier}">
                            <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
                                <div class="column">
                                    <g:link controller="ajax" action="deleteIdentifier" class="ui icon negative mini button"
                                            params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="icon trash alternate"></i>
                                    </g:link>
                                </div>
                            </g:if>
                        </g:if><%-- hidden if org[type=institution] --%>
                    </div>
                </g:each>
            </g:each>
        </div>
        <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
            <g:if test="${editable && nsList}">
                <g:form controller="ajax" action="addIdentifier" class="ui form">
                    <hr/>
                    <div class="three fields">
                        <input name="owner" type="hidden" value="${object.class.name}:${object.id}" />
                        <div class="field">
                            <label for="namespace">${message(code:'identifier.namespace.label')}</label>
                            <g:select name="namespace" id="namespace" class="ui search dropdown"
                                      from="${nsList}"
                                      optionKey="${{ IdentifierNamespace.class.name + ':' + it.id }}"
                                      optionValue="${{ it.getI10n('name') ?: it.ns }}" />
                        </div>
                        <div class="field">
                            <label for="value">${message(code:'default.identifier.label')}</label>
                            <input name="value" id="value" type="text" class="ui" />
                        </div>
                        <div class="field">
                            <label>&nbsp;</label>
                            <button type="submit" class="ui button">${message(code:'default.button.add.label')}</button>
                        </div>
                    </div>
                </g:form>
            </g:if>
        </g:if><%-- hidden if org[type=institution] --%>
    </div>
</aside>

<div class="metaboxContent-spacer"></div>
<!-- template: meta/identifier -->