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
#include <SuperpoweredReverb.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>


#define HEADROOM_DECIBEL 3.0f

using namespace std;


static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025f);

class Project {
public:

    Project(unsigned int samplerate, unsigned int buffersize, const char *path, int fileAoffset, int fileAlength, int fileBoffset, int fileBlength);
    ~Project();

    bool process(short int *output, unsigned int numberOfSamples);
    void onPlayPause(bool play);
    void onCrossfader(int value);
    void onFxSelect(int value);
    void onFxOff();
    void onFxValue(int value);
    void onEQBand(unsigned int index, int gain);

private:
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA, *playerB, *playerC, *playerD;
    SuperpoweredReverb *reverb;
    string projectName;
    string ownerID;
    double projectLength;
    double currentTime;
    double lastEdited;

    float *stereoBuffer;

    int volA, volB, volC, volD;
    int panA, panB, panC, panD;
    int verbWetA, verbWetB, verbWetC, verbWetD;
    bool muteA, muteB, muteC, muteD;
    bool soloA, soloB, soloC, soloD;

//void setTrackSettings(int loadGain, int loadPan, int loadVerbWet, bool loadMute, bool loadSolo);

};


#endif //POCKETSTUDIO_PROJECTEDITOR_H
