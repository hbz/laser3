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



<h2 class="ui header">Hibernate</h2>

    <g:each in="${hibernateStats}" var="hst">
        ${hibernateStats} <br/>
    </g:each>



<h2 class="ui header">CacheManager</h2>

    <g:each in="${cacheManager.getCacheNames()}" var="cacheName">
        <h4 class="ui header">${cacheName}</h4>

        <g:set var="cache" value="${cacheManager.getCache(cacheName)}" />
        <pre>${cache} : ${cache.nativeCache.cacheConfiguration}</pre>

        <ul>
            <g:each in="${cache.allKeys}" var="key">
                <g:set var="cacheEntry" value="${cache.getNativeCache().get(key)}" />
                <li>${key} >> ${cacheEntry} >> TTL: ${cacheEntry.timeToLiveSeconds}</li>
            </g:each>
        </ul>

        <g:link class="ui button negative"
                controller="yoda" action="cacheInfo" params="[cmd: 'clearCache', cache: cacheName]">Cache löschen</g:link>
    </g:each>

</body>
</html>
