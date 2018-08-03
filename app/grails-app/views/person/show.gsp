<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole; com.k_int.kbplus.RefdataValue;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.all_orgs" controller="organisations" action="index" />
    <g:message code="default.show.label" args="[entityName]" class="active"/>
</semui:breadcrumbs>

<g:set var="personType" value="${!personInstance.contactType || personInstance.contactType?.value?.equals('Personal contact')}" />

<h1 class="ui header"><semui:headerIcon/>
${personInstance}
</h1>

<g:render template="nav" contextPath="."/>

<semui:messages data="${flash}"/>

<div class="ui grid">
    <div class="twelve wide column">

        <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <dl><dt>${com.k_int.kbplus.RefdataCategory.findByDesc('Person Contact Type').getI10n('desc')}</dt>
                        <dd>
                            <semui:xEditableRefData owner="${personInstance}" field="contactType" config="Person Contact Type"/>
                        </dd>
                    </dl>

                    <g:if test="${personType}">
                        <dl><dt id="person_title"><g:message code="person.title.label" default="Title"/></dt>
                            <dd><semui:xEditable owner="${personInstance}" field="title"/></dd>
                        </dl>
                    </g:if>

                    <dl>
                        <dt id="person_last_name">
                            <g:if test="${personType}">
                                <g:message code="person.last_name.label" default="Lastname"/>
                            </g:if>
                            <g:else>
                                Benenner
                            </g:else>
                        </dt>
                        <dd><semui:xEditable owner="${personInstance}" field="last_name"/></dd>
                    </dl>

                    <g:if test="${personType}">

                        <dl><dt><g:message code="person.first_name.label" default="Firstname"/></dt>
                            <dd><semui:xEditable owner="${personInstance}" field="first_name"/></dd></dl>

                        <dl><dt><g:message code="person.middle_name.label" default="Middlename"/></dt>
                            <dd><semui:xEditable owner="${personInstance}" field="middle_name"/></dd></dl>

                        <dl><dt><g:message code="person.gender.label" default="Gender"/></dt>
                            <dd><semui:xEditableRefData owner="${personInstance}" field="gender" config="Gender"/></dd>
                        </dl>

                        <dl><dt>${com.k_int.kbplus.RefdataCategory.findByDesc('Person Position').getI10n('desc')}</dt>
                            <dd><semui:xEditableRefData owner="${personInstance}" field="roleType"
                                                        config="Person Position"/></dd></dl>
                    </g:if>


                    <dl><dt><g:message code="person.contacts.label" default="Contacts"/></dt>
                        <dd>
                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${personInstance.contacts.sort{it.content}}" var="c">

                                    <g:render template="/templates/cpa/contact" model="${[
                                            contact: c,
                                            tmplShowDeleteButton: true,
                                            controller: 'person',
                                            action: 'show',
                                            id: personInstance.id
                                    ]}"></g:render>

                                </g:each>
                            </div>
                            <g:if test="${editable}">
                                <input class="ui button" type="button" data-semui="modal" href="#contactFormModal"
                                       value="${message(code: 'default.add.label', args: [message(code: 'person.contacts.label', default: 'Contacts')])}">
                                <g:render template="/contact/formModal" model="['prsId': personInstance?.id]"/>
                            </g:if>
                        </dd>
                    </dl>

                    <dl><dt><g:message code="person.addresses.label" default="Addresses"/></dt>
                        <dd>
                            <div class="ui list">
                                <g:each in="${personInstance.addresses.sort{it.type?.getI10n('value')}}" var="a">

                                    <g:render template="/templates/cpa/address" model="${[
                                            address: a,
                                            tmplShowDeleteButton: true,
                                            controller: 'person',
                                            action: 'show',
                                            id: personInstance.id
                                    ]}"></g:render>

                                </g:each>
                            </div>
                            <g:if test="${editable}">
                                <input class="ui button" type="button" data-semui="modal" href="#addressFormModal"
                                       value="${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}">
                                <g:render template="/address/formModal" model="['prsId': personInstance?.id]"/>
                            </g:if>
                        </dd>
                    </dl>

                    <dl><dt><g:message code="person.functions.label" default="Functions"/></dt>
                        <dd>
                            <ul>
                                <g:each in="${personInstance.roleLinks}" var="link">
                                    <g:if test="${link.functionType}">
                                        <li>
                                            ${link.functionType?.getI10n('value')}
                                            <br/>

                                            <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                            (Organisation)

                                            <g:if test="${editable}">
                                                <br />
                                                <div class="ui mini icon buttons">
                                                    <g:set var="oid" value="${link.class.name}:${link.id}" />
                                                    <g:link class="ui negative button" controller="person" action="deletePersonRole" id="${personInstance.id}" params="[oid: oid]">
                                                        <i class="trash alternate icon"></i>
                                                    </g:link>
                                                </div>
                                            </g:if>

                                        </li>
                                    </g:if>
                                </g:each>
                            </ul>

                            <g:if test="${editable}">
                                <a href="#personRoleFormModal" data-semui="modal" class="ui button">${message('code':'default.button.add.label')}</a>
                            </g:if>
                        </dd>
                    </dl>

                    <dl><dt><g:message code="person.responsibilites.label" default="Responsibilites"/></dt>
                        <dd>
                            <ul>
                                <g:each in="${personInstance.roleLinks}" var="link">
                                    <g:if test="${link.responsibilityType}">
                                        <li>
                                            ${link.responsibilityType?.getI10n('value')}<br/>

                                            <g:if test="${link.pkg}">
                                                <g:link controller="packageDetails" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
                                                (Package) <br/>
                                            </g:if>
                                            <g:if test="${link.cluster}">
                                                <g:link controller="cluster" action="show" id="${link.cluster.id}">${link.cluster.name}</g:link>
                                                (Cluster) <br/>
                                            </g:if>
                                            <g:if test="${link.sub}">
                                                <g:link controller="subscriptionDetails" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
                                                (Subscription) <br/>
                                            </g:if>
                                            <g:if test="${link.lic}">
                                                <g:link controller="licenseDetails" action="show" id="${link.lic.id}">${link.lic}</g:link>
                                                (License) <br/>
                                            </g:if>
                                            <g:if test="${link.title}">
                                                <g:link controller="titleDetails" action="show" id="${link.title.id}">${link.title.title}</g:link>
                                                (Title) <br/>
                                            </g:if>

                                            <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                            (Organisation)

                                            <g:if test="${editable}">
                                                <br />
                                                <div class="ui mini icon buttons">
                                                    <g:set var="oid" value="${link.class.name}:${link.id}" />
                                                    <g:link class="ui negative button" controller="person" action="deletePersonRole" id="${personInstance.id}" params="[oid: oid]">
                                                        <i class="trash alternate icon"></i>
                                                    </g:link>
                                                </div>
                                            </g:if>

                                        </li>
                                    </g:if>
                                </g:each>
                            </ul>

                            <%--
                            <g:if test="${editable}">
                                <button class="ui button add-person-role" type="button">${message('code':'default.button.add.label')}</button>
                            </g:if>
                            --%>
                        </dd></dl>

                    <g:if test="${personInstance?.tenant}">
                        <dl><dt><g:message code="person.tenant.label" default="Tenant"/></dt>
                            <dd>
                                <g:link controller="organisations" action="show"
                                        id="${personInstance.tenant?.id}">${personInstance.tenant?.encodeAsHTML()}</g:link>
                                <g:if test="${personInstance?.isPublic?.value == 'No'}">
                                    <i class="address card outline icon"></i>
                                </g:if>
                                <g:else>
                                    <i class="address card icon"></i>
                                </g:else>
                            </dd></dl>
                    </g:if>
                <%--
                    <g:if test="${personInstance?.isPublic}">
                        <dl><dt><g:message code="person.isPublic.label" default="IsPublic" /></dt>
                        <dd><semui:xEditableRefData owner="${personInstance}" field="isPublic" config="YN" /></dd></dl>
                    </g:if>
                --%>

                </div>
            </div>
        </div>

    </div><!-- .twelve -->

    <aside class="four wide column">
    </aside><!-- .four -->

</div><!-- .grid -->

<g:render template="prsRoleModal" model="[]" />

</body>
</html>
