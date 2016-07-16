package com.festival.tacademy.festivalmate.GCM;

/**
 * Created by Tacademy on 2016-05-30.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.festival.tacademy.festivalmate.Data.MateTalkRoom;
import com.festival.tacademy.festivalmate.Data.RequestNewChatResult;
import com.festival.tacademy.festivalmate.Data.User;
import com.festival.tacademy.festivalmate.MainActivity;
import com.festival.tacademy.festivalmate.Manager.NetworkManager;
import com.festival.tacademy.festivalmate.Manager.PropertyManager;
import com.festival.tacademy.festivalmate.MateTalk.ChattingActivity;
import com.festival.tacademy.festivalmate.MateTalk.Receive;
import com.festival.tacademy.festivalmate.MyApplication;
import com.festival.tacademy.festivalmate.MyPage.SettingsActivity;
import com.festival.tacademy.festivalmate.R;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by saltfactory on 6/8/15.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Request;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    public static final String ACTION_CHAT = "com.festival.tacademy.festivalmate.action.chat";
    public static final String EXTRA_SENDER_RESULT = "senderresult";
    public static final String EXTRA_SENDER_NO = "senderno";
    public static final String EXTRA_RESULT = "result";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    RequestNewChatResult newChatResult;
    @Override
    public void onMessageReceived(String from, Bundle data) {

//        String type = data.getString("type");
//        String senderid = data.getString("sender");
//        final int roomid = Integer.parseInt(data.getString("chatroom_no"));

        final String room = data.getString("chatroom_no");
        final String roomname = data.getString("chatroom_name");
        final String sendername = data.getString("sender_name");
        final String style = data.getString("chatroom_style");
        final int roomid = Integer.parseInt(room);
        final int room_style = Integer.parseInt(style);

        // final int roomid = data.getInt("chatroom_no");
        newChatResult = new RequestNewChatResult();


        final String[] message1 = new String[1];

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {

            // normal downstream message.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.KOREA);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date = sdf.format(new Date(0));
            try {
                RequestNewChatResult result = NetworkManager.getInstance().request_new_chat(MyApplication.getContext(), PropertyManager.getInstance().getNo(), roomid, date);

                // db save...

                Intent intent = new Intent(ACTION_CHAT);
                intent.putExtra(EXTRA_SENDER_RESULT, result);
                intent.putExtra(EXTRA_SENDER_NO,roomid);
                LocalBroadcastManager.getInstance(this).sendBroadcastSync(intent);
                boolean isProcessed = intent.getBooleanExtra(EXTRA_RESULT, false);


                if (!isProcessed && SettingsActivity.switch_alarm) {

                    MateTalkRoom mateTalkRoom = new MateTalkRoom();
                    mateTalkRoom.setChatroom_no(roomid);
                    mateTalkRoom.setChatroom_name(roomname);
                    mateTalkRoom.setChatroom_style(room_style);

                    sendNotification(sendername + "님 : " + result.result.get(result.result.size() - 1).chat_content, mateTalkRoom);

                }

            } catch (IOException e) {
                //     Toast.makeText(MyApplication.getContext(), "제발",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        //     sendNotification(message1[0]);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */

    private void sendNotification(String message, MateTalkRoom room) {

        Intent intent = new Intent(this, ChattingActivity.class);
        intent.putExtra("chatting",room);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(MyApplication.getContext().getResources(),
                R.drawable.icon);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setTicker("GCM message")
                .setSmallIcon(R.drawable.icon)
                //.setLargeIcon(icon)
                .setContentTitle("메시지가 도착했습니다.")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}