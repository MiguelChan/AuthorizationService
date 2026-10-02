package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.stereotype.Component;

/**
 * Validates application input before persistence.
 */
@Component
public class ApplicationInputValidator {

    /**
     * Requires an absolute HTTP(S) origin without credentials, a path, query or fragment.
     * A trailing root slash and a valid explicit port are allowed.
     *
     * @param value Requested redirect URL.
     */
    public void validateRedirectUrl(String value) {
        if (value == null) {
            throw new InvalidArgumentException("Redirect URL is required");
        }
        try {
            URI uri = new URI(value);
            String path = uri.getRawPath();
            String host = uri.getHost();
            if ((!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                || host == null || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null || (path != null && !path.isEmpty() && !"/".equals(path))
                || uri.getPort() == 0 || uri.getPort() > 65535 || uri.getRawAuthority().endsWith(":")) {
                throw new InvalidArgumentException("Redirect URL must be an HTTP(S) origin without credentials, paths, queries or fragments");
            }
            if (host.matches("[0-9]+[.][0-9]+[.][0-9]+[.][0-9]+") && !InetAddressValidator.getInstance().isValidInet4Address(host)) {
                throw new InvalidArgumentException("Redirect URL contains an invalid IP address");
            }
        } catch (URISyntaxException e) {
            throw new InvalidArgumentException("Redirect URL must be a valid HTTP(S) origin");
        }
    }
}
