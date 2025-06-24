
<laser:script file="${this.getGroovyPageFileName()}">
    $('.la-markdown .la-preview').on('click', function(e){
        for (size of ['mini', 'tiny', 'small', 'medium', 'large', 'big', 'huge', 'massive']) {
            let size2 = '!' + size;
            if ($(this).hasClass(size)) {
                $(this).addClass('fluid');
                $(this).addClass(size2);
                $(this).removeClass(size);
            }
            else if ($(this).hasClass(size2)) {
                $(this).removeClass('fluid');
                $(this).removeClass(size2);
                $(this).addClass(size);
            }
        }
    });
</laser:script>