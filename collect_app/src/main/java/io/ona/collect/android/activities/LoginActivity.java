package io.ona.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;

import io.ona.collect.android.R;
import io.ona.collect.android.utils.MqttUtils;

/**
 * The first activity opened when the app is launched.
 * First activity was previously {@link org.odk.collect.android.activities.SplashScreenActivity}
 *
 * Created by Jason Rogena - jrogena@ona.io on 16/11/2016
 */

public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final boolean EXIT = true;
    private boolean firstTimeRender = true;

    private AlertDialog mAlertDialog;
    private ScrollView canvasSV;
    private RelativeLayout canvasRL;
    private LinearLayout credentialsLL;
    private ImageView bannerIV;
    private TextView welcomeMessageTV;
    private TextView instructionsTV;
    private TextView needAccountTV;
    private TextView forgotPasswordTV;
    private EditText usernameET;
    private EditText passwordET;
    private Button loginB;
    private TextInputLayout usernameTIL;
    private TextInputLayout passwordTIL;
    private int canvasHeight = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        if(isUserLoggedIn()) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    private void initViews() {
        canvasSV = (ScrollView) findViewById(R.id.canvasSV);
        canvasRL = (RelativeLayout) findViewById(R.id.canvasRL);
        credentialsLL = (LinearLayout) findViewById(R.id.credentialsLL);
        bannerIV = (ImageView) findViewById(R.id.bannerIV);
        welcomeMessageTV = (TextView) findViewById(R.id.welcomeMessageTV);
        instructionsTV = (TextView) findViewById(R.id.instructionsTV);
        welcomeMessageTV.setText(getResources().getString(R.string.welcome_message, getResources().getString(R.string.app_name)));
        instructionsTV.setText(getResources().getString(R.string.sign_in_instructions, getResources().getString(R.string.company_name)));
        usernameET = (EditText) findViewById(R.id.usernameET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        needAccountTV = (TextView) findViewById(R.id.needAccountTV);
        needAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getResources().getString(R.string.create_account_link)));
                startActivity(intent);
            }
        });
        forgotPasswordTV = (TextView) findViewById(R.id.forgotPasswordTV);
        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getResources().getString(R.string.password_reset_link)));
                startActivity(intent);
            }
        });
        loginB = (Button) findViewById(R.id.loginB);
        loginB.setOnClickListener(this);
        usernameTIL = (TextInputLayout) findViewById(R.id.usernameTIL);
        passwordTIL = (TextInputLayout) findViewById(R.id.passwordTIL);
        positionViews();
    }

    /**
     * This method checks whether there's already a user logged into Ona Collect
     *
     * @return TRUE if a user is already logged in
     */
    private boolean isUserLoggedIn() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
        if(storedUsername == null
                || storedUsername.trim().length() == 0
                || storedPassword == null
                || storedPassword.trim().length() == 0) {
            return false;
        }

        return true;
    }

    /**
     * This method positions the views in their appropriate positions
     */
    private void positionViews() {
        canvasSV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                canvasSV.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if(canvasHeight == -1) {
                    canvasHeight = canvasSV.getHeight();
                }

                int ivMargin = (canvasHeight/2)
                        - (credentialsLL.getHeight() / 2)
                        - ((RelativeLayout.LayoutParams)credentialsLL.getLayoutParams()).topMargin
                        - (bannerIV.getHeight())
                        - instructionsTV.getHeight()
                        - ((RelativeLayout.LayoutParams)instructionsTV.getLayoutParams()).topMargin
                        - welcomeMessageTV.getHeight()
                        - ((RelativeLayout.LayoutParams)welcomeMessageTV.getLayoutParams()).topMargin;
                ivMargin = ivMargin / 2;
                RelativeLayout.LayoutParams bannerIVLayoutParams = (RelativeLayout.LayoutParams)bannerIV.getLayoutParams();
                bannerIVLayoutParams.setMargins(0, ivMargin, 0, ivMargin);
                bannerIV.setLayoutParams(bannerIVLayoutParams);

                int tvMargin = credentialsLL.getHeight()
                        + ((RelativeLayout.LayoutParams)credentialsLL.getLayoutParams()).topMargin
                        + ((RelativeLayout.LayoutParams)credentialsLL.getLayoutParams()).bottomMargin;

                RelativeLayout.LayoutParams needAccountLayoutParams = (RelativeLayout.LayoutParams) needAccountTV.getLayoutParams();
                needAccountLayoutParams.setMargins(0, tvMargin, 0, 0);

                canvasRL.setMinimumHeight(canvasHeight);
            }
        });
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

    private boolean validateInput() {
        boolean response = true;
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        if(response == true && (username == null || username.trim().length() == 0)) {
            usernameTIL.setErrorEnabled(true);
            usernameTIL.setError(getResources().getString(R.string.please_enter_username));
            response = false;
        } else {
            usernameTIL.setErrorEnabled(false);
            usernameTIL.setError(null);
        }

        if(response == true && (password == null || password.trim().length() == 0)) {
            passwordTIL.setErrorEnabled(true);
            passwordTIL.setError(getResources().getString(R.string.please_enter_password));
            response = false;
        } else {
            passwordTIL.setErrorEnabled(false);
            passwordTIL.setError(null);
        }

        positionViews();

        return response;
    }

    private boolean saveCredentials() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PreferencesActivity.KEY_USERNAME, usernameET.getText().toString());
        editor.putString(PreferencesActivity.KEY_PASSWORD, passwordET.getText().toString());
        editor.apply();
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(loginB)) {
            if(validateInput() && saveCredentials()) {
                MqttUtils.initMqttAndroidClient();
                startActivity(new Intent(this, MainMenuActivity.class));
                finish();
            }
        }
    }
}
