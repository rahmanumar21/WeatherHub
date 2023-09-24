package rahman.id.weatherhub.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import rahman.id.weatherhub.utils.ApiInitializer;
import rahman.id.weatherhub.utils.ApiKeyManager;
import rahman.id.weatherhub.utils.DatabaseHelper;
import rahman.id.weatherhub.R;
import rahman.id.weatherhub.model.WeatherResponse;
import rahman.id.weatherhub.api.WeatherService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView    textViewFullName,
                        textViewEmail,
                        weatherDescription,
                        currentTemperature,
                        humidity,
                        wind,
                        locationText;
    SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private EditText cityEditText;
    private Button searchButton;
    private WeatherService weatherService;
    private ImageView weatherIcon;
    private final String API_KEY = ApiKeyManager.getInstance().getApiKey();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        textViewFullName = findViewById(R.id.textViewFullName);
        textViewEmail = findViewById(R.id.textViewEmail);
        dbHelper = new DatabaseHelper(this);
        cityEditText = findViewById(R.id.cityEditText);
        searchButton = findViewById(R.id.searchButton);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherDescription = findViewById(R.id.weatherDescription);
        currentTemperature = findViewById(R.id.currentTemperature);
        humidity = findViewById(R.id.humidity);
        wind = findViewById(R.id.wind);
        locationText = findViewById(R.id.locationText);
        weatherService = ((ApiInitializer) getApplication()).getWeatherService();

        checkLocationPermission();
        searchWeatherByUserInput();
        checkLoginStatus();
        userLogout();
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            String loggedInUserEmail = sharedPreferences.getString("user_email", "");
            if (!loggedInUserEmail.isEmpty()) {
                displayUserProfile(loggedInUserEmail);
            }
        } else {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void displayUserProfile(String userEmail) {
        db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM users WHERE email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            String fullName = cursor.getString(cursor.getColumnIndex("full_name"));
            String email = cursor.getString(cursor.getColumnIndex("email"));

            textViewFullName.setText("Full name: " + fullName);
            textViewEmail.setText("Email: " + email);
        }

        cursor.close();
        db.close();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocationAndFetchWeather();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                Toast.makeText(DashboardActivity.this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchWeatherByUserInput() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityEditText.getText().toString().trim();
                fetchWeatherByCityName(cityName);
            }
        });
    }

    private void fetchWeatherByCityName(String cityName) {
        if (!cityName.isEmpty()) {
            Call<WeatherResponse> call = weatherService.getWeatherData(cityName, API_KEY);
            call.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful()) {
                        WeatherResponse weatherResponse = response.body();
                        if (weatherResponse != null) {
                            displayWeatherData(weatherResponse);
                        }
                    } else {
                        Toast.makeText(DashboardActivity.this, "Failed to fetch weather data.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    Toast.makeText(DashboardActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(DashboardActivity.this, "Enter a city name.", Toast.LENGTH_SHORT).show();
        }

    }

    private void getLocationAndFetchWeather() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            String cityName = getCityName(latitude, longitude);
                            locationText.setText(cityName);

                            fetchWeatherByCoordinates(latitude, longitude);
                        }
                    }
                });
    }

    private void fetchWeatherByCoordinates(double latitude, double longitude) {
        Call<WeatherResponse> call = weatherService.getWeatherByCoordinates(latitude, longitude, API_KEY);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        displayWeatherData(weatherResponse);
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "Failed to fetch weather data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "An error occurred: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                return cityName;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Location Unknown";
    }

    private void displayWeatherData(WeatherResponse weatherResponse) {
        String description = weatherResponse.getWeather()[0].getDescription();
        double temperatureInKelvin = weatherResponse.getMain().getTemp();
        double convertToCelciusResult = temperatureInKelvin - 273.15;
        double roundedTemperature = Math.floor(convertToCelciusResult + 0.5);
        String temperatureInCelcius = (int) roundedTemperature + "°C";
        String cityName = weatherResponse.getName();
        String humidityValue = "Humidity: " + weatherResponse.getMain().getHumidity() + "%";
        String windValue = "Wind: " + weatherResponse.getWind().getSpeed() + " m/s";

        weatherDescription.setText(description);
        currentTemperature.setText(temperatureInCelcius);
        humidity.setText(humidityValue);
        wind.setText(windValue);
        locationText.setText(cityName);

        String iconCode = weatherResponse.getWeather()[0].getIcon();
        String iconUrl = "https://openweathermap.org/img/w/" + iconCode + ".png";
        Glide
                .with(DashboardActivity.this)
                .load(iconUrl)
                .into(weatherIcon);
    }

    private void userLogout() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_logged_in", false);
                editor.apply();

                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}