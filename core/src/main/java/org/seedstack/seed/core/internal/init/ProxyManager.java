/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.core.internal.init;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.seedstack.seed.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class ProxyManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyManager.class);
    private SeedProxySelector seedProxySelector;
    private SeedProxyAuthenticator seedProxyAuthenticator;

    private static class Holder {
        private static final ProxyManager INSTANCE = new ProxyManager();
    }

    public static ProxyManager get() {
        return Holder.INSTANCE;
    }

    private ProxyManager() {
        // noop
    }

    public void install(ProxyConfig proxyConfig) {
        if (!proxyConfig.getMode().equals(ProxyConfig.ProxyMode.DISABLED)) {
            boolean autoProxy = proxyConfig.getMode().equals(ProxyConfig.ProxyMode.AUTO);
            String httpProxyValue = getValue(proxyConfig.getHttpProxy(), "http_proxy", autoProxy);
            String httpsProxyValue = getValue(proxyConfig.getHttpsProxy(), "https_proxy", autoProxy);
            String noProxyValue = getValue(proxyConfig.getNoProxy(), "no_proxy", autoProxy);

            Proxy httpProxy = buildProxy(httpProxyValue, 80);
            Proxy httpsProxy = buildProxy(httpsProxyValue, 443);
            List<Pattern> exclusions = buildExclusions(noProxyValue);

            ProxySelector.setDefault(seedProxySelector = new SeedProxySelector(
                    httpProxy,
                    httpsProxy,
                    ProxySelector.getDefault(),
                    exclusions
            ));

            Authenticator.setDefault(seedProxyAuthenticator = new SeedProxyAuthenticator(
                    buildPasswordAuthentication(httpProxyValue),
                    buildPasswordAuthentication(httpsProxyValue)
            ));

            if (httpProxy != Proxy.NO_PROXY) {
                if (Strings.isNullOrEmpty(noProxyValue)) {
                    LOGGER.info("Proxy configured to {} with no exclusion", httpProxy.address().toString());
                } else {
                    LOGGER.info("Proxy configured to {} with exclusion(s) on {}", httpProxy.address().toString(), noProxyValue);
                }
            }
        }
    }

    public void uninstall() {
        if (seedProxySelector != null) {
            ProxySelector.setDefault(null);
        }
        if (seedProxyAuthenticator != null) {
            Authenticator.setDefault(null);
        }
    }

    private String getValue(String configuredValue, String variable, boolean auto) {
        if (Strings.isNullOrEmpty(configuredValue) && auto) {
            String value = System.getenv(variable);
            if (Strings.isNullOrEmpty(value)) {
                value = System.getenv(variable.toUpperCase());
            }
            return value;
        } else {
            return configuredValue;
        }
    }

    private List<Pattern> buildExclusions(String noProxy) {
        if (noProxy == null) {
            return Lists.newArrayList();
        }
        return Arrays.stream(noProxy.split(",")).map(this::makePattern).collect(toList());
    }

    private Proxy buildProxy(String value, int defaultPort) {
        String[] httpProxyInfo = parseProxy(value, String.valueOf(defaultPort));
        if (httpProxyInfo != null) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyInfo[0], Integer.parseInt(httpProxyInfo[1])));
        } else {
            return Proxy.NO_PROXY;
        }
    }

    private PasswordAuthentication buildPasswordAuthentication(String value) {
        String[] httpCredentials = parseCredentials(value);
        if (httpCredentials != null) {
            return new PasswordAuthentication(httpCredentials[0], httpCredentials[1].toCharArray());
        } else {
            return null;
        }
    }

    /**
     * Given a proxy URL returns a two element arrays containing the user name and the password. The second component
     * of the array is null if no password is specified.
     *
     * @param url The proxy host URL.
     * @return An array containing the user name and the password or null when none are present or the url is empty.
     */
    private String[] parseCredentials(String url) {
        String[] result = new String[2];

        if (url == null || url.isEmpty())
            return null;

        int p = url.indexOf("://");
        if (p != -1)
            url = url.substring(p + 3);

        if ((p = url.indexOf('@')) != -1) {
            String credentials = url.substring(0, p);

            if ((p = credentials.indexOf(':')) != -1) {
                result[0] = credentials.substring(0, p);
                result[1] = credentials.substring(p + 1);
            } else {
                result[0] = credentials;
            }
        } else {
            return null;
        }

        return result;
    }

    /**
     * Given a proxy URL returns a two element arrays containing the host name and the port
     *
     * @param url     The proxy host URL.
     * @param defPort The default proxy port
     * @return An array containing the host name and the proxy port or null when url is empty
     */
    private String[] parseProxy(String url, String defPort) {
        String[] result = new String[2];

        if (url == null || url.isEmpty())
            return null;

        int p = url.indexOf("://");
        if (p != -1)
            url = url.substring(p + 3);

        if ((p = url.indexOf('@')) != -1)
            url = url.substring(p + 1);

        if ((p = url.indexOf(':')) != -1) {
            result[0] = url.substring(0, p);
            result[1] = url.substring(p + 1);
        } else {
            result[0] = url;
            result[1] = defPort;
        }

        // remove trailing slash from the host name
        p = result[0].indexOf("/");
        if (p != -1) {
            result[0] = result[0].substring(0, p);
        }

        // remove trailing slash from the port number
        p = result[1].indexOf("/");
        if (p != -1) {
            result[1] = result[1].substring(0, p);
        }

        return result;
    }

    /**
     * Creates a regexp pattern equivalent to the classic noProxy wildcard expression.
     *
     * @param noProxy the noProxy expression.
     * @return the regexp pattern.
     */
    private Pattern makePattern(String noProxy) {
        return Pattern.compile(noProxy.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*"));
    }
}
