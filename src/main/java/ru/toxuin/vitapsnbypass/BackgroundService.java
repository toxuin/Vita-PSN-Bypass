package ru.toxuin.vitapsnbypass;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.InspectorFilterSourceAdapter;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import ru.toxuin.vitapsnbypass.library.UpdateXmlManipulator;

import java.io.File;

public class BackgroundService extends Service {
    static final int PERSISTENT_NOTIFICATION_ID = 1;
    private static final String TAG = "PSN-PROXY-SERVICE";
    private static BackgroundService self;

    private final IBinder binder = new BackgroundServiceBinder();
    NotificationManager notifications;

    private HttpProxyServer proxy;

    private int port = 8899;
    private static File fwfile = null;

    public static File getFWFile() {
        return fwfile;
    }

    public static void setFWFile(File file) {
        fwfile = file;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;

        notifications = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d(TAG, "Service spawned.");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (proxy == null && getFWFile() != null) {
            port = intent.getIntExtra("port", 8899);
            Toast.makeText(this, "Service started on port " + port, Toast.LENGTH_SHORT).show();
            SettingsActivity.setRunning(true);


            Notification notif = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("PSN Bypass service is running...")
                    .setSmallIcon(R.drawable.laughing_man)
                    .setOngoing(true)
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, SettingsActivity.class), 0))
                    .getNotification();

            notifications.notify(PERSISTENT_NOTIFICATION_ID, notif);
            startForeground(PERSISTENT_NOTIFICATION_ID, notif);

            proxy = DefaultHttpProxyServer.bootstrap()
                    .withFiltersSource(new InspectorFilterSourceAdapter(new UpdateXmlManipulator()))
                    .withPort(port)
                    .withAllowLocalOnly(false)
                    .start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        proxy.stop();
        proxy = null;
        notifications.cancelAll();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        SettingsActivity.setRunning(false);
        Log.d(TAG, "Service killed.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }


    class BackgroundServiceBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
