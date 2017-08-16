<!doctype html>
<html>
<head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} Data import explorer</title>
</head>

<body>
<div class="row-fluid">
    <p>
        ${message(code: 'laser', default: 'LAS:eR')} Data explorer. Use the links above to navigate the data items imported by the ${message(code: 'laser', default: 'LAS:eR')} import process and validate the data.
    </p>

    <p>
        Currently, you can review:

    <ul class="nav">
        <li><g:link controller="package">Packages</g:link></li>
        <li><g:link controller="org">Organisations</g:link></li>
        <li><g:link controller="platform">Platforms</g:link></li>
        <li><g:link controller="titleInstance">Title Instances</g:link></li>
        <li><g:link controller="titleInstancePackagePlatform">Title Instance Package Platform Links</g:link></li>
        <li><g:link controller="subscription">Subscriptions</g:link></li>
        <li><g:link controller="license"><g:message code="license" default="License"/>s</g:link></li>
    </ul>
</p>
</div>
</body>
</html>
