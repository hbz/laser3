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


<h3 class="ui header">Session</h3>

<div class="ui segment">
    ${session.id}
</div>



<h3 class="ui header">Hibernate <span class="ui label">${hibernateSession}</span></h3>

<div class="ui segment">
    <g:each in="${hibernateSession.statistics}" var="hst">
        ${hst} <br/>
    </g:each>
</div>



<h3 class="ui header">Ehcache <span class="ui label">${ehcacheManager.class}</span></h3>

<g:each in="${ehcacheManager.getCacheNames()}" var="cacheName">
    <g:set var="cache" value="${ehcacheManager.getCache(cacheName)}" />

    <h4 class="ui header">${cacheName} <span class="ui label">${cache.class}</span></h4>

    <div class="ui segment">
        ${cache}

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

    </div>
</g:each>



<h3 class="ui header">Plugin-Cache ; not expiring <span class="ui label">${plugincacheManager.class}</span></h3>

<g:each in="${plugincacheManager.getCacheNames()}" var="cacheName">
    <g:set var="cache" value="${plugincacheManager.getCache(cacheName)}" />

    <h4 class="ui header">${cacheName} <span class="ui label">${cache.class}</span></h4>

    <div class="ui segment">
        ${cache}

        <ul>
            <g:each in="${cache.allKeys}" var="key">
                <g:set var="cacheEntry" value="${cache.getNativeCache().get(key)}" />
                <li>${key} >> ${cacheEntry}</li>
            </g:each>
        </ul>

        <g:link class="ui button negative"
                controller="yoda" action="cacheInfo" params="[cmd: 'clearCache', cache: cacheName, type: 'cache']">Cache löschen</g:link>

    </div>
</g:each>


</body>
</html>