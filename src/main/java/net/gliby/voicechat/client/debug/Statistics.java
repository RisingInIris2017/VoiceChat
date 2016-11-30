package net.gliby.voicechat.client.debug;

import net.gliby.voicechat.client.sound.MovingAverage;

public class Statistics {

    MovingAverage decodedAverage = new MovingAverage(8);
    MovingAverage encodedAverage = new MovingAverage(8);
    int encodedSum;
    int decodedSum;


    public void addDecodedSamples(int size) {
        this.decodedSum += size;
        this.decodedAverage.add(size);
    }

    public void addEncodedSamples(int size) {
        this.encodedSum += size;
        this.encodedAverage.add(size);
    }

    public int getDecodedAverageDataReceived() {
        return this.decodedAverage.getAverage().intValue();
    }

    public int getDecodedDataReceived() {
        return this.decodedSum;
    }

    public int getEncodedAverageDataReceived() {
        return this.encodedAverage.getAverage().intValue();
    }

    public int getEncodedDataReceived() {
        return this.encodedSum;
    }
}
