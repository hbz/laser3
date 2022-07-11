<%@ page import="de.laser.UserSetting" %>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.la-js-filterButton').on('click', function(){
        $( ".la-filter").toggle( "fast" );
        $(this).toggleClass("blue");
        $(this).attr('aria-expanded', function(index, attr){
            return attr == 'false' ? 'true' : 'false';
        });
        $.ajax({
            url: '<g:createLink controller="ajax" action="updateSessionCache"/>',
            data: {
                key:    "${UserSetting.KEYS.SHOW_EXTENDED_FILTER}",
                value: !($(this).hasClass('blue')),
                uri:    "${filterAjaxUri}"
            }
        });
    });
</laser:script>