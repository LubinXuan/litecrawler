package me.robin.crawler.push;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Created by Lubin.Xuan on 2017-07-07.
 * {desc}
 */
public class SerializationUtil {
    public static <T> T fromFile(File file, Supplier<T> supplier) {
        if (!file.exists()) {
            return supplier.get();
        } else {
            try {
                byte[] data = FileUtils.readFileToByteArray(file);
                return SerializationUtils.deserialize(data);
            } catch (Exception e) {
                return supplier.get();
            }
        }
    }

    public static <T extends Serializable> void save(File file, T t) throws IOException {
        if (null == t) {
            return;
        }
        FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize(t));
    }
}
