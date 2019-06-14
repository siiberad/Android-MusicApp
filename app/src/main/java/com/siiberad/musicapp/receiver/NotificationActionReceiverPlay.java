package com.siiberad.musicapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.siiberad.musicapp.event.PlaybackCommand;

import org.greenrobot.eventbus.EventBus;

public class NotificationActionReceiverPlay extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String play = intent.getStringExtra("playaction");

        if(play.equals("play")) {
            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_RESUME));
        }
    }
}
