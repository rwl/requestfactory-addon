package org.springframework.roo.addon.requestfactory.android.types;

import org.springframework.roo.model.JavaType;

public enum SystemService {
    WINDOW_MANAGER (new JavaType("android.view.WindowManager"), "WINDOW_SERVICE"),
    LAYOUT_INFLATER (new JavaType("android.view.LayoutInflater"), "LAYOUT_INFLATER_SERVICE"),
    ACTIVITY_MANAGER (new JavaType("android.app.ActivityManager"), "ACTIVITY_SERVICE"),
    POWER_MANAGER (new JavaType("android.os.PowerManager"), "POWER_SERVICE", Permission.WAKE_LOCK),
    ALARM_MANAGER (new JavaType("android.app.AlarmManager"), "ALARM_SERVICE"),
    NOTIFICATION_MANAGER (new JavaType("android.app.NotificationManager"), "NOTIFICATION_SERVICE"),
    KEYGUARD_MANAGER (new JavaType("android.app.KeyguardManager"), "KEYGUARD_SERVICE", Permission.DISABLE_KEYGUARD),
    LOCATION_MANAGER (new JavaType("android.location.LocationManager"), "LOCATION_SERVICE", Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_MOCK_LOCATION),
    SEARCH_MANAGER (new JavaType("android.app.SearchManager"), "SEARCH_SERVICE"),
    SENSOR_MANAGER (new JavaType("android.hardware.SensorManager"), "SENSOR_SERVICE"),
    STORAGE_MANAGER (new JavaType("android.os.storage.StorageManager"), "STORAGE_SERVICE"),
    VIBRATOR (new JavaType("android.os.Vibrator"), "VIBRATOR_SERVICE", Permission.VIBRATE),
    CONNECTIVITY_MANAGER (new JavaType("android.net.ConnectivityManager"), "CONNECTIVITY_SERVICE"),
    WIFI_MANAGER (new JavaType("android.net.wifi.WifiManager"), "WIFI_SERVICE", Permission.INTERNET),
    AUDIO_MANAGER (new JavaType("android.media.AudioManager"), "AUDIO_SERVICE", Permission.MODIFY_AUDIO_SETTINGS, Permission.RECORD_AUDIO),
    TELEPHONY_MANAGER (new JavaType("android.telephony.TelephonyManager"), "TELEPHONY_SERVICE", Permission.CALL_PHONE, Permission.MODIFY_PHONE_STATE, Permission.READ_PHONE_STATE),
    INPUT_METHOD_MANAGER (new JavaType("android.view.inputmethod.InputMethodManager"), "INPUT_METHOD_SERVICE", Permission.BIND_INPUT_METHOD),
    UI_MODE_MANAGER (new JavaType("android.app.UiModeManager"), "UI_MODE_SERVICE"),
    DOWNLOAD_MANAGER (new JavaType("android.app.DownloadManager"), "DOWNLOAD_SERVICE");
    
    private final JavaType serviceType;
    
    private final String contextField;
    
    private final Permission[] permissions;
    
    private SystemService(final JavaType serviceType,
            final String contextField, Permission... permissions) {
        this.serviceType = serviceType;
        this.contextField = contextField;
        this.permissions = permissions;
    }
    
    public JavaType getServiceType() {
        return serviceType;
    }
    
    public String getContextField() {
        return contextField;
    }
    
    public Permission[] getPermissions() {
        return permissions;
    }
    
    public static SystemService forType(final JavaType javaType) {
        for (SystemService service : SystemService.values()) {
            if (javaType.equals(service.getServiceType())) {
                return service;
            }
        }
        return null;
    }
}
