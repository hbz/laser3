<!doctype html>
<html>
<head>
    <meta name="layout" content="pubbootstrap"/>
    <title>${message(code:'public.about.label', default:'About')} | ${message(code:'laser', default:'LAS:eR')}</title>
</head>

<body class="public">
<g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

<div class="container">
    <h1>${message(code:'public.nav.about.label', default:'About LAS:eR')}</h1>
</div>

<div class="container">
    <div class="row">
        <div class="span8">
            <markdown:renderHtml><g:dbContent key="kbplus.about.text"/></markdown:renderHtml>
        </div>

        <div class="span4">
            <g:render template="/templates/loginDiv"/>
        </div>
    </div>
</div>
</body>
</html>
