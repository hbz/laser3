<style>
    .la-markdown img.image {
        max-width: 98%;
        margin: 0.5em;
        border: 2px solid transparent;
    }
    .la-markdown img.image:hover {
        cursor: pointer;
    }
    .la-markdown img.image.medium:hover {
        border-color: #fbbd08;
    }
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.la-markdown img').each( function(){
        $(this).addClass('ui medium image');
    });
    $('.la-markdown img').on('click', function(e){
        $(this).toggleClass('medium');
    });
</laser:script>