import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.avi.AviDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.groupdocs.metadata.internal.c.a.s.internal.jp.FI;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for organizing photos based on their metadata. (EXIF Creation Date)
 *
 * @author SaiBalaji Nagarajan (saibalaji@gatech.edu) Made using the Metadata Library developed by Drew Noakes
 * @version 1.0
 */
public class PhotoOrganizer {

    /**
     * The main method to initiate the photo organizing process.
     *
     * Replace the sourceDirectory variable with the directory containing the all the photos and folders containing photos that you wish to sort.
     * Replace the destinationDirectory variable with the directory you wish to have your sorted photo folders.
     *
     * @param args Command line arguments (not used in this implementation).
     */
    public static void main(String[] args) {
        String sourceDirectory = "D:\\Organized Folder"; // Replace with your desired source directory
        String destinationDirectory = "D:\\FINAL"; // Replace with your desired destination directory.
        organizePhotos(sourceDirectory, destinationDirectory);
        System.out.println("All supported files are in correct destination folder!");
    }

    /**
     * Retrieves the date of a photo from its metadata.
     *
     * @param photoFile The file representing the photo.
     * @return The date when the photo was taken or created.
     * @throws IOException If an I/O error occurs.
     * @throws ParseException If an error occurs while parsing the date.
     * @throws ImageProcessingException If an error occurs during image processing.
     */
    public static Date getPhotoDate(File photoFile) throws IOException, ParseException, ImageProcessingException {
        Metadata metadata = ImageMetadataReader.readMetadata(photoFile);

        InputStream fileInput = new FileInputStream(photoFile);
        BufferedInputStream buffered = new BufferedInputStream(fileInput);
        FileType fileType = FileTypeDetector.detectFileType(buffered);

        Directory directory;
        Date date;
        if (fileType == FileType.QuickTime) {
            System.out.println("Moving QuickTime File");
            directory = metadata.getFirstDirectoryOfType(QuickTimeMetadataDirectory.class);
            if (directory == null) {
                fileInput.close();
                buffered.close();
                return getDateWithNullDirectory(photoFile);
            }
            date = directory.getDate(QuickTimeMetadataDirectory.TAG_CREATION_DATE);
        } else if (fileType == FileType.Jpeg || fileType == FileType.Png) {
            String fileEnd = fileType.name();
            System.out.println("Moving " + fileEnd + " File");
            directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory == null) {
                fileInput.close();
                buffered.close();
                return getDateWithNullDirectory(photoFile);
            }
            date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        } else if (fileType == FileType.Mp4) {
            System.out.println("Moving mp4 File");
            directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
            if (directory == null) {
                fileInput.close();
                buffered.close();
                return getDateWithNullDirectory(photoFile);
            }
            date = directory.getDate(Mp4Directory.TAG_CREATION_TIME);
        } else if (fileType == FileType.Avi) {
            System.out.println("Moving AVI File");
            directory = metadata.getFirstDirectoryOfType(AviDirectory.class);
            if (directory == null) {
                fileInput.close();
                buffered.close();
                return getDateWithNullDirectory(photoFile);
            }
            date = directory.getDate(AviDirectory.TAG_DATETIME_ORIGINAL);

        } else if (fileType == FileType.Heif) {
            System.out.println("Moving Heic File");
            directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory == null) {
                fileInput.close();
                buffered.close();
                return getDateWithNullDirectory(photoFile);
            }
            date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        } else {
            String fileEnd = fileType.name();
            System.out.printf("Files of type %s are not yet supported%n Please contact Sai Nagarajan at saibalaji@gatech.edu for support%n", fileEnd);
            return null;
        }

        fileInput.close();
        buffered.close();

        if (date == null) {
            return getDateWithNullDirectory(photoFile);
        }
        return date;
    }

    /**
     * Gets the date of a photo in case the metadata directory is null.
     *
     * @param photoFile The file representing the photo.
     * @return The date when the photo was taken or created using file attributes.
     * @throws IOException If an I/O error occurs.
     */
    public static Date getDateWithNullDirectory (File photoFile) throws IOException {
        System.out.println("No EXIF Date Able to Be Extracted or Seen on " + photoFile + " Will Take Earliest Known Creation or Modified Date");
        BasicFileAttributes attr = Files.readAttributes(photoFile.toPath(), BasicFileAttributes.class);
        FileTime creationDate = attr.creationTime();
        FileTime modifiedDate = attr.lastModifiedTime();

        if (creationDate.compareTo(modifiedDate) < 0) {
            return new Date(creationDate.toMillis());
        } else {
            return new Date(modifiedDate.toMillis());
        }
    }

    /**
     * Organizes photos from a source directory to a destination directory based on their date.
     *
     * @param sourceDirectory      The path of the source directory.
     * @param destinationDirectory The path of the destination directory.
     */
    public static void organizePhotos(String sourceDirectory, String destinationDirectory) {
        Path sourcePath = Path.of(sourceDirectory);
        Path destinationPath = Path.of(destinationDirectory);

        try {
            Files.walk(sourcePath).filter(Files::isRegularFile).forEach(file -> {
                String fileName = file.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                        || fileName.endsWith(".heic") || fileName.endsWith(".mp4") || fileName.endsWith(".mov") || fileName.endsWith(".avi")) {
                    Date dateTaken;
                    try {
                        dateTaken = getPhotoDate(file.toFile());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ParseException e) {
                        System.out.printf("%s in %s is a Corrupted File. Please Delete if it is not moved to a Corrupted Files Folder in the Source Folder.%n", file.getFileName(), file);
                        moveCorruptedFile(sourcePath, destinationPath);
                        throw new RuntimeException(e);
                    } catch (ImageProcessingException e) {
                        System.out.printf("%s in %s is a Corrupted File. Please Delete if it is not moved to a Corrupted Files Folder in the Source Folder.%n", file.getFileName(), file);
                        moveCorruptedFile(sourcePath, destinationPath);
                        throw new RuntimeException(e);
                    }

                    if (dateTaken != null) {
                        String yearMonthFolder = new SimpleDateFormat("MMMM yyyy").format(dateTaken);
                        Path destinationFolderPath = destinationPath.resolve(yearMonthFolder);

                        try {
                            Files.createDirectories(destinationFolderPath);

                            Path destinationFilePath = destinationFolderPath.resolve(file.getFileName());
                            Files.move(file, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Moved " + file + " to " + destinationFilePath);

                        } catch (IOException e) {
                            // Handle IOException
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            System.out.println("Process Finished/Directory is empty");
                        }
                    }
                } else {
                    System.out.printf("Files of the type that is %s are not yet supported%n Please contact Sai Nagarajan at saibalaji@gatech.edu for support%n", fileName);
                }
            });
        } catch (IOException e) {
            System.out.println("File not Found");
            e.printStackTrace();
        }
    }

    /**
     * Moves a corrupted file to a "Corrupted Files" folder.
     *
     * @param sourceFilePath      The path of the corrupted file.
     * @param destinationDirectory The path of the destination directory.
     */
    public static void moveCorruptedFile(Path sourceFilePath, Path destinationDirectory) {
        try {
            Path corruptedFilesFolder = destinationDirectory.resolve("Corrupted Files");

            // Create "Corrupted Files" folder if it doesn't exist
            if (Files.notExists(corruptedFilesFolder)) {
                Files.createDirectories(corruptedFilesFolder);
            }

            // Move the corrupted file to the "Corrupted Files" folder
            Path destinationFilePath = corruptedFilesFolder.resolve(sourceFilePath.getFileName());
            Files.move(sourceFilePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Moved corrupted file " + sourceFilePath + " to " + destinationFilePath);
        } catch (IOException e) {
            System.err.println("Error moving corrupted file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
