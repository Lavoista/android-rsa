
package org.inftel.androidrsa.asynctask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.inftel.androidrsa.R;
import org.inftel.androidrsa.activities.ContactsActivity;
import org.inftel.androidrsa.activities.LoginActivity;
import org.inftel.androidrsa.activities.RegisterActivity;
import org.inftel.androidrsa.rsa.RSA;
import org.inftel.androidrsa.utils.AndroidRsaConstants;
import org.inftel.androidrsa.xmpp.Conexion;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoginTask extends AsyncTask<Object, Void, Boolean> {
    private static final String TAG = "LoginTask";
    private Connection con;
    private String service;
    private String user;
    private String password;
    private LoginActivity activity;
    private ProgressDialog pDialog;
    private Context ctx;
    private static final int DIALOG_RUN_OTHER = 1000;

    public LoginTask(Context ctx, Connection c, String service, String user,
            String password, LoginActivity activity) {
        super();
        this.ctx = ctx;
        this.con = c;
        this.service = service;
        this.user = user;
        this.password = password;
        this.activity = activity;
    }

    public void onPostExecute(Boolean success) {
        if (success) {
            Log.i(TAG, "Conexión creada correctamente!");
            Log.i(TAG, "Conectado como " + con.getUser());

            SharedPreferences prefs = ctx.getSharedPreferences(
                    AndroidRsaConstants.SHARED_PREFERENCE_FILE,
                    Context.MODE_PRIVATE);
            boolean registered = prefs.getBoolean(AndroidRsaConstants.REGISTERED, false);

            if (registered) {
                try {
                    if (RSA.verifyOwnPk(password)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setMessage(R.string.run_configuration_question)
                                .setCancelable(false)
                                .setPositiveButton(ctx.getResources().getString(R.string.yes),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent i = new Intent(ctx.getApplicationContext(),
                                                        RegisterActivity.class);
                                                i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
                                                ctx.startActivity(i);
                                                dialog.dismiss();
                                            }
                                        })
                                .setNegativeButton(ctx.getResources().getString(R.string.no),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent i = new Intent(activity,
                                                        ContactsActivity.class);
                                                i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
                                                activity.startActivity(i);
                                                dialog.dismiss();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {

                    }
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage(
                            "Invalid passprhase (you has changed your pass or you has registered another service). Register Again, please.")
                            .setCancelable(false)
                            .setPositiveButton(ctx.getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent i = new Intent(ctx.getApplicationContext(),
                                                    RegisterActivity.class);
                                            i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
                                            ctx.startActivity(i);
                                            dialog.dismiss();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    Intent i = new Intent(activity, RegisterActivity.class);
                    i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
                    activity.startActivity(i);
                }

            } else {
                Intent i = new Intent(activity, RegisterActivity.class);
                i.putExtra(AndroidRsaConstants.PASSPHRASE, password);
                activity.startActivity(i);
            }

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
        else {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Log.e(TAG, "ERROR al crear conexión.");
            Toast.makeText(activity, ctx.getResources().getString(R.string.connection_error),
                    Toast.LENGTH_LONG)
                    .show();
            Conexion.disconnect();
        }
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            con = Conexion.getInstance(service, user, password);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            Log.d(TAG, "Excepcion XMPP");
            con = null;
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
        this.pDialog = new ProgressDialog(activity);
        this.pDialog.setMessage(" Login in... ");
        this.pDialog.show();
    }

    private Bitmap obtainBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void writeFile(byte[] data, String fileName) throws IOException {
        OutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.close();
    }
}
