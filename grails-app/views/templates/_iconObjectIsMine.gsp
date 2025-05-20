<%@ page import="de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection />
<g:if test="${isMyPlatform || isMyPkg || isMyOrg || isMyProvider || isMyVendor}">

<span class="la-additionalIcon">
  <g:if test="${isMyPlatform}">
    <i class="${Icon.SIG.MY_OBJECT} circular la-objectIsMine la-popup-tooltip" data-content="${message(code: 'license.relationship.platform')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
  <g:if test="${isMyPkg}">
    <i class="${Icon.SIG.MY_OBJECT} circular la-objectIsMine la-popup-tooltip" data-content="${message(code: 'license.relationship.pkg')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
  <g:if test="${isMyOrg}">
    <i class="${Icon.SIG.MY_OBJECT} circular la-objectIsMine la-popup-tooltip" data-content="${message(code: 'license.relationship.org')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
  <g:if test="${isMyProvider}">
    <i class="${Icon.SIG.MY_OBJECT} circular la-objectIsMine la-popup-tooltip" data-content="${message(code: 'license.relationship.provider')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
  <g:if test="${isMyVendor}">
    <i class="${Icon.SIG.MY_OBJECT} circular la-objectIsMine la-popup-tooltip" data-content="${message(code: 'license.relationship.vendor')}" data-position="left center" data-variation="tiny" ></i>
  </g:if>
</span>

</g:if>
