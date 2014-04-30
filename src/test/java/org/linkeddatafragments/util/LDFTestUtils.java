package org.linkeddatafragments.util;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ldevocht on 4/29/14.
 */
public class LDFTestUtils {
    public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static List<String> readFiles(String directoryPath) {
        List<String> fileContents = Lists.newArrayList();
        Iterator it = FileUtils.iterateFiles(new File(directoryPath), null, false);
        while(it.hasNext()){
            File file = ((File) it.next());
            System.out.println(file.getName());
            try{
                fileContents.add(readFile(file.getPath(),Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileContents;

    }
}
