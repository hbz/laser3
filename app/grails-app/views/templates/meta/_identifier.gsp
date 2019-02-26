<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.License" %>
<!-- template: meta/identifier : editable: ${editable} -->
<aside class="ui segment metaboxContent accordion">
    <div class="title">
        <i class="dropdown icon la-dropdown-accordion"></i> Identifikatoren anzeigen
    </div>

    <div class="content">
        <div class="inline-lists">
            <dl>
                <dt><g:message code="org.globalUID.label" default="Global UID"/></dt>
                <dd><g:fieldValue bean="${object}" field="globalUID"/></dd>
                <dt><g:message code="org.impId.label"/></dt>
                <dd><g:fieldValue bean="${object}" field="impId"/></dd>

                <g:if test="${!(object instanceof com.k_int.kbplus.License) && !(object instanceof com.k_int.kbplus.Subscription)}">
                    <dt><g:message code="org.gokbId.label" default="GOKB UUID"/></dt>
                    <dd>
                        <g:set var="editableGOKBID" value=""/>
                        <sec:ifAnyGranted roles='ROLE_ADMIN,ROLE_YODA,ROLE_ORG_EDITOR,ROLE_ORG_COM_EDITOR'>
                            <g:set var="editableGOKBID" value="${true}"/>
                        </sec:ifAnyGranted>
                        <g:if test="${object instanceof com.k_int.kbplus.Org && object?.gokbId == null && editableGOKBID}">
                            <semui:xEditable owner="${object}" field="gokbId"/>
                        </g:if>
                        <g:else>
                            <g:fieldValue bean="${object}" field="gokbId"/>
                        </g:else>

                        <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}" var="gokbAPI">
                            <g:if test="${object?.gokbId}">
                                <a target="_blank"
                                   href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + object?.gokbId : '#'}"><i
                                        title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                            </g:if>
                        </g:each>
                    </dd>
                </g:if>

                <dt>
                    <g:message code="org.ids.label"/>
                </dt>
                <dd>
                    <table class="ui celled la-table la-table-small table ignore-floatThead">
                        <thead>
                        <tr>
                            <th>${message(code: 'default.authority.label', default: 'Authority')}</th>
                            <th>${message(code: 'default.identifier.label', default: 'Identifier')}</th>
                            <th>${message(code: 'default.actions.label', default: 'Actions')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${object.ids.sort { it.identifier.ns.ns }}" var="io">
                            <tr>
                                <td>
                                    ${io.identifier.ns.ns}
                                </td>
                                <td>
                                    <g:if test="${io.identifier.value =~ /^http/}">
                                        <a href="${io.identifier.value}" target="_blank">
                                            ${message(code: 'component.originediturl.label', default: "${io.identifier.value}")}
                                        </a>
                                    </g:if>
                                    <g:else>
                                        ${io.identifier.value}
                                    </g:else>
                                </td>
                                <td>
                                    <g:if test="${editable}">
                                        <g:link controller="ajax" action="deleteThrough"
                                                params='${[contextOid: "${object.class.name}:${object.id}", contextProperty: "ids", targetOid: "${io.class.name}:${io.id}"]}'>
                                            ${message(code: 'default.delete.label', args: ["${message(code: 'identifier.label')}"])}</g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>
                </dd>

                <g:if test="${editable}">
                    <dt>
                        Indentifikator hinzufügen
                    </dt>
                    <dd>
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
                </g:if>

            </dl>
        </div>
    </div>
</aside>

<div class="metaboxContent-spacer"></div>
<!-- template: meta/identifier -->