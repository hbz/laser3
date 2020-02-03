<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.License; de.laser.helper.RDStore; com.k_int.kbplus.Package; com.k_int.kbplus.IdentifierNamespace" %>
<laser:serviceInjection />
<!-- template: meta/identifier : editable: ${editable} -->
<aside class="ui segment metaboxContent accordion">
    <div class="title">
        <i class="dropdown icon la-dropdown-accordion"></i><g:message code="default.identifiers.show"/>
    </div>

    <div class="content">
        <div class="inline-lists">
            <dl>
                <dt><g:message code="org.globalUID.label"/></dt>
                <dd><g:fieldValue bean="${object}" field="globalUID"/></dd>
                <dt><g:message code="org.impId.label"/></dt>
                <dd><g:fieldValue bean="${object}" field="impId"/></dd>

                <g:if test="${!(object instanceof com.k_int.kbplus.License) && !(object instanceof com.k_int.kbplus.Subscription)}">
                    <dt><g:message code="org.gokbId.label" default="GOKB UUID"/></dt>
                    <dd>
                        <g:set var="editableGOKBID" value=""/>
                        <g:if test="${accessService.checkConstraint_ORG_COM_EDITOR()}">
                            <g:set var="editableGOKBID" value="${true}"/>
                        </g:if>
                        <sec:ifAnyGranted roles='ROLE_ADMIN,ROLE_YODA,ROLE_ORG_EDITOR'>
                            <g:set var="editableGOKBID" value="${true}"/>
                        </sec:ifAnyGranted>
                        <g:if test="${object instanceof com.k_int.kbplus.Org && object?.gokbId == null && editableGOKBID}">
                            <semui:xEditable owner="${object}" field="gokbId"/>
                        </g:if>
                        <g:else>
                            <g:fieldValue bean="${object}" field="gokbId"/>
                        </g:else>

                        <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                                var="gokbAPI">
                            <g:if test="${object?.gokbId}">
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


                <dt>
                    <g:message code="org.ids.label"/>
                </dt>
                <dd>
                    <table class="ui celled la-table la-table-small table la-ignore-fixed">
                        <thead>
                        <tr>
                            <th>${message(code: 'default.authority.label', default: 'Authority')}</th>
                            <th>${message(code: 'default.identifier.label', default: 'Identifier')}</th>
                            <th>${message(code: 'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${object.ids?.sort { it?.ns?.ns }}" var="ident">
                            <tr>
                                <td>
                                    ${ident.ns.ns}
                                </td>
                                <td>
                                    ${ident.value}
                                </td>
                                <td>
                                    <g:if test="${editable}">
                                        <%-- TODO [ticket=1612] new identifier handling
                                        <g:link controller="ajax" action="deleteThrough"
                                                params='${[contextOid: "${object.class.name}:${object.id}", contextProperty: "ids", targetOid: "${ident.class.name}:${ident.id}"]}'>
                                            ${message(code: 'default.delete.label', args: ["${message(code: 'identifier.label')}"])}</g:link>
                                        --%>
                                        <g:link controller="ajax" action="deleteIdentifier"
                                                params='${[owner: "${object.class.name}:${object.id}", target: "${ident.class.name}:${ident.id}"]}'>
                                            ${message(code: 'default.delete.label', args: ["${message(code: 'identifier.label')}"])}</g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </dd>

                <%
                    List<IdentifierNamespace> nsList = IdentifierNamespace.where{(nsType == object.class.name || nsType == null)}
                            .list(sort: 'ns')
                            .sort { a,b -> a.ns.compareToIgnoreCase b.ns }
                            .collect{ it }
                %>
                <g:if test="${editable && nsList}">
                    <dt class="la-js-hideMe">
                        Identifikfator hinzufügen
                    </dt>

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
                                    <label for="namespace">Namensraum</label>
                                    <g:select name="namespace" id="namespace" class="ui search dropdown"
                                              from="${nsList}" optionKey="${{'com.k_int.kbplus.IdentifierNamespace:' + it.id}}" optionValue="ns" />
                                </div>
                                <div class="field">
                                    <label for="value">Identifikator</label>
                                    <input name="value" id="value" type="text" class="ui" />
                                </div>
                                <div class="field">
                                    <label>&nbsp;</label>
                                    <button type="submit" class="ui button">Hinzufügen</button>
                                </div>
                            </div>
                        </g:form>
                    </dd>
                <%-- TODO [ticket=1612] new identifier handling
                    <dd class="la-js-hideMe">
                        <g:if test="${object.class.simpleName == 'License'}">
                            <semui:formAddIdentifier owner="${object}"
                                                     buttonText="${message(code: 'license.edit.identifier.select.add')}"
                                                     uniqueCheck="yes"
                                                     uniqueWarningText="${message(code: 'license.edit.duplicate.warn.list')}">
                                ${message(code: 'identifier.select.text', args: ['gasco-lic:0815'])}
                            </semui:formAddIdentifier>
                        </g:if>

                        <g:if test="${object.class.simpleName == 'Org'}">
                            <semui:formAddIdentifier owner="${object}">
                                ${message(code: 'identifier.select.text', args: ['isil:DE-18'])}
                            </semui:formAddIdentifier>
                        </g:if>

                        <g:if test="${object.class.simpleName == 'Package'}">
                            <semui:formAddIdentifier owner="${object}"/>
                        </g:if>

                        <g:if test="${object.class.simpleName == 'Subscription'}">
                            <semui:formAddIdentifier owner="${object}" uniqueCheck="yes"
                                                     uniqueWarningText="${message(code: 'subscription.details.details.duplicate.warn')}">
                                ${message(code: 'identifier.select.text', args: ['JC:66454'])}
                            </semui:formAddIdentifier>
                        </g:if>

                        <g:if test="${object.class.simpleName == 'TitleInstancePackagePlatform'}">
                            <semui:formAddIdentifier owner="${object}"/>
                        </g:if>

                        <g:if test="${object.class.simpleName in ['BookInstance', 'DatabaseInstance', 'JournalInstance', 'TitleInstance']}">
                            <semui:formAddIdentifier owner="${object}"
                                                     buttonText="${message(code: 'title.edit.identifier.select.add')}"
                                                     uniqueCheck="yes"
                                                     uniqueWarningText="${message(code: 'title.edit.duplicate.warn.list')}">
                                ${message(code: 'identifier.select.text', args: ['eISSN:2190-9180'])}
                            </semui:formAddIdentifier>
                        </g:if>
                    </dd>
                --%>

                </g:if>

            </dl>
        </div>
    </div>
</aside>

<div class="metaboxContent-spacer"></div>
<!-- template: meta/identifier -->