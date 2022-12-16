<%@ page import="de.laser.utils.DateUtils" %>

<laser:htmlStart message="menu.yoda.systemCache" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemCache" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.systemCache" type="yoda" />

<g:set var="sessionCache" value="${contextService.getSessionCache()}" />
<h2 class="ui header">SessionCache <span class="ui label">${sessionCache.getSession().class}</span></h2>

<div class="ui segment">
    <p>ID: ${sessionCache.getSession().id}</p>

    <g:link class="ui button small"
            controller="yoda" action="systemCache" params="[cmd: 'clearCache', type: 'session']">Cache leeren</g:link>

    <g:if test="${sessionCache.list().size() > 0}">
        <dl>
            <g:each in="${contextService.getSessionCache().list()}" var="entry">
                <dt style="margin-top:0.5em">
                    <g:link controller="yoda" action="systemCache" params="${[key: entry.key]}" target="_blank"><i class="icon list alternate outline"></i> ${entry.key}</g:link>
                </dt>
                <dd>
                     ${entry.value} <em>(${entry.value.class?.name})</em>
                </dd>
            </g:each>
        </dl>
    </g:if>

</div>

<hr />

<h2 class="ui header">Ehcache <span class="ui label">${ehcacheManager.class}</span></h2>

<%
    List ehCaches = [
        ehcacheManager.getCacheNames().findAll { it -> !it.startsWith('de.laser.')},
        ehcacheManager.getCacheNames().findAll { it -> it.startsWith('de.laser.')}
    ]
%>

<g:each in="${ehCaches}" var="ehCache">

    <g:each in="${ehCache.toSorted()}" var="cacheName">
        <g:set var="cache" value="${ehcacheManager.getCache(cacheName)}" />
        <g:set var="cacheStats" value="${cache.getStatistics()}" />

        <h3 class="ui header">${cacheName}
            <span class="ui label">
                ${cache.class} (
                ttl: ${cache.getCacheConfiguration().getAt('timeToLiveSeconds') / 60} minutes,
                hitCount: ${cacheStats.cacheHitCount()},
                <g:if test="${cacheService.getDiskStorePath(cache.getCacheManager())}">
                    disk: ${cacheStats.getLocalDiskSize()}kb
                </g:if>
                <g:else>
                    heap: ${cacheStats.getLocalHeapSize()}kb
                </g:else>
                )
            </span>
        </h3>

        <div class="ui segments">

            <div class="ui segment">

                <g:link class="ui button small" controller="yoda" action="systemCache" params="[cmd: 'clearCache', cache: cacheName, type: 'ehcache']">Cache leeren</g:link>

                <button class="ui button small" onclick="$(this).parent('.segment').next('.cacheConfig').toggleClass('hidden')">Konfiguration</button>

                <g:if test="${cache.getKeysWithExpiryCheck().size() > 0}">
                    <button class="ui button small positive" onclick="$(this).parent('.segment').find('.cacheContent').toggleClass('hidden')">Elemente: ${cache.getKeys().size()}</button>

                    <div class="cacheContent hidden">
                        <dl>
                        <g:each in="${cache.getKeys().toSorted()}" var="key">
                            <g:set var="element" value="${cache.get(key)}" />
                            <g:if test="${element}">
                                <dt>
                                    <g:set var="ceKey" value="${element.getObjectKey() instanceof String ? element.getObjectKey() : element.getObjectKey().id}" />
                                    ${ceKey} -
                                    creation: ${DateUtils.getSDF_onlyTime().format(element.getCreationTime())},
                                    lastAccess: ${DateUtils.getSDF_onlyTime().format(element.getLastAccessTime())},
                                    version: ${element.version},
                                    hitCount: ${element.hitCount}
                                </dt>
                                <dd>
                                    <g:set var="objectValue" value="${element.getObjectValue()}" />
                                    <g:if test="${objectValue.getClass().getSimpleName() != 'Item'}">
                                        ${objectValue}
                                    </g:if>
                                    <g:else>
                                        ${objectValue.getValue()}
                                    </g:else>
                                </dd>
                            </g:if>
                        </g:each>
                        </dl>
                    </div>

                </g:if>
            </div>

            <div class="ui secondary segment cacheConfig hidden">
                <%
                    Map<String, Object> cacheConfig = cache.getCacheConfiguration().getProperties().findAll {
                        it.value instanceof Number || it.value instanceof Boolean || it.value instanceof String
                    }.sort{ it.key.toLowerCase() }

                    print cacheConfig.collect{ "${it.key} = ${it.value}" }
                %>
            </div>

        </div>
    </g:each>

</g:each>

<hr />

<h2 class="ui header">Hibernate <span class="ui label">${hibernateSession.class}</span></h2>

<div class="ui segment">
    <g:each in="${hibernateSession.statistics}" var="hst">
        ${hst} <br />
    </g:each>
</div>

<hr />

<laser:htmlEnd />