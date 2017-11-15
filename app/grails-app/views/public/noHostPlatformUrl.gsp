<!doctype html>
<html>
<head>
    <meta name="layout" content="pubbootstrap"/>
    <title>No Host Platform URL | ${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
<g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

<div>
    <h1>No Host Platform URL</h1>
</div>

<div>
    <div class="row">
        <div class="span8">
            <markdown:renderHtml><g:dbContent key="kbplus.noHostPlatformURL"/></markdown:renderHtml>
        </div>
    </div>
</div>
</body>
</html>
