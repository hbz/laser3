<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} License</title>
</head>

<body>

    <div>
      <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>
    </div>

<div>
    <h1>${license.licensee?.name} ${license.type?.value} License : <span id="reference" style="padding-top: 5px;">${license.reference}</span></h1>

    <g:render template="nav" />

</div>

<div>
    <g:render template="/templates/documents_table" model="${[instance:license, redirect:'documents']}"/>
</div>
<g:render template="/templates/addDocument" model="${[ownobj:license, owntp:'license']}" />

</body>
</html>
