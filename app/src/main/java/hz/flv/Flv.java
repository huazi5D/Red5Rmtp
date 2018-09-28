package hz.flv;

import android.media.MediaCodec;
import android.util.Log;

import java.nio.ByteBuffer;

public class Flv {

    public void findNals(ByteBuffer h264, MediaCodec.BufferInfo info) {
        int j = 0;
        for (int i = h264.position(); i < info.offset + info.size - 3; i ++) {
            if (h264.get(i) == 0x00 && h264.get(i + 1) == 0x00 && h264.get(i + 2) == 0x00 && h264.get(i + 3) == 0x01) {
                j ++;
                Log.d("zhx", "findNals: " + j);
            }
        }
    }

    public void pushSpsPps() {
        int size = 9 + 4;// 9代表flv文件头大小 4代表上一个TAG长度
        ByteBuffer headerBuffer = ByteBuffer.allocate(size);
        /**
         *  Flv Header在当前版本中总是由9个字节组成。
         *  第1-3字节为文件标识（Signature），总为“FLV”（0x46 0x4C 0x56），如图中紫色区域。
         *  第4字节为版本，目前为1（0x01）。
         *  第5个字节的前5位保留，必须为0。
         *  第5个字节的第6位表示是否存在音频Tag。
         *  第5个字节的第7位保留，必须为0。
         *  第5个字节的第8位表示是否存在视频Tag。
         *  第6-9个字节为UI32类型的值，表示从File Header开始到File Body开始的字节数，版本1中总为9。
         */
        byte[] signature = new byte[] {'F', 'L', 'V'};  /* always "FLV" */
        byte version = (byte) 0x01;     /* should be 1 */
        byte videoFlag = (byte) 0x01;
        byte audioFlag = 0x00;
        byte flags = (byte) (videoFlag | audioFlag);  /* 4, audio; 1, video; 5 audio+video.*/
        byte[] offset = new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09};  /* always 9 */

        headerBuffer.put(signature);
        headerBuffer.put(version);
        headerBuffer.put(flags);
        headerBuffer.put(offset);

        // 写入上一个TAG长度
        headerBuffer.putInt(0);

        // TODO sendrtmp


        byte[] metadata = new byte[]{0x02, 0x00, 0x0a, 'o', 'n', 'M', 'e', 't', 'a', 'D', 'a', 't', 'a'};
        byte[] mapHead = new byte[]{0x08, 0x00, 0x00, 0x00, 0x04};
        byte[] width = new byte[]{0x05, 'w', 'i', 'd', 't', 'h', 0x00};
        byte[] widthdata = ByteBuffer.allocate(8).putDouble(100).array();

        byte[] height = new byte[]{0x05, 'w', 'i', 'd', 't', 'h', 0x00};
        byte[] heightdata = ByteBuffer.allocate(8).putDouble(100).array();

        byte[] framerate = new byte[]{0x05, 'w', 'i', 'd', 't', 'h', 0x00};
        byte[] frameratedata = ByteBuffer.allocate(8).putDouble(100).array();

        byte[] videocodecid = new byte[]{0x05, 'w', 'i', 'd', 't', 'h', 0x00};
        byte[] videocodeciddata = ByteBuffer.allocate(8).putDouble(100).array();

        /**
         * 第1个byte为记录着tag的类型，音频（0x8），视频（0x9），脚本（0x12）；
         * 第2-4bytes是数据区的长度，UI24类型的值，也就是tag data的长度；注：这个长度等于最后的Tag Size-11
         * 第5-7个bytes是时间戳，UI24类型的值，单位是毫秒，类型为0x12脚本类型数据，则时间戳为0，时间戳控制着文件播放的速度，可以根据音视频的帧率类设置；
         * 第8个byte是扩展时间戳，当24位数值不够时，该字节作为最高位将时间戳扩展为32位值；
         * 第9-11个bytes是streamID，UI24类型的值，但是总为0；
         * tag header 长度为1+3+3+1+3=11。
         */
        /*int sizeAndType = (dataSize & 0x00FFFFFF) | ((type & 0x1F) << 24);
        buffer.putInt(sizeAndType);
        int time = ((timestamp << 8) & 0xFFFFFF00) | ((timestamp >> 24) & 0x000000FF);
        buffer.putInt(time);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 0);*/

    }

}
