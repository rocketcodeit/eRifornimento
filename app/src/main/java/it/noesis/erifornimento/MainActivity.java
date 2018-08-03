package it.noesis.erifornimento;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import it.noesis.erifornimento.tasks.AsyncTaskCallbackContext;
import it.noesis.erifornimento.tasks.PingTask;
import it.noesis.erifornimento.utils.CallbackContext;
import it.noesis.erifornimento.utils.Constants;

public class MainActivity extends AppCompatActivity implements CallbackContext<String>, AsyncTaskCallbackContext<String> {



    private Button btnFattura;
    private Button btnServer;
    private LinearLayout progressLayout;
    private AppCompatImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        initInteface();

        if (getIntent().getExtras() != null){
            String loggedUser = getIntent().getExtras().getString(Constants.LOGGED_USERNAME);
            if ( loggedUser != null){
                Toast.makeText(this, "Benvenuto " + loggedUser, Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()){
            case R.id.menuserver:

                break;
            case R.id.menulogout:
                logout();
                Intent i = new Intent(this, Login2Activity.class);
                startActivity(i);
                finish();
                break;
        }

        return true;
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFERENCES_USER_TOKEN, "");
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);

        return true;
    }

    private void initInteface() {
        btnFattura = ((Button) findViewById(R.id.btnFattura));
        btnServer = ((Button) findViewById(R.id.btnServer));
        progressLayout = ((LinearLayout) findViewById(R.id.progressLayout));
        image = ((AppCompatImageView) findViewById(R.id.image));


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewFatturaActivity();
            }
        });
        btnFattura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewFatturaActivity();
            }
        });

        btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ServerDialogFragment frag = new ServerDialogFragment();

                Bundle args = new Bundle();
                args.putString(Constants.PREFERENCES_SERVER, getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(Constants.PREFERENCES_SERVER,""));
                frag.setArguments(args);


                frag.show(getSupportFragmentManager(), "Server");


            }
        });


        CheckServerStatus();

        progressLayout.setVisibility(View.INVISIBLE);


    }

    private void startNewFatturaActivity() {
        Intent i = new Intent(MainActivity.this, FatturaActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        if (requestCode == 1){

            if (resultCode == RESULT_OK){
                String result = data.getExtras().getString("data");
                Toast.makeText(this,"Invio effettuato con successo: " + result, Toast.LENGTH_LONG).show();
            }

        }
    }

    private void CheckServerStatus() {
        //devo verifcare se tra le shared resources cè quella relativa all'url delc server impostata
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        String server = prefs.getString(Constants.PREFERENCES_SERVER, "");
        if (TextUtils.isEmpty(server)){
            btnFattura.setEnabled(false);
        }else{
            btnFattura.setEnabled(true);
        }
    }


    private void saveUrl(String serverUrl) {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFERENCES_SERVER, serverUrl);
        editor.commit();
    }

    @Override
    public void onPreExecute() {
        progressLayout.setVisibility(View.VISIBLE);
        btnFattura.setVisibility(View.GONE);
        btnServer.setVisibility(View.GONE);
        image.setVisibility(View.GONE);
    }

    @Override
    public void onPostExecute(String s) {

        progressLayout.setVisibility(View.GONE);
        btnFattura.setVisibility(View.VISIBLE);
        btnServer.setVisibility(View.VISIBLE);
        image.setVisibility(View.VISIBLE);

        saveUrl(s);
        CheckServerStatus();

    }

    @Override
    public void onDialogDismiss(String returnData, String dialogTag) {
        if (TextUtils.isEmpty(returnData)){
            CheckServerStatus();
            return;
        }


        new PingTask(this).execute(new String[]{returnData});
    }
}
