<ul>
<g:each in="${OrgTypes.sort{it?.getI10n("value")}}" var="type">
    <li>${type.getI10n("value")}</li>
</g:each>
</ul>