/*
 *  This file is part of Sensorium.
 *
 *   Sensorium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Sensorium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Sensorium. If not, see
 *   <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package at.univie.sensorium.preferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import at.univie.sensorium.R;
import at.univie.sensorium.SensorRegistry;

import com.google.gson.stream.JsonReader;

public class Preferences {

	public static final String SENSOR_AUTOSTART_PREF = "sensor_autostart";
	public static final String INTERFACES_XMLRPC_PREF = "xmlrpc_enabled";
	public static final String UPLOAD_URL_PREF = "upload_url";
	public static final String UPLOAD_USERNAME = "o3gm_upload_user";
	public static final String UPLOAD_PASSWORD = "o3gm_upload_user_password";
	public static final String UPLOAD_AUTOMATIC_PREF = "upload_automatic";
	public static final String UPLOAD_WIFI_PREF = "upload_wifi";
	public static final String UPLOAD_INTERVAL_PREF = "upload_interval";
	public static final String PRIVACY_HASH = "privacy_hash";
	// public static final String FIRST_RUN = "first_run";
	public static final String PREFERENCES_VERSION = "preferences_version";
	
	public static final String WELCOME_SCREEN_SHOWN = "welcome_screen_shown";

	private Context context;
	private SharedPreferences prefs;

	public Preferences(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void putPreference(String key, String value) {
		Log.d(SensorRegistry.TAG, "Setting pref " + key + " from remote config to: " + value);

		if (value.toLowerCase(Locale.US).equals("true")) {
			putBoolean(key, true);
		} else if (value.toLowerCase(Locale.US).equals("false")) {
			putBoolean(key, false);
		} else if (isInt(value)) {
			putInt(key, Integer.parseInt(value));
		} else { // value is String
			putString(key, value);
		}
	}

	private boolean isInt(String str) {
		try {
			int i = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void putBoolean(String key, Boolean value) {
		prefs.edit().putBoolean(key, value).commit();
		Log.d(SensorRegistry.TAG, key + " boolean " + String.valueOf(value));
	}

	public void putInt(String key, Integer value) {
		prefs.edit().putInt(key, value).commit();
		Log.d(SensorRegistry.TAG, key + " int " + value);
	}

	public void putString(String key, String value) {
		prefs.edit().putString(key, value).commit();
		Log.d(SensorRegistry.TAG, key + " String " + value);
	}

	public Integer getInt(String key, Integer defaultvalue) {
		return prefs.getInt(key, defaultvalue);
	}

	public Boolean getBoolean(String key, Boolean defaultvalue) {
		return prefs.getBoolean(key, defaultvalue);
	}

	public String getString(String key, String defaultvalue) {
		return prefs.getString(key, defaultvalue);
	}


	public void loadDefaultPreferences() {
		loadPrefsFromStream(context.getResources().openRawResource(R.raw.defaultpreferences));
	}

	public void loadCampaignPreferences(final String u) {
		urlstring = u;
		Thread x = new Thread(prefretriever);
		x.start();

	}

	private void loadPrefsFromStream(InputStream input) {
		List<BasicNameValuePair> preferencelist = new LinkedList<BasicNameValuePair>();
		try {
			InputStreamReader isreader = new InputStreamReader(input);
			JsonReader reader = new JsonReader(isreader);
//			String jsonVersion = "";

			reader.beginArray(); // do we have an array or just a single object?
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				String value = reader.nextString();
				if (name.equalsIgnoreCase(PREFERENCES_VERSION))
					currentPrefVersion = Integer.valueOf(value);
				BasicNameValuePair kv = new BasicNameValuePair(name, value);
				preferencelist.add(kv);
			}
			reader.endObject();
			reader.endArray();
			reader.close();

			if (newerPrefsAvailable()) {
				Log.d(SensorRegistry.TAG, "Newer preferences available in json, overwriting existing.");
				for (BasicNameValuePair kv : preferencelist) {
					putPreference(kv.getName(), kv.getValue());
				}
				// also reset the welcome screen
				putBoolean(WELCOME_SCREEN_SHOWN, false);
			} else {
				Log.d(SensorRegistry.TAG, "Preferences are recent, not overwriting.");
			}

		} catch (FileNotFoundException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Log.d(SensorRegistry.TAG, sw.toString());
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Log.d(SensorRegistry.TAG, sw.toString());
		}
	}

	private int currentPrefVersion;
	private boolean newerprefsavailable = false;
	public boolean newerPrefsAvailable() {
		if (currentPrefVersion > getInt(PREFERENCES_VERSION, 0))
			newerprefsavailable = true;
//		return false;
		return newerprefsavailable;
	}

	private String urlstring;
	private Runnable prefretriever = new Runnable() {
		@Override
		public void run() {
			try {
				URL url = new URL(urlstring);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout(1000);
				loadPrefsFromStream(urlConnection.getInputStream());
				Log.d(SensorRegistry.TAG, "Done loading remote preferences");
			} catch (IOException e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				Log.d(SensorRegistry.TAG, sw.toString());
			}
		}
	};
}
