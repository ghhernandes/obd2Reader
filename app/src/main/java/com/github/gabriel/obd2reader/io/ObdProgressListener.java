package com.github.gabriel.obd2reader.io;


/**
 * TODO put description
 */
public interface ObdProgressListener {

    void stateUpdate(final ObdCommandJob job);

}