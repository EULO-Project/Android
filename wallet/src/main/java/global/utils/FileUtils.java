package global.utils;

import java.io.File;

/**
 * Created by EULO Developer team on 2019/1/21.
 */

public class FileUtils {

    public static void delete(File file){
        if(!file.delete()){
            file.listFiles()[0].delete();
            delete(file);
        }
    }
}
