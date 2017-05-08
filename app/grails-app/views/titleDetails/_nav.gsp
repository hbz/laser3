<ul class="nav nav-pills">

  <li <%='show'== actionName ? ' class="active"' : '' %>>
    <g:link controller="titleDetails" action="show" params="${[id:params.id]}">${message(code:'title.nav.details', default:'Title Details')}</g:link>
  </li>

  <li<%='history'== actionName ? ' class="active"' : '' %>>
  <g:link controller="titleDetails"  action="history" params="${[id:params.id]}">${message(code:'title.nav.history', default:'Database Title History')}</g:link></li>

</ul>
