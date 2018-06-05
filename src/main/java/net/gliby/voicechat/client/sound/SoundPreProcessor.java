package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.debug.Statistics;
import net.gliby.voicechat.client.sound.custom.AudioOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.xiph.speex.SpeexDecoder;

import javax.sound.sampled.FloatControl;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundPreProcessor {

    VoiceChatClient voiceChat;
    AudioOutput output;
    Statistics stats;
    SpeexDecoder decoder;
    byte[] buffer;


    public SoundPreProcessor(VoiceChatClient voiceChat, Minecraft mc) {
        this.voiceChat = voiceChat;
        this.stats = VoiceChatClient.getStatistics();
        this.output = new AudioOutput();
        output.start();
    }

    public static List<byte[]> divideArray(byte[] source, int chunksize) {
        ArrayList<byte[]> result = new ArrayList<byte[]>();

        for (int start = 0; start < source.length; start += chunksize) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
        }

        return result;
    }

    public boolean process(int id, byte[] encodedSamples, int chunkSize, boolean direct) {
        if (chunkSize > encodedSamples.length) {
            VoiceChatClient.getLogger().fatal("Sound Pre-Processor has been given incorrect data from network, sample pieces cannot be bigger than whole sample. ");
            return false;
        } else {
            if (this.decoder == null) {
                this.decoder = new SpeexDecoder();
                this.decoder.init(0, (int) ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
            }


            byte[] decodedData;
            if (encodedSamples.length <= chunkSize) {
                try {
                    this.decoder.processData(encodedSamples, 0, encodedSamples.length);
                } catch (StreamCorruptedException var12) {
                    var12.printStackTrace();
                    return false;
                }

                decodedData = new byte[this.decoder.getProcessedDataByteSize()];
                this.decoder.getProcessedData(decodedData, 0);
            } else {
                List<byte[]> samplesList = divideArray(encodedSamples, chunkSize);
                this.buffer = new byte[0];

                for (int i = 0; i < samplesList.size(); ++i) {
                    byte[] sample = samplesList.get(i);
                    SpeexDecoder tempDecoder = new SpeexDecoder();
                    tempDecoder.init(0, (int) ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());

                    try {
                        this.decoder.processData(sample, 0, sample.length);
                    } catch (StreamCorruptedException var11) {
                        var11.printStackTrace();
                        return false;
                    }

                    byte[] sampleBuffer = new byte[this.decoder.getProcessedDataByteSize()];
                    this.decoder.getProcessedData(sampleBuffer, 0);
                    this.write(sampleBuffer);
                }

                decodedData = this.buffer;
            }

            if (decodedData != null) {
                Entity speaker = Minecraft.getMinecraft().world.getEntityByID(id);
                if (speaker == null) return false;

                EntityPlayerSP target = Minecraft.getMinecraft().player;

                double d4 = speaker.posX - target.posX;
                double d5 = speaker.posY - target.posY;
                double d6 = speaker.posZ - target.posZ;
                double distanceBetween = d4 * d4 + d5 * d5 + d6 * d6;

                int distance = this.voiceChat.getSettings().getSoundDistance();
                int fade = this.voiceChat.getSettings().getFade();

                if (!(distanceBetween < (distance * distance))) {
                    double inFade = Math.sqrt(distanceBetween) - distance;
                    double loudLeft = (1.0F - inFade / (double) fade);

                    decodedData = adjustVolume(decodedData, (float) loudLeft);

                }
                else {
                    decodedData = adjustVolume(decodedData, 1.0F);
                }


                //VoiceChatClient.getSoundManager().addQueue(decodedData, direct, id); // error source
                output.addToQueue(decodedData); //This will work but all features need to be recoded to work, this is dirty
                if (this.stats != null) {
                    this.stats.addEncodedSamples(encodedSamples.length);
                    this.stats.addDecodedSamples(decodedData.length);
                }

                this.buffer = new byte[0];
                return true;
            } else {
                return false;
            }
        }
    }


    private byte[] adjustVolume(byte[] audioSamples, float volume) {
        float finiteVolume = volume * this.voiceChat.getSettings().getWorldVolume();
        byte[] array = new byte[audioSamples.length];
        for (int i = 0; i < array.length; i+=2) {
            // convert byte pair to int
            short buf1 = audioSamples[i+1];
            short buf2 = audioSamples[i];

            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);

            short res= (short) (buf1 | buf2);
            res = (short) (res * finiteVolume);

            // convert back
            array[i] = (byte) res;
            array[i+1] = (byte) (res >> 8);

        }
        return array;
    }

    private void write(byte[] write) {
        byte[] result = new byte[this.buffer.length + write.length];
        System.arraycopy(this.buffer, 0, result, 0, this.buffer.length);
        System.arraycopy(write, 0, result, this.buffer.length, write.length);
        this.buffer = result;
    }
}
