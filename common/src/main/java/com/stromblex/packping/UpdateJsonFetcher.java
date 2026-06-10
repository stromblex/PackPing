package com.stromblex.packping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

final class UpdateJsonFetcher {
    private static final int MAX_REDIRECTS = 5;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private UpdateJsonFetcher() {
    }

    static String fetch(String updateUrl) throws IOException, InterruptedException, UpdateFetchException {
        URI currentUri = parseUri(updateUrl);
        validateHttpScheme(currentUri);

        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(currentUri)
                    .timeout(READ_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (isRedirect(statusCode)) {
                if (redirects == MAX_REDIRECTS) {
                    throw new UpdateFetchException("too many redirects (maximum " + MAX_REDIRECTS + ")");
                }

                currentUri = resolveRedirect(currentUri, response);
                continue;
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new UpdateFetchException("non-2xx final response, HTTP " + statusCode);
            }

            String body = response.body();
            if (body == null || body.isBlank()) {
                throw new UpdateFetchException("empty response body");
            }

            return body;
        }

        throw new UpdateFetchException("too many redirects (maximum " + MAX_REDIRECTS + ")");
    }

    private static URI parseUri(String value) throws UpdateFetchException {
        try {
            return URI.create(value);
        } catch (IllegalArgumentException e) {
            throw new UpdateFetchException("invalid update URL", e);
        }
    }

    private static URI resolveRedirect(URI currentUri, HttpResponse<?> response) throws UpdateFetchException {
        Optional<String> location = response.headers().firstValue("Location");
        if (location.isEmpty() || location.get().isBlank()) {
            throw new UpdateFetchException("missing Location header for HTTP " + response.statusCode() + " redirect");
        }

        URI nextUri;
        try {
            nextUri = currentUri.resolve(location.get().trim());
        } catch (IllegalArgumentException e) {
            throw new UpdateFetchException("invalid redirect Location header", e);
        }

        validateHttpScheme(nextUri);
        if (isHttps(currentUri) && isHttp(nextUri)) {
            throw new UpdateFetchException("HTTPS to HTTP redirect blocked");
        }

        return nextUri;
    }

    private static void validateHttpScheme(URI uri) throws UpdateFetchException {
        String scheme = uri.getScheme();
        if (!isHttpScheme(scheme)) {
            throw new UpdateFetchException("unsupported URL scheme: " + (scheme == null ? "<none>" : scheme));
        }
    }

    private static boolean isRedirect(int statusCode) {
        return statusCode == 301
                || statusCode == 302
                || statusCode == 303
                || statusCode == 307
                || statusCode == 308;
    }

    private static boolean isHttpScheme(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static boolean isHttps(URI uri) {
        return "https".equalsIgnoreCase(uri.getScheme());
    }

    private static boolean isHttp(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme());
    }

    static final class UpdateFetchException extends Exception {
        UpdateFetchException(String message) {
            super(message);
        }

        UpdateFetchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
