<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : Cache Info</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Cache Info" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header"><semui:headerIcon />Cache Info</h1>

<g:each in="${cacheManager.getCacheNames()}" var="cacheName">
    <h3 class="ui header">${cacheName}</h3>

    <g:set var="cache" value="${cacheManager.getCache(cacheName)}" />
    <pre>${cache}</pre>

    <% cache.put("test-entry", new Date()) %>
    <ul>
        <g:each in="${cache.allKeys}" var="key">
            <li>${key} : ${cache.getNativeCache().get(key)}</li>
        </g:each>
    </ul>
</g:each>

</body>
</html>
