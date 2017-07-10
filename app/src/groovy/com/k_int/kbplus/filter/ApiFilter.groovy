package com.k_int.kbplus.filter

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.UserRole
import grails.converters.JSON
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.security.SignatureException
import com.k_int.kbplus.auth.User

class ApiFilter extends GenericFilterBean {

    @Override
    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest
        HttpServletResponse response = (HttpServletResponse) servletResponse

        // ignore non api calls
        if(request.getServletPath().startsWith('/api/v')) {
            def isAuthorized = false

            def method = request.getMethod()
            def path   = request.getServletPath()
            def query  = request.getQueryString()

            String authorization = request.getHeader('Authorization')
            try {
                if (authorization) {
                    def p1 = authorization.split(' ')
                    def authMethod = p1[0]

                    if (authMethod == 'hmac') {
                        def p2 = p1[1].split(',')
                        def load = p2[0].split(':')
                        def algorithm = p2[1]

                        def key = load[0]
                        def timestamp = load[1]
                        def nounce = load[2]
                        def digest = load[3]

                        // checking digest

                        def apiUser = User.findByApikey(key)
                        def apiSecret = apiUser.apisecret

                        def check = hmac(
                                method +    // http-method
                                path +      // uri
                                timestamp + // timestamp
                                nounce +    // nounce
                                query +     // parameter
                                "",         // body
                                apiSecret)

                        isAuthorized = (check == digest)

                        // checking role permission

                        def readRole  = UserRole.findAllWhere(user: apiUser, role: Role.findByAuthority('ROLE_API_READER'))
                        def writeRole = UserRole.findAllWhere(user: apiUser, role: Role.findByAuthority('ROLE_API_WRITER'))

                        if("GET" == method) {
                            if(!readRole) {
                                isAuthorized = false
                            }
                        }
                        else if("POST" == method) {
                            if(!writeRole) {
                                isAuthorized = false
                            }
                        }
                    }
                }
            } catch(Exception e) {
                isAuthorized = false
            }

            if (isAuthorized) {
                println "VALID authorization: " + authorization
                request.getRequestDispatcher(path).forward(servletRequest, servletResponse)
                return
            } else {
                println "INVALID authorization: " + authorization
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                response.setContentType("application/json")

                def result = new JSON([
                        "message": "unauthorized access",
                        "authorization": authorization,
                        "path": path,
                        "query": query,
                        "method": method
                ])
                response.getWriter().print(result.toString(true))
                return
            }
        }
        filterChain.doFilter(servletRequest, servletResponse)
    }

    def hmac(String data, String secret) throws SignatureException {
        String result
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256")
            Mac mac = Mac.getInstance("HmacSHA256")
            mac.init(signingKey)

            byte[] rawHmac = mac.doFinal(data.getBytes())
            result = rawHmac.encodeHex()
        }
        catch (Exception e) {
            throw new SignatureException("failed to generate HMAC : " + e.getMessage());
        }
        return result
    }
}