package net.gliby.voicechat.client.sound.custom;

import net.gliby.voicechat.client.sound.ClientStreamManager;

import javax.sound.sampled.*;
import java.util.ArrayList;

public class AudioOutput extends Thread {

    private ArrayList<byte[]> queue = new ArrayList<>(); //queue of messages to be played
    private SourceDataLine speaker = null; //speaker

    public void addToQueue(byte[] data) { //adds a message to the play queue
        queue.add(data);
    }

    @Override
    public void run() {
        try {
            //open channel to sound card
            AudioFormat af = ClientStreamManager.getUniversalAudioFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);


            speaker.open(af);
            speaker.start();
            //sound card ready
            while (true) { //this infinite cycle checks for new packets to be played in the queue, and plays them. to avoid busy wait, a sleep(10) is executed at the beginning of each iteration

                if (queue.isEmpty()) { //nothing to play, wait
                    //speaker.stop();
                    speaker.stop();
                    speaker.start();
                    Thread.sleep(10);
                } else { //we got something to play
                    byte[] in = queue.get(0);
                    queue.remove(in);

                    if (in != null) {
                        //speaker.start();

                        speaker.write(in, 0, in.length);
                    }
                }
            }
        } catch (Exception e) { //sound card error or connection error, stop
            if (speaker != null) {
                speaker.close();
            }
            interrupt();
        }
    }
}

