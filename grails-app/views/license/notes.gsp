<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'license.nav.notes')}</title>
</head>
<body>
    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:h1HeaderWithIcon>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </semui:h1HeaderWithIcon>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <laser:render template="/templates/notes/table" model="${[instance: license, redirect: 'notes']}"/>

</body>
</html>
