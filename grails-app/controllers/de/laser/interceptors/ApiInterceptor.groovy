package de.laser.interceptors

import de.laser.Org
import de.laser.OrgSetting
import de.laser.api.v0.ApiManager
import de.laser.storage.Constants
import grails.converters.JSON
import org.apache.commons.io.IOUtils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletResponse
import java.nio.charset.Charset
import java.security.SignatureException

/**
 * This interceptor class performs checks in order to authenticate the given request by matching HMAC checksums
 */
class ApiInterceptor implements grails.artefact.Interceptor {

    /**
     * defines which controller calls should be caught up
     */
    ApiInterceptor() {
        match(controller: 'api')
    }

    /**
     * Checks whether the submitted authentication token matches to the checksum calculated.
     * The HMAC token is composed by:
     * <ol>
     *     <li>request method (GET)</li>
     *     <li>path requested</li>
     *     <li>query containing the request parameters</li>
     *     <li>API password</li>
     * </ol>
     * Each of those arguments is also submitted in the request and the checksum is being calculated here whether they match
     * @return true if the checksums match, false otherwise
     */
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
                String body = IOUtils.toString(request.getInputStream(), Charset.defaultCharset())

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
                            def timestamp = load[1]
                            def nounce = load[2]
                            def digest = load[3]

                            // checking digest

                            Org apiOrg = OrgSetting.executeQuery(
                                    'SELECT os.org FROM OrgSetting os WHERE os.key = :key AND os.strValue = :value',
                                    [key: OrgSetting.KEYS.API_KEY, value: key]
                            )?.get(0)

                            String apiSecret = OrgSetting.get(apiOrg, OrgSetting.KEYS.API_PASSWORD)?.getValue()

                            checksum = _hmac(
                                    method +    // http-method
                                    path +      // uri
                                    timestamp + // timestamp
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

                    JSON result = new JSON([
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

    /**
     * Dummy method stub implementing abstract methods
     * @return true
     */
    boolean after() {
        true
    }

    /**
     * Calculates the HMAC checksum based on the submitted data
     * @param data the request arguments (method, path, query, api password) composing the checksum
     * @param secret the api password used to calculate the hash
     * @return the HMAC checksum calculated
     * @throws SignatureException
     */
    private String _hmac(String data, String secret) throws SignatureException {
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
