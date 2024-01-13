import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class FolderMover {

    /**
     * The main method to initiate the process of moving folders from a source directory to a destination directory.
     *
     * Replace the sourceDirectory variable with the directory containing the all the photos and folders containing photos that you wish to sort.
     * Replace the destinationDirectory variable with the directory you wish to have your sorted photo folders.
     * @param args Command line arguments (not used in this implementation).
     */
    public static void main(String[] args) {
        Path sourceRoot = Paths.get("path-to-source-directory"); // Replace with your desired source directory
        Path destinationRoot = Paths.get("path-to-source-directory"); // Replace with your desired destination directory.

        try {
            Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Skip the source root directory
                    if (dir.equals(sourceRoot)) {
                        return FileVisitResult.CONTINUE;
                    }

                    // Check if the directory exists before moving
                    if (Files.exists(dir)) {
                        // Extract year from the directory name (assuming the year is at the end)
                        String dirName = dir.getFileName().toString();
                        String year = dirName.substring(dirName.length() - 4);

                        // Create destination directory with just the year
                        Path destinationDir = destinationRoot.resolve(year);

                        // Create destination directory if it doesn't exist
                        if (Files.notExists(destinationDir)) {
                            Files.createDirectories(destinationDir);
                        }

                        // Move the directory
                        Files.move(dir, destinationDir.resolve(dir.getFileName()));
                    } else {
                        System.err.println("Directory not found: " + dir);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Folders Moved Successfully!");
    }
}
