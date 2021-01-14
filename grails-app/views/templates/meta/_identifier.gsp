<%@ page import="org.springframework.context.i18n.LocaleContextHolder; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.IdentifierNamespace; de.laser.Package; de.laser.IssueEntitlement; de.laser.I10nTranslation" %>
<laser:serviceInjection />
<!-- template: meta/identifier : editable: ${editable} -->

<g:set var="objIsOrgAndInst" value="${object instanceof Org && object.getAllOrgTypeIds().contains(RDStore.OT_INSTITUTION.id)}" />

<aside class="ui segment metaboxContent accordion">
    <div class="title">
        <i class="dropdown icon la-dropdown-accordion"></i><g:message code="default.identifiers.show"/>
    </div>

    <div class="content">
        <div class="inline-lists">
            <dl>
                <dt><g:message code="globalUID.label"/></dt>
                <dd><g:fieldValue bean="${object}" field="globalUID"/></dd>

                <g:if test="${! objIsOrgAndInst}"><%-- hidden if org.institution--%>

                    <g:if test="${!(object instanceof License) && !(object instanceof Subscription) && !(object instanceof IssueEntitlement)}">

                    <dt><g:message code="org.gokbId.label" default="GOKB UUID"/></dt>
                    <dd>
                        <g:set var="editableGOKBID" value=""/>
                        <g:if test="${accessService.checkConstraint_ORG_COM_EDITOR()}">
                            <g:set var="editableGOKBID" value="${true}"/>
                        </g:if>
                        <sec:ifAnyGranted roles='ROLE_ADMIN,ROLE_YODA,ROLE_ORG_EDITOR'>
                            <g:set var="editableGOKBID" value="${true}"/>
                        </sec:ifAnyGranted>
                        <g:if test="${object instanceof Org && object.gokbId == null && editableGOKBID}">
                            <semui:xEditable owner="${object}" field="gokbId"/>
                        </g:if>
                        <g:else>
                            <g:fieldValue bean="${object}" field="gokbId"/>
                        </g:else>

                        <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                                var="gokbAPI">
                            <g:if test="${object.gokbId}">
                                <g:if test="${object instanceof Package}">
                                    <a target="_blank"
                                       href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/public/packageContent/' + object?.gokbId : '#'}"><i
                                            title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                                </g:if>
                                <g:else>
                                    <a target="_blank"
                                       href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/resource/show/' + object?.gokbId : '#'}"><i
                                            title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                                </g:else>
                            </g:if>
                        </g:each>
                    </dd>

                    </g:if>
                </g:if><%-- hidden if org.institution--%>

                <g:if test="${object.hasProperty('ids')}">
                <dt>
                    <g:message code="org.ids.label"/>
                </dt>
                <dd>
                    <table class="ui celled la-table compact table la-ignore-fixed">
                        <thead>
                        <tr>
                            <th>${message(code: 'identifier.namespace.label')}</th>
                            <th>${message(code: 'default.identifier.label')}</th>
                            <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
                                <th>${message(code: 'default.actions.label')}</th>
                            </g:if><%-- hidden if org[type=institution] --%>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${object.ids?.sort { a, b ->
                            String aVal = a.ns.getI10n('name') ?: a.ns.ns
                            String bVal = b.ns.getI10n('name') ?: b.ns.ns
                            aVal.compareToIgnoreCase bVal
                        }}" var="ident">
                            <tr>
                                <td>
                                    ${ident.ns.getI10n('name') ?: ident.ns.ns}

                                    <g:if test="${ident.ns.getI10n('description')}">
                                        <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${ident.ns.getI10n('description')}">
                                            <i class="question circle icon"></i>
                                        </span>
                                    </g:if>
                                </td>
                                <td>
                                    ${ident.value}
                                </td>
                                <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>
                                    <td>
                                        <g:if test="${editable}">
                                            <g:link controller="ajax" action="deleteIdentifier" class="ui icon negative mini button"
                                                    params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'>
                                                <i class="icon trash alternate"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </g:if><%-- hidden if org[type=institution] --%>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </dd>
                </g:if>

                <g:if test="${! objIsOrgAndInst}"><%-- hidden if org[type=institution] --%>

                    <%
                        String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())
                        List<IdentifierNamespace> nsList = IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where (idns.nsType = :objectType or idns.nsType = null) and idns.isFromLaser = true order by idns.ns asc, idns.name_'+locale+' asc',[objectType:object.class.name])
                    %>
                    <g:if test="${editable && nsList}">

                    <dt class="la-js-hideMe"></dt>

                    <dd class="la-js-hideMe">
                        <g:if test="${object.class.simpleName == 'License'}">
                            ${message(code: 'identifier.select.text', args: ['gasco-lic:0815'])}
                        </g:if>
                        <g:if test="${object.class.simpleName == 'Org'}">
                            ${message(code: 'identifier.select.text', args: ['isil:DE-18'])}
                        </g:if>
                        <g:if test="${object.class.simpleName == 'Subscription'}">
                            ${message(code: 'identifier.select.text', args: ['JC:66454'])}
                        </g:if>
                        <g:if test="${object.class.simpleName in ['BookInstance', 'DatabaseInstance', 'JournalInstance', 'TitleInstance']}">
                            ${message(code: 'identifier.select.text', args: ['eISSN:2190-9180'])}
                        </g:if>

                        <g:form controller="ajax" action="addIdentifier" class="ui form">
                            <input name="owner" type="hidden" value="${object.class.name}:${object.id}" />

                            <div class="fields two">
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
                    </dd>
                    </g:if>

                </g:if><%-- hidden if org[type=institution] --%>

            </dl>
        </div>
    </div>
</aside>

<div class="metaboxContent-spacer"></div>
<!-- template: meta/identifier -->