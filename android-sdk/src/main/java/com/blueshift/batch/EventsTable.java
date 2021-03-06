package com.blueshift.batch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.blueshift.BlueshiftLogger;
import com.blueshift.framework.BaseSqliteTable;
import com.blueshift.util.SdkLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Rahul Raveendran V P
 *         Created on 24/8/16 @ 3:07 PM
 *         https://github.com/rahulrvp
 */
public class EventsTable extends BaseSqliteTable<Event> {

    public static final String TABLE_NAME = "events";

    private static final String LOG_TAG = "EventsTable";
    private static final String FIELD_ID = "id";
    private static final String FIELD_EVENT_PARAMS_JSON = "event_params_json";

    private static EventsTable sInstance;

    public EventsTable(Context context) {
        super(context);
    }

    public static EventsTable getInstance(Context context) {
        synchronized (lock) {
            if (sInstance == null) {
                sInstance = new EventsTable(context);
            }

            return sInstance;
        }
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected Event loadObject(Cursor cursor) {
        Event event = new Event();

        try {
            if (isValidCursor(cursor)) {
                event.setId(cursor.getLong(cursor.getColumnIndex(FIELD_ID)));

                String json = cursor.getString(cursor.getColumnIndex(FIELD_EVENT_PARAMS_JSON));
                HashMap<String, Object> paramsMap = new HashMap<>();
                if (!TextUtils.isEmpty(json)) {
                    Type type = new TypeToken<HashMap<String, Object>>() {
                    }.getType();
                    paramsMap = new Gson().fromJson(json, type);
                }
                event.setEventParams(paramsMap);
            }
        } catch (Exception e) {
            event = null;
            BlueshiftLogger.e(LOG_TAG, e);
        }

        return event;
    }

    @Override
    protected ContentValues getContentValues(Event event) {
        ContentValues contentValues = new ContentValues();

        if (event != null) {
            contentValues.put(FIELD_EVENT_PARAMS_JSON, event.getEventParamsJson());
        }

        return contentValues;
    }

    @Override
    protected HashMap<String, FieldType> getFields() {
        HashMap<String, FieldType> fieldTypeHashMap = new HashMap<>();

        fieldTypeHashMap.put(FIELD_ID, FieldType.Autoincrement);
        fieldTypeHashMap.put(FIELD_EVENT_PARAMS_JSON, FieldType.String);

        return fieldTypeHashMap;
    }

    @Override
    protected Long getId(Event event) {
        return event != null ? event.getId() : 0;
    }

    /**
     * Bulk event api takes max 1MB of events.
     * This method is used for generating an array of event request parameter JSONs.
     * This method will retrieve and delete the first batchSize amount of records from db. Then will create
     * an ArrayList of Strings, where each element will be on event request's parameters as JSON.
     *
     * @return array of event request parameter JSONs
     */
    public ArrayList<HashMap<String,Object>> getBulkEventParameters(int batchSize) {
        ArrayList<HashMap<String,Object>> result = new ArrayList<>();

        ArrayList<Event> events = findWithLimit(batchSize);
        if (events.size() > 0) {
            ArrayList<String> idList = new ArrayList<>();

            for (Event event : events) {
                // get the event parameter
                result.add(event.getEventParams());

                // get id for deleting the item.
                idList.add(String.valueOf(event.getId()));
            }

            int count = delete(FIELD_ID, idList);

            SdkLog.i(LOG_TAG, "Deleted " + count + " events.");
        }

        return result;
    }
}
