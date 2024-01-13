import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.quicktime.QuickTimeMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import com.groupdocs.metadata.QuickTimeMetadata;
import com.groupdocs.metadata.internal.a.F;

import java.io.*;

/**
 * The PhotoTester class demonstrates how to read metadata from an image or video file.
 * ALlows you to test a particular file and get the MetaData by tag
 *
 * @author SaiBalaji Nagarajan and Drew Noakes
 * @version 1.1
 */
public class PhotoTester {


    /**
     * The main method of the PhotoTester class.
     *
     * Replace the photoFile string parameter with the path to the file you would like to test.
     * @param args The command line arguments.
     * @throws ImageProcessingException If an error occurs during image processing.
     * @throws IOException              If an I/O error occurs.
     */
    public static void main(String[] args) throws ImageProcessingException, IOException {
        File photoFile = new File("path-to-file"); // Replace with Path to file
        Metadata metadata = ImageMetadataReader.readMetadata(photoFile);
        QuickTimeVideoDirectory directory = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);
        for (Directory dir : metadata.getDirectories()) {
            System.out.println(dir.getName());
            for (Tag tag : dir.getTags()) {
                System.out.println(tag);
            }
        }

        InputStream fileInput = new FileInputStream(photoFile);
        BufferedInputStream buffered = new BufferedInputStream(fileInput);
        FileType fileType = FileTypeDetector.detectFileType(buffered);

        if (fileType == FileType.Jpeg) {
            System.out.println("JPEG");
        } else if (fileType == FileType.Png) {
            System.out.println("PNG");
        } else if (fileType == FileType.QuickTime) {
            System.out.println("QuickTime");
        }
        System.out.println(directory);
    }
}
