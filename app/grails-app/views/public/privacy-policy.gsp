<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>Privacy Policy | ${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
    <g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

    <div class="ui container">
        <h1 class="ui header">Privacy Policy</h1>

        <div class="ui grid">
            <div class="twelve wide column">
                <markdown:renderHtml><g:dbContent key="kbplus.privacyPolicy"/></markdown:renderHtml>
            </div>

            <div class="four wide column">
                <g:render template="/templates/loginDiv"/>
            </div>
        </div>
    </div>
</body>
</html>
