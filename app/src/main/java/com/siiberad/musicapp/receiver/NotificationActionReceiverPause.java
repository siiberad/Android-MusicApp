package com.siiberad.musicapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.siiberad.musicapp.event.PlaybackCommand;

import org.greenrobot.eventbus.EventBus;

public class NotificationActionReceiverPause extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        String action = intent.getStringExtra("pauseaction");

        if(action.equals("pause")) {
            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PAUSE));
        }
    }
}
