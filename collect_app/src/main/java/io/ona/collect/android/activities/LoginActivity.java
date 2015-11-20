package io.ona.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.ona.collect.android.R;
import io.ona.collect.android.preferences.PreferencesActivity;

/**
 * Created by onamacuser on 10/13/15.
 */
public class LoginActivity extends Activity {
    // UI references.
    private EditText m_usernameView;
    private EditText m_passwordView;
    private Button mCreateAccount;
    private Button mRecoverPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        // Set up the login form.
        m_usernameView = (EditText) findViewById(R.id.username);
        m_passwordView = (EditText) findViewById(R.id.password);
        mCreateAccount = (Button) findViewById(R.id.need_account);
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOnaSite();
            }
        });
        mRecoverPassword = (Button) findViewById(R.id.forgot_password);
        mRecoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOnaSite();
            }
        });

        Button emailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLoginCredentials();
            }
        });
    }

    private void goToOnaSite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.ona_site)));
        startActivity(browserIntent);
    }

    private void setLoginCredentials() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        String username = m_usernameView.getText().toString().trim();
        String password = m_passwordView.getText().toString().trim();

        if (validateNotEmpty(username) && validateNotEmpty(password)) {
            editor.putString(PreferencesActivity.KEY_USERNAME, username);
            editor.putString(PreferencesActivity.KEY_PASSWORD, password);
            editor.commit();
            closeLoginScreen();
        } else {
            setErrorMessage(getResources().getString(R.string.error_username_password_empty));
        }
    }

    private boolean validateNotEmpty(String value) {
        return value.length() > 0;
    }

    private void setErrorMessage(String message) {

        TextView errorBox  = (TextView) findViewById(R.id.text_error_message);
        if (message != null) {
            errorBox.setVisibility(View.VISIBLE);
            errorBox.setText(message);
        } else {
            errorBox.setVisibility(View.GONE);
        }
    }

    private void closeLoginScreen() {

        // launch new activity and close menu activity
        startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
        finish();
    }
}
