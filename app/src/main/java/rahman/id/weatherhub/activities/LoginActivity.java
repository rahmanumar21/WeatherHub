package rahman.id.weatherhub.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.mindrot.jbcrypt.BCrypt;
import rahman.id.weatherhub.utils.DatabaseHelper;
import rahman.id.weatherhub.R;

public class LoginActivity extends AppCompatActivity {

    private EditText    loginEmailForm,
                        loginPasswordForm;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userLoginFormAction();
        userSession();
        userRegistrationFormAction();
    }

    private void userLoginFormAction() {
        loginEmailForm = findViewById(R.id.loginEmailForm);
        loginPasswordForm = findViewById(R.id.loginPasswordForm);
        dbHelper = new DatabaseHelper(this);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmailForm.getText().toString();
                String password = loginPasswordForm.getText().toString();

                SQLiteDatabase db = dbHelper.getReadableDatabase();

                String query = "SELECT * FROM users WHERE email = ?";
                Cursor cursor = db.rawQuery(query, new String[]{email});

                boolean loginSuccessful = false;

                if (cursor.moveToFirst()) {
                    String hashedPasswordFromDB = cursor.getString(cursor.getColumnIndex("password"));

                    if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                        loginSuccessful = true;
                    }
                }

                cursor.close();
                db.close();

                if (loginSuccessful) {
                    Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    intent.putExtra("user_email", email);
                    startActivity(intent);

                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_logged_in", true);
                    editor.putString("user_email", email);
                    editor.apply();


                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Check your email and password.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void userSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void userRegistrationFormAction() {
        TextView registrationButtonText = findViewById(R.id.registrationFormText);
        registrationButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }
}