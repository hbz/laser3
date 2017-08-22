<g:each in="${subPkgs}" var="sp">
  <p><g:link controller="packageDetails" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link></p>
</g:each>
