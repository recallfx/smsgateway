/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common;

public class Constants {
    static public final String TAG = Constants.class.getSimpleName();

    static public class Keys {
        static public final String ERROR_MESSAGE = "error_message";

        static public class Settings {
            public static final String TOKEN = "comp_token";
            public static final String CODE = "comp_code";

            static public class Urls {
                public static final String ROOT = "urls_root";
            }
        }
    }

    static public class Defaults {
        public static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        public static final String code = "2370296";
        public static final String token = "2cc98e89b2f047e7a7740f940a228fd3";

        static public class Urls {
            public static final String root = "http://192.168.1.3/";
            public static final String booking = "api/sms/booking";
            public static final String sms = "api/sms/messages";
        }
    }
}
