console.log('+ bundle: base.js');

// bundle: assets/javascripts/base.js
//
//= require libs/jquery-3.7.1.js
//= require libs/jquery.poshytip.js
//= require libs/jquery-editable-poshytip.js
//
//= require libs/moment-with-locales-modified.js
//= require libs/readmore.js
//= require libs/purify.js
//
//= require /trumbowyg/trumbowyg.js                             //--> assets/vendor
//= require /trumbowyg/langs/de.js                              //--> assets/vendor
//= require /trumbowyg/plugins/history/trumbowyg.history.js     //--> assets/vendor

window.onerror  = (a, b, c, d, e) => {
    if ($.toast) {
        $.toast({
            title: 'LAS:eR-Debug',
            message: '<div>&dot; Javascript</div><div>&dot; ' + a + '</div><div>&dot; ' + b + ':' + c + '</div>',
            displayTime: 30000,
            class: 'red',
            showIcon: 'bug',
            position: 'bottom left' });
    } else {
        alert('LAS:eR-Debug * Javascript-Error\n\n' + a + '\n' + b + ':' + c);
    }
    return false;
}

