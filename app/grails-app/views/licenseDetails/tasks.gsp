<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label', default:'License')}</title>
</head>

<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui header"><semui:headerIcon />

        ${license.licensee?.name}
        ${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />

    <semui:messages data="${flash}" />


    <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList]}"/>


    <g:render template="/templates/tasks/modal_create" model="${[ownobj:license, owntp:'license']}"/>

</body>
</html>
