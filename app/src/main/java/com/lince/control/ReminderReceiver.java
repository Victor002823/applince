package com.lince.control;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;

public class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL_ID = "recordatorios_servicio";

    @Override
    public void onReceive(Context c, Intent i) {
        String titulo = i.getStringExtra("titulo");
        String msg = i.getStringExtra("mensaje");
        int code = i.getIntExtra("code", 0);

        // App abierta: modal directo, sin notificación
        if (MainActivity.mostrarSiVisible(titulo, msg)) {
            try {
                Ringtone r = RingtoneManager.getRingtone(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                if (r != null) r.play();
            } catch (Exception e) { }
            return;
        }

        // App cerrada o en segundo plano: notificación; al tocarla abre el modal
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Recordatorios de servicio", NotificationManager.IMPORTANCE_HIGH));

        Intent open = new Intent(c, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra("mostrar_modal", true);
        open.putExtra("titulo", titulo);
        open.putExtra("mensaje", msg);
        PendingIntent pi = PendingIntent.getActivity(c, code, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(c, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(titulo)
                .setContentText(msg)
                .setStyle(new Notification.BigTextStyle().bigText(msg))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();
        nm.notify(code, n);
    }
}
