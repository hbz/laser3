<%@ page import="de.laser.utils.AppUtils; de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection />
<g:if test="${isMyPlatform || isMyPkg || isMyOrg}">

<div class="la-additionalIcon">
  <g:if test="${isMyPlatform}">
    <i class="icon circular star la-objectIsMine la-popup-tooltip la-delay" data-content="${message(code: 'license.relationship.platform')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
  <g:if test="${isMyPkg}">
    <i class="icon circular star la-objectIsMine la-popup-tooltip la-delay" data-content="${message(code: 'license.relationship.pkg')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
  <g:if test="${isMyOrg}">
    <i class="icon circular star la-objectIsMine la-popup-tooltip la-delay" data-content="${message(code: 'license.relationship.org')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
</div>

%{--<g:if test="${! AppUtils.isPreviewOnly()}">--}%
<laser:script file="${this.getGroovyPageFileName()}">
  $(document).ready(function() {
    $('.la-objectIsMine').visibility({
      type   : 'fixed',
      offset : 55,
      zIndex: 101
    })
  })
</laser:script>
%{--</g:if>--}%

</g:if>
