<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label', default:'License')}</title>
</head>
<body>
    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui header"><semui:headerIcon />

    %{--${license.licensee?.name}--}%
    ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
    <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />

    <g:render template="/templates/notes/table" model="${[instance: license, redirect: 'notes']}"/>

    <g:render template="/templates/notes/modal_create" model="${[doclist: license.documents, ownobj: license, owntp: 'license']}"/>

</body>
</html>
