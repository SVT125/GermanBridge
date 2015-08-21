package com.example.james.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

    public void gameClick(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Dialog_MinWidth);
        builder.setPositiveButton("Multiplayer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runGameActivity(v,false);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Singleplayer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runGameActivity(v,true);
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void runGameActivity(View v, boolean isSinglePlayer) {
        Class<? extends GameActivity> executingActivity = null;
        if(v == findViewById(R.id.hearts_button))
            executingActivity = HeartsActivity.class;
        else if(v == findViewById(R.id.german_button))
            executingActivity = BridgeActivity.class;
        else if(v == findViewById(R.id.spades_button))
            executingActivity = SpadesActivity.class;

        Intent intent = new Intent(MainActivity.this,executingActivity);
        intent.putExtra("isSinglePlayer",isSinglePlayer);
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
