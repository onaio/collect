package io.ona.collect.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.ona.collect.android.R;
import io.ona.collect.android.preferences.PreferencesActivity;
import io.ona.collect.android.utilities.WebUtils;

/**
 * Created by onamacuser on 10/13/15.
 */
public class LoginActivity extends Activity {
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private Button mCreateAccount;
    private Button mRecoverPassword;
    protected Dialog dialog;

    public static final int VALID_CRED = 1;
    public static final int INVALID_CRED = 2;
    public static final int CONN_ERR = 3;

    public static final String authUrl = "https://api.ona.io/api/v1/user.json";
    public static final String AUTHENTICATED = "AUTHENTICATED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mCreateAccount = (Button) findViewById(R.id.need_account);
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSite(R.string.ona_site_join);
            }
        });
        mRecoverPassword = (Button) findViewById(R.id.forgot_password);
        mRecoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSite(R.string.ona_site_password);
            }
        });

        // Set up progress dialog
        setUpProgressDialog();

        Button emailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoginCredentials();
            }
        });
    }

    private void goToSite(int location) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(location)));
        startActivity(browserIntent);
    }

    private void setLoginCredentials() {
        dialog.show();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        if (validateNotEmpty(username) && validateNotEmpty(password)) {
            editor.putString(PreferencesActivity.KEY_USERNAME, username);
            editor.putString(PreferencesActivity.KEY_PASSWORD, password);
            editor.commit();
            authenticateUser(username, password);
        } else {
            setErrorMessage(getResources().getString(R.string.error_username_password_empty));
            dialog.dismiss();
        }
    }

    private void authenticateUser(String username, String password) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
        } else {
            int auth = WebUtils.authenticateUser(authUrl, username, password);
            switch (auth) {
                case VALID_CRED:
                    closeLoginScreen();
                    break;
                case INVALID_CRED:
                    setErrorMessage(getString(R.string.invalid_login_credentials));
                    break;
                case CONN_ERR:
                    setErrorMessage(getString(R.string.connection_error));
                    break;
            }
        }
        dialog.dismiss();
    }

    private boolean validateNotEmpty(String value) {
        return value.length() > 0;
    }

    private void setErrorMessage(String message) {

        TextView errorBox = (TextView) findViewById(R.id.text_error_message);
        if (message != null) {
            errorBox.setVisibility(View.VISIBLE);
            errorBox.setText(message);
        } else {
            errorBox.setVisibility(View.GONE);
        }
    }

    private void closeLoginScreen() {
        setAuthenticated();

        // launch new activity and close login activity
        startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
        finish();
    }

    private void setUpProgressDialog() {
        // custom dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.login_progress);
        dialog.setTitle(getResources().getString(R.string.login_authentication));
        dialog.setCancelable(false);
    }

    private void setAuthenticated() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(AUTHENTICATED, true);
        editor.commit();
    }
}
