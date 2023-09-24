package rahman.id.weatherhub.utils;

public class ApiKeyManager {
    private static ApiKeyManager instance;

    private String apiKey;

    private ApiKeyManager() {
    }

    public static synchronized ApiKeyManager getInstance() {
        if (instance == null) {
            instance = new ApiKeyManager();
        }
        return instance;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}

