package com.siiberad.musicapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.siiberad.musicapp.event.PlaybackCommand;
import org.greenrobot.eventbus.EventBus;

public class NotificationActionReceiverNext extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Boolean next = intent.getBooleanExtra("nextaction", true);

        if(next.equals(true)) {
            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_NEXT));
        }
    }
}
