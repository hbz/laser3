package de.laser.interceptors

import de.laser.Org
import de.laser.OrgSetting
import de.laser.api.v0.ApiManager
import de.laser.helper.Constants
import grails.converters.JSON
import org.apache.commons.io.IOUtils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletResponse
import java.security.SignatureException

class ApiInterceptor implements grails.artefact.Interceptor {

    ApiInterceptor() {
        match(controller: 'api')
    }

    boolean before() {

        String servletPath = request.getServletPath()
        // ignore non api calls
        if (servletPath.startsWith('/api/v')) {
            // ignore api meta calls
            if (! (servletPath.endsWith('/specs.yaml') || servletPath.endsWith('/changelog.md')) ) {

                boolean isAuthorized = false
                String checksum

                String method = request.getMethod()
                String path = request.getServletPath()
                String query = request.getQueryString()
                def body = IOUtils.toString(request.getInputStream())

                String authorization = request.getHeader('X-Authorization')
                boolean debugMode    = request.getHeader('X-Debug') == 'true'

                try {
                    if (authorization) {
                        def p1 = authorization.split(' ')
                        def authMethod = p1[0]

                        if (authMethod == 'hmac') {
                            def p2 = p1[1].split(',')
                            def load = p2[0].split(':')
                            def algorithm = p2[1]

                            def key = load[0]
                            def changedFrom = load[1]
                            def nounce = load[2]
                            def digest = load[3]

                            // checking digest

                            Org apiOrg = OrgSetting.executeQuery(
                                    'SELECT os.org FROM OrgSetting os WHERE os.key = :key AND os.strValue = :value',
                                    [key: OrgSetting.KEYS.API_KEY, value: key]
                            )?.get(0)

                            String apiSecret = OrgSetting.get(apiOrg, OrgSetting.KEYS.API_PASSWORD)?.getValue()

                            checksum = hmac(
                                    method +    // http-method
                                    path +      // uri
                                    changedFrom + // changedFrom
                                    nounce +    // nounce
                                    (query ? URLDecoder.decode(query) : '') + // parameter
                                    body,       // body
                                    apiSecret)

                            isAuthorized = (checksum == digest)
                            if (isAuthorized) {
                                request.setAttribute('authorizedApiOrg', apiOrg)
                                request.setAttribute('authorizedApiPostBody', body)
                                request.setAttribute('debugMode', debugMode)
                            }
                        }
                    }
                }
                catch (Exception e) {
                    isAuthorized = false
                }

                if (isAuthorized) {

                    return true
                }
                else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                    response.setContentType(Constants.MIME_APPLICATION_JSON)
                    response.setHeader("Laser-Api-Version", ApiManager.VERSION.toString())

                    def result = new JSON([
                            "message"      : "unauthorized access",
                            "authorization": authorization,
                            "path"         : path,
                            "query"        : query,
                            "method"       : method
                    ])
                    response.getOutputStream().print(result.toString(true))

                    return false
                }
            }
        }

        true
    }

    boolean after() {

        true
    }

    private String hmac(String data, String secret) throws SignatureException {
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
