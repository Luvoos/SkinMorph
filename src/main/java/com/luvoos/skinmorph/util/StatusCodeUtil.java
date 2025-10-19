package com.luvoos.skinmorph.util;

public final class StatusCodeUtil
{
    private StatusCodeUtil() {}

    /**
     * Convert an HTTP status code to a readable description
     * @param code the HTTP status code
     * @return a readable description of the given code
     */
    public static String toString(int code)
    {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 408 -> "Request Timeout";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            default -> "Unknown Status";
        };
    }
}
