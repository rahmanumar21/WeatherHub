package rahman.id.weatherhub.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.mindrot.jbcrypt.BCrypt;
import androidx.appcompat.app.AppCompatActivity;
import rahman.id.weatherhub.utils.DatabaseHelper;
import rahman.id.weatherhub.R;

public class RegistrationActivity extends AppCompatActivity {

    private EditText    registerFullNameForm,
                        registerEmailForm,
                        registerPasswordForm;
    private String  fullName,
                    email,
                    password,
                    hashedPassword;
    private long newRowId;
    private SQLiteDatabase db;
    private ContentValues values;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        TextView loginFormText = findViewById(R.id.loginFormText);

        userRegistrationFormAction();

        loginFormText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void userRegistrationFormAction() {
        registerFullNameForm = findViewById(R.id.registerFullNameForm);
        registerEmailForm = findViewById(R.id.registerEmailForm);
        registerPasswordForm = findViewById(R.id.registerPasswordForm);
        Button registerNowButton = findViewById(R.id.registerButton);

        registerNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper = new DatabaseHelper(RegistrationActivity.this);

                fullName = registerFullNameForm.getText().toString();
                email = registerEmailForm.getText().toString();
                password = registerPasswordForm.getText().toString();

                if (isEmailExists(email)) {
                    Toast.makeText(RegistrationActivity.this, "Email is already registered.", Toast.LENGTH_SHORT).show();
                    backToLoginActivity();
                } else {
                    hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                    db = dbHelper.getWritableDatabase();
                    values = new ContentValues();
                    values.put("email", email);
                    values.put("password", hashedPassword);
                    values.put("full_name", fullName);

                    newRowId = db.insert("users", null, values);
                    if (newRowId != -1) {
                        Toast.makeText(RegistrationActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                        backToLoginActivity();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }

                    db.close();
                }

            }
        });
    }

    private boolean isEmailExists(String email) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "users",
                new String[]{"id"},
                "email = ?",
                new String[]{email},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void backToLoginActivity() {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}