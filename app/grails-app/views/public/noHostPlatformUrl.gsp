<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>No Host Platform URL | ${message(code: 'laser')}</title>
</head>

<body class="public">
    <g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

    <div class="ui container">
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />No Host Platform URL</h1>

        <div class="ui grid">
            <div class="twelve wide column">
                <markdown:renderHtml><g:dbContent key="kbplus.noHostPlatformURL"/></markdown:renderHtml>
            </div>
        </div>
    </div>
</body>
</html>
