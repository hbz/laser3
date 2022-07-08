<laser:htmlStart message="default.notes.label" />

        <semui:breadcrumbs>
            <g:if test="${!inContextOrg}">
                <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
            </g:if>
        </semui:breadcrumbs>
        <semui:controlButtons>
            <laser:render template="actions" model="[org:org]"/>
        </semui:controlButtons>

        <semui:h1HeaderWithIcon text="${orgInstance.name}" />

        <laser:render template="nav" model="${[orgInstance:orgInstance,inContextOrg:inContextOrg]}"/>

        <semui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: orgInstance, redirect: 'notes']}"/>

  <laser:htmlEnd />
