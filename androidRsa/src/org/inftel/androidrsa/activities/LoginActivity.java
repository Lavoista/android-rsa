
package org.inftel.androidrsa.activities;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.inftel.androidrsa.R;
import org.inftel.androidrsa.asynctask.LoginTask;
import org.inftel.androidrsa.rsa.KeyStore;
import org.inftel.androidrsa.rsa.RSA;
import org.inftel.androidrsa.utils.AndroidRsaConstants;
import org.jivesoftware.smack.Connection;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private SharedPreferences prefs;
    private Connection connection;
    private String selectedItem;
    private String userid;
    private String password;
    private LoginTask task;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        prefs = getSharedPreferences(AndroidRsaConstants.SHARED_PREFERENCE_FILE,
                Context.MODE_PRIVATE);
        loadPreferences();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.services_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedItem = (String) parent.getItemAtPosition(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        SharedPreferences prefs = getSharedPreferences(
                AndroidRsaConstants.SHARED_PREFERENCE_FILE,
                Context.MODE_PRIVATE);

        if (prefs.getBoolean(AndroidRsaConstants.REGISTERED, false)) {
            try {
                File file = new File(prefs.getString(AndroidRsaConstants.KEY_PATH, ""));
                KeyStore.getInstance().setPk(FileUtils.readFileToByteArray(file));
                KeyStore.getInstance().setPb(RSA.getCAPublicKey(this));
                KeyStore.getInstance().setCertificate(AndroidRsaConstants.OWN_ALIAS,
                        RSA.getCertificate(prefs.getString(AndroidRsaConstants.CERT_PATH, "")));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    // Carga los campos de la última ejecución
    private void loadPreferences() {
        if (!prefs.getString(AndroidRsaConstants.USERID, "default").equals("default")) {
            EditText e = (EditText) findViewById(R.id.userid);
            e.setText(prefs.getString(AndroidRsaConstants.USERID, "default"));
        }
        // Cuenta gmail de accounts manager
        else if (prefs.getString("userid", "default").equals("default")) {
            String userid = "";
            EditText e = (EditText) findViewById(R.id.userid);
            Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
            for (Account account : accounts) {
                userid = account.name;
            }
            Log.i(TAG, "Encontrado user por defecto: " + userid);
            e.setText(userid);
        }
        String service = prefs.getString(AndroidRsaConstants.SERVICE, "default");
        if (!service.equals("default")) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            if (service.equals("Gmail"))
                spinner.setSelection(0);
            else if (service.equals("Facebook"))
                spinner.setSelection(1);
            else if (service.equals("Jabber"))
                spinner.setSelection(2);
            else
                spinner.setSelection(3);
        }
    }

    public void login(View v) {
        userid = ((EditText) findViewById(R.id.userid)).getText().toString();
        password = ((EditText) findViewById(R.id.password)).getText().toString();
        if (userid != null && !userid.isEmpty() && password != null && !password.isEmpty()) {
            // Saving passphrase
            Editor editor = prefs.edit();
            editor.putString(AndroidRsaConstants.SERVICE, selectedItem);
            editor.putString(AndroidRsaConstants.USERID, userid);
            editor.apply();

            // Check we've been run once.

            task = new LoginTask(this, connection,
                    selectedItem, userid, password, this);
            task.execute();
        } else {
            Toast.makeText(this, getResources().getString(R.string.info_empty_fields),
                    Toast.LENGTH_LONG)
                    .show();
        }

    }

    // /** llamado cuando se pulsa el boton menu. */
    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // MenuInflater inflater = getMenuInflater();
    // inflater.inflate(R.menu.menu_login, menu);
    // return true;
    // }
    //
    // /** llamado cuando un elemento del menu es seleccionado. */
    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    // Intent i;
    // switch (item.getItemId()) {
    // case R.id.button_register:
    // i = new Intent(this, RegisterActivity.class);
    // startActivity(i);
    // break;
    // // TODO cambiar contrase�a
    // // case R.id.button_change_password:
    // // i = new Intent(this, ChangePassword.class);
    // // startActivity(i);
    // // break;
    //
    // }
    // return true;
    // }

    // Dialogs

}
