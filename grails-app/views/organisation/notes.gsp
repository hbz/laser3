<laser:htmlStart message="default.notes.label" />

        <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

        <ui:controlButtons>
            <laser:render template="actions" model="[org:org]"/>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon text="${orgInstance.name}">
                <g:if test="${isMyOrg}">
                        <laser:render template="/templates/iconObjectIsMine"/>
                </g:if>
        </ui:h1HeaderWithIcon>

        <laser:render template="nav" model="${[orgInstance:orgInstance,inContextOrg:inContextOrg]}"/>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: orgInstance, redirect: 'notes']}"/>

<laser:htmlEnd />
