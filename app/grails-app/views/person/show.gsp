
<%@ page import="com.k_int.kbplus.Person" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>
		<g:render template="nav" contextPath="." />

		<semui:messages data="${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

			<div class="inline-lists">
				<dl>
                    <dt><g:message code="person.first_name.label" default="Firstname" /></dt>
                    <dd><semui:xEditable owner="${personInstance}" field="first_name" /></dd>

                    <dt><g:message code="person.middle_name.label" default="Middlename" /></dt>
                    <dd><semui:xEditable owner="${personInstance}" field="middle_name" /></dd>

                    <dt><g:message code="person.last_name.label" default="Lastname" /></dt>
                    <dd><semui:xEditable owner="${personInstance}" field="last_name" /></dd>

                    <dt><g:message code="person.gender.label" default="Gender" /></dt>
                    <dd><semui:xEditableRefData owner="${personInstance}" field="gender" config="Gender" /></dd>

                    <dt><g:message code="person.roleType.label" default="Person Position" /></dt>
                    <dd><semui:xEditableRefData owner="${personInstance}" field="roleType" config="Person Position" /></dd>

                    <dt><g:message code="person.contactType.label" default="Person Contact Type" /></dt>
                    <dd><semui:xEditableRefData owner="${personInstance}" field="contactType" config="Person Contact Type" /></dd>

                    <dt><g:message code="person.contacts.label" default="Contacts" /></dt>
                    <dd>
                        <ul>
                        <g:each in="${personInstance.contacts}" var="c">
                            <li>
                                <g:render template="/templates/cpa/contact" model="${[contact: c]}"></g:render>
                            </li>
                        </g:each>
                        </ul>
                        <g:if test="${editable}">
                            <input class="ui button" type="button" data-semui="modal" href="#contactFormModal"
                                    value="${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}">
                            <g:render template="/contact/formModal" model="['prsId': personInstance?.id]"/>
                        </g:if>
                    </dd>

                    <dt><g:message code="person.addresses.label" default="Addresses" /></dt>
                    <dd>
                        <ul>
                        <g:each in="${personInstance.addresses}" var="a">
                            <li>
                                <g:render template="/templates/cpa/address" model="${[address: a]}"></g:render>
                            </li>
                        </g:each>
                        </ul>
                        <g:if test="${editable}">
                            <input class="ui button" type="button" data-semui="modal" href="#addressFormModal"
                                   value="${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}">
                            <g:render template="/address/formModal" model="['prsId': personInstance?.id]"/>
                        </g:if>
                    </dd>

                    <%--
					<g:if test="${personInstance?.tenant}">
						<dt><g:message code="person.tenant.label" default="Tenant" /></dt>
						<dd><g:link controller="organisations" action="show" id="${personInstance.tenant?.id}">${personInstance.tenant?.encodeAsHTML()}</g:link></dd>	
					</g:if>

					<g:if test="${personInstance?.isPublic}">
						<dt><g:message code="person.isPublic.label" default="IsPublic" /></dt>
						<dd><semui:xEditableRefData owner="${personInstance}" field="isPublic" config="YN" /></dd>
					</g:if>
				    --%>

                    <dt><g:message code="person.functions.label" default="Functions" /></dt>
                    <dd><ul>
                        <g:each in="${personInstance.roleLinks}" var="link">
                            <g:if test="${link.functionType}">
                                <li>
                                    ${link.functionType?.getI10n('value')}
                                    <br/>

                                    <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                    (Organisation)
                                </li>
                            </g:if>
                        </g:each>
                    </ul></dd>

                    <dt><g:message code="person.responsibilites.label" default="Responsibilites" /></dt>
                    <dd><ul>
                        <g:each in="${personInstance.roleLinks}" var="link">
                            <g:if test="${link.responsibilityType}">
                                <li>
                                    ${link.responsibilityType?.getI10n('value')}<br/>

                                    <g:if test="${link.pkg}">
                                        <g:link controller="packageDetails" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
                                        (Package) <br />
                                    </g:if>
                                    <g:if test="${link.cluster}">
                                        <g:link controller="cluster" action="show" id="${link.cluster.id}">${link.cluster.name}</g:link>
                                        (Cluster) <br />
                                    </g:if>
                                    <g:if test="${link.sub}">
                                        <g:link controller="subscriptionDetails" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
                                        (Subscription) <br />
                                    </g:if>
                                    <g:if test="${link.lic}">
                                        ${link.lic}
                                        (License) <br />
                                    </g:if>
                                    <g:if test="${link.title}">
                                        <g:link controller="titleDetails" action="show" id="${link.title.id}">${link.title.title}</g:link>
                                        (Title) <br />
                                    </g:if>

                                    <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
                                    (Organisation)
                                </li>
                            </g:if>
                        </g:each>
                    </ul>
                    </dd>
                </dl>
            </div>
            <g:if test="${editable}">
				<g:form>
					<g:hiddenField name="id" value="${personInstance?.id}" />
					<div class="ui form-actions">
						<g:link class="ui button" action="edit" id="${personInstance?.id}">
							<i class="write icon"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui negative button" type="submit" name="_action_delete">
							<i class="trash alternate icon"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>
            </g:if>

			</div><!-- .twelve -->

            <aside class="four wide column">
            </aside><!-- .four -->

		</div><!-- .grid -->

	</body>
</html>
