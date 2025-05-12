

<laser:script file="${this.getGroovyPageFileName()}">
    $('.la-markdown img').not('.la-js-questionMark').each( function(){
        $(this).addClass('ui medium image');
    });
    $('.la-markdown img').not('.la-js-questionMark').on('click', function(e){
        $(this).toggleClass('ui medium image');
        $(this).toggleClass('ui fluid image');
    });
</laser:script>