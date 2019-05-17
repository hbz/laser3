<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label')}</title>
</head>

<body>
    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <h1 class="ui left aligned icon header"><semui:headerIcon /></h1>

    <g:render template="nav" />

    <g:each in="${stats}" var="stat">
        <strong>${stat.key}</strong> <br />

        <g:each in="${stat.value}" var="entry">
           ${entry.hasProperty('id') ? '[ ' + entry.id + ' ] ' : ''} ${entry} <br />
        </g:each>
        <br />
    </g:each>
</body>
</html>
