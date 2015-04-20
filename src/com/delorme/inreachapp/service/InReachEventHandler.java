package com.delorme.inreachapp.service;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * An inReach Event Handler. Send a MSG_REGISTER_MESSENGER with the
 * replyTo set to your messenger to receive messages.
 * 
 * @author Eric Semle
 * @since inReachApp (07 May 2012)
 * @version 1.0
 * @bug AND-1009
 */
public class InReachEventHandler extends Handler
{
    /**
     * Dispatches messages to registered messengers.
     * 
     * @author Eric Semle
     * @since inReachApp (07 May 2012)
     * @version 1.0
     * @bug AND-1009
     */
    @Override
    public void handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case MSG_REGISTER_MESSENGER:
                m_messengers.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_MESSENGER:
                m_messengers.remove(msg.replyTo);
                break;
            default:
                for (int i = m_messengers.size()-1; i >= 0; i--)
                {
                    try
                    {
                        m_messengers.get(i).send(Message.obtain(msg));
                    }
                    catch (RemoteException e)
                    {
                        // The client is dead. Remove it from the list
                        m_messengers.remove(i);
                    }
                }
                break;
        }
    }
    
    /**
     * Registers a client to receive messages. The Message.replyTo
     * should be sent to the clients Messager.
     */
    public static final int MSG_REGISTER_MESSENGER = -1;
    
    /**
     * Unregisters a client to receive messages. The Message.replyTo
     * should be sent to the clients Messager.
     */
    public static final int MSG_UNREGISTER_MESSENGER = -2;
    
    /** Keeps track of all current registered messengers. */
    ArrayList<Messenger> m_messengers = new ArrayList<Messenger>();
}
