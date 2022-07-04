<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'license.nav.docs')}</title>
</head>

<body>

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:headerWithIcon>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </semui:headerWithIcon>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <laser:render template="/templates/documents/table" model="${[instance:license, redirect:'documents',owntp:'license']}"/>

</body>
</html>
