<div id="orgRoleContainer">

<g:each in="${listOfLinks.sort()}" var="rdv_id,link_cat">

    <dl>
        <dt>
            <h5 class="ui header">
                ${message(code: ('template.orgRole.' + link_cat.rdv.value).replaceAll(' ',''), default: link_cat.rdv.getI10n('value'))}
            </h5>
        <dd>

            <div class="ui list">

                <g:each in="${link_cat.links}" var="i">

                    <div class="item">
                        <g:if test="${i.pkg}">
                            <g:link controller="package" action="show" id="${i.pkg.id}">
                                ${message(code:'package.label')}: ${i.pkg.name}
                            </g:link>
                        </g:if>
                        <g:if test="${i.sub}">
                            <g:link controller="subscription" action="show" id="${i.sub.id}">
                                ${message(code:'default.subscription.label')}: ${i.sub.dropdownNamingConvention()}
                            </g:link>
                        </g:if>
                        <g:if test="${i.lic}">
                            <g:link controller="license" action="show" id="${i.lic.id}">
                                ${message(code:'license.label')}: ${i.lic.reference ?: i.lic.id}
                            </g:link>
                        </g:if>
                        <g:if test="${i.title}">
                            <g:link controller="title" action="show" id="${i.title.id}">
                                ${message(code:'title.label')}: ${i.title.title}
                            </g:link>
                        </g:if>

                        <g:if test="${i.getOwnerStatus() && !i.sub}">
                            (${i.getOwnerStatus().getI10n('value')})
                        </g:if>
                    </div>
                </g:each>

                <g:set var="local_offset" value="${params[link_cat.rdvl] ? Long.parseLong(params[link_cat.rdvl]) : null}" />

                <g:if test="${link_cat.total > 10}">
                    <div class="item">
                        ${message(code:'default.paginate.offset', args:[(local_offset ?: 1),(local_offset ? (local_offset + 10 > link_cat.total ? link_cat.total : local_offset + 10) : 10), link_cat.total])}
                    </div>
                    <div class="item">
                        <g:if test="${local_offset}">
                            <g:set var="os_prev" value="${local_offset > 9 ? (local_offset - 10) : 0}" />
                            <button class="ui icon button tiny" data-params="rdvl_${rdv_id}=${os_prev}">
                                <i class="left arrow icon"></i>
                            </button>
                        </g:if>
                        <g:if test="${!local_offset || ( local_offset < (link_cat.total - 10) )}">
                            <g:set var="os_next" value="${local_offset ? (local_offset + 10) : 10}" />
                            <button class="ui icon button tiny" data-params="rdvl_${rdv_id}=${os_next}">
                                <i class="right arrow icon"></i>
                            </button>
                        </g:if>
                    </div>
                </g:if>
            </div><!-- .list -->
        </dd>
    </dl>
</g:each>


<laser:script file="${this.getGroovyPageFileName()}">
        $("#orgRoleContainer .button").on('click', function() {
            var ajaxUrl = "<g:createLink controller="organisation" action="show" id="${orgInstance.id}" />"
                        + "?ajax=true&" + $(this).attr('data-params')

            $.ajax({
                url: ajaxUrl
            }).done( function(html) {
                $("#orgRoleContainer").empty().append(html)
            })
        })
</laser:script>

</div>
