<g:if test="${filtered}">
	<span class="label label-info">${filtered}</span>
</g:if>
  <ul>
   <g:each in="${hits}" var="hit">
      <li>
      	<g:if test="${!filtered}">
            <g:if test="${hit.getSource().rectype == 'action'}"><span class="label label-info"><g:message
                    code="default.action.label" default="Action"/></span></g:if>
            <g:if test="${hit.getSource().rectype == 'Organisation'}"><span class="label label-info"><g:message
                    code="org" default="Organisation"/></span></g:if>
            <g:if test="${hit.getSource().rectype == 'TitleInstance'}"><span class="label label-info"><g:message
                    code="title" default="Title Instance"/></span></g:if>
            <g:if test="${hit.getSource().rectype == 'Package'}"><span class="label label-info"><g:message
                    code="package" default="Package"/></span></g:if>
            <g:if test="${hit.getSource().rectype == 'Platform'}"><span class="label label-info"><g:message
                    code="platform" default="Platform"/></span></g:if>
            <g:if test="${hit.getSource().rectype == 'Subscription'}"><span class="label label-info"><g:message
                    code="subscription" default="Subscription"/></span></g:if>
            <g:if test="${hit.getSource().rectype == 'License'}"><span class="label label-info"><g:message
                    code="license" default="License"/></span></g:if>
       </g:if>

          <g:if test="${hit.getSource().rectype == 'Organisation'}">
            <g:link controller="organisations" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>
        </g:if>
          <g:if test="${hit.getSource().rectype == 'TitleInstance'}">
          <g:link controller="titleDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().title}</g:link> 
        </g:if>
          <g:if test="${hit.getSource().rectype == 'action'}">
          <g:link controller="${hit.getSource().controller}" action="${hit.getSource().action}">${hit.getSource().alias}</g:link> 
        </g:if>
          <g:if test="${hit.getSource().rectype == 'Package'}">
          <g:link controller="packageDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>
        </g:if>
          <g:if test="${hit.getSource().rectype == 'Platform'}">
          <g:link controller="platform" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>
        </g:if>
          <g:if test="${hit.getSource().rectype == 'Subscription'}">
              <g:link controller="subscriptionDetails" action="index"
                      id="${hit.getSource().dbId}">${hit.getSource().name} (${hit.getSource().rectype})</g:link>
        </g:if>
          <g:if test="${hit.getSource().rectype == 'License'}">
          <g:link controller="licenseDetails" action="show" id="${hit.getSource().dbId}">${hit.getSource().name}</g:link>
        </g:if>
      </li>
    </g:each>
  </ul>


