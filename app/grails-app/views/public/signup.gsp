<!doctype html>
<html>
<head>
    <meta name="layout" content="pubbootstrap"/>
    <title>${message(code: 'public.nav.signUp.label', default: 'Sign Up')} | ${message(code: 'laser', default: 'LAS:eR')}</title>
</head>

<body class="public">
<g:render template="public_navbar" contextPath="/templates" model="['active': 'signup']"/>

<div>
    <h1>${message(code: 'public.signUp.label', default: 'How can institutions get involved?')}</h1>
</div>

<div>
    <div class="row">
        <div class="span8">
            <markdown:renderHtml><g:dbContent key="kbplus.signup.text"/></markdown:renderHtml>
        </div>

        <div class="span4">
            <g:render template="/templates/loginDiv"/>
        </div>
    </div>
</div>
</body>
</html>
