//
// Created by Matt Eisenberg on 3/19/17.
//

#ifndef POCKETSTUDIO_PROJECTEDITOR_H
#define POCKETSTUDIO_PROJECTEDITOR_H

#include <math.h>
#include <pthread.h>
#include <string>

#include "ProjectEditor.h"
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredFilter.h>
#include <SuperpoweredRoll.h>
#include <SuperpoweredFlanger.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>


#define HEADROOM_DECIBEL 3.0f

using namespace std;


static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025f);

class SuperpoweredExample {
public:

    SuperpoweredExample(unsigned int samplerate, unsigned int buffersize, const char *path, int fileAoffset, int fileAlength, int fileBoffset, int fileBlength);
    ~SuperpoweredExample();

    bool process(short int *output, unsigned int numberOfSamples);
    void onPlayPause(bool play);
    void onCrossfader(int value);
    void onFxSelect(int value);
    void onFxOff();
    void onFxValue(int value);
    void onEQBand(unsigned int index, int gain);

private:
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA, *playerB;
    SuperpoweredRoll *roll;
    SuperpoweredFilter *filter;
    SuperpoweredFlanger *flanger;
    float *stereoBuffer;
    unsigned char activeFx;
    float crossValue, volA, volB;
};

class Project {
public:
    //Constructor
    Project();

    //Tracks are important I guess
    class Track {
    public:

    private:
        int gain, pan, verbWet;
        bool mute, solo;
        void setTrackSettings(int loadGain, int loadPan, int loadVerbWet, bool loadMute, bool loadSolo);
    };


private:
    //Project variables
    string projectName;
    string ownerID;
    double projectLength;
    double currentTime;
    double lastEdited;

};

#endif //POCKETSTUDIO_PROJECTEDITOR_H
