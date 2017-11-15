<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div>
        	<img class="brand-img" alt="LAS:eR" src="${resource(dir: 'images', file: 'laser-logo-1.png')}"/>
            <ul class="nav">
                <li id="home" <g:if test="${active.equals("home")}"> class="active" </g:if>>
                    <a href="${createLink(uri: '/')}">${message(code:'default.home.label', default:'Home')}</a>
                </li>
                <li id="about" <g:if test="${active.equals("about")}"> class="active" </g:if>>
                    <a href="${createLink(uri: '/about')}">${message(code:'public.nav.about.label', default:'About LAS:eR')}</a>
                </li>
                <li id="signup" <g:if test="${active.equals("signup")}"> class="active" </g:if>>
                    <a href="${createLink(uri: '/signup')}">${message(code:'public.nav.signUp.label', default:'Sign Up')}</a>
                </li>
               
            <% /*
            <li class="${active.equals('publicExport')?'active':''}">
                            <a href="${createLink(uri: '/publicExport')}">${message(code:'public.nav.exports.label', default:'Exports')}</a>

            </li>
            */ %>
                <li id="contact" <g:if test="${active.equals("contact")}"> class="active" </g:if>>
                    <a href="${createLink(uri: '/contact-us')}">${message(code:'public.nav.contact.label', default:'Contact Us')}</a>
                </li>
            </ul>
        </div>
    </div>
</div>


