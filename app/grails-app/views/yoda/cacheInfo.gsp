<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.yoda.cacheInfo')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.cacheInfo" class="active"/>
</semui:breadcrumbs>



<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.yoda.cacheInfo')}</h1>

<% /* --------------------------------------------------------------------------------- */ %>

<h3 class="ui header">Session</h3>

${session.id}

<hr />

<% /* --------------------------------------------------------------------------------- */ %>

<h3 class="ui header">Hibernate // ${hibernateSession}</h3>

<g:each in="${hibernateSession.statistics}" var="hst">
    ${hst} <br/>
</g:each>

<hr />

<% /* --------------------------------------------------------------------------------- */ %>

<h3 class="ui header">Ehcache // ${ehcacheManager.class}</h3>

<g:each in="${ehcacheManager.getCacheNames()}" var="cacheName">
    <g:set var="cache" value="${ehcacheManager.getCache(cacheName)}" />

    <h4 class="ui header">${cacheName}: ${cache.class}</h4>
    <p>${cache}</p>

    <dl>
        <g:each in="${cache.getKeys()}" var="key">
            <g:if test="${cache.get(key)}">
                <dt>${key}</dt>
                <dd>${cache.get(key)?.getObjectValue()}</dd>
                <br />
            </g:if>
        </g:each>
    </dl>

    <g:link class="ui button negative"
            controller="yoda" action="cacheInfo" params="[cmd: 'clearCache', cache: cacheName, type: 'ehcache']">Cache löschen</g:link>

    <hr />
</g:each>

<% /* --------------------------------------------------------------------------------- */ %>

<h3 class="ui header">Plugin-Cache ; not expiring // ${plugincacheManager.class}</h3>

<g:each in="${plugincacheManager.getCacheNames()}" var="cacheName">
    <g:set var="cache" value="${plugincacheManager.getCache(cacheName)}" />

    <h4 class="ui header">${cacheName} ${cache.class}</h4>
    <pre>${cache}</pre>

    <ul>
        <g:each in="${cache.allKeys}" var="key">
            <g:set var="cacheEntry" value="${cache.getNativeCache().get(key)}" />
            <li>${key} >> ${cacheEntry}</li>
        </g:each>
    </ul>

    <g:link class="ui button negative"
            controller="yoda" action="cacheInfo" params="[cmd: 'clearCache', cache: cacheName, type: 'cache']">Cache löschen</g:link>

    <hr />
</g:each>


</body>
</html>