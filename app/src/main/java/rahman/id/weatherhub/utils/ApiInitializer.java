package rahman.id.weatherhub.utils;

import android.app.Application;
import android.content.res.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import rahman.id.weatherhub.api.RetrofitClient;
import rahman.id.weatherhub.api.WeatherService;

public class ApiInitializer extends Application {

    private WeatherService weatherService;

    @Override
    public void onCreate() {
        super.onCreate();
        apiKeyHandler();
        retrofitInitializer();
    }

    private void apiKeyHandler() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("api_config", "raw", getPackageName());
        InputStream inputStream = resources.openRawResource(resourceId);

        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String apiKey = properties.getProperty("api_key");

        ApiKeyManager.getInstance().setApiKey(apiKey);
    }

    private void retrofitInitializer() {
        weatherService = RetrofitClient.getClient().create(WeatherService.class);
    }

    public WeatherService getWeatherService() {
        return weatherService;
    }
}

