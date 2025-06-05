package dev.ultreon.quantum.dedicated.http;

public class HttpCodes {
    private HttpCodes() {

    }

    // 2xx
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NO_CONTENT = 204;

    // 3xx - redirection
    public static final int MULTIPLE_CHOICES = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int USE_PROXY = 305;
    public static final int SWITCH_PROXY = 306;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int LOOP_DETECTED = 308;
    public static final int PERMANENT_REDIRECT = 308;

    // 4xx - client error
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int PAYLOAD_TOO_LARGE = 413;
    public static final int URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;
    public static final int IM_A_TEAPOT = 418;
    public static final int MISDIRECTED_REQUEST = 421;
    public static final int UNPROCESSABLE_ENTITY = 422;
    public static final int LOCKED = 423;
    public static final int FAILED_DEPENDENCY = 424;
    public static final int UPGRADE_REQUIRED = 426;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 430;
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

    // 5xx - server error
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    public static final int VARIANT_ALSO_NEGOTIATES = 506;
    public static final int INSUFFICIENT_STORAGE = 507;
    public static final int LOOP_DETECTED_2 = 508;
    public static final int BANDWIDTH_LIMIT_EXCEEDED = 509;
    public static final int NOT_EXTENDED = 510;
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

    // 6xx - json error
    public static final int UNEXPECTED_TOKEN = 600;
    public static final int JSON_DECODE_ERROR = 601;

    // 7xx - quantum error
    public static final int QUANTUM_ERROR = 700;
    public static final int INVALID_RESOURCE = 701;
    public static final int INVALID_REQUEST = 702;
    public static final int INVALID_METHOD = 703;
    public static final int INVALID_HEADER = 704;
    public static final int INVALID_BODY = 705;
    public static final int INVALID_PARAMETER = 706;
    public static final int INVALID_STATE = 707;
    public static final int INVALID_CREDENTIAL = 708;
    public static final int INVALID_TOKEN = 709;
    public static final int INVALID_AUTHENTICATION = 710;
    public static final int INVALID_AUTHORIZATION = 711;
}
