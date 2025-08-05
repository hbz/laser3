<%@ page import="de.laser.storage.BeanStore" %>
<laser:htmlStart text="GASCO" layout="${BeanStore.getSpringSecurityService().isLoggedIn() ? 'laser':'public'}" />

<g:render template="/public/gasco/nav" />

<laser:htmlEnd />
