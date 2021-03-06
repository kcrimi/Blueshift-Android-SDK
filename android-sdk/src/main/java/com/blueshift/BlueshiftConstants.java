package com.blueshift;

/**
 * @author Rahul Raveendran V P
 *         Created on 4/3/15 @ 3:02 PM
 *         https://github.com/rahulrvp
 */
@SuppressWarnings("WeakerAccess")
public class BlueshiftConstants {

    /**
     * URLs for server communication
     */
    public static final String BASE_URL = "https://api.getblueshift.com";
    public static final String TRACK_API_URL = BASE_URL + "/track";
    public static final String LIVE_CONTENT_API_URL = BASE_URL + "/live";
    public static final String V1_API_URL = BASE_URL + "/api/v1";
    public static final String EVENT_API_URL = V1_API_URL + "/event";
    public static final String BULK_EVENT_API_URL = V1_API_URL + "/bulkevents";
    public static final String IN_APP_API_URL = BASE_URL + "/inapp/msg";

    /**
     * Event names sent to Blueshift server
     */
    public static final String EVENT_IDENTIFY = "identify";
    public static final String EVENT_PAGE_LOAD = "pageload";
    public static final String EVENT_SUBSCRIBE = "subscribe";
    public static final String EVENT_UNSUBSCRIBE = "unsubscribe";
    public static final String EVENT_VIEW = "view";
    public static final String EVENT_ADD_TO_CART = "add_to_cart";
    public static final String EVENT_CHECKOUT = "checkout";
    public static final String EVENT_PURCHASE = "purchase";
    public static final String EVENT_CANCEL = "cancel";
    public static final String EVENT_RETURN = "return";
    public static final String EVENT_SEARCH = "search";
    public static final String EVENT_SUBSCRIPTION_UPGRADE = "subscription_upgrade";
    public static final String EVENT_SUBSCRIPTION_DOWNGRADE = "subscription_downgrade";
    public static final String EVENT_SUBSCRIPTION_BILLING = "subscription_billing";
    public static final String EVENT_SUBSCRIPTION_CANCEL = "subscription_cancel";
    public static final String EVENT_APP_OPEN = "app_open";
    public static final String EVENT_APP_INSTALL = "app_install";
    public static final String EVENT_PUSH_DELIVERED = "delivered";
    public static final String EVENT_PUSH_CLICK = "click";
    public static final String EVENT_DISMISS_ALERT = "dismiss_alert";

    /**
     * Names of parameters (key) we send to Blueshift server along with events
     */

    // App
    public static final String KEY_APP_NAME = "app_name";
    public static final String KEY_APP_VERSION = "app_version";

    // Device
    public static final String KEY_LIMIT_AD_TRACKING = "limit_ad_tracking";
    public static final String KEY_DEVICE_IDENTIFIER = "device_id";
    public static final String KEY_DEVICE_TYPE = "device_type";
    public static final String KEY_DEVICE_TOKEN = "device_token";
    public static final String KEY_DEVICE_IDFA = "device_idfa";
    public static final String KEY_DEVICE_IDFV = "device_idfv";
    public static final String KEY_DEVICE_MANUFACTURER = "device_manufacturer";
    public static final String KEY_OS_NAME = "os_name";
    public static final String KEY_NETWORK_CARRIER = "network_carrier";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    // User
    public static final String KEY_EMAIL = "email";
    public static final String KEY_CUSTOMER_ID = "customer_id";
    public static final String KEY_FIRST_NAME = "firstname";
    public static final String KEY_LAST_NAME = "lastname";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_JOINED_AT = "joined_at";
    public static final String KEY_FACEBOOK_ID = "facebook_id";
    public static final String KEY_EDUCATION = "education";
    public static final String KEY_UNSUBSCRIBED = "unsubscribed";
    public static final String KEY_DATE_OF_BIRTH = "date_of_birth";

    // Events
    public static final String KEY_EVENT = "event";
    public static final String KEY_SCREEN_VIEWED = "screen_viewed";
    public static final String KEY_SKU = "sku";
    public static final String KEY_SKUS = "skus";
    public static final String KEY_CATEGORY_ID = "category_id";
    public static final String KEY_QUANTITY = "quantity";
    public static final String KEY_PRODUCTS = "products";
    public static final String KEY_REVENUE = "revenue";
    public static final String KEY_DISCOUNT = "discount";
    public static final String KEY_COUPON = "coupon";
    public static final String KEY_ORDER_ID = "order_id";
    public static final String KEY_SHIPPING_COST = "shipping_cost";
    public static final String KEY_NUMBER_OF_RESULTS = "number_of_results";
    public static final String KEY_PAGE_NUMBER = "page_number";
    public static final String KEY_QUERY = "query";
    public static final String KEY_FILTERS = "filters";
    public static final String KEY_SUBSCRIPTION_PERIOD_TYPE = "subscription_period_type";
    public static final String KEY_SUBSCRIPTION_PERIOD_LENGTH = "subscription_period_length";
    public static final String KEY_SUBSCRIPTION_PLAN_TYPE = "subscription_plan_type";
    public static final String KEY_SUBSCRIPTION_AMOUNT = "subscription_amount";
    public static final String KEY_SUBSCRIPTION_START_DATE = "subscription_start_date";
    public static final String KEY_SUBSCRIPTION_STATUS = "subscription_status";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_SDK_VERSION = "bsft_sdk_version";
    public static final String KEY_UID = "uid";
    public static final String KEY_MID = "mid";
    public static final String KEY_EID = "eid";
    public static final String KEY_TXNID = "txnid";
    public static final String KEY_ACTION = "a";

    // live content
    public static final String KEY_SLOT = "slot";
    public static final String KEY_API_KEY = "api_key";
    public static final String KEY_USER  = "user";
    public static final String KEY_CONTEXT = "context";

    // in-app message
    public static final String KEY_ENABLE_INAPP = "enable_inapp";
    public static final String KEY_LAST_TIMESTAMP = "last_timestamp";

    /**
     * Subscription status values
     */
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_CANCELED = "canceled";

    /**
     * Bulk Event
     */
    public static final int BULK_EVENT_PAGE_SIZE = 100;

    /*
     * Silent push
     */
    public static final String SILENT_PUSH = "silent_push";
    public static final String SILENT_PUSH_ACTION = "action";
    public static final String ACTION_IN_APP_BACKGROUND_FETCH = "in_app_background_fetch";

    // Universal links
    public static final String KEY_REDIR = "redir";
}
