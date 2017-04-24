/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.AudioService;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.SFBException;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment
        implements ConversationHelper.ConversationCallback {

    public Button mMuteAudioButton;
    private OnFragmentInteractionListener mListener;
    private static Conversation mConversation;
    private static DevicesManager mDevicesManager;

    //// TODO: 1/13/2017 give hint re: location of the source file
	protected ConversationHelper mConversationHelper;
    View mRootView;

    /**
     * Create the Video fragment.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static SkypeCallFragment newInstance(
            Conversation conversation,
            DevicesManager devicesManager) {
        mConversation = conversation;
        mDevicesManager = devicesManager;
        SkypeCallFragment fragment = new SkypeCallFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_skype_call, container, false);


        Button mEndCallButton = (Button) mRootView.findViewById(R.id.endCallButton);
        mEndCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConversation.canLeave()) {
                    try {
                        mConversation.leave();
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mMuteAudioButton = (Button) mRootView.findViewById(R.id.muteAudioButton);
        mMuteAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConversationHelper.toggleMute();
            }
        });

        TextureView mPreviewVideoTextureView = (TextureView) mRootView.findViewById(
                R.id.selfParticipantVideoView);

        MMVRSurfaceView mParticipantVideoSurfaceView = (MMVRSurfaceView) mRootView.findViewById(R.id.mmvrSurfaceViewId);

        mParticipantVideoSurfaceView.setActivated(true);
        mPreviewVideoTextureView.setActivated(true);

        //Initialize the conversation helper with the established conversation,
        //the SfB App SDK devices manager, the outgoing video TextureView,
        //The view container for the incoming video, and a conversation helper
        //callback.
        if (mConversationHelper == null){
            mConversationHelper = new ConversationHelper(
                    mConversation,
                    mDevicesManager,
                    mPreviewVideoTextureView,
                    mParticipantVideoSurfaceView,
                    this);
            Log.i("SkypeCallFragment", "onViewCreated");

        }

        Log.i("SkypeCallFragment", "onCreateView");

        // https://github.com/OfficeDev/skype-android-app-sdk-samples/issues/41
        // First situation, with this line commented //mConversationHelper.ensureVideoIsStartedAndRunning();
        // Second situation, with this line uncommented commented mConversationHelper.ensureVideoIsStartedAndRunning();
        mConversationHelper.ensureVideoIsStartedAndRunning();

        return mRootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    /**
     * Used to interact with parent activity
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View rootView, String fragmentAction);
    }

    /**
     * Invoked when the state of the established conversation changes from
     * ESTABLISHED to IDle. State change happens when the call ends.
     * @param state
     */
    @Override
    public void onConversationStateChanged(Conversation.State state) {
        Log.i("SkypeCallFragment", "onConversationStateChanged(Conversation.State state = "+state+")");
        if (this.isDetached())
            return;

        try{
            String newState = "";
            switch (state){
                case IDLE:
                    newState = getActivity().
                            getString(R.string.leaveCall);
                    break;
                case ESTABLISHING:
                    break;
                case INLOBBY:
                    break;
                case ESTABLISHED:
                    break;
            }
            if (mListener != null) {
                mListener.onFragmentInteraction(
                        mRootView,newState);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onCanSendMessage(boolean canSendMessage) {
        Log.i("SkypeCallFragment", "onCanSendMessage(boolean canSendMessage = "+canSendMessage+")");
    }

    @Override
    public void onMessageReceived(MessageActivityItem messageActivityItem) {
        Log.d("SkypeCallFragment", "onMessageReceived(MessageActivityItem messageActivityItem = " + messageActivityItem + ")");
    }

    @Override
    public void onSelfAudioMuteChanged(final AudioService.MuteState muteState) {

        Log.i("SkypeCallFragment", "onSelfAudioMuteChanged(AudioService.MuteState newMuteState = "+muteState+")");

        String muteText = "";
        switch (muteState) {
            case MUTED:
                muteText = "Unmute";
                break;
            case UNMUTED:
                muteText = "Mute";
                break;
            case UNMUTING:
                muteText = "Unmuting";
                break;
            default:
        }
        mMuteAudioButton.setText(muteText);
    }


    /**
     * Called when the video service on the established conversation can be
     * started. Use the callback to start video.
     * @param canStartVideoService
     *
     * This seems to be called when the conversation is ended, not when it starts.
     */
    @Override
    public void onCanStartVideoServiceChanged(boolean canStartVideoService) {
        Log.i("SkypeCallFragment", "onCanStartVideoServiceChanged(boolean canStartVideoService = "+canStartVideoService+")");


        if (canStartVideoService == true) {
            mConversationHelper.startOutgoingVideo();
            mConversationHelper.startIncomingVideo();
            mConversationHelper.ensureVideoIsStartedAndRunning();
        }
    }


    /**
     * This method is called when the state of {@link VideoService#CAN_SET_PAUSED_PROPERTY_ID}
     * changes.
     *
     * @param canSetPausedVideoService The new value retrieved by calling {@link VideoService#canSetPaused()}
     */
    public void onCanSetPausedVideoServiceChanged(boolean canSetPausedVideoService) {
        Log.i("SkypeCallFragment", "onCanSetPausedVideoServiceChanged " + String.valueOf(canSetPausedVideoService));

        if (this.isDetached())
            return;

        if (canSetPausedVideoService){
            //set the pause/resume text of the SkypeCall menu
            mListener.onFragmentInteraction(mRootView,getString(R.string.pauseVideo));
        } else {
            //set the pause/resume text of the SkypeCall menu
            mListener.onFragmentInteraction(mRootView,getString(R.string.resume_video));
        }
    }

    @Override
    public void onCanSetActiveCameraChanged(boolean canSetActiveCamera) {
        Log.i("SkypeCallFragment", "onCanSetPausedVideoServiceChanged(boolean canSetActiveCamera = "+ canSetActiveCamera +")");
        if (this.isDetached())
            return;

        if (mListener != null){
            mListener.onFragmentInteraction(mRootView, getString(R.string.canToggleCamera));
        }

    }
}
