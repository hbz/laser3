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

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <laser:render template="/templates/notes/table" model="${[instance: license, redirect: 'notes']}"/>

</body>
</html>
