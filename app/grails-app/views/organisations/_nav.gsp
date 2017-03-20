<ul class="nav nav-pills">
  <li <%='show'== actionName ? ' class="active"' : '' %>><g:link controller="organisations" action="show" params="${[id:params.id]}">${message(code:'org.nav.details', default:'Details')}</g:link></li>
  <li <%='users'== actionName ? ' class="active"' : '' %>><g:link controller="organisations" action="users" params="${[id:params.id]}">${message(code:'org.nav.users', default:'Users')}</g:link></li>
    <li <%='config'== actionName ? ' class="active"' : '' %>><g:link controller="organisations" action="config" params="${[id:params.id]}">${message(code:'org.nav.options', default:'Options')}</g:link></li>
</ul>
