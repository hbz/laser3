<laser:script file="${this.getGroovyPageFileName()}">
    const formObject = $('.la-pagination-custom-input .ui.form');
    const linkObject = $('.la-pagination-custom-link');
    const inputObject = $('.la-pagination-custom-input input');
    let oldHref = linkObject.attr('href');
    let validFlag;

    inputObject.on('input', function() {
        formObject.form('validate form');
        let newOffset = ($(this).val() - 1) * ${max};
        let newHref = oldHref + '&offset=' + newOffset;
        linkObject.attr('href', newHref);
    });
</laser:script>