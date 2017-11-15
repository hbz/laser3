<!doctype html>
<html>
<head>
    <meta name="layout" content="pubbootstrap"/>
    <title>Terms and Conditions | ${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
<g:render template="public_navbar" contextPath="/templates" model="['active': 'about']"/>

<div>
    <h1>Terms and Conditions</h1>
</div>

<div>
    <div class="row">
        <div class="span8">
            <markdown:renderHtml><g:dbContent key="kbplus.termsAndConditions"/></markdown:renderHtml>
        </div>

        <div class="span4">
            <g:render template="/templates/loginDiv"/>
        </div>
    </div>
</div>
</body>
</html>
