<%@ page import="de.laser.storage.BeanStore" %>
<laser:htmlStart text="GASCO-Über uns" layout="${BeanStore.getSpringSecurityService().isLoggedIn() ? 'laser':'public'}" />

<div class="gasco">
    <g:render template="/public/gasco/nav" />
</div>

<laser:htmlEnd />
