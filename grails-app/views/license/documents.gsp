<laser:htmlStart message="license.nav.docs" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}">
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>

    <ui:anualRings object="${license}" controller="license" action="documents" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/documents/table" model="${[instance:license, redirect:'documents',owntp:'license']}"/>

<laser:htmlEnd />
