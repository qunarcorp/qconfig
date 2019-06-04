package qunar.tc.qconfig.common.codec;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * 编解码注册器
 *
 * Created by chenjk on 2017/8/30.
 */
public class CodecRegister {

    private static final Map<String, Codec> CODEC_MAP = Maps.newHashMap();

    public static final CodecRegister register = new CodecRegister();

    private CodecRegister () {
        init();
    }

    private void init() {
        ServiceLoader<Codec> codecs = ServiceLoader.load(Codec.class);
        if(codecs != null) {
            for(Codec codec : codecs) {
                CODEC_MAP.put(codec.name(), codec);
            }
        }
    }

    public Codec getCodec(String name) {
        return CODEC_MAP.get(name);
    }

    public String joinNames(){
        Set<String> codecSet = Sets.newHashSet();
        for(Codec codec : CODEC_MAP.values()) {
            codecSet.add(codec.name());
        }
        return Joiner.on(",").join(codecSet);
    }
}
