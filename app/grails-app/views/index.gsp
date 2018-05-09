<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} Data import explorer</title>
</head>

<body>
<div>
    <p>
        ${message(code: 'laser', default: 'LAS:eR')} Data explorer. Use the links above to navigate the data items imported by the ${message(code: 'laser', default: 'LAS:eR')} import process and validate the data.
    </p>

    <p>
        Currently, you can review:

    <ul class="nav">
        <li><g:link controller="packageDetails">Packages</g:link></li>
        <li><g:link controller="org">Organisations</g:link></li>
        <li><g:link controller="platform">Platforms</g:link></li>
        <li><g:link controller="titleDetails">Title Instances</g:link></li>
        <li><g:link controller="titleInstancePackagePlatform">Title Instance Package Platform Links</g:link></li>
        <li><g:link controller="subscriptionDetails">Subscriptions</g:link></li>
        <li><g:link controller="licenseDetails"><g:message code="license" default="License"/>s</g:link></li>
    </ul>
</p>
</div>
</body>
</html>
