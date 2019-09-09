<div class="ui grid">
  <div class="sixteen wide column">
    <semui:listIcon type="${ie.tipp?.title?.type?.value}"/>
    <g:link controller="issueEntitlement" id="${ie.id}"
            action="show"><strong>${ie.tipp.title.title}</strong>
    </g:link>
    <g:if test="${ie.tipp?.hostPlatformURL}">
      <a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
      <%-- data-content="${message(code: 'tipp.tooltip.callUrl')}" --%>
         data-content="${ie.tipp?.platform.name}"

         href="${ie.tipp?.hostPlatformURL.contains('http') ? ie.tipp?.hostPlatformURL : 'http://' + ie.tipp?.hostPlatformURL}"
         target="_blank"><i class="cloud icon"></i></a>
    </g:if>
    <br>
    <g:each in="${ie?.tipp?.title?.ids?.sort { it?.identifier?.ns?.ns }}" var="title_id">
      <g:if test="${title_id.identifier.ns.ns.toLowerCase() != 'originediturl'}">
        <span class="ui small teal image label">
          ${title_id.identifier.ns.ns}: <div
                class="detail">${title_id.identifier.value}</div>
        </span>
      </g:if>
    </g:each>
    <br/>
    <!--                  ISSN:<strong>${ie?.tipp?.title?.getIdentifierValue('ISSN') ?: ' - '}</strong>,
                      eISSN:<strong>${ie?.tipp?.title?.getIdentifierValue('eISSN') ?: ' - '}</strong><br/>-->
    <div class="la-icon-list">

      <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance && ie?.tipp?.title?.volume}">
      <div class="item">
        <i class="grey icon la-books la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
        <div class="content">
          ${message(code: 'title.volume.label')} ${ie?.tipp?.title?.volume}
        </div>
      </div>

      (${message(code: 'title.volume.label')} ${ie?.tipp?.title?.volume})
    </g:if>

      <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance && (ie?.tipp?.title?.firstAuthor || ie?.tipp?.title?.firstEditor)}">
      <div class="item">
        <i class="grey icon user circle la-popup-tooltip la-delay" data-content="${message(code: 'author.slash.editor')}"></i>
        <div class="content">
          ${ie?.tipp?.title?.getEbookFirstAutorOrFirstEditor()}
        </div>
      </div>
    </g:if>

      <g:if test="${ie?.tipp?.title instanceof com.k_int.kbplus.BookInstance}">
      <div class="item">
        <i class="grey icon copy la-popup-tooltip la-delay" data-content="${message(code: 'title.editionStatement.label')}"></i>
        <div class="content">
          ${ie?.tipp?.title?.editionStatement}
        </div>
      </div>
    </g:if>

      <g:if test="${ie.availabilityStatus?.getI10n('value')}">
      <div class="item">
        <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.access.label', default: 'Access')}"></i>
        <div class="content">
          ${ie.availabilityStatus?.getI10n('value')}
        </div>
      </div>
    </g:if>

      <g:if test="${ie?.tipp?.pkg?.id}">
      <div class="item">
        <i class="grey icon gift scale la-popup-tooltip la-delay" data-content="${message(code: 'tipp.package', default: 'Package')}"></i>
        <div class="content">
          <g:link controller="package" action="show"
                  id="${ie?.tipp?.pkg?.id}">${ie?.tipp?.pkg?.name}</g:link>
        </div>
      </div>
    </g:if>

      <g:if test="${ie.tipp?.platform.name}">
      <div class="item">
        <i class="grey icon cloud la-popup-tooltip la-delay" data-content="${message(code: 'tipp.platform', default: 'Platform')}"></i>
        <div class="content">
          <g:link controller="platform" action="show"
                  id="${ie.tipp?.platform.id}">
            ${ie.tipp?.platform.name}
          </g:link>
        </div>
      </div>
    </g:if>
    <g:else>${message(code: 'default.unknown')}</g:else>

      <g:if test="${ie.availabilityStatus?.value == 'Expected'}">
          ${message(code: 'default.on', default: 'on')} <g:formatDate
                format="${message(code: 'default.date.format.notime')}"
                date="${ie.accessStartDate}"/>
        </g:if>
      <g:if test="${ie.availabilityStatus?.value == 'Expired'}">
          ${message(code: 'default.on', default: 'on')} <g:formatDate
                format="${message(code: 'default.date.format.notime')}"
                date="${ie.accessEndDate}"/>
      </g:if>

      <g:if test="${ie.tipp.id}">
        <div class="la-title">${message(code: 'default.details.label')}</div>
        <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                data-content="${message(code: 'laser')}"
                href="${ie.tipp?.hostPlatformURL.contains('http') ? ie.tipp?.hostPlatformURL : 'http://' + ie.tipp?.hostPlatformURL}"
                target="_blank"
                controller="tipp" action="show"
                id="${ie.tipp.id}">
          <i class="book icon"></i>
        </g:link>
      </g:if>
      <g:each in="${apisources}" var="gokbAPI">
        <g:if test="${ie?.tipp?.gokbId}">
          <a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
             data-content="${message(code: 'gokb')}"
             href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + ie?.tipp?.gokbId : '#'}"
             target="_blank"><i class="la-gokb  icon"></i>
          </a>
        </g:if>
      </g:each>
    </div>
  </div>

</div>

