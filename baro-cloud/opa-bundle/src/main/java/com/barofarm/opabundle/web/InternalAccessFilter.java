package com.barofarm.opabundle.web;

import com.barofarm.opabundle.config.OpaAccessProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
// [0] 내부망/토큰 접근 제어를 담당하는 OPA 번들 접근 필터 클래스.
public class InternalAccessFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(InternalAccessFilter.class);
    private static final String TOKEN_HEADER = "X-Internal-Token";

    // [1] Optional internal-only guard for bundle endpoints.
    private final OpaAccessProperties accessProperties;

    public InternalAccessFilter(OpaAccessProperties accessProperties) {
        this.accessProperties = accessProperties;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (!accessProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String remoteAddr = request.getRemoteAddr();
        boolean ipAllowed = isAllowedIp(remoteAddr);
        if (!ipAllowed) {
            LOG.warn("Blocked non-internal access from {}", remoteAddr);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        if (accessProperties.isRequireToken()) {
            String token = request.getHeader(TOKEN_HEADER);
            if (token == null || !token.equals(accessProperties.getToken())) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/opa/");
    }

    private boolean isAllowedIp(String remoteAddr) {
        if (remoteAddr == null) {
            return false;
        }
        if ("127.0.0.1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return true;
        }

        List<String> allowedCidrs = accessProperties.getAllowedCidrs();
        if (allowedCidrs != null) {
            boolean hasCidrs = allowedCidrs.stream().anyMatch(this::hasText);
            if (hasCidrs) {
                return allowedCidrs.stream()
                    .filter(this::hasText)
                    .anyMatch(cidr -> matchesCidr(remoteAddr, cidr));
            }
        }

        if (!accessProperties.isAllowPrivate()) {
            return false;
        }

        return isPrivateIpv4(remoteAddr);
    }

    private boolean isPrivateIpv4(String remoteAddr) {
        String[] parts = remoteAddr.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            if (first == 10) {
                return true;
            }
            if (first == 172 && second >= 16 && second <= 31) {
                return true;
            }
            return first == 192 && second == 168;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean matchesCidr(String remoteAddr, String cidr) {
        if (cidr == null || !cidr.contains("/")) {
            return false;
        }
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(remoteAddr);
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            byte[] addr = address.getAddress();
            byte[] net = network.getAddress();
            if (addr.length != 4 || net.length != 4 || prefix < 0 || prefix > 32) {
                return false;
            }
            int mask = prefix == 0 ? 0 : -1 << (32 - prefix);
            int addrInt = bytesToInt(addr);
            int netInt = bytesToInt(net);
            return (addrInt & mask) == (netInt & mask);
        } catch (UnknownHostException | NumberFormatException ex) {
            return false;
        }
    }

    private int bytesToInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) | (b & 0xFF);
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
