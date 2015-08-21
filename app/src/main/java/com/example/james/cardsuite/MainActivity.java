package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView greetingView = (TextView)findViewById(R.id.greetingView);
        greetingView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Asimov.ttf"));
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public void heartsClick(View v) {
        //Add bundle content as needed
        Intent intent = new Intent(MainActivity.this,HeartsActivity.class);
        startActivity(intent);
        finish();
    }

    public void bridgeClick(View v) {
        //Add bundle content as needed
        Intent intent = new Intent(MainActivity.this,BridgeActivity.class);
        startActivity(intent);
        finish();
    }

    public void spadesClick(View v) {
        //Add bundle content as needed
        Intent intent = new Intent(MainActivity.this,SpadesActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
