/*
 * Copyright (C) 2018 CarbonROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tipsy.martysplace.fragments.buttons;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle; 
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;

import com.android.internal.util.cr.CrUtils;

import com.android.settings.R;
import com.android.settings.carbon.CustomSettingsPreferenceFragment;
import com.android.settings.carbon.CustomSeekBarPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Buttons extends CustomSettingsPreferenceFragment implements Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "Buttons";
    private static final String CATEGORY_KEYS = "button_keys";
    private static final String NAV_BAR_LAYOUT = "nav_bar_layout";
    private static final String SYSUI_NAV_BAR = "sysui_nav_bar";
    private static final String INVERSE_NAVBAR = "sysui_nav_bar_inverse";
    private static final String VOLUME_BUTTON_MUSIC_CONTROL = "volume_button_music_control";
    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";
    private static final String SWAP_VOLUME_KEYS_ON_ROTATION = "swap_volume_keys_on_rotation";
    private static final String KEY_BUTON_BACKLIGHT_OPTIONS = "button_lights_category";

    private ListPreference mNavBarLayout;
    private ListPreference mTorchPowerButton;
    private ContentResolver mResolver;
    private CustomSeekBarPreference mBrightness;
    private CustomSeekBarPreference mTimeout;
    private SwitchPreference mEnabled;
    private SwitchPreference mTouch;
    private SwitchPreference mScreen;
    private PreferenceCategory mButtonBackLightCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.buttons);
        addCustomPreference(findPreference(INVERSE_NAVBAR), SECURE_TWO_STATE, STATE_OFF);
        addCustomPreference(findPreference(VOLUME_BUTTON_MUSIC_CONTROL), SYSTEM_TWO_STATE, STATE_OFF);
        addCustomPreference(findPreference(SWAP_VOLUME_KEYS_ON_ROTATION), SYSTEM_TWO_STATE, STATE_OFF);

        boolean isEnabled = Settings.System.getIntForUser(mResolver,
                Settings.System.BUTTON_BACKLIGHT_ENABLE, 1, UserHandle.USER_CURRENT) == 1;
        mEnabled = (SwitchPreference) findPreference("button_backlight_enable");
        mEnabled.setChecked(isEnabled);
        mEnabled.setOnPreferenceChangeListener(this);

        boolean isTouch = Settings.System.getIntForUser(mResolver,
                Settings.System.BUTTON_BACKLIGHT_ON_TOUCH_ONLY, 1, UserHandle.USER_CURRENT) == 1;
        mTouch = (SwitchPreference) findPreference("button_backlight_on_touch_only");
        mTouch.setChecked(isTouch);
        mTouch.setOnPreferenceChangeListener(this);

        boolean isScreen = Settings.System.getIntForUser(mResolver,
                Settings.System.CUSTOM_BUTTON_USE_SCREEN_BRIGHTNESS, 1, UserHandle.USER_CURRENT) == 1;
        mScreen = (SwitchPreference) findPreference("custom_button_use_screen_brightness");
        mScreen.setChecked(isScreen);
        mScreen.setOnPreferenceChangeListener(this);

        int value = Settings.System.getIntForUser(mResolver,
                Settings.System.CUSTOM_BUTTON_BRIGHTNESS, 1, UserHandle.USER_CURRENT);
        mBrightness = (CustomSeekBarPreference) findPreference("custom_button_brightness");
        mBrightness.setValue(value);
        mBrightness.setOnPreferenceChangeListener(this);
        mBrightness.setEnabled(isEnabled);

        int timeoutValue = Settings.System.getIntForUser(mResolver,
                Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 1, UserHandle.USER_CURRENT);
        mTimeout = (CustomSeekBarPreference) findPreference("button_backlight_timeout");
        mTimeout.setValue(timeoutValue);
        mTimeout.setOnPreferenceChangeListener(this);
        mTimeout.setEnabled(isEnabled);
        final boolean enableBacklightOptions = getResources().getBoolean(
                com.android.internal.R.bool.config_button_brightness_support);

        mButtonBackLightCategory = (PreferenceCategory) findPreference(KEY_BUTON_BACKLIGHT_OPTIONS);

        if (!enableBacklightOptions) {
            mButtonBackLightCategory.getParent().removePreference(mButtonBackLightCategory);
        }

        mNavBarLayout = (ListPreference) findPreference(NAV_BAR_LAYOUT);
        mNavBarLayout.setOnPreferenceChangeListener(this);
        String navBarLayoutValue = Settings.Secure.getString(mResolver, SYSUI_NAV_BAR);
        if (navBarLayoutValue != null) {
            mNavBarLayout.setValue(navBarLayoutValue);
        } else {
            mNavBarLayout.setValueIndex(0);
        }

        PreferenceScreen prefSet = getPreferenceScreen();
        final PreferenceCategory keysCategory =  
                (PreferenceCategory) prefSet.findPreference(CATEGORY_KEYS);
        if (!CrUtils.deviceSupportsFlashLight(getContext())) {
            Preference toRemove = (Preference) prefSet.findPreference(TORCH_POWER_BUTTON_GESTURE);
            if (toRemove != null) {
                keysCategory.removePreference(toRemove);
            }
        } else {
            mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
            mTorchPowerButton.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mNavBarLayout) {
            Settings.Secure.putString(mResolver,
                        SYSUI_NAV_BAR, (String) objValue);
            return true;
        }
        if (preference == mTorchPowerButton) {
            int torchPowerButtonValue = Integer.valueOf((String) objValue);
            boolean doubleTapCameraGesture = Settings.Secure.getInt(mResolver,
                    Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0) == 0;
            if (torchPowerButtonValue == 1 && doubleTapCameraGesture) {
                // if double-tap for torch is enabled, switch off double-tap for camera
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 1);
                Toast.makeText(getActivity(),
                        (R.string.torch_power_button_gesture_dt_toast),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (preference == mEnabled) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_ENABLE, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mEnabled.setChecked(value);
            mTimeout.setEnabled(value);
            mBrightness.setEnabled(value);
            return true;
        } else if (preference == mTouch) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_ON_TOUCH_ONLY, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mTouch.setChecked(value);
            return true;
        } else if (preference == mScreen) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_USE_SCREEN_BRIGHTNESS, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mScreen.setChecked(value);
            return true;
        } else if (preference == mBrightness) {
            int val = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mTimeout) {
            int val = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, val,
                    UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.buttons;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    if (!CrUtils.deviceSupportsFlashLight(context)) {
                        keys.add(TORCH_POWER_BUTTON_GESTURE);
                    }
                    return keys;
                }
            };
}
