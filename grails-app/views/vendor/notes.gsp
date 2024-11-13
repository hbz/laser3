<laser:htmlStart message="default.notes.label" />

        <laser:render template="breadcrumb"
              model="${[vendor: vendor]}"/>

        <ui:controlButtons>
            <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="[vendor: vendor]"/>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon text="${vendor.name}">
                <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor: isMyVendor]}"/>
        </ui:h1HeaderWithIcon>

        <laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[vendor: vendor]}"/>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: vendor, redirect: 'notes']}"/>

<laser:htmlEnd />
