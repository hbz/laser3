<%@ page import="de.laser.ui.Btn; de.laser.CacheService; groovy.json.JsonBuilder; de.laser.utils.DateUtils" %>

<laser:htmlStart message="menu.yoda.systemCache" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemCache" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.systemCache" type="yoda" />

<g:set var="sessionCache" value="${contextService.getSessionCache()}" />
<h2 class="ui header">SessionCache <span class="ui label">${sessionCache.getSession().class}</span></h2>

<div class="ui fluid card">
    <div class="content">
        <p>ID: ${sessionCache.getSession().id}</p>

        <g:link class="${Btn.SIMPLE} small" controller="yoda" action="systemCache" params="[cmd:'clearCache', type:'session']">Cache leeren</g:link>

        <g:if test="${sessionCache.list().size() > 0}">
            <dl>
                <g:each in="${contextService.getSessionCache().list()}" var="entry">
                    <dt style="margin-top:0.5em">
                        <g:link controller="yoda" action="systemCache" params="${[cmd:'get', type:'session', key:entry.key]}" target="_blank"><icon:database /> ${entry.key}</g:link>
                    </dt>
                    <dd>
                         ${entry.value} <em>(${entry.value.class?.name})</em>
                    </dd>
                </g:each>
            </dl>
        </g:if>
    </div>
</div>

<hr />

<h2 class="ui header">Ehcache <span class="ui label">${ehcacheManager.class}</span></h2>

<%
    List ehCaches = [
        ehcacheManager.getCacheNames().findAll { it -> !it.startsWith('de.laser.')},
        ehcacheManager.getCacheNames().findAll { it -> it.startsWith('de.laser.')}
    ]
    int cacheContentIdx = 0
%>

<g:each in="${ehCaches}" var="ehCache">

    <g:each in="${ehCache.toSorted()}" var="cacheName">
        <g:set var="cache" value="${ehcacheManager.getCache(cacheName)}" />
        <g:set var="cacheStats" value="${cache.getStatistics()}" />

        <h3 class="ui icon header">
            ${cacheName}

            <span class="ui label">
                ${cache.class} (
                ttl: ${cache.getCacheConfiguration().getAt('timeToLiveSeconds') / 60} minutes,
                hitCount: ${Math.max(cacheStats.cacheHitCount(), cacheStats.localHeapHitCount())},
                <g:if test="${cacheService.getDiskStorePath(cache.getCacheManager())}">
                    disk: ${cacheStats.getLocalDiskSize()} kb
                </g:if>
                <g:else>
                    heap: ${cacheStats.getLocalHeapSize()} kb
                </g:else>
                )
            </span>
        </h3>

        <div class="ui fluid card">
            <div class="content">

                <g:link class="${Btn.SIMPLE} small" controller="yoda" action="systemCache" params="[cmd:'clearCache', type: 'ehcache', cache:cacheName]">Cache leeren</g:link>

                <button class="${Btn.SIMPLE} small" onclick="$(this).parent('.content').parent('.card').find('.cacheConfig').toggleClass('hidden')">Konfiguration</button>

                <g:if test="${cache.getKeysWithExpiryCheck().size() > 0}">
                    <button class="${Btn.POSITIVE} small" onclick="$(this).parent('.content').parent('.card').find('.cacheContent').toggleClass('hidden')">Elemente: ${cache.getKeys().size()}</button>

                    <div class="cacheContent hidden">
                        <dl>
                        <g:each in="${cache.getKeys().toSorted()}" var="key">
                            <g:set var="element" value="${cache.get(key)}" />
                            <g:if test="${element}">
                                <dt style="margin-top: 0.5em;">
                                    <g:set var="ceKey" value="${element.getObjectKey() instanceof String ? element.getObjectKey() : element.getObjectKey().id}" />
                                    <a href="#" class="cacheContent-toggle" data-cc="${++cacheContentIdx}"><i class="icon list alternate outline"></i>${ceKey}</a> -
                                    creation: ${DateUtils.getSDF_onlyTime().format(element.getCreationTime())},
                                    lastAccess: ${DateUtils.getSDF_onlyTime().format(element.getLastAccessTime())},
                                    version: ${element.version},
                                    hitCount: ${element.hitCount}
                                </dt>
                                <dd style="display:none" data-cc="${cacheContentIdx}">
                                    <g:set var="objectValue" value="${element.getObjectValue()}" />
                                    <g:if test="${objectValue.getClass().getSimpleName() != 'Item'}">
                                        ${objectValue}
                                    </g:if>
                                    <g:else>
                                        ${new JsonBuilder(objectValue.getValue()).toString()}
                                    </g:else>
                                </dd>
                            </g:if>
                        </g:each>
                        </dl>
                    </div>

                </g:if>
            </div>

            <div class="content cacheConfig hidden">
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

<div class="ui fluid card">
    <div class="content">
        <g:each in="${hibernateSession.statistics}" var="hst">
            ${hst} <br />
        </g:each>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.cacheContent-toggle').click( function() {
        event.preventDefault();
        $('dd[data-cc=' + $(this).attr('data-cc') + ']').toggle();
    });
</laser:script>

<laser:htmlEnd />