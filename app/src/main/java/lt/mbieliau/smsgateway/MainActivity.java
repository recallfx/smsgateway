/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import lt.mbieliau.smsgateway.common.CustomCursorAdapter;
import lt.mbieliau.smsgateway.common.SmsDb;

public class MainActivity extends ActionBarActivity {
    BroadcastReceiver receiver;
    CustomCursorAdapter adapter;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SmsDb smsDb = SmsDb.getInstance(this);

        cursor = smsDb.getBookingCursor();

        if (cursor != null){
            if (adapter == null){
                adapter = new CustomCursorAdapter(this, cursor);
            }

            ListView list = (ListView)findViewById(R.id.list);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ReadActivity.class);
                    intent.putExtra("id", String.valueOf(id));
                    startActivity(intent);
                }
            });
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (adapter != null){
                    SmsDb smsDb = SmsDb.getInstance(context);

                    closeCursor();

                    cursor = smsDb.getBookingCursor();

                    if (cursor != null){
                        adapter.swapCursor(cursor);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(SmsDb.DB_UPDATED);
        registerReceiver(receiver,intentFilter );
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (receiver != null){
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeCursor();
    }

    private void closeCursor(){
        if (cursor != null && !cursor.isClosed()){
            cursor.close();
        }
    }
}
