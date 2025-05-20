<laser:htmlStart message="default.notes.label" />

        <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

        <ui:controlButtons>
            <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="[org:org]"/>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon text="${orgInstance.name}" type="${orgInstance.getCustomerType()}">
                <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
        </ui:h1HeaderWithIcon>

        <laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: orgInstance, redirect: 'notes']}"/>

<laser:htmlEnd />
