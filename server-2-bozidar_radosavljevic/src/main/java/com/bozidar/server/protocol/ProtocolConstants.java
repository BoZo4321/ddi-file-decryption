package com.bozidar.server.protocol;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ProtocolConstants {

    private ProtocolConstants() {
    }

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final String REQUEST_HEADER = "DDI/1.0 DECRYPT";
    public static final String RESPONSE_HEADER = "DDI/1.0 RESPONSE";

    public static final String HEADER_FILE_COUNT = "FILE_COUNT";
    public static final String HEADER_NAME = "NAME";
    public static final String HEADER_SIZE = "SIZE";
    public static final String CONTENT_MARKER = "CONTENT_BASE64";

    public static final String HEADER_STATUS = "STATUS";
    public static final String HEADER_FILES_PROCESSED = "FILES_PROCESSED";
    public static final String HEADER_ZIP_PATH = "ZIP_PATH";
    public static final String HEADER_MESSAGE = "MESSAGE";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";

    public static final String FILE_SEPARATOR = "---";
    public static final String END_MARKER = "END";

    public static final String KEY_VALUE_SEPARATOR = ": ";
}
