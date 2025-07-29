<%@ page import="de.laser.storage.BeanStore" %>
<laser:htmlStart text="GASCO-Über uns" layout="${BeanStore.getSpringSecurityService().isLoggedIn() ? 'laser':'public'}" />

<main class="ui main container">
    <g:render template="/public/gasco/nav" />
</main>

<laser:htmlEnd />
