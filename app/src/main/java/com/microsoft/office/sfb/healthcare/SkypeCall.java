/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */

package com.microsoft.office.sfb.healthcare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.microsoft.office.sfb.appsdk.AnonymousSession;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * SkypeCall activity hosts one fragment. The activity shows a progress indicator and is loaded
 * immediately after this activity is inflated.  When the ConversationPropertyChangeListener reports that
 * the conversation is established, SkypeCall loads the SkypeCallFragment. The SkypeCallFragment hosts the
 * incoming video stream and a preview
 * of the outgoing stream.
 * SkypeCall finds the containing views in the SkypeCallFragment and provides those container views to
 * SkypeManagerImpl. SkypeManagerImpl sends the two video streams to the UI via those containers.
 */
public class SkypeCall extends AppCompatActivity
        implements
        SkypeCallFragment.OnFragmentInteractionListener {

    private SkypeCallFragment mCallFragment = null;
    private com.microsoft.office.sfb.appsdk.Application mApplication;
    private Conversation mConversation;
    private MenuItem mCameraToggleItem;
    private MenuItem mVideoPauseToggleItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skype_call);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        askPermissions();

        String skypeForBusinessMeetingURL = ...;

        joinTheCall(skypeForBusinessMeetingURL);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_skype_call, menu);
        mCameraToggleItem = menu.findItem(R.id.changeCamera);
        mVideoPauseToggleItem = menu.findItem(R.id.pauseVideoMenuItem);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {

                case R.id.pauseVideoMenuItem:
                    if (mConversation.getVideoService().canSetPaused() == true) {
                        Log.i("SkypeCall","select pauseVideoMenuItem ");

                        mCallFragment.mConversationHelper.toggleVideoPaused();
                    }

                    break;
                case R.id.muteAudioMenuItem:

                    break;
                case R.id.changeCamera:
                    if (mConversation.getVideoService().canSetActiveCamera() == true) {
                        mCallFragment.mConversationHelper.changeActiveCamera();
                    }
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }

        } catch (Throwable t) {
            if (t.getMessage() == null)
                Log.e("Asset", " ");
            else
                Log.e("Asset", t.getMessage());
        }
        return true;
    }

    private void askPermissions(){

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



    private void loadCallFragment() {

        mCallFragment = SkypeCallFragment.newInstance(mConversation, mApplication.getDevicesManager());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mCallFragment, "video")
                .commit();

    }

    /**
     * Invoked from SkypeCallFragment when inflated. Provides the TextureView for preview to the
     * SkypeManagerImpl
     *
     * @param callView
     * @param fragmentAction
     */
    @Override
    public void onFragmentInteraction(View callView, String fragmentAction) {
        try {
            if (fragmentAction.contentEquals(getString(R.string.leaveCall))) {
                finish();
            }
            if (fragmentAction.contentEquals(getString(R.string.canToggleCamera))) {
                //Toggle the enable state of the Change Camera
                //menu option
                if (mCameraToggleItem != null)
                    mCameraToggleItem.setEnabled(!mCameraToggleItem.isEnabled());
            }
            if (fragmentAction.contentEquals(getString(R.string.pauseVideo))) {
                if (mVideoPauseToggleItem != null)
                    mVideoPauseToggleItem.setTitle(R.string.pauseVideo);
            }
            if (fragmentAction.contentEquals(getString(R.string.resume_video))) {
                if (mVideoPauseToggleItem != null)
                    mVideoPauseToggleItem.setTitle(R.string.resume_video);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set up AV call configuration parameters from user preferences
     */
    private void setMeetingConfiguration(){

        mApplication = Application.getInstance(this.getBaseContext());

        mApplication.getConfigurationManager().setEndUserAcceptedVideoLicense();

        mApplication.getConfigurationManager().enablePreviewFeatures(false);

        mApplication.getConfigurationManager().setRequireWiFiForAudio(false);

        mApplication.getConfigurationManager().setRequireWiFiForVideo(false);

        mApplication.getConfigurationManager().setMaxVideoChannelCount(5);

    }

    /**
     * Connect to an existing Skype for Business meeting with the URI you get
     * from a server-side UCWA-based web service.
     */
    private void joinTheCall(String meetingUrl){
        try {
            setMeetingConfiguration();

            AnonymousSession mAnonymousSession = mApplication
                    .joinMeetingAnonymously(getString(R.string.userDisplayName), new URI(meetingUrl));

            mConversation = mAnonymousSession.getConversation();

            if (mConversation != null)
                mConversation.addOnPropertyChangedCallback(new ConversationPropertyChangeListener());


        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            Log.e("SkypeCall", "On premise meeting uri syntax error");
        } catch (SFBException e) {
            e.printStackTrace();
            Log.e("SkypeCall", "exception on start to join meeting");
        } catch (Exception e) {
            Log.e("SkypeCall", "Exception");
            e.printStackTrace();
        }

    }

    /**
     * Callback implementation for listening for conversation property changes.
     */
    class ConversationPropertyChangeListener extends Observable.OnPropertyChangedCallback {

        ConversationPropertyChangeListener() {
        }

        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            Conversation conversation = (Conversation) sender;
            if (propertyId == Conversation.STATE_PROPERTY_ID) {
                if (conversation.getState() == Conversation.State.ESTABLISHED) {
                    Log.e("SkypeCall", conversation.getMeetingInfo().getMeetingDescription()+ " is established");
                    try {
                        loadCallFragment();
                        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        Log.e("SkypeCall", "exception on meeting started");
                    }
                }
            }
        }
    }
}
