<!doctype html>
<html>
<head>
    <meta name="layout" content="public"/>
    <title>Freedom of Information Policy | ${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
    <g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

    <div class="ui container">
        <h1 class="ui header">Freedom of Information Policy</h1>

        <div class="ui grid">
            <div class="twelve wide column">
                <markdown:renderHtml><g:dbContent key="kbplus.freedomOfInformationPolicy"/></markdown:renderHtml>
            </div>

            <div class="four wide column">
                <g:render template="/templates/loginDiv"/>
            </div>
        </div>
    </div>
</body>
</html>
