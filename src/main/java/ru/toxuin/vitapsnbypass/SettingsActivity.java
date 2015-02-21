package ru.toxuin.vitapsnbypass;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.toxuin.vitapsnbypass.library.JSONParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = "UI-SETTINGS";
    public static final String SERVER_URL = "http://nighthunters.ca/psn_bypass/";


    private static SettingsActivity self;

    BackgroundService service;
    private boolean bound = false;
    private static boolean running = false;
    SharedPreferences prefs;

    ListPreference latestFW;

    /*
    ServiceConnection sConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d("ER", "Service connected from Settings Activity!");
            service = ((BackgroundService.BackgroundServiceBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ER", "Service disconnected!");
            service = null;
        }
    };
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.settings_activity_title));
        addPreferencesFromResource(R.xml.settings);
        self = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(self);

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Preference myip = findPreference("myIp");
        if (wm.getConnectionInfo().getIpAddress() != -1) {
            myip.setSummary(ip);
        }

        ListPreference region = (ListPreference) findPreference("region");
        region.setEntries(new String[] {"US", "EU", "UK", "AU", "RU", "JP", "CN"});
        region.setEntryValues(new String[]{"us", "eu", "uk", "au", "ru", "jp", "cn"});
        region.setDefaultValue("us");
        region.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return handleFWChange();
            }
        });

        final Preference port = findPreference("port");
        port.setDefaultValue(8899);
        port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val;
                try {
                    val = Integer.parseInt(newValue.toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(self, newValue + " is an invalid port!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if ((!newValue.toString().equals("")  &&  newValue.toString().matches("\\d*")) && (val > 999 && val < 65535)) {
                    return true;
                } else {
                    Toast.makeText(self, newValue + " is an invalid port!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        latestFW = (ListPreference) findPreference("lastFirmware");
        latestFW.setEntries(new String[] {"Please update first"});
        latestFW.setEntryValues(new String[]{"-1"});

        latestFW.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (!o.toString().equals("-1")) {
                    new FWFileDownloader().execute(o.toString(), prefs.getString("region", "us"));
                    return true;
                } else {
                    new DataUpdater().execute();
                    return false;
                }
            }
        });

        Preference startService = findPreference("startService");
        startService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                int portNum = Integer.parseInt(prefs.getString("port", "8899"));
                if (portNum > 65535 || portNum < 1000) {
                    Toast.makeText(getApplicationContext(), "Port needs to be a number between 1000 and 65535!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                startService();
                //SettingsHelper.clearLocationData();
                return true;
            }
        });

        Preference stopService = findPreference("stopService");
        stopService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                stopService();
                return true;
            }
        });

        Preference refresh = findPreference("refreshData");
        refresh.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new DataUpdater().execute();
                return true;
            }
        });
    }

    private boolean handleFWChange() {
        String region = prefs.getString("region", "us");
        String version = prefs.getString("lastFirmware", "335");
        BackgroundService.setFWFile(getFile(version, region));
        return true;
    }

    private File getFile(String version, String region) {
        File fwfolder = new File(getFilesDir(), "fwfiles");
        fwfolder.mkdirs();
        return new File(fwfolder, version + "." + region + ".xml");
    }


    private void startService() {
        if (!running) {
            Log.d(TAG, "STARTING SERVICE");
            Intent intent = new Intent(this, BackgroundService.class);
            intent.putExtra("port", Integer.parseInt(prefs.getString("port", "8899")));
            startService(intent);
            //bound = getApplicationContext().bindService(new Intent(getApplicationContext(), BackgroundService.class), sConn, 0);
        }
    }

    private void stopService() {
        if (!running) return;
        Log.d(TAG, "STOPPING SERVICE");
        Intent intent = new Intent(this, BackgroundService.class);
        stopService(intent);
    }

    public static void setRunning(boolean running) {
        SettingsActivity.running = running;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bound) {
            //bound = getApplicationContext().bindService(new Intent(getApplicationContext(), BackgroundService.class), sConn, 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!bound) return;
        //getApplicationContext().unbindService(sConn);
        bound = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!bound) return;
        //getApplicationContext().unbindService(sConn);
        bound = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bound) return;
        //bound = getApplicationContext().bindService(new Intent(getApplicationContext(), BackgroundService.class), sConn, 0);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }






    // GETS FRESH SHIT AND POPULATES MAPS
    private class DataUpdater extends AsyncTask<Void, Void, JSONObject> {
        private static final String TAG_ROOT = "firmwares";

        @Override
        protected void onPreExecute() {
            Toast.makeText(self, "Loading fresh data...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            if (isNetworkAvailable(self)) {
                return new JSONParser().getJSONFromUrl(SERVER_URL);
            } else return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (!isNetworkAvailable(self)) {
                Toast.makeText(self, "No internet connectrion detected!", Toast.LENGTH_SHORT).show();
                return;
            } else if (json == null) {
                Toast.makeText(self, "No info from server. Check your connection settings.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Log.d("JSON", json.toString());
                JSONArray array = json.getJSONArray(TAG_ROOT);

                if (json.isNull(TAG_ROOT)) {
                    Log.e("JSON", json.getString("error"));
                    Toast.makeText(self, "Got error: " + json.getString("error"), Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> fws = new HashMap<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject fw = array.getJSONObject(i);
                    fws.put(fw.getString("version"), fw.getString("url"));
                }
                latestFW.setEntries(fws.keySet().toArray(new String[fws.keySet().size()]));
                latestFW.setEntryValues(fws.values().toArray(new String[fws.values().size()]));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }











    private class FWFileDownloader extends AsyncTask<String, Integer, File> {
        private ProgressDialog pDialog;
        private FWFileDownloader me = null;

        @Override
        protected void onPreExecute() {
            me = this;
            pDialog = new ProgressDialog(self);
            pDialog.setMessage("Getting fw file...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (me == null) return;
                    me.cancel(true);
                }
            });
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgress(progress[0]);
        }

        @Override
        protected File doInBackground(String... strings) {
            if (strings.length < 2) return null;
            String version = strings[0];
            String region = strings[1];
            if (version.equals("") || region.equals("")) return null;

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            File fwfile = getFile(version, region);

            try {
                URL url = new URL(SERVER_URL + "fwfiles/" + version + "." + region + ".xml");
                Log.d(TAG, "URL: " + url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Toast.makeText(self, "HTTP ERROR " + connection.getResponseCode() + ": " + connection.getResponseMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                if (fwfile.exists() && fwfile.length() > 0) {
                    return fwfile;
                }

                //output = new FileOutputStream("/sdcard/file_name.extension");
                output = new FileOutputStream(fwfile);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (connection != null)
                    connection.disconnect();
            }
            return fwfile;
        }

        @Override
        protected void onPostExecute(File file) {
            pDialog.hide();
            if (file != null) {
                Toast.makeText(self, "Downloaded!", Toast.LENGTH_SHORT).show();
                handleFWChange();
            } else {
                Toast.makeText(self, "Downloading error.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}