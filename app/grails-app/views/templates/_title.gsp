<div class="la-icon-list">
  <g:if test="${item?.tipp?.title instanceof com.k_int.kbplus.BookInstance && item?.tipp?.title?.volume}">
    <div class="item">
      <i class="grey icon la-books la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
      <div class="content">
        ${message(code: 'title.volume.label')} ${item?.tipp?.title?.volume}
      </div>
    </div>
  </g:if>
  <g:if test="${item?.tipp?.title instanceof com.k_int.kbplus.BookInstance && (item?.tipp?.title?.firstAuthor || item?.tipp?.title?.firstEditor)}">
    <div class="item">
      <i class="grey icon user circle la-popup-tooltip la-delay" data-content="${message(code: 'author.slash.editor')}"></i>
      <div class="content">
        ${item?.tipp?.title?.getEbookFirstAutorOrFirstEditor()}
      </div>
    </div>
  </g:if>

  <g:if test="${item?.tipp?.title instanceof com.k_int.kbplus.BookInstance}">
    <div class="item">
      <i class="grey icon copy la-popup-tooltip la-delay" data-content="${message(code: 'title.editionStatement.label')}"></i>
      <div class="content">
        ${item?.tipp?.title?.editionStatement}
      </div>
    </div>
  </g:if>
</div>
<g:each in="${item?.tipp?.title?.ids?.sort { t?.identifier?.ns?.ns }}" var="title_id">
  <g:if test="${title_id.identifier.ns.ns.toLowerCase() != 'originediturl'}">
    <span class="ui small teal image label">
      ${title_id.identifier.ns.ns}: <div
            class="detail">${title_id.identifier.value}</div>
    </span>
  </g:if>
</g:each>
<br/>

<!--                  ISSN:<strong>${item?.tipp?.title?.getIdentifierValue('ISSN') ?: ' - '}</strong>,
                  eISSN:<strong>${item?.tipp?.title?.getIdentifierValue('eISSN') ?: ' - '}</strong><br/>-->


<div class="la-icon-list">
  <g:if test="${item.availabilityStatus?.getI10n('value')}">
    <div class="item">
      <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.access.label', default: 'Access')}"></i>
      <div class="content">
        ${item.availabilityStatus?.getI10n('value')}
      </div>
    </div>
  </g:if>

  <g:if test="${item?.tipp?.pkg?.id}">
    <div class="item">
      <i class="grey icon gift scale la-popup-tooltip la-delay" data-content="${message(code: 'tipp.package', default: 'Package')}"></i>
      <div class="content">
        <g:link controller="package" action="show"
                id="${item?.tipp?.pkg?.id}">${item?.tipp?.pkg?.name}</g:link>
      </div>
    </div>
  </g:if>

  <g:if test="${item.tipp?.platform.name}">
    <div class="item">
      <i class="grey icon cloud la-popup-tooltip la-delay" data-content="${message(code: 'tipp.platform', default: 'Platform')}"></i>
      <div class="content">
        <g:if test="${item.tipp?.platform.name}">
          <g:link controller="platform" action="show"
                  id="${item.tipp?.platform.id}">
            ${item.tipp?.platform.name}
          </g:link>
        </g:if>
        <g:else>
          ${message(code: 'default.unknown')}
        </g:else>
      </div>
    </div>
  </g:if>

  <g:else>${message(code: 'default.unknown')}</g:else>

  <g:if test="${item?.availabilityStatus?.value == 'Expected'}">
    ${message(code: 'default.on', default: 'on')} <g:formatDate
        format="${message(code: 'default.date.format.notime')}"
        date="${ie.accessStartDate}"/>
  </g:if>

  <g:if test="${item?.availabilityStatus?.value == 'Expired'}">
    ${message(code: 'default.on', default: 'on')} <g:formatDate
      format="${message(code: 'default.date.format.notime')}"
      date="${item.accessEndDate}"/>
  </g:if>

  <g:if test="${item?.tipp?.id}">
    <div class="la-title">${message(code: 'default.details.label')}</div>
    <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
        data-content="${message(code: 'laser')}"
        href="${item?.tipp?.hostPlatformURL.contains('http') ? item?.tipp?.hostPlatformURL : 'http://' + item.tipp?.hostPlatformURL}"
        target="_blank"
        controller="tipp" action="show"
        id="${item?.tipp?.id}">
    <i class="book icon"></i>
    </g:link>
  </g:if>

  <g:each in="${apisources}" var="gokbAPI">
    <g:if test="${item?.tipp?.gokbId}">
      <a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
       data-content="${message(code: 'gokb')}"
       href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + item?.tipp?.gokbId : '#'}"
       target="_blank"><i class="la-gokb  icon"></i>
      </a>
    </g:if>
  </g:each>

</div>



