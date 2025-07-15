package com.example.myapplication.socket;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String groupKey = intent.getStringExtra("groupKey");
        if (groupKey != null) {
            cancelGroupNotifications(context, groupKey);
        }
    }

    // 그룹 내의 모든 알림을 취소하는 메서드
    private void cancelGroupNotifications(Context context, String groupKey) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (groupKey.equals(notification.getNotification().getGroup())) {
                    notificationManager.cancel(notification.getId());
                }
            }
        }
    }

}
