<% import grails.persistence.Event %>
<%=packageName%>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<h1 class="ui header"><g:message code="default.show.label" args="[entityName]" /></h1>

        <semui:messages data="\${flash}" />

		<div class="ui grid">

			<div class="twelve wide column">

				<dl>
				<%  excludedProps = Event.allEvents.toList() << 'id' << 'version'
					allowedNames = domainClass.persistentProperties*.name << 'dateCreated' << 'lastUpdated'
					props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) }
					Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
					props.each { p -> %>
					<g:if test="\${${propertyName}?.${p.name}}">
						<dt><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></dt>
						<%  if (p.isEnum()) { %>
							<dd><g:fieldValue bean="\${${propertyName}}" field="${p.name}"/></dd>
						<%  } else if (p.oneToMany || p.manyToMany) { %>
							<g:each in="\${${propertyName}.${p.name}}" var="${p.name[0]}">
							<dd><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${p.name[0]}.id}">\${${p.name[0]}?.encodeAsHTML()}</g:link></dd>
							</g:each>
						<%  } else if (p.manyToOne || p.oneToOne) { %>
							<dd><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${propertyName}?.${p.name}?.id}">\${${propertyName}?.${p.name}?.encodeAsHTML()}</g:link></dd>
						<%  } else if (p.type == Boolean || p.type == boolean) { %>
							<dd><g:formatBoolean boolean="\${${propertyName}?.${p.name}}" /></dd>
						<%  } else if (p.type == Date || p.type == java.sql.Date || p.type == java.sql.Time || p.type == Calendar) { %>
							<dd><g:formatDate date="\${${propertyName}?.${p.name}}" /></dd>
						<%  } else if(!p.type.isArray()) { %>
							<dd><g:fieldValue bean="\${${propertyName}}" field="${p.name}"/></dd>
						<%  } %>
					</g:if>
				<%  } %>
				</dl>

				<g:form class="ui form">
					<g:hiddenField name="id" value="\${${propertyName}?.id}" />
					<div class="ui segment form-actions">
						<g:link class="ui button" action="edit" id="\${${propertyName}?.id}">
							<i class="icon-pencil"></i>
							<g:message code="default.button.edit.label" default="Edit" />
						</g:link>
						<button class="ui button negative" type="submit" name="_action_delete">
							<i class="icon-trash icon-white"></i>
							<g:message code="default.button.delete.label" default="Delete" />
						</button>
					</div>
				</g:form>

			</div><!-- .twelve -->

            <div class="four wide column">
                <g:render template="../templates/sideMenu" />
            </div><!-- .four -->

		</div><!-- .grid -->
	</body>
</html>
