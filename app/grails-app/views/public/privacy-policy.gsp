<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>Privacy Policy | ${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
    <g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

    <div class="ui container">
        <h1 class="ui left aligned icon header"><semui:headerIcon />Privacy Policy</h1>

        <div class="ui grid">
            <div class="twelve wide column">
                <markdown:renderHtml><g:dbContent key="kbplus.privacyPolicy"/></markdown:renderHtml>
            </div>

            <aside class="four wide column">
                <g:render template="/templates/loginDiv"/>
            </aside>
        </div>
    </div>
</body>
</html>
