<!doctype html>
<laser:serviceInjection />
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'default.notes.label')}</title>
    </head>
    <body>
        <semui:breadcrumbs>
            <g:if test="${!inContextOrg}">
                <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
            </g:if>
        </semui:breadcrumbs>
        <semui:controlButtons>
            <g:render template="actions" model="[org:org]"/>
        </semui:controlButtons>

        <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />${orgInstance.name}</h1>

        <g:render template="nav" model="${[orgInstance:orgInstance,inContextOrg:inContextOrg]}"/>

        <semui:messages data="${flash}" />

        <g:render template="/templates/notes/table" model="${[instance: orgInstance, redirect: 'notes']}"/>

  </body>
</html>
