<div class="control-group">
  <label class="control-label" for="links">${message(code:'org.links.label', default:'Org Links')}</label>
  <div class="controls" style="display:flex">
    <g:if test="${orgInstance.links && orgInstance.links.size() > 0}">
      <ul>
        <g:each in="${orgInstance.links}" var="ol">
          <li>
            <g:if test="${ol.pkg?.id}">
              <g:link controller="package" action="show" id="${ol.pkg.id}" >${ol.pkg.name} (${message(code:'package.label')})</g:link>
            </g:if>
            <g:elseif test="${ol.sub?.id}">
              <g:link controller="subscription" action="show" id="${ol.sub.id}" >${ol.sub.name ?: ol.sub.id} (${message(code:'subscription.label')})</g:link>
            </g:elseif>
            <g:elseif test="${ol.lic?.id}">
              <g:link controller="license" action="show" id="${ol.lic.id}" >${ol.lic.reference ?: ol.lic.id} (${message(code:'license.label')})</g:link>
            </g:elseif>
            <g:elseif test="${ol.cluster?.id}">
              <g:link controller="cluster" action="show" id="${ol.cluster.id}" >${ol.cluster.name ?: ol.cluster.id} (${message(code:'cluster.label')})</g:link>
            </g:elseif>
            <g:elseif test="${ol.title?.id}">
              <g:link controller="titleInstance" action="show" id="${ol.title.id}" >${ol.title.title ?: ol.title.id} (${message(code:'title.label')})</g:link>
            </g:elseif>
            <g:else>
              ${ol}
            </g:else>
            (${ol.roleType.getI10n('value')})
          </li>
        </g:each>
      </ul>
    </g:if>
  </div>
</div>
